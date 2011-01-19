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

package org.nuxeo.correspondence.web.mailbox;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.LockableAdapter;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.mailbox.CaseManagementAbstractActionsBean;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Nicolas Ulrich
 *
 */
@Name("correspMailboxActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CorrespondenceMailboxActionsBean extends
        CaseManagementAbstractActionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected NavigationContext navigationContext;

    // FIXME: reset also when a new plan is created/deleted
    @Override
    protected void resetMailboxCache(Mailbox cachedMailbox, Mailbox newMailbox)
            throws ClientException {
    }

    public boolean isIncomingInitialEnvelope() throws ClientException {
        Case currentCase = getCurrentCase();
        if (currentCase != null && currentCase.isDraft()) {
            if (isIncomingEnvelope()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current envelope display an incoming mail
     *
     * @return true if it's an incoming mail
     * @throws ClientException
     */
    public boolean isIncomingEnvelope() throws ClientException {
        return hasCurrentCaseFacet(MailConstants.INCOMING_MAIL_FACET);
    }

    /**
     * @param facet
     * @return true if the first caseItem of the current envelope has the given
     *         facet.
     * @throws ClientException
     */
    public boolean hasCurrentCaseFacet(String facet) throws ClientException {

        CaseManagementDocumentTypeService correspDocumentTypeService = null;
        try {
            correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        } catch (Exception e) {
            /*
             * log.error("Could not retrieve CorrespondenceDocumentType Service",
             * e);
             */

        }

        Case currentEnvelope = getCurrentCase();
        if (currentEnvelope != null) {
            CaseItem item = currentEnvelope.getFirstItem(documentManager);
            if (item != null && correspDocumentTypeService != null) {
                return item.getDocument().hasFacet(facet);
            }
        }
        return false;

    }

    public String openDraft(String envelopeId) throws ClientException {

        DocumentModel envelopeDoc = documentManager.getDocument(new IdRef(
                envelopeId));
        Case envelope = envelopeDoc.getAdapter(Case.class);
        DocumentModel mailDoc = envelope.getFirstItem(documentManager).getDocument();
        if (mailDoc.hasFacet(MailConstants.OUTGOING_MAIL_FACET)) {
            // Edit the outgoing mail
            String view = navigationContext.navigateToDocument(envelopeDoc,
                    "edit_outgoing_envelope");
            // Lock the document
            LockableAdapter lockable = mailDoc.getAdapter(LockableAdapter.class);
            if (lockable.getDocumentLockDetails(documentManager).isEmpty()
                    && documentManager.hasPermission(
                            getCurrentCase().getDocument().getRef(),
                            SecurityConstants.WRITE)) {
                lockable.lockDocument(documentManager);
            }
            return view;

        } else if (mailDoc.hasFacet(MailConstants.INCOMING_MAIL_FACET)) {
            return navigationContext.navigateToId(envelopeId);

        }

        return null;

    }

}
