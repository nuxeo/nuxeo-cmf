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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.core.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.CaseManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.SimplePrincipal;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.runtime.api.Framework;


/**
 * Listen to loginSuccess event and create the personal mailbox if needed
 *
 * @author Nicolas Ulrich
 *
 */
public class SuccessLoginListener implements EventListener {

    private static final Log log = LogFactory.getLog(SuccessLoginListener.class);

    public void handleEvent(Event event) throws ClientException {
        try {
            CaseManagementService nxcService = Framework.getService(CaseManagementService.class);
            if (nxcService == null) {
                throw new CaseManagementException(
                        "CorrespondenceService not found.");
            }

            SimplePrincipal principal = (SimplePrincipal) event.getContext().getPrincipal();
            if (!nxcService.hasUserPersonalCaseFolder(getCoreSession(),
                    principal.getName())) {
                nxcService.createPersonalCaseFolders(getCoreSession(),
                        principal.getName());
            }

        } catch (Exception e) {
            log.error("Error during personal mailbox creation.", e);
        }

    }

    protected CoreSession getCoreSession() throws Exception {
        RepositoryManager mgr = Framework.getService(RepositoryManager.class);
        if (mgr == null) {
            throw new ClientException("Cannot find RepositoryManager");
        }

        Repository repo = mgr.getDefaultRepository();
        return repo.open();
    }

}
