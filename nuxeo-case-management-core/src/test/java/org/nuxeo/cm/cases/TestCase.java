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

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author arussel
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
        envelope = new CaseImpl(document, adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i1");
        item1 = new CaseItemImpl(document, adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i2");
        item2 = new CaseItemImpl(document, adapter);
    }

    public void testGetDocument() {
        DocumentModel doc = envelope.getDocument();
        assertNotNull(doc);
        assertEquals(CaseConstants.CASE_TYPE, doc.getType());
    }

    public void testItemsMethods() throws ClientException {
        String envId = envelope.getDocument().getId();
        envelope.addCaseItem(item1, session);
        envelope.save(session);
        session.save();
        closeSession();
        openSession();
        List<CaseItem> items = envelope.getCaseItems(session);
        assertNotNull(items);
        assertEquals(1, items.size());

        envelope.addCaseItem(item2, session);
        envelope.save(session);
        closeSession();
        openSession();
        assertEquals(2, envelope.getCaseItems(session).size());
        assertEquals(envelope.getFirstItem(session), item1);

        envelope.moveUpEmailsInCase(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item2);

        envelope.moveDownEmailsInCase(Collections.singletonList(item1), session);
        assertEquals(envelope.getFirstItem(session), item2);

        envelope.moveDownEmailsInCase(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item1);

        envelope.moveDownEmailsInCase(Collections.singletonList(item1), session);
        session.save();
        closeSession();
        TransactionHelper.commitOrRollbackTransaction();
        openSession();
        DocumentModel document = session.getDocument(new IdRef(envId));
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        envelope = new CaseImpl(document, adapter);
        assertEquals(2, envelope.getCaseItems(session).size());
        assertEquals(envelope.getFirstItem(session), item2);
    }

}
