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
package org.nuxeo.cm.cases;

import java.util.Collections;
import java.util.List;

import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseImpl;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.CaseItemImpl;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;


/**
 * @author arussel
 *
 */
public class TestCase extends CaseManagementRepositoryTestCase {
    protected Case envelope;

    protected CaseItem item1;

    protected CaseItem item2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = createDocument(CaseConstants.CASE_TYPE, "env");
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        envelope = new CaseImpl(document,adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i1");
        item1 = new CaseItemImpl(document,adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i2");
        item2 = new CaseItemImpl(document,adapter);
    }

    public void testGetDocument() {
        DocumentModel doc = envelope.getDocument();
        assertNotNull(doc);
        assertEquals(doc.getType(), CaseConstants.CASE_TYPE);
    }

    public void testItemsMethods() throws ClientException {
        String envId = envelope.getDocument().getId();
        envelope.addCaseItem(item1, session);
        envelope.save(session);
        closeSession();
        openSession();
        List<CaseItem> items = envelope.getCaseItems(session);
        assertNotNull(items);
        assertEquals(1, items.size());
        envelope.addCaseItem(item2, session);
        envelope.save(session);
        closeSession();
        openSession();
        assertEquals(envelope.getCaseItems(session).size(), 2);
        assertEquals(envelope.getFirstItem(session), item1);
        envelope.moveUpEmailsInCase(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item2);
        envelope.moveDownEmailsInCase(Collections.singletonList(item1), session);
        assertEquals(envelope.getFirstItem(session), item2);
        envelope.moveDownEmailsInCase(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item1);
        envelope.moveDownEmailsInCase(Collections.singletonList(item1), session);
        closeSession();
        openSession();
        DocumentModel document =session.getDocument(new IdRef(envId));
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        envelope = new CaseImpl(document, adapter);
        openSession();
        assertEquals(envelope.getCaseItems(session).size(), 2);
        assertEquals(envelope.getFirstItem(session), item1);
    }
}
