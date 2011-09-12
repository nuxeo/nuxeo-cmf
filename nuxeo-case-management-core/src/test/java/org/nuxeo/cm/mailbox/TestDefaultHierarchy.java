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

package org.nuxeo.cm.mailbox;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

public class TestDefaultHierarchy extends CaseManagementRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    @Override
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    public void testRoots() throws Exception {
        DocumentModel root = session.getRootDocument();

        DocumentModelList rootChildren = session.getChildren(root.getRef());
        assertEquals(1, rootChildren.size());

        DocumentModel domain = rootChildren.get(0);
        assertEquals(CaseConstants.CASE_DOMAIN_PATH, domain.getPathAsString());

        DocumentModelList domainChildren = session.getChildren(domain.getRef());
        assertEquals(2, domainChildren.size());

        DocumentModel mailRoot = domainChildren.get(0);
        DocumentModel mailboxRoot = domainChildren.get(1);

        // don't assume that the clidren's order will be the same on all
        // databases
        if (CaseConstants.CASE_ROOT_TYPE.equals(mailRoot.getType())) {
            assertEquals(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    mailRoot.getPathAsString());
            assertEquals(MailboxConstants.MAILBOX_ROOT_DOCUMENT_PATH,
                    mailboxRoot.getPathAsString());
        } else {
            assertEquals(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    mailboxRoot.getPathAsString());
            assertEquals(MailboxConstants.MAILBOX_ROOT_DOCUMENT_PATH,
                    mailRoot.getPathAsString());
        }

    }

}
