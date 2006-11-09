/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for deploying resources from the block artifacts.
 *
 * @version $Id$
 * @since 2.2
 */
public class DeploymentUtil {

    protected static final Log logger = LogFactory.getLog(DeploymentUtil.class);

    protected static final String RESOURCES_PATH = "COB-INF";

    protected final String destinationDirectory;
    
    protected static final Map blockContexts = new HashMap(); 

    public DeploymentUtil(ServletContext servletContext) {
        // TODO how do we handle non servlet container environment?
        if ( servletContext == null ) {
            this.destinationDirectory = null;
            return;
        }
        
        this.destinationDirectory =
            ((File) servletContext.getAttribute("javax.servlet.context.tempdir")).getPath();
    }

    protected void deploy(JarFile jarFile, String prefix, String destination)
    throws IOException {
        if ( logger.isDebugEnabled() ) {
            logger.debug("Deploying jar " + jarFile + " to " + destination);
        }
        // FIXME - We should check if a deploy is required
        final Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry)entries.nextElement();
            if ( !entry.isDirectory() && entry.getName().startsWith(prefix) ) {
                final String fileName = destination + entry.getName().substring(prefix.length());
                final File out = new File(fileName);
                // create directory
                out.getParentFile().mkdirs();
                IOUtils.copy(jarFile.getInputStream(entry), new FileOutputStream(out));
            }
        }        
    }

    protected void deployBlockResources(String resourcePattern, String relativeDirectory)
    throws IOException {
        final Enumeration jarUrls = this.getClass().getClassLoader().getResources(resourcePattern);
        while ( jarUrls.hasMoreElements() ) {
            final URL resourceUrl = (URL)jarUrls.nextElement();
            // we only handle jar files!
            // TODO - Should we throw an exception if it's not a jar file? (or log?)
            if ( "jar".equals(resourceUrl.getProtocol()) ) {
                // if this is a jar url, it has this form "jar:{url-to-jar}!/{resource-path}
                // to open the jar, we can simply remove everything after "!/"
                String url = resourceUrl.toExternalForm();
                int pos = url.indexOf('!');
                url = url.substring(0, pos+2); // +2 as we include "!/"
                final URL jarUrl = new URL(url);
                final JarURLConnection connection = (JarURLConnection)jarUrl.openConnection();
                final JarFile jarFile = connection.getJarFile();
                String blockName = jarFile.getManifest().getMainAttributes().getValue("Cocoon-Block-Name");
                if ( blockName == null ) {
                    String jarPath = jarFile.getName();
                    // extract jar name
                    String jarName = jarPath.substring(jarPath.lastIndexOf(File.separatorChar) + 1);
                    // drop file extension
                    blockName = jarName.substring(0, jarName.lastIndexOf('.'));
                    // TODO how do we strip version from blockName?
                }
                final StringBuffer buffer = new StringBuffer(this.destinationDirectory);
                buffer.append(File.separatorChar);
                buffer.append(relativeDirectory);
                buffer.append(File.separatorChar);
                buffer.append(blockName);
                String destination = buffer.toString();
                this.deploy(jarFile, resourcePattern, destination);
                // register the root URL for the block resources
                DeploymentUtil.blockContexts.put(blockName, destination);
            }
        }        
    }

    public void deploy()
    throws IOException {
        // Check if we run unexpanded
        if ( this.destinationDirectory != null ) {
            // deploy all artifacts containing block resources
            this.deployBlockResources(DeploymentUtil.RESOURCES_PATH, "blocks");
        }
    }

    /**
     * Get a map that associates a block name with the root URL of the blocks resources
     *  
     * @return the blockContexts
     */
    // FIXME: It would be better to make the block contexts mapping available as a
    // component instead of as a static method
    public static Map getBlockContexts() {
        return blockContexts;
    }    
}