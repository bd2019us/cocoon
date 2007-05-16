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
package org.apache.cocoon.auth.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.auth.User;

/**
 * This class keeps track of the number of applications a user is logged into
 * using the same security handler.
 *
 * @version $Id$
*/
public class LoginInfo
implements Serializable {

    /** Number of applications using the security handler. */
    protected int   counter;

    /** The corresponding user. */
    protected final User user;

    /** The applications the user is logged in to. */
    protected List applications = new ArrayList();

    /**
     * Instantiate new info object.
     * @param aUser The user object returned by the security handler.
     */
    public LoginInfo(final User aUser) {
        this.user = aUser;
    }

    /**
     * Notification of another application using the handler.
     * @param appName The application name.
     */
    public void incUsageCounter(final String appName) {
        this.counter++;
        this.applications.add(appName);
    }

    /**
     * Notification of an application that is not using the handler anymore.
     * @param appName The application name.
     */
    public void decUsageCounter(final String appName) {
        this.applications.remove(appName);
        this.counter--;
    }

    /**
     * Is the handler used by any application.
     * @return true if it's used, otherwise false.
     */
    public boolean isUsed() {
        return (this.counter > 0);
    }

    /**
     * Return the corresponding user.
     * @return The user generated by the security handler.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Return the list of all applications.
     * @return The list containing all applications this user is logged in to.
     */
    public List getApplications() {
         return this.applications;
    }
}