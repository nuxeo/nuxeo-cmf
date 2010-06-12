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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.core.service;

import java.util.List;

import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * Get mailboxes headers using an unrestricted session and the given muids.
 *
 * @author Laurent Doguin
 */
public class GetMailboxesHeadersUnrestricted extends GetMailboxesUnrestricted {

    protected List<MailboxHeader> mailboxesHeaders;

    public GetMailboxesHeadersUnrestricted(CoreSession session,
            List<String> muids) {
        super(session, muids);
    }

    @Override
    public void run() throws ClientException {
        List<DocumentModel> docs = getMailboxesDocumentModel(muids);
        mailboxesHeaders = MailboxConstants.getMailboxHeaderList(docs);
    }

    public List<MailboxHeader> getMailboxesHeaders() {
        return mailboxesHeaders;
    }

}
