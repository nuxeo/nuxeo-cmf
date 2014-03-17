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

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.SimplePrincipal;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Listen to loginSuccess event and create the personal mailbox if needed
 *
 * @author Nicolas Ulrich
 */
public class SuccessLoginListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        MailboxManagementService nxcService = Framework.getLocalService(MailboxManagementService.class);
        if (nxcService == null) {
            throw new CaseManagementException(
                    "CorrespondenceService not found.");
        }
        String principalName = event.getContext().getPrincipal().getName();
        boolean isNewTransactionStarted = false;
        if (!TransactionHelper.isTransactionActive()) {
            isNewTransactionStarted = TransactionHelper.startTransaction();
        }
        try {
            try (CoreSession session = CoreInstance.openCoreSession(null)) {
                if (!nxcService.hasUserPersonalMailbox(session, principalName)) {
                    nxcService.createPersonalMailboxes(session, principalName);
                }
            }
        } finally {
            if (isNewTransactionStarted) {
                TransactionHelper.commitOrRollbackTransaction();
            }
        }
    }

}
