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

package org.nuxeo.cm.mailbox;

import java.util.Arrays;
import java.util.List;

import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;

/**
 * @author Anahide Tchertchian
 */
public class TestMailbox extends SQLRepositoryTestCase {

    private static final String MAILBOX = "mailbox";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);
        deployBundle("org.nuxeo.ecm.platform.routing.core");
        // create mailbox at server root
        openSession();
    }

    protected DocumentModel getBareMailboxDoc() throws Exception {
        DocumentModel mailbox = session.createDocumentModel(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        mailbox.setPathInfo(session.getRootDocument().getPathAsString(),
                MAILBOX);
        return mailbox;
    }

    protected Mailbox getMailbox() throws Exception {
        DocumentRef docRef = new PathRef("/" + MAILBOX);
        return session.getDocument(docRef).getAdapter(Mailbox.class);
    }

    protected DocumentModel createMailboxDoc() throws Exception {
        DocumentModel doc = getBareMailboxDoc();
        Mailbox mb = doc.getAdapter(Mailbox.class);

        mb.setId("mailboxid");
        mb.setTitle("mailbox title");
        mb.setDescription("mb description");
        mb.setType(MailboxConstants.type.personal.name());

        mb.setProfiles(Arrays.asList(new String[] { "profile1" }));

        mb.setOwner("mbowner");
        mb.setUsers(Arrays.asList(new String[] { "toto", "titi" }));
        mb.setNotifiedUsers(Arrays.asList(new String[] { "toto" }));
        mb.setGroups(Arrays.asList(new String[] { "group1", "group2" }));

        mb.setFavorites(Arrays.asList(new String[] { "fav1", "fav2" }));

        MailingList ml = mb.getMailingListTemplate();
        ml.setId("mlid");
        ml.setTitle("ml title");
        ml.setDescription("ml description");
        ml.setMailboxIds(Arrays.asList(new String[] { "mb1", "mb2" }));
        mb.addMailingList(ml);

        return session.createDocument(mb.getDocument());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMailboxCreation() throws Exception {
        createMailboxDoc();

        Mailbox mb = getMailbox();
        assertEquals("mailboxid", mb.getId());
        assertEquals("mailbox title", mb.getTitle());
        assertEquals("mb description", mb.getDescription());
        assertEquals("personal", mb.getType());

        assertEquals(Arrays.asList(new String[] { "profile1" }),
                mb.getProfiles());

        assertEquals("mbowner", mb.getOwner());
        assertEquals(Arrays.asList(new String[] { "toto", "titi" }),
                mb.getUsers());
        assertEquals(Arrays.asList(new String[] { "toto" }),
                mb.getNotifiedUsers());

        assertEquals(Arrays.asList(new String[] { "mbowner", "toto", "titi" }),
                mb.getAllUsers());

        assertEquals(Arrays.asList(new String[] { "group1", "group2" }),
                mb.getGroups());

        assertEquals(Arrays.asList(new String[] { "fav1", "fav2" }),
                mb.getFavorites());
        assertEquals(Arrays.asList(new String[] { "mlid" }),
                mb.getMailingListIds());
        List<MailingList> mls = mb.getMailingLists();
        assertEquals(1, mls.size());
        MailingList ml = mls.get(0);
        assertEquals("mlid", ml.getId());
        assertEquals("ml title", ml.getTitle());
        assertEquals("ml description", ml.getDescription());
        assertEquals(Arrays.asList(new String[] { "mb1", "mb2" }),
                ml.getMailboxIds());

        assertEquals((Integer) 4, mb.getConfidentiality());
    }

    public void testMailboxEdition() throws Exception {
        createMailboxDoc();

        Mailbox mb = getMailbox();
        mb.setId("newid");
        mb.setTitle("new mailbox title");
        mb.setDescription("new mb description");
        mb.setType(MailboxConstants.type.generic.name());

        mb.setProfiles(Arrays.asList(new String[] { "profile1", "profile2" }));

        mb.setOwner("newmbowner");
        mb.setUsers(Arrays.asList(new String[] { "toto", "titi", "tutu" }));
        mb.setNotifiedUsers(Arrays.asList(new String[] { "toto", "tutu" }));
        mb.setGroups(Arrays.asList(new String[] { "group1", "group2", "group3" }));

        mb.setFavorites(Arrays.asList(new String[] { "fav1" }));
        mb.setConfidentiality(3);

        mb.removeMailingList("mlid");

        MailingList ml = mb.getMailingListTemplate();
        ml.setId("newmlid");
        ml.setTitle("new ml title");
        ml.setDescription("new ml description");
        ml.setMailboxIds(Arrays.asList(new String[] { "mb1" }));
        mb.addMailingList(ml);

        session.saveDocument(mb.getDocument());

        mb = getMailbox();
        assertEquals("newid", mb.getId());
        assertEquals("new mailbox title", mb.getTitle());
        assertEquals("new mb description", mb.getDescription());
        assertEquals("generic", mb.getType());

        assertEquals(Arrays.asList(new String[] { "profile1", "profile2" }),
                mb.getProfiles());

        assertEquals("newmbowner", mb.getOwner());
        assertEquals(Arrays.asList(new String[] { "toto", "titi", "tutu" }),
                mb.getUsers());
        assertEquals(Arrays.asList(new String[] { "toto", "tutu" }),
                mb.getNotifiedUsers());

        assertEquals(Arrays.asList(new String[] { "newmbowner", "toto", "titi",
                "tutu" }), mb.getAllUsers());

        assertEquals(
                Arrays.asList(new String[] { "group1", "group2", "group3" }),
                mb.getGroups());

        assertEquals(Arrays.asList(new String[] { "fav1" }), mb.getFavorites());
        assertEquals(Arrays.asList(new String[] { "newmlid", }),
                mb.getMailingListIds());

        List<MailingList> mls = mb.getMailingLists();
        assertEquals(1, mls.size());

        ml = mls.get(0);
        assertEquals("newmlid", ml.getId());
        assertEquals("new ml title", ml.getTitle());
        assertEquals("new ml description", ml.getDescription());
        assertEquals(Arrays.asList(new String[] { "mb1" }), ml.getMailboxIds());

        assertEquals((Integer) 3, mb.getConfidentiality());
    }

}
