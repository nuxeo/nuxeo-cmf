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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseManagementSourceNode;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.caseimporter.AbstractXMLCaseReader;
import org.nuxeo.ecm.platform.importer.base.GenericMultiThreadedImporter;
import org.nuxeo.ecm.platform.importer.executor.AbstractImporterExecutor;
import org.nuxeo.ecm.platform.importer.source.FileSourceNode;
import org.nuxeo.ecm.platform.importer.source.SourceNode;

public class CaseImporter extends AbstractImporterExecutor {

    private static final Log log = LogFactory.getLog(CaseImporter.class);

    private int noImportingThreads;

    private AbstractXMLCaseReader xmlCaseReader;

    private CaseManagementCaseImporterDocumentsFactory caseManagementCaseImporterDocumentsFactory;

    private CaseManagementImporterThreadingPolicy caseManagementImporterThreadingPolicy;

    private CaseImporterThreadedTask caseImporterThreadedTask;

    public CaseImporter(int noImportingThreads, AbstractXMLCaseReader xmlCaseReader) {
        this.noImportingThreads = noImportingThreads;
        this.xmlCaseReader = xmlCaseReader;
    }

    @Override
    protected Log getJavaLogger() {
        return log;
    }

    public void importDocuments(String sourcePath) {
        SourceNode src = new FileSourceNode(sourcePath) {
            @Override
            public List<SourceNode> getChildren() {
                List<SourceNode> children = new ArrayList<SourceNode>();
                for (File child : file.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                })) {
                    children.add(new CaseManagementSourceNode(child, xmlCaseReader));
                }
                return children;
            }
        };
        GenericMultiThreadedImporter importer;
        try {
            importer = new GenericMultiThreadedImporter(src, CaseConstants.CASE_ROOT_DOCUMENT_PATH, true,
                    noImportingThreads, new Integer(noImportingThreads), getLogger());
            if (caseManagementCaseImporterDocumentsFactory == null) {
                setCaseManagementCaseImporterDocumentsFactory(new CaseManagementCaseImporterDocumentsFactory());
            }
            if (caseManagementImporterThreadingPolicy == null) {
                setCaseManagementImporterThreadingPolicy(new CaseManagementImporterThreadingPolicy());
            }
            if (caseImporterThreadedTask == null) {
                setCaseImporterThreadedTask(new CaseImporterThreadedTask(null));
            }

            importer.setFactory(caseManagementCaseImporterDocumentsFactory);
            importer.setThreadPolicy(caseManagementImporterThreadingPolicy);
            importer.setRootImportTask(caseImporterThreadedTask);
            doRun(importer, Boolean.TRUE);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public CaseManagementCaseImporterDocumentsFactory getCaseManagementCaseImporterDocumentsFactory() {
        return caseManagementCaseImporterDocumentsFactory;
    }

    public void setCaseManagementCaseImporterDocumentsFactory(
            CaseManagementCaseImporterDocumentsFactory caseManagementCaseImporterDocumentsFactory) {
        this.caseManagementCaseImporterDocumentsFactory = caseManagementCaseImporterDocumentsFactory;
    }

    public CaseManagementImporterThreadingPolicy getCaseManagementImporterThreadingPolicy() {
        return caseManagementImporterThreadingPolicy;
    }

    public void setCaseManagementImporterThreadingPolicy(
            CaseManagementImporterThreadingPolicy caseManagementImporterThreadingPolicy) {
        this.caseManagementImporterThreadingPolicy = caseManagementImporterThreadingPolicy;
    }

    public CaseImporterThreadedTask getCaseImporterThreadedTask() {
        return caseImporterThreadedTask;
    }

    public void setCaseImporterThreadedTask(CaseImporterThreadedTask caseImporterThreadedTask) {
        this.caseImporterThreadedTask = caseImporterThreadedTask;
    }
}