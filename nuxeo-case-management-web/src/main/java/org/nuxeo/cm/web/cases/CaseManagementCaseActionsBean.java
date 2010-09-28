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

package org.nuxeo.cm.web.cases;

import static org.jboss.seam.ScopeType.EVENT;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.web.distribution.CaseManagementDistributionActionsBean;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.mailbox.CaseManagementAbstractActionsBean;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteElement;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.routing.api.LocalizableDocumentRouteElement;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.model.impl.SelectDataModelImpl;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Nicolas Ulrich
 */
@Name("cmCaseActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CaseManagementCaseActionsBean extends
        CaseManagementAbstractActionsBean {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CaseManagementDistributionActionsBean.class);

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(required = true, create = true)
    protected NavigationContext navigationContext;

    private String relatedRouteModelDocumentId;

    private DocumentRoutingService documentRoutingService;

    /**
     * @return true if this envelope is still in draft
     */
    public boolean isInitialCase() throws ClientException {
        Case env = getCurrentCase();

        if (env != null) {
            return getCurrentCase().isDraft();
        } else {
            return false;
        }
    }

    /**
     * Removes a mail from the current envelope.
     * 
     * @param doc the mail to remove
     */
    public void removeCaseItem(DocumentModel doc) throws ClientException {
        Case currentEnvelope = getCurrentCase();
        CaseItem item = doc.getAdapter(CaseItem.class);
        currentEnvelope.removeCaseItem(item, documentManager);
    }

    /**
     * Check if the related route to this case is started (ready or running) or
     * no
     * 
     * @param doc the mail to remove
     */
    public boolean hasRelatedRoute() throws ClientException {
        relatedRouteModelDocumentId = getRelatedRouteModelDocument();
        if (StringUtils.isEmpty(relatedRouteModelDocumentId)) {
            return false;
        }
        return true;
    }

    public String getRelatedRouteModelDocument() {
        if (StringUtils.isEmpty(relatedRouteModelDocumentId)) {
            List<DocumentModel> relatedRoute;
            try {
                relatedRoute = findRelatedRouteDocument();
            } catch (ClientException e) {
                return "";
            }
            if (relatedRoute.size() > 0) {
                relatedRouteModelDocumentId = relatedRoute.get(0).getId();
            }
        }
        return relatedRouteModelDocumentId;
    }

    public void setRelatedRouteModelDocument(String relatedRouteModelDocumentId) {
        this.relatedRouteModelDocumentId = relatedRouteModelDocumentId;
    }

    public String startRouteRelatedToCase() throws ClientException {
        // if no relatedRouteModelDocumentId
        if (StringUtils.isEmpty(relatedRouteModelDocumentId)) {
            facesMessages.add(
                    FacesMessage.SEVERITY_WARN,
                    resourcesAccessor.getMessages().get(
                            "feedback.casemanagement.document.route.no.valid.route"));
            return null;
        }
        DocumentModel relatedRouteModel = documentManager.getDocument(new IdRef(
                relatedRouteModelDocumentId));
        // set currentCaseId to participatingDocuments on the route
        DocumentRoute route = relatedRouteModel.getAdapter(DocumentRoute.class);
        List<String> documentIds = new ArrayList<String>();
        documentIds.add(getCurrentCase().getDocument().getId());
        route.setAttachedDocuments(documentIds);
        route.save(documentManager);
        getDocumentRoutingService().createNewInstance(
                route, route.getAttachedDocuments(), documentManager);
        resetCaseInfo();
        return null;
    }

    public List<DocumentModel> findRelatedRouteDocument()
            throws ClientException {
        List<DocumentModel> docs = new ArrayList<DocumentModel>();
        List<DocumentRoute> relatedRoutes = getDocumentRoutingService().getRelatedDocumentRoutesForAttachedDocument(
                documentManager, getCurrentCase().getDocument().getId());
        for (DocumentRoute documentRoute : relatedRoutes) {
            docs.add(documentRoute.getDocument());
        }
        return docs;
    }

    @Factory(value = "relatedRouteElementsSelectModel", scope = EVENT)
    public SelectDataModel computeSelectDataModelRouteElements()
            throws ClientException {
        return new SelectDataModelImpl("cm_route_elements",
                computeRelatedRouteElements(), null);
    }

    private ArrayList<LocalizableDocumentRouteElement> computeRelatedRouteElements()
            throws ClientException {
        DocumentModel relatedRouteDocumentModel = documentManager.getDocument(new IdRef(
                getRelatedRouteModelDocument()));
        DocumentRouteElement currentRouteModelElement = relatedRouteDocumentModel.getAdapter(DocumentRouteElement.class);
        ArrayList<LocalizableDocumentRouteElement> routeElements = new ArrayList<LocalizableDocumentRouteElement>();
        getDocumentRoutingService().getRouteElements(currentRouteModelElement,
                documentManager, routeElements, 0);
        return routeElements;
    }

    public DocumentRoutingService getDocumentRoutingService() {
        try {
            if (documentRoutingService == null) {
                documentRoutingService = Framework.getService(DocumentRoutingService.class);
            }
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
        return documentRoutingService;
    }

    @Override
    protected void resetCaseCache(Case cachedEnvelope, Case newEnvelope)
            throws ClientException {
        super.resetCaseCache(cachedEnvelope, newEnvelope);
        resetCaseInfo();
    }

    public void resetCaseInfo() {
        relatedRouteModelDocumentId = null;
    }
}