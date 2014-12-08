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
package org.nuxeo.cm.core.event;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.service.CaseManagementImporterService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;

/**
 * When a case Item was created by import, rename the original doc so that it won't be imported again
 *
 * @author Mariana Cedica
 */
public class CaseManagementCaseImporterListener implements EventListener {

    CaseManagementImporterService caseManagementImporterService;

    public void handleEvent(Event event) throws ClientException {

        EventContext context = event.getContext();
        String category = (String) context.getProperty("category");
        if (!CaseManagementEventConstants.EVENT_CASE_MANAGEMENET_IMPORT_CATEGORY.equals(category)) {
            return;
        }
        if (CaseManagementEventConstants.EVENT_CASE_MANAGEMENET_CASE_IMPORT.equals(event.getName())) {
            Map<String, Serializable> properties = event.getContext().getProperties();
            String sourcePath = (String) properties.get(CaseManagementEventConstants.EVENT_CASE_MANAGEMENT_CASE_ITEM_SOURCE_PATH);
            if (sourcePath == null) {
                // do nothing, source path not valid
                return;
            }
            renameFile(sourcePath);
        }
    }

    private void renameFile(String filePath) {
        File origFile = new File(filePath);
        origFile.renameTo(new File(filePath.replaceAll(origFile.getName(), CaseConstants.DOCUMENT_IMPORTED_PREFIX
                + origFile.getName())));
    }

}
