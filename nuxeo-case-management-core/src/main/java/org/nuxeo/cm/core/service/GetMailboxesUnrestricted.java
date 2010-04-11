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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModel;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModelService;
import org.nuxeo.ecm.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
import org.nuxeo.runtime.api.Framework;


/**
 * Get mailboxes using an unrestricted session and the given muids.
 *
 * @author Laurent Doguin
 *
 */
public class GetMailboxesUnrestricted extends UnrestrictedSessionRunner {

    private static final Log log = LogFactory.getLog(GetMailboxesUnrestricted.class);

    private static final String QUERY_GET_MAILBOX_FROM_ID = "GET_CASE_FOLDER_FROM_ID";

    protected List<Mailbox> mailboxes;

    final protected List<String> muids;

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
        List<DocumentModel> docs = getMailboxesDocumentModel(muids);
        mailboxes = MailboxConstants.getMailboxList(docs);
    }

    protected List<DocumentModel> getMailboxesDocumentModel(List<String> muids)
            throws ClientException {

        if (muids == null || muids.isEmpty()) {
            return null;
        }

        List<DocumentModel> docs = new ArrayList<DocumentModel>();
        try {
            QueryModelService qmService = Framework.getService(QueryModelService.class);
            QueryModelDescriptor qmd = qmService.getQueryModelDescriptor(QUERY_GET_MAILBOX_FROM_ID);
            QueryModel qm = new QueryModel(qmd);

            for (String muid : muids) {

                DocumentModelList res = qm.getDocuments(session,
                        new Object[] { muid });

                if (res.size() > 1) {
                    log.warn(String.format(
                            "Several mailboxes with id %s, returning first found",
                            muid));
                }
                if (res.size() > 0) {
                    docs.add(res.get(0));
                }
            }
            return docs;
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

}
