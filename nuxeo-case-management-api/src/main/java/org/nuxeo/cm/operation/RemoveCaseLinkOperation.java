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

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
@Operation(id = RemoveCaseLinkOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Remove a Case Link from a Mailbox", description = "This operation remove the case link attached to this chains from their mailbox")
public class RemoveCaseLinkOperation {
    public final static String ID = "Case.Management.Step.Remove.CaseLink";

    @Context
    protected OperationContext context;

    @OperationMethod
    public void removeCaseLink() {
        CaseLink link = (CaseLink) context.get(CaseConstants.OPERATION_CASE_LINK_KEY);
        getDistributionService().removeCaseLink(link, context.getCoreSession());
    }

    /**
     *
     */
    protected CaseDistributionService getDistributionService() {
        try {
            return Framework.getService(CaseDistributionService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
