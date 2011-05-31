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

package org.nuxeo.cm.web.cases;

import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
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
import org.nuxeo.cm.web.distribution.CaseManagementDistributionActionsBean;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.trash.TrashService;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Nicolas Ulrich
 */
@Name("cmCaseActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
@Install(precedence = Install.FRAMEWORK)
public class CaseManagementCaseActionsBean extends
        CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseManagementDistributionActionsBean.class);

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(required = true, create = true)
    protected WebActions webActions;

    @In(required = true, create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected transient CaseDistributionService caseDistributionService;

    @In(required = false, create = true)
    protected transient DocumentsListsManager documentsListsManager;

    protected transient TrashService trashService;

    /**
     * @return true if this envelope is still in draft
     */
    public boolean isInitialCase() throws ClientException {
        Case env = getCurrentCase();

        if (env != null) {
            return getCurrentCase().isDraft();
        } else {
            return false;
        }
    }

    /**
     * Removes a mail from the current envelope.
     *
     * @param doc the mail to remove
     */
    public void removeCaseItem(DocumentModel doc) throws ClientException {
        Case currentEnvelope = getCurrentCase();
        CaseItem item = doc.getAdapter(CaseItem.class);
        currentEnvelope.removeCaseItem(item, documentManager);
    }

    public DocumentRoutingService getDocumentRoutingService() {
        try {
            return Framework.getService(DocumentRoutingService.class);

        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    protected void resetCaseCache(Case cachedEnvelope, Case newEnvelope)
            throws ClientException {
        super.resetCaseCache(cachedEnvelope, newEnvelope);
    }

    /**
     * Returns true if we have an empty case
     */
    public boolean isEmptyCase() throws ClientException {
        Case currentCase = getCurrentCase();
        if (currentCase != null) {
            return getCurrentCase().isEmpty();
        }
        return true;
    }

    public String markAsSent() throws ClientException {
        if (!documentsListsManager.isWorkingListEmpty(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION)) {
            List<DocumentModel> workingList = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
            CaseLink post = null;
            DocumentModel parentDoc = null;
            Mailbox parentMailbox = null;
            Case envelope = null;
            CaseLink postRequest = null;
            for (DocumentModel documentModel : workingList) {
                post = documentModel.getAdapter(CaseLink.class);
                parentDoc = documentManager.getParentDocument(post.getDocument().getRef());
                parentMailbox = parentDoc.getAdapter(Mailbox.class);
                envelope = post.getCase(documentManager);
                postRequest = new CaseLinkRequestImpl(parentMailbox.getId(),
                        post.getDate(),
                        (String) envelope.getDocument().getPropertyValue(
                                CaseConstants.TITLE_PROPERTY_NAME),
                        post.getComment(), envelope,
                        post.getInitialInternalParticipants(),
                        post.getInitialExternalParticipants());

                caseDistributionService.sendCase(documentManager, postRequest,
                        true);
                EventManager.raiseEventsOnDocumentChildrenChange(parentDoc);
            }
        }
        return null;
    }

    public String purgeCaseSelection() throws ClientException {
        if (!isEmptyDraft()) {
            List<DocumentModel> currentDraftCasesList = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
            purgeCaseSelection(currentDraftCasesList);
            EventManager.raiseEventsOnDocumentChildrenChange(getCurrentMailbox().getDocument());
        } else {
            log.debug("No documents selection in context to process delete on...");
        }
        return null;
    }

    public boolean isEmptyDraft() {
        return documentsListsManager.isWorkingListEmpty(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
    }

    public boolean getCanPurge() {
        List<DocumentModel> docs = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        if (docs.isEmpty()) {
            return false;
        }
        try {
            return getTrashService().canDelete(docs,
                    documentManager.getPrincipal(), false);
        } catch (ClientException e) {
            log.error("Cannot check delete permission", e);
            return false;
        }
    }

    protected void purgeCaseSelection(List<DocumentModel> workingList)
            throws ClientException {
        final List<DocumentRef> caseRefs = new ArrayList<DocumentRef>();
        final List<DocumentRef> postRefs = new ArrayList<DocumentRef>();
        for (DocumentModel documentModel : workingList) {
            CaseLink caselink = documentModel.getAdapter(CaseLink.class);
            try {
                caseRefs.add(caselink.getCase(documentManager).getDocument().getRef());
            } catch (Exception e) {
                // doc may not exist anymore
                log.error(e, e);
            }
            postRefs.add(documentModel.getRef());
        }
        new UnrestrictedSessionRunner(documentManager) {
            @Override
            public void run() throws ClientException {
                // permanently delete cases
                getTrashService().purgeDocuments(session, caseRefs);
                // permanently delete caseLinks
                getTrashService().purgeDocuments(session, postRefs);
            }
        }.runUnrestricted();
    }

    protected TrashService getTrashService() {
        if (trashService == null) {
            try {
                trashService = Framework.getService(TrashService.class);
            } catch (Exception e) {
                throw new RuntimeException("TrashService not available", e);
            }
        }
        return trashService;
    }

    public Boolean getCanEditCurrentCase() throws ClientException {
        Case currentCase = getCurrentCase();
        if (currentCase == null) {
            return false;
        }
        DocumentModel caseDoc = currentCase.getDocument();
        if (caseDoc == null) {
            return false;
        }
        LockableAdapter lockableCase = caseDoc.getAdapter(LockableAdapter.class);
        if (lockableCase.isLocked(documentManager)) {
            return false;
        }

        if (documentManager.hasPermission(caseDoc.getRef(),
                SecurityConstants.WRITE)) {
            return true;
        }

        return false;

    }

    public Boolean canCaseSelectionFollowTransition(String transition) {
        if (!isEmptyDraft()) {
            List<DocumentModel> currentDraftCasesList = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
            for (DocumentModel documentModel : currentDraftCasesList) {
                CaseLink caselink = documentModel.getAdapter(CaseLink.class);
                Case kase = caselink.getCase(documentManager);
                if (!kase.canFollowTransition(transition)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String followTranstionCaseSelection(String transition)
            throws ClientException {
        if (!isEmptyDraft()) {
            List<DocumentModel> currentDraftCasesList = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
            for (DocumentModel documentModel : currentDraftCasesList) {
                CaseLink caselink = documentModel.getAdapter(CaseLink.class);
                Case kase = caselink.getCase(documentManager);
                kase.followTransition(transition);
            }
            EventManager.raiseEventsOnDocumentChildrenChange(getCurrentMailbox().getDocument());
            webActions.resetTabList();
        } else {
            log.debug("No documents selection in context to process delete on...");
        }
        return null;
    }
}