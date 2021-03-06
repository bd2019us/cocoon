/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
cocoon.load("servlet:forms:/resource/internal/flow/javascript/Form.js");

function xinha() {
    var form = new Form("forms/xinha.xml");

    form.showForm("xinha-display-pipeline");

    var model = form.getModel();
    var htmldata = { 
                      "data1" : model.data1,
                      "data2" : new Packages.org.apache.cocoon.xml.StringXMLizable(model.data2)
                   }
    cocoon.sendPage("xinha-success-pipeline", htmldata);
}
