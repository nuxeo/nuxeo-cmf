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

package org.nuxeo.cm.core.event;

import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * Create a route root document as a child of the mailBox, in unrestricted
 * mode.
 *
 * @author ldoguin
 */
public class CreateMailboxRouteRootUnrestricted extends
        UnrestrictedSessionRunner {

    // Mailbox document model
    protected final DocumentModel doc;

    public CreateMailboxRouteRootUnrestricted(CoreSession session,
            DocumentModel doc) {
        super(session);
        this.doc = doc;
    }

    @Override
    public void run() throws ClientException {
        Mailbox mb = doc.getAdapter(Mailbox.class);
        String routeRootName = getRouteRootNamePrefix() + mb.getTitle();
        String routeFolderId = IdUtils.generateId(routeRootName, "-", true,
                24);
        DocumentModel routeRoot = session.createDocumentModel(
                doc.getPathAsString(), routeFolderId,
                MailboxConstants.ROUTE_ROOT_DOCUMENT_TYPE);
        routeRoot.setPropertyValue(MailboxConstants.TITLE_FIELD,
                routeRootName);
        routeRoot = session.createDocument(routeRoot);
        ACP acp = routeRoot.getACP();
        ACL acl = acp.getOrCreateACL(ACL.LOCAL_ACL);
        acl.add(new ACE(CaseManagementSecurityConstants.MAILBOX_PREFIX
                + mb.getId(), SecurityConstants.EVERYTHING, true));
        acp.addACL(acl);
        routeRoot.setACP(acp, true);
        session.saveDocument(routeRoot);
    }

    public static String getRouteRootNamePrefix() {
        return "Routes - ";
    }

}
