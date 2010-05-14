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
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.casefolder.CaseFolderHeader;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.CaseFolderCreator;
import org.nuxeo.cm.service.CaseFolderManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.EventProducer;
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
public class CaseFolderManagementServiceImpl implements CaseFolderManagementService {

    // FIXME: do not use a query model: this is service specific and won't
    // change
    protected static final String QUERY_GET_ALL_CASE_FOLDER = "GET_ALL_CASE_FOLDER";

    // FIXME: do not use a query model: this is service specific and won't
    // change
    protected static final String QUERY_GET_CASE_FOLDER_FROM_ID = "GET_CASE_FOLDER_FROM_ID";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseFolderManagementServiceImpl.class);

    protected CaseFolderCreator personalMailboxCreator;

    protected EventProducer eventProducer;

    protected Map<String, Serializable> context = null;


    public CaseFolder getCaseFolder(CoreSession session, String muid) {

        if (muid == null) {
            return null;
        }

        DocumentModelList res = executeQueryModel(session,
                QUERY_GET_CASE_FOLDER_FROM_ID, new Object[] { muid });

        if (res == null || res.isEmpty()) {
            return null;
        }

        if (res.size() > 1) {
            log.warn(String.format(
                    "Several mailboxes with id %s, returning first found", muid));
        }

        return res.get(0).getAdapter(CaseFolder.class);

    }


    public CaseFolder getCaseFolder(String muid) {
        CoreSession session = null;
        try {
            session = getCoreSession();
            return getCaseFolder(session, muid);
        } finally {
            closeCoreSession(session);
        }

    }


    public boolean hasCaseFolder(String muid) {
        if (getCaseFolderHeader(muid) == null) {
            return false;
        }
        return true;
    }


    public CaseFolderHeader getCaseFolderHeader(String muid) {
        if (muid == null) {
            return null;
        }
        List<String> muids = new ArrayList<String>();
        muids.add(muid);
        List<CaseFolderHeader> mailboxesHeaders = getCaseFoldersHeaders(muids);
        if (mailboxesHeaders == null || mailboxesHeaders.isEmpty()) {
            return null;
        } else {
            return mailboxesHeaders.get(0);
        }

    }

    public List<CaseFolder> getCaseFolders(CoreSession session,
            List<String> muids) {

        if (muids == null) {
            return null;
        }

        List<DocumentModel> docs = new ArrayList<DocumentModel>();

        for (String muid : muids) {

            DocumentModelList res = executeQueryModel(session,
                    QUERY_GET_CASE_FOLDER_FROM_ID, new Object[] { muid });

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
        return CaseFolderConstants.getMailboxList(docs);

    }


    public List<CaseFolderHeader> getCaseFoldersHeaders(List<String> muids) {
        CoreSession session = null;
        try {
            session = getCoreSession();
            GetCaseFoldersHeadersUnrestricted sessionSearch = new GetCaseFoldersHeadersUnrestricted(
                    session, muids);
            sessionSearch.runUnrestricted();
            return sessionSearch.getMailboxesHeaders();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        } finally {
            closeCoreSession(session);
        }
    }


    public List<CaseFolderHeader> getCaseFoldersHeaders(CoreSession session,
            List<String> muids) {
        GetCaseFoldersHeadersUnrestricted sessionSearch = new GetCaseFoldersHeadersUnrestricted(
                session, muids);
        try {
            sessionSearch.runUnrestricted();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        return sessionSearch.getMailboxesHeaders();
    }


    public List<CaseFolder> getCaseFolders(List<String> muids) {

        CoreSession session = null;
        try {
            session = getCoreSession();
            return getCaseFolders(session, muids);
        } finally {
            closeCoreSession(session);
        }

    }


    public List<CaseFolder> getUserCaseFolders(CoreSession session, String user) {

        // return all mailboxes user has access to
        DocumentModelList res = executeQueryModel(session,
                QUERY_GET_ALL_CASE_FOLDER);

        List<CaseFolder> mailboxes = new ArrayList<CaseFolder>();

        // Load all the Mailbox adapters
        if (res != null && !res.isEmpty()) {
            for (DocumentModel mbModel : res) {
                CaseFolder mb = mbModel.getAdapter(CaseFolder.class);
                mailboxes.add(mb);
            }
        }

        Collections.sort(mailboxes);

        return mailboxes;

    }


    public CaseFolder getUserPersonalCaseFolder(CoreSession session, String user) {
        String mailboxId = getUserPersonalCaseFolderId(user);
        return getCaseFolder(session, mailboxId);
    }


    public List<CaseFolder> createPersonalCaseFolders(CoreSession session, String user) {
        if (personalMailboxCreator == null) {
            throw new CaseManagementRuntimeException(
            "Cannot create personal mailbox: missing creator configuration");
        }
        // First check if mailbox exists using unrestricted session to
        // avoid creating multiple personal mailboxes for a given user in
        // case there's something wrong with Read rights on mailbox folder
        String muid = getUserPersonalCaseFolderId(user);
        if (hasCaseFolder(muid)) {
            log.error(String.format(
                    "Cannot create personal mailbox for user '%s': "
                    + "it already exists with id '%s'", user, muid));
            return Arrays.asList(getCaseFolder(muid));
        }
        try {
            return personalMailboxCreator.createCaseFolders(session, user);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
    }



    public CaseFolder getUserPersonalCaseFolderForEmail(CoreSession session,
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
            throw new CaseManagementRuntimeException(
                    "Couldn't query user directory", e);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
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
            return getUserPersonalCaseFolder(session, userIds.get(0));
        }

        return null;
    }


    public List<CaseFolderHeader> searchCaseFolders(String pattern, String type) {
        SearchCaseFoldersHeadersUnrestricted sessionSearch = new SearchCaseFoldersHeadersUnrestricted(
                getCoreSession(), pattern, type);
        try {
            sessionSearch.runUnrestricted();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        return sessionSearch.getMailboxesHeaders();

    }


    public boolean hasUserPersonalCaseFolder(CoreSession session, String userId) {
        // FIXME: shouldn't check be unrestricted?
        return (getUserPersonalCaseFolder(session, userId) != null);
    }

    /**
     * Retrieve the Personal Mailbox Id from the Mailbox Creator
     *
     * @param user Owner of the mailbox
     *
     * @return The personal Mailbox Id
     * @throws ClientException
     */
    public String getUserPersonalCaseFolderId(String user) {
        UserManager userManager = null;
        try {
            userManager = Framework.getService(UserManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (userManager == null) {
            throw new CaseManagementRuntimeException("User manager not found");
        }
        DocumentModel userModel = null;
        try {
            userModel = userManager.getUserModel(user);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (userModel == null) {
            log.warn(String.format("No User by that name. Maybe a wrong id or virtual user"));
            return null;
        }
        return personalMailboxCreator.getPersonalCaseFolderId(userModel);
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
            throw new CaseManagementRuntimeException(
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
            throw new CaseManagementRuntimeException(
                    "Error while looking up directory service", e);
        }
    }



    /**
     * Execute a query model
     *
     * @param queryModel The name of the query model
     * @return the corresponding documentModels
     * @throws CaseManagementException
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
     * @throws CaseManagementException
     */
    protected DocumentModelList executeQueryModel(CoreSession session,
            String queryModel, Object[] params) {
        // TODO use session query instead of query model
        QueryModelService qmService = null;
        try {
            qmService = Framework.getService(QueryModelService.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (qmService == null) {
            throw new CaseManagementRuntimeException("Query Manager not found");
        }
        QueryModelDescriptor qmd = qmService.getQueryModelDescriptor(queryModel);
        QueryModel qm = new QueryModel(qmd);
        DocumentModelList list = null;
        try {
            list = qm.getDocuments(session, params);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        return list;

    }

    protected CoreSession getCoreSession() {

        RepositoryManager mgr = null;
        try {
            mgr = Framework.getService(RepositoryManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (mgr == null) {
            throw new CaseManagementRuntimeException(
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
            throw new CaseManagementRuntimeException(e);
        }

        return session;

    }


    protected void closeCoreSession(CoreSession coreSession) {
        if (coreSession != null) {
            CoreInstance.getInstance().close(coreSession);
        }
    }


    CaseFolderCreator getPersonalMailboxCreator() {
        return personalMailboxCreator;
    }

    void setPersonalMailboxCreator(CaseFolderCreator personalMailboxCreator) {
        this.personalMailboxCreator = personalMailboxCreator;
    }


}
