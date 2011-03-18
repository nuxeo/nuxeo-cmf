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
package org.nuxeo.cm.core.event;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public abstract class AbstractDraftListener {

    protected abstract Log getLog();

    protected abstract String getEventName();

    public void handleEvent(Event event) throws ClientException {
        if (!getEventName().equals(event.getName())) {
            getLog().warn(
                    "DraftUpdatedListener called on a non DraftUpdatedListener event: "
                            + event);
            return;
        }
        DocumentEventContext docCtx = null;
        Principal principal = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
            principal = docCtx.getPrincipal();
        } else {
            getLog().warn(
                    "DraftUpdatedListener called with non DocumentEventContext context: "
                            + event.getContext());
            return;
        }
        CoreSession session = docCtx.getCoreSession();
        DocumentModel envelopeDM = docCtx.getSourceDocument();
        if (envelopeDM == null) {
            return;
        }

        // Check if the document model is an envelope
        if (!(envelopeDM.hasFacet(CaseConstants.DISTRIBUTABLE_FACET) && !envelopeDM.hasFacet(CaseConstants.CASE_GROUPABLE_FACET))) {
            return;
        }

        Case envelope = envelopeDM.getAdapter(Case.class);
        if (envelope == null) {
            return;
        }
        CaseItem item = envelope.getFirstItem(session);
        if (item == null) {
            return;
        }
        DocumentModel firstDoc = item.getDocument();
        CaseLink draft = (CaseLink) docCtx.getProperties().get(
                CaseManagementEventConstants.EVENT_CONTEXT_DRAFT);
        updateDraft(draft.getDocument(), firstDoc, envelopeDM, principal);
        session.saveDocument(draft.getDocument());
    }

    protected void updateDraft(DocumentModel draft, DocumentModel firstDoc,
            DocumentModel envelope, Principal principal) throws ClientException {
        draft.setPropertyValue(CaseConstants.CONTACTS_SENDERS,
                firstDoc.getPropertyValue(CaseConstants.CONTACTS_SENDERS));
        draft.setPropertyValue(CaseConstants.CONTACTS_PARTICIPANTS,
                firstDoc.getPropertyValue(CaseConstants.CONTACTS_PARTICIPANTS));
        draft.setPropertyValue(CaseLinkConstants.SENDER_FIELD,
                principal.getName());
    }

}
