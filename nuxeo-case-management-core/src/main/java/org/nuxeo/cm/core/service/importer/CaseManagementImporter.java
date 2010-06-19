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
 *    mcedica
 */
package org.nuxeo.cm.core.service.importer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.importer.base.GenericMultiThreadedImporter;
import org.nuxeo.ecm.platform.importer.executor.AbstractImporterExecutor;
import org.nuxeo.ecm.platform.importer.source.FileSourceNode;
import org.nuxeo.ecm.platform.importer.source.SourceNode;

/**
 * Implementation for creating caseItems in cases
 * 
 * @author Mariana Cedica
 */
public class CaseManagementImporter extends AbstractImporterExecutor {

    private static final Log log = LogFactory.getLog(CaseManagementImporter.class);

    private String destionationMailboxPath;

    private String noImportingThreads;

    private String folderPath;

    private CaseManagementCaseItemDocumentFactory cmCaseItemDocFactory;

    public CaseManagementImporter(String destionationMailboxPath,
            String noImportingThreads, String folderPath,
            CaseManagementCaseItemDocumentFactory factory) {
        this.destionationMailboxPath = destionationMailboxPath;
        this.noImportingThreads = noImportingThreads;
        this.folderPath = folderPath;
        this.cmCaseItemDocFactory = factory;
    }

    @Override
    protected Log getJavaLogger() {
        return log;
    }

    public void importDocuments() throws ClientException {
        SourceNode sourceNode = new FileSourceNode(folderPath);
        try {
            GenericMultiThreadedImporter importer = new GenericMultiThreadedImporter(
                    sourceNode, destionationMailboxPath, 50, new Integer(
                            noImportingThreads),getLogger());
            //TODO : bachSize?
            cmCaseItemDocFactory.setDestionationMailboxPath(destionationMailboxPath);
            importer.setFactory(cmCaseItemDocFactory);
            // TODO : add the type checker?
            doRun(importer, Boolean.TRUE);
        } catch (Exception e) {
            log.error(e);
            throw new ClientException(e);
        }
    }

}
