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

import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.service.CaseManagementImporterService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Mariana Cedica
 */
public class CaseManagementImporterListener implements EventListener {

    CaseManagementImporterService caseManagementImporterService;

    public void handleEvent(Event event) throws ClientException {

        EventContext context = event.getContext();
        String category = (String) context.getProperty("category");
        if (!CaseManagementEventConstants.EVENT_CASE_MANAGEMENET_IMPORT_CATEGORY.equals(category)) {
            return;
        }
        if (CaseManagementEventConstants.EVENT_CASE_MANAGEMENET_IMPORT.equals(event.getName())) {
            // import docs
            getCaseManagementImporterService().importDocuments();
        }
    }

    private CaseManagementImporterService getCaseManagementImporterService() throws ClientException {
        try {
            if (caseManagementImporterService == null) {
                caseManagementImporterService = Framework.getService(CaseManagementImporterService.class);
            }
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return caseManagementImporterService;
    }

}
