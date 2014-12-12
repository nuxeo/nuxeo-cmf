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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;

/**
 * Searches mailboxes using an unrestricted session
 * <p>
 * Mailboxes have to be searchable to be able to select recipients, but senders may not have access to them.
 *
 * @author Anahide Tchertchian
 * @author Laurent Doguin
 */
public class SearchMailboxesUnrestricted extends UnrestrictedSessionRunner {

    private static final Log log = LogFactory.getLog(SearchMailboxesUnrestricted.class);

    protected List<Mailbox> mailboxes;

    protected final String pattern;

    protected final String type;

    public SearchMailboxesUnrestricted(CoreSession session, String pattern, String type) {
        super(session);
        this.pattern = pattern;
        this.type = type;
    }

    @Override
    public void run() throws ClientException {
        DocumentModelList res = queryMailboxes();
        mailboxes = MailboxConstants.getMailboxList(res);
    }

    protected DocumentModelList queryMailboxes() throws ClientException {
        String query = String.format("SELECT * FROM %s WHERE %s ILIKE '%%%s%%' AND ecm:currentLifeCycleState != '%s'",
                getMailboxType(), MailboxConstants.TITLE_FIELD, pattern, MailboxConstants.MAILBOX_DELETED_STATE);
        if (type != null) {
            query += String.format(" AND %s='%s'", MailboxConstants.TYPE_FIELD, type);
        }
        if (log.isDebugEnabled()) {
            log.debug(query);
        }
        return session.query(query);
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

    private String getMailboxType() throws ClientException {
        CaseManagementDocumentTypeService correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        return correspDocumentTypeService.getMailboxType();
    }

}
