/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.expression.javascript;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionCompiler;
import org.apache.cocoon.components.expression.ExpressionException;

/**
 * @version $Id$
 */
public class JavaScriptCompiler implements ExpressionCompiler, ThreadSafe {

    /**
     * @see org.apache.cocoon.components.expression.ExpressionCompiler#compile(java.lang.String, java.lang.String)
     */
    public Expression compile(String language, String expression) throws ExpressionException {
        return new JavaScriptExpression(language, expression);
    }
}