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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * @author arussel
 */
public class TestCaseItem extends CaseManagementRepositoryTestCase {

    protected CaseItem item;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = session.createDocumentModel(CaseConstants.CASE_ITEM_DOCUMENT_TYPE);
        document.setPathInfo("/", "foo");
        document = session.createDocument(document);
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        item = new CaseItemImpl(document, adapter);
    }

    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    /**
     * Test method for {@link org.nuxeo.cm.cases.CaseItemImpl#getDocument()}.
     */
    @Test
    public void testGetDocument() {
        DocumentModel model = item.getDocument();
        assertNotNull(model);
        assertTrue(model.hasFacet(CaseConstants.CASE_GROUPABLE_FACET));
    }

    @Test
    public void testProperties() throws ClientException {
        String id = item.getDocument().getId();
        Calendar date = GregorianCalendar.getInstance();
        int millis = date.get(Calendar.MILLISECOND);
        String title = "my title";
        String cdf = "4";

        item.setTitle(title);
        item.setConfidentiality(cdf);
        item.setSendingDate(date);
        item.save(session);
        session.save();
        closeSession();

        openSession();
        DocumentModel model = session.getDocument(new IdRef(id));
        HasParticipants adapter = model.getAdapter(HasParticipants.class);
        item = new CaseItemImpl(model, adapter);
        assertEquals(item.getTitle(), title);
        assertEquals(item.getConfidentiality(), cdf);
        Calendar sendingDate = item.getSendingDate();
        // SQL Server < 2008 does not have full millisecond resolution
        int sendingMillis = sendingDate.get(Calendar.MILLISECOND);
        if (sendingMillis != millis) {
            sendingDate.set(Calendar.MILLISECOND, millis);
        }
        assertEquals(sendingDate, date);
    }

    @Test
    public void testCreateCase() throws ClientException {
        Case envelope = item.createMailCase(session,
                session.getRootDocument().getPathAsString(), null);
        assertNotNull(envelope);
        String id = envelope.getDocument().getId();
        session.save();
        closeSession();

        openSession();
        DocumentModel model = session.getDocument(new IdRef(id));
        HasParticipants adapter = model.getAdapter(HasParticipants.class);
        envelope = new CaseImpl(model, adapter);
        assertNotNull(envelope);
        assertEquals(1, envelope.getCaseItems(session).size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testLock() throws ClientException {

        DocumentModel doc = item.getDocument();

        // Set Read rights
        ACP acp = doc.getACP();
        ACL localACL = acp.getOrCreateACL(ACL.LOCAL_ACL);
        localACL.add(new ACE("user1", SecurityConstants.READ, true));
        acp.addACL(localACL);
        doc.setACP(acp, true);
        session.saveDocument(doc);
        session.save();

        // Admin lock the document
        doc = session.getDocument(doc.getRef());
        LockableAdapter lockableDocument = doc.getAdapter(LockableAdapter.class);
        lockableDocument.lockDocument(session);
        session.saveDocument(doc);
        session.save();

        // It is not locked for the locker
        assertFalse(lockableDocument.isLocked(session));

        // User1 fetch the document and check that it is locked
        closeSession();
        session = openSessionAs("user1");
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        assertTrue(lockableDocument.isLocked(session));

        // Admin unlock
        closeSession();
        session = openSessionAs(SecurityConstants.ADMINISTRATOR);
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        lockableDocument.unlockDocument(session);
        session.saveDocument(doc);
        session.save();

        // User1 fetch the document and check that it is unlocked
        closeSession();
        session = openSessionAs("user1");
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        assertFalse(lockableDocument.isLocked(session));
    }

}
