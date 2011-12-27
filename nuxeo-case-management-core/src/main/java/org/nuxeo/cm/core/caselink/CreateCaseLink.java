/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.core.caselink;

import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_REPOSITORY_NAME_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.COMMENT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.DATE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_SENT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_MAILBOX_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SUBJECT_FIELD;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.core.service.GetMailboxesUnrestricted;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * A creator of {@link CaseLink}.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class CreateCaseLink {

    protected CoreSession session;

    protected CaseLink createdPost;

    protected final String subject;

    protected final String comment;

    protected final Case envelope;

    protected final Mailbox sender;

    protected final String recipientId;

    protected final Map<String, List<String>> internalRecipients;

    protected final Map<String, List<String>> externalRecipients;

    protected final boolean isSent;

    protected final boolean isInitial;

    protected CaseLink draft;

    protected Mailbox recipient;

    protected boolean isActionable = false;

    protected String validateId;

    protected String refuseId;

    protected String stepId;

    public CaseLink getCreatedPost() {
        return createdPost;
    }

    /**
     * @param draft The draft used to sent this envelope
     * @param repositoryName The name of the repository in which the
     *            {@link CaseLink} will be created.
     * @param subject The subject of the post.
     * @param comment The comment of the post.
     * @param envelope The envelope sent.
     * @param mailboxes The mailbox of the sender.
     * @param internalRecipients A map of recipients keyed by type of Message
     *            and keyed with a list of mailboxes.
     * @param isSent The post can be Sent or Received
     * @param isInitial Is it an initial sent?
     */
    public CreateCaseLink(CaseLink draft, CoreSession session, String subject,
            String comment, Case envelope, Mailbox sender, String recipientId,
            Map<String, List<String>> internalRecipients,
            Map<String, List<String>> externalRecipients, boolean isSent,
            boolean isInitial) {
        this.draft = draft;
        this.comment = comment;
        this.envelope = envelope;
        this.subject = subject;
        this.sender = sender;
        this.recipientId = recipientId;
        this.internalRecipients = internalRecipients;
        this.externalRecipients = externalRecipients;
        this.isSent = isSent;
        this.isInitial = isInitial;
        if (draft != null) {
            this.isActionable = draft.isActionnable();
        }
        this.session = session;
    }

    public void create() throws ClientException {
        GetMailboxesUnrestricted getMailboxesUnrestricted = new GetMailboxesUnrestricted(
                session, recipientId);
        getMailboxesUnrestricted.runUnrestricted();
        List<Mailbox> mailboxes = getMailboxesUnrestricted.getMailboxes();
        if (mailboxes == null || mailboxes.isEmpty()) {
            throw new CaseManagementException(
                    "Can't send post because sender mailbox does not exist.");
        }

        recipient = mailboxes.get(0);
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        recipient = mailboxes.get(0);
        DocumentModel doc = session.createDocumentModel(
                recipient.getDocument().getPathAsString(),
                UUID.randomUUID().toString(),
                correspDocumentTypeService.getCaseLinkType());

        if (draft != null) {
            doc.copyContent(draft.getDocument());
        }
        CaseLink post = doc.getAdapter(CaseLink.class);
        if (isInitial) {
            post.addInitialInternalParticipants(internalRecipients);
            post.addInitialExternalParticipants(externalRecipients);
        }
        post.setActionnable(isActionable);
        if (isActionable) {
            doc.putContextData(
                    LifeCycleConstants.INITIAL_LIFECYCLE_STATE_OPTION_NAME,
                    CaseLink.CaseLinkState.todo.name());
        }
        post.addParticipants(internalRecipients);
        post.addParticipants(externalRecipients);
        setPostValues(doc);
        session.createDocument(doc);
        createdPost = doc.getAdapter(CaseLink.class);
    }

    /**
     * Sets the values of the document.
     */
    protected void setPostValues(DocumentModel doc) throws ClientException {
        doc.setPropertyValue(IS_DRAFT_FIELD, false);
        doc.setPropertyValue(SUBJECT_FIELD, subject);
        doc.setPropertyValue(CASE_REPOSITORY_NAME_FIELD,
                envelope.getDocument().getRepositoryName());
        doc.setPropertyValue(CASE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        if (sender != null) {
            doc.setPropertyValue(SENDER_MAILBOX_ID_FIELD, sender.getId());
            doc.setPropertyValue(SENDER_FIELD, sender.getOwner());
        }
        doc.setPropertyValue(DATE_FIELD, Calendar.getInstance().getTime());
        doc.setPropertyValue(COMMENT_FIELD, comment);
        doc.setPropertyValue(IS_SENT_FIELD, isSent);
    }
}
