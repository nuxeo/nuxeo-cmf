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
import org.nuxeo.cm.service.CorrespondenceDocumentTypeService;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.runtime.api.Framework;


/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class MailEnvelopeItemImpl implements MailEnvelopeItem {

    private static final long serialVersionUID = 1L;

    protected MailEnvelope envelope;

    protected HasRecipients recipientAdapter;

    protected DocumentModel document;

    public MailEnvelopeItemImpl(DocumentModel document,
            HasRecipients recipientAdapter) {
        super();
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

    public String getDefaultEnvelopeId() {
        return getStringProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID);
    }

    public Calendar getDocumentDate() {
        return getDateProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DATE);
    }

    public MailEnvelope getEnvelope() {
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

    public void setDefaultEnvelope(String mailEnvelopeId) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID,
                mailEnvelopeId);
    }

    public void setDocumentDate(Calendar date) {
        setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DATE, date);
    }

    public void setEnvelope(MailEnvelope envelope) {
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
        if (!(other instanceof MailEnvelopeItemImpl)) {
            return false;
        }
        MailEnvelopeItemImpl otherItem = (MailEnvelopeItemImpl) other;
        return document.equals(otherItem.document);
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
            session.save();
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public MailEnvelope createMailEnvelope(CoreSession session,
            String parentPath, String initialLifeCysleState) {
        try {
            String emailTitle = getTitle();
            String envelopeid = IdUtils.generateId(emailTitle == null ? ""
                    : emailTitle);
            MailEnvelope mailEnvelope = createEnvelope(session,
                    parentPath, envelopeid, initialLifeCysleState);
            mailEnvelope.addMailEnvelopeItem(
                    document.getAdapter(MailEnvelopeItem.class), session);
            mailEnvelope.save(session);
            return mailEnvelope;
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected MailEnvelope createEnvelope(CoreSession session,
            String parentPath, String id, String initialLifeCysleState)
            throws ClientException {

        CorrespondenceDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        DocumentModel envelope = session.createDocumentModel(parentPath, id,
                correspDocumentTypeService.getEnvelopeType());
        envelope.setPropertyValue(CaseConstants.TITLE_PROPERTY_NAME,
                document.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME));
        // FIXME: make this constant available in nuxeo-core-api
        envelope.putContextData("initialLifecycleState", initialLifeCysleState);
        envelope = session.createDocument(envelope);
        return envelope.getAdapter(MailEnvelope.class);
    }

    @Override
    public String toString() {
        return document.toString();
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
