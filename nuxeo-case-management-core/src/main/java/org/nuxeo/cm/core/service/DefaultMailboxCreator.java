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
import org.nuxeo.cm.core.service.synchronization.DefaultPersonalMailboxTitleGenerator;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.service.MailboxCreator;
import org.nuxeo.cm.service.MailboxTitleGenerator;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Default creator for a personal mailbox
 *
 * @author Anahide Tchertchian
 */
public class DefaultMailboxCreator implements MailboxCreator {

    protected static final String CM_DEFAULT_MAILBOX_CREATOR_SKIP = "cm.defaultMailboxCreator.skip";

    private static final Log log = LogFactory.getLog(DefaultMailboxCreator.class);

    protected String getMailboxType() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        return correspDocumentTypeService.getMailboxType();
    }

    public String getPersonalMailboxId(DocumentModel userModel) {
        String userId = userModel.getId();
        return IdUtils.generateId(NuxeoPrincipal.PREFIX + userId, "-", true, 24);
    }

    @Override
    public MailboxTitleGenerator getTitleGenerator() {
        return new DefaultPersonalMailboxTitleGenerator();
    }

    public List<Mailbox> createMailboxes(CoreSession session, String user)
            throws CaseManagementException {

        String skipCreation = Framework.getProperty(CM_DEFAULT_MAILBOX_CREATOR_SKIP);
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
                log.debug(String.format("No User by that name. Maybe a wrong id or virtual user"));
                return Collections.emptyList();
            }

            // Create the personal mailbox for the user
            DocumentModel mailboxModel = session.createDocumentModel(getMailboxType());
            Mailbox mailbox = mailboxModel.getAdapter(Mailbox.class);

            // Set mailbox properties
            String id = getPersonalMailboxId(userModel);
            mailbox.setId(id);
            MailboxTitleGenerator gen = getTitleGenerator();
            if (gen == null) {
                // fallback on default title generator
                gen = new DefaultPersonalMailboxTitleGenerator();
            }
            mailbox.setTitle(gen.getMailboxTitle(userModel));
            mailbox.setOwner(user);
            mailbox.setType(MailboxConstants.type.personal.name());
            mailboxModel.setPathInfo(getMailboxParentPath(session),
                    getMailboxPathSegment(mailboxModel));
            // call hook method
            beforeMailboxCreation(session, mailbox, userModel);

            mailboxModel = session.createDocument(mailboxModel);
            // save because the mailbox will be queried just after in another
            // session
            session.save();
            mailbox = mailboxModel.getAdapter(Mailbox.class);

            return Collections.singletonList(mailbox);

        } catch (Exception e) {
            throw new CaseManagementException(
                    "Error during mailboxes creation", e);
        }
    }

    /**
     * Hook method to fill additional info on mailbox, or override other info
     */
    protected void beforeMailboxCreation(CoreSession session, Mailbox mailbox,
            DocumentModel userEntry) {
        // do nothing
    }

    protected String getMailboxParentPath(CoreSession session)
            throws ClientException {
        return getNewMailboxParentPath(session);
    }

    protected String getMailboxPathSegment(DocumentModel mailboxModel)
            throws ClientException {
        return getNewMailboxPathSegment(mailboxModel);
    }

    public static String getNewMailboxParentPath(CoreSession session)
            throws ClientException {
        // XXX: save it in first mailbox folder found for now
        DocumentModelList res = session.query(String.format("SELECT * from %s",
                MailboxConstants.MAILBOX_ROOT_DOCUMENT_TYPE));
        if (res == null || res.isEmpty()) {
            throw new CaseManagementException("Cannot find any mailbox folder");
        }
        return res.get(0).getPathAsString();
    }

    public static String getNewMailboxPathSegment(DocumentModel mailboxModel)
            throws ClientException {
        String baseid = mailboxModel.getTitle();
        if (baseid == null) {
            throw new ClientException("No base id for mailbox path creation");
        }
        return IdUtils.generateId(baseid, "-", true, 24);
    }

}
