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

import java.util.Collections;
import java.util.List;

import org.nuxeo.cm.cases.HasRecipients;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.MailEnvelope;
import org.nuxeo.cm.cases.MailEnvelopeImpl;
import org.nuxeo.cm.cases.MailEnvelopeItem;
import org.nuxeo.cm.cases.MailEnvelopeItemImpl;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;


/**
 * @author arussel
 *
 */
public class TestMailEnvelope extends CorrespondenceRepositoryTestCase {
    protected MailEnvelope envelope;

    protected MailEnvelopeItem item1;

    protected MailEnvelopeItem item2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = createDocument(CaseConstants.CASE_TYPE, "env");
        HasRecipients adapter = document.getAdapter(HasRecipients.class);
        envelope = new MailEnvelopeImpl(document,adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i1");
        item1 = new MailEnvelopeItemImpl(document,adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i2");
        item2 = new MailEnvelopeItemImpl(document,adapter);
    }

    public void testGetDocument() {
        DocumentModel doc = envelope.getDocument();
        assertNotNull(doc);
        assertEquals(doc.getType(), CaseConstants.CASE_TYPE);
    }

    public void testItemsMethods() throws ClientException {
        String envId = envelope.getDocument().getId();
        envelope.addMailEnvelopeItem(item1, session);
        envelope.save(session);
        closeSession();
        openSession();
        List<MailEnvelopeItem> items = envelope.getMailEnvelopeItems(session);
        assertNotNull(items);
        assertEquals(1, items.size());
        envelope.addMailEnvelopeItem(item2, session);
        envelope.save(session);
        closeSession();
        openSession();
        assertEquals(envelope.getMailEnvelopeItems(session).size(), 2);
        assertEquals(envelope.getFirstItem(session), item1);
        envelope.moveUpEmailsInEnvelope(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item2);
        envelope.moveDownEmailsInEnvelope(Collections.singletonList(item1), session);
        assertEquals(envelope.getFirstItem(session), item2);
        envelope.moveDownEmailsInEnvelope(Collections.singletonList(item2), session);
        assertEquals(envelope.getFirstItem(session), item1);
        envelope.moveDownEmailsInEnvelope(Collections.singletonList(item1), session);
        closeSession();
        openSession();
        DocumentModel document =session.getDocument(new IdRef(envId));
        HasRecipients adapter = document.getAdapter(HasRecipients.class);
        envelope = new MailEnvelopeImpl(document, adapter);
        openSession();
        assertEquals(envelope.getMailEnvelopeItems(session).size(), 2);
        assertEquals(envelope.getFirstItem(session), item1);
    }
}
