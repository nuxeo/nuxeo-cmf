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

import org.nuxeo.cm.service.caseimporter.AbstractXMLCaseReader;
import org.nuxeo.cm.service.caseimporter.CaseManagementCaseImporterService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class CaseManagementCaseImporterServiceImpl extends DefaultComponent implements
        CaseManagementCaseImporterService {

    private static final long serialVersionUID = 1L;

    private AbstractXMLCaseReader xmlCaseReader;

    protected int noImportingThreads = 5;

    @Override
    public void importCases(String sourcePath) throws ClientException {
        CaseImporter importer = new CaseImporter(noImportingThreads, xmlCaseReader);
        try {
            importer.importDocuments(sourcePath);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if ("importer".equals(extensionPoint)) {
            CaseManagementCaseImporterDescriptor caseImporterDescriptor = (CaseManagementCaseImporterDescriptor) contribution;
            if (caseImporterDescriptor.caseReader != null) {
                try {
                    xmlCaseReader = caseImporterDescriptor.caseReader.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
            if (caseImporterDescriptor.noImportingThreads != null) {
                noImportingThreads = Integer.parseInt(caseImporterDescriptor.noImportingThreads);
            }
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        xmlCaseReader = null;
    }
}