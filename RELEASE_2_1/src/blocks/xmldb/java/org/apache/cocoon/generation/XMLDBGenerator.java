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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

import java.io.IOException;
import java.util.Map;

/**
 * This class implements generation of XML documents from a
 * XML:DB compliant database.
 * It must to be configured as follows:
 * <pre>
 * &lt;driver&gt;
 *   (a valid DB:XML compliant driver)
 * &lt;/driver&gt;
 * &lt;base&gt;
 *   xmldb:yourdriver://host/an/optional/path/to/be/prepended
 * &lt;/base&gt;
 * </pre>
 *
 * NOTE: the driver can be any DB:XML compliant driver (although this
 * component has been tested only with
 * <a href="http://www.dbxml.org">dbXML</a>, and the trailing
 * slash in the base tag is important!
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id: XMLDBGenerator.java,v 1.2 2003/03/19 15:42:16 cziegeler Exp $
 * @deprecated Use the XML:DB pseudo protocol instead.
 */
public class XMLDBGenerator extends ComposerGenerator
        implements CacheableProcessingComponent, Configurable,Initializable {

    protected String driver;
    protected String base;
    protected String col;
    protected String res;
    protected Database database;
    protected Collection collection;
    protected XMLResource xmlResource;

    public void compose(ComponentManager manager) throws ComponentException {
        super.compose(manager);
    }

    /**
     * Recycle the component, keep only the configuration variables
     * and the database instance for reuse.
     */
    public void recycle() {
        super.recycle();
        this.col = null;
        this.res = null;
        this.xmlResource = null;
        this.collection = null;
    }

    /**
     * Configure the component. This class is expecting a configuration
     * like the following one:
     * <pre>
     * &lt;driver&gt;org.dbxml.client.xmldb.DatabaseImpl&lt;/driver&gt;
     * &lt;base&gt;xmldb:dbxml:///db/&lt;/base&gt;
     * </pre>
     * NOTE: the driver can be any DB:XML compliant driver (although this
     * component has been tested only with
     * <a href="http://www.dbxml.org">dbXML</a>, and the trailing
     * slash in the base tag is important!
     *
     * @exception ConfigurationException (configuration invalid or missing)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.driver = conf.getChild("driver").getValue();
        this.base = conf.getChild("base").getValue();
    }

    /**
     * Initialize the component getting a database instance.
     *
     * @exception Exception if an error occurs
     */
    public void initialize() throws Exception {
        try {
            Class c = Class.forName(driver);
            database = (Database)c.newInstance();
            DatabaseManager.registerDatabase(database);
        } catch (XMLDBException xde) {
            this.getLogger().error("Unable to connect to the XML:DB database");
            throw new ProcessingException("Unable to connect to the XMLDB database: "
                                          + xde.getMessage());
        } catch (Exception e) {
            this.getLogger().error("There was a problem setting up the connection");
            this.getLogger().error("Make sure that your driver is available");
            throw new ProcessingException("Problem setting up the connection: "
                                          + e.getMessage());
        }
    }

    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
            throws ProcessingException, SAXException,IOException {
        super.setup(resolver, objectModel, src, par);
    }

    /**
     * The component isn't cached (yet)
     */
    public SourceValidity getValidity() {
        return null;
    }

    /**
     * The component isn't cached (yet)
     */
    public java.io.Serializable getKey() {
        return null;
    }

    /**
     * Parse the requested URI, connect to the XML:DB database
     * and fetch the requested resource.
     *
     * @exception ProcessingException something unexpected happened with the DB
     */
    public void generate()
            throws IOException, SAXException, ProcessingException {
        String col = "/";

        if (source.indexOf('/') != -1)
            col = "/" + source.substring(0, source.lastIndexOf('/'));
        res = source.substring(source.lastIndexOf('/') + 1);

        try {
            collection = DatabaseManager.getCollection(base + col);
            xmlResource = (XMLResource) collection.getResource(res);
            if (xmlResource == null) {
                throw new ResourceNotFoundException("Document " + col + "/" + res +
                                                    " not found");
            }

            xmlResource.getContentAsSAX(this.xmlConsumer);
            collection.close();
        } catch (XMLDBException xde) {
            throw new ProcessingException("Unable to fetch content: " +
                                          xde.getMessage());
        } catch (NullPointerException npe) {
            this.getLogger().error("The XML:DB driver raised an exception");
            this.getLogger().error("probably the document was not found");
            throw new ProcessingException("Null pointer exception while " +
                                          "retrieving document : " + npe.getMessage());
        }
    }
}