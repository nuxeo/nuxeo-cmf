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
 *     mcedica
 */
package org.nuxeo.cm.service.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.service.CaseManagementImporterService;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.common.utils.FileNamePattern;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author Mariana Cedica
 */
public class TestCaseManagementImporterService extends
        CaseManagementRepositoryTestCase {

    @Override
    public void setUp() throws Exception {

        super.setUp();
        // since we are using multiple threads in order to import and create
        // docs we need to make sure that the repository is
        // correctly initialized (in the database) and that means the
        // transaction in which these docs are created is committed
        // force commit transaction
        closeSession();
        TransactionHelper.commitOrRollbackTransaction();
        openSession();
    }

    @Override
    protected void deployRepositoryContrib() throws Exception {
        // TODO Auto-generated method stub
        super.deployRepositoryContrib();
        deployBundle("org.nuxeo.ecm.platform.audit.api");
        deployContrib("org.nuxeo.cm.core.test",
                "test-cm-default-importer-contrib.xml");
    }

    public void testImporter() throws Exception {

        File classResources = FileUtils.getFileFromURL(Thread.currentThread().getContextClassLoader().getResource(
                "."));
        File rootDir = new File(classResources.getAbsolutePath().substring(0,
                classResources.getAbsolutePath().indexOf("target/"))
                + "/src/test/resources/import-src/");

        CaseManagementImporterService importerService = getCaseManagementImporterService();
        assertNotNull(importerService);

        // import to a personal caseFolder
        CaseFolder destinationCaseFolder = getPersonalCaseFolder(user1);
        assertNotNull(destinationCaseFolder);
        assertEquals("/case-management/case-folder-root/user1-lambda",
                destinationCaseFolder.getDocument().getPathAsString());

        importerService.importDocuments();
        List<File> resourcesFiles = collectResourceFiles(rootDir);

        // Retrieve the post in the initial receiver casefolder
        List<CaseLink> postInCaseFolder = distributionService.getReceivedCaseLinks(
                session, destinationCaseFolder, 0, 0);
        // test only for pdf resources
        List<String> resourceTitles = resourceFilesTitles(resourcesFiles);
        for (CaseLink caseLink : postInCaseFolder) {
            Case caseFromPost = caseLink.getCase(session);
            List<CaseItem> itemsInCase = caseFromPost.getCaseItems(session);
            assertEquals(1, itemsInCase.size());
            assertEquals(caseFromPost.getDocument().getId(),
                    itemsInCase.get(0).getDefaultCaseId());
            assertNotNull(itemsInCase.get(0).getDocument().getPropertyValue(
                    "file:filename"));
            if (itemsInCase.get(0).getTitle().endsWith("pdf")) {
                assertTrue(resourceTitles.contains(CaseConstants.DOCUMENT_IMPORTED_PREFIX
                        + itemsInCase.get(0).getTitle()));
                assertTrue(resourceTitles.contains(CaseConstants.DOCUMENT_IMPORTED_PREFIX + (String) itemsInCase.get(0).getDocument().getPropertyValue(
                        "file:filename")));
            }
        }
        // during the import the file names were changed to _imported_ in order
        // to avoid importing them twice, we have to restore them back
        testImportedFilNames(rootDir);
        restoreTestFiles(rootDir);

    }

    private CaseFolder getPersonalCaseFolder(String username) throws Exception {
        return correspCaseFolderService.createPersonalCaseFolders(session,
                username).get(0);
    }

    private CaseManagementImporterService getCaseManagementImporterService()
            throws ClientException {
        try {
            return Framework.getService(CaseManagementImporterService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    private List<File> collectResourceFiles(File rootDir) {
        List<File> testFiles = new ArrayList<File>();
        // collect only pdf resource files
        FileUtils.collectFiles(rootDir, new FileNamePattern("*.pdf"), testFiles);
        return testFiles;
    }

    private List<String> resourceFilesTitles(List<File> testFiles) {
        List<String> titles = new ArrayList<String>();
        for (File file : testFiles) {
            titles.add(file.getName());
        }
        return titles;
    }

    private void testImportedFilNames(File rootDir) {
        for (File child : rootDir.listFiles()) {
            if (child.isFile() && child.getName().endsWith("pdf")) {
                assertTrue(child.getName().startsWith(
                        CaseConstants.DOCUMENT_IMPORTED_PREFIX));
            } else if (child.isDirectory()) {
                testImportedFilNames(child);
            }
        }
    }

    private void restoreTestFiles(File rootFiles) {
        // during the import the file names were changed to _imported_ in order
        // to avoid importing them twice, we have to restore them back
        for (File child : rootFiles.listFiles()) {
            if (child.isFile()
                    && child.getName().startsWith(
                            CaseConstants.DOCUMENT_IMPORTED_PREFIX)) {
                child.renameTo(new File(child.getAbsolutePath().replace(
                        child.getName(),
                        child.getName().replace(
                                CaseConstants.DOCUMENT_IMPORTED_PREFIX, ""))));
            } else if (child.isDirectory()) {
                restoreTestFiles(child);
            }
        }
    }

}
