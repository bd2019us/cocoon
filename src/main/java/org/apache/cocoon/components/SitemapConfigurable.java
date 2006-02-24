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
package org.apache.cocoon.components;

import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Objects implementing this marker interface can get a configuration
 * from the map:pipelines section of the sitemap when they are created.
 *
 * @since 2.1
 * @version $Id$
 */
public interface SitemapConfigurable {

    /**
     * Set the <code>Configuration</code>.
     */
    void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException;
}
