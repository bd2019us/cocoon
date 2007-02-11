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
package org.apache.cocoon.environment.commandline;

import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.List;

/**
 * This environment is used to save the requested file to disk.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: FileSavingEnvironment.java,v 1.3 2003/05/12 13:26:17 stephan Exp $
 */
public class FileSavingEnvironment extends AbstractCommandLineEnvironment {

    public FileSavingEnvironment(String uri,
                                 File context,
                                 Map attributes,
                                 Map parameters,
                                 Map links,
                                 List gatheredLinks,
                                 CommandLineContext cliContext,
                                 OutputStream stream,
                                 Logger log)
    throws MalformedURLException {
        super(uri, null, context, stream, log);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, new CommandLineRequest(this, null, uri, null, attributes, parameters));
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, new CommandLineResponse());
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, cliContext);
        if (links != null) {
            this.objectModel.put(Constants.LINK_OBJECT, links);
        }
        if (gatheredLinks != null) {
            this.objectModel.put(Constants.LINK_COLLECTION_OBJECT, gatheredLinks);
        }
    }
}


