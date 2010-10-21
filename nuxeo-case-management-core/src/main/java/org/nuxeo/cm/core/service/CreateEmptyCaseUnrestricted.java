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

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * Creates an empty case from a given detached documentModel
 * */
public class CreateEmptyCaseUnrestricted extends UnrestrictedSessionRunner {

    protected final String parentPath;

    protected final Mailbox mailbox;

    protected DocumentModel caseDoc;

    public CreateEmptyCaseUnrestricted(CoreSession session,
            DocumentModel caseDoc, String parentPath, Mailbox mailbox) {
        super(session);
        this.caseDoc = caseDoc;
        this.mailbox = mailbox;
        this.parentPath = parentPath;
    }

    @Override
    public void run() throws ClientException {
        String caseTitle = (String) caseDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        caseDoc.setPathInfo(parentPath, caseTitle);
        caseDoc = session.createDocument(caseDoc);
        caseDoc = session.saveDocument(caseDoc);
        ACP acp = caseDoc.getACP();
        ACL acl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
        acl.add(new ACE(CaseManagementSecurityConstants.MAILBOX_PREFIX
                + mailbox.getId(), SecurityConstants.READ_WRITE, true));
        acp.addACL(acl);
        session.setACP(caseDoc.getRef(), acp, true);
        caseDoc = session.saveDocument(caseDoc);
    }

    public DocumentModel getEmptyCaseDocument() {
        return caseDoc;
    }

}
