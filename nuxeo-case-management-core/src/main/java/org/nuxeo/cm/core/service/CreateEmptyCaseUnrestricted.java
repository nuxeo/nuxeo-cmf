/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     mcedica
 */
package org.nuxeo.cm.core.service;

import java.util.List;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * Creates an empty case from a given detached documentModel
 */
public class CreateEmptyCaseUnrestricted extends UnrestrictedSessionRunner {

    protected final String parentPath;

    protected final List<Mailbox> mailboxes;

    protected DocumentModel caseDoc;

    public CreateEmptyCaseUnrestricted(CoreSession session,
            DocumentModel caseDoc, String parentPath, List<Mailbox> mailbox) {
        super(session);
        this.caseDoc = caseDoc;
        this.mailboxes = mailbox;
        this.parentPath = parentPath;
    }

    @Override
    public void run() throws ClientException {
        PathSegmentService pathSegmentService;
        try {
            pathSegmentService = Framework.getService(PathSegmentService.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
        String caseName = pathSegmentService.generatePathSegment(caseDoc);
        caseDoc.setPathInfo(parentPath, caseName);
        caseDoc = session.createDocument(caseDoc);
        ACP acp = caseDoc.getACP();
        ACL acl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
        for (Mailbox mailbox : mailboxes) {
            acl.add(new ACE(CaseManagementSecurityConstants.MAILBOX_PREFIX
                    + mailbox.getId(), SecurityConstants.READ_WRITE, true));
        }
        acp.addACL(acl);
        session.setACP(caseDoc.getRef(), acp, true);
    }

    public DocumentRef getEmptyCaseDocumentRef() {
        return caseDoc.getRef();
    }

}
