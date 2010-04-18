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

import java.io.Serializable;
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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkMode;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.caselink.CaseLinkType;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.distribution.ParticipantItem;
import org.nuxeo.cm.service.CaseFolderManagementService;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
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
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CaseManagementDistributionActionsBean extends
CaseManagementContextBoundInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CaseManagementDistributionActionsBean.class);

    public static final String DISTRIBUTION_ACTION_TABS_CATEGORY = "DISTRIBUTION_TABS";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(required = true, create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient CaseDistributionService caseDistributionService;

    @In(create = true)
    protected transient CaseFolderManagementService caseFolderManagementService;

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
            CaseFolder currentMailbox = getCurrentCaseFolder();
            List<String> favs = currentMailbox.getFavorites();
            if (favs != null && !favs.isEmpty()) {
                List<ParticipantItem> favoriteMailboxes = new ArrayList<ParticipantItem>();
                for (String fav : favs) {
                    // TODO: Update with post
                    ParticipantItem item = (ParticipantItem) caseFolderManagementService.getCaseFolderHeader(fav);
                    item.setMessageType(CaseLinkType.NONE.getStringType());
                    favoriteMailboxes.add(item);
                }
                distributionInfo.setFavoriteCaseFolders(favoriteMailboxes);
            }
            // entire envelope by default
            distributionInfo.setMode(CaseLinkMode.ENTIRE_ENVELOPE.getStringType());
        }
        return distributionInfo;
    }

    public String validateWizard() throws ClientException {
        DocumentModel envelopeDoc = null;
        if (distributionInfo != null) {
            if (!distributionInfo.hasParticipants()) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                        "feedback.corresp.distribution.noRecipients"));
                return null;
            }
            CaseLinkMode mode = CaseLinkMode.valueOfString(distributionInfo.getMode());
            if (mode == null) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                        "feedback.corresp.distribution.invalidMode"));
                return null;
            }
            CaseFolder currentMailbox = getCurrentCaseFolder();
            if (currentMailbox == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                        "feedback.corresp.distribution.invalidCurrentMailbox"));
                return null;
            }
            DocumentModel emailDoc = null;
            Case envelope = null;
            envelope = getCurrentCase();
            if (CaseLinkMode.ENTIRE_ENVELOPE.equals(mode)) {
                envelopeDoc = envelope.getDocument();
                emailDoc = envelope.getFirstItem(documentManager).getDocument();
            } else if (CaseLinkMode.DOC_ONLY.equals(mode)) {
                emailDoc = getCurrentCaseItem();
                // XXX: user same parent than current email for new envelope,
                // maybe to change.
                DocumentModel parent = documentManager.getDocument(emailDoc.getParentRef());
                CaseItem item = emailDoc.getAdapter(CaseItem.class);
                item.createMailCase(documentManager, parent.getPathAsString(), null);
                // FIXME: Null value here
                envelopeDoc = envelope.getDocument();
            }
            if (envelope == null || envelopeDoc == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                        "feedback.corresp.distribution.invalidEnvelope"));
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
                    "feedback.corresp.distribution.done"));
        }
        // navigate to default view
        webActions.resetCurrentTab();
        return navigationContext.navigateToDocument(envelopeDoc);
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
    protected void resetCaseCache(Case cachedEnvelope,
            Case newEnvelope) throws ClientException {
        resetWizard();
    }

}
