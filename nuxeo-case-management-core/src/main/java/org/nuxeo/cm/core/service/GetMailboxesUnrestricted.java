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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;

/**
 * Get mailboxes using an unrestricted session and the given muids.
 *
 * @author Laurent Doguin
 */
public class GetMailboxesUnrestricted extends UnrestrictedSessionRunner {

    protected List<Mailbox> mailboxes = new ArrayList<Mailbox>();

    protected final List<String> muids;

    public GetMailboxesUnrestricted(CoreSession session, List<String> muids) {
        super(session);
        this.muids = muids;
    }

    public GetMailboxesUnrestricted(CoreSession session, String muid) {
        super(session);
        List<String> muids;
        if (muid == null) {
            muids = new ArrayList<String>(0);
        } else {
            muids = new ArrayList<String>(1);
            muids.add(muid);
        }
        this.muids = muids;
    }

    @Override
    public void run() throws ClientException {
        if (muids == null) {
            return;
        }
        MailboxManagementService service = null;
        try {
            service = Framework.getService(MailboxManagementService.class);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException(e);
        }
        if (service == null) {
            throw new ClientException("MailboxManagementService not found");
        }
        mailboxes = service.getMailboxes(session, muids);
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

}
