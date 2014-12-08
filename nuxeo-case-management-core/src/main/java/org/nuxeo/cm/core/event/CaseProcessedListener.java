/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id$
 */

package org.nuxeo.cm.core.event;

import java.util.List;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Remove Write permission to all users once a case is processed.
 * 
 * @author Laurent Doguin
 */
public class CaseProcessedListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        EventContext eventCtx = event.getContext();
        String transiton = (String) eventCtx.getProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_TRANSITION);
        if (!CaseLifeCycleConstants.TRANSITION_PROCESS.equals(transiton)) {
            return;
        }
        DocumentEventContext docCtx = null;
        if (eventCtx instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) eventCtx;
        } else {
            return;
        }
        DocumentModel kaseDoc = docCtx.getSourceDocument();
        Case kase = kaseDoc.getAdapter(Case.class);
        if (kase == null) {
            return;
        }
        try {
            RemoveWritePermissionUnrestricted session = new RemoveWritePermissionUnrestricted(
                    eventCtx.getCoreSession(), kase);
            session.runUnrestricted();
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e.getMessage(), e);
        }

    }

    public static class RemoveWritePermissionUnrestricted extends UnrestrictedSessionRunner {

        protected final Case kase;

        public RemoveWritePermissionUnrestricted(CoreSession session, Case kase) {
            super(session);
            this.kase = kase;
        }

        @Override
        public void run() throws ClientException {
            List<DocumentModel> items = kase.getDocuments();
            items.add(kase.getDocument());
            ACP acp;
            ACL processedAcl;
            for (DocumentModel doc : items) {
                acp = doc.getACP();
                processedAcl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_PROCESSED_CASE_NAME);
                processedAcl.add(new ACE(SecurityConstants.EVERYONE, SecurityConstants.WRITE_LIFE_CYCLE, true));
                processedAcl.add(new ACE(SecurityConstants.EVERYONE, SecurityConstants.WRITE, false));
                acp.removeACL(CaseManagementSecurityConstants.ACL_PROCESSED_CASE_NAME);
                acp.addACL(0, processedAcl);
                session.setACP(doc.getRef(), acp, true);
            }
        }

    }

}