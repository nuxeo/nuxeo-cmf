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

import java.util.List;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
@Operation(id = DistributionOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Distribute a case", description = "Distribute a case according to the CaseLink found in the context.")
public class DistributionOperation {
    public final static String ID = "Case.Management.Distribution";

    @Context
    protected OperationContext context;

    public CaseDistributionService getDistributionService() {
        try {
            return Framework.getService(CaseDistributionService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OperationMethod
    public void distribute() {
        CoreSession session = context.getCoreSession();
        @SuppressWarnings("unchecked")
        List<CaseLink> caseLinks = (List<CaseLink>) context.get(CaseConstants.OPERATION_CASE_LINK_KEY);
        for (CaseLink caseLink : caseLinks) {
            getDistributionService().sendCase(session, caseLink, false, true);
        }
    }
}
