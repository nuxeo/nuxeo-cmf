/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.casefolder;

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author Anahide Tchertchian
 *
 */
public class TestDefaultHierarchy extends CaseManagementRepositoryTestCase {

    private static final String CASE_MANAGEMENT = "/case-management";
    private static final String CASE_FOLDER_FOLDER = CASE_MANAGEMENT + "/case-folder-root";
    private static final String CASE = CASE_MANAGEMENT + "/case-root";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testRoots() throws Exception {

        DocumentModel root = session.getRootDocument();

        DocumentModelList rootChildren = session.getChildren(root.getRef());
        assertEquals(1, rootChildren.size());

        DocumentModel domain = rootChildren.get(0);
        assertEquals(CASE_MANAGEMENT, domain.getPathAsString());

        DocumentModelList domainChildren = session.getChildren(domain.getRef());
        assertEquals(2, domainChildren.size());

        DocumentModel mailRoot = domainChildren.get(0);
        assertEquals(CASE, mailRoot.getPathAsString());

        DocumentModel mailboxRoot = domainChildren.get(1);
        assertEquals(CASE_FOLDER_FOLDER, mailboxRoot.getPathAsString());
    }
}