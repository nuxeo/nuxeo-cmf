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
import org.dom4j.Element;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.io.ExportedDocument;

public interface CaseManagementXMLCaseReader {

    /**
     * Loads the list of cases contained into the imported file
     *
     * @throws ClientException
     * */
    List<Document> loadCases(File file) throws ClientException;

    ExportedDocument read(Element caseElement) throws ClientException;

    List<Element> loadCaseItems(Element caseElement);

    String getCaseItemPathFile(Element caseItemElement);

}
