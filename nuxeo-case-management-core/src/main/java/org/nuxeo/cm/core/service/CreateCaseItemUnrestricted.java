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

import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;

/**
 * Creates a new caseItem document from a given document model
 *
 * @author arussel
 */
public class CreateCaseItemUnrestricted extends UnrestrictedSessionRunner {

    protected final DocumentModel doc;

    protected final ACP caseACP;

    protected DocumentRef ref;

    protected final String parentPath;

    public CreateCaseItemUnrestricted(CoreSession session, DocumentModel doc, ACP caseACP, String parentPath)
            throws ClientException {
        super(session);
        if (doc.getId() != null) {
            // don't detach if not yet created
            doc.detach(true);
        }
        this.doc = doc;
        this.caseACP = caseACP;
        this.parentPath = parentPath;
    }

    @Override
    public void run() throws ClientException {
        DocumentModel newDoc = session.createDocumentModel(parentPath, doc.getName(), doc.getType());
        newDoc.copyContent(doc);
        newDoc = session.createDocument(newDoc);
        ACP acp = newDoc.getACP();
        ACL acl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
        List<ACE> aces = caseACP.getACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
        acl.addAll(aces == null ? new ArrayList<ACE>() : aces);
        acp.addACL(acl);
        session.setACP(newDoc.getRef(), acp, true);
        ref = newDoc.getRef();
    }

    public DocumentRef getDocRef() {
        return ref;
    }

}
