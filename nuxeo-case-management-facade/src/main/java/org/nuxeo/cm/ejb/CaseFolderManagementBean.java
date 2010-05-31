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
 *    Mariana Cedica
 *
 * $Id$
 */
package org.nuxeo.cm.ejb;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderHeader;
import org.nuxeo.cm.service.CaseFolderManagementService;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

@Stateless
@Local(CaseFolderManagementService.class)
@Remote(CaseFolderManagementService.class)
public class CaseFolderManagementBean implements CaseFolderManagementService {

    private static final long serialVersionUID = -7789753806870594748L;

    private static final Log log = LogFactory.getLog(CaseFolderManagementBean.class);

    protected CaseFolderManagementService caseFolderManagementService;

    public List<CaseFolder> createPersonalCaseFolders(CoreSession session,
            String userId) {
        return getCaseFolderManagementService().createPersonalCaseFolders(
                session, userId);
    }

    public CaseFolder getCaseFolder(String muid) {
        return getCaseFolderManagementService().getCaseFolder(muid);
    }

    public CaseFolder getCaseFolder(CoreSession session, String muid) {
        return getCaseFolderManagementService().getCaseFolder(session, muid);
    }

    public CaseFolderHeader getCaseFolderHeader(String muid) {
        return getCaseFolderManagementService().getCaseFolderHeader(muid);
    }

    public List<CaseFolder> getCaseFolders(List<String> muids) {
        return getCaseFolderManagementService().getCaseFolders(muids);
    }

    public List<CaseFolder> getCaseFolders(CoreSession session,
            List<String> muids) {
        return getCaseFolderManagementService().getCaseFolders(session, muids);
    }

    public List<CaseFolderHeader> getCaseFoldersHeaders(List<String> muids) {
        return getCaseFolderManagementService().getCaseFoldersHeaders(muids);
    }

    public List<CaseFolderHeader> getCaseFoldersHeaders(CoreSession session,
            List<String> muids) {
        return getCaseFolderManagementService().getCaseFoldersHeaders(session,
                muids);
    }

    public List<CaseFolder> getUserCaseFolders(CoreSession session,
            String userId) {
        return getCaseFolderManagementService().getUserCaseFolders(session,
                userId);
    }

    public CaseFolder getUserPersonalCaseFolder(CoreSession session,
            String userId) {
        return getCaseFolderManagementService().getUserPersonalCaseFolder(
                session, userId);
    }

    public CaseFolder getUserPersonalCaseFolderForEmail(CoreSession session,
            String email) {
        return getCaseFolderManagementService().getUserPersonalCaseFolderForEmail(
                session, email);
    }

    public String getUserPersonalCaseFolderId(String user) {
        return getCaseFolderManagementService().getUserPersonalCaseFolderId(
                user);
    }

    public boolean hasCaseFolder(String muid) {
        return getCaseFolderManagementService().hasCaseFolder(muid);
    }

    public boolean hasUserPersonalCaseFolder(CoreSession session, String userId) {
        return getCaseFolderManagementService().hasUserPersonalCaseFolder(
                session, userId);
    }

    public List<CaseFolderHeader> searchCaseFolders(String pattern, String type) {
        return getCaseFolderManagementService().searchCaseFolders(pattern, type);
    }

    private CaseFolderManagementService getCaseFolderManagementService() {
        caseFolderManagementService = Framework.getLocalService(CaseFolderManagementService.class);
        if (caseFolderManagementService == null) {
            log.error("Unable to retreive CaseFolderManagementService");
            throw new ClientRuntimeException(
                    "Unable to retreive CaseFolderManagementService");
        }
        return caseFolderManagementService;
    }

}
