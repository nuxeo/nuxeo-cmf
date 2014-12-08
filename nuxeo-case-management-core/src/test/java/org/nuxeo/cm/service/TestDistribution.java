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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.caselink.CaseLinkType;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.IdRef;

/**
 * Test the distribution process.
 *
 * @author Nicolas Ulrich
 */
public class TestDistribution extends CaseManagementRepositoryTestCase {

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

    public Mailbox getPersonalMailbox(String name) throws Exception {
        return correspMailboxService.createPersonalMailboxes(session, name).get(0);
    }

    @Test
    public void testSendCase() throws Exception {

        // Initialize mailboxes
        Mailbox initialSender = getPersonalMailbox(user1);
        Mailbox initialReceiverMailbox = getPersonalMailbox(user2);
        Mailbox receiverMailbox1 = getPersonalMailbox(user3);

        // Create an envelope
        Case envelope = getMailEnvelope();
        CaseItem envelopeItem = getMailEnvelopeItem();
        envelope.addCaseItem(envelopeItem, session);
        createDraftPost(initialSender, envelope);

        assertNotNull(distributionService.getDraftCaseLink(session, initialSender, envelope.getDocument().getId()));

        // Create initial recipients list
        Map<String, List<String>> initialRecipients = new HashMap<String, List<String>>();
        initialRecipients.put(CaseLinkType.FOR_ACTION.toString(),
                Collections.singletonList(initialReceiverMailbox.getId()));

        // Create a post request
        CaseLink postRequest = new CaseLinkRequestImpl(initialSender.getId(), Calendar.getInstance(), "Check this out",
                "it is a bit boring", envelope, initialRecipients, null);

        // Check mailboxes of initial recipient and sender
        assertEquals(1, distributionService.getDraftCaseLinks(session, initialSender, 0, 0).size());
        assertEquals(0, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        assertTrue(envelope.isDraft());

        // Initial sending
        distributionService.sendCase(session, postRequest, true);

        assertFalse(envelope.isDraft());
        // Check mailbox of recipient and sender
        assertEquals(1, distributionService.getSentCaseLinks(session, initialSender, 0, 0).size());
        assertEquals(1, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        // Retrieve the post in the initial receiver mailbox
        CaseLink postInMailbox = distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).get(0);

        // Retrieve the envelope from this post
        Case envelopeFromPost = postInMailbox.getCase(session);

        // Check the envelope
        assertEquals(envelopeFromPost.getDocument().getId(), envelope.getDocument().getId());

        // Check initial recipients in the envelope
        assertEquals(1,
                envelopeFromPost.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).size());
        assertTrue(envelopeFromPost.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));

        // Prepare recipients list for a transfer
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put(CaseLinkType.FOR_INFORMATION.toString(), Collections.singletonList(receiverMailbox1.getId()));

        // Create a post request
        postRequest = new CaseLinkRequestImpl(initialReceiverMailbox.getId(), Calendar.getInstance(),
                "Check this out (Transfert)", "it is a bit boring too", envelopeFromPost, recipients, null);

        // Check mailbox of recipients
        assertEquals(0, distributionService.getReceivedCaseLinks(session, receiverMailbox1, 0, 0).size());
        assertEquals(1, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        // Transfer
        distributionService.sendCase(session, postRequest, false);

        // Check mailbox of recipients
        assertEquals(1, distributionService.getReceivedCaseLinks(session, receiverMailbox1, 0, 0).size());
        assertEquals(1, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());
        assertEquals(1, distributionService.getSentCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        // Retrieve the post in the recipient1 mailbox
        postInMailbox = distributionService.getReceivedCaseLinks(session, receiverMailbox1, 0, 0).get(0);

        // Retrieve the envelope from the post
        envelopeFromPost = postInMailbox.getCase(session);

        // Check the envelope
        assertEquals(envelopeFromPost.getDocument().getId(), envelope.getDocument().getId());

        assertEquals(2, envelopeFromPost.getAllParticipants().size());
        assertEquals(1,
                envelopeFromPost.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).size());

        assertTrue(envelopeFromPost.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));
        assertTrue(envelopeFromPost.getAllParticipants().get(CaseLinkType.FOR_INFORMATION.toString()).contains(
                receiverMailbox1.getId()));

        // Check Envelope item
        CaseItem item = envelopeFromPost.getFirstItem(session);

        assertEquals(2, item.getAllParticipants().size());
        assertEquals(1, item.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).size());

        assertTrue(item.getInitialInternalParticipants().get(CaseLinkType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));
        assertTrue(item.getAllParticipants().get(CaseLinkType.FOR_INFORMATION.toString()).contains(
                receiverMailbox1.getId()));
    }

    @Test
    public void testSendCaseAndRemove() throws Exception {

        // Initialize mailboxes
        Mailbox initialSender = getPersonalMailbox(user1);
        Mailbox initialReceiverMailbox = getPersonalMailbox(user2);

        // Create an envelope
        Case envelope = getMailEnvelope();
        CaseItem envelopeItem = getMailEnvelopeItem();
        envelope.addCaseItem(envelopeItem, session);
        createDraftPost(initialSender, envelope);

        assertNotNull(distributionService.getDraftCaseLink(session, initialSender, envelope.getDocument().getId()));

        // Create initial recipients list
        Map<String, List<String>> initialRecipients = new HashMap<String, List<String>>();
        initialRecipients.put(CaseLinkType.FOR_ACTION.toString(),
                Collections.singletonList(initialReceiverMailbox.getId()));

        // Create a post request
        CaseLink postRequest = new CaseLinkRequestImpl(initialSender.getId(), Calendar.getInstance(), "Check this out",
                "it is a bit boring", envelope, initialRecipients, null);

        // Check mailboxes of initial recipient and sender
        assertEquals(1, distributionService.getDraftCaseLinks(session, initialSender, 0, 0).size());
        assertEquals(0, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        assertTrue(envelope.isDraft());

        // Initial sending
        distributionService.sendCase(session, postRequest, true);

        assertFalse(envelope.isDraft());
        // Check mailbox of recipient and sender
        closeSession();
        session = openSessionAs(userManager.getPrincipal(user2));
        assertEquals(1, distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).size());

        // Retrieve the post in the initial receiver mailbox
        CaseLink postInMailbox = distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0).get(0);

        // Retrieve the envelope from this post
        Case kase = postInMailbox.getCase(session);
        String caseId = kase.getDocument().getId();
        // Check the envelope
        assertEquals(kase.getDocument().getId(), envelope.getDocument().getId());
        // remove the case link
        closeSession();
        openSession();
        distributionService.removeCaseLink(postInMailbox, session);
        closeSession();
        session = openSessionAs(userManager.getPrincipal(user2));
        // check the case link is not here
        List<CaseLink> links = distributionService.getReceivedCaseLinks(session, initialReceiverMailbox, 0, 0);
        assertTrue(links.isEmpty());
        // check we can't access the case anymore
        boolean error = false;
        try {
            session.getDocument(new IdRef(caseId));
            fail();
        } catch (DocumentSecurityException e) {
            error = true;
        }
        assertTrue(error);
    }
}
