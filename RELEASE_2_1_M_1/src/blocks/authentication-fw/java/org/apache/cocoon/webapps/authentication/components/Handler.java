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
package org.apache.cocoon.webapps.authentication.components;

import java.io.IOException;
import java.util.*;

import org.apache.excalibur.source.SourceParameters;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Request;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.xml.sax.SAXException;

/**
 * The authentication Handler.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: Handler.java,v 1.1 2003/03/09 00:02:18 pier Exp $
*/
public final class Handler
implements java.io.Serializable {

    /** The unique name of the handler */
    private final String name;

    /** The redirect-to URI */
    private String redirectURI;

    /** The redirect parameters */
    private SourceParameters redirectParameters;

    /** The authentication resource */
    private String authenticationResource;

    /** The authentication resource parameters */
    private SourceParameters authenticationResourceParameters;

    /** The load resource (optional) */
    private String loadResource;

    /** The load resource (optional) parameters */
    private SourceParameters loadResourceParameters;

    /** The save resource (optional) */
    private String saveResource;

    /** The save resource (optional) parameters */
    private SourceParameters saveResourceParameters;

    /** The ApplicationHandler */
    private Map applications = new Hashtable(3, 2);

    /** The handler contexts */
    private List handlerContexts = new ArrayList(2);

    /** Are all apps loaded? */
    private boolean appsLoaded = false;

    /** The configuration fragments */
    private Map configurations;

    /**
     * Create a new handler object.
     */
    public Handler(String name) {
        this.name = name;
        this.configurations = new HashMap(3, 2);
    }

    /**
     * Make a copy
     */
    public Handler copy() {
        final Handler copy = new Handler(this.name);
        copy.redirectURI = this.redirectURI;
        copy.redirectParameters = this.redirectParameters;
        copy.authenticationResource = this.authenticationResource;
        copy.authenticationResourceParameters = this.authenticationResourceParameters;
        copy.loadResource = this.loadResource;
        copy.saveResource = this.saveResource;
        copy.loadResourceParameters = this.loadResourceParameters;
        copy.saveResourceParameters = this.saveResourceParameters;
        copy.configurations = this.configurations;
        Iterator iter = this.applications.keySet().iterator();
        while (iter.hasNext()) {
            final String name = (String)iter.next();
            copy.applications.put(name, ((ApplicationHandler)this.applications.get(name)).copy(copy));
        }
        return copy;
    }

    /**
     * Configure
     */
    public void configure(SourceResolver resolver,
                          Request        request,
                          Configuration  conf)
    throws ProcessingException, SAXException, IOException, ConfigurationException {
        // get login (required)
        Configuration child = conf.getChild("redirect-to", false);
        if (child == null)
            throw new ConfigurationException("Handler '"+this.name+"' needs a redirect-to URI.");
        this.redirectURI = child.getAttribute("uri");
        if ( this.redirectURI.startsWith("cocoon:") ) {
            final int pos = this.redirectURI.indexOf('/');
            if ( pos != -1 && this.redirectURI.length() > pos) {
                if (this.redirectURI.charAt(pos+1) == '/') {
                    this.redirectURI = this.redirectURI.substring(pos+2).trim();
                    this.redirectURI = request.getContextPath()+"/"+this.redirectURI;
                } else {
                    this.redirectURI = this.redirectURI.substring(pos+1).trim();
                }
            }
        }

        this.redirectParameters = SourceParameters.create(child);

        // get load resource (required)
        child = conf.getChild("authentication", false);
        if (child == null)
            throw new ConfigurationException("Handler '"+this.name+"' needs authentication configuration");
        this.authenticationResource = child.getAttribute("uri");
        this.authenticationResourceParameters = SourceParameters.create(child);

        // get load resource (optional)
        child = conf.getChild("load", false);
        if (child != null) {
            this.loadResource = child.getAttribute("uri");
            this.loadResourceParameters = SourceParameters.create(child);
        }

        // get save resource (optional)
        child = conf.getChild("save", false);
        if (child != null) {
            this.saveResource = child.getAttribute("uri");
            this.saveResourceParameters = SourceParameters.create(child);
        }

        // And now: Applications
        child = conf.getChild("applications", false);
        if (child != null) {
            Configuration[] appConfs = child.getChildren("application");
            Configuration appconf;

            if (appConfs != null) {
                for(int i = 0; i < appConfs.length; i++) {
                    appconf = appConfs[i];

                    // get name
                    String appName = appconf.getAttribute("name");

                    // test if handler is unique
                    if (this.applications.get(appName) != null) {
                        throw new ConfigurationException("Application names must be unique: " + appName);
                    }

                    // create handler
                    ApplicationHandler apphandler = new ApplicationHandler(this, appName);

                    // store handler
                    this.applications.put(appName, apphandler);

                    // configure
                    apphandler.configure(resolver, appconf);
                }
            }
        }

        // get configurations (optional)
        Configuration[] configurations = conf.getChildren("configuration");
        if (configurations != null) {
            for(int i = 0; i < configurations.length; i++) {
                child = configurations[i];
                String value = child.getAttribute("name");
                if (this.getConfiguration(value) != null) {
                    throw new ConfigurationException("Configuration names must be unique for application " + this.name + ": " + value);
                }
                this.configurations.put(value, child);
            }
        }

    }


    /**
     * Get the handler name.
     */
    public String getName() { return name; }

    /**
     * Get the redirect URI
     */
    public String getRedirectURI() {
        return this.redirectURI;
    }

    /**
     * Get the redirect parameters
     */
    public SourceParameters getRedirectParameters() {
        return this.redirectParameters;
    }

    /**
     * Get the authentication resource
     */
    public String getAuthenticationResource() {
        return this.authenticationResource;
    }

    /**
     * Get the authentication resource
     */
    public SourceParameters getAuthenticationResourceParameters() {
        return this.authenticationResourceParameters;
    }

    /** Get the save resource */
    public String getSaveResource() { return this.saveResource; }

    /** Get the load resource */
    public String getLoadResource() { return this.loadResource; }

    /** Get the save resource */
    public SourceParameters getSaveResourceParameters() { return this.saveResourceParameters; }

    /** Get the load resource parameters */
    public SourceParameters getLoadResourceParameters() { return this.loadResourceParameters; }

    /**
     * Get the applications map
     */
    public Map getApplications() { return applications; }

    public void setApplicationsLoaded(boolean value)
    throws ProcessingException {
        this.appsLoaded = value;
    }

    public boolean getApplicationsLoaded()
    throws ProcessingException {
        if (this.applications.isEmpty() == true) {
            return true;
        } else {
            return this.appsLoaded;
        }
    }

    /**
     * Add a handler context
     */
    public void addHandlerContext(SessionContext context) {
        this.handlerContexts.add( context );
    }

    /**
     * Get handler contexts
     */
    public List getHandlerContexts() {
        return this.handlerContexts;
    }

    /**
     * Clear handler contexts
     */
    public void clearHandlerContexts() {
        this.handlerContexts.clear();
    }

    /**
     * Get the configuration
     */
    public Configuration getConfiguration(String name) {
        return (Configuration)this.configurations.get(name);
    }

    /**
     * toString()
     */
    public String toString() {
        return "authentication-Handler " + this.name;
    }
}