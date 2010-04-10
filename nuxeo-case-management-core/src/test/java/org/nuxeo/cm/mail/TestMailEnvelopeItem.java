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
package org.nuxeo.cm.mail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.nuxeo.cm.mail.HasRecipients;
import org.nuxeo.cm.mail.LockableAdapter;
import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeImpl;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mail.MailEnvelopeItemImpl;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;


/**
 * @author arussel
 *
 */
public class TestMailEnvelopeItem extends CorrespondenceRepositoryTestCase {
    protected MailEnvelopeItem item;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = session.createDocumentModel(MailConstants.MAIL_DOCUMENT_TYPE);
        document.setPathInfo("/", "foo");
        document = session.createDocument(document);
        HasRecipients adapter = document.getAdapter(HasRecipients.class);
        item = new MailEnvelopeItemImpl(document, adapter);
    }

    /**
     * Test method for
     * {@link org.nuxeo.cm.mail.MailEnvelopeItemImpl#getDocument()}.
     */
    public void testGetDocument() {
        DocumentModel model = item.getDocument();
        assertNotNull(model);
        assertTrue(model.hasFacet(MailConstants.MAIL_FACET));
    }

    public void testProperties() throws ClientException {
        String id = item.getDocument().getId();
        Calendar date = GregorianCalendar.getInstance();
        String title = "my title";
        String cdf = "4";
        item.setTitle(title);
        item.setConfidentiality(cdf);
        item.setSendingDate(date);
        item.save(session);
        closeSession();
        openSession();
        DocumentModel model = session.getDocument(new IdRef(id));
        HasRecipients adapter = model.getAdapter(HasRecipients.class);
        item = new MailEnvelopeItemImpl(model, adapter);
        assertEquals(item.getTitle(), title);
        assertEquals(item.getConfidentiality(), cdf);
        assertEquals(item.getSendingDate(), date);
    }

    public void testCreateMailEnvelope() throws ClientException {
        MailEnvelope envelope = item.createMailEnvelope(session,
                session.getRootDocument().getPathAsString(), null);
        assertNotNull(envelope);
        String id = envelope.getDocument().getId();
        closeSession();
        openSession();
        DocumentModel model = session.getDocument(new IdRef(id));
        HasRecipients adapter = model.getAdapter(HasRecipients.class);
        envelope = new MailEnvelopeImpl(model, adapter);
        assertNotNull(envelope);
        assertEquals(envelope.getMailEnvelopeItems(session).size(), 1);
    }

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
        session = openSessionAs("user1");
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        assertTrue(lockableDocument.isLocked(session));

        // Admin unlock
        session = openSessionAs(SecurityConstants.ADMINISTRATOR);
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        lockableDocument.unlockDocument(session);
        session.saveDocument(doc);
        session.save();

        // User1 fetch the document and check that it is unlocked
        session = openSessionAs("user1");
        doc = session.getDocument(doc.getRef());
        lockableDocument = doc.getAdapter(LockableAdapter.class);
        assertFalse(lockableDocument.isLocked(session));

    }
}
