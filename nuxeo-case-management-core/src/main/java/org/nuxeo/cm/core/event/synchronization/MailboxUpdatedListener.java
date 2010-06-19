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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class MailboxUpdatedListener extends AbstractSyncMailboxListener {

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docEventContext = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docEventContext = (DocumentEventContext) event.getContext();
        } else {
            // can't get associated Document.
            throw new ClientException("Could not get Document from event");
        }
        DocumentModel sourceDoc = docEventContext.getSourceDocument();
        if (sourceDoc == null) {

        }
        Mailbox mailboxToUpdate = sourceDoc.getAdapter(Mailbox.class);
        if (mailboxToUpdate == null) {

        }
        List<String> newUsers = new LinkedList<String>();
        List<String> newGroups = new LinkedList<String>();
        Map<String, Serializable> properties = docEventContext.getProperties();
        String type = (String) properties.get(MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TYPE);
        String mailboxTitle = (String) properties.get(MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE);
        String owner = (String) properties.get(MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_OWNER);
        if (owner != null && !"".equals(owner)) {
            newUsers.add(owner);
        }
        String entryId = (String) properties.get(MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY_ID);
        Boolean isPersonal = false;
        if (type != null && !"".equals(type)) {
            if (MailboxConstants.type.personal.toString().equals(type)) {
                isPersonal = true;
            } else {
                newGroups.add(entryId);
            }
        }
        Mailbox cf = sourceDoc.getAdapter(Mailbox.class);
        cf.setTitle(mailboxTitle);
        CoreSession coreSession = docEventContext.getCoreSession();
        String updatePolicy = Framework.getProperty(MailboxConstants.GROUP_UPDATE_SYNC_POLICY_PROPERTY);
        if (updatePolicy == null || "".equals(updatePolicy)) {
            updatePolicy = MailboxConstants.updatePolicy.merge.toString();
            if (updatePolicy.equals(MailboxConstants.updatePolicy.merge.toString())) {
                if (isPersonal && !newUsers.isEmpty()) {
                    List<String> users = cf.getUsers();
                    newUsers.addAll(users);
                    cf.setUsers(newUsers);
                } else if (!newGroups.isEmpty()) {
                    List<String> groups = cf.getGroups();
                    newGroups.addAll(groups);
                    cf.setGroups(newGroups);
                }
            } else if (updatePolicy.equals(MailboxConstants.updatePolicy.override.toString())) {
                if (isPersonal && !newUsers.isEmpty()) {
                    cf.setUsers(newUsers);
                } else if (!newGroups.isEmpty()) {
                    cf.setGroups(newGroups);
                }
            }
        }
        cf.save(coreSession);
    }

}
