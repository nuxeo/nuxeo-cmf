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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.cm.service.MailboxCreator;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Correspondence service core implementation
 */
public class MailboxManagementServiceImpl implements MailboxManagementService {

    protected static final String QUERY_GET_ALL_MAILBOX = "GET_ALL_MAILBOX";

    protected static final String QUERY_GET_MAILBOX_FROM_ID = "GET_MAILBOX_FROM_ID";

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(MailboxManagementServiceImpl.class);

    protected MailboxCreator personalMailboxCreator;

    protected EventProducer eventProducer;

    public Mailbox getMailbox(CoreSession session, String muid) {
        if (muid == null) {
            return null;
        }
        List<DocumentModel> res = executeQuery(session,
                QUERY_GET_MAILBOX_FROM_ID, muid);
        if (res == null || res.isEmpty()) {
            return null;
        }
        if (res.size() > 1) {
            log.warn(String.format(
                    "Several mailboxes with id %s, returning first found", muid));
        }
        return res.get(0).getAdapter(Mailbox.class);
    }

    public boolean hasMailbox(CoreSession session, String muid) {
        // use getMailboxHeader so that check is unrestricted
        return getMailboxHeader(session, muid) != null;
    }

    public MailboxHeader getMailboxHeader(CoreSession session, String muid) {
        if (muid == null) {
            return null;
        }

        List<String> muids = new ArrayList<String>();
        muids.add(muid);
        List<MailboxHeader> mailboxesHeaders = getMailboxesHeaders(session,
                muids);
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
            if (muid == null) {
                continue;
            }
            List<DocumentModel> res = executeQuery(session,
                    QUERY_GET_MAILBOX_FROM_ID, muid);
            if (res == null || res.isEmpty()) {
                log.warn(String.format("No mailbox found with id '%s'", muid));
                continue;
            }
            if (res.size() > 1) {
                log.warn(String.format(
                        "Several mailboxes with id '%s', returning first found",
                        muid));
            }
            docs.add(res.get(0));
        }
        return MailboxConstants.getMailboxList(docs);
    }

    public List<MailboxHeader> getMailboxesHeaders(CoreSession session,
            List<String> muids) {
        GetMailboxesHeadersUnrestricted sessionSearch = new GetMailboxesHeadersUnrestricted(
                session, muids);
        try {
            sessionSearch.runUnrestricted();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        return sessionSearch.getMailboxesHeaders();
    }

    public List<Mailbox> getUserMailboxes(CoreSession session, String user) {
        // return all mailboxes user has access to
        // FIXME: need the user as param...
        List<DocumentModel> res = executeQuery(session, QUERY_GET_ALL_MAILBOX);

        List<Mailbox> mailboxes = new ArrayList<Mailbox>();
        // Load all the Mailbox adapters
        if (res != null && !res.isEmpty()) {
            for (DocumentModel mbModel : res) {
                Mailbox mb = mbModel.getAdapter(Mailbox.class);
                mailboxes.add(mb);
            }
        }
        return mailboxes;
    }

    public Mailbox getUserPersonalMailbox(CoreSession session, String user) {
        String mailboxId = getUserPersonalMailboxId(user);
        return getMailbox(session, mailboxId);
    }

    public List<Mailbox> createPersonalMailboxes(CoreSession session,
            String user) {
        if (personalMailboxCreator == null) {
            throw new CaseManagementRuntimeException(
                    "Cannot create personal mailbox: missing creator configuration");
        }
        // First check if mailbox exists using unrestricted session to
        // avoid creating multiple personal mailboxes for a given user in
        // case there's something wrong with Read rights on mailbox folder
        String muid = getUserPersonalMailboxId(user);
        if (hasMailbox(session, muid)) {
            log.error(String.format(
                    "Cannot create personal mailbox for user '%s': "
                            + "it already exists with id '%s'", user, muid));
            return Arrays.asList(getMailbox(session, muid));
        }
        try {
            return personalMailboxCreator.createMailboxes(session, user);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
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
            return getUserPersonalMailbox(session, userIds.get(0));
        }

        return null;
    }

    public List<MailboxHeader> searchMailboxes(CoreSession session,
            String pattern, String type) {
        SearchMailboxesHeadersUnrestricted sessionSearch = new SearchMailboxesHeadersUnrestricted(
                session, pattern, type);
        try {
            sessionSearch.runUnrestricted();
            return sessionSearch.getMailboxesHeaders();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public boolean hasUserPersonalMailbox(CoreSession session, String userId) {
        String muid = getUserPersonalMailboxId(userId);
        return hasMailbox(session, muid);
    }

    /**
     * Retrieves the Personal Mailbox Id from the Mailbox Creator.
     *
     * @param user Owner of the mailbox
     * @return The personal Mailbox Id
     */
    public String getUserPersonalMailboxId(String user) {
        UserManager userManager;
        try {
            userManager = Framework.getService(UserManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (userManager == null) {
            throw new CaseManagementRuntimeException("User manager not found");
        }

        DocumentModel userModel;
        try {
            userModel = userManager.getUserModel(user);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (userModel == null) {
            log.debug(String.format("No User by that name. Maybe a wrong id or virtual user"));
            return null;
        }
        return personalMailboxCreator.getPersonalMailboxId(userModel);
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
     * Executes a page provider query.
     *
     * @param ppName the page provider name
     * @param params optional parameters for this provider name
     * @return the corresponding documentModels
     */
    @SuppressWarnings("unchecked")
    protected List<DocumentModel> executeQuery(CoreSession session,
            String ppName, Object... params) {
        PageProviderService pps;
        try {
            pps = Framework.getService(PageProviderService.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (pps == null) {
            throw new CaseManagementRuntimeException(
                    "PageProviderService not found");
        }

        HashMap<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY,
                (Serializable) session);

        List<DocumentModel> list;
        try {
            PageProvider<DocumentModel> pp = (PageProvider<DocumentModel>) pps.getPageProvider(
                    ppName, null, null, null, props, null, params);
            list = pp.getCurrentPage();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        return list;
    }

    MailboxCreator getPersonalMailboxCreator() {
        return personalMailboxCreator;
    }

    void setPersonalMailboxCreator(MailboxCreator personalMailboxCreator) {
        this.personalMailboxCreator = personalMailboxCreator;
    }

}
