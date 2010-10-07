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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.core.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.runtime.api.Framework;

/**
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class RouteSecurityUpdaterListener implements EventListener {
    @SuppressWarnings("unchecked")
    public void handleEvent(Event event) throws ClientException {
        EventContext eventCtx = event.getContext();

        Object envelopeObject = eventCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_CASE);
        if (!(envelopeObject instanceof Case)) {
            return;
        }
        Case kase = (Case) envelopeObject;
        DocumentModel kaseDoc = kase.getDocument();
        CoreSession session = eventCtx.getCoreSession();
        @SuppressWarnings("rawtypes")
        Map<String, List<String>> recipients = (Map) eventCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_INTERNAL_PARTICIPANTS);
        if (recipients == null) {
            return;
        }
        Set<String> allMailboxIds = new HashSet<String>();
        for (Map.Entry<String, List<String>> recipient : recipients.entrySet()) {
            allMailboxIds.addAll(recipient.getValue());
        }
        List<DocumentRoute> relatedRoutes = getDocumentRoutingService().getDocumentRoutesForAttachedDocument(
                session, kaseDoc.getId());
        if (!allMailboxIds.isEmpty()) {
            for (DocumentRoute route : relatedRoutes) {
                DocumentModel routeDoc = route.getDocument();
                ACP acp = routeDoc.getACP();
                ACL routeACL = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
                List<ACE> newACEs = new ArrayList<ACE>();
                for (String mailboxId : allMailboxIds) {
                    newACEs.add(new ACE(
                            CaseManagementSecurityConstants.MAILBOX_PREFIX
                                    + mailboxId, SecurityConstants.READ, true));
                }
                routeACL.addAll(newACEs);
                acp.removeACL(CaseManagementSecurityConstants.ACL_MAILBOX_PREFIX);
                acp.addACL(routeACL);
                session.setACP(kaseDoc.getRef(), acp, true);
            }
        }
    }

    protected DocumentRoutingService getDocumentRoutingService() {
        try {
            return Framework.getService(DocumentRoutingService.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
