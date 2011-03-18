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
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.LockableAdapter;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.web.mailbox.CaseManagementAbstractActionsBean;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * @author Nicolas Ulrich
 */
@Name("correspDistributeOutgoingMailActionsBean")
@Scope(ScopeType.CONVERSATION)
public class CorrespondenceDistributeOutgoingMailActionsBean extends
        CaseManagementAbstractActionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected CaseDistributionService caseDistributionService;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    private transient CoreSession documentManager;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    public String backToEnvelope() throws ClientException {

        updateCurrentPost();

        Case envelope = getCurrentCase();

        return navigationContext.navigateToDocument(envelope.getDocument(),
                "edit_outgoing_envelope");

    }

    public String saveAndbackToMailBox() throws ClientException {

        updateCurrentPost();
        return backToMailBox();

    }

    public String backToMailBox() throws ClientException {

        // Unlock the outgoing mail
        LockableAdapter lockable = getCurrentCaseItem().getAdapter(
                LockableAdapter.class);
        lockable.unlockDocument(documentManager);

        Mailbox mb = getCurrentMailbox();
        TypeInfo typeInfo = mb.getDocument().getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();

    }

    /**
     * Save the Post Draft of the current envelope
     */
    public void updateCurrentPost() throws ClientException {

        try {
            DocumentModel postDoc = navigationContext.getChangeableDocument();
            CaseLink post = postDoc.getAdapter(CaseLink.class);
            post.save(documentManager);

            // some changes (versioning) happened server-side, fetch new one
            navigationContext.invalidateCurrentDocument();
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    resourcesAccessor.getMessages().get("document_modified"),
                    resourcesAccessor.getMessages().get(postDoc.getType()));

        } catch (Throwable t) {
            throw ClientException.wrap(t);
        }

    }

    public boolean displaySendButton() {

        DocumentModel postDoc = navigationContext.getCurrentDocument();
        CaseLink post = postDoc.getAdapter(CaseLink.class);

        return !post.getAllParticipants().isEmpty();

    }

    public String distribute() throws ClientException {

        // Check Mailbox
        Mailbox currentMailbox = getCurrentMailbox();

        if (currentMailbox == null) {
            facesMessages.add(
                    FacesMessage.SEVERITY_ERROR,
                    resourcesAccessor.getMessages().get(
                            "feedback.corresp.distribution.invalidCurrentMailbox"));
            return null;
        }

        // Check Envelope
        Case envelope = getCurrentCase();
        if (envelope == null) {
            facesMessages.add(FacesMessage.SEVERITY_ERROR,
                    resourcesAccessor.getMessages().get(
                            "feedback.corresp.distribution.invalidEnvelope"));
            return null;
        }

        // Get the draft post
        DocumentModel postDoc = navigationContext.getCurrentDocument();
        CaseLink post = postDoc.getAdapter(CaseLink.class);

        // Create a Post Request
        CaseLink postRequest = new CaseLinkRequestImpl(currentMailbox.getId(),
                Calendar.getInstance(),
                (String) envelope.getDocument().getPropertyValue(
                        CaseConstants.TITLE_PROPERTY_NAME), post.getComment(),
                envelope, post.getInitialInternalParticipants(),
                post.getInitialExternalParticipants());

        // Send envelope (initial)
        caseDistributionService.sendCase(documentManager, postRequest, true);
        envelope.save(documentManager);

        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(
                        "feedback.corresp.distribution.done"));

        // Go back to the envelope
        DocumentModel envelopeDoc = envelope.getDocument();
        return navigationContext.navigateToDocument(envelopeDoc);

    }

    public boolean canMarkCurrentSelectionAsSent() throws ClientException {
        if (!documentsListsManager.isWorkingListEmpty(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION)) {
            List<DocumentModel> workingList = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
            CaseLink post = null;
            Case envelope = null;
            CaseItem selectedItem = null;
            for (DocumentModel documentModel : workingList) {
                post = documentModel.getAdapter(CaseLink.class);
                if (post == null) {
                    return false;
                }
                envelope = post.getCase(documentManager);
                selectedItem = envelope.getFirstItem(documentManager);
                if (selectedItem == null) {
                    return false;
                }
                if (!selectedItem.getDocument().hasFacet(
                        CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}