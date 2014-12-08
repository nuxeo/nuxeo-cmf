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

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

/**
 * A creator of {@link CaseLink}.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class UpdateCaseLinkUnrestricted extends UnrestrictedSessionRunner {

    protected CaseLink post;

    protected final String subject;

    protected final String comment;

    protected final Case envelope;

    protected final Mailbox sender;

    protected final String recipientId;

    protected final Map<String, List<String>> internalRecipients;

    protected final Map<String, List<String>> externalRecipients;

    protected final boolean isSent;

    protected final boolean isInitial;

    protected Mailbox recipient;

    public CaseLink getUpdatedPost() {
        return post;
    }

    /**
     * @param repositoryName The name of the repository in which the {@link CaseLink} will be created.
     * @param subject The subject of the post.
     * @param comment The comment of the post.
     * @param envelope The envelope sent.
     * @param mailboxes The mailbox of the sender.
     * @param internalRecipients A map of recipients keyed by type of Message and keyed with a list of mailboxes.
     * @param isSent The post can be Sent or Received
     * @param isInitial Is it an initial sent?
     */
    public UpdateCaseLinkUnrestricted(CoreSession session, String subject, String comment, Case envelope,
            Mailbox sender, String recipientId, Map<String, List<String>> internalRecipients,
            Map<String, List<String>> externalRecipients, boolean isSent, boolean isInitial, CaseLink post) {
        super(session);
        this.comment = comment;
        this.envelope = envelope;
        this.subject = subject;
        this.sender = sender;
        this.recipientId = recipientId;
        this.internalRecipients = internalRecipients;
        this.externalRecipients = externalRecipients;
        this.isSent = isSent;
        this.isInitial = isInitial;
        this.post = post;
    }

    @Override
    public void run() throws ClientException {

        DocumentModel doc = post.getDocument();

        CaseLink post = doc.getAdapter(CaseLink.class);
        if (isInitial) {
            post.addInitialInternalParticipants(internalRecipients);
            post.addInitialExternalParticipants(externalRecipients);
        }

        post.addParticipants(internalRecipients);
        post.addParticipants(externalRecipients);

        setPostValues(doc);
        session.saveDocument(doc);
        this.post = doc.getAdapter(CaseLink.class);
    }

    /**
     * Sets the values of the document.
     */
    protected void setPostValues(DocumentModel doc) throws ClientException {
        // FIXME: use CorrespondencePost setters
        doc.setPropertyValue(IS_DRAFT_FIELD, false);
        doc.setPropertyValue(SUBJECT_FIELD, subject);
        doc.setPropertyValue(CASE_REPOSITORY_NAME_FIELD, envelope.getDocument().getRepositoryName());
        doc.setPropertyValue(CASE_DOCUMENT_ID_FIELD, envelope.getDocument().getId());
        doc.setPropertyValue(SENDER_MAILBOX_ID_FIELD, sender.getId());
        doc.setPropertyValue(SENDER_FIELD, sender.getOwner());
        doc.setPropertyValue(DATE_FIELD, Calendar.getInstance().getTime());
        doc.setPropertyValue(COMMENT_FIELD, comment);
        doc.setPropertyValue(IS_SENT_FIELD, isSent);
        // FIXME: what should be put here? because uid schema is not forced on
        // envelope
        // doc.setPropertyValue(ENVELOPE_ID_FIELD,
        // envelope.getDocument().getPropertyValue("uid:uid"));
    }

}
