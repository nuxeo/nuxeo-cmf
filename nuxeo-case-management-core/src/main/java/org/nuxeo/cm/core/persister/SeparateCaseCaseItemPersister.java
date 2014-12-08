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
package org.nuxeo.cm.core.persister;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.service.CaseManagementPersister;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class SeparateCaseCaseItemPersister extends CaseManagementAbstractPersister implements CaseManagementPersister {

    @Override
    public Case createCaseFromExistingCaseItem(CaseItem item, CoreSession session) {
        DocumentModel parent;
        try {
            parent = session.getDocument(item.getDocument().getParentRef());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return item.createMailCase(session, parent.getPathAsString(), null);
    }

    @Override
    public String getParentDocumentPathForCaseItem(CoreSession session, Case kase) {
        return getParentDocumentPathForCase(session);
    }
}
