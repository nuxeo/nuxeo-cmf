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

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfo;
import org.nuxeo.ecm.platform.api.login.UserIdentificationInfoCallbackHandler;
import org.nuxeo.ecm.platform.computedgroups.AbstractGroupComputer;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.runtime.api.Framework;

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
    public List<String> getAllGroupIds() throws Exception {
        return Collections.emptyList();
    }

    /**
     * Returns an empty list as mailboxes are not searchable
     */
    @Override
    public List<String> searchGroups(Map<String, Serializable> filter,
            HashSet<String> fulltext) throws Exception {
        return Collections.emptyList();
    }

    public List<String> getGroupMembers(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getService(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxMembers();
    }

    /**
     * Method called at startup to compute users groups, so returns an empty
     * list when local thread is flagged.
     */
    public List<String> getGroupsForUser(NuxeoPrincipalImpl nuxeoPrincipal)
            throws Exception {
        if (nuxeoPrincipal != null) {

            if (!Boolean.TRUE.equals(disableRetrieveMailboxes.get())) {
                disableRetrieveMailboxes.set(true);
                CoreSession session = null;
                LoginContext loginContext = null;
                try {
                    final String username = nuxeoPrincipal.getName();
                    loginContext = loginOnContext(username);
                    session = openCoreSession(username);
                    // TODO: optimize, retrieving ids directly on service (?)
                    List<Mailbox> mailboxes = getService().getUserMailboxes(
                            session, nuxeoPrincipal.getName());

                    List<String> res = new ArrayList<String>();
                    if (mailboxes != null) {
                        for (Mailbox folder : mailboxes) {
                            res.add(CaseManagementSecurityConstants.MAILBOX_PREFIX
                                    + folder.getId());
                        }
                    }
                    return res;
                } finally {
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

    public List<String> getParentsGroupNames(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getService(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxParentNames();
    }

    public List<String> getSubGroupsNames(String groupName) throws Exception {
        GetMailboxInformationUnrestricted runner = new GetMailboxInformationUnrestricted(
                getRepoName(), getService(), groupName);
        runner.runUnrestricted();
        return runner.getMailboxSubFolderNames();
    }

    /**
     * Return false: no mailbox should be seen as a group
     */
    @Override
    public boolean hasGroup(String name) throws Exception {
        return false;
    }

    protected MailboxManagementService getService() {
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
            String repositoryName = getRepoName();
            Repository repository = Framework.getService(
                    RepositoryManager.class).getRepository(repositoryName);
            if (repository == null) {
                throw new ClientException("Cannot get repository: "
                        + repositoryName);
            }
            Map<String, Serializable> context = new HashMap<String, Serializable>();
            if (Framework.isTestModeSet()) {
                // open core session as given user for tests
                context.put("username", username);
            }
            return repository.open(context);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
    }

    protected void closeCoreSession(CoreSession session) {
        if (session != null) {
            Repository.close(session);
        }
    }

    protected String getRepoName() {
        RepositoryManager mgr = null;
        try {
            mgr = Framework.getService(RepositoryManager.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }
        if (mgr == null) {
            throw new CaseManagementRuntimeException(
                    "Unable to find Repository Manager.");
        }
        Repository repo = mgr.getDefaultRepository();
        return repo.getName();
    }

}
