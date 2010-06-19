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

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxSyncTestListener implements EventListener {

    private static final Log log = LogFactory.getLog(MailboxSyncTestListener.class);

    public static int onMailboxCreatedForGroup = 0;

    public static int onMailboxUpdatedForGroup = 0;

    public static int onMailboxDeletedForGroup = 0;

    public static int onMailboxCreatedForUser = 0;

    public static int onMailboxUpdatedForUser = 0;

    public static int onMailboxDeletedForUser = 0;

    public void handleEvent(Event event) throws ClientException {
        String MailboxTitle = (String) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE);
        if (MailboxTitle == null || "".equals(MailboxTitle)) {
            log.error("MailboxTitle was not set");
            return;
        }
        Calendar synchronizerDate = (Calendar) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE);
        if (synchronizerDate == null || "".equals(synchronizerDate)) {
            log.error("synchronizerDate was not set");
            return;
        }
        String synchronizerId = (String) event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID);
        if (synchronizerId == null || "".equals(synchronizerId)) {
            log.error("synchronizerId was not set");
            return;
        }
        if ("userDirectory".equals(event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxCreated.toString())) {
                onMailboxCreatedForUser++;
            } else if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxDeleted.toString())) {
                onMailboxDeletedForUser++;
            } else if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxUpdated.toString())) {
                onMailboxUpdatedForUser++;
            }
        } else if ("groupDirectory".equals(event.getContext().getProperty(
                MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxCreated.toString())) {
                onMailboxCreatedForGroup++;
            } else if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxDeleted.toString())) {
                onMailboxDeletedForGroup++;
            } else if (event.getName().equals(
                    MailboxSynchronizationConstants.EventNames.onMailboxUpdated.toString())) {
                onMailboxUpdatedForGroup++;
            }
        }
    }

    public static void resetCounter() {
        onMailboxCreatedForGroup = 0;
        onMailboxUpdatedForGroup = 0;
        onMailboxDeletedForGroup = 0;
        onMailboxCreatedForUser = 0;
        onMailboxUpdatedForUser = 0;
        onMailboxDeletedForUser = 0;
    }

}
