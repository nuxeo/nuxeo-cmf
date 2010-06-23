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

package org.nuxeo.cm.core.event;

import org.nuxeo.ecm.classification.api.ClassificationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Listener for mailbox events that sets user/groups rights when mailbox is
 * created/edited.
 *
 * @author Anahide Tchertchian
 */
public class UpdateClassificationRootRightsListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }
        // set all rights to mailbox users

        final DocumentModel doc = docCtx.getSourceDocument();
        if (!ClassificationConstants.CLASSIFICATION_ROOT.equals(doc.getType())){
            return;
        }
        final String name = docCtx.getPrincipal().getName();
        CoreSession coreSession = docCtx.getCoreSession();
        new UnrestrictedSessionRunner(coreSession) {
            @Override
            public void run() throws ClientException {
                DocumentModel document = session.getDocument(doc.getRef());
                ACP acp = document.getACP();
                ACL acl = acp.getOrCreateACL(ACL.LOCAL_ACL);
                acl.add(new ACE(name, ClassificationConstants.CLASSIFY, true));
                acp.addACL(acl);
                document.setACP(acp, true);
                session.saveDocument(document);
            }
        }.runUnrestricted();
    }

}
