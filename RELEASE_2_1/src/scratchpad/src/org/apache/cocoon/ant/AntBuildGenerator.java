/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.ant;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *  A Cocoon Generator that runs an Ant build file
 *
 * @author <a href="mailto:ceyates@stanford.edu">Charles Yates</a>
 * @version CVS $Id: AntBuildGenerator.java,v 1.1 2003/06/03 14:19:56 cziegeler Exp $
 */
public class AntBuildGenerator
    extends AbstractLogEnabled
    implements
        Generator,
        BuildListener,
        Parameterizable,
        Initializable,
        Contextualizable,
        ThreadSafe {

    /** various String constants. */
    private static final String LOG_PARAM_NAME = "log-level";
    private static final String DEFAULT_LOG_PARAM_NAME = "default-log-level";
    private static final String BUILD_FILE_PARAM_NAME = "build-file";
    private static final String DEFAULT_BUILD_FILE = "WEB-INF/build.xml";
    private static final String BUILD = "build";
    private static final String TARGET = "target";
    private static final String TASK = "task";
    private static final String MESSAGE = "message";
    private static final String NAME = "name";
    private static final String PRIORITY = "priority";
    private static final String ERROR = "error";
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String VERBOSE = "verbose";
    private static final String DEBUG = "debug";
    private static final String CDATA = "CDATA";
    private static final String EMPTY_STRING = "";

    /** Attributes stuff for output */
    private static final AttributesImpl EMPTY_ATTRS = new AttributesImpl();
    private static final AttributesImpl MSSG_ATTRS = new AttributesImpl();
    private static final AttributesImpl NAME_ATTRS = new AttributesImpl();
    static {
        MSSG_ATTRS.addAttribute(EMPTY_STRING, PRIORITY, PRIORITY, CDATA, null);
        NAME_ATTRS.addAttribute(EMPTY_STRING, NAME, NAME, CDATA, null);
    }

    /** the build file */
    private File myBuildFile;

    /** the name of the build file */
    private String myBuildFileName;

    /** the context for resolving path of build file */
    private HttpContext myContext;

    /** default priority level for when level is not passed as parameter in the request */
    private int myDefaultPriorityLevel;

    /** holds an exception if one occured during processing */
    private SAXException mySAXException;

    /** the content/lexical handler */
    private XMLConsumer myConsumer;

    /** the message priority level */
    private int myPriorityLevel;

    /** a stack to hold element names so the SAXEvents can be ended correctly in case of an exception */
    private Stack myStack = new Stack();

    /** the current request's target */
    private String target;

    /** ThreadLocal data for priority and target so threads don't stomp on each other */
    private ThreadLocal myThreadPriorityLevel;
    private ThreadLocal myThreadTarget;

    /**
     * gets the HttpContext for resolving the path to build.xml.
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context aContext) throws ContextException {
        myContext =
            (HttpContext) aContext.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * gets the default values.
     * parameters:
     * &lt;parameter name="default-log-level" value="info"/&gt;
     * &lt:parameter name="build-file" value="WEB-INF/build.xml"/&gt;
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters aParams) throws ParameterException {
        myDefaultPriorityLevel =
            aParams.getParameterAsInteger(
                DEFAULT_LOG_PARAM_NAME,
                Project.MSG_INFO);
        myBuildFileName =
            aParams.getParameter(BUILD_FILE_PARAM_NAME, DEFAULT_BUILD_FILE);
    }

    /**
     * instantiates ThreadLocal objects and finds the build file.
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        myThreadTarget = new ThreadLocal();
        myThreadPriorityLevel = new ThreadLocal();
        myBuildFile = new File(myContext.getRealPath(myBuildFileName));
    }

    /**
     * parses parameters passed with url  eg: ?target=myTarget&amp;log-level=verbose
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(
        SourceResolver aResolver,
        Map aMap,
        String aString,
        Parameters aParams)
        throws ProcessingException, SAXException, IOException {
        String requestPriority =
            ObjectModelHelper.getRequest(aMap).getParameter(LOG_PARAM_NAME);
        int priorityLevel = myDefaultPriorityLevel;
        if (requestPriority != null) {
            if (requestPriority.equals(ERROR)) {
                priorityLevel = Project.MSG_ERR;
            } else if (requestPriority.equals(WARN)) {
                priorityLevel = Project.MSG_WARN;
            } else if (requestPriority.equals(INFO)) {
                priorityLevel = Project.MSG_INFO;
            } else if (requestPriority.equals(VERBOSE)) {
                priorityLevel = Project.MSG_VERBOSE;
            } else if (requestPriority.equals(DEBUG)) {
                priorityLevel = Project.MSG_DEBUG;
            }
        }
        String target = ObjectModelHelper.getRequest(aMap).getParameter(TARGET);
        if (target == null) {
            target = EMPTY_STRING;
        }
        myThreadTarget.set(target);
        myThreadPriorityLevel.set(new Integer(priorityLevel));
    }

    /**
     * @see org.apache.cocoon.xml.XMLProducer#setConsumer(org.apache.cocoon.xml.XMLConsumer)
     */
    public void setConsumer(XMLConsumer aConsumer) {
        myConsumer = aConsumer;
    }
    /**
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public synchronized void generate()
        throws IOException, SAXException, ProcessingException {
        myPriorityLevel = ((Integer) myThreadPriorityLevel.get()).intValue();
        target = (String) myThreadTarget.get();
        try {
            myConsumer.startDocument();
            Project theProject = new Project();
            theProject.addBuildListener(this);
            theProject.fireBuildStarted();
            theProject.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(theProject, myBuildFile);
            if (target.equals(EMPTY_STRING)) {
                target = theProject.getDefaultTarget();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("executing target "+target+" with log priority level "+myPriorityLevel);
            }
            theProject.executeTarget(target);
            theProject.fireBuildFinished(mySAXException);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            while (!myStack.isEmpty()) {
                String tag = (String) myStack.pop();
                myConsumer.endElement(EMPTY_STRING, tag, tag);
            }
        } finally {
            myConsumer.endDocument();
            mySAXException = null;
        }
    }

    /**
     * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
     */
    public void buildStarted(BuildEvent anEvent) {
        try {
            myConsumer.startElement(EMPTY_STRING, BUILD, BUILD, EMPTY_ATTRS);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        } finally {
            myStack.push(BUILD);
        }
    }


    /**
     * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
     */
    public void buildFinished(BuildEvent anEvent) {
        try {
            myConsumer.endElement(EMPTY_STRING, BUILD, BUILD);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        } finally {
            myStack.pop();
        }
    }

    /**
     * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
     */
    public void targetStarted(BuildEvent anEvent) {
        if (anEvent.getPriority() > myPriorityLevel) {
            return;
        }
        try {
            NAME_ATTRS.setValue(0, anEvent.getTarget().getName());
            myConsumer.startElement(EMPTY_STRING, TARGET, TARGET, NAME_ATTRS);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        } finally {
            myStack.push(TARGET);
        }
    }
    /**
     * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
     */
    public void targetFinished(BuildEvent anEvent) {
        if (anEvent.getPriority() > myPriorityLevel) {
            return;
        }
        try {
            myConsumer.endElement(EMPTY_STRING, TARGET, TARGET);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        } finally {
            myStack.pop();
        }
    }

    /**
     * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
     */
    public void taskStarted(BuildEvent anEvent) {
        if (anEvent.getPriority() > myPriorityLevel) {
            return;
        }
        try {
            NAME_ATTRS.setValue(0, anEvent.getTask().getTaskName());
            myConsumer.startElement(EMPTY_STRING, TASK, TASK, NAME_ATTRS);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        }
        myStack.push(TASK);
    }

    /**
     * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
     */
    public void taskFinished(BuildEvent anEvent) {
        if (anEvent.getPriority() > myPriorityLevel) {
            return;
        }
        try {
            myConsumer.endElement(EMPTY_STRING, TASK, TASK);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        } finally {
            myStack.pop();
        }
    }

    /**
     * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
     */
    public void messageLogged(BuildEvent anEvent) {
        if (anEvent.getPriority() > myPriorityLevel) {
            return;
        }
        String value = DEBUG;
        switch (anEvent.getPriority()) {
            case Project.MSG_ERR :
                value = ERROR;
                break;
            case Project.MSG_WARN :
                value = WARN;
                break;
            case Project.MSG_INFO :
                value = INFO;
                break;
            case Project.MSG_VERBOSE :
                value = VERBOSE;
        }
        MSSG_ATTRS.setValue(0, value);
        String message = anEvent.getMessage();
        try {
            myConsumer.startElement(EMPTY_STRING, MESSAGE, MESSAGE, MSSG_ATTRS);
            myConsumer.startCDATA();
            myConsumer.characters(message.toCharArray(), 0, message.length());
            myConsumer.endCDATA();
            myConsumer.endElement(EMPTY_STRING, MESSAGE, MESSAGE);
        } catch (SAXException e) {
            getLogger().error(e.getMessage(), e);
            if (mySAXException == null) {
                mySAXException = e;
            }
        }
    }
}