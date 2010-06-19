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

import java.util.List;
import java.util.Map;

import org.nuxeo.cm.core.service.synchronization.DefaultPersonalMailboxTitleGenerator;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Laurent Doguin
 */
public class TestMailboxSynchronizationService extends SQLRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // deploy repository manager
        deployBundle("org.nuxeo.ecm.core.api");

        // deploy search
        deployBundle("org.nuxeo.ecm.platform.search.api");

        // deploy api and core bundles
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_API_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);

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
        openSession();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        MailboxSyncTestListener.resetCounter();
    }

    public void testService() throws Exception {
        MailboxSynchronizationService synchronizationService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(synchronizationService);
        Map<String, MailboxDirectorySynchronizationDescriptor> synchronizerMap = synchronizationService.getSynchronizerMap();
        MailboxUserSynchronizationDescriptor userDirectorySynchronizer = synchronizationService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalMailboxTitleGenerator);

        MailboxGroupSynchronizationDescriptor groupDirectorySynchronizer = synchronizationService.getGroupSynchronizer();
        assertTrue(groupDirectorySynchronizer.isEnabled());

        // Test service override
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-cm-synchronization-override-contrib.xml");

        MailboxDirectorySynchronizationDescriptor uncompleteContrib = synchronizerMap.get("uncompleteContrib");
        assertNull(uncompleteContrib);

        synchronizationService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(synchronizationService);
        userDirectorySynchronizer = synchronizationService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalMailboxTitleGenerator);

        groupDirectorySynchronizer = synchronizationService.getGroupSynchronizer();
        assertTrue(!groupDirectorySynchronizer.isEnabled());
    }

    public void testSynchro() throws Exception {
        MailboxSynchronizationService synchronizationService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test synchronization
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(11, MailboxSyncTestListener.onMailboxCreatedForGroup);
        assertEquals(7, MailboxSyncTestListener.onMailboxCreatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxUpdatedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxUpdatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForUser);
        DocumentModel subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-6"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-6/group-9"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-7"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-5/"));
        assertNotNull(subGroup);
        List<DocumentModel> group5Children = session.getChildren(subGroup.getRef());
        assertTrue(group5Children.isEmpty());
        // test onDelete event
        DirectoryService dirService = Framework.getService(DirectoryService.class);
        dirService.open("userDirectory").deleteEntry("user");
        dirService.open("groupDirectory").deleteEntry("group_4");
        MailboxSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForUser);
        assertEquals(10, MailboxSyncTestListener.onMailboxUpdatedForGroup);
        assertEquals(6, MailboxSyncTestListener.onMailboxUpdatedForUser);
        assertEquals(1, MailboxSyncTestListener.onMailboxDeletedForGroup);
        assertEquals(1, MailboxSyncTestListener.onMailboxDeletedForUser);
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/"));
        assertEquals("deleted",
                session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-6"));
        assertEquals("deleted",
                session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-6/group-9"));
        assertEquals("deleted",
                session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef(
                "/case-management/mailbox-root/group-4/group-7"));
        assertEquals("deleted",
                session.getCurrentLifeCycleState(subGroup.getRef()));
        Mailbox cfGroup = subGroup.getAdapter(Mailbox.class);
        String groupOwner = cfGroup.getOwner();
        assertNull(groupOwner);
        DocumentModel userMailboxDoc = session.getDocument(new PathRef(
                "/case-management/mailbox-root/user-lambda-mycomp"));
        Mailbox userMailbox = userMailboxDoc.getAdapter(Mailbox.class);
        String owner = userMailbox.getOwner();
        assertEquals("user", owner);
        // test unSynchronized mailbox
        DocumentModel membersMailboxDoc = session.getDocument(new PathRef(
                "/case-management/mailbox-root/members"));
        Mailbox membersMailbox = membersMailboxDoc.getAdapter(Mailbox.class);
        membersMailbox.setSynchronizeState(MailboxSynchronizationConstants.synchronisedState.unsynchronised.toString());
        membersMailbox.save(session);
        MailboxSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForUser);
        assertEquals(9, MailboxSyncTestListener.onMailboxUpdatedForGroup);
        assertEquals(6, MailboxSyncTestListener.onMailboxUpdatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForUser);

    }

    public void testSynchroWithExistingMailbox() throws Exception {
        DocumentModel membersGroupMailboxDoc = session.createDocumentModel(
                "/case-management/mailbox-root/", "members",
                MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        membersGroupMailboxDoc = session.createDocument(membersGroupMailboxDoc);
        membersGroupMailboxDoc.setPropertyValue("dc:title", "members");
        session.saveDocument(membersGroupMailboxDoc);
        DocumentModel ldoguinUserMailbox = session.createDocumentModel(
                "/case-management/mailbox-root/", "laurent-o-doguin-nuxeo",
                MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        ldoguinUserMailbox = session.createDocument(membersGroupMailboxDoc);
        ldoguinUserMailbox.setPropertyValue("dc:title",
                "laurent O'doguin (nuxeo/reponseD)");
        session.saveDocument(ldoguinUserMailbox);
        MailboxSynchronizationService synchronizationService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test unsynchronized Mailbox with same title
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(10, MailboxSyncTestListener.onMailboxCreatedForGroup);
        assertEquals(6, MailboxSyncTestListener.onMailboxCreatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxUpdatedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxUpdatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForUser);
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
        MailboxSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxCreatedForUser);
        assertEquals(11, MailboxSyncTestListener.onMailboxUpdatedForGroup);
        assertEquals(7, MailboxSyncTestListener.onMailboxUpdatedForUser);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForGroup);
        assertEquals(0, MailboxSyncTestListener.onMailboxDeletedForUser);
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
        MailboxSynchronizationService synchronizationService = Framework.getService(MailboxSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test unsynchronized Mailbox with same title
        synchronizationService.doSynchronize();
        session.save();
        DocumentModel userMailboxDoc = session.getDocument(new PathRef(
                "/case-management/mailbox-root/user-lambda-mycomp"));
        ACP userACP = userMailboxDoc.getACP();
        ACL userACL = userACP.getACL(ACL.LOCAL_ACL);
        ACE[] userACE = userACL.getACEs();
        Boolean found = false;
        for (ACE ace : userACE) {
            if (ace.toString().endsWith("user:ReadWrite:true")) {
                found = true;
            }
        }
        assertTrue(found);

        DocumentModel groupMailboxDoc = session.getDocument(new PathRef(
                "/case-management/mailbox-root/members"));
        ACP groupACP = groupMailboxDoc.getACP();
        ACL groupACL = groupACP.getACL(ACL.LOCAL_ACL);
        ACE[] groupACE = groupACL.getACEs();
        found = false;
        for (ACE ace : groupACE) {
            if (ace.toString().equals("members:ReadWrite:true")) {
                found = true;
            }
        }
        assertTrue(found);
    }

}
