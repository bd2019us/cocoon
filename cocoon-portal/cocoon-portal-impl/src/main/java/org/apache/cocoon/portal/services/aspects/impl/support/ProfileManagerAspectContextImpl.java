/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.services.aspects.impl.support;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.scratchpad.Profile;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;
import org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl;

/**
 * The aspect context is passed to every aspect.
 * @since 2.2
 * @version $Id$
 */
public final class ProfileManagerAspectContextImpl
    extends BasicAspectContextImpl
    implements ProfileManagerAspectContext {

    public ProfileManagerAspectContextImpl(PortalService service,
                                              AspectChain   chain) {
        super(service, chain);
    }

	/**
	 * @see org.apache.cocoon.portal.services.aspects.ProfileManagerAspectContext#invokeNext(org.apache.cocoon.portal.scratchpad.Profile)
	 */
	public void invokeNext(Profile portalProfile) {
        final ProfileManagerAspect aspect = (ProfileManagerAspect)this.getNext();
        if ( aspect != null ) {
            aspect.prepare(this, portalProfile);
        }
    }
}
