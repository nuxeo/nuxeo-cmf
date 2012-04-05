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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.service.synchronization;

import static org.nuxeo.cm.mailbox.MailboxConstants.MAILBOX_ROOT_DOCUMENT_PATH;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbCreatedForGroup;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbCreatedForUser;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbDeletedForGroup;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbDeletedForUser;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbUpdatedForGroup;
import static org.nuxeo.cm.service.synchronization.MailboxSyncTestListener.mbUpdatedForUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.core.service.synchronization.DefaultPersonalMailboxTitleGenerator;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.classification.api.ClassificationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Laurent Doguin
 */
public class TestMailboxSynchronizationService extends SQLRepositoryTestCase {

    protected static String MB_ROOT = MAILBOX_ROOT_DOCUMENT_PATH + "/";

    protected MailboxSynchronizationService syncService;

    protected MailboxManagementService mbService;

    protected UserManager umService;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // deploy repository manager
        deployBundle("org.nuxeo.ecm.core.api");

        // deploy page provider service
        deployBundle("org.nuxeo.ecm.platform.query.api");

        // deploy api and core bundles
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_API_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);
        deployBundle("org.nuxeo.ecm.platform.classification.core");
        deployBundle("org.nuxeo.ecm.platform.routing.core");

        // needed for users
        deployBundle("org.nuxeo.ecm.directory");
        deployBundle("org.nuxeo.ecm.platform.usermanager");
        deployBundle("org.nuxeo.ecm.directory.types.contrib");
        deployBundle("org.nuxeo.ecm.directory.sql");
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_TEST_BUNDLE);

        // needed for default hierarchy
        deployBundle("org.nuxeo.ecm.platform.content.template");

        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-cm-event-listener-contrib.xml");
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-cm-synchronization-contrib.xml");

        fireFrameworkStarted();

        openSession();

        syncService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(syncService);

        mbService = Framework.getService(MailboxManagementService.class);
        assertNotNull(mbService);

        umService = Framework.getService(UserManager.class);
        assertNotNull(umService);
    }

    @Override
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
        MailboxSyncTestListener.reset();
    }

    public void testService() throws Exception {
        Map<String, MailboxDirectorySynchronizationDescriptor> synchronizerMap = syncService.getSynchronizerMap();
        MailboxUserSynchronizationDescriptor userDirectorySynchronizer = syncService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalMailboxTitleGenerator);

        MailboxGroupSynchronizationDescriptor groupDirectorySynchronizer = syncService.getGroupSynchronizer();
        assertEquals(Boolean.TRUE, groupDirectorySynchronizer.isEnabled());

        // Test service override
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-cm-synchronization-override-contrib.xml");

        MailboxDirectorySynchronizationDescriptor uncompleteContrib = synchronizerMap.get("uncompleteContrib");
        assertNull(uncompleteContrib);

        userDirectorySynchronizer = syncService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalMailboxTitleGenerator);

        groupDirectorySynchronizer = syncService.getGroupSynchronizer();
        assertEquals(Boolean.FALSE, groupDirectorySynchronizer.isEnabled());
    }

    public void testSynchro() throws Exception {
        syncService.doSynchronize();
        session.save();

        assertEquals(11, mbCreatedForGroup.size());
        assertEquals(10, mbCreatedForUser.size());
        assertEquals(0, mbUpdatedForGroup.size());
        assertEquals(0, mbUpdatedForUser.size());
        assertEquals(0, mbDeletedForGroup.size());
        assertEquals(0, mbDeletedForUser.size());
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-3")));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-4/group-4-1")));
        assertTrue(session.exists(new PathRef(MB_ROOT
                + "group-4/group-4-1/group-4-1-1")));
        assertTrue(session.exists(new PathRef(MB_ROOT
                + "group-4/group-4-1/group-4-1-2")));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-4/group-4-2")));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-5/")));
        DocumentModel group5 = session.getDocument(new PathRef(MB_ROOT
                + "group-5/"));
        assertNotNull(group5);
        List<DocumentModel> group5Children = session.getChildren(group5.getRef());
        assertFalse(group5Children.isEmpty());
        assertEquals(2, group5Children.size());
        group5Children = session.getChildren(group5.getRef(),
                ClassificationConstants.CLASSIFICATION_ROOT);
        // order is database-dependent
        if (MailboxConstants.ROUTE_ROOT_DOCUMENT_TYPE.equals(group5Children.get(
                0).getType())) {
            Collections.reverse(group5Children);
        }
        assertEquals(ClassificationConstants.CLASSIFICATION_ROOT,
                group5Children.get(0).getType());
        // test onDelete event
        DirectoryService dirService = Framework.getService(DirectoryService.class);
        dirService.open("userDirectory").deleteEntry("user");
        dirService.open("groupDirectory").deleteEntry("group_4");
        MailboxSyncTestListener.reset();
        syncService.doSynchronize();
        session.save();

        assertTrue(session.exists(new PathRef(MB_ROOT + "group-3")));
        assertEquals("deleted", getState(MB_ROOT + "group-4"));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-4")));
        assertEquals("deleted", getState(MB_ROOT + "group-4"));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-4/group-4-1")));
        assertEquals("deleted", getState(MB_ROOT + "group-4/group-4-1"));
        assertTrue(session.exists(new PathRef(MB_ROOT
                + "group-4/group-4-1/group-4-1-1")));
        assertEquals("deleted", getState(MB_ROOT
                + "group-4/group-4-1/group-4-1-1"));
        assertTrue(session.exists(new PathRef(MB_ROOT
                + "group-4/group-4-1/group-4-1-2")));
        assertEquals("deleted", getState(MB_ROOT
                + "group-4/group-4-1/group-4-1-2"));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-4/group-4-2")));
        assertEquals("deleted", getState(MB_ROOT + "group-4/group-4-2"));
        assertTrue(session.exists(new PathRef(MB_ROOT + "group-5/")));
        group5 = session.getDocument(new PathRef(MB_ROOT + "group-5/"));
        assertEquals("project", group5.getCurrentLifeCycleState());

        assertEquals(0, mbCreatedForGroup.size());
        assertEquals(0, mbCreatedForUser.size());
        assertEquals(9, mbUpdatedForGroup.size());
        assertEquals(9, mbUpdatedForUser.size());
        assertEquals(2, mbDeletedForGroup.size());
        assertTrue(mbDeletedForGroup.toString(),
                mbDeletedForGroup.contains(MB_ROOT + "group-4"));
        assertTrue(mbDeletedForGroup.toString(),
                mbDeletedForGroup.contains(MB_ROOT + "group-4/group-4-2"));
        // group-4-1 is kept because it has sub mailboxes(?)

        assertEquals(1, mbDeletedForUser.size());

        Mailbox cfGroup = group5.getAdapter(Mailbox.class);
        String groupOwner = cfGroup.getOwner();
        assertNull(groupOwner);

        DocumentModel userMailboxDoc = session.getDocument(new PathRef(MB_ROOT
                + "user-lambda-mycomp"));
        Mailbox userMailbox = userMailboxDoc.getAdapter(Mailbox.class);
        String owner = userMailbox.getOwner();
        assertEquals("user", owner);
        // test unSynchronized mailbox
        DocumentModel membersMailboxDoc = session.getDocument(new PathRef(
                MB_ROOT + "members"));
        Mailbox membersMailbox = membersMailboxDoc.getAdapter(Mailbox.class);
        membersMailbox.setSynchronizeState(MailboxSynchronizationConstants.synchronisedState.unsynchronised.toString());
        membersMailbox.save(session);
        MailboxSyncTestListener.reset();
        syncService.doSynchronize();
        assertEquals(0, mbCreatedForGroup.size());
        assertEquals(0, mbCreatedForUser.size());
        assertEquals(8, mbUpdatedForGroup.size());
        assertEquals(9, mbUpdatedForUser.size());
        assertEquals(0, mbDeletedForGroup.size());
        assertEquals(0, mbDeletedForUser.size());
    }

    protected String getState(String path) throws ClientException {
        return session.getCurrentLifeCycleState(new PathRef(path));
    }

    public void testSynchroWithExistingMailbox() throws Exception {
        // create a mailbox
        DocumentModel membersGroupMailboxDoc = session.createDocumentModel(
                MB_ROOT + "", "members", MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        membersGroupMailboxDoc = session.createDocument(membersGroupMailboxDoc);
        membersGroupMailboxDoc.setPropertyValue("dc:title", "members");
        session.saveDocument(membersGroupMailboxDoc);
        DocumentModel ldoguinUserMailbox = session.createDocumentModel(MB_ROOT
                + "", "laurent-o-doguin-nuxeo",
                MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        ldoguinUserMailbox = session.createDocument(membersGroupMailboxDoc);
        ldoguinUserMailbox.setPropertyValue("dc:title",
                "laurent O'doguin (nuxeo/reponseD)");
        session.saveDocument(ldoguinUserMailbox);

        // synchronize
        syncService.doSynchronize();
        session.save();

        assertEquals(10, mbCreatedForGroup.size());
        assertEquals(9, mbCreatedForUser.size());
        assertEquals(0, mbUpdatedForGroup.size());
        assertEquals(0, mbUpdatedForUser.size());
        assertEquals(0, mbDeletedForGroup.size());
        assertEquals(0, mbDeletedForUser.size());
        membersGroupMailboxDoc = session.getDocument(membersGroupMailboxDoc.getRef());
        Mailbox cf = membersGroupMailboxDoc.getAdapter(Mailbox.class);
        String state = cf.getSynchronizeState();
        assertEquals(
                MailboxSynchronizationConstants.synchronisedState.doublon.toString(),
                state);
        String origin = cf.getOrigin();
        assertEquals("", origin);
        cf.setSynchronizeState(MailboxSynchronizationConstants.synchronisedState.synchronised.toString());
        session.saveDocument(membersGroupMailboxDoc);
        ldoguinUserMailbox = session.getDocument(ldoguinUserMailbox.getRef());
        cf = ldoguinUserMailbox.getAdapter(Mailbox.class);
        state = cf.getSynchronizeState();
        assertEquals(
                MailboxSynchronizationConstants.synchronisedState.doublon.toString(),
                state);
        origin = cf.getOrigin();
        assertEquals("", origin);
        cf.setSynchronizeState(MailboxSynchronizationConstants.synchronisedState.synchronised.toString());
        session.saveDocument(ldoguinUserMailbox);
        MailboxSyncTestListener.reset();
        syncService.doSynchronize();
        session.save();
        assertEquals(0, mbCreatedForGroup.size());
        assertEquals(0, mbCreatedForUser.size());
        assertEquals(11, mbUpdatedForGroup.size());
        assertEquals(10, mbUpdatedForUser.size());
        assertEquals(0, mbDeletedForGroup.size());
        assertEquals(0, mbDeletedForUser.size());
        membersGroupMailboxDoc = session.getDocument(membersGroupMailboxDoc.getRef());
        cf = membersGroupMailboxDoc.getAdapter(Mailbox.class);
        origin = cf.getOrigin();
        assertEquals("groupDirectory", origin);
        ldoguinUserMailbox = session.getDocument(ldoguinUserMailbox.getRef());
        cf = ldoguinUserMailbox.getAdapter(Mailbox.class);
        origin = cf.getOrigin();
        assertEquals("userDirectory", origin);
    }

    public void testRights() throws Exception {
        syncService.doSynchronize();
        session.save();

        String domainACL = "administrators:Everything:true, Administrator:Everything:true, Everyone:Everything:false";
        String onlyDomainACL = "[" + domainACL + "]";
        checkRights("user-lambda-mycomp", "[user:ReadWrite:true]",
                onlyDomainACL);
        checkRights("members", "[cmfgroup_members:ReadWrite:true]",
                onlyDomainACL);
        checkRights("group-4", "[cmfgroup_group_4:ReadWrite:true]",
                onlyDomainACL);
        checkRights("group-4/group-4-1", "[cmfgroup_group_4_1:ReadWrite:true]",
                "[cmfgroup_group_4:ReadWrite:true, " + domainACL + "]");
        checkRights("group-4/group-4-1/group-4-1-1",
                "[cmfgroup_group_4_1_1:ReadWrite:true]",
                "[cmfgroup_group_4_1:ReadWrite:true, cmfgroup_group_4:ReadWrite:true, "
                        + domainACL + "]");
    }

    protected void checkRights(String mbPath, String localACL,
            String inheritedACL) throws ClientException {
        DocumentModel mbDoc = session.getDocument(new PathRef(MB_ROOT + mbPath));
        ACP acp = mbDoc.getACP();
        ACL localValue = acp.getACL(ACL.LOCAL_ACL);
        assertEquals(localACL, localValue.toString());
        ACL inhValue = acp.getACL(ACL.INHERITED_ACL);
        assertEquals(inheritedACL, inhValue.toString());
    }

    public void testGetMailboxes() throws Exception {
        syncService.doSynchronize();
        session.save();

        checkMailboxes("user", 2, new String[] { "user-user", "group-group-1" });
        checkMailboxes("user1", 4, new String[] { "user-user1",
                "group-group-1", "group-group-5", "group-group-4-2" });
        checkMailboxes("user2", 5, new String[] { "user-user2",
                "group-group-2", "group-group-4-1", "group-group-4-1-1",
                "group-group-4-1-2", });
        checkMailboxes("user3", 4, new String[] { "user-user3",
                "group-group-3", "group-members", "group-group-4-1-1" });
    }

    protected void checkMailboxes(String user, int numberOfMailboxes,
            String[] mailboxes) throws ClientException {
        // check mailboxes with own user session
        CoreSession userSession = null;
        try {
            NuxeoPrincipal pal = umService.getPrincipal(user);
            userSession = openSessionAs(pal);
            List<Mailbox> userMailboxes = mbService.getUserMailboxes(
                    userSession, user);
            checkMailboxes(userMailboxes, user, numberOfMailboxes, mailboxes);
        } finally {
            if (userSession != null) {
                CoreInstance.getInstance().close(userSession);
            }
        }

        // check mailboxes with admin session
        // disabled: FIXME: NXCM-506: when using the default user session
        // (who's an admin, mailboxes retrieved should be the same than the
        // ones retrieved when using a session with given user as principal)
        // List<Mailbox> userMailboxes = mbService.getUserMailboxes(session,
        // user);
        // checkMailboxes(userMailboxes, user, numberOfMailboxes, mailboxes);
    }

    protected void checkMailboxes(List<Mailbox> userMailboxes, String user,
            int numberOfMailboxes, String[] mailboxes) {
        assertFalse(userMailboxes.isEmpty());
        List<String> mbIds = new ArrayList<String>();
        for (Mailbox mailbox : userMailboxes) {
            mbIds.add(mailbox.getId());
        }
        assertEquals(numberOfMailboxes, userMailboxes.size());

        for (String mailbox : mailboxes) {
            assertTrue("User " + user + " is missing mailbox " + mailbox,
                    mbIds.contains(mailbox));
        }

        boolean personalMbFound = false;
        for (Mailbox mailbox : userMailboxes) {
            if (("user-" + user).equals(mailbox.getId())) {
                // personal mailbox
                assertEquals(MailboxConstants.type.personal.name(),
                        mailbox.getType());
                assertEquals(user, mailbox.getOwner());
                personalMbFound = true;
            } else {
                assertEquals(MailboxConstants.type.generic.name(),
                        mailbox.getType());
            }
        }
        assertTrue("No personal mailbox found for user " + user,
                personalMbFound);
    }
}
