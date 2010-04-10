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
import java.util.Map;

import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * Correspondence service.
 *
 * Distributes an email to users/groups/mailboxes and manages mailboxes.
 *
 */
public interface CorrespondenceService extends Serializable {

    /**
     * Returns the mailbox with given unique identifier.
     *
     * @param muid User id
     */
    Mailbox getMailbox(CoreSession session, String muid);

    /**
     * Returns the mailbox with given unique identifier.
     *
     * @param muid mailbox id
     */
    Mailbox getMailbox(String muid);

    /**
     * Returns true if a mailbox with given id exists
     *
     * @param muid mailbox id
     */
    public boolean hasMailbox(String muid);

    /**
     * Returns the mailbox header with given unique identifier.
     *
     * @param muid User id
     */
    MailboxHeader getMailboxHeader(String muid);

    /**
     * Returns the mailboxes with given unique identifiers and a session. Giving
     * session can be helpful when you already are in an
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
     *
     */
    List<Mailbox> getMailboxes(CoreSession session, List<String> muids);

    /**
     * Returns the mailboxes with given unique identifiers.
     *
     * @param muids Users ids
     *
     */
    List<Mailbox> getMailboxes(List<String> muids);

    /**
     * Returns the mailboxes headers with given unique identifiers.
     *
     * @param muids Users ids
     *
     */
    List<MailboxHeader> getMailboxesHeaders(List<String> muids);

    /**
     * Returns the personal mailbox id for this user.
     *
     * @param user User id
     */
    String getUserPersonalMailboxId(String user);

    /**
     * Returns all mailboxes for given user. Creates a personal mailbox for real
     * users if needed.
     *
     * @param userId User id
     *
     */
    List<Mailbox> getUserMailboxes(CoreSession session, String userId);

    /**
     * Returns the personal mailbox of the given user.
     *
     * @param userId User id
     * @throws ClientException
     */
    Mailbox getUserPersonalMailbox(CoreSession session, String userId);

    /**
     * Returns a mailbox for given email
     */
    Mailbox getUserPersonalMailboxForEmail(CoreSession session, String email);

    /**
     * Search mailboxes with given pattern
     *
     * @param pattern matching the box title
     * @param type mailbox type (generic, personal, etc... or null to match all)
     */
    List<MailboxHeader> searchMailboxes(String pattern, String type);

    /**
     * Create the personal Mailbox with the registered
     *
     * @param userId
     */
    public List<Mailbox> createPersonalMailbox(CoreSession session,
            String userId);

    /**
     * Test if the user has a personal mailbox created
     *
     * @param userId
     * @return true if the user has a personal mailbox
     * @throws ClientException
     */
    boolean hasUserPersonalMailbox(CoreSession session, String userId);

    /**
     * Send an envelope to a mailbox.
     */
    CorrespondencePost sendEnvelope(CoreSession session,
            CorrespondencePost postRequest, boolean initial);

    /**
     * Returns the sent posts for given mailbox
     */
    List<CorrespondencePost> getSentPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit);

    /**
     * Returns the received posts for given mailbox
     */
    List<CorrespondencePost> getReceivedPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit);

    /**
     * Returns the draft posts for given mailbox
     */
    List<CorrespondencePost> getDraftPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit);

    /**
     * Returns the draft post of an envelope in given mailbox. Returns null if
     * post is not found.
     */
    CorrespondencePost getDraftPost(CoreSession session, Mailbox mailbox,
            String envelopeId);

    /**
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are created
     * @return a MailEnvelope containing default MailItem.
     */
    MailEnvelope createMailEnvelope(CoreSession session,
            DocumentModel emailDoc, String parentPath);

    /**
     * @param mailboxes The list of mailboxes in which the document will be seen.
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are created
     * @return a MailEnvelope containing default MailItem.
     */
    MailEnvelope createMailEnvelope(CoreSession session,
            DocumentModel emailDoc, String parentPath, List<Mailbox> mailboxes);

    /**
     * Create a draft post for an envelope in given mailbox.
     */
    CorrespondencePost createDraftPost(CoreSession session, Mailbox mailbox,
            MailEnvelope envelope);

    /**
     * Throw a core event.
     *
     * @param session The session use in the event context and to get the
     *            principal.
     * @param name the name of the event
     * @param document The document use for DocumentEventContext
     * @param eventProperties The properties used in the event context.
     */
    void notify(CoreSession session, String name, DocumentModel document,
            Map<String, Serializable> eventProperties);

    /**
     * Create a reply Mail. It is a changeable document, with some properties
     * already setted.
     *
     * @param session a Core Session
     * @param mailbox the mailbox of the user that create the response
     * @param mailToReply the document to reply to.
     * @return a DocumentModel which contains the reply
     * @throws ClientException
     */
    DocumentModel getReplyDocument(CoreSession session, Mailbox mailbox,
            DocumentModel mailToReply) throws ClientException;
}
