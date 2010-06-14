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

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.core.service.synchronization.DefaultPersonalCFTitleGenerator;
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
public class TestCaseFolderSynchronizationService extends
        SQLRepositoryTestCase {

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
        CaseFolderSyncTestListener.resetCounter();
    }

    public void testService() throws Exception {
        CaseFolderSynchronizationService synchronizationService = Framework.getService(CaseFolderSynchronizationService.class);
        assertNotNull(synchronizationService);
        Map<String, CaseFolderDirectorySynchronizationDescriptor> synchronizerMap = synchronizationService.getSynchronizerMap();
        CaseFolderUserSynchronizationDescriptor userDirectorySynchronizer = synchronizationService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalCFTitleGenerator);

        CaseFolderGroupSynchronizationDescriptor groupDirectorySynchronizer = synchronizationService.getGroupSynchronizer();
        assertTrue(groupDirectorySynchronizer.isEnabled());

        // Test service override
        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-cm-synchronization-override-contrib.xml");

        CaseFolderDirectorySynchronizationDescriptor uncompleteContrib = synchronizerMap.get("uncompleteContrib");
        assertNull(uncompleteContrib);

        synchronizationService = Framework.getService(CaseFolderSynchronizationService.class);
        assertNotNull(synchronizationService);
        userDirectorySynchronizer = synchronizationService.getUserSynchronizer();
        assertTrue(userDirectorySynchronizer.getTitleGenerator() instanceof DefaultPersonalCFTitleGenerator);

        groupDirectorySynchronizer = synchronizationService.getGroupSynchronizer();
        assertTrue(!groupDirectorySynchronizer.isEnabled());
    }

    public void testSynchro() throws Exception {
        CaseFolderSynchronizationService synchronizationService = Framework.getService(CaseFolderSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test synchronization
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(11, CaseFolderSyncTestListener.onCaseFolderCreatedForGroup);
        assertEquals(7, CaseFolderSyncTestListener.onCaseFolderCreatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderUpdatedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderUpdatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForUser);
        DocumentModel subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-6"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-6/group-9"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-7"));
        assertNotNull(subGroup);
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-5/"));
        assertNotNull(subGroup);
        List<DocumentModel> group5Children = session.getChildren(subGroup.getRef());
        assertTrue(group5Children.isEmpty());
        // test onDelete event
        DirectoryService dirService = Framework.getService(DirectoryService.class);
        dirService.open("userDirectory").deleteEntry("user");
        dirService.open("groupDirectory").deleteEntry("group_4");
        CaseFolderSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForUser);
        assertEquals(10, CaseFolderSyncTestListener.onCaseFolderUpdatedForGroup);
        assertEquals(6, CaseFolderSyncTestListener.onCaseFolderUpdatedForUser);
        assertEquals(1, CaseFolderSyncTestListener.onCaseFolderDeletedForGroup);
        assertEquals(1, CaseFolderSyncTestListener.onCaseFolderDeletedForUser);
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/"));
        assertEquals("deleted", session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-6"));
        assertEquals("deleted", session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-6/group-9"));
        assertEquals("deleted", session.getCurrentLifeCycleState(subGroup.getRef()));
        subGroup = session.getDocument(new PathRef("/case-management/case-folder-root/group-4/group-7"));
        assertEquals("deleted", session.getCurrentLifeCycleState(subGroup.getRef()));
        CaseFolder cfGroup = subGroup.getAdapter(CaseFolder.class);
        String groupOwner  = cfGroup.getOwner();
        assertNull(groupOwner);
        DocumentModel userCaseFolderDoc = session.getDocument(new PathRef("/case-management/case-folder-root/user-lambda-mycomp"));
        CaseFolder userCaseFolder = userCaseFolderDoc.getAdapter(CaseFolder.class);
        String owner = userCaseFolder.getOwner();
        assertEquals("user", owner);
        // test unSynchronized case folder
        DocumentModel membersCaseFolderDoc = session.getDocument(new PathRef("/case-management/case-folder-root/members"));
        CaseFolder membersCaseFolder = membersCaseFolderDoc.getAdapter(CaseFolder.class);
        membersCaseFolder.setSynchronizeState(CaseFolderSynchronizationConstants.synchronisedState.unsynchronised.toString());
        membersCaseFolder.save(session);
        CaseFolderSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForUser);
        assertEquals(9, CaseFolderSyncTestListener.onCaseFolderUpdatedForGroup);
        assertEquals(6, CaseFolderSyncTestListener.onCaseFolderUpdatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForUser);

    }

    public void testSynchroWithExistingCaseFolder() throws Exception {
        DocumentModel membersGroupCaseFolderDoc = session.createDocumentModel("/case-management/case-folder-root/", "members", CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
        membersGroupCaseFolderDoc = session.createDocument(membersGroupCaseFolderDoc);
        membersGroupCaseFolderDoc.setPropertyValue("dc:title", "members");
        session.saveDocument(membersGroupCaseFolderDoc);
        DocumentModel ldoguinUserCaseFolder = session.createDocumentModel("/case-management/case-folder-root/", "laurent-o-doguin-nuxeo", CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
        ldoguinUserCaseFolder = session.createDocument(membersGroupCaseFolderDoc);
        ldoguinUserCaseFolder.setPropertyValue("dc:title", "laurent O'doguin (nuxeo/reponseD)");
        session.saveDocument(ldoguinUserCaseFolder);
        CaseFolderSynchronizationService synchronizationService = Framework.getService(CaseFolderSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test unsynchronized caseFolder with same title
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(10, CaseFolderSyncTestListener.onCaseFolderCreatedForGroup);
        assertEquals(6, CaseFolderSyncTestListener.onCaseFolderCreatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderUpdatedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderUpdatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForUser);
        membersGroupCaseFolderDoc = session.getDocument(membersGroupCaseFolderDoc.getRef());
        CaseFolder cf = membersGroupCaseFolderDoc.getAdapter(CaseFolder.class);
        String state = cf.getSynchronizeState();
        assertEquals(CaseFolderSynchronizationConstants.synchronisedState.doublon.toString(), state);
        String origin = cf.getOrigin();
        assertEquals("", origin);
        cf.setSynchronizeState(CaseFolderSynchronizationConstants.synchronisedState.synchronised.toString());
        session.saveDocument(membersGroupCaseFolderDoc);
        ldoguinUserCaseFolder = session.getDocument(ldoguinUserCaseFolder.getRef());
        cf = ldoguinUserCaseFolder.getAdapter(CaseFolder.class);
        state = cf.getSynchronizeState();
        assertEquals(CaseFolderSynchronizationConstants.synchronisedState.doublon.toString(), state);
        origin = cf.getOrigin();
        assertEquals("", origin);
        cf.setSynchronizeState(CaseFolderSynchronizationConstants.synchronisedState.synchronised.toString());
        session.saveDocument(ldoguinUserCaseFolder);
        CaseFolderSyncTestListener.resetCounter();
        synchronizationService.doSynchronize();
        session.save();
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderCreatedForUser);
        assertEquals(11, CaseFolderSyncTestListener.onCaseFolderUpdatedForGroup);
        assertEquals(7, CaseFolderSyncTestListener.onCaseFolderUpdatedForUser);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForGroup);
        assertEquals(0, CaseFolderSyncTestListener.onCaseFolderDeletedForUser);
        membersGroupCaseFolderDoc = session.getDocument(membersGroupCaseFolderDoc.getRef());
        cf = membersGroupCaseFolderDoc.getAdapter(CaseFolder.class);
        origin = cf.getOrigin();
        assertEquals("groupDirectory", origin);
        ldoguinUserCaseFolder = session.getDocument(ldoguinUserCaseFolder.getRef());
        cf = ldoguinUserCaseFolder.getAdapter(CaseFolder.class);
        origin = cf.getOrigin();
        assertEquals("userDirectory", origin);
    }

    public void testRights() throws Exception {
        CaseFolderSynchronizationService synchronizationService = Framework.getService(CaseFolderSynchronizationService.class);
        assertNotNull(synchronizationService);
        // test unsynchronized caseFolder with same title
        synchronizationService.doSynchronize();
        session.save();
        DocumentModel userCaseFolderDoc = session.getDocument(new PathRef("/case-management/case-folder-root/user-lambda-mycomp"));
        ACP userACP = userCaseFolderDoc.getACP();
        ACL userACL = userACP.getACL(ACL.LOCAL_ACL);
        ACE[] userACE = userACL.getACEs();
        Boolean found = false;
        for (ACE ace : userACE) {
            if (ace.toString().endsWith("user:ReadWrite:true")) {
                found = true;
            }
        }
        assertTrue(found);

        DocumentModel groupCaseFolderDoc = session.getDocument(new PathRef("/case-management/case-folder-root/members"));
        ACP groupACP = groupCaseFolderDoc.getACP();
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
