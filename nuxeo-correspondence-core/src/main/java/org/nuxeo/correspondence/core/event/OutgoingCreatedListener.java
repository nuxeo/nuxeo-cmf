/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.correspondence.core.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * @author arussel
 */
public class OutgoingCreatedListener implements EventListener {
    public static final Log log = LogFactory.getLog(OutgoingCreatedListener.class);

    public void handleEvent(Event event) throws ClientException {
        if (!"documentCreated".equals(event.getName())) {
            log.warn("OutgoingCreatedListener called on a non documentCreated event: " + event);
            return;
        }
        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        }
        CoreSession session = docCtx.getCoreSession();
        DocumentModel dm = docCtx.getSourceDocument();

        // Check if the document model is an envelope
        if (!dm.hasFacet(MailConstants.MAIL_ENVELOPE_FACET)) {
            return;
        }
        Case env = dm.getAdapter(Case.class);
        if (env == null) {
            return;
        }
        CaseItem item = env.getFirstItem(session);
        if (item == null) {
            return;
        }
        DocumentModel doc = item.getDocument();
        doc.setPropertyValue(CorrespondenceConstants.OUT_SENDING_MAILBOX,
                docCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_MAILBOX_ID));
        doc.setPropertyValue(CorrespondenceConstants.OUT_SENDING_UNIT,
                docCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_AFFILIATED_MAILBOX_ID));
        session.saveDocument(doc);
    }

}
