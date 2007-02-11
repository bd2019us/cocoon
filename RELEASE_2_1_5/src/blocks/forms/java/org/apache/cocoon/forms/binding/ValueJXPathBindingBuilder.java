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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * ValueJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link ValueJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:value id="<i>widget-id</i>" path="<i>xpath-expression</i>"&gt;
 *   &lt;!-- optional child binding to be executed upon 'save' of changed value --&gt;
 *   &lt;fb:on-update&gt;
 *     &lt;!-- any childbinding --&gt;
 *   &lt;/fb:on-update&gt;
 * &lt;/fb:value&gt;
 * </code></pre>
 *
 * @version CVS $Id: ValueJXPathBindingBuilder.java,v 1.3 2004/04/01 12:59:57 mpo Exp $
 */
public class ValueJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link ValueJXPathBinding} based on the attributes
     * and nested configuration of the provided bindingElm.
     */
    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path");
            String widgetId = DomHelper.getAttribute(bindingElm, "id");

            Element updateWrapElement =
                DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "on-update");
            JXPathBindingBase[] updateBindings = assistant.makeChildBindings(updateWrapElement);

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElm, Constants.DEFINITION_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = convertorEl.getAttribute("datatype");
                if (!localeStr.equals("")) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }

                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            ValueJXPathBinding fieldBinding =
                    new ValueJXPathBinding(commonAtts,
                            widgetId, xpath, updateBindings, convertor, convertorLocale);

            return fieldBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}