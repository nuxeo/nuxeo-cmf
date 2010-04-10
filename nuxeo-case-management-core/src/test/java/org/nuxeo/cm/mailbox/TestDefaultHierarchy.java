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

import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author Anahide Tchertchian
 *
 */
public class TestDefaultHierarchy extends CorrespondenceRepositoryTestCase {

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
        assertEquals("/correspondence", domain.getPathAsString());

        DocumentModelList domainChildren = session.getChildren(domain.getRef());
        assertEquals(2, domainChildren.size());

        DocumentModel mailRoot = domainChildren.get(0);
        assertEquals("/correspondence/mail", mailRoot.getPathAsString());

        DocumentModel mailboxRoot = domainChildren.get(1);
        assertEquals("/correspondence/mailboxes", mailboxRoot.getPathAsString());

        // TODO: maybe test rights

    }
}
