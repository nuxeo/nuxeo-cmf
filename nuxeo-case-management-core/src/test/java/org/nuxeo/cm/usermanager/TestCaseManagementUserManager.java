package org.nuxeo.cm.usermanager;

import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.core.usermanager.CaseManagementPrincipalImpl;
import org.nuxeo.cm.core.usermanager.CaseManagementUserManagerImpl;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;

import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;

public class TestCaseManagementUserManager extends CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testGetPrincipalGroups() throws Exception {
        assertTrue(userManager instanceof CaseManagementUserManagerImpl);
        NuxeoPrincipal admin = userManager.getPrincipal(user);
        assertNotNull(admin);
        assertTrue(admin instanceof CaseManagementPrincipalImpl);
        CaseFolder mailbox = correspService.createPersonalCaseFolders(session, user).get(0);
        assertNotNull(mailbox);
        List<String> groups = admin.getGroups();
        assertNotNull(groups);
        assertTrue(groups.contains("mailbox_" + mailbox.getId()));
    }

}
