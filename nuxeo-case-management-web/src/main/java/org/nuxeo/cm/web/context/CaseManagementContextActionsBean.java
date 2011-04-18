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
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;

@Name("cmContextActions")
@Scope(ScopeType.CONVERSATION)
public class CaseManagementContextActionsBean implements Serializable,
        CaseManagementContextActions {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected transient CaseManagementContextHolderBean cmContextHolder;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @Override
    public String getCurrentCaseItemId() throws ClientException {
        DocumentModel currentEmail = cmContextHolder.getCurrentCaseItem();
        if (currentEmail != null) {
            return currentEmail.getId();
        }
        return null;
    }

    @Override
    public void setCurrentCaseItemId(String id) throws ClientException {
        if (id != null && documentManager != null) {
            id = id.trim();
            if (!"".equals(id)) {
                DocumentModel currentEmail = documentManager.getDocument(new IdRef(
                        id));
                if (currentEmail != null) {
                    cmContextHolder.setCurrentCaseItem(currentEmail);
                }
            }
        }
    }

    // XXX: see if needs to be moved
    @Override
    @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED }, create = true)
    public void currentDocumentChanged(DocumentModel newDocument) {
        if (newDocument != null) {
            // mailbox case
            if (newDocument.hasFacet(CaseConstants.MAILBOX_FACET)) {
                cmContextHolder.setCurrentMailbox(newDocument.getAdapter(Mailbox.class));
            }
            // document cases
            if (newDocument.hasFacet(CaseConstants.DISTRIBUTABLE_FACET)
                    && !newDocument.hasFacet(CaseConstants.CASE_GROUPABLE_FACET)) {
                cmContextHolder.setCurrentCase(newDocument.getAdapter(Case.class));
                cmContextHolder.setCurrentCaseItem(null);
            } else if (newDocument.hasFacet(CaseConstants.DISTRIBUTABLE_FACET)
                    && newDocument.hasFacet(CaseConstants.CASE_GROUPABLE_FACET)) {
                cmContextHolder.setCurrentCaseItem(newDocument);
            } else if (newDocument.hasSchema("classification")) {
                cmContextHolder.setCurrentCase(null);
                cmContextHolder.setCurrentCaseItem(null);
            }
        }
    }

    public boolean currentDocumentIsDistributable() {
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        if (currentDoc == null) {
            return false;
        }
        return currentDoc.hasFacet(CaseConstants.DISTRIBUTABLE_FACET);
    }
}
