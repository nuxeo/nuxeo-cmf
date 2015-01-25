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
import java.util.List;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseSourceNode;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.importer.base.GenericMultiThreadedImporter;
import org.nuxeo.ecm.platform.importer.base.GenericThreadedImportTask;
import org.nuxeo.ecm.platform.importer.source.SourceNode;
import org.nuxeo.runtime.api.Framework;

/***
 * CaseManagement custom import task. Overrides methods for creating documents to handle commits When all the caseItems
 * are imported for a given thread, the case is distributed
 */
// TODO add and implement a rollback on error policy
public class CaseImporterThreadedTask extends GenericThreadedImportTask {

    CaseDistributionService distributionService;

    public CaseImporterThreadedTask(CoreSession session) {
        super(session);
    }

    @Override
    protected DocumentModel doCreateFolderishNode(DocumentModel parent, SourceNode node) {
        if (!shouldImportDocument(node)) {
            return null;
        }
        DocumentModel folder;
        try {
            folder = getFactory().createFolderishNode(session, parent, node);
        } catch (IOException e) {
            throw new NuxeoException("Failed to import node " + node.getName(), e);
        }
        if (folder != null) {
            String parentPath = (parent == null) ? "null" : parent.getPathAsString();
            fslog("Created Folder " + folder.getName() + " at " + parentPath, true);
        }
        return folder;

    }

    @Override
    protected DocumentModel doCreateLeafNode(DocumentModel parent, SourceNode node) throws IOException {
        if (!shouldImportDocument(node)) {
            return null;
        }

        DocumentModel leaf;
        try {
            leaf = getFactory().createLeafNode(session, parent, node);
        } catch (IOException e) {
            throw new NuxeoException("Failed to import node " + node.getName(), e);
        }
        if (leaf != null && node.getBlobHolder() != null) {
            long fileSize = node.getBlobHolder().getBlob().getLength();
            String fileName = node.getBlobHolder().getBlob().getFilename();
            if (fileSize > 0) {
                long kbSize = fileSize / 1024;
                String parentPath = (parent == null) ? "null" : parent.getPathAsString();
                fslog("Created doc " + leaf.getName() + " at " + parentPath + " with file " + fileName + " of size "
                        + kbSize + "KB", true);
            }

            uploadedKO += fileSize;
        }
        return leaf;
    }

    @Override
    protected void recursiveCreateDocumentFromNode(DocumentModel parent, SourceNode node) throws IOException {

        if (getFactory().isTargetDocumentModelFolderish(node)) {
            DocumentModel folder;
            Boolean newThread = false;
            if (skipContainerCreation) {
                folder = parent;
                skipContainerCreation = false;
                newThread = true;
            } else {
                folder = doCreateFolderishNode(parent, node);
                if (folder == null) {
                    return;
                }
            }
            List<SourceNode> nodes = node.getChildren();
            if (nodes != null && nodes.size() > 0) {
                // get a new TaskImporter if available to start
                // processing the sub-tree
                GenericThreadedImportTask task = null;
                if (!newThread) {
                    task = createNewTaskIfNeeded(folder, node);
                }
                if (task != null) {
                    commit(true);
                    GenericMultiThreadedImporter.getExecutor().execute(task);
                } else {
                    for (SourceNode child : nodes) {
                        recursiveCreateDocumentFromNode(folder, child);
                    }
                }
                if (node instanceof CaseSourceNode) {
                    distributeCase(folder, (CaseSourceNode) node);
                }
            }
        } else {
            doCreateLeafNode(parent, node);
        }
    }

    protected void distributeCase(DocumentModel caseDoc, CaseSourceNode node) {
        Case kase = caseDoc.getAdapter(Case.class);
        getCaseDistributionService().sendCase(session, "Import", kase, node.getDistributionInfo());
    }

    private CaseDistributionService getCaseDistributionService() {
        if (distributionService == null) {
            distributionService = Framework.getService(CaseDistributionService.class);
        }
        return distributionService;
    }
}
