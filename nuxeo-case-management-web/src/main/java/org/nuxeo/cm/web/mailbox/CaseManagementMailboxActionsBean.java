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

package org.nuxeo.cm.web.mailbox;

import static org.jboss.seam.ScopeType.EVENT;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.UserAction;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.pagination.ResultsProvidersCache;

/**
 * Handles mailboxes creation/edition and views.
 */
@Name("cmMailboxActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CaseManagementMailboxActionsBean extends
        CaseManagementAbstractActionsBean {

    private static final long serialVersionUID = 1L;

    protected static String MAILBOX_INBOX = "MAILBOX_INBOX";

    protected static String MAILBOX_SERVICE = "MAILBOX_SERVICE";

    protected static String MAILBOX_SENT = "MAILBOX_SENT";

    protected static String MAILBOX_DRAFT = "MAILBOX_DRAFT";

    protected static String MAILBOX_PLANS = "MAILBOX_PLANS";

    protected static final Log log = LogFactory.getLog(CaseManagementMailboxActionsBean.class);

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true)
    protected transient MailboxManagementService mailboxManagementService;

    @In(required = false)
    protected transient Principal currentUser;

    protected List<Mailbox> userMailboxes;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @RequestParameter
    protected String mailboxSuggestionSearchType;

    protected String parentMailboxId;

    public Object getMailboxSuggestions(Object input) throws ClientException {
        String searchPattern = (String) input;
        String searchType = mailboxSuggestionSearchType;
        if (searchType == null || StringUtils.isEmpty(searchType)) {
            searchType = null;
        }
        return mailboxManagementService.searchMailboxes(documentManager, searchPattern,
                searchType);
    }

    /**
     * Returns all mailboxes for logged user.
     */
    @Factory(value = "userMailboxes", scope = EVENT)
    public List<Mailbox> getUserMailboxes() throws CaseManagementException {
        if (userMailboxes == null) {
            userMailboxes = new ArrayList<Mailbox>();
            if (currentUser != null) {
                Mailbox personalMailbox = null;
                List<Mailbox> mailboxes = mailboxManagementService.getUserMailboxes(
                        documentManager, currentUser.getName());
                if (mailboxes != null && !mailboxes.isEmpty()) {
                    userMailboxes.addAll(mailboxes);
                    for (Iterator<Mailbox> it = userMailboxes.iterator(); it.hasNext();) {
                        Mailbox mbox = it.next();
                        if (MailboxConstants.type.personal.name().equals(
                                mbox.getType())
                                && currentUser.getName().equals(mbox.getOwner())) {
                            personalMailbox = mbox;
                            it.remove();
                            break;
                        }
                    }
                }
                // Sort mailboxes: personal mailbox comes first, then
                // alphabetical order is used
                Collections.sort(userMailboxes, new Comparator<Mailbox>() {
                    public int compare(Mailbox o1, Mailbox o2) {
                        return o1.getTitle().compareTo(o2.getTitle());
                    }
                });
                if (personalMailbox != null) {
                    userMailboxes.add(0, personalMailbox);
                }
            }
        }
        return userMailboxes;
    }

    /**
     * Performs a validation error when trying to set a mailbox id that already
     * exists in the system.
     */
    public void validateMailboxId(FacesContext context,
            UIComponent component, Object value) {
        if (!(value instanceof String)
                || mailboxManagementService.hasMailbox(documentManager, (String) value)) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    ComponentUtils.translate(context,
                            "feedback.casemanagement.mailboxIdAlreadyExists"),
                    null);
            // also add global message?
            // context.addMessage(null, message);
            throw new ValidatorException(message);
        }
    }

    /**
     * Performs a validation error when trying to create a personal mailbox for
     * a user that already has one.
     */
    public void validatePersonalMailboxCreation(FacesContext context,
            UIComponent component, Object value) {
        Map<String, Object> attributes = component.getAttributes();
        String mailboxTypeInputId = (String) attributes.get("mailboxTypeInputId");
        String mailboxOwnerInputId = (String) attributes.get("mailboxOwnerInputId");
        if (mailboxTypeInputId == null || mailboxOwnerInputId == null) {
            log.error("Cannot validate personal mailbox creation: input id(s) not found");
            return;
        }

        UIInput mailboxTypeComp = (UIInput) component.findComponent(mailboxTypeInputId);
        UIInput mailboxOwnerComp = (UIInput) component.findComponent(mailboxOwnerInputId);
        if (mailboxTypeComp == null || mailboxOwnerComp == null) {
            log.error("Cannot validate personal mailbox creation: input(s) not found");
            return;
        }

        Object mailboxType = mailboxTypeComp.getLocalValue();
        Object mailboxOwner = mailboxOwnerComp.getLocalValue();

        if (mailboxType == null || mailboxOwner == null) {
            log.error("Cannot validate personal mailbox creation: value(s) not found");
            return;
        }

        if (MailboxConstants.type.personal.name().equals(mailboxType)) {
            String mbId = mailboxManagementService.getUserPersonalMailboxId((String) mailboxOwner);
            if (mailboxManagementService.hasMailbox(documentManager, mbId)) {
                FacesMessage message = new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        ComponentUtils.translate(context,
                                "feedback.casemanagement.personalMailboxAlreadyExists"),
                        null);
                // also add global message?
                // context.addMessage(null, message);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Creates a new document mailBox document with the parent given by
     * parentMailboxId
     */
    public String createMailbox() throws CaseManagementException {
        try {
            DocumentModel newDocument = navigationContext.getChangeableDocument();
            if (newDocument.getId() != null) {
                log.debug("Document " + newDocument.getName()
                        + " already created");
                return navigationContext.navigateToDocument(newDocument,
                        "after-create");
            }
            DocumentModel parentDocument = getParentMailbox(parentMailboxId);
            // reset the parent id
            resetParentMailboxId();
            String parentDocumentPath = parentDocument.getPathAsString();
            String title = (String) newDocument.getPropertyValue(MailboxConstants.TITLE_FIELD);
            if (title == null) {
                title = "";
            }
            String name = IdUtils.generateId(title);
            newDocument.setPathInfo(parentDocumentPath, name);
            newDocument = documentManager.createDocument(newDocument);
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    resourcesAccessor.getMessages().get("document_saved"),
                    resourcesAccessor.getMessages().get(newDocument.getType()));
            Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
                    parentDocument);
            return navigationContext.navigateToDocument(newDocument,
                    "after-create");
        } catch (Throwable t) {
            throw new CaseManagementException(t);
        }
    }

    @Factory(value = "inboxChildrenSelectModel", scope = EVENT)
    public SelectDataModel getInboxSelectModel() throws ClientException {
        return getSelectDataModelFromProvider(MAILBOX_INBOX);
    }

    @Factory(value = "serviceChildrenSelectModel", scope = EVENT)
    public SelectDataModel getServicelectModel() throws ClientException {
        return getSelectDataModelFromProvider(MAILBOX_SERVICE);
    }

    @Factory(value = "sentChildrenSelectModel", scope = EVENT)
    public SelectDataModel getSentSelectModel() throws ClientException {
        return getSelectDataModelFromProvider(MAILBOX_SENT);
    }

    @Factory(value = "draftChildrenSelectModel", scope = EVENT)
    public SelectDataModel getDraftSelectModel() throws ClientException {
        return getSelectDataModelFromProvider(MAILBOX_DRAFT);
    }

    @Factory(value = "plansChildrenSelectModel", scope = EVENT)
    public SelectDataModel getPlansSelectModel() throws ClientException {
        return getSelectDataModelFromProvider(MAILBOX_PLANS);
    }

    @Override
    protected void resetMailboxCache(Mailbox cachedMailbox,
            Mailbox newMailbox) throws ClientException {
        ResultsProvidersCache resultsProvidersCache = (ResultsProvidersCache) Component.getInstance("resultsProvidersCache");

        resultsProvidersCache.invalidate(MAILBOX_INBOX);
        resultsProvidersCache.invalidate(MAILBOX_SERVICE);
        resultsProvidersCache.invalidate(MAILBOX_SENT);
        resultsProvidersCache.invalidate(MAILBOX_DRAFT);
        resultsProvidersCache.invalidate(MAILBOX_PLANS);
        super.resetMailboxCache(cachedMailbox, newMailbox);
    }

    /**
     * Gets the mailbox root folder
     */
    protected DocumentModel getMailboxRoot() throws ClientException {
        DocumentModelList res = documentManager.query(String.format(
                "SELECT * from %s",
                MailboxConstants.MAILBOX_ROOT_DOCUMENT_TYPE));
        if (res == null || res.isEmpty()) {
            throw new CaseManagementException(
                    "Cannot find any mailbox root");
        }
        return res.get(0);
    }

    protected DocumentModel getParentMailbox(String parentMailboxId)
            throws ClientException {
        DocumentModel mailboxDoc = null;
        if (parentMailboxId != null && !StringUtils.isEmpty(parentMailboxId)) {
            try {
                mailboxDoc = mailboxManagementService.getMailbox(
                        documentManager, parentMailboxId).getDocument();
            } catch (Exception e) {
                log.error(String.format(
                        "Unable to find parent mailbox with id '%s', using default "
                                + "mailbox root as parent", parentMailboxId));
            }
        }
        if (mailboxDoc == null) {
            mailboxDoc = getMailboxRoot();
        }
        return mailboxDoc;
    }

    public String getParentMailboxId() {
        return parentMailboxId;
    }

    public void setParentMailboxId(String parentMailboxId) {
        this.parentMailboxId = parentMailboxId;
    }

    public void resetParentMailboxId() {
        parentMailboxId = null;
    }

    /**
     * @return true if CurrentMailbox is Generic Mailbox
     */
    public Boolean isGenericMailbox() throws ClientException {
        Mailbox currentMailbox = getCurrentMailbox();
        if(currentMailbox == null) {
            return false;
        }
        DocumentModel doc = getCurrentMailbox().getDocument();
        if (!doc.hasFacet(MailboxConstants.MAILBOX_FACET)) {
            return false;
        }
        Mailbox mailbox = doc.getAdapter(Mailbox.class);
        String type = mailbox.getType();
        return type.equals(MailboxConstants.type.generic.name());
    }

    /**
     * Creates a mail draft mail.
     */
    public String addCaseItem(String type) throws ClientException {
        DocumentModel changeableDocument = documentManager.createDocumentModel(type);
        navigationContext.setChangeableDocument(changeableDocument);
        return navigationContext.getActionResult(changeableDocument,
                UserAction.CREATE);
    }

    /**
     * Creates a mail draft mail.
     */
    public String createDraftCaseItem(String type) throws ClientException {
        Map<String, Object> context = new HashMap<String, Object>();

        // Set the path of MailRoot
        context.put(CoreEventConstants.PARENT_PATH,
                CaseConstants.CASE_ROOT_DOCUMENT_PATH);
        context.put(CaseManagementEventConstants.EVENT_CONTEXT_MAILBOX_ID,
                getCurrentMailbox().getId());
        context.put(
                CaseManagementEventConstants.EVENT_CONTEXT_AFFILIATED_MAILBOX_ID,
                getCurrentMailbox().getAffiliatedMailboxId());

        // Create the new Mail document model in the MailRoot
        DocumentModel changeableDocument = documentManager.createDocumentModel(
                type, context);

        navigationContext.setChangeableDocument(changeableDocument);

        // Redirect to the creation form
        return navigationContext.getActionResult(changeableDocument,
                UserAction.CREATE);
    }

    /**
     * Updates the current mailbox
     */
    public void updateManagerTabMailbox() throws ClientException {
        Mailbox mailbox = getCurrentMailbox();
        mailbox.save(documentManager);
        facesMessages.add(FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(
                        "feedback.casemanagement.delegation.modified"));
        EventManager.raiseEventsOnDocumentChange(mailbox.getDocument());
    }

    public String getCurrentMailboxParent() throws ClientException {
        Mailbox mailbox = getCurrentMailbox();
        return mailbox.getParentId(documentManager);
    }

    public String openDraft(String envelopeId) throws ClientException {
        return navigationContext.navigateToId(envelopeId);
    }

    public String readCaseLink(String caseLinkId, String caseDocumentId,
            Boolean read) throws ClientException {
        if (!read) {
            DocumentModel caseLink = documentManager.getDocument(new IdRef(
                    caseLinkId));
            caseLink.setPropertyValue(CaseLinkConstants.IS_READ_FIELD,
                    Boolean.TRUE);
            documentManager.saveDocument(caseLink);
        }
        return navigationContext.navigateToId(caseDocumentId);
    }

}
