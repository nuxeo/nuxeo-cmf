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

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.core.service.SetMailboxAclUnrestricted;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Listener for mailbox events that sets user/groups rights when mailbox is created/edited.
 *
 * @author Anahide Tchertchian
 */
public class UpdateMailboxRightsListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }
        // set all rights to mailbox users
        DocumentModel doc = docCtx.getSourceDocument();
        if (!doc.hasFacet(CaseConstants.MAILBOX_FACET)) {
            return;
        }
        CoreSession session = docCtx.getCoreSession();
        SetMailboxAclUnrestricted sessionCreator = new SetMailboxAclUnrestricted(session, doc.getRef());
        sessionCreator.runUnrestricted();
    }

}
