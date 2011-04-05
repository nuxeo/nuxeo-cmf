package org.nuxeo.cm.service;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.service.caseimporter.CaseManagementCaseImporterService;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

public class TestCaseImporter extends CaseManagementRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.platform.importer.core");
        openSession();
    }

    public void testImport() throws Exception {
        CaseManagementCaseImporterService caseImporterService = getCaseManagementCaseImporterService();
        assertNotNull(caseImporterService);
        caseImporterService.importCases(FileUtils.getResourceFileFromContext(
                ("case-import-src")).getPath());

        DocumentModel kaseParentDoc = session.getDocument(new PathRef(
                distributionService.getParentDocumentPathForCase(session)));
        DocumentModelList allCases = session.getChildren(kaseParentDoc.getRef());
        assertEquals(1, allCases.size());
        DocumentModel caseDoc1 = allCases.get(0);
        assertNotNull(caseDoc1);
        assertEquals("Test Case", caseDoc1.getTitle());

        Case case1 = caseDoc1.getAdapter(Case.class);
        String caseItemsParentPath = distributionService.getParentDocumentPathForCaseItem(
                session, case1);
        DocumentModelList caseItemDocs = session.getChildren(new PathRef(
                caseItemsParentPath));
        assertEquals(2, caseItemDocs.size());
        assertEquals("hello.pdf", caseItemDocs.get(0).getTitle());
        assertEquals("hello2.pdf", caseItemDocs.get(1).getTitle());
    }

    CaseManagementCaseImporterService getCaseManagementCaseImporterService()
            throws Exception {
        return Framework.getService(CaseManagementCaseImporterService.class);
    }

}
