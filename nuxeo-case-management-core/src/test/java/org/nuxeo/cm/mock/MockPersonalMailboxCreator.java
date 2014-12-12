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

package org.nuxeo.cm.mock;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.MailboxCreator;
import org.nuxeo.cm.service.MailboxTitleGenerator;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Anahide Tchertchian
 */
public class MockPersonalMailboxCreator implements MailboxCreator {

    public String getPersonalMailboxId(DocumentModel userModel) {
        String userId = userModel.getId();
        return IdUtils.generateId(NuxeoPrincipal.PREFIX + userId, "-", true, 24);
    }

    @Override
    public MailboxTitleGenerator getTitleGenerator() {
        return null;
    }

    public List<Mailbox> createMailboxes(CoreSession session, String user) throws CaseManagementException {

        List<Mailbox> mailboxes = new ArrayList<Mailbox>();

        UserManager userManager = Framework.getService(UserManager.class);
        if (userManager == null) {
            throw new CaseManagementException("User manager not found");
        }
        DocumentModel userModel = userManager.getUserModel(user);
        if (userModel == null) {
            // no user by that name => wrong id or virtual user
            return null;
        }

        DocumentModel mailboxModel = session.createDocumentModel(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        Mailbox mailbox = mailboxModel.getAdapter(Mailbox.class);

        String userId = userModel.getId();
        mailbox.setId(getPersonalMailboxId(userModel));
        mailbox.setTitle(userId + "'s personal mailbox");
        mailbox.setType(MailboxConstants.type.personal.name());
        mailbox.setOwner(user);

        session.save();

        mailboxes.add(mailbox);

        return mailboxes;
    }

}
