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
package org.nuxeo.cm.service.caseimporter;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.ecm.core.api.ClientException;

/***
 * Parses xml file and returns corresponding dom4j elements for cases and
 * caseItems
 */
public abstract class AbstractXMLCaseReader {

    /**
     * Loads the list of cases contained into the imported file
     *
     * @throws ClientException
     * */
    public abstract List<Document> loadCases(File file) throws ClientException;

    /***
     * Loads the list of case items for a given caseElement
     *
     * @param caseElement
     * @return
     */
    public abstract List<Element> loadCaseItems(Element caseElement);

    /***
     * Gets the path of the file to be imported as a case Item
     *
     * @param caseItemElement
     * @return
     */
    public abstract String getCaseItemPathFile(Element caseItemElement);


    /**
     * Gets the recipients from the case for the distribution
     * @param caseElement
     * @return
     */
    public abstract DistributionInfo getDistributionInfo(Element caseElement);

    public Document extractEntireCase(Element caseElement) {
        Element freeElem = (Element) caseElement.detach();
        Document document = DocumentHelper.createDocument(freeElem);
        return document;
    }

}