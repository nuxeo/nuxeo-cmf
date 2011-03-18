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

package org.nuxeo.correspondence.web.outgoingmail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.LockableAdapter;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants.EventNames;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.web.caseitem.CaseManagementDocumentActions;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.mailbox.CaseManagementAbstractActionsBean;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Nicolas Ulrich
 */
@Name("correspEditOutgoingMailActionsBean")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CorrespondenceEditOutgoingMailActionsBean extends
        CaseManagementAbstractActionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected transient CaseManagementDocumentActions cmDocumentActions;

    @In(create = true)
    protected CaseDistributionService caseDistributionService;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    public String backToMailBox() throws ClientException {

        // Unlock the outgoing mail
        LockableAdapter lockable = getCurrentCaseItem().getAdapter(
                LockableAdapter.class);
        lockable.unlockDocument(documentManager);

        Mailbox mb = getCurrentMailbox();
        TypeInfo typeInfo = mb.getDocument().getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();

    }

    public String saveAndbackToMailBox() throws ClientException {

        updateCurrentEnvelope();
        return backToMailBox();

    }

    /**
     * Saves changes hold by the changeableDocument document model.
     */
    public void updateCurrentEnvelope() throws ClientException {

        // Save current envelope
        Case envelope = getCurrentCase();

        DocumentModel envelopeDoc = envelope.getDocument();
        envelope.save(documentManager);
        EventManager.raiseEventsOnDocumentChange(envelopeDoc);

        // Save the mail
        DocumentModel currentEmail = getCurrentCaseItem();
        currentEmail = documentManager.saveDocument(currentEmail);
        draftUpdated(currentEmail, envelope);
        EventManager.raiseEventsOnDocumentChange(currentEmail);

        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get("document_modified"),
                resourcesAccessor.getMessages().get(envelopeDoc.getType()));

    }

    protected void draftUpdated(DocumentModel currentEmail, Case envelope)
            throws ClientException {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        CaseLink draft = caseDistributionService.getDraftCaseLink(
                documentManager, getCurrentMailbox(),
                getCurrentCase().getDocument().getId());
        properties.put(CaseManagementEventConstants.EVENT_CONTEXT_DRAFT, draft);
        caseDistributionService.notify(documentManager,
                EventNames.draftUpdated.name(), envelope.getDocument(),
                properties);
    }

    public String createOutgoingMailDocumentInEnvelope() throws ClientException {
        cmDocumentActions.createCaseItemInCase();
        // get the created envelope and email
        DocumentModel envelope = getCurrentCase().getDocument();
        DocumentModel email = getCurrentCaseItem();

        // lock
        LockableAdapter lockableDocument = email.getAdapter(LockableAdapter.class);
        lockableDocument.lockDocument(documentManager);

        // redirect to edit view
        TypeInfo typeInfo = envelope.getAdapter(TypeInfo.class);
        return typeInfo.getView("edit_outgoing_envelope");
    }

    /**
     * Open the Draft Post in edit
     *
     * @throws Exception
     */
    public String openDistribution() throws Exception {

        // Save last updates
        updateCurrentEnvelope();

        // Open the Draft Post
        Mailbox mailbox = getCurrentMailbox();
        Case envelope = getCurrentCase();

        CaseDistributionService correspondenceService = Framework.getService(CaseDistributionService.class);

        CaseLink post = correspondenceService.getDraftCaseLink(documentManager,
                mailbox, envelope.getDocument().getId());

        return navigationContext.navigateToDocument(post.getDocument(),
                "distribution_outgoing_post");

    }

}
