/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.operation;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
@Operation(id = RemoveCaseLinkOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Remove Case Links from Mailboxes", description = RemoveCaseLinkOperation.DESCRIPTION)
public class RemoveCaseLinkOperation {

    public final static String ID = "Case.Management.Step.Remove.CaseLink";

    public final static String DESCRIPTION = "This operation remove case links from mailboxes fetching links from context and, if none, find the running link created from this step";

    @Context
    protected OperationContext context;

    @Context
    protected CoreSession session;

    @OperationMethod
    public void removeCaseLink() throws ClientException {
        List<CaseLink> links = fetchCaseLinks();
        for (CaseLink link : links) {
            getDistributionService().removeCaseLink(link, context.getCoreSession());
        }
    }

    protected List<CaseLink> fetchCaseLinks() throws ClientException {
        List<CaseLink> links = new ArrayList<CaseLink>();
        CaseLink link = (CaseLink) context.get(CaseConstants.OPERATION_CASE_LINK_KEY);
        if (link != null) {
            links.add(link);
        }
        @SuppressWarnings("unchecked")
        List<CaseLink> attachedLinks = (List<CaseLink>) context.get(CaseConstants.OPERATION_CASE_LINKS_KEY);
        if (attachedLinks != null && !attachedLinks.isEmpty()) {
            links.addAll(attachedLinks);
        }
        if (links.isEmpty()) {
            links.addAll(fetchCaseLinksFromStep());
        }
        return links;
    }

    protected List<CaseLink> fetchCaseLinksFromStep() throws ClientException {
        DocumentRouteStep step = (DocumentRouteStep) context.get(DocumentRoutingConstants.OPERATION_STEP_DOCUMENT_KEY);
        String query = String.format(
                "Select * from CaseLink where ecm:currentLifeCycleState = 'todo' and acslk:stepDocumentId = '%s'",
                step.getDocument().getId());
        DocumentModelList docs = session.query(query);
        List<CaseLink> result = new ArrayList<CaseLink>();
        for (DocumentModel doc : docs) {
            result.add(doc.getAdapter(ActionableCaseLink.class));
        }
        return result;
    }

    protected CaseDistributionService getDistributionService() {
        try {
            return Framework.getService(CaseDistributionService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
