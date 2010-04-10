/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.core.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.core.post.CreateDraftPostUnrestricted;
import org.nuxeo.cm.core.post.CreatePostUnrestricted;
import org.nuxeo.cm.core.post.UpdatePostUnrestricted;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants.EventNames;
import org.nuxeo.cm.exception.CorrespondenceException;
import org.nuxeo.cm.exception.CorrespondenceRuntimeException;
import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mail.MailEnvelopeLifeCycleConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostConstants;
import org.nuxeo.cm.service.CorrespondenceDocumentTypeService;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.cm.service.MailboxCreator;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModel;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModelService;
import org.nuxeo.ecm.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.relations.api.ResourceAdapter;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * Correspondence service core implementation
 */
public class CorrespondenceServiceImpl implements CorrespondenceService {

    // FIXME: should be configurable and internationalized
    private static final String REP_SUFFIX = "Rep: ";

    // FIXME: do not use a query model: this is service specific and won't
    // change
    protected static final String QUERY_GET_ALL_MAILBOX = "GET_ALL_MAILBOX";

    // FIXME: do not use a query model: this is service specific and won't
    // change
    protected static final String QUERY_GET_MAILBOX_FROM_ID = "GET_MAILBOX_FROM_ID";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CorrespondenceServiceImpl.class);

    protected MailboxCreator personalMailboxCreator;

    protected EventProducer eventProducer;

    protected Map<String, Serializable> context = null;

    public Mailbox getMailbox(CoreSession session, String muid) {

        if (muid == null) {
            return null;
        }

        DocumentModelList res = executeQueryModel(session,
                QUERY_GET_MAILBOX_FROM_ID, new Object[] { muid });

        if (res == null || res.isEmpty()) {
            return null;
        }

        if (res.size() > 1) {
            log.warn(String.format(
                    "Several mailboxes with id %s, returning first found", muid));
        }

        return res.get(0).getAdapter(Mailbox.class);

    }

    public Mailbox getMailbox(String muid) {
        CoreSession session = null;
        try {
            session = getCoreSession();
            return getMailbox(session, muid);
        } finally {
            closeCoreSession(session);
        }

    }

    public boolean hasMailbox(String muid) {
        if (getMailboxHeader(muid) == null) {
            return false;
        }
        return true;
    }

    public MailboxHeader getMailboxHeader(String muid) {
        if (muid == null) {
            return null;
        }
        List<String> muids = new ArrayList<String>();
        muids.add(muid);
        List<MailboxHeader> mailboxesHeaders = getMailboxesHeaders(muids);
        if (mailboxesHeaders == null || mailboxesHeaders.isEmpty()) {
            return null;
        } else {
            return mailboxesHeaders.get(0);
        }

    }

    public List<Mailbox> getMailboxes(CoreSession session, List<String> muids) {

        if (muids == null) {
            return null;
        }

        List<DocumentModel> docs = new ArrayList<DocumentModel>();

        for (String muid : muids) {

            DocumentModelList res = executeQueryModel(session,
                    QUERY_GET_MAILBOX_FROM_ID, new Object[] { muid });

            if (res == null || res.isEmpty()) {
                continue;
            }
            if (res.size() > 1) {
                log.warn(String.format(
                        "Several mailboxes with id %s, returning first found",
                        muid));
            }

            docs.add(res.get(0));

        }
        return MailboxConstants.getMailboxList(docs);

    }

    public List<MailboxHeader> getMailboxesHeaders(List<String> muids) {
        CoreSession session = null;
        try {
            session = getCoreSession();
            GetMailboxesHeadersUnrestricted sessionSearch = new GetMailboxesHeadersUnrestricted(
                    session, muids);
            sessionSearch.runUnrestricted();
            return sessionSearch.getMailboxesHeaders();
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        } finally {
            closeCoreSession(session);
        }
    }

    public List<MailboxHeader> getMailboxesHeaders(CoreSession session,
            List<String> muids) {
        GetMailboxesHeadersUnrestricted sessionSearch = new GetMailboxesHeadersUnrestricted(
                session, muids);
        try {
            sessionSearch.runUnrestricted();
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        return sessionSearch.getMailboxesHeaders();
    }

    public List<Mailbox> getMailboxes(List<String> muids) {

        CoreSession session = null;
        try {
            session = getCoreSession();
            return getMailboxes(session, muids);
        } finally {
            closeCoreSession(session);
        }

    }

    public List<Mailbox> getUserMailboxes(CoreSession session, String user) {

        // return all mailboxes user has access to
        DocumentModelList res = executeQueryModel(session,
                QUERY_GET_ALL_MAILBOX);

        List<Mailbox> mailboxes = new ArrayList<Mailbox>();

        // Load all the Mailbox adapters
        if (res != null && !res.isEmpty()) {
            for (DocumentModel mbModel : res) {
                Mailbox mb = mbModel.getAdapter(Mailbox.class);
                mailboxes.add(mb);
            }
        }

        Collections.sort(mailboxes);

        return mailboxes;

    }

    public Mailbox getUserPersonalMailbox(CoreSession session, String user) {
        String mailboxId = getUserPersonalMailboxId(user);
        return getMailbox(session, mailboxId);
    }

    public Mailbox getUserPersonalMailboxForEmail(CoreSession session,
            String userEmail) {
        if (userEmail == null
                || org.apache.commons.lang.StringUtils.isEmpty(userEmail)) {
            return null;
        }
        Directory dir = getUserDirectory();
        Session dirSession = null;
        List<String> userIds = null;
        Map<String, Serializable> filter = new HashMap<String, Serializable>();
        try {
            UserManager userManager = Framework.getService(UserManager.class);
            if (userManager != null) {
                filter.put(userManager.getUserEmailField(), userEmail);
                dirSession = dir.getSession();
                userIds = dirSession.getProjection(filter, dir.getIdField());
            } else {
                log.error("Could not resolve UserManager service");
            }
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(
                    "Couldn't query user directory", e);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        } finally {
            if (dirSession != null) {
                try {
                    dirSession.close();
                } catch (DirectoryException e) {
                }
            }
        }
        if (userIds != null && !userIds.isEmpty()
                && !org.apache.commons.lang.StringUtils.isEmpty(userIds.get(0))) {
            // return first found
            return getUserPersonalMailbox(session, userIds.get(0));
        }

        return null;
    }

    protected Directory getUserDirectory() {
        try {
            UserManager userManager = Framework.getService(UserManager.class);
            String dirName;
            if (userManager == null) { // unit tests
                dirName = "userDirectory";
            } else {
                dirName = userManager.getUserDirectoryName();
            }
            return getDirService().getDirectory(dirName);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(
                    "Error acccessing user directory", e);
        }
    }

    /**
     * Encapsulates lookup and exception management.
     *
     * @return The DirectoryService, guaranteed not null
     * @throws DirectoryException
     */
    protected DirectoryService getDirService() {
        try {
            // get local service to be able to retrieve directories, because
            // they cannot be retrieved remotely
            return Framework.getLocalService(DirectoryService.class);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(
                    "Error while looking up directory service", e);
        }
    }

    public List<MailboxHeader> searchMailboxes(String pattern, String type) {
        SearchMailboxesHeadersUnrestricted sessionSearch = new SearchMailboxesHeadersUnrestricted(
                getCoreSession(), pattern, type);
        try {
            sessionSearch.runUnrestricted();
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        return sessionSearch.getMailboxesHeaders();

    }

    public CorrespondencePost sendEnvelope(CoreSession session,
            CorrespondencePost postRequest, boolean isInitial) {
        try {
            SendPostUnrestricted sendPostUnrestricted = new SendPostUnrestricted(
                    session, postRequest, isInitial);
            sendPostUnrestricted.runUnrestricted();
            return sendPostUnrestricted.getPost();
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }

    }

    MailboxCreator getPersonalMailboxCreator() {
        return personalMailboxCreator;
    }

    void setPersonalMailboxCreator(MailboxCreator personalMailboxCreator) {
        this.personalMailboxCreator = personalMailboxCreator;
    }

    protected CoreSession getCoreSession() {

        RepositoryManager mgr = null;
        try {
            mgr = Framework.getService(RepositoryManager.class);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (mgr == null) {
            throw new CorrespondenceRuntimeException(
                    "Cannot find RepostoryManager");
        }
        Repository repo = mgr.getDefaultRepository();

        CoreSession session = null;
        try {
            if (context == null) {
                session = repo.open();
                context = new HashMap<String, Serializable>();
                context.put(ResourceAdapter.CORE_SESSION_ID_CONTEXT_KEY,
                        session.getSessionId());
            } else {
                session = repo.open(context);
            }

        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }

        return session;

    }

    protected void closeCoreSession(CoreSession coreSession) {
        if (coreSession != null) {
            CoreInstance.getInstance().close(coreSession);
        }
    }

    /**
     * Execute a query model
     *
     * @param queryModel The name of the query model
     * @return the corresponding documentModels
     * @throws CorrespondenceException
     */
    protected DocumentModelList executeQueryModel(CoreSession session,
            String queryModel) {
        return executeQueryModel(session, queryModel, new Object[] {});
    }

    /**
     * Execute a query model
     *
     * @param queryModel The name of the query model
     * @param params params if the query model
     * @return the corresponding documentModels
     * @throws CorrespondenceException
     */
    protected DocumentModelList executeQueryModel(CoreSession session,
            String queryModel, Object[] params) {
        // TODO use session query instead of query model
        QueryModelService qmService = null;
        try {
            qmService = Framework.getService(QueryModelService.class);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (qmService == null) {
            throw new CorrespondenceRuntimeException("Query Manager not found");
        }
        QueryModelDescriptor qmd = qmService.getQueryModelDescriptor(queryModel);
        QueryModel qm = new QueryModel(qmd);
        DocumentModelList list = null;
        try {
            list = qm.getDocuments(session, params);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        return list;

    }

    public List<Mailbox> createPersonalMailbox(CoreSession session, String user) {
        if (personalMailboxCreator == null) {
            throw new CorrespondenceRuntimeException(
                    "Cannot create personal mailbox: missing creator configuration");
        }
        // First check if mailbox exists using unrestricted session to
        // avoid creating multiple personal mailboxes for a given user in
        // case there's something wrong with Read rights on mailbox folder
        String muid = getUserPersonalMailboxId(user);
        if (hasMailbox(muid)) {
            log.error(String.format(
                    "Cannot create personal mailbox for user '%s': "
                            + "it already exists with id '%s'", user, muid));
            return Arrays.asList(getMailbox(muid));
        }
        try {
            return personalMailboxCreator.createMailboxes(session, user);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve the Personal Mailbox Id from the Mailbox Creator
     *
     * @param user Owner of the mailbox
     *
     * @return The personal Mailbox Id
     * @throws ClientException
     */
    public String getUserPersonalMailboxId(String user) {
        UserManager userManager = null;
        try {
            userManager = Framework.getService(UserManager.class);
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (userManager == null) {
            throw new CorrespondenceRuntimeException("User manager not found");
        }
        DocumentModel userModel = null;
        try {
            userModel = userManager.getUserModel(user);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (userModel == null) {
            log.warn(String.format("No User by that name. Maybe a wrong id or virtual user"));
            return null;
        }
        return personalMailboxCreator.getPersonalMailboxId(userModel);
    }

    public boolean hasUserPersonalMailbox(CoreSession session, String userId) {
        // FIXME: shouldn't check be unrestricted?
        return (getUserPersonalMailbox(session, userId) != null);
    }

    protected List<CorrespondencePost> getPosts(CoreSession coreSession,
            long offset, long limit, String query) {
        List<CorrespondencePost> posts = new ArrayList<CorrespondencePost>();
        DocumentModelList result = null;

        try {
            result = coreSession.query(query, null, limit, offset, false);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }

        for (DocumentModel documentModel : result) {
            // TODO Lazy Loading
            posts.add(documentModel.getAdapter(CorrespondencePost.class));
        }
        return posts;
    }

    public List<CorrespondencePost> getReceivedPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        if (mailbox == null) {
            return null;
        }
        String query = String.format(
                "SELECT * FROM Document WHERE ecm:parentId ='%s' and %s=0",
                mailbox.getDocument().getId(),
                CorrespondencePostConstants.IS_SENT_FIELD);
        return getPosts(coreSession, offset, limit, query);
    }

    public List<CorrespondencePost> getSentPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        if (mailbox == null) {
            return null;
        }
        String query = String.format(
                "SELECT * FROM Document WHERE ecm:parentId ='%s' and %s=1",
                mailbox.getDocument().getId(),
                CorrespondencePostConstants.IS_SENT_FIELD);
        return getPosts(coreSession, offset, limit, query);
    }

    public List<CorrespondencePost> getDraftPosts(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        if (mailbox == null) {
            return null;
        }
        String query = String.format(
                "SELECT * FROM Document WHERE ecm:parentId ='%s' and %s=1",
                mailbox.getDocument().getId(),
                CorrespondencePostConstants.IS_DRAFT_FIELD);
        return getPosts(coreSession, offset, limit, query);
    }

    public MailEnvelope createMailEnvelope(CoreSession session,
            DocumentModel emailDoc, String parentPath, List<Mailbox> mailboxes) {
        // Save the new mail in the MailRoot folder
        MailEnvelopeItem item = emailDoc.getAdapter(MailEnvelopeItem.class);
        String docName = IdUtils.generateId("doc " + item.getTitle());
        emailDoc.setPathInfo(parentPath, docName);
        DocumentModel mail = null;
        try {
            CreateMailDocumentUnrestricted mailCreator = new CreateMailDocumentUnrestricted(
                    session, emailDoc, mailboxes);
            mailCreator.runUnrestricted();
            mail = session.getDocument(mailCreator.getDocRef());
            // Create envelope
            CreateEnvelopeUnrestricted envelopeCreator = new CreateEnvelopeUnrestricted(
                    session, mail.getAdapter(MailEnvelopeItem.class), parentPath, mailboxes);
            envelopeCreator.runUnrestricted();
            DocumentModel envelopeDoc = session.getDocument(envelopeCreator.getDocumentRef());
            return envelopeDoc.getAdapter(MailEnvelope.class);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public MailEnvelope createMailEnvelope(CoreSession session,
            DocumentModel emailDoc, String parentPath) {
        return createMailEnvelope(session, emailDoc, parentPath, new ArrayList<Mailbox>());
    }
    public CorrespondencePost createDraftPost(CoreSession session,
            Mailbox mailbox, MailEnvelope envelope) {
        try {

            Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();
            eventProperties.put(
                    CaseManagementEventConstants.EVENT_CONTEXT_CASE,
                    envelope);
            eventProperties.put("category",
                    CaseManagementEventConstants.DISTRIBUTION_CATEGORY);
            fireEvent(session, envelope, eventProperties,
                    EventNames.beforeDraftCreated.name());
            CreateDraftPostUnrestricted runner = new CreateDraftPostUnrestricted(
                    session.getRepositoryName(),
                    (String) envelope.getDocument().getPropertyValue(
                            MailConstants.TITLE_PROPERTY_NAME), envelope,
                    mailbox);
            runner.runUnrestricted();
            CorrespondencePost draft = runner.getCreatedPost();
            eventProperties.put(
                    CaseManagementEventConstants.EVENT_CONTEXT_DRAFT, draft);
            fireEvent(session, envelope, eventProperties,
                    EventNames.afterDraftCreated.name());
            return draft;
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public CorrespondencePost getDraftPost(CoreSession coreSession,
            Mailbox mailbox, String envelopeId) {
        if (mailbox == null) {
            return null;
        }
        String query = String.format(
                "SELECT * FROM Document WHERE ecm:parentId ='%s' AND %s='%s' and %s=1",
                mailbox.getDocument().getId(),
                CorrespondencePostConstants.ENVELOPE_DOCUMENT_ID_FIELD,
                envelopeId, CorrespondencePostConstants.IS_DRAFT_FIELD);
        List<CorrespondencePost> result = getPosts(coreSession, 0, 0, query);
        int size = result.size();
        if (size > 1) {
            log.error("More than one draft for envelope '" + envelopeId + "'.");
            return null;
        } else if (size == 0) {
            return null;
        }

        return result.get(0);
    }

    /**
     * Fire an event for a MailEnvelope object.
     */
    protected void fireEvent(CoreSession coreSession, MailEnvelope envelope,
            Map<String, Serializable> eventProperties, String eventName) {
        try {
            DocumentEventContext envContext = new DocumentEventContext(
                    coreSession, coreSession.getPrincipal(),
                    envelope.getDocument());
            envContext.setProperties(eventProperties);
            getEventProducer().fireEvent(envContext.newEvent(eventName));
            List<MailEnvelopeItem> items = envelope.getMailEnvelopeItems(coreSession);
            for (MailEnvelopeItem item : items) {
                DocumentModel doc = item.getDocument();
                DocumentEventContext docContext = new DocumentEventContext(
                        coreSession, coreSession.getPrincipal(), doc);
                docContext.setProperties(eventProperties);
                getEventProducer().fireEvent(docContext.newEvent(eventName));
            }
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public void notify(CoreSession session, String name,
            DocumentModel document, Map<String, Serializable> eventProperties) {
        DocumentEventContext envContext = new DocumentEventContext(session,
                session.getPrincipal(), document);
        envContext.setProperties(eventProperties);
        try {
            getEventProducer().fireEvent(envContext.newEvent(name));
        } catch (Exception e) {
            throw new CorrespondenceRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve and cache the Event Producer Service
     *
     * @return The Event Producer Service
     * @throws Exception
     */
    protected EventProducer getEventProducer() throws Exception {
        if (eventProducer == null) {
            eventProducer = Framework.getService(EventProducer.class);
        }
        return eventProducer;
    }

    public class SendPostUnrestricted extends UnrestrictedSessionRunner {

        protected final CorrespondencePost postRequest;

        protected final boolean isInitial;

        protected EventProducer eventProducer;

        protected CorrespondencePost post;

        public SendPostUnrestricted(CoreSession session,
                CorrespondencePost postRequest, boolean isInitial) {
            super(session);
            this.postRequest = postRequest;
            this.isInitial = isInitial;

        }

        @Override
        public void run() throws CorrespondenceException {

            try {
                String senderMailboxId = postRequest.getSender();
                GetMailboxesUnrestricted getMailboxesUnrestricted = new GetMailboxesUnrestricted(
                        session, senderMailboxId);
                getMailboxesUnrestricted.run();
                List<Mailbox> senderMailboxes = getMailboxesUnrestricted.getMailboxes();
                Mailbox senderMailbox = null;
                if (senderMailboxes != null && !senderMailboxes.isEmpty()) {
                    senderMailbox = senderMailboxes.get(0);
                }

                String subject = postRequest.getSubject();
                String comment = postRequest.getComment();
                MailEnvelope envelope = postRequest.getMailEnvelope(session);

                // Create Event properties
                Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();

                Map<String, List<String>> externalRecipients = postRequest.getInitialExternalRecipients();
                Map<String, List<String>> internalRecipientIds = postRequest.getInitialInternalRecipients();
                List<String> mailboxTitles = new ArrayList<String>();
                for (String type : internalRecipientIds.keySet()) {
                    // TODO: optimize;
                    mailboxTitles.clear();
                    List<MailboxHeader> mailboxesHeaders = getMailboxesHeaders(
                            session, internalRecipientIds.get(type));
                    if (senderMailboxes != null) {
                        for (MailboxHeader mailboxHeader : mailboxesHeaders) {
                            mailboxTitles.add(mailboxHeader.getTitle());
                        }
                    }
                    eventProperties.put(
                            CaseManagementEventConstants.EVENT_CONTEXT_PARTICIPANTS_TYPE_
                                    + type, StringUtils.join(mailboxTitles,
                                    ", "));
                }

                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_SENDER_CASE_FOLDER,
                        senderMailbox);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_SUBJECT,
                        subject);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_COMMENT,
                        comment);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_CASE,
                        envelope);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_INTERNAL_PARTICIPANTS,
                        (Serializable) internalRecipientIds);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_EXTERNAL_PARTICIPANTS,
                        (Serializable) externalRecipients);
                eventProperties.put("category",
                        CaseManagementEventConstants.DISTRIBUTION_CATEGORY);
                eventProperties.put(
                        CaseManagementEventConstants.EVENT_CONTEXT_IS_INITIAL,
                        isInitial);

                fireEvent(session, envelope, eventProperties,
                        EventNames.beforeCaseSentEvent.name());

                if (isInitial) {

                    // Update the lifecycle of the envelope
                    envelope.getDocument().followTransition(
                            MailEnvelopeLifeCycleConstants.TRANSITION_SEND);

                    if (senderMailbox != null) {
                        // Update the Draft post for the sender
                        CorrespondencePost draft = getDraftPost(session,
                                senderMailbox, envelope.getDocument().getId());

                        if (draft == null) {
                            throw new CorrespondenceException(
                                    "No draft for an initial send.");
                        }

                        UpdatePostUnrestricted createPostUnrestricted = new UpdatePostUnrestricted(
                                session, subject, comment, envelope,
                                senderMailbox, senderMailbox.getId(),
                                internalRecipientIds, externalRecipients, true,
                                isInitial, draft);
                        createPostUnrestricted.run();
                        post = createPostUnrestricted.getUpdatedPost();
                    }

                } else if (senderMailbox != null) {
                    // No draft, create the Post for the sender
                    CreatePostUnrestricted createPostUnrestricted = new CreatePostUnrestricted(
                            null, session, subject, comment, envelope,
                            senderMailbox, senderMailbox.getId(),
                            internalRecipientIds, externalRecipients, true,
                            isInitial);
                    createPostUnrestricted.run();
                    post = createPostUnrestricted.getCreatedPost();
                }

                // Create the Posts for the recipients
                for (String type : internalRecipientIds.keySet()) {
                    for (String recipient : internalRecipientIds.get(type)) {
                        CreatePostUnrestricted createMessageUnrestricted = new CreatePostUnrestricted(
                                post, session, subject, comment, envelope,
                                senderMailbox, recipient, internalRecipientIds,
                                externalRecipients, false, isInitial);
                        createMessageUnrestricted.run();
                    }
                }

                fireEvent(session, envelope, eventProperties,
                        EventNames.afterCaseSentEvent.name());

            } catch (ClientException e) {
                throw new CorrespondenceException(e);
            }
        }

        public CorrespondencePost getPost() {
            return post;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.nuxeo.correspondence.service.CorrespondenceService#getReplyDocument
     *      (org.nuxeo.ecm.core.api.CoreSession,
     *      com.nuxeo.correspondence.mailbox.Mailbox,
     *      org.nuxeo.ecm.core.api.DocumentModel)
     */
    public DocumentModel getReplyDocument(CoreSession session, Mailbox mailbox,
            DocumentModel receivedMail) throws ClientException {

        MailEnvelopeItem receivedItem = receivedMail.getAdapter(MailEnvelopeItem.class);

        Map<String, Object> context = new HashMap<String, Object>();

        // Set the path of MailRoot
        context.put(CoreEventConstants.PARENT_PATH,
                MailConstants.MAIL_ROOT_DOCUMENT_PATH);
        context.put(CaseManagementEventConstants.EVENT_CONTEXT_CASE_FOLDER_ID,
                mailbox.getId());
        context.put(
                CaseManagementEventConstants.EVENT_CONTEXT_AFFILIATED_CASE_FOLDER_ID,
                mailbox.getAffiliatedMailboxId());

        // Use the Correspondence Type Service to retrieve the used outgoing
        // mail core type
        CorrespondenceDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        // Create the new Mail document model in the MailRoot
        DocumentModel responseMail = session.createDocumentModel(
                correspDocumentTypeService.getResponseOutgoingDocType(),
                context);

        // Set recipients
        responseMail.setPropertyValue(
                MailConstants.CORRESPONDENCE_CONTACTS_RECIPIENTS,
                receivedMail.getPropertyValue(MailConstants.CORRESPONDENCE_CONTACTS_SENDERS));

        // Set senders
        NuxeoPrincipal nxp = (NuxeoPrincipal) session.getPrincipal();

        // FIXME: should be configurable
        HashMap<String, String> senderItem = new HashMap<String, String>();
        senderItem.put("name", nxp.getFirstName());
        senderItem.put("surname", nxp.getLastName());
        senderItem.put("service", nxp.getCompany());

        List<HashMap<String, String>> senders = new ArrayList<HashMap<String, String>>();
        senders.add(senderItem);
        responseMail.setPropertyValue(
                MailConstants.CORRESPONDENCE_CONTACTS_SENDERS,
                (Serializable) senders);

        // Set the title
        MailEnvelopeItem responseItem = responseMail.getAdapter(MailEnvelopeItem.class);
        responseItem.setTitle(REP_SUFFIX + receivedItem.getTitle());

        // Set the answered document id
        responseMail.setPropertyValue(
                MailConstants.CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID,
                receivedMail.getId());

        return responseMail;
    }

}
