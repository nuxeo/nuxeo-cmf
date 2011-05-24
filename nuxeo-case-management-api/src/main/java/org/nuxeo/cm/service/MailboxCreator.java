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

package org.nuxeo.cm.service;

import java.util.List;

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Interface for creation of personal mailbox.
 *
 * @author Anahide Tchertchian
 */
public interface MailboxCreator {

    String getPersonalMailboxId(DocumentModel userModel);

    /**
     * Returns the title generator to use when creating the personal mailbox
     *
     * @since 1.7
     */
    MailboxTitleGenerator getTitleGenerator();

    List<Mailbox> createMailboxes(CoreSession session, String user)
            throws CaseManagementException;

}
