/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *
 * $Id: MailEnvelopeItemImpl.java 57494 2008-09-11 17:17:23Z atchertchian $
 */

package org.nuxeo.cm.cases;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class CaseItemImpl implements CaseItem {

    private static final String QUERY_CASES = "SELECT * FROM Document WHERE case:documentsId = '%s' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState != 'deleted' AND ecm:isCheckedInVersion = 0";

    private static final long serialVersionUID = 1L;

    protected Case envelope;

    protected final HasParticipants recipientAdapter;

    protected final DocumentModel document;

    public CaseItemImpl(DocumentModel document, HasParticipants recipientAdapter) {
        this.document = document;
        this.recipientAdapter = recipientAdapter;
    }

    public DocumentModel getDocument() {
        return document;
    }

    public String getConfidentiality() {
        return getStringProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_CONFIDENTIALITY);
    }

    protected String getStringProperty(String schema, String value) {
        try {
            return (String) document.getProperty(schema, value);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected void setProperty(String schema, String property, Object value) {
        try {
            document.setProperty(schema, property, value);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public String getTitle() {
        try {
            return (String) document.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public void setTitle(String title) {
        try {
            this.document.setPropertyValue(CaseConstants.TITLE_PROPERTY_NAME,
                    title);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected Calendar getDateProperty(String schema, String value) {
        try {
            return (Calendar) document.getProperty(schema, value);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public String getDefaultCaseId() {
        return getStringProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID);
    }

    public Calendar getDocumentDate() {
        return getDateProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DATE);
    }

    public Case getCase() {
        return envelope;
    }

    public Calendar getImportDate() {
        return getDateProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_IMPORT_DATE);
    }

    public String getOrigin() {
        return getStringProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_ORIGIN);
    }

    public Calendar getReceiveDate() {
        return getDateProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_RECEIVE_DATE);
    }

    public Calendar getSendingDate() {
        return getDateProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_SENDING_DATE);
    }

    public String getType() {
        return document.getType();
    }

    public void setConfidentiality(String cdf) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_CONFIDENTIALITY, cdf);
    }

    public void setDefaultCase(String mailEnvelopeId) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID, mailEnvelopeId);
    }

    public void setDocumentDate(Calendar date) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DATE, date);
    }

    public void setCase(Case envelope) {
        this.envelope = envelope;
    }

    public void setImportDate(Calendar date) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_IMPORT_DATE, date);
    }

    public void setOrigin(String origin) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_ORIGIN, origin);
    }

    public void setReceiveDate(Calendar date) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_RECEIVE_DATE, date);
    }

    public void setSendingDate(Calendar date) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_SENDING_DATE, date);
    }

    public void setType(String type) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.CASE_ITEM_DOCUMENT_TYPE, type);
    }

    /**
     * Overrides equality to check documents equality
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CaseItemImpl)) {
            return false;
        }
        CaseItemImpl otherItem = (CaseItemImpl) other;
        return document.equals(otherItem.document);
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public Case createMailCase(CoreSession session, String parentPath,
            String initialLifeCycleState) {
        try {
            String emailTitle = getTitle();
            String envelopeid = IdUtils.generateId(emailTitle == null ? ""
                    : emailTitle);
            Case mailEnvelope = createEnvelope(session, parentPath, envelopeid,
                    initialLifeCycleState);
            mailEnvelope.addCaseItem(document.getAdapter(CaseItem.class),
                    session);
            mailEnvelope.save(session);
            return mailEnvelope;
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected Case createEnvelope(CoreSession session, String parentPath,
            String id, String initialLifeCycleState) throws ClientException {

        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        DocumentModel envelope = session.createDocumentModel(parentPath, id,
                correspDocumentTypeService.getCaseType());
        envelope.setPropertyValue(CaseConstants.TITLE_PROPERTY_NAME,
                document.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME));
        // FIXME: make this constant available in nuxeo-core-api
        envelope.putContextData("initialLifecycleState", initialLifeCycleState);
        envelope = session.createDocument(envelope);
        return envelope.getAdapter(Case.class);
    }

    @Override
    public String toString() {
        return document.toString();
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

    public DocumentModelList getCases(CoreSession session) {
        try {
            String query = String.format(QUERY_CASES, document.getId());
            return session.query(query);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public boolean hasSeveralCases(CoreSession coreSession) {
        DocumentModelList envelopes = getCases(coreSession);
        return envelopes.size() > 1;
    }

}
