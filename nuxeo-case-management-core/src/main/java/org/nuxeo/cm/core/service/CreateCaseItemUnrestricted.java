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
package org.nuxeo.cm.core.service;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
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

/**
 * 
 * Creates a new caseItem document from a given document model
 * 
 * @author arussel
 */
public class CreateCaseItemUnrestricted extends UnrestrictedSessionRunner {

    protected final DocumentModel doc;

    protected List<CaseFolder> mailboxes;

    protected Case kase;

    protected DocumentRef ref;

    public CreateCaseItemUnrestricted(CoreSession session, DocumentModel doc,
            List<CaseFolder> mailboxes) {
        super(session);
        this.doc = doc;
        this.mailboxes = mailboxes;
    }

    public CreateCaseItemUnrestricted(CoreSession session, DocumentModel doc,
            Case kase) {
        super(session);
        this.doc = doc;
        this.kase = kase;
    }

    @Override
    public void run() throws ClientException {
        DocumentModel newDoc = session.createDocument(doc);
        newDoc.copyContent(doc);
        ACP acp = newDoc.getACP();
        ACL acl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_CASE_FOLDER_PREFIX);
        addACL(acl);
        acp.addACL(acl);
        session.setACP(newDoc.getRef(), acp, true);
        ref = newDoc.getRef();
        session.save();
    }

    private void addACL(ACL acl) {
        if (mailboxes != null) {
            for (CaseFolder mailbox : mailboxes) {
                acl.add(new ACE(
                        CaseManagementSecurityConstants.CASE_FOLDER_PREFIX
                                + mailbox.getId(),
                        SecurityConstants.READ_WRITE, true));
            }
        } else {
            try {
                List<ACE> aces = kase.getDocument().getACP().getACL(CaseManagementSecurityConstants.ACL_CASE_FOLDER_PREFIX);
                acl.addAll(aces == null ? new ArrayList<ACE>() : aces);
            } catch (ClientException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }
    }

    public DocumentRef getDocRef() {
        return ref;
    }

}
