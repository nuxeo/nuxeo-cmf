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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.correspondence.web.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.web.CaseManagementWebConstants;
import org.nuxeo.cm.web.caseitem.CaseItemDocumentActionsBean;
import org.nuxeo.cm.web.distribution.CaseManagementDistributionActionsBean;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.correspondence.core.service.CorrespondenceDocumentTypeService;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.UserAction;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Anahide Tchertchian
 */
@Name("correspDocumentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CaseManagementContextBound
public class CorrespondenceDocumentActionsBean extends CaseManagementContextBoundInstance implements
        CorrespondenceDocumentActions {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient CaseItemDocumentActionsBean cmDocumentActions;

    @In(create = true)
    protected transient CaseManagementDistributionActionsBean cmDistributionActions;

    @In(create = true, required = false)
    protected transient DistributionInfo distributionInfo;

    private static final String REP_SUFFIX = "Rep: ";

    public DocumentModel getReplyDocument(CoreSession session, Mailbox mailbox, DocumentModel receivedMail)
            throws ClientException {

        CaseItem receivedItem = receivedMail.getAdapter(CaseItem.class);

        Map<String, Object> context = new HashMap<String, Object>();

        // Set the path of MailRoot
        context.put(CoreEventConstants.PARENT_PATH, CaseConstants.CASE_ROOT_DOCUMENT_PATH);
        context.put(CaseManagementEventConstants.EVENT_CONTEXT_MAILBOX_ID, mailbox.getId());
        context.put(CaseManagementEventConstants.EVENT_CONTEXT_AFFILIATED_MAILBOX_ID, mailbox.getAffiliatedMailboxId());

        // Use the Correspondence Type Service to retrieve the used outgoing
        // mail core type
        CorrespondenceDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        // Create the new Mail document model in the MailRoot
        DocumentModel responseMail = session.createDocumentModel(correspDocumentTypeService.getOutgoingDocType(),
                context);
        // Set recipients
        responseMail.setPropertyValue(CaseConstants.CONTACTS_PARTICIPANTS,
                receivedMail.getPropertyValue(CaseConstants.CONTACTS_SENDERS));

        // Set senders
        NuxeoPrincipal nxp = (NuxeoPrincipal) session.getPrincipal();

        // FIXME: should be configurable
        HashMap<String, String> senderItem = new HashMap<String, String>();
        senderItem.put("name", nxp.getFirstName());
        senderItem.put("surname", nxp.getLastName());
        senderItem.put("service", nxp.getCompany());

        List<HashMap<String, String>> senders = new ArrayList<HashMap<String, String>>();
        senders.add(senderItem);
        responseMail.setPropertyValue(CaseConstants.CONTACTS_SENDERS, (Serializable) senders);

        // Set the title
        CaseItem responseItem = responseMail.getAdapter(CaseItem.class);
        responseItem.setTitle(REP_SUFFIX + receivedItem.getTitle());

        // Set the answered document id
        responseMail.setPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID, receivedMail.getId());

        return responseMail;
    }

    public String reply() throws ClientException {

        DocumentModel emailDoc = getCurrentCaseItem();
        DocumentModel reply = getReplyDocument(documentManager, getCurrentMailbox(), emailDoc);
        reply.putContextData(CaseManagementWebConstants.CREATE_NEW_CASE_KEY, Boolean.TRUE);
        // Set changeable document
        navigationContext.setChangeableDocument(reply);
        // Redirect to the creation form
        return navigationContext.getActionResult(reply, UserAction.CREATE);

    }

    public String createAndDistributeIncomingDocument() throws ClientException {
        DistributionInfo distributionInfos = distributionInfo;
        if (cmDistributionActions.validateDistributionParticipants()) {
            cmDocumentActions.createCaseItemInDefaultCase();
            return cmDistributionActions.validateWizard(distributionInfos);
        }
        return null;
    }

    public String saveAndDistributeIncomingDocument() throws ClientException {
        DistributionInfo distributionInfos = distributionInfo;
        if (cmDistributionActions.validateDistributionParticipants()) {
            if (cmDocumentActions.isEditingCaseItem()) {
                cmDocumentActions.quitEditingMail();
                cmDocumentActions.updateCurrentCaseItem();
            }
            return cmDistributionActions.validateWizard(distributionInfos);
        }
        return null;
    }

}
