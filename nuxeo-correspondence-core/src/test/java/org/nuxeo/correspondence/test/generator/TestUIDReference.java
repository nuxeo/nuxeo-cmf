/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     mcedica
 */
package org.nuxeo.correspondence.test.generator;

import static org.junit.Assert.assertEquals;
import static org.nuxeo.cm.test.CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE;
import static org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants.CORRESPONDENCE_CORE_BUNDLE;
import static org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants.CORRESPONDENCE_CORE_TEST_BUNDLE;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Test;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.runtime.jtajca.NuxeoContainer;

/**
 * Tests if the uid property is set on a document at creation
 */
public class TestUIDReference extends CaseManagementRepositoryTestCase {

    static int cnt = 0;

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();
        NuxeoContainer.installNaming();
        deployBundle("org.nuxeo.ecm.core.persistence");
        deployBundle("org.nuxeo.ecm.platform.uidgen.core");
        deployContrib(CORRESPONDENCE_CORE_TEST_BUNDLE,
                "test-corresp-nxuidgenerator-bundle.xml");
        deployBundle(CASE_MANAGEMENT_CORE_BUNDLE);
        deployContrib(CORRESPONDENCE_CORE_BUNDLE,
                "OSGI-INF/corresp-core-types-contrib.xml");
        deployContrib(CORRESPONDENCE_CORE_BUNDLE,
                "OSGI-INF/corresp-uid-contrib.xml");
        openSession();
    }

    @After
    public void tearDown() throws Exception {
        try {
            closeSession();
        } finally {
            if (NuxeoContainer.isInstalled()) {
                NuxeoContainer.uninstall();
            }
            super.tearDown();
        }
    }

    public DocumentModel createTestDocument() throws Exception {
        // Create an incoming document
        DocumentModel folder = new DocumentModelImpl("/", "testfolder",
                "Folder");
        session.createDocument(folder);
        session.saveDocument(folder);

        DocumentModel mailbox = new DocumentModelImpl(folder, "myMailBox",
                MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        mailbox = session.createDocument(mailbox);
        session.saveDocument(mailbox);
        DocumentModel document = new DocumentModelImpl(folder, "doc1",
                "IncomingCorrespondenceDocument");
        document = session.createDocument(document);
        session.saveDocument(document);
        return document;
    }

    @Test
    public void testUidReference() throws Exception {
        DocumentModel doc = createTestDocument();
        String expectedUID = String.format("NXC-IN-%s-00001", getSequenceKey());
        assertEquals(expectedUID, doc.getPropertyValue("uid:uid"));
    }

    protected String getSequenceKey() throws DocumentException {
        Calendar cal = new GregorianCalendar();
        return Integer.toString(cal.get(Calendar.YEAR));
    }

}
