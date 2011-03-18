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
 *     Alexandre Russel
 */
package org.nuxeo.cm.operation;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteElement;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class TestOperationChains extends CaseManagementRepositoryTestCase {

    public void testChainsDeclaration() throws Exception {
        assertNotNull(routingService);
        assertNotNull(automationService);
        String chainId = routingService.getOperationChainId(CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_STEP);
        assertEquals(chainId,
                CaseConstants.OPERATION_CHAIN_DISTRIBUTION_STEP_CHAIN);
    }

    public void testDistributionTaskChain() throws Exception {
        openSession();
        String chainId = routingService.getOperationChainId(CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK);
        assertEquals(chainId,
                CaseConstants.OPERATION_CHAIN_DISTRIBUTION_TASK_CHAIN);
        OperationContext ctx = new OperationContext(session);
        DocumentModel stepDocument = createDocumentModel(session, "step1",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK, "/");
        DocumentRouteStep step = stepDocument.getAdapter(DocumentRouteStep.class);
        ctx.put(DocumentRoutingConstants.OPERATION_STEP_DOCUMENT_KEY, step);
        DocumentModel processedDocument = createDocumentModel(session, "foo",
                CaseConstants.CASE_TYPE, "/");
        session.save();
        ctx.setInput(processedDocument);
        automationService.run(ctx, chainId);
    }

    public void testDistributionStepChain() throws Exception {
        openSession();
        String chainId = routingService.getOperationChainId(CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_STEP);
        assertEquals(chainId,
                CaseConstants.OPERATION_CHAIN_DISTRIBUTION_STEP_CHAIN);
        OperationContext ctx = new OperationContext(session);
        DocumentModel stepDocument = createDocumentModel(session, "step1",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_STEP, "/");
        stepDocument.followTransition(DocumentRouteElement.ElementLifeCycleTransistion.toValidated.name());
        stepDocument.followTransition(DocumentRouteElement.ElementLifeCycleTransistion.toReady.name());
        stepDocument.followTransition(DocumentRouteElement.ElementLifeCycleTransistion.toRunning.name());
        DocumentRouteStep step = stepDocument.getAdapter(DocumentRouteStep.class);
        ctx.put(DocumentRoutingConstants.OPERATION_STEP_DOCUMENT_KEY, step);
        DocumentModel processedDocument = createDocumentModel(session, "foo",
                CaseConstants.CASE_TYPE, "/");
        DocumentModelList listOfDocs = new DocumentModelListImpl();
        listOfDocs.add(processedDocument);
        session.save();
        ctx.setInput(listOfDocs);
        automationService.run(ctx, chainId);
    }
}
