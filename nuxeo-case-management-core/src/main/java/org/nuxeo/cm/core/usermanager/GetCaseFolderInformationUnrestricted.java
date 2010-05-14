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
 *     arussel
 */
package org.nuxeo.cm.core.usermanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.service.CaseFolderManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

/**
 * Returns a case folder information using an unrestricted core session
 *
 * @author arussel
 * @author Anahide Tchertchian
 */
public class GetCaseFolderInformationUnrestricted extends
        UnrestrictedSessionRunner {

    protected CaseFolderManagementService service;

    protected String caseFolderId;

    protected List<String> members;

    protected List<String> parents;

    protected List<String> children;

    public GetCaseFolderInformationUnrestricted(String repoName,
            CaseFolderManagementService service, String caseFolderId) {
        super(repoName);
        this.service = service;
        this.caseFolderId = caseFolderId;
    }

    @Override
    public void run() throws ClientException {
        // make sure all variables are reset
        members = null;
        parents = null;
        children = null;

        CaseFolder caseFolder = service.getCaseFolder(session, caseFolderId);
        if (caseFolder != null) {
            members = caseFolder.getAllUsers();
            final String parentId = caseFolder.getParentId(session);
            if (parentId != null) {
                parents = new ArrayList<String>();
                parents.add(parentId);
            }
            children = caseFolder.getChildrenIds(session);
        }
    }

    public List<String> getCaseFolderMembers() {
        if (members == null) {
            members = Collections.emptyList();
        }
        return members;
    }

    public List<String> getCaseFolderParentNames() {
        if (parents == null) {
            parents = Collections.emptyList();
        }
        return parents;
    }

    public List<String> getCaseFolderSubFolderNames() {
        if (children == null) {
            children = Collections.emptyList();
        }
        return children;
    }

}
