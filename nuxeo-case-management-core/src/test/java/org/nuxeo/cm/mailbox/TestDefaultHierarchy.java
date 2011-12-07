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

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;

public class TestDefaultHierarchy extends SQLRepositoryTestCase {

    protected NuxeoPrincipal realAdmin;

    protected NuxeoPrincipalImpl memberUser;

    protected NuxeoPrincipal anonymousUser;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // needed for users
        deployBundle(CaseManagementTestConstants.DIRECTORY_BUNDLE);
        deployBundle(CaseManagementTestConstants.USERMANAGER_BUNDLE);
        deployBundle(CaseManagementTestConstants.DIRECTORY_TYPES_BUNDLE);
        deployBundle(CaseManagementTestConstants.DIRECTORY_SQL_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_TEST_BUNDLE);

        // contributions to the ContentTemplate service
        deployContrib(CaseManagementTestConstants.TEMPLATE_BUNDLE,
                "OSGI-INF/content-template-framework.xml");
        deployContrib(CaseManagementTestConstants.TEMPLATE_BUNDLE,
                "OSGI-INF/content-template-listener.xml");

        // deploy CMF document types
        deployContrib(CaseManagementTestConstants.ROUTING_CORE_BUNDLE,
                "OSGI-INF/document-routing-core-types-contrib.xml");
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE,
                "OSGI-INF/cm-core-types-contrib.xml");

        realAdmin = new NuxeoPrincipalImpl("realAdmin", false, true);
        memberUser = new NuxeoPrincipalImpl("memberUser", false, false);
        memberUser.setGroups(Arrays.asList(new String[] { "members" }));
        memberUser.updateAllGroups();
        anonymousUser = new NuxeoPrincipalImpl("anonymous", true, false);
    }

    public void testRootsWithCAP() throws Exception {
        deployContrib(CaseManagementTestConstants.TEMPLATE_BUNDLE,
                "OSGI-INF/content-template-contrib.xml");
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE,
                "OSGI-INF/cm-content-template-contrib.xml");

        openSession();
        try {
            DocumentModel domain = session.getRootDocument();
            assertTrue(session.hasPermission(realAdmin, domain.getRef(),
                    SecurityConstants.READ));
            assertTrue(session.hasPermission(memberUser, domain.getRef(),
                    SecurityConstants.READ));
            assertFalse(session.hasPermission(anonymousUser, domain.getRef(),
                    SecurityConstants.READ));

            DocumentModelList domainChildren = session.getChildren(domain.getRef());
            assertEquals(2, domainChildren.size());

            DocumentModel defaultDomain = session.getChild(domain.getRef(),
                    "default-domain");
            assertNotNull(defaultDomain);
            assertTrue(session.hasPermission(realAdmin, defaultDomain.getRef(),
                    SecurityConstants.READ));
            assertTrue(session.hasPermission(memberUser,
                    defaultDomain.getRef(), SecurityConstants.READ));
            assertFalse(session.hasPermission(anonymousUser,
                    defaultDomain.getRef(), SecurityConstants.READ));

            checkCMFDomainHierarchy();
        } finally {
            closeSession();
        }
    }

    public void testRootsWithoutCAP() throws Exception {
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE,
                "OSGI-INF/cm-content-template-contrib.xml");

        openSession();
        try {
            DocumentModel domain = session.getRootDocument();
            assertTrue(session.hasPermission(realAdmin, domain.getRef(),
                    SecurityConstants.READ));
            assertTrue(session.hasPermission(memberUser, domain.getRef(),
                    SecurityConstants.READ));
            assertFalse(session.hasPermission(anonymousUser, domain.getRef(),
                    SecurityConstants.READ));

            DocumentModelList domainChildren = session.getChildren(domain.getRef());
            assertEquals(1, domainChildren.size());

            checkCMFDomainHierarchy();
        } finally {
            closeSession();
        }
    }

    protected void checkCMFDomainHierarchy() throws Exception {
        DocumentModel domain = session.getDocument(new PathRef(
                CaseConstants.CASE_DOMAIN_PATH));
        assertEquals(CaseConstants.CASE_DOMAIN_PATH, domain.getPathAsString());
        assertTrue(session.hasPermission(realAdmin, domain.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(memberUser, domain.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(anonymousUser, domain.getRef(),
                SecurityConstants.READ));

        DocumentModelList domainChildren = session.getChildren(domain.getRef());
        assertEquals(3, domainChildren.size());

        DocumentModel mailRoot = session.getChild(domain.getRef(),
                CaseConstants.CASE_ROOT_DOCUMENT_NAME);
        assertNotNull(mailRoot);
        assertTrue(session.hasPermission(realAdmin, mailRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(memberUser, mailRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(anonymousUser, mailRoot.getRef(),
                SecurityConstants.READ));

        DocumentModel mailboxRoot = session.getChild(domain.getRef(),
                MailboxConstants.MAILBOX_ROOT_DOCUMENT_NAME);
        assertNotNull(mailboxRoot);
        assertTrue(session.hasPermission(realAdmin, mailboxRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(memberUser, mailboxRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(anonymousUser, mailboxRoot.getRef(),
                SecurityConstants.READ));

        DocumentModel sectionsRoot = session.getChild(domain.getRef(),
                "sections");
        assertNotNull(sectionsRoot);
        assertTrue(session.hasPermission(realAdmin, sectionsRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(memberUser, sectionsRoot.getRef(),
                SecurityConstants.READ));
        assertFalse(session.hasPermission(anonymousUser, sectionsRoot.getRef(),
                SecurityConstants.READ));
    }

}
