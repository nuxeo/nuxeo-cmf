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
package org.nuxeo.cm.service.synchronization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxSyncTestListener implements EventListener {

    public static List<String> mbCreatedForGroup = new ArrayList<String>();

    public static List<String> mbUpdatedForGroup = new ArrayList<String>();

    public static List<String> mbDeletedForGroup = new ArrayList<String>();

    public static List<String> mbCreatedForUser = new ArrayList<String>();

    public static List<String> mbUpdatedForUser = new ArrayList<String>();

    public static List<String> mbDeletedForUser = new ArrayList<String>();

    public void handleEvent(Event event) throws ClientException {
        String eventName = event.getName();
        String failMessage = "Error handling event " + eventName + ": ";
        String mbTitle = (String) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE);
        assertFalse(failMessage + "Empty mailbox title",
                StringUtils.isEmpty(mbTitle));
        Calendar synchronizerDate = (Calendar) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE);
        assertFalse(
                failMessage + "Empty synchronizer date for mailbox with title "
                        + mbTitle,
                synchronizerDate == null
                        || StringUtils.isEmpty(synchronizerDate.toString()));
        String synchronizerId = (String) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID);
        assertFalse(failMessage
                + "Empty synchronizer id for mailbox with title " + mbTitle,
                StringUtils.isEmpty(synchronizerId));
        DocumentModel mbDoc = ((DocumentEventContext) event.getContext()).getSourceDocument();
        assertNotNull(failMessage
                + "Null document for mailbox with title " + mbTitle, mbDoc);
        String mbPath = mbDoc.getPathAsString();
        assertFalse(failMessage
                + "Empty path for mailbox document with title " + mbTitle,
                StringUtils.isEmpty(mbPath));
        if ("userDirectory".equals(event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (MailboxSynchronizationConstants.EventNames.onMailboxCreated.equals(eventName)) {
                mbCreatedForUser.add(mbPath);
            } else if (MailboxSynchronizationConstants.EventNames.onMailboxDeleted.equals(eventName)) {
                mbDeletedForUser.add(mbPath);
            } else if (MailboxSynchronizationConstants.EventNames.onMailboxUpdated.equals(eventName)) {
                mbUpdatedForUser.add(mbPath);
            }
        } else if ("groupDirectory".equals(event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (MailboxSynchronizationConstants.EventNames.onMailboxCreated.equals(eventName)) {
                mbCreatedForGroup.add(mbPath);
            } else if (MailboxSynchronizationConstants.EventNames.onMailboxDeleted.equals(eventName)) {
                mbDeletedForGroup.add(mbPath);
            } else if (MailboxSynchronizationConstants.EventNames.onMailboxUpdated.equals(eventName)) {
                mbUpdatedForGroup.add(mbPath);
            }
        }
    }

    public static void reset() {
        mbCreatedForGroup.clear();
        mbUpdatedForGroup.clear();
        mbDeletedForGroup.clear();
        mbCreatedForUser.clear();
        mbUpdatedForUser.clear();
        mbDeletedForUser.clear();
    }

}
