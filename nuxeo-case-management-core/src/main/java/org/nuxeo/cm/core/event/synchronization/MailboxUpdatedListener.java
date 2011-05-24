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

import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY_ID;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_OWNER;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;

/**
 * Updates a mailbox setting users/groups on top on synchronisation
 * information.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxUpdatedListener extends AbstractSyncMailboxListener {

    private static final Log log = LogFactory.getLog(MailboxUpdatedListener.class);

    public void handleEvent(Event event) throws ClientException {
        DocumentModel sourceDoc = getMailboxDocument(event);
        try {
            Mailbox mailbox = sourceDoc.getAdapter(Mailbox.class);
            beforeMailboxUpdate(mailbox, event);
            List<String> newUsers = new LinkedList<String>();
            List<String> newGroups = new LinkedList<String>();
            Map<String, Serializable> properties = event.getContext().getProperties();
            String mailboxTitle = (String) properties.get(EVENT_CONTEXT_MAILBOX_TITLE);
            String owner = (String) properties.get(EVENT_CONTEXT_MAILBOX_OWNER);
            boolean isPersonal = isMailboxPersonal(event);
            if (owner != null && !"".equals(owner)) {
                newUsers.add(owner);
                if (isPersonal) {
                    mailbox.setOwner(owner);
                }
            }
            String entryId = (String) properties.get(EVENT_CONTEXT_MAILBOX_ENTRY_ID);
            if (!isPersonal) {
                newGroups.add(entryId);
            }
            mailbox.setTitle(mailboxTitle);
            boolean doMerge = isGroupUpdatePolicy(event,
                    MailboxConstants.updatePolicy.merge);
            boolean doOverride = isGroupUpdatePolicy(event,
                    MailboxConstants.updatePolicy.override);
            if (doMerge) {
                if (isPersonal && !newUsers.isEmpty()) {
                    List<String> users = mailbox.getUsers();
                    newUsers.addAll(users);
                    mailbox.setUsers(newUsers);
                } else if (!newGroups.isEmpty()) {
                    List<String> groups = mailbox.getGroups();
                    newGroups.addAll(groups);
                    mailbox.setGroups(newGroups);
                }
            } else if (doOverride) {
                if (isPersonal && !newUsers.isEmpty()) {
                    mailbox.setUsers(newUsers);
                } else if (!newGroups.isEmpty()) {
                    mailbox.setGroups(newGroups);
                }
            } else {
                log.debug("No users or groups were changed on mailbox, unknown update policy: "
                        + getGroupUpdatePolicy(event));
            }
            beforeMailboxSave(mailbox, event);
            CoreSession coreSession = event.getContext().getCoreSession();
            mailbox.save(coreSession);
        } catch (Exception e) {
            throw new CaseManagementException("Error during mailboxes update",
                    e);
        }
    }

    /**
     * Hook method to fill additional info on mailbox, or override other info,
     * before performing default updates
     */
    protected void beforeMailboxUpdate(Mailbox mailbox, Event event)
            throws ClientException {
        // do nothing
    }

    /**
     * Hook method to fill additional info on mailbox, or override other info,
     * after performing default updates
     */
    protected void beforeMailboxSave(Mailbox mailbox, Event event)
            throws ClientException {
        // do nothing
    }

}
