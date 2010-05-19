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
 *
 * $Id$
 */

package org.nuxeo.cm.core.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.casefolder.CaseFolderImpl;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.CaseFolderCreator;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author Anahide Tchertchian
 */
public class DefaultCaseFolderCreator implements CaseFolderCreator {

    protected static final String CM_DEFAULT_CASE_FOLDER_CREATOR_SKIP = "cm.defaultCaseFolderCreator.skip";

    private static final Log log = LogFactory.getLog(DefaultCaseFolderCreator.class);

    public String getPersonalCaseFolderId(DocumentModel userModel) {
        String userId = userModel.getId();
        return IdUtils.generateId(NuxeoPrincipal.PREFIX + userId);
    }

    public List<CaseFolder> createCaseFolders(CoreSession session, String user)
    throws CaseManagementException {

        String skipCreation = Framework.getProperty(CM_DEFAULT_CASE_FOLDER_CREATOR_SKIP);
        if (skipCreation != null
                && skipCreation.equals(Boolean.TRUE.toString())) {
            return Collections.emptyList();
        }

        try {

            // Retrieve the user
            UserManager userManager = Framework.getService(UserManager.class);
            if (userManager == null) {
                throw new CaseManagementException("User manager not found");
            }

            DocumentModel userModel = userManager.getUserModel(user);
            if (userModel == null) {
                log.warn(String.format("No User by that name. Maybe a wrong id or virtual user"));
                return Collections.emptyList();
            }

            // Create the personal mailbox for the user
            DocumentModel mailboxModel = session.createDocumentModel(getCaseFolderType());
            CaseFolder mailbox = mailboxModel.getAdapter(CaseFolder.class);

            // Set mailbox properties
            mailbox.setId(getPersonalCaseFolderId(userModel));
            mailbox.setTitle(getUserDisplayName(userModel));
            mailbox.setOwner(user);
            mailbox.setType(CaseFolderConstants.type.personal.name());

            // XXX: save it in first mailbox folder found for now
            DocumentModelList res = session.query(String.format(
                    "SELECT * from %s",
                    CaseFolderConstants.CASE_FOLDER_ROOT_DOCUMENT_TYPE));
            if (res == null || res.isEmpty()) {
                throw new CaseManagementException(
                "Cannot find any mailbox folder");
            }

            mailboxModel.setPathInfo(res.get(0).getPathAsString(),
                    IdUtils.generateId(mailbox.getTitle()));
            mailboxModel = session.createDocument(mailboxModel);
            mailbox = mailboxModel.getAdapter(CaseFolder.class);

            session.save();
            return Collections.singletonList(mailbox);

        } catch (Exception e) {
            throw new CaseManagementException(
                    "Error during mailboxes creation", e);
        }
    }

    protected String getUserSchemaName() {
        return "user";
    }

    protected String getUserDisplayName(DocumentModel userModel)
    throws ClientException {
        String schemaName = getUserSchemaName();
        String first = (String) userModel.getProperty(schemaName, "firstName");
        String last = (String) userModel.getProperty(schemaName, "lastName");
        if (first == null || first.length() == 0) {
            if (last == null || last.length() == 0) {
                return userModel.getId();
            } else {
                return last;
            }
        } else {
            if (last == null || last.length() == 0) {
                return first;
            } else {
                return first + ' ' + last;
            }
        }
    }

    private String getCaseFolderType() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return correspDocumentTypeService.getCaseFolderType();
    }
}
