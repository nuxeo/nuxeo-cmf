/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.cm.core.event.synchronization;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Puts a mailbox in deleted state.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxDeletedListener extends AbstractSyncMailboxListener {

    private static final Log log = LogFactory.getLog(MailboxDeletedListener.class);

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docEventContext;
        if (event.getContext() instanceof DocumentEventContext) {
            docEventContext = (DocumentEventContext) event.getContext();
        } else {
            // can't get associated Document.
            throw new ClientException("Could not get Document from event");
        }
        Map<String, Serializable> properties = docEventContext.getProperties();
        Calendar synchronizeDate = (Calendar) properties.get(MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE);
        CoreSession session = docEventContext.getCoreSession();
        DocumentModel sourceDoc = docEventContext.getSourceDocument();
        deleteDoc(sourceDoc, synchronizeDate, session);
    }

    private void deleteDoc(DocumentModel doc, Calendar synchronizeDate, CoreSession session) throws ClientException {
        if (doc.getAllowedStateTransitions().contains(CaseLifeCycleConstants.TRANSITION_DELETE)) {
            doc.followTransition(CaseLifeCycleConstants.TRANSITION_DELETE);
            log.debug("Deleted " + doc.getName());
            Mailbox cf = doc.getAdapter(Mailbox.class);
            cf.setLastSyncUpdate(synchronizeDate);
            cf.save(session);
        }
    }

}
