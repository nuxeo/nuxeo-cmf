/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.cm.cases;

/**
 * Mail envelope life cycle constants
 *
 * @author Anahide Tchertchian
 */
public class CaseLifeCycleConstants {


    public static final String STATE_DRAFT = "draft";

    public static final String STATE_SENT = "sent";

    public static final String STATE_OPEN = "opened";

    public static final String STATE_PROCESS = "processed";

    public static final String STATE_ARCHIVE = "archived";

    public static final String TRANSITION_OPEN = "open";

    public static final String TRANSITION_PROCESS = "process";

    public static final String TRANSITION_ARCHIVE = "archive";

    public static final String TRANSITION_SEND = "send";

    public static final String TRANSITION_DELETE = "delete";

    private CaseLifeCycleConstants() {
    }

}
