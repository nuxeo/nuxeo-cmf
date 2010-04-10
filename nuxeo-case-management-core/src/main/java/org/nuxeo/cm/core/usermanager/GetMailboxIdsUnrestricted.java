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
import java.util.List;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;


/**
 * @author arussel
 * 
 */
public class GetMailboxIdsUnrestricted extends UnrestrictedSessionRunner {

    protected CorrespondenceService service;

    protected List<String> mailboxIds = new ArrayList<String>();

    protected String principalId;

    public GetMailboxIdsUnrestricted(String repoName,
            CorrespondenceService service, String principalId) {
        super(repoName);
        this.principalId = principalId;
        this.service = service;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nuxeo.ecm.core.api.UnrestrictedSessionRunner#run()
     */
    @Override
    public void run() throws ClientException {
        List<Mailbox> mailboxes = service.getUserMailboxes(session, principalId);
        for(Mailbox mailbox : mailboxes) {
            mailboxIds.add(mailbox.getId());
        }
    }

    public List<String> getMailboxIds() {
        return mailboxIds;
    }

}
