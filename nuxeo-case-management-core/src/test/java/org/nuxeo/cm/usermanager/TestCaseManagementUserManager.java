/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.cm.usermanager;

import static org.nuxeo.cm.security.CaseManagementSecurityConstants.MAILBOX_GROUP_PREFIX;
import static org.nuxeo.cm.security.CaseManagementSecurityConstants.MAILBOX_PREFIX;

import java.util.List;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;
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
        closeSession();
        super.tearDown();
        System.clearProperty("org.nuxeo.runtime.testing");
    }

    public void testGetPrincipalGroups() throws Exception {
        assertTrue(userManager instanceof UserManagerWithComputedGroups);

        checkUser(user, 3,
                new String[] { "group_1", MAILBOX_PREFIX + "user-user",
                        MAILBOX_GROUP_PREFIX + "group_1" });

        checkUser(user2, 7, new String[] { "group_2", "group_4_1",
                MAILBOX_PREFIX + "user-user2",
                MAILBOX_GROUP_PREFIX + "group_2",
                MAILBOX_GROUP_PREFIX + "group_4_1",
                MAILBOX_GROUP_PREFIX + "group_4_1_1",
                MAILBOX_GROUP_PREFIX + "group_4_1_2" });

        checkUser(user3, 8, new String[] { "group_3", "members", "group_4_1_1",
                "group_unknown", MAILBOX_PREFIX + "user-user3",
                MAILBOX_GROUP_PREFIX + "group_3",
                MAILBOX_GROUP_PREFIX + "members",
                MAILBOX_GROUP_PREFIX + "group_4_1_1" });

    }

    protected void checkUser(String user, int numberOfGroups, String[] groups)
            throws ClientException {
        Mailbox mailbox = correspMailboxService.createPersonalMailboxes(
                session, user).get(0);
        assertNotNull(mailbox);
        assertTrue(correspMailboxService.hasMailbox(session, mailbox.getId()));

        NuxeoPrincipal pal = userManager.getPrincipal(user);
        assertNotNull(pal);
        assertTrue(pal instanceof NuxeoPrincipalImpl);
        assertFalse(pal.isAdministrator());

        List<String> pGroups = pal.getGroups();
        assertNotNull(pGroups);
        assertEquals(pGroups.toString(), numberOfGroups, pGroups.size());
        for (String group : groups) {
            assertTrue("User " + user + " is missing group " + group,
                    pGroups.contains(group));
        }
    }

}
