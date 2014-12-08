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
package org.nuxeo.cm.service;

import java.util.Date;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public interface CaseManagementPersister {

    /**
     * Returns the case root path in the application.
     *
     * @since 1.7
     */
    String getCaseRootPath();

    /**
     * Sets the case root path in the application.
     *
     * @since 1.7
     */
    void setCaseRootPath(String caseRootPath);

    DocumentModel getParentDocumentForCase(CoreSession session);

    /**
     * @param session
     * @return
     */
    String getParentDocumentPathForCase(CoreSession session);

    /**
     * Default implementation uses date to find or create a set of folders representing the given date hierarchy.
     *
     * @param session
     * @param date
     * @return
     */
    DocumentModel getParentDocumentForCase(CoreSession session, Date date);

    /**
     * Default implementation uses date to find or create a set of folders representing the given date hierarchy.
     *
     * @param session
     * @param date
     * @return
     */
    String getParentDocumentPathForCase(CoreSession session, Date date);

    /**
     * @param item
     * @param session
     * @return
     */
    Case createCaseFromExistingCaseItem(CaseItem item, CoreSession session);

    /**
     * @param session
     * @param kase
     * @return
     */
    String getParentDocumentPathForCaseItem(CoreSession session, Case kase);

}
