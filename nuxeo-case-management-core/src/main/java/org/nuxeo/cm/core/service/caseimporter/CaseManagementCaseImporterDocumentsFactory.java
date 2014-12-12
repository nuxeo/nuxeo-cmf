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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseItemSourceNode;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseSourceNode;
import org.nuxeo.cm.core.service.importer.CaseManagementCaseItemDocumentFactory;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.ecm.platform.importer.source.SourceNode;
import org.nuxeo.runtime.api.Framework;

public class CaseManagementCaseImporterDocumentsFactory extends CaseManagementCaseItemDocumentFactory {

    private static final Log log = LogFactory.getLog(CaseManagementCaseImporterDocumentsFactory.class);

    private CaseManagementDocumentTypeService cmTypeService;

    @Override
    public DocumentModel createLeafNode(CoreSession session, DocumentModel parent, SourceNode node) {
        if (node instanceof CaseItemSourceNode) {
            DocumentModel caseItemDoc = defaultCreateCaseItemDocType(session, null, node);
            Case kase = parent.getAdapter(Case.class);
            CaseItem caseItem = getCaseDistributionService().addCaseItemToCase(session, kase, caseItemDoc);
            return caseItem.getDocument();
        }
        return null;
    }

    // TODO change this to avoid fetching parent doc each time
    @Override
    public DocumentModel createFolderishNode(CoreSession session, DocumentModel parent, SourceNode node) {
        if (node instanceof CaseSourceNode) {
            return defaultCreateCaseDoc(session, parent.getPathAsString(), node, "Case");
        }
        return getCaseDistributionService().getParentDocumentForCase(session);
    }

    protected DocumentModel defaultCreateCaseItemDocType(CoreSession session, String parentPath, SourceNode node) {
        return defaultCreateNodeDoc(session, parentPath, node, getCaseManagementDocumentTypeService().getCaseItemType());
    }

    protected DocumentModel defaultCreateCaseDoc(CoreSession session, String parentPath, SourceNode node, String docType) {
        CaseSourceNode caseNode = (CaseSourceNode) node;
        ExportedDocument xdoc = exportDocumentFromNode(caseNode);
        DocumentModel doc = session.createDocumentModel(parentPath, "Case",
                getCaseManagementDocumentTypeService().getCaseType());
        doc = session.createDocument(doc);
        DocumentWriter writer = new DocumentModelWriter(session, doc.getPathAsString(), 1);
        try {
            writer.write(xdoc);
        } catch (IOException e) {
            log.error("Failed to read ExportedDocument", e);
        }
        doc = session.getDocument(doc.getRef());
        notifyCaseImported(session, doc, node);
        return doc;
    }

    protected ExportedDocument exportDocumentFromNode(CaseSourceNode node) throws ClientException {
        ExportedDocument xdoc = new ExportedDocumentImpl();
        xdoc.setDocument(node.getCaseDocument());
        String envelopeId = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        xdoc.setId("ImportedCase_" + envelopeId);
        return xdoc;
    }

    private CaseManagementDocumentTypeService getCaseManagementDocumentTypeService() {
        if (cmTypeService == null) {
            cmTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        }
        return cmTypeService;
    }

}