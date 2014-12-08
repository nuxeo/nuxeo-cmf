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

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

public class TestMailboxManagementService extends CaseManagementRepositoryTestCase {

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
    public void testDefaultPersonalMailboxCreation() throws Exception {

        correspMailboxService.createPersonalMailboxes(session, "toto");
        List<Mailbox> mailboxes = correspMailboxService.getUserMailboxes(session, "toto");
        assertTrue(mailboxes.isEmpty());

        correspMailboxService.createPersonalMailboxes(session, user);
        mailboxes = correspMailboxService.getUserMailboxes(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());

        Mailbox mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("User Lambda (mycomp)", mb.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    @Test
    public void testPersonalMailboxCreatorContribution() throws Exception {
        // override creation behaviour
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-personalmailbox-creator-corresp-contrib.xml");

        List<Mailbox> mailboxes = correspMailboxService.createPersonalMailboxes(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());

        Mailbox mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("user's personal mailbox", mb.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    @Test
    public void testGetUserPersonalMailboxId() throws Exception {
        String totoMbId = correspMailboxService.getUserPersonalMailboxId("toto");
        assertNull(totoMbId);

        String userMbId = correspMailboxService.getUserPersonalMailboxId(user);
        assertNotNull(userMbId);
        assertEquals("user-user", userMbId);
    }

    @Test
    public void testGetMailbox() throws Exception {
        createMailbox();

        // log as given user and check he still got access
        closeSession();
        session = openSessionAs(user);
        Mailbox mb = correspMailboxService.getMailbox(session, "test");
        assertEquals("Test", mb.getTitle());
    }

    @Test
    public void testHasMailbox() throws Exception {
        createMailbox();
        assertTrue(correspMailboxService.hasMailbox(session, "test"));
        assertFalse(correspMailboxService.hasMailbox(session, "foo"));
    }

    @Test
    public void testGetMailboxes() throws Exception {
        correspMailboxService.createPersonalMailboxes(session, user);

        // Create an other mailbox
        createMailbox();

        List<Mailbox> mailboxes = correspMailboxService.getUserMailboxes(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(2, mailboxes.size());

        // query result order is not fixed
        if ("test".equals(mailboxes.get(0).getId())) {
            Collections.reverse(mailboxes);
        }

        Mailbox mbPerso = mailboxes.get(0);
        assertEquals("user-user", mbPerso.getId());
        assertEquals("User Lambda (mycomp)", mbPerso.getTitle());
        assertEquals(MailboxConstants.type.personal.name(), mbPerso.getType());
        assertEquals(user, mbPerso.getOwner());
        assertEquals(MailboxConstants.MAILBOX_CASE_CREATION_PROFILE, mbPerso.getProfiles().get(0).toString());

        Mailbox mbGeneric = mailboxes.get(1);
        assertEquals("test", mbGeneric.getId());
        assertEquals("Test", mbGeneric.getTitle());
        assertEquals(MailboxConstants.type.generic.name(), mbGeneric.getType());

        // log as given user and check he still got access
        closeSession();
        session = openSessionAs(user);
        mailboxes = correspMailboxService.getUserMailboxes(session, user);
        assertEquals(2, mailboxes.size());
    }

    @Test
    public void testSearchMailboxes() throws Exception {
        // create personal mailboxes for users, calling getMailboxes on each
        correspMailboxService.getUserMailboxes(session, user);
        correspMailboxService.getUserMailboxes(session, user1);
        correspMailboxService.getUserMailboxes(session, user2);
        correspMailboxService.getUserMailboxes(session, user3);

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
        mailboxModel.setPathInfo("/case-management/mailbox-root", newMailbox.getId());
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

    @Test
    public void testSendCase() throws Exception {
        Mailbox senderMailbox = getPersonalMailbox(user1);
        assertNotNull(senderMailbox);
        Mailbox receiver1Mailbox = getPersonalMailbox(user2);
        String receiver1MailboxId = receiver1Mailbox.getDocument().getId();
        assertNotNull(receiver1Mailbox);
        Mailbox receiver2Mailbox = getPersonalMailbox(user3);
        assertNotNull(receiver2Mailbox);
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put("FOR_ACTION", Collections.singletonList(receiver1Mailbox.getId()));
        recipients.put("FOR_INFORMATION", Collections.singletonList(receiver2Mailbox.getId()));
        Case envelope = getMailEnvelope();
        createDraftPost(senderMailbox, envelope);

        assertTrue(distributionService.getDraftCaseLink(session, senderMailbox, envelope.getDocument().getId()).isDraft());

        CaseLink postRequest = new CaseLinkRequestImpl(senderMailbox.getId(), Calendar.getInstance(), "Check this out",
                "it is a bit boring", envelope, recipients, null);

        CaseLink post = distributionService.sendCase(session, postRequest, true);
        assertNotNull(post);
        assertFalse(post.isDraft());
        assertEquals("user1", post.getSender());
        DocumentModelList docList = session.query("select * from Document where ecm:mixinType = 'CaseLink' AND ecm:parentId = '"
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
        mailboxModel.setPathInfo("/case-management/mailbox-root", newMailbox.getId());
        session.createDocument(mailboxModel);
        // save to make it available to other sessions
        session.save();
    }

}
