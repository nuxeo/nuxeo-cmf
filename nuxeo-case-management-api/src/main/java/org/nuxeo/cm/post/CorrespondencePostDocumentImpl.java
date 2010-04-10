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
package org.nuxeo.cm.post;

import static org.nuxeo.cm.post.CorrespondencePostConstants.COMMENT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.DATE_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.ENVELOPE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_READ_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_MAILBOX_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENT_DATE_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SUBJECT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.TYPE_FIELD;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CorrespondenceRuntimeException;
import org.nuxeo.cm.mail.HasRecipients;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.model.PropertyException;


/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class CorrespondencePostDocumentImpl implements CorrespondencePost {

    private static final long serialVersionUID = -94563234903621891L;

    protected HasRecipients recipientAdapter;

    protected DocumentModel document;

    public CorrespondencePostDocumentImpl(DocumentModel doc,
            HasRecipients recipientAdapted) {
        this.document = doc;
        this.recipientAdapter = recipientAdapted;
    }

    public String getComment() {
        return getPropertyValue(COMMENT_FIELD);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getPropertyValue(String value) {
        try {
            return (T) document.getPropertyValue(value);
        } catch (PropertyException e) {
            throw new CorrespondenceRuntimeException(e);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public Calendar getDate() {
        return getPropertyValue(DATE_FIELD);
    }

    public MailEnvelope getMailEnvelope(CoreSession session) {
        MailEnvelope envelope;
        try {
            String envelopeDocumentId = (String) document.getPropertyValue(ENVELOPE_DOCUMENT_ID_FIELD);
            DocumentModel mailDocument = session.getDocument(new IdRef(
                    envelopeDocumentId));
            envelope = mailDocument.getAdapter(MailEnvelope.class);
        } catch (PropertyException e) {
            throw new CorrespondenceRuntimeException(e);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
        return envelope;
    }

    public String getId() {
        return document.getId();
    }

    public String getSender() {
        return getPropertyValue(SENDER_FIELD);
    }

    public String getSubject() {
        return getPropertyValue(SUBJECT_FIELD);
    }

    public String getSenderMailboxId() {
        return getPropertyValue(SENDER_MAILBOX_ID_FIELD);
    }

    public Date getSentDate() {
        return getPropertyValue(SENT_DATE_FIELD);
    }

    public String getType() {
        return getPropertyValue(TYPE_FIELD);
    }

    public boolean isRead() {
        return (Boolean) getPropertyValue(IS_READ_FIELD);
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
            session.save();
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public DocumentModel getDocument() {
        return document;
    }

    public boolean isDraft() {
        return (Boolean) getPropertyValue(IS_DRAFT_FIELD);
    }

    public void addInitialExternalRecipients(
            Map<String, List<String>> recipients) {
        recipientAdapter.addInitialExternalRecipients(recipients);
    }

    public void addInitialInternalRecipients(
            Map<String, List<String>> recipients) {
        recipientAdapter.addInitialInternalRecipients(recipients);
    }

    public void addRecipients(Map<String, List<String>> recipients) {
        recipientAdapter.addRecipients(recipients);
    }

    public Map<String, List<String>> getAllRecipients() {
        return recipientAdapter.getAllRecipients();
    }

    public Map<String, List<String>> getInitialExternalRecipients() {
        return recipientAdapter.getInitialExternalRecipients();
    }

    public Map<String, List<String>> getInitialInternalRecipients() {
        return recipientAdapter.getInitialInternalRecipients();
    }

}
