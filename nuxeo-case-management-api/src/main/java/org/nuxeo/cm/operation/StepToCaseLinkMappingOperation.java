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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkType;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
@Operation(id = StepToCaseLinkMappingOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Step To CaseLink Mapping", description = "Create a CaseLink from the value of the Step docuemnt")
public class StepToCaseLinkMappingOperation {

    public static final String STEP_PREFIX = "Step:";

    public static final String CASE_PREFIX = "Case:";

    public final static String ID = "Case.Management.Step.CaseLink.Mapping";

    @Context
    protected OperationContext context;

    @Param(name = "actionnable")
    protected boolean actionnable = false;

    @Param(name = "mappingProperties")
    protected Properties mappingProperties;

    @Param(name = "leavingChainsProperties", required = false)
    protected Properties leavingChainsProperties;

    @OperationMethod
    public void mapCaseLinkOperation() {
        @SuppressWarnings("unchecked")
        List<CaseLink> links = (List<CaseLink>) context.get(CaseConstants.OPERATION_CASE_LINKS_KEY);
        CoreSession session = context.getCoreSession();
        for (CaseLink link : links) {
            link.setActionnable(actionnable);
            DocumentModel kaseDoc = link.getCase(session).getDocument();
            DocumentModel linkDoc = link.getDocument();
            DocumentRouteStep step = (DocumentRouteStep) context.get(DocumentRoutingConstants.OPERATION_STEP_DOCUMENT_KEY);
            DocumentModel stepDoc = step.getDocument();
            Map<String, List<String>> recipients = new HashMap<String, List<String>>();
            String recipient;
            try {
                recipient = (String) step.getDocument().getPropertyValue(
                        CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME);
                step.setCanValidateStep(context.getCoreSession(), recipient);
                recipients.put(CaseLinkType.FOR_ACTION.name(), Arrays.asList(new String[] { recipient }));
                if (recipient != null) {
                    link.addInitialInternalParticipants(recipients);
                }
                for (Map.Entry<String, String> prop : mappingProperties.entrySet()) {
                    String getter = prop.getKey();
                    String setter = prop.getValue();
                    DocumentModel setterDoc = null;
                    if (setter.startsWith(CASE_PREFIX)) {
                        setterDoc = kaseDoc;
                        setter = setter.substring(CASE_PREFIX.length());
                    } else if (setter.startsWith(STEP_PREFIX)) {
                        setterDoc = stepDoc;
                        setter = setter.substring(STEP_PREFIX.length());
                    }
                    linkDoc.setPropertyValue(getter, setterDoc.getPropertyValue(setter));
                }
                if (link.isActionnable()) {
                    ActionableCaseLink actionableLink = link.getDocument().getAdapter(ActionableCaseLink.class);
                    String refuseChainId = leavingChainsProperties.get("refuse");
                    String validateChainId = leavingChainsProperties.get("validate");
                    actionableLink.setRefuseOperationChainId(refuseChainId);
                    actionableLink.setValidateOperationChainId(validateChainId);
                    actionableLink.setStepId(stepDoc.getId());
                }
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
