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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.core.event;

import org.nuxeo.cm.core.caselink.ValidateDueCaseLinkUnrestricted;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener that fetch all ActionCaseLink with automatic validation and date
 * passed and not done to validate them.
 *
 * @author Laurent Doguin
 */
public class AutomaticActionCaseLinkValidatorListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        CoreSession session = getCoreSession();
        ValidateDueCaseLinkUnrestricted runner = new ValidateDueCaseLinkUnrestricted(
                session);
        runner.runUnrestricted();
        closeCoreSession(session);
    }

    protected CoreSession getCoreSession() {

        RepositoryManager mgr = null;
        try {
            mgr = Framework.getService(RepositoryManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (mgr == null) {
            throw new CaseManagementRuntimeException(
                    "Cannot find RepostoryManager");
        }
        Repository repo = mgr.getDefaultRepository();

        CoreSession session;
        try {
            session = repo.open();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException("Cannot open CoreSession");
        }
        return session;
    }

    protected void closeCoreSession(CoreSession coreSession) {
        if (coreSession != null) {
            CoreInstance.getInstance().close(coreSession);
        }
    }

}
