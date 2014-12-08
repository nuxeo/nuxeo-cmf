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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.core.service;

import java.util.List;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;

/**
 * Set the right WRITE for all the users of the mailbox, in unrestricted mode, and removes the whole local ACL when
 * there are no users for this mailbox.
 *
 * @author nulrich
 * @author ldoguin
 */
public class SetMailboxAclUnrestricted extends UnrestrictedSessionRunner {

    // Mailbox document model
    protected final DocumentRef ref;

    protected final String permission;

    public SetMailboxAclUnrestricted(CoreSession session, DocumentRef ref, String permission) {
        super(session);
        this.ref = ref;
        if (permission == null) {
            throw new RuntimeException("Permission cannot be null");
        }
        this.permission = permission;
    }

    public SetMailboxAclUnrestricted(CoreSession session, DocumentRef ref) {
        this(session, ref, SecurityConstants.READ_WRITE);
    }

    @Override
    public void run() throws ClientException {
        DocumentModel doc = session.getDocument(ref);
        Mailbox mb = doc.getAdapter(Mailbox.class);
        List<String> total = mb.getAllUsersAndGroups();
        if (total != null && !total.isEmpty()) {
            ACL localACL = new ACLImpl(ACL.LOCAL_ACL);
            // set a specific ACL for groups so that mailbox hierarchy is
            // preserved, see NXCM-499
            List<String> users = mb.getAllUsers();
            if (users != null && !users.isEmpty()) {
                for (String user : users) {
                    ACE ace = new ACE(user, permission, true);
                    localACL.add(ace);
                }
            }
            List<String> groups = mb.getGroups();
            if (groups != null && !groups.isEmpty()) {
                for (String group : groups) {
                    ACE ace = new ACE(CaseManagementSecurityConstants.MAILBOX_GROUP_PREFIX + group, permission, true);
                    localACL.add(ace);
                }
            }
            ACP acp = doc.getACP();
            acp.removeACL(ACL.LOCAL_ACL);
            acp.addACL(localACL);
            doc.setACP(acp, true);
        } else {
            // remove all local rights
            ACP acp = doc.getACP();
            acp.removeACL(ACL.LOCAL_ACL);
            doc.setACP(acp, true);
        }
    }

}
