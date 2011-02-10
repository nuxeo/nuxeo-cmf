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

package org.nuxeo.cm.web.caseitem;

import java.util.Collections;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.LockableAdapter;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.web.CaseManagementWebConstants;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinModes;
import org.nuxeo.ecm.platform.preview.seam.PreviewActionBean;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.UserAction;
import org.nuxeo.ecm.webapp.action.TypesTool;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * @author Anahide Tchertchian
 *
 */
@Name("cmDocumentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CaseManagementContextBound
public class CaseItemDocumentActionsBean extends
        CaseManagementContextBoundInstance implements
        CaseManagementDocumentActions {

    private static final String DOCUMENT_SAVED = "document_saved";

    private static final String DOCUMENT_MODIFIED = "document_modified";

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected TypeManager typeManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient CaseDistributionService caseDistributionService;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient PreviewActionBean previewActions;

    @In(create = true)
    protected TypesTool typesTool;

    protected Boolean editingMail = false;

    /**
     * Returns the create view of given document type.
     */
    public String createDocument(String typeName) throws ClientException {
        Type docType = typeManager.getType(typeName);
        // we cannot use typesTool as intermediary since the DataModel callback
        // will alter whatever type we set
        typesTool.setSelectedType(docType);
        try {
            DocumentModel changeableDocument = documentManager.createDocumentModel(typeName);
            changeableDocument.putContextData(
                    CaseManagementWebConstants.CREATE_NEW_CASE_KEY, true);
            navigationContext.setChangeableDocument(changeableDocument);
            return navigationContext.getActionResult(changeableDocument,
                    UserAction.CREATE);
        } catch (Throwable t) {
            throw ClientException.wrap(t);
        }
    }

    public String createCaseItemInCase() throws ClientException {
        DocumentModel emailDoc = navigationContext.getChangeableDocument();
        // The new mail
        Case kase = getCurrentCase();
        if (kase != null
                && emailDoc.getContextData(CaseManagementWebConstants.CREATE_NEW_CASE_KEY) == null) {
            // adding a case item in a case
            CaseItem newCaseItem = caseDistributionService.addCaseItemToCase(
                    documentManager, kase, emailDoc);
            emailDoc = newCaseItem.getDocument();
            emailDoc.setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                    CaseConstants.DOCUMENT_DEFAULT_CASE_ID,
                    getCurrentCase().getDocument().getId());
            documentManager.saveDocument(emailDoc);
            documentManager.save();
            TypeInfo typeInfo = kase.getDocument().getAdapter(TypeInfo.class);
            return typeInfo.getDefaultView();
        }
        Mailbox currentMailbox = getCurrentMailbox();
        // creating a case item in a case
        Case envelope = caseDistributionService.createCase(documentManager,
                emailDoc, Collections.singletonList(currentMailbox));
        emailDoc = envelope.getFirstItem(documentManager).getDocument();
        emailDoc.setProperty(CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_ID,
                envelope.getDocument().getId());
        documentManager.saveDocument(emailDoc);
        // Create the Draft post in the mailbox
        caseDistributionService.createDraftCaseLink(documentManager,
                getCurrentMailbox(), envelope);
        documentManager.save();
        Events.instance().raiseEvent(
                EventNames.DOCUMENT_CHILDREN_CHANGED,
                documentManager.getDocument(currentMailbox.getDocument().getRef()));
        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(DOCUMENT_SAVED),
                resourcesAccessor.getMessages().get(emailDoc.getType()));

        // Navigate to the created envelope
        DocumentModel envelopeDocModel = envelope.getDocument();
        navigationContext.navigateToDocument(envelopeDocModel);

        TypeInfo typeInfo = envelopeDocModel.getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();
    }

    public String createEmptyCase() throws ClientException {
        DocumentModel caseDoc = navigationContext.getChangeableDocument();
        Mailbox currentMailbox = getCurrentMailbox();
        Case emptyCase = caseDistributionService.createEmptyCase(
                documentManager, caseDoc, currentMailbox);
        caseDistributionService.createDraftCaseLink(documentManager,
                getCurrentMailbox(), emptyCase);
        documentManager.save();
        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(DOCUMENT_SAVED),
                resourcesAccessor.getMessages().get(caseDoc.getType()));
        Events.instance().raiseEvent(
                EventNames.DOCUMENT_CHILDREN_CHANGED,
                documentManager.getDocument(currentMailbox.getDocument().getRef()));
        caseDoc = emptyCase.getDocument();
        navigationContext.setCurrentDocument(caseDoc);
        TypeInfo typeInfo = caseDoc.getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();
    }

    protected DocumentModel getParentFolder() throws ClientException {
        return caseDistributionService.getParentDocumentForCase(documentManager);
    }

    public boolean getCanEditCurrentCaseItem() throws ClientException {

        DocumentModel currentEmail = getCurrentCaseItem();

        if (currentEmail == null) {
            return false;
        }

        LockableAdapter lockableMail = currentEmail.getAdapter(LockableAdapter.class);
        if (lockableMail.isLocked(documentManager)) {
            return false;
        }

        if (documentManager.hasPermission(currentEmail.getRef(),
                SecurityConstants.WRITE)) {
            return true;
        }

        return false;
    }

    /**
     * Saves changes hold by the changeableDocument document model.
     */
    public void updateCurrentCaseItem() throws ClientException {
        DocumentModel currentEmailDoc = getCurrentCaseItem();
        CaseItem currentEmail = currentEmailDoc.getAdapter(CaseItem.class);
        currentEmail.save(documentManager);
        documentManager.save();
        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(DOCUMENT_MODIFIED),
                resourcesAccessor.getMessages().get(currentEmail.getType()));
        EventManager.raiseEventsOnDocumentChange(currentEmail.getDocument());
    }

    public boolean isCurrentCaseItemPreviewAvailable() throws ClientException {
        if (getCurrentCase() == null || getCurrentCase().isEmpty()) {
            return false;
        }
        DocumentModel currentEmail = getCurrentCaseItem();
        if (currentEmail != null) {
            return previewActions.documentHasPreview(currentEmail);
        }
        return false;
    }

    public boolean isEditingCaseItem() throws ClientException {
        DocumentModel caseItem = getCurrentCaseItem();
        if (caseItem == null) {
            return false;
        }
        LockableAdapter lockable = caseItem.getAdapter(LockableAdapter.class);
        if (lockable.isLockedByCurrentUser(documentManager)) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(editingMail);
    }

    public void startEditingCaseItem() throws ClientException {
        LockableAdapter lockable = getCurrentCaseItem().getAdapter(
                LockableAdapter.class);
        if (documentManager.hasPermission(getCurrentCaseItem().getRef(),
                SecurityConstants.WRITE)) {
            if (!lockable.isLockedByCurrentUser(documentManager)) {
                lockable.lockDocument(documentManager);
            }
            editingMail = true;
        }
    }

    public void quitEditingMail() throws ClientException {
        editingMail = false;
        LockableAdapter lockable = getCurrentCaseItem().getAdapter(
                LockableAdapter.class);
        if (lockable.isLockedByCurrentUser(documentManager)) {
            lockable.unlockDocument(documentManager);
        }
    }

    public String getCaseItemLayoutMode() throws ClientException {
        // view by default
        String mode = BuiltinModes.VIEW;
        if (isEditingCaseItem()) {
            DocumentModel currentEmail = getCurrentCaseItem();
            DocumentRef currentEmailRef = currentEmail.getRef();
            if (documentManager.hasPermission(currentEmailRef,
                    SecurityConstants.WRITE)) {
                mode = BuiltinModes.EDIT;
            }
        }
        return mode;
    }

    public String backToMailbox() throws ClientException {
        quitEditingMail();
        return returnToDocView();
    }

    protected String returnToDocView() throws ClientException {
        DocumentModel doc = getCurrentCase().getDocument();
        TypeInfo typeInfo = doc.getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();
    }

    public String save() throws ClientException {
        quitEditingMail();
        updateCurrentCaseItem();
        return returnToDocView();
    }

    @Override
    protected void resetCurrentCaseItemCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        editingMail = false;
    }

}
