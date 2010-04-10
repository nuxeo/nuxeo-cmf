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
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.distribution.RecipientItem;
import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostMode;
import org.nuxeo.cm.post.CorrespondencePostRequestImpl;
import org.nuxeo.cm.post.CorrespondencePostType;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBound;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBoundInstance;
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
@Name("correspDistributionActions")
@Scope(ScopeType.CONVERSATION)
@CorrespondenceContextBound
public class CorrespondenceDistributionActionsBean extends
        CorrespondenceContextBoundInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CorrespondenceDistributionActionsBean.class);

    public static final String DISTRIBUTION_ACTION_TABS_CATEGORY = "DISTRIBUTION_TABS";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(required = true, create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient CorrespondenceService correspondenceService;

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
            Mailbox currentMailbox = getCurrentMailbox();
            List<String> favs = currentMailbox.getFavorites();
            if (favs != null && !favs.isEmpty()) {
                List<RecipientItem> favoriteMailboxes = new ArrayList<RecipientItem>();
                for (String fav : favs) {
                    // TODO: Update with post
                    RecipientItem item = (RecipientItem) correspondenceService.getMailboxHeader(fav);
                    item.setMessageType(CorrespondencePostType.NONE.getStringType());
                    favoriteMailboxes.add(item);
                }
                distributionInfo.setFavoriteMailboxes(favoriteMailboxes);
            }
            // entire envelope by default
            distributionInfo.setMode(CorrespondencePostMode.ENTIRE_ENVELOPE.getStringType());
        }
        return distributionInfo;
    }

    public String validateWizard() throws ClientException {
        DocumentModel envelopeDoc = null;
        if (distributionInfo != null) {
            if (!distributionInfo.hasRecipients()) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.corresp.distribution.noRecipients"));
                return null;
            }
            CorrespondencePostMode mode = CorrespondencePostMode.valueOfString(distributionInfo.getMode());
            if (mode == null) {
                facesMessages.add(FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.corresp.distribution.invalidMode"));
                return null;
            }
            Mailbox currentMailbox = getCurrentMailbox();
            if (currentMailbox == null) {
                facesMessages.add(
                        FacesMessage.SEVERITY_ERROR,
                        resourcesAccessor.getMessages().get(
                                "feedback.corresp.distribution.invalidCurrentMailbox"));
                return null;
            }
            DocumentModel emailDoc = null;
            MailEnvelope envelope = null;
            envelope = getCurrentEnvelope();
            if (CorrespondencePostMode.ENTIRE_ENVELOPE.equals(mode)) {
                envelopeDoc = envelope.getDocument();
                emailDoc = envelope.getFirstItem(documentManager).getDocument();
            } else if (CorrespondencePostMode.DOC_ONLY.equals(mode)) {
                emailDoc = getCurrentEmail();
                // XXX: user same parent than current email for new envelope,
                // maybe to change.
                DocumentModel parent = documentManager.getDocument(emailDoc.getParentRef());
                MailEnvelopeItem item = emailDoc.getAdapter(MailEnvelopeItem.class);
                item.createMailEnvelope(documentManager, parent.getPathAsString(), null);
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
            Map<String, List<String>> recipients = distributionInfo.getAllRecipients();

            CorrespondencePost postRequest = new CorrespondencePostRequestImpl(
                    currentMailbox.getId(),
                    Calendar.getInstance(),
                    (String) envelopeDoc.getPropertyValue(MailConstants.TITLE_PROPERTY_NAME),
                    distributionInfo.getComment(), envelope, recipients, null);

            correspondenceService.sendEnvelope(documentManager, postRequest,
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
    protected void resetCurrentEmailCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        resetWizard();
    }

    @Override
    protected void resetEnvelopeCache(MailEnvelope cachedEnvelope,
            MailEnvelope newEnvelope) throws ClientException {
        resetWizard();
    }

}
