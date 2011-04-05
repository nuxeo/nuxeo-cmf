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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.nuxeo.cm.service.caseimporter.CaseManagementXMLCaseReader;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;

public class DefaultXMLCaseReader implements CaseManagementXMLCaseReader {

    public static final Log log = LogFactory.getLog(DefaultXMLCaseReader.class);

    public static final String CASE_TAG = "document";

    public static final String CASE_ITEMS = "caseItems";

    @Override
    public List<Document> loadCases(File file) throws ClientException {
        return readDomDoc(loadXML(file));
    }

    protected Document loadXML(File file) throws ClientException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            return new SAXReader().read(in);
        } catch (Exception e) {
            log.error("Failed to read document from file "
                    + file.getAbsolutePath());
            throw new ClientException();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    in = null;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Document> readDomDoc(Document doc) throws ClientException {
        XPath xpathSelector = DocumentHelper.createXPath("/" + "document");
        List<Element> allCases = xpathSelector.selectNodes(doc);
        List<Document> caseReaders = new ArrayList<Document>();
        for (Element element : allCases) {
            caseReaders.add(extractEntireCase(element));
        }
        return caseReaders;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Element> loadCaseItems(Element caseElement) {
        return caseElement.element("caseItems").elements("path");
    }

    @Override
    public String getCaseItemPathFile(Element caseItemElement) {
        return (String) caseItemElement.getData();

    }

    public Document extractEntireCase(Element caseElement) {
        Element freeElem = (Element) caseElement.detach();
        Document document = DocumentHelper.createDocument(freeElem);
        return document;
    }

    public ExportedDocument read(Element caseDocument) throws ClientException {
        ExportedDocument xdoc = new ExportedDocumentImpl();
        xdoc.setDocument(extractEntireCase(caseDocument));
        String envelopeId = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        xdoc.setId("ImportedCase_" + envelopeId);
        return xdoc;
    }
}