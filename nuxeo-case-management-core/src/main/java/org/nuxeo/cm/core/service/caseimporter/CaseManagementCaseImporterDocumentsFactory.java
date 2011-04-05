/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *    mcedica
 */
package org.nuxeo.cm.core.service.caseimporter;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseItemSourceNode;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseSourceNode;
import org.nuxeo.cm.core.service.importer.CaseManagementCaseItemDocumentFactory;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.service.caseimporter.CaseManagementXMLCaseReader;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.ecm.platform.importer.source.SourceNode;
import org.nuxeo.runtime.api.Framework;

public class CaseManagementCaseImporterDocumentsFactory extends
        CaseManagementCaseItemDocumentFactory {

    private CaseManagementXMLCaseReader xmlCaseReader;

    private CaseManagementDocumentTypeService cmTypeService;

    CaseManagementCaseImporterDocumentsFactory(
            CaseManagementXMLCaseReader xmlCaseReader) {
        this.xmlCaseReader = xmlCaseReader;
    }

    @Override
    public DocumentModel createLeafNode(CoreSession session,
            DocumentModel parent, SourceNode node) throws Exception {
        if (node instanceof CaseItemSourceNode) {
            DocumentModel caseItemDoc = defaultCreateCaseItemDocType(session,
                    null, node);
            Case kase = parent.getAdapter(Case.class);
            CaseItem caseItem = getCaseDistributionService().addCaseItemToCase(
                    session, kase, caseItemDoc);
            return caseItem.getDocument();
        }
        return null;
    }

    // TODO change this to avoid fetching parent doc each time
    @Override
    public DocumentModel createFolderishNode(CoreSession session,
            DocumentModel parent, SourceNode node) throws Exception {
        if (node instanceof CaseSourceNode) {
            return defaultCreateCaseDoc(session, parent.getPathAsString(),
                    node, "Case");
        }
        return getCaseDistributionService().getParentDocumentForCase(session);
    }

    protected DocumentModel defaultCreateCaseItemDocType(CoreSession session,
            String parentPath, SourceNode node) throws Exception {
        return defaultCreateNodeDoc(session, parentPath, node,
                getCaseManagementDocumentTypeService().getCaseItemType());
    }

    protected DocumentModel defaultCreateCaseDoc(CoreSession session,
            String parentPath, SourceNode node, String docType)
            throws Exception {
        CaseSourceNode caseNode = (CaseSourceNode) node;
        ExportedDocument xdoc = xmlCaseReader.read(caseNode.getCaseElement());
        DocumentModel doc = session.createDocumentModel(parentPath, "Case",
                getCaseManagementDocumentTypeService().getCaseType());
        doc = session.createDocument(doc);
        DocumentWriter writer = new DocumentModelWriter(session,
                doc.getPathAsString(), 1);
        try {
            writer.write(xdoc);
        } catch (Exception e) {
        }
        return session.getDocument(doc.getRef());
    }

    private CaseManagementDocumentTypeService getCaseManagementDocumentTypeService()
            throws Exception {
        if (cmTypeService == null) {
            cmTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        }
        return cmTypeService;
    }
}
