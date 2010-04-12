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

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.post.CaseLink;
import org.nuxeo.cm.post.CaseLinkRequestImpl;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author Anahide Tchertchian
 *
 */
public class TestCaseManagementService extends CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testDefaultPersonalCaseFolderCreation() throws Exception {

        correspService.createPersonalCaseFolders(session, "toto");
        List<CaseFolder> mailboxes = correspService.getUserCaseFolders(session,
                "toto");
        assertTrue(mailboxes.isEmpty());

        correspService.createPersonalCaseFolders(session, user);
        mailboxes = correspService.getUserCaseFolders(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());
        CaseFolder mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("User Lambda", mb.getTitle());
        assertEquals(CaseFolderConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    public void testPersonalCaseFolderCreatorContribution() throws Exception {
        // override creation behaviour
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-personalmailbox-creator-corresp-contrib.xml");

        List<CaseFolder> mailboxes = correspService.createPersonalCaseFolders(session,
                user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(1, mailboxes.size());
        CaseFolder mb = mailboxes.get(0);
        assertEquals("user-user", mb.getId());
        assertEquals("user's personal mailbox", mb.getTitle());
        assertEquals(CaseFolderConstants.type.personal.name(), mb.getType());
        assertEquals(user, mb.getOwner());
    }

    public void testGetUserPersonalCaseFolderId() throws Exception {
        String totoMbId = correspService.getUserPersonalCaseFolderId("toto");
        assertNull(totoMbId);
        String userMbId = correspService.getUserPersonalCaseFolderId(user);
        assertNotNull(userMbId);
        assertEquals("user-user", userMbId);
    }

    public void testGetCaseFolder() throws Exception {

        createCaseFolder();

        // log as given user and check he still got access
        openSessionAs(user);
        CaseFolder mb = correspService.getCaseFolder(session, "test");
        assertEquals(mb.getTitle(), "Test");

    }

    public void testHasCaseFolder() throws Exception {
        createCaseFolder();
        assertTrue(correspService.hasCaseFolder("test"));
        assertFalse(correspService.hasCaseFolder("foo"));
    }

    public void testGetCaseFolders() throws Exception {

        correspService.createPersonalCaseFolders(session, user);

        // Create an other mailbox
        createCaseFolder();

        List<CaseFolder> mailboxes = correspService.getUserCaseFolders(session, user);
        assertFalse(mailboxes.isEmpty());
        assertEquals(2, mailboxes.size());

        CaseFolder mbPerso = mailboxes.get(0);
        assertEquals("user-user", mbPerso.getId());
        assertEquals("User Lambda", mbPerso.getTitle());
        assertEquals(CaseFolderConstants.type.personal.name(), mbPerso.getType());
        assertEquals(user, mbPerso.getOwner());

        CaseFolder mbGeneric = mailboxes.get(1);
        assertEquals("test", mbGeneric.getId());
        assertEquals("Test", mbGeneric.getTitle());
        assertEquals(CaseFolderConstants.type.generic.name(), mbGeneric.getType());

        // log as given user and check he still got access
        openSessionAs(user);
        mailboxes = correspService.getUserCaseFolders(session, user);
        assertEquals(2, mailboxes.size());

    }

    public void testSearchCaseFolders() throws Exception {
        // create personal mailboxes for users, calling getMailboxes on each
        correspService.getUserCaseFolders(session, user);
        correspService.getUserCaseFolders(session, user1);
        correspService.getUserCaseFolders(session, user2);
        correspService.getUserCaseFolders(session, user3);

        // create a generic mailbox too
        DocumentModel mailboxModel = session.createDocumentModel(CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
        CaseFolder newMailbox = mailboxModel.getAdapter(CaseFolder.class);
        // set users
        newMailbox.setId("test");
        newMailbox.setTitle("Test");
        newMailbox.setType(CaseFolderConstants.type.generic.name());

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

    public CaseFolder getPersonalCaseFolder(String name) throws Exception {
        return correspService.createPersonalCaseFolders(session, name).get(0);
    }

    public void testSendCase() throws Exception {
        CaseFolder senderMailbox = getPersonalCaseFolder(user1);
        assertNotNull(senderMailbox);
        CaseFolder receiver1Mailbox = getPersonalCaseFolder(user2);
        String receiver1MailboxId = receiver1Mailbox.getDocument().getId();
        assertNotNull(receiver1Mailbox);
        CaseFolder receiver2Mailbox = getPersonalCaseFolder(user3);
        assertNotNull(receiver2Mailbox);
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put("FOR_ACTION",
                Collections.singletonList(receiver1Mailbox.getId()));
        recipients.put("FOR_INFORMATION",
                Collections.singletonList(receiver2Mailbox.getId()));
        Case envelope = getMailEnvelope();
        createDraftPost(senderMailbox, envelope);

        assertTrue(correspService.getDraftCaseLink(session, senderMailbox,
                envelope.getDocument().getId()).isDraft());

        CaseLink postRequest = new CaseLinkRequestImpl(
                senderMailbox.getId(), Calendar.getInstance(),
                "Check this out", "it is a bit boring", envelope, recipients,
                null);

        CaseLink post = correspService.sendCase(session,
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

    protected void createCaseFolder() throws ClientException {
        // create a mailbox with given user, and check it's retrieved correctly
        DocumentModel mailboxModel = session.createDocumentModel(CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
        CaseFolder newMailbox = mailboxModel.getAdapter(CaseFolder.class);
        // set users
        newMailbox.setId("test");
        newMailbox.setTitle("Test");
        newMailbox.setType(CaseFolderConstants.type.generic.name());
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
