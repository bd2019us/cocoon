/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletDefinitionFeatures;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider;
import org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * This is the adapter to use JSR-168 portlets as coplets.
 *
 * @version $Id$
 */
public class PortletAdapter 
    extends AbstractCopletAdapter
    implements PortalManagerAspect, CopletDecorationProvider, Receiver, Parameterizable {

    /** Name of the temporary coplet instance attribute holding the portlet window. */
    public static final String PORTLET_WINDOW_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/window";

    /** Name of the temporary coplet instance attribute holding the dynamic title (if any). */
    public static final String DYNAMIC_TITLE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/dynamic-title";

    /** Name of the temporary coplet instance attribute holding the window state. */
    public static final String WINDOW_STATE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/window-state";

    /** Name of the temporary coplet instance attribute holding the portlet mode. */
    public static final String PORTLET_MODE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/portlet-mode";

    /** Name of the portlet mode for full screen (if supported). */
    public static final String FULL_SCREEN_WINDOW_STATE_ATTRIBUTE_NAME = "full-screen-mode";

    /** Name of attribute in the coplet definition storing the portlet identifier. */
    public static final String PORTLET_ATTRIBUTE_NAME = "portlet";

    /** The configuration. */
    protected Parameters parameters;

    /** Is full-screen enabled? */
    protected boolean enableFullScreen;

    /** Is maximized enabled? */
    protected boolean enableMaximized;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.parameters = params;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#login(org.apache.cocoon.portal.coplet.CopletInstance)
     */
    public void login(CopletInstance coplet) {
        super.login(coplet);
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstance, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstance coplet,
                              ContentHandler contentHandler)
    throws SAXException {
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#logout(org.apache.cocoon.portal.coplet.CopletInstance)
     */
    public void logout(CopletInstance coplet) {
        super.logout(coplet);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        super.initialize();
        this.enableFullScreen = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_FULL_SCREEN_ENABLED, true);
        this.enableMaximized = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_MAXIMIZED_ENABLED, true);
        this.initContainer();
    }

    /**
     * Initialize the container
     */
    public void initContainer() throws Exception {
    }

    /**
     * This method is invoked each time an event for a portlet is received (user clicking/activating
     * something in the portlet).
     * @see Receiver
     */
    public void inform(PortletURLProviderImpl event, PortalService service) {
    }

    /**
     * This method is invoked each time a coplet instance is resized.
     * @see Receiver
     */
    public void inform(CopletInstanceSizingEvent event, PortalService service) {
        WindowState ws = WindowState.NORMAL;
        if ( event.getSize() == CopletInstance.SIZE_NORMAL ) {
            ws = WindowState.NORMAL;
        } else if ( event.getSize() == CopletInstance.SIZE_MAXIMIZED ) {
            ws = WindowState.MAXIMIZED;
        } else if ( event.getSize() == CopletInstance.SIZE_MINIMIZED ) {
            ws = WindowState.MINIMIZED;
        } else if ( event.getSize() == CopletInstance.SIZE_FULLSCREEN ) {
            ws = new WindowState((String)CopletDefinitionFeatures.getAttributeValue(event.getTarget().getCopletDefinition(), FULL_SCREEN_WINDOW_STATE_ATTRIBUTE_NAME, null));
        }
        final String wsString = (String)event.getTarget().getTemporaryAttribute(WINDOW_STATE_ATTRIBUTE_NAME);
        if ( !wsString.equals(ws.toString()) ) {
            event.getTarget().setTemporaryAttribute(WINDOW_STATE_ATTRIBUTE_NAME, ws.toString());
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#prepare(org.apache.cocoon.portal.PortalManagerAspectPrepareContext, org.apache.cocoon.portal.PortalService)
     */
    public void prepare(PortalManagerAspectPrepareContext aspectContext,
                        PortalService service)
    throws ProcessingException {
        // process the events
        aspectContext.invokeNext();

    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#render(org.apache.cocoon.portal.PortalManagerAspectRenderContext, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(PortalManagerAspectRenderContext aspectContext,
                       PortalService service,
                       ContentHandler ch,
                       Properties properties)
    throws SAXException {
        final Map objectModel = aspectContext.getObjectModel();

        // don't generate a response, if we issued a redirect
        if (objectModel.remove("portlet-event") == null) {
            aspectContext.invokeNext(ch, properties);
        }
    }

    protected String getResponse(CopletInstance instance, HttpServletResponse response) {
        return response.toString();
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getPossibleCopletModes(CopletInstance)
     */
    public List getPossibleCopletModes(CopletInstance copletInstanceData) {
        final List modes = new ArrayList();
        return modes;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getPossibleWindowStates(CopletInstance)
     */
    public List getPossibleWindowStates(CopletInstance copletInstanceData) {
        final List states = new ArrayList();
        return states;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getTitle(org.apache.cocoon.portal.coplet.CopletInstance)
     */
    public String getTitle(CopletInstance copletInstanceData) {
        String title = null;
        if ( title == null ) {
            title = copletInstanceData.getTitle();
        }
        return title;
    }
}
