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

package org.nuxeo.cm.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.cases.MailEnvelope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostRequestImpl;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author Anahide Tchertchian
 *
 */
public class TestCorrespondenceService extends CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testDefaultPersonalMailboxCreation() throws Exception {

        correspService.createPersonalMailbox(session, "toto");
        List<Mailbox> mailboxes = correspService.getUserMailboxes(session,
                "toto");
        assertTrue(mailboxes.isEmpty());

        correspService.createPersonalMailbox(session, user);
        mailboxes = correspService.getUserMailboxes(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());
        Mailbox mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("User Lambda", mb.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    public void testPersonalMailboxCreatorContribution() throws Exception {
        // override creation behaviour
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-personalmailbox-creator-corresp-contrib.xml");

        List<Mailbox> mailboxes = correspService.createPersonalMailbox(session,
                user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());
        Mailbox mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("user's personal mailbox", mb.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    public void testGetUserPersonalMailboxId() throws Exception {
        String totoMbId = correspService.getUserPersonalMailboxId("toto");
        assertNull(totoMbId);
        String userMbId = correspService.getUserPersonalMailboxId(user);
        assertNotNull(userMbId);
        assertEquals("user-user", userMbId);
    }

    public void testGetMailbox() throws Exception {

        createMailbox();

        // log as given user and check he still got access
        openSessionAs(user);
        Mailbox mb = correspService.getMailbox(session, "test");
        assertEquals(mb.getTitle(), "Test");

    }

    public void testHasMailbox() throws Exception {
        createMailbox();
        assertTrue(correspService.hasMailbox("test"));
        assertFalse(correspService.hasMailbox("foo"));
    }

    public void testGetMailboxes() throws Exception {

        correspService.createPersonalMailbox(session, user);

        // Create an other mailbox
        createMailbox();

        List<Mailbox> mailboxes = correspService.getUserMailboxes(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(2, mailboxes.size());

        Mailbox mbPerso = mailboxes.get(0);
        assertEquals("user-user", mbPerso.getId());
        assertEquals("User Lambda", mbPerso.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mbPerso.getType());
        assertEquals(user, mbPerso.getOwner());

        Mailbox mbGeneric = mailboxes.get(1);
        assertEquals("test", mbGeneric.getId());
        assertEquals("Test", mbGeneric.getTitle());
        assertEquals(MailboxConstants.type.generic.name(), mbGeneric.getType());

        // log as given user and check he still got access
        openSessionAs(user);
        mailboxes = correspService.getUserMailboxes(session, user);
        assertEquals(2, mailboxes.size());

    }

    public void testSearchMailboxes() throws Exception {
        // create personal mailboxes for users, calling getMailboxes on each
        correspService.getUserMailboxes(session, user);
        correspService.getUserMailboxes(session, user1);
        correspService.getUserMailboxes(session, user2);
        correspService.getUserMailboxes(session, user3);

        // create a generic mailbox too
        DocumentModel mailboxModel = session.createDocumentModel(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        Mailbox newMailbox = mailboxModel.getAdapter(Mailbox.class);
        // set users
        newMailbox.setId("test");
        newMailbox.setTitle("Test");
        newMailbox.setType(MailboxConstants.type.generic.name());

        // create doc
        mailboxModel = newMailbox.getDocument();
        // XXX: use default domain path here
        mailboxModel.setPathInfo("/case-management/case-folder-root",
                newMailbox.getId());
        session.createDocument(mailboxModel);
        // save to make it available to other sessions
        session.save();

        // TODO
        // List<Mailbox> mailboxes = correspService.searchMailboxes("user",
        // null);
        // assertFalse(mailboxes.isEmpty());
        //
        // mailboxes = correspService.searchMailboxes("test",
        // MailboxConstants.type.generic.name());
        // assertFalse(mailboxes.isEmpty());
    }

    public Mailbox getPersonalMailbox(String name) throws Exception {
        return correspService.createPersonalMailbox(session, name).get(0);
    }

    public void testSendEnvelope() throws Exception {
        Mailbox senderMailbox = getPersonalMailbox(user1);
        assertNotNull(senderMailbox);
        Mailbox receiver1Mailbox = getPersonalMailbox(user2);
        String receiver1MailboxId = receiver1Mailbox.getDocument().getId();
        assertNotNull(receiver1Mailbox);
        Mailbox receiver2Mailbox = getPersonalMailbox(user3);
        assertNotNull(receiver2Mailbox);
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put("FOR_ACTION",
                Collections.singletonList(receiver1Mailbox.getId()));
        recipients.put("FOR_INFORMATION",
                Collections.singletonList(receiver2Mailbox.getId()));
        MailEnvelope envelope = getMailEnvelope();
        createDraftPost(senderMailbox, envelope);

        assertTrue(correspService.getDraftPost(session, senderMailbox,
                envelope.getDocument().getId()).isDraft());

        CorrespondencePost postRequest = new CorrespondencePostRequestImpl(
                senderMailbox.getId(), Calendar.getInstance(),
                "Check this out", "it is a bit boring", envelope, recipients,
                null);

        CorrespondencePost post = correspService.sendEnvelope(session,
                postRequest, true);
        assertNotNull(post);
        assertFalse(post.isDraft());
        assertEquals("user1", post.getSender());
        DocumentModelList docList = session.query("select * from Document where ecm:parentId = '"
                + receiver1MailboxId + "'");
        assertEquals(1, docList.size());
        DocumentModel message = docList.get(0);
        assertEquals("CaseLink", message.getType());
    }

    protected void createMailbox() throws ClientException {
        // create a mailbox with given user, and check it's retrieved correctly
        DocumentModel mailboxModel = session.createDocumentModel(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        Mailbox newMailbox = mailboxModel.getAdapter(Mailbox.class);
        // set users
        newMailbox.setId("test");
        newMailbox.setTitle("Test");
        newMailbox.setType(MailboxConstants.type.generic.name());
        newMailbox.setUsers(Arrays.asList(new String[] { user }));

        // create doc
        mailboxModel = newMailbox.getDocument();
        // XXX: use default domain path here
        mailboxModel.setPathInfo("/case-management/case-folder-root",
                newMailbox.getId());
        session.createDocument(mailboxModel);
        // save to make it available to other sessions
        session.save();
    }

}
