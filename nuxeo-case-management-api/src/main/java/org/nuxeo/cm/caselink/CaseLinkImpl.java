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
package org.nuxeo.cm.caselink;

import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.COMMENT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.DATE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_READ_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_MAILBOX_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENT_DATE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SUBJECT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.TYPE_FIELD;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class CaseLinkImpl implements CaseLink {

    private static final long serialVersionUID = -94563234903621891L;

    protected HasParticipants recipientAdapter;

    protected DocumentModel document;

    public CaseLinkImpl(DocumentModel doc, HasParticipants recipientAdapted) {
        document = doc;
        recipientAdapter = recipientAdapted;
    }

    public String getComment() {
        return getPropertyValue(COMMENT_FIELD);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getPropertyValue(String value) {
        try {
            return (T) document.getPropertyValue(value);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public Calendar getDate() {
        return getPropertyValue(DATE_FIELD);
    }

    public Case getCase(CoreSession session) {
        Case envelope;
        try {
            String envelopeDocumentId = (String) document.getPropertyValue(CASE_DOCUMENT_ID_FIELD);
            DocumentModel mailDocument = session.getDocument(new IdRef(
                    envelopeDocumentId));
            envelope = mailDocument.getAdapter(Case.class);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
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
            throw new CaseManagementRuntimeException(e);
        }
    }

    public DocumentModel getDocument() {
        return document;
    }

    public boolean isDraft() {
        return (Boolean) getPropertyValue(IS_DRAFT_FIELD);
    }

    public void addInitialExternalParticipants(
            Map<String, List<String>> recipients) {
        recipientAdapter.addInitialExternalParticipants(recipients);
    }

    public void addInitialInternalParticipants(
            Map<String, List<String>> recipients) {
        recipientAdapter.addInitialInternalParticipants(recipients);
    }

    public void addParticipants(Map<String, List<String>> recipients) {
        recipientAdapter.addParticipants(recipients);
    }

    public Map<String, List<String>> getAllParticipants() {
        return recipientAdapter.getAllParticipants();
    }

    public Map<String, List<String>> getInitialExternalParticipants() {
        return recipientAdapter.getInitialExternalParticipants();
    }

    public Map<String, List<String>> getInitialInternalParticipants() {
        return recipientAdapter.getInitialInternalParticipants();
    }

    @Override
    public void setActionnable(boolean actionnable) {
        try {
            document.setPropertyValue(CaseLinkConstants.IS_ACTIONABLE_FIELD,
                    actionnable);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isActionnable() {
        return (Boolean) getPropertyValue(CaseLinkConstants.IS_ACTIONABLE_FIELD);
    }

}
