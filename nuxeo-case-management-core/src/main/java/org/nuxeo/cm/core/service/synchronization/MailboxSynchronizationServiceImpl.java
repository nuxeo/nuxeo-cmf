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

import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_NAME;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_DIRECTORY_SCHEMA_NAME;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_ENTRY_ID;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_OWNER;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TITLE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_MAILBOX_TYPE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZED_DATE;
import static org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EVENT_CONTEXT_SYNCHRONIZER_ID;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.service.MailboxTitleGenerator;
import org.nuxeo.cm.service.synchronization.MailboxDirectorySynchronizationDescriptor;
import org.nuxeo.cm.service.synchronization.MailboxGroupSynchronizationDescriptor;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.EventNames;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants.synchronisedState;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationService;
import org.nuxeo.cm.service.synchronization.MailboxUserSynchronizationDescriptor;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.ReconnectedEventBundle;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.UnboundEventContext;
import org.nuxeo.ecm.core.query.sql.model.DateLiteral;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.usermanager.UserManagerImpl;
import org.nuxeo.ecm.platform.web.common.exceptionhandling.ExceptionHelper;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.services.event.Event;
import org.nuxeo.runtime.services.event.EventService;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author Laurent Doguin
 */
public class MailboxSynchronizationServiceImpl extends DefaultComponent
        implements MailboxSynchronizationService {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(MailboxSynchronizationService.class);

    protected static final String QUERY_GET_MAILBOX_FROM_ID = "SELECT * FROM Mailbox WHERE "
            + "mlbx:synchronizerId= '%s'";

    protected static final String QUERY_GET_MAILBOX_FROM_TITLE = "SELECT * FROM Mailbox WHERE "
            + "dc:title= '%s'";

    protected static final String QUERY_GET_DELETED_MAILBOX = "SELECT * FROM Mailbox WHERE "
            + "mlbx:origin= '%s'  AND mlbx:lastSyncUpdate < TIMESTAMP '%s' AND ecm:currentLifeCycleState != 'deleted'";

    protected EventProducer eventProducer;

    protected final Map<String, MailboxDirectorySynchronizationDescriptor> directorySynchronizer = new HashMap<String, MailboxDirectorySynchronizationDescriptor>();

    protected MailboxUserSynchronizationDescriptor userSynchronizer;

    protected MailboxGroupSynchronizationDescriptor groupSynchronizer;

    protected int count;

    protected int total;

    protected int batchSize = 100;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (contribution instanceof MailboxDirectorySynchronizationDescriptor) {
            MailboxDirectorySynchronizationDescriptor synchronizer = (MailboxDirectorySynchronizationDescriptor) contribution;
            String directoryName = synchronizer.getDirectoryName();
            MailboxDirectorySynchronizationDescriptor existingDirSynchronizer = directorySynchronizer.get(directoryName);
            if (existingDirSynchronizer == null) {
                if (synchronizer.getDirectoryEntryIdField() != null
                        && synchronizer.getMailboxIdField() != null
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
        } else if (contribution instanceof MailboxGroupSynchronizationDescriptor) {
            MailboxGroupSynchronizationDescriptor synchronizer = (MailboxGroupSynchronizationDescriptor) contribution;
            if (synchronizer.getTitleGenerator() != null) {
                groupSynchronizer = synchronizer;
            } else {
                log.error("Could not register contribution because of missing field(s) in contribution");
            }
        } else if (contribution instanceof MailboxUserSynchronizationDescriptor) {
            MailboxUserSynchronizationDescriptor synchronizer = (MailboxUserSynchronizationDescriptor) contribution;
            if (synchronizer.getTitleGenerator() != null) {
                userSynchronizer = synchronizer;
            } else {
                log.error("Could not register contribution because of missing field(s) in contribution");
            }

        }
    }

    protected MailboxDirectorySynchronizationDescriptor mergeDirectoryContribution(
            MailboxDirectorySynchronizationDescriptor existingDirSynchronizer,
            MailboxDirectorySynchronizationDescriptor synchronizer)
            throws InstantiationException, IllegalAccessException {
        existingDirSynchronizer.setEnabled(synchronizer.isEnabled());
        if (synchronizer.getMailboxIdField() != null) {
            existingDirSynchronizer.setMailboxIdField(synchronizer.getMailboxIdField());
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

    @Override
    public Map<String, MailboxDirectorySynchronizationDescriptor> getSynchronizerMap() {
        return directorySynchronizer;
    }

    @Override
    public MailboxUserSynchronizationDescriptor getUserSynchronizer() {
        return userSynchronizer;
    }

    @Override
    public MailboxGroupSynchronizationDescriptor getGroupSynchronizer() {
        return groupSynchronizer;
    }

    @Override
    public void doSynchronize() throws Exception {
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
        String batchSize = Framework.getProperty(MailboxConstants.SYNC_BATCH_SIZE_PROPERTY);
        if (batchSize != null && !"".equals(batchSize)) {
            this.batchSize = Integer.parseInt(batchSize);
        }
        Repository repo = mgr.getDefaultRepository();
        SynchronizeSessionRunner runner = new SynchronizeSessionRunner(
                repo.getName());
        runner.runUnrestricted();

        flushJaasCache();
    }

    /**
     * Flushes jaas cache otherwise mailboxes may not be updated on users who
     * login, logout and login again
     */
    // FIXME: should be also called when editing a Mailbox in the interface
    protected void flushJaasCache() {
        try {
            EventService eventService = Framework.getService(EventService.class);
            if (eventService != null) {
                eventService.sendEvent(new Event(
                        UserManagerImpl.USERMANAGER_TOPIC,
                        UserManagerImpl.USERCHANGED_EVENT_ID, this, null));
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected void synchronizeGroupList(Map<String, List<String>> groupMap,
            String directoryName, String directoryIdField, Calendar now,
            UserManager userManager, MailboxTitleGenerator titleGenerator,
            CoreSession coreSession, Boolean txStarted) throws ClientException {
        String type = MailboxConstants.type.generic.toString();
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
                    try {
                        groupModel = userManager.getGroupModel(groupName);
                        if (groupModel == null) {
                            log.error("Could not synchronize mailbox for user " + groupName);
                            continue;
                        }
                        synchronizerId = String.format("%s:%s", directoryName,
                                groupName);
                        generatedTitle = titleGenerator.getMailboxTitle(groupModel);
                        synchronizeMailbox(groupModel, directoryName,
                                userManager.getGroupSchemaName(),
                                parentSynchronizerId, synchronizerId,
                                groupName, generatedTitle, null, type, now,
                                coreSession);
                        List<String> groupChilds = userManager.getGroupsInGroup(groupName);
                        if (groupChilds != null && !groupChilds.isEmpty()) {
                            nextChildrenBatch.put(synchronizerId, groupChilds);
                        }
                        if (++count % batchSize == 0) {
                            if (txStarted) {
                                log.debug("Transaction ended during Mailbox synchronization");
                                TransactionHelper.commitOrRollbackTransaction();
                                txStarted = TransactionHelper.startTransaction();
                                log.debug("New Transaction started during Mailbox synchronization");
                            }
                        }
                        log.debug(String.format(
                                "Updated %d/%d group Mailboxes", count, total));
                    } catch (DirectoryException de) {
                        Throwable t = ExceptionHelper.unwrapException(de);
                        if (t.getMessage().contains(
                                "javax.naming.NameNotFoundException")) {
                            log.warn("Searched entry does not exist: "
                                    + groupName);
                        } else {
                            throw new CaseManagementRuntimeException(
                                    "User synchronization failed", de);
                        }
                    }
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
            UserManager userManager, MailboxTitleGenerator titleGenerator,
            CoreSession coreSession, Boolean txStarted) throws ClientException {
        String type = MailboxConstants.type.personal.toString();
        String synchronizerId;
        String generatedTitle;
        DocumentModel userModel;
        int total = userIds.size();
        try {
            for (String userId : userIds) {
                try {
                    userModel = userManager.getUserModel(userId);
                    if (userModel == null) {
                        log.error("Could not synchronize mailbox for user " + userId);
                        continue;
                    }
                    synchronizerId = String.format("%s:%s", directoryName,
                            userId);
                    generatedTitle = titleGenerator.getMailboxTitle(userModel);
                    synchronizeMailbox(userModel, directoryName,
                            userManager.getUserSchemaName(), "",
                            synchronizerId, userId, generatedTitle, userId,
                            type, now, coreSession);
                    if (++count % batchSize == 0) {
                        if (txStarted) {
                            log.debug("Transaction ended during Mailbox synchronization");
                            TransactionHelper.commitOrRollbackTransaction();
                            txStarted = TransactionHelper.startTransaction();
                            log.debug("New Transaction started during Mailbox synchronization");
                        }
                    }
                    log.debug(String.format("Updated %d/%d user Mailboxes",
                            count, total));
                } catch (DirectoryException de) {
                    Throwable t = ExceptionHelper.unwrapException(de);
                    if (t.getMessage().contains(
                            "javax.naming.NameNotFoundException")) {
                        log.warn("Searched entry does not exist: " + userId);
                    } else {
                        throw new CaseManagementRuntimeException(
                                "User synchronization failed", de);
                    }
                }
            }
        } catch (Exception e) {
            if (txStarted) {
                TransactionHelper.setTransactionRollbackOnly();
            }
            throw new CaseManagementRuntimeException(
                    "User synchronization failed", e);
        }
    }

    protected void synchronizeMailbox(DocumentModel entry,
            String directoryName, String directorySchema,
            String parentSynchronizerId, String synchronizerId, String entryId,
            String generatedTitle, String owner, String type, Calendar now,
            CoreSession coreSession) throws ClientException {

        // TODO: hook mailbox resolvers so that synchronizerId can be
        // customized depending on what mailbox is found
        DocumentModel cfDoc = getMailboxFromSynchronizerId(synchronizerId,
                coreSession);

        Mailbox cf = null;
        if (cfDoc != null) {
            cf = cfDoc.getAdapter(Mailbox.class);
            // use the actual synchronizer id
            synchronizerId = cf.getSynchronizerId();
        }

        // initiate eventPropertiesMap
        Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();
        eventProperties.put(EVENT_CONTEXT_MAILBOX_ENTRY_ID, entryId);
        // avoid reconnecting document
        entry.putContextData(
                ReconnectedEventBundle.SKIP_REFETCH_DOCUMENT_CONTEXT_KEY,
                Boolean.TRUE);
        eventProperties.put(EVENT_CONTEXT_MAILBOX_ENTRY, entry);
        eventProperties.put(EVENT_CONTEXT_DIRECTORY_NAME, directoryName);
        eventProperties.put(EVENT_CONTEXT_DIRECTORY_SCHEMA_NAME,
                directorySchema);
        eventProperties.put(EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID,
                parentSynchronizerId);
        eventProperties.put(EVENT_CONTEXT_SYNCHRONIZER_ID, synchronizerId);
        eventProperties.put(EVENT_CONTEXT_MAILBOX_TITLE, generatedTitle);
        eventProperties.put(EVENT_CONTEXT_MAILBOX_OWNER, owner);
        eventProperties.put(EVENT_CONTEXT_MAILBOX_TYPE, type);
        eventProperties.put(EVENT_CONTEXT_SYNCHRONIZED_DATE, now);
        eventProperties.put(CoreEventConstants.SESSION_ID,
                coreSession.getSessionId());

        if (cf != null) {
            Calendar lastSyncUpdate = cf.getLastSyncUpdate();
            // Look if case has already been updated during this batch.
            if (lastSyncUpdate == null || !lastSyncUpdate.equals(now)) {
                if (cf.isSynchronized()) {
                    // throw onMailboxUpdated
                    cf.setLastSyncUpdate(now);
                    coreSession.saveDocument(cfDoc);
                    log.debug(String.format("Update Mailbox %s", synchronizerId));
                    notify(EventNames.onMailboxUpdated.toString(),
                            cf.getDocument(), eventProperties, coreSession);

                } else {
                    // a user set it as unsynchronized, we don't modify it
                    // anymore
                    cf.setLastSyncUpdate(now);
                    coreSession.saveDocument(cfDoc);
                    log.debug(String.format(
                            "set Unsynchronized state for Mailbox %s",
                            synchronizerId));
                    return;
                }
            }
        } else {
            cfDoc = getMailboxFromTitle(generatedTitle, coreSession);
            cf = null;
            if (cfDoc != null) {
                cf = cfDoc.getAdapter(Mailbox.class);
            }
            if (cf != null) {
                Calendar lastSyncUpdate = cf.getLastSyncUpdate();
                boolean setDoublon = false;
                // Look if case has already been updated during this batch.
                if (lastSyncUpdate == null || !lastSyncUpdate.equals(now)) {
                    if (cf.isSynchronized()) {
                        // set synchronizerId, throw onMailboxUpdate
                        cf.setSynchronizerId(synchronizerId);
                        cf.setLastSyncUpdate(now);
                        cf.setOrigin(directoryName);
                        coreSession.saveDocument(cfDoc);
                        log.debug(String.format("Update Mailbox %s",
                                synchronizerId));
                        notify(EventNames.onMailboxUpdated.toString(),
                                cf.getDocument(), eventProperties, coreSession);
                    } else {
                        setDoublon = true;
                    }
                } else {
                    setDoublon = true;
                }
                if (setDoublon) {
                    // set doublon: A user created a CF with the same title
                    // we assume he doesn't want the same mailbox created
                    cf.setSynchronizeState(synchronisedState.doublon.toString());
                    cf.setLastSyncUpdate(now);
                    coreSession.saveDocument(cfDoc);
                    log.debug(String.format("set Doublon state for Mailbox %s",
                            synchronizerId));
                }
            } else {
                // throws onMailboxCreated
                log.debug(String.format("Creates Mailbox %s", synchronizerId));
                DocumentModel mailboxModel = coreSession.createDocumentModel(getMailboxType());
                notify(EventNames.onMailboxCreated.toString(), mailboxModel,
                        eventProperties, coreSession);
            }
        }
    }

    protected String getMailboxType() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return correspDocumentTypeService.getMailboxType();
    }

    protected void handleDeletedMailboxes(String directoryName, Calendar now,
            CoreSession coreSession) throws ClientException {
        String dateLiteral = DateLiteral.dateTimeFormatter.print(now.getTimeInMillis());
        String query = String.format(QUERY_GET_DELETED_MAILBOX, directoryName,
                dateLiteral);
        DocumentModelList deletedMailboxes = coreSession.query(query);
        if (deletedMailboxes == null) {
            return;
        }
        Mailbox cf;
        String synchronizerId;
        Map<String, Serializable> eventProperties;
        for (DocumentModel mailboxDoc : deletedMailboxes) {
            cf = mailboxDoc.getAdapter(Mailbox.class);
            if (cf == null) {
                log.error(String.format(
                        "Could not get Mailbox adapter for doc %s",
                        mailboxDoc.getId()));
                continue;
            }
            synchronizerId = cf.getSynchronizerId();
            eventProperties = new HashMap<String, Serializable>();
            eventProperties.put(EVENT_CONTEXT_DIRECTORY_NAME, directoryName);
            eventProperties.put(EVENT_CONTEXT_SYNCHRONIZER_ID, synchronizerId);
            eventProperties.put(EVENT_CONTEXT_MAILBOX_TITLE,
                    mailboxDoc.getTitle());
            eventProperties.put(EVENT_CONTEXT_SYNCHRONIZED_DATE, now);
            log.debug(String.format(
                    "Mailbox %s has been remove from directory, deleting it.",
                    synchronizerId));
            notify(EventNames.onMailboxDeleted.toString(), mailboxDoc,
                    eventProperties, coreSession);
        }
    }

    protected DocumentModel getMailboxFromSynchronizerId(String id,
            CoreSession coreSession) throws ClientException {
        String query = String.format(QUERY_GET_MAILBOX_FROM_ID, escape(id));
        DocumentModelList mailboxDocs = coreSession.query(query);
        if (mailboxDocs == null || mailboxDocs.isEmpty()) {
            log.debug(String.format("Mailbox with id %s does not exist", id));
            return null;
        } else if (mailboxDocs.size() > 1) {
            // more than one mailbox for given Id
            // Should not happen
            log.error(String.format("Found more than one Mailbox for id %s", id));
            return null;
        }
        DocumentModel mailboxDoc = mailboxDocs.get(0);
        return mailboxDoc;
    }

    protected DocumentModel getMailboxFromTitle(String title,
            CoreSession coreSession) throws ClientException {
        String query = String.format(QUERY_GET_MAILBOX_FROM_TITLE,
                escape(title));
        DocumentModelList mailboxDocs = coreSession.query(query);
        if (mailboxDocs == null || mailboxDocs.isEmpty()) {
            log.debug(String.format("Mailbox with title %s does not exist",
                    title));
            return null;
        } else if (mailboxDocs.size() > 1) {
            // more than one mailbox for given title
            // return first Mailbox
            log.debug(String.format(
                    "Found more than one Mailbox for Title %s, uses first found.",
                    title));
        }
        DocumentModel mailboxDoc = mailboxDocs.get(0);
        return mailboxDoc;
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

    private class SynchronizeSessionRunner extends UnrestrictedSessionRunner {
        public SynchronizeSessionRunner(String repository) {
            super(repository);
        }

        @Override
        public void run() throws ClientException {
            UserManager userManager;
            try {
                userManager = Framework.getService(UserManager.class);
            } catch (Exception e) {
                throw new CaseManagementRuntimeException(e);
            }
            if (userManager == null) {
                throw new CaseManagementException("User manager not found");
            }

            Calendar now;
            String directoryName;
            MailboxTitleGenerator titleGenerator;
            String directoryIdField;

            // synchronize group
            if (groupSynchronizer != null && groupSynchronizer.isEnabled()) {
                try {
                    titleGenerator = groupSynchronizer.getTitleGenerator();
                } catch (Exception e) {
                    throw new CaseManagementRuntimeException(e);
                }
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
                        if (!TransactionHelper.isTransactionActive()) {
                            txStarted = TransactionHelper.startTransaction();
                            log.debug("New Transaction started during Mailbox synchronization");
                        }
                        synchronizeGroupList(topBatch, directoryName,
                                directoryIdField, now, userManager,
                                titleGenerator, session, txStarted);
                    } catch (Exception e) {
                        if (txStarted) {
                            TransactionHelper.setTransactionRollbackOnly();
                        }
                        throw new CaseManagementRuntimeException(
                                "Group synchronization failed", e);
                    } finally {
                        if (txStarted) {
                            TransactionHelper.commitOrRollbackTransaction();
                            log.debug("Transaction ended during Mailbox synchronization");
                        }
                    }
                    log.info("Looking for deleted group entries");
                    handleDeletedMailboxes(directoryName, now, session);
                    log.info("Group directory has been synchronized");
                } else {
                    log.error("Could not find GroupTitleGenerator, abort group directory synchronization.");
                }
            }
            // synchronize users
            if (userSynchronizer != null && userSynchronizer.isEnabled()) {
                try {
                    titleGenerator = userSynchronizer.getTitleGenerator();
                } catch (Exception e) {
                    throw new CaseManagementRuntimeException(e);
                }
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
                        if (!TransactionHelper.isTransactionActive()) {
                            txStarted = TransactionHelper.startTransaction();
                            log.debug("New Transaction started during Mailbox synchronization");
                        }
                        synchronizeUserList(userIds, directoryName,
                                directoryIdField, now, userManager,
                                titleGenerator, session, txStarted);
                    } catch (Exception e) {
                        if (txStarted) {
                            TransactionHelper.setTransactionRollbackOnly();
                        }
                        throw new CaseManagementRuntimeException(
                                "User synchronization failed", e);
                    } finally {
                        if (txStarted) {
                            TransactionHelper.commitOrRollbackTransaction();
                            log.debug("Transaction ended during Mailbox synchronization");
                        }
                    }
                    log.debug(String.format("Updated %d/%d mailboxes", count,
                            total));
                    handleDeletedMailboxes(directoryName, now, session);
                    log.info("User directory has been synchronized");
                } else {
                    log.error("Could not find UserTitleGenerator, abort user directory synchronization.");
                }
            }
        }
    }
}
