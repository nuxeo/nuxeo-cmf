/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.cm.core.usermanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.cm.service.CaseManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * Principal that will returns mailboxes as well as groups, useful for rights
 * resolution.
 * 
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class CaseManagementPrincipalImpl extends NuxeoPrincipalImpl {

    private static final Log log = LogFactory.getLog(CaseManagementPrincipalImpl.class);

    public CaseManagementPrincipalImpl(String name, boolean isAnonymous,
            boolean isAdministrator, boolean updateAllGroups)
            throws ClientException {
        super(name, isAnonymous, isAdministrator, updateAllGroups);
    }

    private static final long serialVersionUID = -354913792689270461L;

    @Override
    public List<String> getGroups() {
        List<String> groups = super.getGroups();
        try {
            CaseManagementService correspondenceService = Framework.getService(CaseManagementService.class);
            GetCaseFolderIdsUnrestricted runner = new GetCaseFolderIdsUnrestricted(
                    getRepoName(), correspondenceService, getPrincipalId());
            runner.runUnrestricted();
            List<String> mailboxIds = runner.getMailboxIds();
            for (String mailboxId : mailboxIds) {
                groups.add(CaseManagementSecurityConstants.CASE_FOLDER_PREFIX
                        + mailboxId);
            }
            return groups;
        } catch (Exception e) {
            log.error(
                    "Could not get principal mailboxes from CorrespondenceService",
                    e);
        }
        return groups;
    }

    @Override
    public void updateAllGroups() throws ClientException {
        UserManager userManager;
        try {
            userManager = Framework.getService(UserManager.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        Set<String> checkedGroups = new HashSet<String>();
        List<String> groupsToProcess = new ArrayList<String>();
        List<String> resultingGroups = new ArrayList<String>();
        groupsToProcess.addAll(getGroups());

        while (!groupsToProcess.isEmpty()) {
            String groupName = groupsToProcess.remove(0);
            if (!checkedGroups.contains(groupName)) {
                checkedGroups.add(groupName);
                NuxeoGroup nxGroup = null;
                if (userManager != null) {
                    nxGroup = userManager.getGroup(groupName);
                }
                if (nxGroup == null) {
                    if (virtualGroups.contains(groupName)) {
                        // just add the virtual group as is
                        resultingGroups.add(groupName);
                    } else if(userManager != null && groupName.startsWith(CaseManagementSecurityConstants.CASE_FOLDER_PREFIX)) {
                        log.debug("Adding a maibox group: " + groupName);
                        resultingGroups.add(groupName);
                    } else if (userManager != null && !groupName.startsWith(CaseManagementSecurityConstants.CASE_FOLDER_PREFIX)) {
                        // XXX this should only happens in case of
                        // inconsistency in DB
                        log.error("User " + getName() + " references the "
                                + groupName + " group that does not exists");
                    }
                } else {
                    groupsToProcess.addAll(nxGroup.getParentGroups());
                    resultingGroups.add(groupName);
                    // XXX: maybe remove group from virtual groups if it
                    // actually exists? otherwise it would be ignored when
                    // setting groups
                }
            }
        }

        allGroups = new ArrayList<String>(resultingGroups);

        // set isAdministrator boolean according to groups declared on user
        // manager
        if (!isAdministrator() && userManager != null) {
            List<String> adminGroups = userManager.getAdministratorsGroups();
            for (String adminGroup : adminGroups) {
                if (allGroups.contains(adminGroup)) {
                    isAdministrator = true;
                    break;
                }
            }
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
