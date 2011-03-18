/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.mailbox;

import java.io.Serializable;

/**
 * Mailbox minimal interface
 *
 * @author Laurent Doguin
 */
public interface MailboxHeader extends Serializable, Comparable<MailboxHeader> {

    /**
     * Returns the mailbox identifier.
     */
    String getId();

    /**
     * Returns title of the mailbox.
     */
    String getTitle();

    /**
     * Returns type of the mailbox.
     */
    String getType();

}
