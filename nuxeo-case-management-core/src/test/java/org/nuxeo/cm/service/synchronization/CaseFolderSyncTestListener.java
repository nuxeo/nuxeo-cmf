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
public class CaseFolderSyncTestListener implements EventListener {

    private static final Log log = LogFactory.getLog(CaseFolderSyncTestListener.class);

    public static int onCaseFolderCreatedForGroup = 0;

    public static int onCaseFolderUpdatedForGroup = 0;

    public static int onCaseFolderDeletedForGroup = 0;

    public static int onCaseFolderCreatedForUser = 0;

    public static int onCaseFolderUpdatedForUser = 0;

    public static int onCaseFolderDeletedForUser = 0;

    @SuppressWarnings("unchecked")
    public void handleEvent(Event event) throws ClientException {
        String caseFolderTitle = (String) event.getContext().getProperty(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TITLE);
        if (caseFolderTitle == null || "".equals(caseFolderTitle)) {
            log.error("CaseFolderTitle was not set");
            return;
        }
        Calendar synchronizerDate = (Calendar) event.getContext().getProperty(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE);
        if (synchronizerDate == null || "".equals(synchronizerDate)) {
            log.error("synchronizerDate was not set");
            return;
        }
        String synchronizerId = (String) event.getContext().getProperty(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID);
        if (synchronizerId == null || "".equals(synchronizerId)) {
            log.error("synchronizerId was not set");
            return;
        }
        if ("userDirectory".equals(event.getContext().getProperty(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderCreated.toString())) {
                onCaseFolderCreatedForUser++;
            } else if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderDeleted.toString())) {
                onCaseFolderDeletedForUser++;
            } else if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderUpdated.toString())) {
                onCaseFolderUpdatedForUser++;
            }
        } else if ("groupDirectory".equals(event.getContext().getProperty(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME))) {
            if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderCreated.toString())) {
                onCaseFolderCreatedForGroup++;
            } else if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderDeleted.toString())) {
                onCaseFolderDeletedForGroup++;
            } else if (event.getName().equals(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderUpdated.toString())) {
                onCaseFolderUpdatedForGroup++;
            }
        }
    }

    public static void resetCounter() {
        onCaseFolderCreatedForGroup = 0;
        onCaseFolderUpdatedForGroup = 0;
        onCaseFolderDeletedForGroup = 0;
        onCaseFolderCreatedForUser = 0;
        onCaseFolderUpdatedForUser = 0;
        onCaseFolderDeletedForUser = 0;
    }

}
