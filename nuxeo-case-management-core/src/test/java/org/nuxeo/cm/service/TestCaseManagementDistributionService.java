/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author arussel
 */
public class TestCaseManagementDistributionService extends
        CaseManagementRepositoryTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    @Test
    public void testAddCaseItemToCase() throws Exception {
        Mailbox senderMailbox = getPersonalMailbox(user1);
        assertNotNull(senderMailbox);
        DocumentModel emailDoc = getMailEnvelopeItem().getDocument();
        Case kase = distributionService.createCase(session, emailDoc);
        assertNotNull(kase);
        emailDoc = getMailEnvelopeItem().getDocument();
        CaseItem item = distributionService.addCaseItemToCase(session, kase,
                emailDoc);
        assertNotNull(item);
        assertEquals(kase.getDocuments().size(), 2);
    }
}
