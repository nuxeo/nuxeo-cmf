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

import org.nuxeo.cm.cases.MailEnvelope;
import org.nuxeo.cm.cases.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostRequestImpl;
import org.nuxeo.cm.post.CorrespondencePostType;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;


/**
 * Test the distribution process
 *
 * @author Nicolas Ulrich
 *
 */
public class TestCorrespondenceDistribution extends
        CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public Mailbox getPersonalMailbox(String name) throws Exception {
        return correspService.createPersonalMailbox(session, name).get(0);
    }

    public void testSendEnvelope() throws Exception {

        // Initialize mailboxes
        Mailbox initialSender = getPersonalMailbox(user1);
        Mailbox initialReceiverMailbox = getPersonalMailbox(user2);
        Mailbox receiverMailbox1 = getPersonalMailbox(user3);

        // Create an envelope
        MailEnvelope envelope = getMailEnvelope();
        MailEnvelopeItem envelopeItem = getMailEnvelopeItem();
        envelope.addMailEnvelopeItem(envelopeItem, session);
        createDraftPost(initialSender, envelope);

        assertNotNull(correspService.getDraftPost(session, initialSender,
                envelope.getDocument().getId()));

        // Create initial recipients list
        Map<String, List<String>> initialRecipients = new HashMap<String, List<String>>();
        initialRecipients.put(CorrespondencePostType.FOR_ACTION.toString(),
                Collections.singletonList(initialReceiverMailbox.getId()));

        // Create a post request
        CorrespondencePost postRequest = new CorrespondencePostRequestImpl(
                initialSender.getId(), Calendar.getInstance(),
                "Check this out", "it is a bit boring", envelope,
                initialRecipients, null);

        // Check mailboxes of initial recipient and sender
        assertEquals(1, correspService.getDraftPosts(session, initialSender, 0,
                0).size());
        assertEquals(0, correspService.getReceivedPosts(session,
                initialReceiverMailbox, 0, 0).size());

        assertTrue(envelope.isDraft());

        // Initial sending
        correspService.sendEnvelope(session, postRequest, true);

        assertFalse(envelope.isDraft());
        // Check mailbox of recipient and sender
        assertEquals(1, correspService.getSentPosts(session, initialSender, 0,
                0).size());
        assertEquals(1, correspService.getReceivedPosts(session,
                initialReceiverMailbox, 0, 0).size());

        // Retrieve the post in the initial receiver mailbox
        CorrespondencePost postInMailbox = correspService.getReceivedPosts(
                session, initialReceiverMailbox, 0, 0).get(0);

        // Retrieve the envelope from this post
        MailEnvelope envelopeFromPost = postInMailbox.getMailEnvelope(session);

        // Check the envelope
        assertEquals(envelopeFromPost.getDocument().getId(),
                envelope.getDocument().getId());

        // Check initial recipients in the envelope
        assertEquals(1, envelopeFromPost.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).size());
        assertTrue(envelopeFromPost.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));

        // Prepare recipients list for a transfer
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put(CorrespondencePostType.FOR_INFORMATION.toString(),
                Collections.singletonList(receiverMailbox1.getId()));

        // Create a post request
        postRequest = new CorrespondencePostRequestImpl(
                initialReceiverMailbox.getId(), Calendar.getInstance(),
                "Check this out (Transfert)", "it is a bit boring too",
                envelopeFromPost, recipients, null);

        // Check mailbox of recipients
        assertEquals(0, correspService.getReceivedPosts(session,
                receiverMailbox1, 0, 0).size());
        assertEquals(1, correspService.getReceivedPosts(session,
                initialReceiverMailbox, 0, 0).size());

        // Transfer
        correspService.sendEnvelope(session, postRequest, false);

        // Check mailbox of recipients
        assertEquals(1, correspService.getReceivedPosts(session,
                receiverMailbox1, 0, 0).size());
        assertEquals(1, correspService.getReceivedPosts(session,
                initialReceiverMailbox, 0, 0).size());
        assertEquals(1, correspService.getSentPosts(session,
                initialReceiverMailbox, 0, 0).size());

        // Retrieve the post in the recipient1 mailbox
        postInMailbox = correspService.getReceivedPosts(session,
                receiverMailbox1, 0, 0).get(0);

        // Retrieve the envelope from the post
        envelopeFromPost = postInMailbox.getMailEnvelope(session);

        // Check the envelope
        assertEquals(envelopeFromPost.getDocument().getId(),
                envelope.getDocument().getId());

        assertEquals(2, envelopeFromPost.getAllRecipients().size());
        assertEquals(1, envelopeFromPost.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).size());

        assertTrue(envelopeFromPost.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));
        assertTrue(envelopeFromPost.getAllRecipients().get(CorrespondencePostType.FOR_INFORMATION.toString()).contains(
                receiverMailbox1.getId()));

        //Check Envelope item
        MailEnvelopeItem item = envelopeFromPost.getFirstItem(session);

        assertEquals(2, item.getAllRecipients().size());
        assertEquals(1, item.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).size());

        assertTrue(item.getInitialInternalRecipients().get(CorrespondencePostType.FOR_ACTION.toString()).contains(
                initialReceiverMailbox.getId()));
        assertTrue(item.getAllRecipients().get(CorrespondencePostType.FOR_INFORMATION.toString()).contains(
                receiverMailbox1.getId()));


    }
}