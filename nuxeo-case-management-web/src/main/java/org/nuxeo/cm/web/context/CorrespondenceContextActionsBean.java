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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.web.context;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.webapp.helpers.EventNames;


@Name("correspContextActions")
@Scope(ScopeType.CONVERSATION)
public class CorrespondenceContextActionsBean implements Serializable,
        CorrespondenceContextActions {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected transient CorrespondenceContextHolderBean correspContextHolder;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    public String getCurrentEmailId() throws ClientException {
        DocumentModel currentEmail = correspContextHolder.getCurrentEmail();
        if (currentEmail != null) {
            return currentEmail.getId();
        }
        return null;
    }

    public void setCurrentEmailId(String id) throws ClientException {
        if (id != null && documentManager != null) {
            id = id.trim();
            if (!"".equals(id)) {
                DocumentModel currentEmail = documentManager.getDocument(new IdRef(
                        id));
                if (currentEmail != null) {
                    correspContextHolder.setCurrentEmail(currentEmail);
                }
            }
        }
    }

    // XXX: see if needs to be moved
    @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED }, create = true, inject = true)
    public void currentDocumentChanged(DocumentModel newDocument) {
        if (newDocument != null) {
            // mailbox case
            if (CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE.equals(newDocument.getType())) {
                correspContextHolder.setCurrentMailbox(newDocument.getAdapter(CaseFolder.class));
            }
            // document cases
            if (newDocument.hasFacet(CaseConstants.CASE_FACET)) {
                correspContextHolder.setCurrentEnvelope(newDocument.getAdapter(Case.class));
                correspContextHolder.setCurrentEmail(null);
            } else if (newDocument.hasFacet(CaseConstants.CASE_ITEM_FACET)) {
                correspContextHolder.setCurrentEnvelope(null);
                correspContextHolder.setCurrentEmail(newDocument);
            }
        }
    }
}
