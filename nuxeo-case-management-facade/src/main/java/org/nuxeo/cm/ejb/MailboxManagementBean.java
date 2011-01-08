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
 *    Mariana Cedica
 *
 * $Id$
 */
package org.nuxeo.cm.ejb;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

@Stateless
@Local(LocalMailboxManagementService.class)
@Remote(RemoteMailboxManagementService.class)
public class MailboxManagementBean implements MailboxManagementService {

    private static final long serialVersionUID = -7789753806870594748L;

    private static final Log log = LogFactory.getLog(MailboxManagementBean.class);

    protected MailboxManagementService mailboxManagementService;

    public List<Mailbox> createPersonalMailboxes(CoreSession session,
            String userId) {
        return getMailboxManagementService().createPersonalMailboxes(
                session, userId);
    }

    public Mailbox getMailbox(String muid) {
        return getMailboxManagementService().getMailbox(muid);
    }

    public Mailbox getMailbox(CoreSession session, String muid) {
        return getMailboxManagementService().getMailbox(session, muid);
    }

    public MailboxHeader getMailboxHeader(String muid) {
        return getMailboxManagementService().getMailboxHeader(muid);
    }

    public List<Mailbox> getMailboxes(List<String> muids) {
        return getMailboxManagementService().getMailboxes(muids);
    }

    public List<Mailbox> getMailboxes(CoreSession session,
            List<String> muids) {
        return getMailboxManagementService().getMailboxes(session, muids);
    }

    public List<MailboxHeader> getMailboxesHeaders(List<String> muids) {
        return getMailboxManagementService().getMailboxesHeaders(muids);
    }

    public List<MailboxHeader> getMailboxesHeaders(CoreSession session,
            List<String> muids) {
        return getMailboxManagementService().getMailboxesHeaders(session,
                muids);
    }

    public List<Mailbox> getUserMailboxes(CoreSession session,
            String userId) {
        return getMailboxManagementService().getUserMailboxes(session,
                userId);
    }

    public Mailbox getUserPersonalMailbox(CoreSession session,
            String userId) {
        return getMailboxManagementService().getUserPersonalMailbox(
                session, userId);
    }

    public Mailbox getUserPersonalMailboxForEmail(CoreSession session,
            String email) {
        return getMailboxManagementService().getUserPersonalMailboxForEmail(
                session, email);
    }

    public String getUserPersonalMailboxId(String user) {
        return getMailboxManagementService().getUserPersonalMailboxId(
                user);
    }

    public boolean hasMailbox(String muid) {
        return getMailboxManagementService().hasMailbox(muid);
    }

    public boolean hasUserPersonalMailbox(CoreSession session, String userId) {
        return getMailboxManagementService().hasUserPersonalMailbox(
                session, userId);
    }

    public List<MailboxHeader> searchMailboxes(String pattern, String type) {
        return getMailboxManagementService().searchMailboxes(pattern, type);
    }

    private MailboxManagementService getMailboxManagementService() {
        mailboxManagementService = Framework.getLocalService(MailboxManagementService.class);
        if (mailboxManagementService == null) {
            log.error("Unable to retreive MailboxManagementService");
            throw new ClientRuntimeException(
                    "Unable to retreive MailboxManagementService");
        }
        return mailboxManagementService;
    }

}
