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

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
@Operation(id = CreateCaseLinkOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Case Link creation", description = "Create a CaseLink to be used latter in the chain.")
public class CreateCaseLinkOperation {
    public final static String ID = "Case.Management.CreateCaseLink";

    @Context
    OperationContext context;

    @OperationMethod
    public DocumentModel createCaseLink(DocumentModel doc) {
        CoreSession session = context.getCoreSession();
        try {
            DocumentModel model = session.createDocumentModel(CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE);
            model.setPropertyValue(CaseLinkConstants.CASE_DOCUMENT_ID_FIELD,
                    doc.getId());
            CaseLink cl = model.getAdapter(CaseLink.class);
            context.put(CaseConstants.OPERATION_CASE_LINK_KEY, cl);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }
}
