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

import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY_ID;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_OWNER;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TYPE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.core.service.DefaultMailboxCreator;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.synchronisedState;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.event.Event;

/**
 * Creates a mailbox filling id, title, title, and adding default user (if
 * mailbox is personal) or group (otherwise) on top of synchronization
 * information.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxCreatedListener extends AbstractSyncMailboxListener {

    private static final Log log = LogFactory.getLog(MailboxCreatedListener.class);

    // TODO: factor out personal mailbox creation, as same logic needs to be
    // implemented in MailboxCreator contribution and here
    @Override
    public void handleEvent(Event event) throws ClientException {
        try {
            Map<String, Serializable> properties = event.getContext().getProperties();
            String mailboxTitle = (String) properties.get(EVENT_CONTEXT_MAILBOX_TITLE);
            String directoryName = (String) properties.get(EVENT_CONTEXT_DIRECTORY_NAME);
            String parentSynchronizerId = (String) properties.get(EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID);
            String synchronizerId = (String) properties.get(EVENT_CONTEXT_SYNCHRONIZER_ID);
            String owner = (String) properties.get(EVENT_CONTEXT_MAILBOX_OWNER);
            String type = (String) properties.get(EVENT_CONTEXT_MAILBOX_TYPE);
            String entryId = (String) properties.get(EVENT_CONTEXT_MAILBOX_ENTRY_ID);
            DocumentModel entry = (DocumentModel) properties.get(EVENT_CONTEXT_MAILBOX_ENTRY);
            Calendar synchronizeDate = (Calendar) properties.get(EVENT_CONTEXT_SYNCHRONIZED_DATE);

            CoreSession session = event.getContext().getCoreSession();
            String id = null;
            boolean isPersonal = isMailboxPersonal(event);
            boolean isGeneric = isMailboxGeneric(event);
            if (isPersonal) {
                DefaultMailboxCreator mbCreator = new DefaultMailboxCreator();
                id = mbCreator.getPersonalMailboxId(entry);
            } else if (isGeneric) {
                id = IdUtils.generateId(NuxeoGroup.PREFIX + entryId, "-", true,
                        24);
            } else {
                log.debug("No id generation for unknown mailbox type: "
                        + getMailboxType(event));
            }
            // Create the personal mailbox for the user
            DocumentModel mailboxModel = getMailboxDocument(event);
            Mailbox mailbox = mailboxModel.getAdapter(Mailbox.class);
            // Set mailbox properties
            mailbox.setSynchronizeState(synchronisedState.synchronised.toString());
            if (synchronizerId != null && !"".equals(synchronizerId)) {
                mailbox.setSynchronizerId(synchronizerId);
            }
            if (synchronizeDate != null) {
                mailbox.setLastSyncUpdate(synchronizeDate);
            }
            if (mailboxTitle != null && !"".equals(mailboxTitle)) {
                mailbox.setTitle(mailboxTitle);
            }
            if (directoryName != null && !"".equals(directoryName)) {
                mailbox.setOrigin(directoryName);
            }
            if (owner != null && !"".equals(owner)) {
                mailbox.setOwner(owner);
            }
            if (isPersonal) {
                List<String> users = new LinkedList<String>();
                users.add(entryId);
                mailbox.setUsers(users);
            } else {
                List<String> groups = new LinkedList<String>();
                groups.add(entryId);
                mailbox.setGroups(groups);
            }
            mailbox.setType(type);

            // Set case creation profile
            List<String> list = new ArrayList<String>();
            list.add(MailboxConstants.MAILBOX_CASE_CREATION_PROFILE);
            mailbox.setProfiles(list);

            mailbox.setId(id);
            mailboxModel.setPathInfo(getMailboxParentPath(session,
                    parentSynchronizerId), getMailboxPathSegment(entry,
                    mailboxModel));

            // call hook
            beforeMailboxCreation(mailbox, event);

            mailboxModel = session.createDocument(mailboxModel);

            mailbox = mailboxModel.getAdapter(Mailbox.class);
            afterMailboxCreation(mailbox, event);
            // save because the mailbox will be queried just after in another
            // session
            mailbox.save(session);
        } catch (Exception e) {
            throw new CaseManagementException(
                    "Error during mailboxes creation", e);
        }

    }

    /**
     * Hook method to fill additional info on mailbox, or override other info
     */
    protected void beforeMailboxCreation(Mailbox mailbox, Event event)
            throws ClientException {
        // do nothing
    }

    /**
     * Hook method to override other info filled by
     * {@link #CoreSession.createDocument(DocumentModel)} method.
     */
    protected void afterMailboxCreation(Mailbox mailbox, Event event)
            throws ClientException {
        // do nothing
    }

    protected String getMailboxParentPath(CoreSession session,
            String parentSynchronizerId) throws ClientException {
        String searchQuery;
        // Take the first MailboxRoot when there is no parent.
        if (parentSynchronizerId == null || "".equals(parentSynchronizerId)) {
            searchQuery = String.format("SELECT * from %s",
                    MailboxConstants.MAILBOX_ROOT_DOCUMENT_TYPE);
        } else {
            searchQuery = String.format(
                    "SELECT * from Mailbox WHERE mlbx:synchronizerId= '%s'",
                    parentSynchronizerId);
        }
        DocumentModelList res = session.query(searchQuery);
        if (res == null || res.isEmpty()) {
            throw new CaseManagementException("Cannot find any mailbox folder");
        }
        return res.get(0).getPathAsString();
    }

    protected String getMailboxPathSegment(DocumentModel dirEntry,
            DocumentModel mailboxModel) throws ClientException {
        return DefaultMailboxCreator.getNewMailboxPathSegment(mailboxModel);
    }

}
