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

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderHeader;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.post.CaseLink;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * Correspondence service.
 *
 * Distributes an email to users/groups/mailboxes and manages mailboxes.
 *
 */
public interface CaseManagementService extends Serializable {

    /**
     * Returns the mailbox with given unique identifier.
     *
     * @param muid User id
     */
    CaseFolder getCaseFolder(CoreSession session, String muid);

    /**
     * Returns the mailbox with given unique identifier.
     *
     * @param muid mailbox id
     */
    CaseFolder getCaseFolder(String muid);

    /**
     * Returns true if a mailbox with given id exists
     *
     * @param muid mailbox id
     */
    public boolean hasCaseFolder(String muid);

    /**
     * Returns the mailbox header with given unique identifier.
     *
     * @param muid User id
     */
    CaseFolderHeader getCaseFolderHeader(String muid);

    /**
     * Returns the mailboxes with given unique identifiers and a session. Giving
     * session can be helpful when you already are in an
     * unrestrictedSessionRunner.
     *
     * @param session
     * @param muids
     * @return
     */
    List<CaseFolderHeader> getCaseFoldersHeaders(CoreSession session,
            List<String> muids);

    /**
     * Returns the mailboxes with given unique identifiers.
     *
     * @param session a Core Session
     * @param muids Users ids
     *
     */
    List<CaseFolder> getCaseFolders(CoreSession session, List<String> muids);

    /**
     * Returns the mailboxes with given unique identifiers.
     *
     * @param muids Users ids
     *
     */
    List<CaseFolder> getCaseFolders(List<String> muids);

    /**
     * Returns the mailboxes headers with given unique identifiers.
     *
     * @param muids Users ids
     *
     */
    List<CaseFolderHeader> getCaseFoldersHeaders(List<String> muids);

    /**
     * Returns the personal mailbox id for this user.
     *
     * @param user User id
     */
    String getUserPersonalCaseFolderId(String user);

    /**
     * Returns all mailboxes for given user. Creates a personal mailbox for real
     * users if needed.
     *
     * @param userId User id
     *
     */
    List<CaseFolder> getUserCaseFolders(CoreSession session, String userId);

    /**
     * Returns the personal mailbox of the given user.
     *
     * @param userId User id
     * @throws ClientException
     */
    CaseFolder getUserPersonalCaseFolder(CoreSession session, String userId);

    /**
     * Returns a mailbox for given email
     */
    CaseFolder getUserPersonalCaseFolderForEmail(CoreSession session, String email);

    /**
     * Search mailboxes with given pattern
     *
     * @param pattern matching the box title
     * @param type mailbox type (generic, personal, etc... or null to match all)
     */
    List<CaseFolderHeader> searchCaseFolders(String pattern, String type);

    /**
     * Create the personal Mailbox with the registered
     *
     * @param userId
     */
    public List<CaseFolder> createPersonalCaseFolders(CoreSession session,
            String userId);

    /**
     * Test if the user has a personal mailbox created
     *
     * @param userId
     * @return true if the user has a personal mailbox
     * @throws ClientException
     */
    boolean hasUserPersonalCaseFolder(CoreSession session, String userId);

    /**
     * Send an envelope to a mailbox.
     */
    CaseLink sendCase(CoreSession session,
            CaseLink postRequest, boolean initial);

    /**
     * Returns the sent posts for given mailbox
     */
    List<CaseLink> getSentCaseLinks(CoreSession coreSession,
            CaseFolder mailbox, long offset, long limit);

    /**
     * Returns the received posts for given mailbox
     */
    List<CaseLink> getReceivedCaseLinks(CoreSession coreSession,
            CaseFolder mailbox, long offset, long limit);

    /**
     * Returns the draft posts for given mailbox
     */
    List<CaseLink> getDraftCaseLinks(CoreSession coreSession,
            CaseFolder mailbox, long offset, long limit);

    /**
     * Returns the draft post of an envelope in given mailbox. Returns null if
     * post is not found.
     */
    CaseLink getDraftCaseLink(CoreSession session, CaseFolder mailbox,
            String envelopeId);

    /**
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are created
     * @return a MailEnvelope containing default MailItem.
     */
    Case createCase(CoreSession session,
            DocumentModel emailDoc, String parentPath);

    /**
     * @param mailboxes The list of mailboxes in which the document will be seen.
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are created
     * @return a MailEnvelope containing default MailItem.
     */
    Case createCase(CoreSession session,
            DocumentModel emailDoc, String parentPath, List<CaseFolder> mailboxes);

    /**
     * Create a draft post for an envelope in given mailbox.
     */
    CaseLink createDraftCaseLink(CoreSession session, CaseFolder mailbox,
            Case envelope);

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
}
