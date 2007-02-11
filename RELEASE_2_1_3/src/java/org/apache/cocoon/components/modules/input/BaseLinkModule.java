/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * BaseLinkModule returns a relative link (<code>../</code>,
 * <code>../../</code> etc) to the base of the current request or sitemap URI.  For
 * instance, if called within a &lt;map:match pattern="a/b/c.xsp"> pipeline,
 * <code>{baselink:SitemapBaseLink}</code> would evaluate to <code>../../</code>.
 *
 * @author <a href="mailto:tk-cocoon@datas-world.de">Torsten Knodt</a>
 * based on RequestURIModule
 *
 */
public class BaseLinkModule extends AbstractInputModule implements ThreadSafe {

    final static Vector returnNames = new Vector() {
        {
            add("RequestBaseLink");
            add("SitemapBaseLink");
        }
    };

    public Object getAttribute(
        final String name,
        final Configuration modeConf,
        final Map objectModel)
        throws ConfigurationException {

        String uri;
        if (name.equals("SitemapBaseLink"))
            uri = ObjectModelHelper.getRequest(objectModel).getSitemapURI();
        else if (name.equals("RequestBaseLink"))
            uri = ObjectModelHelper.getRequest(objectModel).getRequestURI();
        else
            uri = "";

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        StringBuffer result = new StringBuffer(uri.length());

        int nextIndex = 0;
        while ((nextIndex = uri.indexOf('/', nextIndex) + 1) > 0) {
            result.append("../");
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("Returns " + result + " for uri " + uri + " and attribute " + name);

        return result.toString();
    }

    public Iterator getAttributeNames(final Configuration modeConf, final Map objectModel)
        throws ConfigurationException {

        return RequestURIModule.returnNames.iterator();
    }

    public Object[] getAttributeValues(
        final String name,
        final Configuration modeConf,
        final Map objectModel)
        throws ConfigurationException {

        Object result = new Object[1];
        result = getAttribute(name, modeConf, objectModel);
        return (result == null? null : new Object[]{result});        
    }

}