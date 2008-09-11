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
dojo.provide("cocoon.forms.RichTextArea");

dojo.require("dijit.Editor");
dojo.require("cocoon.forms._FieldMixin");


/**
 * CForms Rich Text Editor Widget.
 * A wrapper for dijit.Editor that handles value transfer for cforms
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.RichTextArea", [dijit.Editor, cocoon.forms._FieldMixin, cocoon.forms._FieldHintMixin], {	
    
	onSubmit: function(){
    this.getValueNode().value = this.getValue();
	},
	
	getValueNode: function() {
    return dojo.byId(this.name + ":save");
  }

});