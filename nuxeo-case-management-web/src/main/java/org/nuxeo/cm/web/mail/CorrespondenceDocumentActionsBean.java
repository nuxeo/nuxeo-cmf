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

package org.nuxeo.cm.web.mail;

import java.util.Collections;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.GetParentPathUnrestricted;
import org.nuxeo.cm.cases.LockableAdapter;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.MailEnvelope;
import org.nuxeo.cm.cases.MailEnvelopeItem;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBound;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinModes;
import org.nuxeo.ecm.platform.preview.seam.PreviewActionBean;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.UserAction;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;


/**
 * @author Anahide Tchertchian
 * 
 */
@Name("correspDocumentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CorrespondenceContextBound
public class CorrespondenceDocumentActionsBean extends
        CorrespondenceContextBoundInstance implements
        CorrespondenceDocumentActions {

    private static final String DOCUMENT_SAVED = "document_saved";

    private static final String DOCUMENT_MODIFIED = "document_modified";

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient CorrespondenceService correspondenceService;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true)
    protected transient PreviewActionBean previewActions;

    protected Boolean editingMail = false;

    public String createMailDocumentInEnvelope() throws ClientException {
        // The new mail
        DocumentModel emailDoc = navigationContext.getChangeableDocument();

        String parentPath = getParentFolderPath();

        MailEnvelope envelope = correspondenceService.createMailEnvelope(
                documentManager, emailDoc, parentPath, Collections.singletonList(getCurrentMailbox()));
        emailDoc.setProperty(CaseConstants.MAIL_DOCUMENT_SCHEMA,
                CaseConstants.DOCUMENT_DEFAULT_CASE_FOLDER_ID,
                envelope.getDocument().getId());
        documentManager.saveDocument(emailDoc);
        // Create the Draft post in the mailbox
        correspondenceService.createDraftPost(documentManager,
                getCurrentMailbox(), envelope);
        documentManager.save();

        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(DOCUMENT_SAVED),
                resourcesAccessor.getMessages().get(emailDoc.getType()));

        // Navigate to the created envelope
        DocumentModel envelopeDocModel = envelope.getDocument();
        navigationContext.navigateToDocument(envelopeDocModel);

        TypeInfo typeInfo = envelopeDocModel.getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();

    }

    protected String getParentFolderPath() throws ClientException {
        GetParentPathUnrestricted runner = new GetParentPathUnrestricted(documentManager);
        runner.runUnrestricted();
        return runner.getParentPath();
    }

    public String getEmailLayoutMode() throws ClientException {
        if (getCanEditCurrentEmail()) {
            return BuiltinModes.EDIT;
        } else {
            return BuiltinModes.VIEW;
        }
    }

    public boolean getCanEditCurrentEmail() throws ClientException {

        DocumentModel currentEmail = getCurrentEmail();

        if (currentEmail == null)
            return false;

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
    public void updateCurrentEmail() throws ClientException {
        DocumentModel currentEmailDoc = getCurrentEmail();
        MailEnvelopeItem currentEmail = currentEmailDoc.getAdapter(MailEnvelopeItem.class);
        currentEmail.save(documentManager);
        documentManager.save();
        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(DOCUMENT_MODIFIED),
                resourcesAccessor.getMessages().get(currentEmail.getType()));
        EventManager.raiseEventsOnDocumentChange(currentEmail.getDocument());

    }

    public boolean isCurrentEmailPreviewAvailable() throws ClientException {
        DocumentModel currentEmail = getCurrentEmail();
        if (currentEmail != null) {
            return previewActions.documentHasPreview(currentEmail);
        }
        return false;
    }

    public boolean isEditingMail() throws ClientException {
        LockableAdapter lockable = getCurrentEmail().getAdapter(
                LockableAdapter.class);
        if (lockable.isLockedByCurrentUser(documentManager)) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(editingMail);
    }

    public void startEditingMail() throws ClientException {
        LockableAdapter lockable = getCurrentEmail().getAdapter(
                LockableAdapter.class);
        if (documentManager.hasPermission(getCurrentEmail().getRef(),
                SecurityConstants.WRITE)) {
            if (!lockable.isLockedByCurrentUser(documentManager)) {
                lockable.lockDocument(documentManager);
            }
            editingMail = true;
        }

    }

    public void quitEditingMail() throws ClientException {
        editingMail = false;
        LockableAdapter lockable = getCurrentEmail().getAdapter(
                LockableAdapter.class);
        if (lockable.isLockedByCurrentUser(documentManager)) {
            lockable.unlockDocument(documentManager);
        }
    }

    public String getMailLayoutMode() throws ClientException {
        // view by default
        String mode = BuiltinModes.VIEW;
        if (isEditingMail()) {
            DocumentModel currentEmail = getCurrentEmail();
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
        DocumentModel doc = getCurrentEmail();
        TypeInfo typeInfo = doc.getAdapter(TypeInfo.class);
        return typeInfo.getDefaultView();
    }

    public void save() throws ClientException {
        quitEditingMail();
        updateCurrentEmail();

    }

    @Override
    protected void resetCurrentEmailCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        editingMail = false;
    }

    public String reply() throws ClientException {

        DocumentModel emailDoc = getCurrentEmail();
        DocumentModel reply = correspondenceService.getReplyDocument(
                documentManager, getCurrentMailbox(), emailDoc);

        // Set changeable document
        navigationContext.setChangeableDocument(reply);

        // Redirect to the creation form
        return navigationContext.getActionResult(reply, UserAction.CREATE);

    }

}
