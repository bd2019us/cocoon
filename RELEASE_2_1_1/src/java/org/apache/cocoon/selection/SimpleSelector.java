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
package org.apache.cocoon.selection;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * A very simple selector that operates on string literals, useful especially 
 * in conjunction with input modules. Usage example:
 * <pre>
 *    &lt;map:selector name="simple" src="org.apache.cocoon.selection.SimpleSelector"/&gt;
 * 
 *    &lt;map:select type="simple"&gt;
 *       &lt;map:parameter name="value" value="{request:method}"/&gt;
 *       &lt;map:when test="GET"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:when test="POST"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:when test="PUT"&gt;
 *           ...
 *       &lt;/map:when&gt;
 *       &lt;map:otherwise&gt;
 *           ...
 *       &lt;/map:otherwise&gt;
 *    &lt;/map:select&gt;
 * </pre>
 * 
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SimpleSelector.java,v 1.2 2003/07/01 18:23:19 cziegeler Exp $
 * @since 2.1
 */
public class SimpleSelector extends AbstractSwitchSelector implements ThreadSafe {

    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        return parameters.getParameter("value", "");
    }

    public boolean select(String expression, Object selectorContext) {
        if (selectorContext == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("Value not set -- failing.");
            return false;
        }

        return selectorContext.equals(expression);
    }

}