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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *     Nicolas Ulrich
 *
 */

package org.nuxeo.cm.service;

import java.io.Serializable;
import java.util.List;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.ecm.core.api.CoreSession;

/**
 * Correspondence service.
 */
public interface MailboxManagementService extends Serializable {

    /**
     * Returns the mailbox with given unique identifier.
     *
     * @param session
     * @param muid User id
     */
    Mailbox getMailbox(CoreSession session, String muid);

    /**
     * Returns true if a mailbox with given id exists
     *
     * @param session
     * @param muid mailbox id
     */
    boolean hasMailbox(CoreSession session, String muid);

    /**
     * Returns the mailboxes with given unique identifiers and a session.
     * Giving session can be helpful when you already are in an
     * unrestrictedSessionRunner.
     *
     * @param session
     * @param muids
     * @return
     */
    List<MailboxHeader> getMailboxesHeaders(CoreSession session,
            List<String> muids);

    /**
     * Returns the mailboxes with given unique identifiers.
     *
     * @param session a Core Session
     * @param muids Users ids
     */
    List<Mailbox> getMailboxes(CoreSession session, List<String> muids);

    /**
     * Returns the personal mailbox id for this user.
     *
     * @param user User id
     */
    String getUserPersonalMailboxId(String user);

    /**
     * Returns all mailboxes for given user. Creates a personal mailbox for
     * real users if needed.
     *
     * @param session
     * @param userId User id
     */
    List<Mailbox> getUserMailboxes(CoreSession session, String userId);

    /**
     * Returns the personal mailbox of the given user.
     *
     * @param session
     * @param userId User id
     */
    Mailbox getUserPersonalMailbox(CoreSession session, String userId);

    /**
     * Returns a mailbox for given email
     *
     * @param session
     * @param email
     */
    Mailbox getUserPersonalMailboxForEmail(CoreSession session, String email);

    /**
     * Search mailboxes with given pattern
     *
     * @param session
     * @param pattern matching the box title
     * @param type mailbox type (generic, personal, etc... or null to match
     *            all)
     */
    List<MailboxHeader> searchMailboxes(CoreSession session, String pattern,
            String type);

    /**
     * Create the personal Mailbox with the registered
     *
     * @param session
     * @param userId
     * @return personal mailbox list.
     */
    List<Mailbox> createPersonalMailboxes(CoreSession session, String userId);

    /**
     * Test if the user has a personal mailbox created
     *
     * @param session
     * @param userId
     * @return true if the user has a personal mailbox
     */
    boolean hasUserPersonalMailbox(CoreSession session, String userId);

    MailboxHeader getMailboxHeader(CoreSession documentManager, String fav);
}
