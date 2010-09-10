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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: $
 */

package org.nuxeo.cm.web.distribution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkMode;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.caselink.CaseLinkType;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.distribution.ParticipantItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Distribution actions bean.
 *
 * @author Anahide Tchertchian
 */
@Name("cmDistributionActions")
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CaseManagementDistributionActionsBean extends
        CaseManagementContextBoundInstance {

    public static final String DISTRIBUTION_ACTION_TABS_CATEGORY = "DISTRIBUTION_TABS";

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CaseManagementDistributionActionsBean.class);

    @In(required = true, create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient CaseDistributionService caseDistributionService;

    @In(create = true)
    protected transient MailboxManagementService mailboxManagementService;

    @In(create = true)
    protected WebActions webActions;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    protected DistributionInfo distributionInfo;

    @Factory(value = "distributionInfo", scope = ScopeType.EVENT)
    public DistributionInfo getDistributionInfo() throws ClientException {
        if (distributionInfo == null) {
            distributionInfo = new DistributionInfo();
            // initialize quick items values
            List<String> favs = null;
            Mailbox currentMailbox = getCurrentMailbox();
            if (currentMailbox != null) {
                favs = currentMailbox.getFavorites();
            }
            if (favs != null && !favs.isEmpty()) {
                List<ParticipantItem> favoriteMailboxes = new ArrayList<ParticipantItem>();
                for (String fav : favs) {
                    // TODO: Update with post
                    ParticipantItem item = (ParticipantItem) mailboxManagementService.getMailboxHeader(fav);
                    item.setMessageType(CaseLinkType.NONE.getStringType());
                    favoriteMailboxes.add(item);
                }
                distributionInfo.setFavoriteMailboxes(favoriteMailboxes);
            }
            // entire envelope by default
            distributionInfo.setMode(CaseLinkMode.ENTIRE_ENVELOPE.getStringType());
        }
        return distributionInfo;
    }

    public boolean validateDistributionParticipants() {
        if (!distributionInfo.hasParticipants()) {
            facesMessages.add(
                    FacesMessage.SEVERITY_ERROR,
                    resourcesAccessor.getMessages().get(
                            "feedback.casemanagement.distribution.noParticipants"));
            return false;
        }
        return true;
    }

    public String validateWizard(DistributionInfo distributionInfo)
            throws ClientException {
        this.distributionInfo = distributionInfo;
        return validateWizard();
    }

    public String validateWizard() throws ClientException {
        DocumentModel envelopeDoc = null;
        if (distributionInfo != null) {
            if (!validateDistributionParticipants()) {
                return null;
            }
            CaseLinkMode mode = CaseLinkMode.valueOfString(distributionInfo.getMode());
            if (mode == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.casemanagement.distribution.invalidMode"));
                return null;
            }
            Mailbox currentMailbox = getCurrentMailbox();
            if (currentMailbox == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.casemanagement.distribution.invalidCurrentMailbox"));
                return null;
            }
            Case envelope = getCurrentCase();
            DocumentModel emailDoc;
            if (mode == CaseLinkMode.ENTIRE_ENVELOPE) {
                envelopeDoc = envelope.getDocument();
                emailDoc = envelope.getFirstItem(documentManager).getDocument();
            } else if (mode == CaseLinkMode.DOC_ONLY) {
                emailDoc = getCurrentCaseItem();
                // XXX: user same parent than current email for new envelope,
                // maybe to change.
                DocumentModel parent = documentManager.getDocument(emailDoc.getParentRef());
                CaseItem item = emailDoc.getAdapter(CaseItem.class);
                item.createMailCase(documentManager, parent.getPathAsString(),
                        null);
                // FIXME: Null value here
                envelopeDoc = envelope.getDocument();
            }
            if (envelope == null || envelopeDoc == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.casemanagement.distribution.invalidCase"));
                return null;
            }
            Map<String, List<String>> recipients = distributionInfo.getAllParticipants();
            CaseLink postRequest = new CaseLinkRequestImpl(
                    currentMailbox.getId(),
                    Calendar.getInstance(),
                    (String) envelopeDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME),
                    distributionInfo.getComment(), envelope, recipients, null);
            caseDistributionService.sendCase(documentManager, postRequest,
                    envelope.isDraft());
            // check there were actual recipients
            if (recipients.isEmpty()) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.corresp.distribution.noFinalRecipients"));
                return null;
            }
            envelope.save(documentManager);

            // save changes to core
            documentManager.save();

            resetWizard();
            facesMessages.add(FacesMessage.SEVERITY_INFO,
                    resourcesAccessor.getMessages().get(
                            "feedback.casemanagement.distribution.done"));
        }
        // navigate to default view
        webActions.resetCurrentTab();
        return navigationContext.navigateToDocument(envelopeDoc);
    }

    public boolean canDistributeCase() throws ClientException {
        Case kase = getCurrentCase();
        if(kase == null) {
            return false;
        }
        List<CaseLink> links = caseDistributionService.getCaseLinks(
                documentManager, null, kase);
        return !links.isEmpty();
    }

    /**
     * Resets wizard and navigates to document
     */
    public String cancelWizard() throws ClientException {
        resetWizard();
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return navigationContext.navigateToDocument(currentDoc);
    }

    public void resetWizard() {
        distributionInfo = null;
    }

    @Override
    protected void resetCurrentCaseItemCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        resetWizard();
    }

    @Override
    protected void resetCaseCache(Case cachedEnvelope, Case newEnvelope)
            throws ClientException {
        resetWizard();
    }

}
