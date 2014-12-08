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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.nuxeo.cm.distribution.CMFDistributionInfo;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.service.caseimporter.AbstractXMLCaseReader;
import org.nuxeo.ecm.core.api.ClientException;

public class DefaultXMLCaseReader extends AbstractXMLCaseReader {

    public static final Log log = LogFactory.getLog(DefaultXMLCaseReader.class);

    public static final String ALL_CASES_TAG = "cases";

    public static final String CASE_TAG = "document";

    public static final String CASE_ITEMS = "caseItems";

    public static final String CASE_ITEM_DOCUMENT_PATH = "path";

    public static final String CASE_RECIPIENTS_TAG = "recipients";

    public static final String CASE_RECIPIENTS_ACTION = "action";

    public static final String CASE_RECIPIENTS_INFORMATION = "information";

    public static final String CASE_RECIPIENTS_MAILBOX_TAG = "mailbox";

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
            log.error("Failed to read document from file " + file.getAbsolutePath());
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
        XPath xpathSelector = DocumentHelper.createXPath("/" + ALL_CASES_TAG + "/" + CASE_TAG);
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
        return caseElement.element(CASE_ITEMS).elements(CASE_ITEM_DOCUMENT_PATH);
    }

    @Override
    public String getCaseItemPathFile(Element caseItemElement) {
        return (String) caseItemElement.getData();
    }

    @Override
    public DistributionInfo getDistributionInfo(Element caseElement) {
        List<String> actionMailboxes = new ArrayList<String>();
        List<String> informationMailboxes = new ArrayList<String>();

        CMFDistributionInfo distributionInfo = new CMFDistributionInfo();
        Element allRecipients = caseElement.element(CASE_RECIPIENTS_TAG);
        // TODO : better error handling
        if (allRecipients != null) {

            List<Element> actionMailboxesElements = allRecipients.element(CASE_RECIPIENTS_ACTION).elements(
                    CASE_RECIPIENTS_MAILBOX_TAG);
            for (Element element : actionMailboxesElements) {
                actionMailboxes.add((String) element.getData());
            }

            List<Element> informationMailboxesElements = caseElement.element(CASE_RECIPIENTS_TAG).element(
                    CASE_RECIPIENTS_INFORMATION).elements(CASE_RECIPIENTS_MAILBOX_TAG);

            for (Element element : informationMailboxesElements) {
                informationMailboxes.add((String) element.getData());
            }
        }

        distributionInfo.setForActionMailboxes(actionMailboxes);
        distributionInfo.setForInformationMailboxes(informationMailboxes);
        return distributionInfo;

    }
}