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
package org.nuxeo.cm.core.service.caseimporter.sourcenodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.nuxeo.cm.service.caseimporter.CaseManagementXMLCaseReader;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.importer.source.FileSourceNode;
import org.nuxeo.ecm.platform.importer.source.SourceNode;

/**
 * The file is actually the file containing the case definitions
 * */
public class CaseManagementSourceNode extends FileSourceNode {

    public static final Log log = LogFactory.getLog(CaseManagementSourceNode.class);

    private CaseManagementXMLCaseReader xmlCaseReader;

    private List<Document> allCases = new ArrayList<Document>();

    public CaseManagementSourceNode(File child,
            CaseManagementXMLCaseReader xmlCaseReader) {
        super(child);
        this.xmlCaseReader = xmlCaseReader;
    }

    @Override
    public List<SourceNode> getChildren() {
        List<SourceNode> cases = new ArrayList<SourceNode>();
        try {
            allCases = xmlCaseReader.loadCases(file);
        } catch (ClientException e) {
            log.error("Can not fetch cases");
        }
        for (Document el : allCases) {
            cases.add(new CaseSourceNode(el.getRootElement(), xmlCaseReader,
                    file.getPath()));
        }
        return cases;
    }

    @Override
    public boolean isFolderish() {
        return getChildren().size() > 0;
    }

}