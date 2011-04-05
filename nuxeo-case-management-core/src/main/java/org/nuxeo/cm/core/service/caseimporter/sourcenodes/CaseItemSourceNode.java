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

import org.dom4j.Element;
import org.nuxeo.cm.service.caseimporter.AbstractXMLCaseReader;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.platform.importer.source.FileSourceNode;

public class CaseItemSourceNode extends FileSourceNode {

    private Element caseItemElement;

    public CaseItemSourceNode(Element caseItemElement,
            AbstractXMLCaseReader xmlReader, String rootPath) {
        super(new File(FileUtils.getParentPath(rootPath) + File.separator
                + xmlReader.getCaseItemPathFile(caseItemElement)));
        this.caseItemElement = caseItemElement;
    }

    public Element getCaseItemElement() {
        return caseItemElement;
    }
}