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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.synchronization.CaseFolderSynchronizationConstants;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.Event;

/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class CaseFolderCreatedListener extends AbstractSyncCaseFolderListener {

    private static final Log log = LogFactory.getLog(CaseFolderCreatedListener.class);

    @SuppressWarnings("unchecked")
    public void handleEvent(Event event) throws ClientException {
        Map<String, Serializable> properties = event.getContext().getProperties();
        String caseFolderTitle = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TITLE);
        String directoryName = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME);
        String parentSynchronizerId = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID);
        String synchronizerId = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID);
        String owner = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_OWNER);
        String type = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TYPE);
        String entryId = (String) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_ENTRY_ID);
        Calendar synchronizeDate = (Calendar) properties.get(CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE);

        String sessionId = (String) properties.get(CoreEventConstants.SESSION_ID);

        try {
            CoreSession session = getCoreSession(sessionId);

            String searchQuery;
            // Take the first CaseFolderRoot when there is no parent.
            if (parentSynchronizerId == null || "".equals(parentSynchronizerId)) {
                searchQuery = String.format("SELECT * from %s",
                        CaseFolderConstants.CASE_FOLDER_ROOT_DOCUMENT_TYPE);
            } else {
                searchQuery = String.format(
                        "SELECT * from CaseFolder WHERE csfd:synchronizerId= '%s'",
                        parentSynchronizerId);
            }
            DocumentModelList res = session.query(searchQuery);
            if (res == null || res.isEmpty()) {
                throw new CaseManagementException(
                        "Cannot find any mailbox folder");
            }
            String id = null;
            Boolean isPersonal = false;
            if (type != null && !"".equals(type)) {
                if (CaseFolderConstants.type.personal.toString().equals(type)) {
                    id = IdUtils.generateId(NuxeoPrincipal.PREFIX + entryId);
                    isPersonal = true;
                } else {
                    id = IdUtils.generateId(NuxeoGroup.PREFIX + entryId);
                    isPersonal = false;
                }
            }
            // Create the personal mailbox for the user
            DocumentModel mailboxModel = session.createDocumentModel(
                    res.get(0).getPathAsString(),
                    IdUtils.generateId(caseFolderTitle), getCaseFolderType());
            CaseFolder mailbox = mailboxModel.getAdapter(CaseFolder.class);
            // Set mailbox properties
            mailbox.setSynchronizeState(CaseFolderSynchronizationConstants.synchronisedState.synchronised.toString());
            if (synchronizerId != null && !"".equals(synchronizerId)) {
                mailbox.setSynchronizerId(synchronizerId);
            }
            if (caseFolderTitle != null && !"".equals(caseFolderTitle)) {
                mailbox.setTitle(caseFolderTitle);
            }
            if (directoryName != null && !"".equals(directoryName)) {
                mailbox.setOrigin(directoryName);
            }
            if (owner != null && !"".equals(owner)) {
                mailbox.setOwner(owner);
            }
            if (synchronizeDate != null) {
                mailbox.setLastSyncUpdate(synchronizeDate);
            }
            if (isPersonal){
                List<String> users = new LinkedList<String>();
                users.add(entryId);
                mailbox.setUsers(users);
            } else {
                List<String> groups = new LinkedList<String>();
                groups.add(entryId);
                mailbox.setGroups(groups);
            }
            mailbox.setType(type);
            mailbox.setId(id);

            mailboxModel = session.createDocument(mailboxModel);
            session.saveDocument(mailboxModel);
        } catch (Exception e) {
            throw new CaseManagementException(
                    "Error during mailboxes creation", e);
        }

    }

}
