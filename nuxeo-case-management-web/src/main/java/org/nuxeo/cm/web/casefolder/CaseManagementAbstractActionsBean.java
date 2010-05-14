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
 *     Nicolas Ulrich
 *
 * $Id$
 */
package org.nuxeo.cm.web.casefolder;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PagedDocumentsProvider;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.model.impl.SelectDataModelImpl;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.pagination.ResultsProvidersCache;


/**
 * Provide ResultsProvidersCache access in order to get SelectDataModel from a
 * provider name.
 *
 * @author nulrich
 *
 */
public abstract class CaseManagementAbstractActionsBean extends
        CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 1L;

    static String RESULTS_PROVIDERS_CACHE = "resultsProvidersCache";

    static String CHILDREN_DOCUMENT_LIST = "CHILDREN_DOCUMENT_LIST";

    @In(required = false, create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @Out(required = false)
    protected PagedDocumentsProvider resultsProvider;

    /**
     * Retrieve a SelectDataModel in the cache from it the provider name
     *
     * @param providerName
     * @return
     * @throws ClientException
     */
    protected SelectDataModel getSelectDataModelFromProvider(
            final String providerName) throws ClientException {

        ResultsProvidersCache resultsProvidersCache = (ResultsProvidersCache) Component.getInstance(RESULTS_PROVIDERS_CACHE);

        resultsProvider = resultsProvidersCache.get(providerName);

        DocumentModelList documents = resultsProvider.getCurrentPage();
        List<DocumentModel> selectedDocuments = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        SelectDataModel model = new SelectDataModelImpl(CHILDREN_DOCUMENT_LIST,
                documents, selectedDocuments);

        return model;
    }

}
