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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.service.caseimporter.AbstractXMLCaseReader;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.importer.source.SourceNode;

public class CaseSourceNode implements SourceNode {

    private Element caseDocument;

    private AbstractXMLCaseReader xmlReader;

    private String rootPath;

    CaseSourceNode(Element caseDocument, AbstractXMLCaseReader xmlReader, String rootPath) {
        this.caseDocument = caseDocument;
        this.xmlReader = xmlReader;
        this.rootPath = rootPath;
    }

    @Override
    public BlobHolder getBlobHolder() {
        return null;
    }

    @Override
    public List<SourceNode> getChildren() {
        List<SourceNode> itemNodes = new ArrayList<SourceNode>();
        List<Element> docs = xmlReader.loadCaseItems(caseDocument);
        for (Element el : docs) {
            itemNodes.add(new CaseItemSourceNode(el, xmlReader, rootPath));
        }
        return itemNodes;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getSourcePath() {
        return null;
    }

    @Override
    public boolean isFolderish() {
        return getChildren().size() > 0;
    }

    public Element getCaseElement() {
        return caseDocument;
    }

    public Document getCaseDocument() {
        return xmlReader.extractEntireCase(caseDocument);
    }

    public DistributionInfo getDistributionInfo() {
        return xmlReader.getDistributionInfo(caseDocument);
    }
}
