/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id$
 */

package org.nuxeo.cm.web.routing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.web.CaseManagementWebConstants;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteElement;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * Related routing web actions.
 * 
 * @author Laurent Doguin
 */
@Name("cmRoutingActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CaseManagementContextBound
public class CaseManagementRoutingActionsBean extends
        CaseManagementContextBoundInstance {

    private static final String QUERY_ACTIONNABLE_CASE_LINK_FROM_CASE = "SELECT * FROM CaseLink WHERE cslk:isActionable = 1 AND cslk:caseDocumentId = %s";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseManagementRoutingActionsBean.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(required = true, create = true)
    protected NavigationContext navigationContext;

    @In(required = true, create = true)
    protected WebActions webActions;

    protected Boolean showHistoryPanel = false;

    public String navigateToRouteTab(DocumentModel routeDoc)
            throws ClientException {
        String view = navigationContext.navigateToDocument(routeDoc,
                "view_documents");
        webActions.setCurrentTabId(CaseManagementWebConstants.DOCUMENT_ROUTE_TAB_ID);
        return view;
    }

    public List<DocumentRoute> getCurrentDocumentLinkedRoute() throws Exception {
        List<DocumentRoute> routes = new LinkedList<DocumentRoute>();
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        if (currentDoc == null) {
            return routes;
        }
        List<DocumentRouteElement.ElementLifeCycleState> states = new ArrayList<DocumentRouteElement.ElementLifeCycleState>();
        states.add(DocumentRouteElement.ElementLifeCycleState.ready);
        states.add(DocumentRouteElement.ElementLifeCycleState.running);
        states.add(DocumentRouteElement.ElementLifeCycleState.done);
        routes = getDocumentRoutingService().getDocumentRoutesForAttachedDocument(
                documentManager, currentDoc.getId(), states);
        return routes;
    }

    public DocumentRoutingService getDocumentRoutingService() {
        try {
            return Framework.getService(DocumentRoutingService.class);

        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    public DocumentModelList getPendingActionnableClsk(DocumentModel caseDoc)
            throws ClientException {
        String query = String.format(QUERY_ACTIONNABLE_CASE_LINK_FROM_CASE,
                caseDoc.getId());
        DocumentModelList pendingAClsk = documentManager.query(query);
        return pendingAClsk;
    }

    public Boolean getShowHistoryPanel() {
        return showHistoryPanel;
    }

    public  void setShowHistoryPanel(Boolean showHistoryPanel) {
        this.showHistoryPanel = showHistoryPanel;
    }
}
