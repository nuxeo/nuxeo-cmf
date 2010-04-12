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

import java.util.List;

import org.nuxeo.cm.mailbox.CaseFolderConstants;
import org.nuxeo.cm.mailbox.CaseFolderHeader;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * Searches mailboxes headers using an unrestricted session
 * <p>
 * Mailboxes have to be searchable to be able to select recipients, but senders
 * may not have access to them.
 *
 * @author Laurent Doguin
 *
 */
public class SearchMailboxesHeadersUnrestricted extends
        SearchMailboxesUnrestricted {

    protected List<CaseFolderHeader> mailboxesHeaders;

    public SearchMailboxesHeadersUnrestricted(CoreSession session,
            String pattern, String type) {
        super(session, pattern, type);
    }

    @Override
    public void run() throws ClientException {
        try {
            DocumentModelList res = queryMailboxes();
            mailboxesHeaders = CaseFolderConstants.getMailboxHeaderList(res);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public List<CaseFolderHeader> getMailboxesHeaders() {
        return mailboxesHeaders;
    }

}
