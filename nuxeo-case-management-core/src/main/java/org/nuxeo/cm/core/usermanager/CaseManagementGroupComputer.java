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
 *     Anahide Tchertchian
 */
package org.nuxeo.cm.core.usermanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfoCallbackHandler;
import org.nuxeo.ecm.platform.computedgroups.AbstractGroupComputer;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Group computer for case management, adding mailboxes ids to the user virtual
 * groups so that it can be used for permissions resolution.
 *
 * @author Anahide Tchertchian
 */
public class CaseManagementGroupComputer extends AbstractGroupComputer {

    public static final ThreadLocal<Boolean> disableRetrieveMailboxes = new ThreadLocal<Boolean>();

    protected static MailboxManagementService cfms;

    /**
     * Returns an empty list for efficiency
     */
    @Override
    public List<String> getAllGroupIds() throws Exception {
        // should handle computed virtual groups?
        return Collections.emptyList();
    }

    /**
     * Returns an empty list as mailboxes are not searchable
     */
    @Override
    public List<String> searchGroups(Map<String, Serializable> filter,
            Set<String> fulltext) throws Exception {
        // should handle computed virtual groups?
        return Collections.emptyList();
    }

    @Override
    public List<String> getGroupMembers(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getMailboxManager(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxMembers();
    }

    /**
     * Method called at startup to compute users groups, so returns an empty
     * list when local thread is flagged.
     */
    @Override
    public List<String> getGroupsForUser(NuxeoPrincipalImpl pal)
            throws Exception {
        if (pal != null) {
            if (!Boolean.TRUE.equals(disableRetrieveMailboxes.get())) {
                disableRetrieveMailboxes.set(Boolean.TRUE);
                CoreSession session = null;
                LoginContext loginContext = null;
                boolean isNewTransactionStarted = false;
                try {
                    final String username = pal.getName();
                    loginContext = loginOnContext(username);
                    session = openCoreSession(username);
                    // TODO: optimize, retrieving ids directly on service (?)
                    if (!TransactionHelper.isTransactionActive()) {
                        isNewTransactionStarted = TransactionHelper.startTransaction();
                    }
                    List<Mailbox> mailboxes = getMailboxManager().getUserMailboxes(
                            session, pal.getName());
                    List<String> res = new ArrayList<String>();
                    if (mailboxes != null) {
                        for (Mailbox folder : mailboxes) {
                            res.add(CaseManagementSecurityConstants.MAILBOX_PREFIX
                                    + folder.getId());
                        }
                    }
                    String userMailboxId = CaseManagementSecurityConstants.MAILBOX_PREFIX
                            + getMailboxManager().getUserPersonalMailboxId(
                                    username);
                    if (userMailboxId != null && !res.contains(userMailboxId)) {
                        res.add(userMailboxId);
                    }
                    // add virtual groups used for mailboxes rights
                    List<String> vgroups = getVirtualGroupsForMailboxHierarchy(pal);
                    if (vgroups != null) {
                        res.addAll(vgroups);
                    }
                    return res;
                } finally {
                    if (isNewTransactionStarted) {
                        TransactionHelper.commitOrRollbackTransaction();
                    }
                    closeCoreSession(session);
                    disableRetrieveMailboxes.remove();
                    if (loginContext != null) {
                        loginContext.logout();
                    }
                }
            }

        }

        return Collections.emptyList();
    }

    protected List<String> getVirtualGroupsForMailboxHierarchy(
            NuxeoPrincipal pal) throws ClientException {
        UserManager um = getUM();
        List<String> res = new ArrayList<String>();

        Set<String> checkedGroups = new HashSet<String>();
        List<String> groupsToProcess = new ArrayList<String>();
        groupsToProcess.addAll(pal.getGroups());

        while (!groupsToProcess.isEmpty()) {
            String groupName = groupsToProcess.remove(0);
            if (!checkedGroups.contains(groupName)) {
                checkedGroups.add(groupName);
                NuxeoGroup nxGroup = null;
                if (um != null) {
                    nxGroup = um.getGroup(groupName);
                }
                if (nxGroup != null) {
                    // add member groups instead
                    groupsToProcess.addAll(nxGroup.getMemberGroups());
                    // add prefix
                    res.add(CaseManagementSecurityConstants.MAILBOX_GROUP_PREFIX
                            + nxGroup.getName());
                }
            }
        }
        return res;
    }

    @Override
    public List<String> getParentsGroupNames(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getMailboxManager(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxParentNames();
    }

    @Override
    public List<String> getSubGroupsNames(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getMailboxManager(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxSubFolderNames();
    }

    /**
     * Return false: no mailbox should be seen as a group
     */
    @Override
    public boolean hasGroup(String name) throws Exception {
        // should handle computed virtual groups?
        return false;
    }

    protected MailboxManagementService getMailboxManager() {
        if (cfms == null) {
            cfms = Framework.getLocalService(MailboxManagementService.class);
        }
        return cfms;
    }

    protected LoginContext loginOnContext(String username)
            throws LoginException {
        if (!Framework.isTestModeSet()) {
            // bind to the jaas context
            UserIdentificationInfo userIdent = new UserIdentificationInfo(
                    username, "");
            userIdent.setLoginPluginName("Trusting_LM");
            CallbackHandler handler = new UserIdentificationInfoCallbackHandler(
                    userIdent);
            LoginContext loginContext = new LoginContext("nuxeo-ecm-web",
                    handler);
            loginContext.login();
            return loginContext;
        }
        return null;
    }

    protected CoreSession openCoreSession(String username) {
        try {
            if (!Framework.isTestModeSet()) {
                username = null;
                // open core session as given user only for tests
            }
            return CoreInstance.openCoreSession(getRepoName(), username);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
    }

    protected void closeCoreSession(CoreSession session) {
        if (session != null) {
            session.close();
        }
    }

    protected String getRepoName() {
        RepositoryManager repositoryManager = Framework.getLocalService(RepositoryManager.class);
        return repositoryManager.getDefaultRepositoryName();
    }

}
