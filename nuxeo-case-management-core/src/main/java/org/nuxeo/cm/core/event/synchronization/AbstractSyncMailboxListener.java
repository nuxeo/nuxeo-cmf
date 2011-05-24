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

import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TYPE;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public abstract class AbstractSyncMailboxListener implements EventListener {

    public static String getMailboxType() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return correspDocumentTypeService.getMailboxType();
    }

    public static DocumentModel getMailboxDocument(Event event)
            throws ClientException {
        DocumentEventContext docEventContext = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docEventContext = (DocumentEventContext) event.getContext();
        } else {
            // can't get associated Document.
            throw new ClientException("Could not get Document from event");
        }
        return docEventContext.getSourceDocument();
    }

    public static String getMailboxType(Event event) {
        Map<String, Serializable> properties = event.getContext().getProperties();
        return (String) properties.get(EVENT_CONTEXT_MAILBOX_TYPE);
    }

    public static boolean isMailboxType(Event event, MailboxConstants.type type) {
        String eventType = getMailboxType(event);
        if (eventType != null && !"".equals(eventType)) {
            if (eventType.equals(type.name())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMailboxPersonal(Event event) {
        return isMailboxType(event, MailboxConstants.type.personal);
    }

    public static boolean isMailboxGeneric(Event event) {
        return isMailboxType(event, MailboxConstants.type.generic);
    }

    public static String getGroupUpdatePolicy(Event event) {
        Map<String, Serializable> properties = event.getContext().getProperties();
        return (String) properties.get(MailboxConstants.GROUP_UPDATE_SYNC_POLICY_PROPERTY);
    }

    public static boolean isGroupUpdatePolicy(Event event,
            MailboxConstants.updatePolicy updatePolicy) {
        String eventPolicy = getGroupUpdatePolicy(event);
        if (eventPolicy != null && !"".equals(eventPolicy)) {
            if (eventPolicy.equals(updatePolicy.name())) {
                return true;
            }
        }
        return false;
    }

}
