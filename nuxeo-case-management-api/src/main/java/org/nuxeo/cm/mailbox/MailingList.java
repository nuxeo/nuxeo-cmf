/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.mailbox;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * A named set of mailbox.
 */
public interface MailingList extends Serializable {

    /**
     * Returns this mailing list id.
     */
    String getId();

    /**
     * Sets id of the mailing list.
     */
    void setId(String id);

    /**
     * Returns title of the mailing list.
     */
    String getTitle();

    /**
     * Sets title of the mailing list.
     */
    void setTitle(String title);

    /**
     * @return the description
     */
    String getDescription();

    /**
     * Updates the description of this mailing list.
     *
     * @param descr
     */
    void setDescription(String descr);

    /**
     * Gets the id list of mailboxes of this list.
     */
    List<String> getMailboxIds();

    /**
     * Sets the list of mailboxes of this list.
     *
     * @param mailboxes
     */
    void setMailboxIds(List<String> mailboxes);

    /**
     * Gets the complete property map.
     */
    Map<String, Serializable> getMap();
}
