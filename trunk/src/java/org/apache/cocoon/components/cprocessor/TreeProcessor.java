/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.cprocessor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.LocationAugmentationPipe;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.xml.sax.ContentHandler;

/**
 * 
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @avalon.component
 * @avalon.service type=Processor
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=sitemap
 */
public class TreeProcessor extends AbstractLogEnabled 
implements Processor, Contextualizable, Serviceable, Configurable, Initializable, Disposable {

    /** Environment attribute key for redirection status communication */
    public static final String COCOON_REDIRECT_ATTR = "cocoon: redirect url";
    
    /** The sitemap namespace */
    public static final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
    
    /** The key for the processor inside the component context */
    public static final String CONTEXT_TREE_PROCESSOR = TreeProcessor.class.getName();
    
    /* The xsl transformation location for turning a 
     * sitemap into a Fortress container configuration 
     */
    private static final String SITEMAP2XCONF_URL = 
        "resource://org/apache/cocoon/components/cprocessor/sitemap2xconf.xsl";
//        "file://d:/apache/cocoon-2.2/src/java/org/apache/cocoon/components/cprocessor/sitemap2xconf.xsl";
    
    /* The parent TreeProcessor, if any */
    private TreeProcessor m_parent;
    private EnvironmentHelper m_environmentHelper;
    
    private Context m_context;
    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    
    /* The object that manages the sitemap container */
    private DefaultContainerManager m_cm;
    private SitemapContainer m_container;
    
    /* some configuration options */
    private String m_fileName;
    private boolean m_checkReload;
    private long m_lastModifiedDelay;
    
    /* the sitemap source */
    private Source m_source;
    private long m_creationTime;
    
    /* the sitemap2xconf.xsl source */
    private Source m_transform;
    
    /* The root node of the processing tree */
    private ProcessingNode m_rootNode;
    
    /** The component configurations from the sitemap (if any) */
    protected Configuration componentConfigurations;
    
    /** The different sitemap component configurations */
    protected Map sitemapComponentConfigurations;

    // ---------------------------------------------------- lifecycle
    
    public TreeProcessor() {
    }
    
    /**
     * @param parent  The parent processor
     * @param source  The sitemap
     * @param checkReload Check for changes in the sitemap?
     * @param prefix  The prefix used to mount this sitemap
     * @throws Exception
     */
    private TreeProcessor(TreeProcessor parent, Source source, boolean checkReload, String prefix) 
    throws Exception {
        m_parent = parent;
        m_transform = parent.m_transform;
        m_lastModifiedDelay = parent.m_lastModifiedDelay;
        m_checkReload = checkReload;
        m_source = source;
        m_environmentHelper = new EnvironmentHelper(parent.m_environmentHelper);
        // Setup tree processor
        ContainerUtil.enableLogging(this, parent.getLogger());
        ContainerUtil.contextualize(this, parent.m_context);
        ContainerUtil.service(this, parent.m_container.getServiceManager());
        ContainerUtil.initialize(this);
        // Setup environment helper
        ContainerUtil.enableLogging(m_environmentHelper, this.getLogger());
        ContainerUtil.service(m_environmentHelper, this.m_manager);
        m_environmentHelper.changeContext(source, prefix);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) {
        m_context = context;
    }
    
    /**
     * @avalon.dependency  type=SourceResolver
     * @avalon.dependency  type=SAXParser
     * @avalon.dependency  type=XSLTProcessor
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        m_fileName = config.getAttribute("file", null);
        m_checkReload = config.getAttributeAsBoolean("check-reload", true);
        
        // Reload check delay. Default is 1 second.
        m_lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay",1000L);
        
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // setup the environment helper
        if (m_environmentHelper == null ) {
            m_environmentHelper = new EnvironmentHelper(
                (String) m_context.get(Constants.CONTEXT_ROOT_URL));
        }
        ContainerUtil.enableLogging(m_environmentHelper,getLogger());
        ContainerUtil.service(m_environmentHelper,m_manager);
        
        // resolve the sources
        // in the case of a child processor these are already set
        // hence the null checks
        if (m_transform == null) {
            m_transform = m_resolver.resolveURI(SITEMAP2XCONF_URL);
        }
        if (m_source == null) {
            Source source = m_resolver.resolveURI(m_fileName);
            m_source = new DelayedRefreshSourceWrapper(source,m_lastModifiedDelay);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        disposeContainer();
        ContainerUtil.dispose(m_environmentHelper);
        m_environmentHelper = null;
        if (m_manager != null) {
            if (m_source != null ) {
                m_resolver.release(((DelayedRefreshSourceWrapper) m_source).getSource());
                m_source = null;
            }
            if (m_transform != null) {
                m_resolver.release(m_transform);
            }
            m_manager.release(m_resolver);
            m_resolver = null;
            m_manager = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment) throws Exception {
        InvokeContext context = new InvokeContext();
        context.enableLogging(getLogger());
        try {
            return process(environment, context);
        } finally {
            context.dispose();
        }
    }
    
    /**
     * Do the actual processing, be it producing the response 
     * or just building the pipeline.
     */
    private boolean process(Environment environment, InvokeContext context)
    throws Exception {
    
        final ProcessingNode rootNode = getRootNode();
        
        // and now process
        EnvironmentHelper.enterProcessor(this, m_manager, environment);
        
        // Build a redirector
        TreeProcessorRedirector redirector = new TreeProcessorRedirector(environment, context);
        setupLogger(redirector);
        context.setRedirector(redirector);
        try {
            boolean success = rootNode.invoke(environment, context);
            return success;
        } finally {
            EnvironmentHelper.leaveProcessor();
        }
    }
    
    private ProcessingNode getRootNode() throws Exception {
        // first, check whether we need to load the sitemap
        if (m_rootNode == null || shouldReload()) {
            setupRootNode();
        }
        return m_rootNode;
    }
    
    private boolean shouldReload() {
        return m_checkReload && m_source.getLastModified() > m_creationTime;
    }
    
    private synchronized void setupRootNode() throws Exception {

        // Now that we entered the synchronized area, recheck if
        // we still need to setup the root node
        if (m_rootNode != null && !shouldReload()) {
            return;
        }
        
        disposeContainer();
        long startTime = System.currentTimeMillis();
        createContainer();
        m_rootNode = m_container.getRootNode();
        long endTime = System.currentTimeMillis();

        if (getLogger().isDebugEnabled()) {
            double time = (endTime - startTime) / 1000.0;
            getLogger().debug("TreeProcessor built in " + time + " secs from " + m_source.getURI());
        }
        
        m_creationTime = endTime;
    }
    
    private void createContainer() throws Exception {
        // create the sitemap container
        DefaultContext context = new DefaultContext(m_context);
        context.put(TreeProcessor.CONTEXT_TREE_PROCESSOR, this);
        context.makeReadOnly();
        FortressConfig config = new FortressConfig(context);
        config.setContainerClass(SitemapContainer.class);
        config.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        config.setContainerConfiguration(buildConfiguration(m_source));
        config.setServiceManager(m_manager);
        config.setLoggerManager((LoggerManager) m_manager.lookup(LoggerManager.ROLE));
        m_cm = new DefaultContainerManager(config.getContext(),getLogger());
        m_cm.initialize();
        m_container = (SitemapContainer) m_cm.getContainer();
    }
    
    private void disposeContainer() {
        if (m_cm != null) {
            ContainerUtil.dispose(m_cm);
        }
        m_cm = null;
        m_container = null;
    }
    
    /**
     * Build the configuration from the sitemap
     * @param source The sitemap
     * @return The configuration
     * @throws Exception Any exception
     */
    private Configuration buildConfiguration(Source source) 
    throws Exception {        
        SAXParser parser = null;
        XSLTProcessor xsltProcessor = null;
        try {
            // get the SAX parser
            parser = (SAXParser) m_manager.lookup(SAXParser.ROLE);
            
            // setup the sitemap2xconf transformation handler
            xsltProcessor = (XSLTProcessor) m_manager.lookup(XSLTProcessor.ROLE);
            final TransformerHandler transformHandler = xsltProcessor.getTransformerHandler(m_transform);
            
            final NamespacedSAXConfigurationHandler configHandler = new NamespacedSAXConfigurationHandler();
            ContentHandler handler = configHandler;
            // TODO - remove the logging of the generated configuration before we go final
            // if debug is turned on, output the whole generated configuration
            DOMBuilder domBuilder = null;
            if ( this.getLogger().isDebugEnabled() ) {
                domBuilder = new DOMBuilder();
                handler = new XMLTeePipe(new ContentHandlerWrapper(handler), domBuilder);
            }
            transformHandler.setResult(new SAXResult(handler));
            
            final LocationAugmentationPipe pipe = new LocationAugmentationPipe();
            pipe.setConsumer(XMLUtils.getConsumer(transformHandler));
            
            parser.parse(SourceUtil.getInputSource(source), pipe);
            if ( domBuilder != null ) {
                this.getLogger().debug("Configuration from sitemap: " + this.m_source.getURI());
                this.getLogger().debug(XMLUtils.serializeNode(domBuilder.getDocument(), XMLUtils.createPropertiesForXML(false)));
            }
            Configuration config = configHandler.getConfiguration();
            
            this.componentConfigurations = config.getChild("pipelines-node")
                                             .getChild("component-configurations");
            return config;
        } finally {
            m_manager.release(parser);
            m_manager.release(xsltProcessor);
        }
    }
    
    private boolean handleCocoonRedirect(String uri, Environment environment, InvokeContext context) throws Exception
    {
        
        // Build an environment wrapper
        // If the current env is a facade, change the delegate and continue processing the facade, since
        // we may have other redirects that will in turn also change the facade delegate
        
        MutableEnvironmentFacade facade = environment instanceof MutableEnvironmentFacade ?
            ((MutableEnvironmentFacade)environment) : null;
        
        if (facade != null) {
            // Consider the facade delegate (the real environment)
            environment = facade.getDelegate();
        }
        
        Environment newEnv = new ForwardEnvironmentWrapper(environment, uri, getLogger());
        ((ForwardEnvironmentWrapper)newEnv).setInternalRedirect(true);
        
        if (facade != null) {
            // Change the facade delegate
            facade.setDelegate((EnvironmentWrapper)newEnv);
            newEnv = facade;
        }
        
        // Get the processor that should process this request
        TreeProcessor processor;
        if (getRootProcessor().getContext().equals(this.getContext())) {
            processor = (TreeProcessor) getRootProcessor();
        } else {
            processor = this;
        }
        
        // Process the redirect
//      No more reset since with TreeProcessorRedirector, we need to pop values from the redirect location
//             context.reset();
        return processor.process(newEnv, context);
    }
    
    public InternalPipelineDescription buildPipeline(Environment environment) throws Exception {
        InvokeContext context = new InvokeContext(true);
        context.enableLogging(getLogger());
        context.setLastProcessor(this);
        try {
            if (process(environment, context)) {
                return context.getInternalPipelineDescription(environment);
            } else {
                return null;
            }
        } finally {
            context.dispose();
        }
    }

    /**
     * Set the sitemap component configurations
     */
    protected void setComponentConfigurations(Configuration componentConfigurations) {
        this.componentConfigurations = componentConfigurations;
        this.sitemapComponentConfigurations = null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        // do we have the sitemap configurations prepared for this processor?
        if ( null == this.sitemapComponentConfigurations ) {
            
            synchronized (this) {

                if ( this.sitemapComponentConfigurations == null ) {
                    // do we have configurations?
                    final Configuration[] childs = (this.componentConfigurations == null 
                            ? null 
                            : this.componentConfigurations.getChildren());
                    
                    if ( null != childs ) {
                        
                        if ( null == this.m_parent ) {
                            this.sitemapComponentConfigurations = new HashMap(12);
                        } else {
                            // copy all configurations from parent
                            this.sitemapComponentConfigurations = new HashMap(this.m_parent.getComponentConfigurations()); 
                        }
                        
                        // and now check for new configurations
                        for(int m = 0; m < childs.length; m++) {
                            //FIXME - get the role
                            final String r = childs[m].getName();
                            this.sitemapComponentConfigurations.put(r, new ChainedConfiguration(childs[m], 
                                    (ChainedConfiguration)this.sitemapComponentConfigurations.get(r)));
                        }
                    } else {
                        // we don't have configurations
                        if ( null == this.m_parent ) {
                            this.sitemapComponentConfigurations = Collections.EMPTY_MAP;
                        } else {
                            // use configuration from parent
                            this.sitemapComponentConfigurations = this.m_parent.getComponentConfigurations(); 
                        }
                    }
                }
            }
        }
        return this.sitemapComponentConfigurations;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return m_environmentHelper.getContext();
    }
    
    /**
     * Returns the root sitemap processor.
     */
    public Processor getRootProcessor() {
        TreeProcessor result = this;
        while(result.m_parent != null) {
            result = result.m_parent;
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getEnvironmentHelper()
     */
    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return m_environmentHelper;
    }

    public EnvironmentHelper getEnvironmentHelper() {
        return m_environmentHelper;   
    }
    
    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @param source  the location of the child sitemap.
     * @return  a new child processor.
     */
    public TreeProcessor createChildProcessor(String src, 
                                              boolean checkReload,
                                              String  prefix) 
    throws Exception {
        Source delayedSource = new DelayedRefreshSourceWrapper(
            m_resolver.resolveURI(src), m_lastModifiedDelay);
        return new TreeProcessor(this, delayedSource, checkReload, prefix);
    }
    
    private class TreeProcessorRedirector extends ForwardRedirector {
        
        private InvokeContext context;
        public TreeProcessorRedirector(Environment env, InvokeContext context) {
            super(env);
            this.context = context;
        }
        
        protected void cocoonRedirect(String uri) throws IOException, ProcessingException {
            try {
                TreeProcessor.this.handleCocoonRedirect(uri, this.env, this.context);
            } catch(IOException ioe) {
                throw ioe;
            } catch(ProcessingException pe) {
                throw pe;
            } catch(RuntimeException re) {
                throw re;
            } catch(Exception ex) {
                throw new ProcessingException(ex);
            }
        }
    }

    /**
     * Local extension of EnvironmentWrapper to propagate otherwise blocked
     * methods to the actual environment.
     */
    private static final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

        public ForwardEnvironmentWrapper(Environment env,
            String uri, Logger logger) throws MalformedURLException {
            super(env, uri, logger);
        }

        public void setStatus(int statusCode) {
            environment.setStatus(statusCode);
        }

        public void setContentLength(int length) {
            environment.setContentLength(length);
        }

        public void setContentType(String contentType) {
            environment.setContentType(contentType);
        }

        public String getContentType() {
            return environment.getContentType();
        }

        public boolean isResponseModified(long lastModified) {
            return environment.isResponseModified(lastModified);
        }
        
        public void setResponseIsNotModified() {
            environment.setResponseIsNotModified();
        }
    }

}
