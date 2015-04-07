/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Contributors: Sun Seng David TAN <stan@nuxeo.com>, Mariana Cedica <mcedica@nuxeo.com>
 */
package org.nuxeo.cm.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.storage.sql.TXSQLRepositoryTestCase;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class TestMailTreeHelper extends TXSQLRepositoryTestCase {

    static Log log = LogFactory.getLog(TestMailTreeHelper.class);

    protected DocumentModel mailFolderDocument;

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();
        deployBundle("org.nuxeo.ecm.core.api");
        deployBundle("org.nuxeo.ecm.platform.classification.core");
        deployBundle("org.nuxeo.ecm.platform.routing.core");
        deployBundle("org.nuxeo.ecm.automation.core");
        deployBundle("org.nuxeo.ecm.platform.task.api");
        deployBundle("org.nuxeo.ecm.platform.task.core");
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_API_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);
        deployBundle("org.nuxeo.ecm.webapp.core");
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_TEST_BUNDLE);
        deployBundle("org.nuxeo.ecm.platform.content.template");

    }

    @Before
    public void setUp() throws Exception {
        // to have the test working with postgres - setup postgres database
        // "nuxeojunittests" on localhost with nuxeo/nuxeo
        // database = DatabasePostgreSQL.INSTANCE;
        super.setUp();
        // make sure this is actually created before the others docs
        mailFolderDocument = session.getDocument(new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH));
        assertNotNull(mailFolderDocument);
        closeSession();
        TransactionHelper.commitOrRollbackTransaction();
    }

    @Ignore("NXP-15519")
    @Test
    public void testSimple() throws Exception {
        CaseTreeHelper.getOrCreateTxDateTreeFolder(database.getRepositoryName(), mailFolderDocument,
                Calendar.getInstance().getTime(), CaseConstants.CASE_TREE_TYPE);

        TransactionHelper.startTransaction();
        openSession();

        DocumentModel caseRootFolder = session.getDocument(new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH));
        DocumentModelList yearsDocs = session.getChildren(caseRootFolder.getRef());
        assertEquals("the total number of years documents in the tree date folder is", yearsDocs.size(), 1);

        DocumentModelList monthsDocs = session.getChildren(yearsDocs.get(0).getRef());
        assertEquals("the total number of months documents in the tree date folder is", monthsDocs.size(), 1);

        DocumentModelList daysDocs = session.getChildren(monthsDocs.get(0).getRef());
        assertEquals("the total number of days documents in the tree date folder is", daysDocs.size(), 1);

    }

    @Test
    public void testParallelDocumentCreation() throws Exception {
        Thread[] threads = new Thread[2];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new MailTreeCreator(database.getRepositoryName(), mailFolderDocument));
            threads[i].start();
        }
        Thread.sleep(500);
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        TransactionHelper.startTransaction();
        openSession();

        DocumentModel caseRootFolder = session.getDocument(new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH));
        DocumentModelList yearsDocs = session.getChildren(caseRootFolder.getRef());
        assertEquals("the total number of years documents in the tree date folder is", yearsDocs.size(), 1);

        DocumentModelList monthsDocs = session.getChildren(yearsDocs.get(0).getRef());
        assertEquals("the total number of months documents in the tree date folder is", monthsDocs.size(), 1);

        DocumentModelList daysDocs = session.getChildren(monthsDocs.get(0).getRef());
        assertEquals("the total number of days documents in the tree date folder is", daysDocs.size(), 1);
    }

}
