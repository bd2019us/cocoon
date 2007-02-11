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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.NamedProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActionSetNode.java,v 1.3 2003/08/07 17:13:39 joerg Exp $
 */

public class ActionSetNode extends SimpleSelectorProcessingNode
  implements NamedProcessingNode {
      
    public static final String CALLER_PARAMETERS = ActionSetNode.class.getName() + "/CallerParameters";
    public static final String ACTION_RESULTS = ActionSetNode.class.getName() + "/ActionResults";

    /** The action nodes */
    private ProcessingNode[] nodes;

    /** The 'action' attribute for each action */
    private String[] actionNames;

    public ActionSetNode(
      String name, ProcessingNode[] nodes, String[] actionNames) {
        super(name);
        this.nodes = nodes;
        this.actionNames = actionNames;
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {
	
        // Perform any common invoke functionalty 
        // super.invoke(env, context);
        String msg = "An action-set cannot be invoked, at " + this.getLocation();
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Call the actions composing the action-set and return the combined result of
     * these actions.
     */
    public final Map call(Environment env, InvokeContext context, Parameters params) throws Exception {

        String cocoonAction = env.getAction();

        // Store the parameters from the caller into the environment so that they can be merged with
        // each action's parameters.
        

        Map result = null;

        // Call each action that either has no cocoonAction, or whose cocoonAction equals
        // the one from the environment.
        env.setAttribute(CALLER_PARAMETERS, params);

        for (int i = 0; i < nodes.length; i++) {


            String actionName = actionNames[i];
            if (actionName == null || actionName.equals(cocoonAction)) {
                
                this.nodes[i].invoke(env, context);
                
                // Get action results. They're passed back through the environment since action-sets
                // "violate" the tree hierarchy (the returned Map is visible outside of the node)
                Map actionResult = (Map)env.getAttribute(ACTION_RESULTS);
                // Don't forget to clear it
                env.removeAttribute(ACTION_RESULTS);
                
                if (actionResult != null) {
                    // Merge the result in the global result, creating it if necessary.
                    if (result == null) {
                        result = new HashMap(actionResult);
                    } else {
                        result.putAll(actionResult);
                    }
                }
                
            } // if (actionName...
        } // for (int i...

        return result;
    }

    /**
     * Implementation of <code>NamedProcessingNode</code>.
     */

    public String getName() {
        return this.componentName;
    }
}