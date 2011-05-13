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

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Correspondence service.
 * <p>
 * Distributes an email to users/groups/mailboxes and manages mailboxes.
 */
public interface CaseDistributionService extends Serializable {

    /**
     * Send an envelope to a mailbox.
     */
    CaseLink sendCase(CoreSession session, CaseLink postRequest, boolean initial);

    /**
     * Distributes a case to a mailbox. the case is not currently in any
     * mailbox, (it was created by import for example)
     *
     */
    CaseLink sendCase(CoreSession session, String sender, Case kase,
            DistributionInfo initialDistribution);

    /**
     * Remove a case link from the mailbox. It is the duty of a listener to
     * update the security on the case if necessary.
     *
     * @param link
     */
    void removeCaseLink(CaseLink link, CoreSession sessiion);

    /**
     * Returns the sent posts for given mailbox
     */
    List<CaseLink> getSentCaseLinks(CoreSession coreSession, Mailbox mailbox,
            long offset, long limit);

    /**
     * Returns the received posts for given mailbox
     */
    List<CaseLink> getReceivedCaseLinks(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit);

    /**
     * Returns all the case links for this kase in this mailbox.
     *
     * @param session
     * @param mailbox if <code>null</code> returns the links of all mailboxes.
     * @param kase
     * @return
     */
    List<CaseLink> getCaseLinks(CoreSession session, Mailbox mailbox, Case kase);

    /**
     * Returns the draft posts for given mailbox
     */
    List<CaseLink> getDraftCaseLinks(CoreSession coreSession, Mailbox mailbox,
            long offset, long limit);

    /**
     * Returns the draft post of an envelope in given mailbox. Returns null if
     * post is not found.
     */
    CaseLink getDraftCaseLink(CoreSession session, Mailbox mailbox,
            String envelopeId);

    /**
     * Add a CaseItem to a Case
     *
     * @param session The core session
     * @param kase The case in which the CaseItem will be added
     * @param parentPath The path in which the CaseItem will be created
     * @param emailDoc The document model for the CaseItem
     * @return
     */
    CaseItem addCaseItemToCase(CoreSession session, Case kase,
            DocumentModel emailDoc);

    /**
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are
     *            created
     * @return a MailEnvelope containing default MailItem.
     */
    Case createCase(CoreSession session, DocumentModel emailDoc);

    /**
     * @param session
     * @param changeableDocument
     * @param the mailbox in which this case is created
     * @param parentPath the path where the document and its envelope are
     *            created
     * @return an emptyCase
     */
    Case createEmptyCase(CoreSession session, DocumentModel caseDoc,
            Mailbox mailbox);

    Case createEmptyCase(CoreSession session, DocumentModel caseDoc,
            List<Mailbox> mailboxes);

    Case createEmptyCase(CoreSession session, String title, String id,
            List<Mailbox> mailboxes);

    Case createEmptyCase(CoreSession session, String title, String id,
            Mailbox mailbox);

    Case createEmptyCase(CoreSession session, String title, String id,
            String type, List<Mailbox> mailboxes);

    /**
     * @param mailboxes The list of mailboxes in which the document will be
     *            seen.
     * @param session
     * @param changeableDocument
     * @param parentPath the path where the document and its envelope are
     *            created
     * @return a MailEnvelope containing default MailItem.
     */
    Case createCase(CoreSession session, DocumentModel emailDoc,
            List<Mailbox> mailboxes);

    /**
     * Create a draft post for an envelope in given mailbox.
     */
    CaseLink createDraftCaseLink(CoreSession session, Mailbox mailbox,
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

    /**
     * Send an case to a mailbox.
     */
    CaseLink sendCase(CoreSession session, CaseLink postRequest,
            boolean initial, boolean actionable);

    /**
     * This method calls {@link CaseManagementPersister} to find the parent document of
     * a case to be created.
     *
     * @param session
     * @return the case Parent Document
     */
    DocumentModel getParentDocumentForCase(CoreSession session);

    /**
     * This method calls {@link CaseManagementPersister} to find the parent document path of
     * a case to be created.
     *
     * @param session
     * @return
     */
    String getParentDocumentPathForCase(CoreSession session);

    /**
     * This method calls {@link CaseManagementPersister} to create a
     * {@link Case} from the given {@link CaseItem}
     *
     * @param {@link CaseItem}
     * @param documentManager
     * @return
     */
    Case createCaseFromExistingCaseItem(CaseItem adapter,
            CoreSession documentManager);

    /**
     * This method calls {@link CaseManagementPersister} to find the parent document path of
     * a {@link CaseItem} to be created.
     * @param coreSession
     * @param the parent {@link Case}
     * @return
     */
    String getParentDocumentPathForCaseItem(CoreSession session, Case kase);
}
