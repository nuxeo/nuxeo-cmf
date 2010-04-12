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

import org.nuxeo.cm.mailbox.CaseFolder;
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
 * Set the right WRITE for all the users of the mailbox, in unrestricted mode.
 *
 * @author nulrich
 * @author ldoguin
 *
 */
public class SetCaseFolderAclUnrestricted extends UnrestrictedSessionRunner {

    // Mailbox document model
    protected DocumentRef ref;

    public SetCaseFolderAclUnrestricted(CoreSession session, DocumentRef ref) {
        super(session);
        this.ref = ref;
    }

    @Override
    public void run() throws ClientException {
        DocumentModel doc = session.getDocument(ref);
        CaseFolder mb = doc.getAdapter(CaseFolder.class);
        ACL localACL = new ACLImpl(ACL.LOCAL_ACL);
        ACL mailACL = new ACLImpl(mb.getId());
        List<String> total = mb.getAllUsersAndGroups();
        if (!total.isEmpty()) {
            ACE ace;
            for (String user : total) {
                ace = new ACE(user, SecurityConstants.READ_WRITE, true);
                localACL.add(ace);
                mailACL.add(ace);
            }
            ACP acp = doc.getACP();
            acp.removeACL(ACL.LOCAL_ACL);
            acp.addACL(localACL);
            doc.setACP(acp, true);
        }
        session.save(); // this save is necessary in unit test
    }

}
