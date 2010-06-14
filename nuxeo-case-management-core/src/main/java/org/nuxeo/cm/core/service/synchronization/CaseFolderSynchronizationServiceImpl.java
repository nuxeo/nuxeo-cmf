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
 *     Laurent Doguin
 */
package org.nuxeo.cm.core.service.synchronization;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.CaseFolderTitleGenerator;
import org.nuxeo.cm.service.synchronization.CaseFolderDirectorySynchronizationDescriptor;
import org.nuxeo.cm.service.synchronization.CaseFolderGroupSynchronizationDescriptor;
import org.nuxeo.cm.service.synchronization.CaseFolderSynchronizationConstants;
import org.nuxeo.cm.service.synchronization.CaseFolderSynchronizationService;
import org.nuxeo.cm.service.synchronization.CaseFolderUserSynchronizationDescriptor;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.UnboundEventContext;
import org.nuxeo.ecm.core.query.sql.model.DateLiteral;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author Laurent Doguin
 */
public class CaseFolderSynchronizationServiceImpl extends DefaultComponent
        implements CaseFolderSynchronizationService {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseFolderSynchronizationService.class);

    protected static final String QUERY_GET_CASE_FOLDER_FROM_ID = "SELECT * FROM CaseFolder WHERE "
            + "csfd:synchronizerId= '%s'";

    protected static final String QUERY_GET_CASE_FOLDER_FROM_TITLE = "SELECT * FROM CaseFolder WHERE "
            + "dc:title= '%s'";

    protected static final String QUERY_GET_DELETED_CASE_FOLDER = "SELECT * FROM CaseFolder WHERE "
            + "csfd:origin= '%s'  AND csfd:lastSyncUpdate < TIMESTAMP '%s' AND ecm:currentLifeCycleState != 'deleted'";

    protected EventProducer eventProducer;

    private Map<String, CaseFolderDirectorySynchronizationDescriptor> directorySynchronizer = new HashMap<String, CaseFolderDirectorySynchronizationDescriptor>();

    private CaseFolderUserSynchronizationDescriptor userSynchronizer;

    private CaseFolderGroupSynchronizationDescriptor groupSynchronizer;

    private int count;

    private int total;

    private int batchSize = 100;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (contribution instanceof CaseFolderDirectorySynchronizationDescriptor) {
            CaseFolderDirectorySynchronizationDescriptor synchronizer = (CaseFolderDirectorySynchronizationDescriptor) contribution;
            String directoryName = synchronizer.getDirectoryName();
            CaseFolderDirectorySynchronizationDescriptor existingDirSynchronizer = directorySynchronizer.get(directoryName);
            if (existingDirSynchronizer == null) {
                if (synchronizer.getDirectoryEntryIdField() != null
                        && synchronizer.getCaseFolderIdField() != null
                        && synchronizer.getDirectoryName() != null
                        && synchronizer.getTitleGenerator() != null) {
                    directorySynchronizer.put(synchronizer.getDirectoryName(),
                            synchronizer);
                } else {
                    log.error("Could not register contribution because of missing field(s) in contribution");
                }
            } else {
                mergeDirectoryContribution(existingDirSynchronizer,
                        synchronizer);
            }
        } else if (contribution instanceof CaseFolderGroupSynchronizationDescriptor) {
            CaseFolderGroupSynchronizationDescriptor synchronizer = (CaseFolderGroupSynchronizationDescriptor) contribution;
            if (synchronizer.getTitleGenerator() != null) {
                groupSynchronizer = synchronizer;
            } else {
                log.error("Could not register contribution because of missing field(s) in contribution");
            }
        } else if (contribution instanceof CaseFolderUserSynchronizationDescriptor) {
            CaseFolderUserSynchronizationDescriptor synchronizer = (CaseFolderUserSynchronizationDescriptor) contribution;
            if (synchronizer.getTitleGenerator() != null) {
                userSynchronizer = synchronizer;
            } else {
                log.error("Could not register contribution because of missing field(s) in contribution");
            }

        }
    }

    private CaseFolderDirectorySynchronizationDescriptor mergeDirectoryContribution(
            CaseFolderDirectorySynchronizationDescriptor existingDirSynchronizer,
            CaseFolderDirectorySynchronizationDescriptor synchronizer)
            throws InstantiationException, IllegalAccessException {
        existingDirSynchronizer.setEnabled(synchronizer.isEnabled());
        if (synchronizer.getCaseFolderIdField() != null) {
            existingDirSynchronizer.setCaseFolderIdField(synchronizer.getCaseFolderIdField());
        }
        if (synchronizer.getChildrenField() != null) {
            existingDirSynchronizer.setChildrenField(synchronizer.getChildrenField());
        }
        if (synchronizer.getDirectoryEntryIdField() != null) {
            existingDirSynchronizer.setDirectoryEntryIdField(synchronizer.getDirectoryEntryIdField());
        }
        if (synchronizer.getTitleGenerator() != null) {
            existingDirSynchronizer.setTitleGenerator(synchronizer.getTitleGenerator());
        }
        return existingDirSynchronizer;
    }

    public Map<String, CaseFolderDirectorySynchronizationDescriptor> getSynchronizerMap() {
        return directorySynchronizer;
    }

    public CaseFolderUserSynchronizationDescriptor getUserSynchronizer() {
        return userSynchronizer;
    }

    public CaseFolderGroupSynchronizationDescriptor getGroupSynchronizer() {
        return groupSynchronizer;
    }

    public void doSynchronize() throws Exception {
        CoreSession coreSession = getCoreSession();
        UserManager userManager = Framework.getService(UserManager.class);
        if (userManager == null) {
            throw new CaseManagementException("User manager not found");
        }

        Calendar now;
        String directoryName;
        CaseFolderTitleGenerator titleGenerator;
        String directoryIdField;
        String batchSize = Framework.getProperty(CaseFolderConstants.SYNC_BATCH_SIZE_PROPERTY);
        if (batchSize != null && !"".equals(batchSize)) {
            this.batchSize = Integer.parseInt(batchSize);
        }

        // synchronize group
        if (groupSynchronizer != null && groupSynchronizer.isEnabled()) {
            titleGenerator = groupSynchronizer.getTitleGenerator();
            if (titleGenerator != null) {
                now = GregorianCalendar.getInstance();
                directoryName = userManager.getGroupDirectoryName();
                directoryIdField = userManager.getGroupIdField();
                List<String> topLevelgroups = userManager.getTopLevelGroups();
                Map<String, List<String>> topBatch = new HashMap<String, List<String>>();
                topBatch.put("", topLevelgroups);
                log.info("Start groups synchronization");
                count = 0;
                total = userManager.getGroupIds().size();
                boolean txStarted = false;
                try {
                    txStarted = TransactionHelper.startTransaction();
                    log.debug("New Transaction started during CaseFolder synchronization");
                    synchronizeGroupList(topBatch, directoryName,
                            directoryIdField, now, userManager, titleGenerator,
                            coreSession, txStarted);
                } catch (Exception e) {
                    if (txStarted) {
                        TransactionHelper.setTransactionRollbackOnly();
                    }
                    throw new CaseManagementRuntimeException(
                            "Group synchronization failed", e);
                } finally {
                    if (txStarted) {
                        TransactionHelper.commitOrRollbackTransaction();
                        log.debug("Transaction ended during CaseFolder synchronization");
                    }
                }
                log.info("Looking for deleted group entries");
                handleDeletedCaseFolders(directoryName, now, coreSession);
                log.info("Group directory has been synchronized");
            } else {
                log.error("Could not find GroupTitleGenerator, abort group directory synchronization.");
            }
        }
        // synchronize users
        if (userSynchronizer != null && userSynchronizer.isEnabled()) {
            titleGenerator = userSynchronizer.getTitleGenerator();
            if (titleGenerator != null) {
                now = new GregorianCalendar();
                directoryName = userManager.getUserDirectoryName();
                directoryIdField = userManager.getUserIdField();
                List<String> userIds = userManager.getUserIds();
                log.debug("Start users synchronization");
                count = 0;
                total = userIds.size();

                boolean txStarted = false;
                try {
                    txStarted = TransactionHelper.startTransaction();
                    log.debug("New Transaction started during CaseFolder synchronization");
                    synchronizeUserList(userIds, directoryName,
                            directoryIdField, now, userManager, titleGenerator,
                            coreSession, txStarted);
                } catch (Exception e) {
                    if (txStarted) {
                        TransactionHelper.setTransactionRollbackOnly();
                    }
                    throw new CaseManagementRuntimeException(
                            "User synchronization failed", e);
                } finally {
                    if (txStarted) {
                        TransactionHelper.commitOrRollbackTransaction();
                        log.debug("Transaction ended during CaseFolder synchronization");
                    }
                }
                log.debug(String.format("Updated %d/%d mailboxes", count, total));
                handleDeletedCaseFolders(directoryName, now, coreSession);
                log.info("User directory has been synchronized");
            } else {
                log.error("Could not find UserTitleGenerator, abort user directory synchronization.");
            }
        }
    }

    protected void synchronizeGroupList(Map<String, List<String>> groupMap,
            String directoryName, String directoryIdField, Calendar now,
            UserManager userManager, CaseFolderTitleGenerator titleGenerator,
            CoreSession coreSession, Boolean txStarted) throws ClientException {
        String type = CaseFolderConstants.type.generic.toString();
        String synchronizerId;
        String generatedTitle;
        String parentSynchronizerId;
        DocumentModel groupModel;
        Set<Entry<String, List<String>>> groupSet = groupMap.entrySet();
        Map<String, List<String>> nextChildrenBatch = new HashMap<String, List<String>>();
        try {
            for (Entry<String, List<String>> groupEntry : groupSet) {
                parentSynchronizerId = groupEntry.getKey();
                List<String> groups = groupEntry.getValue();
                for (String groupName : groups) {
                    groupModel = userManager.getGroupModel(groupName);
                    synchronizerId = String.format("%s:%s", directoryName,
                            groupName);
                    generatedTitle = titleGenerator.getCaseFolderTitle(groupModel);
                    synchronizeCaseFolder(groupModel, directoryName,
                            parentSynchronizerId, synchronizerId, groupName,
                            generatedTitle, null, type, now, coreSession);
                    List<String> groupChilds = userManager.getGroupsInGroup(groupName);
                    if (groupChilds != null && !groupChilds.isEmpty()) {
                        nextChildrenBatch.put(synchronizerId, groupChilds);
                    }
                    if (++count % batchSize == 0) {
                        if (txStarted) {
                            log.debug("Transaction ended during CaseFolder synchronization");
                            TransactionHelper.commitOrRollbackTransaction();
                            txStarted = TransactionHelper.startTransaction();
                            log.debug("New Transaction started during CaseFolder synchronization");
                        }
                    }
                    log.debug(String.format("Updated %d/%d group CaseFolders",
                            count, total));
                }
            }
            if (!nextChildrenBatch.isEmpty()) {
                synchronizeGroupList(nextChildrenBatch, directoryName,
                        directoryIdField, now, userManager, titleGenerator,
                        coreSession, txStarted);
            }
        } catch (Exception e) {
            if (txStarted) {
                TransactionHelper.setTransactionRollbackOnly();
            }
            throw new CaseManagementRuntimeException(
                    "Group synchronization failed", e);
        }
    }

    protected void synchronizeUserList(List<String> userIds,
            String directoryName, String directoryIdField, Calendar now,
            UserManager userManager, CaseFolderTitleGenerator titleGenerator,
            CoreSession coreSession, Boolean txStarted) throws ClientException {
        String type = CaseFolderConstants.type.personal.toString();
        String synchronizerId;
        String generatedTitle;
        DocumentModel userModel;
        int total = userIds.size();
        try {
            for (String userId : userIds) {
                userModel = userManager.getUserModel(userId);
                synchronizerId = String.format("%s:%s", directoryName, userId);
                generatedTitle = titleGenerator.getCaseFolderTitle(userModel);
                synchronizeCaseFolder(userModel, directoryName, "",
                        synchronizerId, userId, generatedTitle, userId, type,
                        now, coreSession);
                if (++count % batchSize == 0) {
                    if (txStarted) {
                        log.debug("Transaction ended during CaseFolder synchronization");
                        TransactionHelper.commitOrRollbackTransaction();
                        txStarted = TransactionHelper.startTransaction();
                        log.debug("New Transaction started during CaseFolder synchronization");
                    }
                }
                log.debug(String.format("Updated %d/%d user CaseFolders",
                        count, total));
            }
        } catch (Exception e) {
            if (txStarted) {
                TransactionHelper.setTransactionRollbackOnly();
            }
            throw new CaseManagementRuntimeException(
                    "User synchronization failed", e);
        }
    }

    protected void synchronizeCaseFolder(DocumentModel entry,
            String directoryName, String parentSynchronizerId,
            String synchronizerId, String entryId, String generatedTitle,
            String owner, String type, Calendar now, CoreSession coreSession)
            throws ClientException {

        // initiate eventPropertiesMap
        Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_ENTRY_ID,
                entryId);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME,
                directoryName);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID,
                parentSynchronizerId);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID,
                synchronizerId);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TITLE,
                generatedTitle);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_OWNER,
                owner);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TYPE,
                type);
        eventProperties.put(
                CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE,
                now);
        eventProperties.put(CoreEventConstants.SESSION_ID,
                coreSession.getSessionId());

        DocumentModel cfDoc = getCaseFolderFromSynchronizerId(synchronizerId,
                coreSession);
        CaseFolder cf = null;
        if (cfDoc != null) {
            cf = cfDoc.getAdapter(CaseFolder.class);
        }
        if (cf != null) {
            Calendar lastSyncUpdate = cf.getLastSyncUpdate();
            // Look if case has already been updated during this batch.
            if (lastSyncUpdate == null || !lastSyncUpdate.equals(now)) {
                if (cf.isSynchronized()) {
                    // throw onCaseFolderUpdated
                    cf.setLastSyncUpdate(now);
                    coreSession.saveDocument(cfDoc);
                    log.debug(String.format("Update CaseFolder %s",
                            synchronizerId));
                    notify(
                            CaseFolderSynchronizationConstants.EventNames.onCaseFolderUpdated.toString(),
                            cf.getDocument(), eventProperties, coreSession);

                } else {
                    // a user set it as unsynchronized, we don't modify it
                    // anymore
                    cf.setLastSyncUpdate(now);
                    coreSession.saveDocument(cfDoc);
                    log.debug(String.format(
                            "set Unsynchronized state for CaseFolder %s",
                            synchronizerId));
                    return;
                }
            }
        } else {
            cfDoc = getCaseFolderFromTitle(generatedTitle, coreSession);
            cf = null;
            if (cfDoc != null) {
                cf = cfDoc.getAdapter(CaseFolder.class);
            }
            if (cf != null) {
                Calendar lastSyncUpdate = cf.getLastSyncUpdate();
                // Look if case has already been updated during this batch.
                if (lastSyncUpdate == null || !lastSyncUpdate.equals(now)) {
                    if (cf.isSynchronized()) {
                        // set synchronizerId, throw onCaseFolderUpdate
                        cf.setSynchronizerId(synchronizerId);
                        cf.setLastSyncUpdate(now);
                        cf.setOrigin(directoryName);
                        coreSession.saveDocument(cfDoc);
                        log.debug(String.format("Update CaseFolder %s",
                                synchronizerId));
                        notify(
                                CaseFolderSynchronizationConstants.EventNames.onCaseFolderUpdated.toString(),
                                cf.getDocument(), eventProperties, coreSession);
                    } else {
                        // set doublon: A user created a CF with the same title
                        // we assume he doesn't want the same caseFolder created
                        cf.setSynchronizeState(CaseFolderSynchronizationConstants.synchronisedState.doublon.toString());
                        cf.setLastSyncUpdate(now);
                        coreSession.saveDocument(cfDoc);
                        log.debug(String.format(
                                "set Doublon state for CaseFolder %s",
                                synchronizerId));
                    }
                }
            } else {
                // throws onCaseFolderCreated
                log.debug(String.format("Creates CaseFolder %s", synchronizerId));
                notify(
                        CaseFolderSynchronizationConstants.EventNames.onCaseFolderCreated.toString(),
                        null, eventProperties, coreSession);
            }
        }
    }

    protected void handleDeletedCaseFolders(String directoryName, Calendar now,
            CoreSession coreSession) throws ClientException {
        String dateLiteral = DateLiteral.dateTimeFormatter.print(now.getTimeInMillis());
        String query = String.format(QUERY_GET_DELETED_CASE_FOLDER,
                directoryName, dateLiteral);
        DocumentModelList deletedCaseFolders = coreSession.query(query);
        if (deletedCaseFolders == null) {
            return;
        }
        CaseFolder cf;
        String synchronizerId;
        Map<String, Serializable> eventProperties;
        for (DocumentModel caseFolderDoc : deletedCaseFolders) {
            cf = caseFolderDoc.getAdapter(CaseFolder.class);
            if (cf == null) {
                log.error(String.format(
                        "Could not get CaseFolder adapter for doc %s",
                        caseFolderDoc.getId()));
                continue;
            }
            synchronizerId = cf.getSynchronizerId();
            eventProperties = new HashMap<String, Serializable>();
            eventProperties.put(
                    CaseFolderSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME,
                    directoryName);
            eventProperties.put(
                    CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID,
                    synchronizerId);
            eventProperties.put(
                    CaseFolderSynchronizationConstants.EVENT_CONTEXT_CASE_FOLDER_TITLE,
                    caseFolderDoc.getTitle());
            eventProperties.put(
                    CaseFolderSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE,
                    now);
            log.debug(String.format(
                    "Case Folder %s has been remove from directory, deleting it.",
                    synchronizerId));
            notify(
                    CaseFolderSynchronizationConstants.EventNames.onCaseFolderDeleted.toString(),
                    caseFolderDoc, eventProperties, coreSession);
        }
    }

    protected DocumentModel getCaseFolderFromSynchronizerId(String id,
            CoreSession coreSession) throws ClientException {
        String query = String.format(QUERY_GET_CASE_FOLDER_FROM_ID, id);
        DocumentModelList caseFolderDocs = coreSession.query(query);
        if (caseFolderDocs == null || caseFolderDocs.isEmpty()) {
            log.debug(String.format("CaseFolder with id %s does not exist", id));
            return null;
        } else if (caseFolderDocs.size() > 1) {
            // more than one caseFolder for given Id
            // Should not happen
            log.error(String.format(
                    "Found more than one Case Folder for id %s", id));
            return null;
        }
        DocumentModel caseFolderDoc = caseFolderDocs.get(0);
        return caseFolderDoc;
    }

    protected DocumentModel getCaseFolderFromTitle(String title,
            CoreSession coreSession) throws ClientException {
        String query = String.format(QUERY_GET_CASE_FOLDER_FROM_TITLE,
                escape(title));
        DocumentModelList caseFolderDocs = coreSession.query(query);
        if (caseFolderDocs == null || caseFolderDocs.isEmpty()) {
            log.debug(String.format("CaseFolder with title %s does not exist",
                    title));
            return null;
        } else if (caseFolderDocs.size() > 1) {
            // more than one caseFolder for given title
            // return first CaseFolder
            log.debug(String.format(
                    "Found more than one Case Folder for Title %s, uses first found.",
                    title));
        }
        DocumentModel caseFolderDoc = caseFolderDocs.get(0);
        return caseFolderDoc;
    }

    protected String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '+' || c == '-' || c == '!' || c == '"' || c == '\'') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public void notify(String name, DocumentModel document,
            Map<String, Serializable> eventProperties, CoreSession session) {
        EventContext envContext;
        if (document == null) {
            envContext = new UnboundEventContext(session,
                    session.getPrincipal(), null);
        } else {
            envContext = new DocumentEventContext(session,
                    session.getPrincipal(), document);
        }
        envContext.setProperties(eventProperties);
        try {
            getEventProducer().fireEvent(envContext.newEvent(name));
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
    }

    protected EventProducer getEventProducer() throws Exception {
        if (eventProducer == null) {
            eventProducer = Framework.getService(EventProducer.class);
        }
        return eventProducer;
    }

    protected CoreSession getCoreSession() {
        RepositoryManager mgr;
        try {
            mgr = Framework.getService(RepositoryManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }

        if (mgr == null) {
            throw new CaseManagementRuntimeException(
                    "Cannot find RepositoryManager");
        }
        Repository repo = mgr.getDefaultRepository();

        CoreSession session;
        try {
            session = repo.open();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }

        return session;
    }

    protected void closeCoreSession(CoreSession coreSession) {
        if (coreSession != null) {
            CoreInstance.getInstance().close(coreSession);
        }
    }

}
