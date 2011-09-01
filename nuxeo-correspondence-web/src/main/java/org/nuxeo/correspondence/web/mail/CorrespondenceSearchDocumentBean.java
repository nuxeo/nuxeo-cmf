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

import javax.faces.application.FacesMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PagedDocumentsProvider;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.ResultsProviderFarm;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.model.impl.SelectDataModelImpl;
import org.nuxeo.ecm.platform.ui.web.pagination.ResultsProviderFarmUserException;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.pagination.ResultsProvidersCache;
import org.nuxeo.ecm.webapp.querymodel.QueryModelActions;

/**
 * @author Nicolas Ulrich
 */
@Name("correspSearchDocument")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CorrespondenceSearchDocumentBean extends
        CaseManagementContextBoundInstance implements
        CorrespondenceSearchDocument, ResultsProviderFarm {

    protected static final String MAIL_ATTACHEMENT_SEARCH = "MAIL_ATTACHEMENT_SEARCH";

    protected static final String MAIL_ATTACHEMENT_SEARCH_PROVIDER = "MAIL_ATTACHEMENT_SEARCH_PROVIDER";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CorrespondenceSearchDocumentBean.class);

    @In(required = true)
    protected transient QueryModelActions queryModelActions;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @In(create = true, required = false)
    protected transient ResultsProvidersCache resultsProvidersCache;

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
        try {
            // TODO: Search unrestricted
            resultsProvidersCache.invalidate(MAIL_ATTACHEMENT_SEARCH_PROVIDER);
            DocumentModelList searchEmailResults = resultsProvidersCache.get(
                    MAIL_ATTACHEMENT_SEARCH_PROVIDER).getCurrentPage();
            hasSearchResults = !searchEmailResults.isEmpty();
        } catch (ClientException e) {
            facesMessages.add(FacesMessage.SEVERITY_WARN,
                    resourcesAccessor.getMessages().get(
                            "label.search.service.wrong.query"));
            // log.error("ClientException in search popup : " +
            // e.getMessage());
        }
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
        resultsProvidersCache.invalidate(MAIL_ATTACHEMENT_SEARCH_PROVIDER);
        documentsListsManager.resetWorkingList(MAIL_ATTACHEMENT_SEARCH);
    }

    public SelectDataModel getSearchEmailResults() throws ClientException {
        DocumentModelList searchEmailResults = resultsProvidersCache.get(
                MAIL_ATTACHEMENT_SEARCH_PROVIDER).getCurrentPage();
        List<DocumentModel> selectedDocuments = documentsListsManager.getWorkingList(MAIL_ATTACHEMENT_SEARCH);
        SelectDataModel model = new SelectDataModelImpl(
                MAIL_ATTACHEMENT_SEARCH, searchEmailResults, selectedDocuments);
        return model;
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

    public PagedDocumentsProvider getResultsProvider(String name)
            throws ClientException, ResultsProviderFarmUserException {
        return getResultsProvider(name, null);
    }

    public PagedDocumentsProvider getResultsProvider(String name,
            SortInfo sortInfo) throws ClientException,
            ResultsProviderFarmUserException {
        PagedDocumentsProvider provider = null;

        if (MAIL_ATTACHEMENT_SEARCH_PROVIDER.equals(name)) {
            Object[] params = { searchString };
            try {
                provider = getQmDocuments(name, params, sortInfo);
            } catch (Exception e) {
                log.error("sorted query failed");
                log.debug(e);
                log.error("retrying without sort parameters");
                provider = getQmDocuments(name, params, null);
            }
        }

        if (provider == null) {
            throw new ClientException(String.format(
                    "Provider '%s' is not handled by this component", name));
        }

        provider.setName(name);
        return provider;

    }

    protected PagedDocumentsProvider getQmDocuments(String qmName,
            Object[] params, SortInfo sortInfo) throws ClientException {
        return queryModelActions.get(qmName).getResultsProvider(
                documentManager, params, sortInfo);
    }

}
