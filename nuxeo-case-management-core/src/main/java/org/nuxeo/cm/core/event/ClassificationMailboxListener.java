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

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Listener for mailbox events that creates associated ClassificationRoot when mailbox is created.
 *
 * @author Laurent Doguin
 */
public class ClassificationMailboxListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }

        DocumentModel doc = docCtx.getSourceDocument();
        if (!doc.hasFacet(CaseConstants.MAILBOX_FACET)) {
            return;
        }
        CreateMailboxFilingRootUnrestricted filingRootCreator = new CreateMailboxFilingRootUnrestricted(
                docCtx.getCoreSession(), doc);
        filingRootCreator.runUnrestricted();
    }
}
