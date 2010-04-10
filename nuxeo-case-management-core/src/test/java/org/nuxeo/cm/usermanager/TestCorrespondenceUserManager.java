package org.nuxeo.cm.usermanager;

import java.util.List;

import org.nuxeo.cm.core.usermanager.CorrespondencePrincipalImpl;
import org.nuxeo.cm.core.usermanager.CorrespondenceUserManagerImpl;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;

import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;

public class TestCorrespondenceUserManager extends CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testGetPrincipalGroups() throws Exception {
        assertTrue(userManager instanceof CorrespondenceUserManagerImpl);
        NuxeoPrincipal admin = userManager.getPrincipal(user);
        assertNotNull(admin);
        assertTrue(admin instanceof CorrespondencePrincipalImpl);
        Mailbox mailbox = correspService.createPersonalMailbox(session, user).get(0);
        assertNotNull(mailbox);
        List<String> groups = admin.getGroups();
        assertNotNull(groups);
        assertTrue(groups.contains("mailbox_" + mailbox.getId()));
    }

}
