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
package org.nuxeo.cm.core.service.importer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.CaseManagementImporterService;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * CaseManagementImporter service for importing case in a specified Mailbox
 *
 * @author Mariana Cedica
 */
public class CaseManagementImporterServiceImpl extends DefaultComponent
        implements CaseManagementImporterService {

    private static final Log log = LogFactory.getLog(CaseManagementImporterServiceImpl.class);

    private static final long serialVersionUID = 4984067871511405259L;

    CaseManagementImporterDescriptor importInfo;

    private String destionationMailboxPath;

    private String noImportingThreads;

    private Class<CaseManagementCaseItemDocumentFactory> importerDocumentModelfactoryClass;

    private String folderPath;

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {

        importInfo = (CaseManagementImporterDescriptor) contribution;

        if (importInfo.destionationMailboxPath != null) {
            destionationMailboxPath = importInfo.destionationMailboxPath;
        }

        if (importInfo.noImportingThreads != null) {
            noImportingThreads = importInfo.noImportingThreads;
        }

        if (importInfo.importerDocumentModelfactoryClass != null) {
            importerDocumentModelfactoryClass = importInfo.importerDocumentModelfactoryClass;
        }

        if (importInfo.folderPath != null) {
            folderPath = importInfo.folderPath;
        }

    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        destionationMailboxPath = null;
        noImportingThreads = null;
        importerDocumentModelfactoryClass = null;
        importInfo = null;
    }

    public void importDocuments() {
        // check if any importers are configured
        if (!importerConfigured()) {
            log.warn("No there is no importer configured!");
            return;
        }
        new CaseManagementImporter(destionationMailboxPath, noImportingThreads,
                folderPath, getImporterDocumentModelFactory()).importDocuments();
    }

    public final CaseManagementCaseItemDocumentFactory getImporterDocumentModelFactory() {
        try {
            return importerDocumentModelfactoryClass.newInstance();
        } catch (InstantiationException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    private boolean importerConfigured() {
        return importInfo != null;
    }

}
