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

import java.util.Map;
import java.util.Properties;

import org.nuxeo.cm.caselink.AcionnableCaseLink;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
@Operation(id = StepToCaseLinkMappingOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Step To CaseLink Mapping", description = "Create a CaseLink from the value of the Step docuemnt")
public class StepToCaseLinkMappingOperation {
    public final static String ID = "Case.Management.Step.CaseLink.Mapping";

    @Context
    protected OperationContext context;

    @Param(name = "actionnable")
    protected boolean actionnable;

    @Param(name = "mappingProperties")
    protected Properties mappingProperties;

    public void mapCaseLinkOperation() {
        AcionnableCaseLink link = (AcionnableCaseLink) context.get(CaseConstants.OPERATION_CASE_LINK_KEY);
        link.setActionnable(actionnable);
        DocumentModel linkDoc = link.getDocument();
        DocumentRouteStep step = (DocumentRouteStep) context.get(DocumentRoutingConstants.OPERATION_STEP_DOCUMENT_KEY);
        DocumentModel stepDoc = step.getDocument();
        for(Map.Entry<Object, Object> prop: mappingProperties.entrySet()) {
            String linkXPath = (String) prop.getKey();
            String stepXPath = (String) prop.getValue();
            try {
                linkDoc.setPropertyValue(linkXPath, stepDoc.getPropertyValue(stepXPath));
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            } catch (ClientException e) {
                throw new RuntimeException(e);
            }
        }
        link.save(context.getCoreSession());
    }
}
