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

import java.util.ArrayList;

import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;


/**
 * @author Nulrich
 * 
 */
public class TestModel extends SQLRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // deploy type contrib
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE,
                "OSGI-INF/corresp-core-types-contrib.xml");

        openSession();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public DocumentModel createTestFolder() throws ClientException {
        // Create Test Folder
        DocumentModel folder = new DocumentModelImpl("/", "testfolder",
                "Folder");
        return session.createDocument(folder);

    }

    public void testMailCreation() throws Exception {

        DocumentModel folder = createTestFolder();

        // Create the Mail Box
        DocumentModel mailbox = new DocumentModelImpl(folder, "myMailBox",
                "CorrespondenceMailbox");
        mailbox = session.createDocument(mailbox);

        // Create Documents
        DocumentModel document = new DocumentModelImpl(folder, "doc1",
                "CorrespondenceDocument");
        document = session.createDocument(document);

        DocumentModel document2 = new DocumentModelImpl(folder, "doc1",
                "CorrespondenceDocument");
        document2 = session.createDocument(document2);

        // Create Envelope with 2 Documents
        ArrayList<String> documentIds = new ArrayList<String>();
        documentIds.add(document.getId());
        documentIds.add(document2.getId());

        DocumentModel envelope = new DocumentModelImpl(folder, "envelope",
                "CorrespondenceEnvelope");
        envelope.setPropertyValue("cenv:documentsId", documentIds);
        envelope = session.createDocument(envelope);

        // Dispatch the Envelope
        DocumentModel dispatch = new DocumentModelImpl(folder, "post",
                "CorrespondencePost");
        dispatch.setPropertyValue("cpost:envelopeId", envelope.getId());
        dispatch = session.createDocument(dispatch);

    }

   
}
