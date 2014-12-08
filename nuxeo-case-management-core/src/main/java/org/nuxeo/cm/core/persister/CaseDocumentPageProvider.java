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
 *     Laurent Doguin
 */
package org.nuxeo.cm.core.persister;

import static org.nuxeo.cm.cases.CaseConstants.CASE_GROUPABLE_FACET;
import static org.nuxeo.cm.cases.CaseConstants.DISTRIBUTABLE_FACET;

import java.util.List;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;

/**
 * CaseDocumentPageProvider fetch case children using Case adapter or CaseItem children using CoreSession API.
 *
 * @author Laurent Doguin
 * @since 1.8
 */
public class CaseDocumentPageProvider extends CoreQueryDocumentPageProvider {

    private static final long serialVersionUID = 1L;

    protected List<DocumentModel> currentPageDocuments;

    @Override
    public List<DocumentModel> getCurrentPage() {
        CoreSession coreSession = (CoreSession) getProperties().get(CORE_SESSION_PROPERTY);
        String parentId = (String) getParameters()[0];
        DocumentRef refId = new IdRef(parentId);
        DocumentModel parent;
        try {
            parent = coreSession.getDocument(refId);
            if (parent.hasFacet(DISTRIBUTABLE_FACET) && !parent.hasFacet(CASE_GROUPABLE_FACET)) {
                Case kase = parent.getAdapter(Case.class);
                return kase.getDocuments(coreSession);
            } else {
                return coreSession.getChildren(parent.getRef(), null, SecurityConstants.READ);
            }
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

}
