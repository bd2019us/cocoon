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
package org.apache.cocoon.i18n;

import java.util.Locale;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;

/**
 * Bundle Factory implementations are responsible for loading and providing
 * particular types of resource bundles, implementors of Bundle interface.
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: BundleFactory.java,v 1.8 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public interface BundleFactory extends ComponentSelector {

    /**
     * Bundle factory ROLE name
     */
    String ROLE = BundleFactory.class.getName();

    /**
     * Constants for configuration keys
     */
    static class ConfigurationKeys {
        public static final String CACHE_AT_STARTUP = "cache-at-startup";
        public static final String ROOT_DIRECTORY = "catalogue-location";
    }

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale name.
     *
     * @param base    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String base, String bundleName, String locale) throws ComponentException;

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param base    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String base, String bundleName, Locale locale) throws ComponentException;

    /**
     * Select a bundle based on the catalogue base location, bundle name,
     * and the locale.
     *
     * @param directories    catalogue base location (URI)
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String[] directories, String bundleName, Locale locale) throws ComponentException;

    /**
     * Select a bundle based on the bundle name and the locale name from
     * the default catalogue.
     *
     * @param bundleName    bundle name
     * @param locale  locale name
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String bundleName, String locale) throws ComponentException;

    /**
     * Select a bundle based on the bundle name and the locale from
     * the default catalogue.
     *
     * @param bundleName    bundle name
     * @param locale  locale
     * @return        the bundle
     * @exception     ComponentException if a bundle is not found
     */
    Bundle select(String bundleName, Locale locale) throws ComponentException;
}