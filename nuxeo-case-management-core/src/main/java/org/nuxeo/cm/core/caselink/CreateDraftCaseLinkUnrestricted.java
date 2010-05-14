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
import static org.nuxeo.cm.caselink.CaseLinkConstants.DATE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_SENT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_CASE_FOLDER_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SUBJECT_FIELD;

import java.util.Calendar;
import java.util.UUID;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;

/**
 * A creator of {@link CaseLink}.
 *
 * @author Nicolas Ulrich
 */
public class CreateDraftCaseLinkUnrestricted extends UnrestrictedSessionRunner {

    protected CaseLink createdPost;

    protected final String subject;

    protected final Case envelope;

    protected final CaseFolder sender;

    public CaseLink getCreatedPost() {
        return createdPost;
    }

    public CreateDraftCaseLinkUnrestricted(String repositoryName, String subject,
            Case envelope, CaseFolder sender) {
        super(repositoryName);
        this.envelope = envelope;
        this.subject = subject;
        this.sender = sender;
    }

    @Override
    public void run() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        DocumentModel doc = session.createDocumentModel(
                sender.getDocument().getPathAsString(),
                UUID.randomUUID().toString(), correspDocumentTypeService.getCaseLinkType());
        setPostValues(doc);
        doc = session.createDocument(doc);
        session.save();
        createdPost = doc.getAdapter(CaseLink.class);
    }

    /**
     * Sets the values of the document.
     */
    protected void setPostValues(DocumentModel doc) throws ClientException {
        // FIXME: use CorrespondencePost setters
        doc.setPropertyValue(IS_DRAFT_FIELD, true);
        doc.setPropertyValue(SUBJECT_FIELD, subject);
        doc.setPropertyValue(CASE_REPOSITORY_NAME_FIELD,
                envelope.getDocument().getRepositoryName());
        doc.setPropertyValue(CASE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        doc.setPropertyValue(SENDER_CASE_FOLDER_ID_FIELD, sender.getId());
        doc.setPropertyValue(DATE_FIELD, Calendar.getInstance().getTime());
        doc.setPropertyValue(SENDER_FIELD, sender.getId());
        doc.setPropertyValue(IS_SENT_FIELD, false);
        // FIXME: what should be put here? because uid schema is not forced on
        // envelope
        // doc.setPropertyValue(ENVELOPE_ID_FIELD,
        // envelope.getDocument().getPropertyValue("uid:uid"));
    }
}
