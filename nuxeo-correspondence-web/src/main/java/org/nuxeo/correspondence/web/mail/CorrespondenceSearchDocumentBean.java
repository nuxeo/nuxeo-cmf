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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.correspondence.web.mail;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

/**
 * @author Nicolas Ulrich
 */
@Name("correspSearchDocument")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CorrespondenceSearchDocumentBean extends
        CaseManagementContextBoundInstance implements
        CorrespondenceSearchDocument {

    private static final long serialVersionUID = 1L;

    protected static final String MAIL_ATTACHEMENT_SEARCH = "MAIL_ATTACHEMENT_SEARCH";

    protected static final String MAIL_ATTACHEMENT_SEARCH_PROVIDER = "MAIL_ATTACHEMENT_SEARCH_PROVIDER";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentsListsManager documentsListsManager;

    protected String searchString;

    protected boolean hasSearchResults;

    public String getSearchKeywords() {
        return searchString;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchString = searchKeywords;
    }

    public boolean isHasSearchResults() {
        return hasSearchResults;
    }

    public void searchMailDocument() {
        hasSearchResults = true;
    }

    public String cancelEmailAttachmentsSearch() throws ClientException {
        resetEmailAttachmentsSearchResults();
        // navigate to current email edit view
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return navigationContext.navigateToDocument(currentDoc);
    }

    protected void resetEmailAttachmentsSearchResults() {
        hasSearchResults = false;
        searchString = null;
        documentsListsManager.resetWorkingList(MAIL_ATTACHEMENT_SEARCH);
    }

    /**
     * Adds selected email to current envelope documents
     * <p>
     * As long as envelope is not saved, relations are not updated, but current
     * envelope keeps track of its email documents.
     */
    public String addSelectedEmails() throws ClientException {

        if (!documentsListsManager.isWorkingListEmpty(MAIL_ATTACHEMENT_SEARCH)) {
            List<DocumentModel> selectedDocuments = documentsListsManager.getWorkingList(MAIL_ATTACHEMENT_SEARCH);
            Case currentEnvelope = getCurrentCase();
            for (DocumentModel document : selectedDocuments) {
                if (document != null) {
                    currentEnvelope.addCaseItem(
                            document.getAdapter(CaseItem.class),
                            documentManager);
                }
            }
        }

        // navigate to current email edit view
        return cancelEmailAttachmentsSearch();
    }

}
