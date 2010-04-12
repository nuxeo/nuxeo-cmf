/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.core.event;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mailbox.CaseFolder;
import org.nuxeo.cm.mailbox.CaseFolderConstants;
import org.nuxeo.cm.service.CaseManagementService;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;


/**
 * Listener for mailbox creation events that sets the mailbox id according to
 * the mailbox type, and possibly owner in the case of a personal mailbox.
 * <p>
 * If mailbox id is already set (for instance when creating personal mailbox
 * using the correspondence service), nothing is done.
 *
 * @author Anahide Tchertchian
 */
public class CreateCaseFolderIdListener implements EventListener {

    private static final Log log = LogFactory.getLog(CreateCaseFolderIdListener.class);

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }

        DocumentModel doc = docCtx.getSourceDocument();
        CaseFolder mb = doc.getAdapter(CaseFolder.class);
        if (mb == null || mb.getId() != null) {
            return;
        }

        try {
            CaseManagementService correspService = Framework.getService(CaseManagementService.class);
            setIdForMailbox(correspService, mb);
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected void setIdForMailbox(CaseManagementService correspService,
            CaseFolder mb) {
        if (correspService == null) {
            log.error("Cannot set mailbox id: correspondence service is null");
            return;
        }
        // set the mailbox id
        String id = null;
        if (CaseFolderConstants.type.personal.name().equals(mb.getType())) {
            String owner = mb.getOwner();
            if (owner == null) {
                log.warn("Creating a personal mailbox without owner");
            } else {
                id = correspService.getUserPersonalCaseFolderId(owner);
            }
        }
        if (id == null) {
            String title = mb.getTitle();
            if (title != null) {
                id = IdUtils.generateId(mb.getTitle());
                if (correspService.hasCaseFolder(id)) {
                    // add timestamp
                    id += "_" + String.valueOf(new Date().getTime());
                }
            } else {
                id = String.valueOf(new Date().getTime());
            }
        }
        mb.setId(id);
    }

}
