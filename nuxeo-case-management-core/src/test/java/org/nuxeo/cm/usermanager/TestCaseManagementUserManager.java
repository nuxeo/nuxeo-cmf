package org.nuxeo.cm.usermanager;

import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.computedgroups.UserManagerWithComputedGroups;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;

public class TestCaseManagementUserManager extends
        CaseManagementRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        // set test property so that login module stuff is ignored during tests
        System.setProperty("org.nuxeo.runtime.testing", "true");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty("org.nuxeo.runtime.testing");
    }

    public void testGetPrincipalGroups() throws Exception {
        CaseFolder mailbox = correspCaseFolderService.createPersonalCaseFolders(
                session, user).get(0);
        assertNotNull(mailbox);
        assertTrue(correspCaseFolderService.hasCaseFolder(mailbox.getId()));
        CaseFolder mailbox2 = correspCaseFolderService.createPersonalCaseFolders(
                session, user2).get(0);
        assertNotNull(mailbox2);
        assertTrue(correspCaseFolderService.hasCaseFolder(mailbox2.getId()));

        assertTrue(userManager instanceof UserManagerWithComputedGroups);
        NuxeoPrincipal pal = userManager.getPrincipal(user);
        assertNotNull(pal);
        assertTrue(pal instanceof NuxeoPrincipalImpl);
        assertFalse(pal.isAdministrator());
        List<String> groups = pal.getGroups();
        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertTrue(groups.contains("group_1"));
        assertTrue(groups.contains("mailbox_user-user"));

        pal = userManager.getPrincipal(user2);
        assertNotNull(pal);
        assertTrue(pal instanceof NuxeoPrincipalImpl);
        assertFalse(pal.isAdministrator());
        groups = pal.getGroups();
        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertTrue(groups.contains("group_1"));
        assertTrue(groups.contains("mailbox_user-user2"));

        NuxeoGroup grp = userManager.getGroup("mailbox_user-user2");
        assertNull(grp);
        grp = userManager.getGroup("user-user2");
        assertNull(grp);

    }

}
