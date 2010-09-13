/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id:$
 */

package org.nuxeo.cm.mail.actionpipe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.cases.CaseTreeHelper;
import org.nuxeo.cm.contact.Contact;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.distribution.CMFDistributionInfo;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.runtime.api.Framework;

/**
 * Transforms received email in a set of document models and distribute them.
 *
 * @author Laurent Doguin
 */
public class CreateAndDistributeDocuments extends
        AbstractCaseManagementMailAction {

    private static final Log log = LogFactory.getLog(CreateAndDistributeDocuments.class);

    @SuppressWarnings("unchecked")
    public boolean execute(ExecutionContext context) throws Exception {
        CoreSession session = getCoreSession(context);
        if (session == null) {
            log.error("Could not open core session");
            return false;
        }

        // retrieve metadata
        Calendar receptionDate = (Calendar) context.get(RECEPTION_DATE_KEY);
        String subject = (String) context.get(SUBJECT_KEY);
        String messageId = (String) context.get(MESSAGE_ID_KEY);
        String senderEmail = (String) context.get(SENDER_EMAIL_KEY);
        Contacts origSenders = (Contacts) context.get(ORIGINAL_SENDERS_KEY);
        Contacts origToRecipients = (Contacts) context.get(ORIGINAL_TO_RECIPIENTS_KEY);
        Contacts origCcRecipients = (Contacts) context.get(ORIGINAL_CC_RECIPIENTS_KEY);
        Calendar origReceptionDate = (Calendar) context.get(ORIGINAL_RECEPTION_DATE_KEY);

        MailboxManagementService mailboxManagemenetService = Framework.getService(MailboxManagementService.class);

        Mailbox senderMailbox = mailboxManagemenetService.getUserPersonalMailboxForEmail(
                session, senderEmail);
        if (senderMailbox == null) {
            // cannot find mailbox for user who forwarded => abort
            return false;
        }
        Contact senderContact = Contact.getContactForMailbox(senderMailbox,
                senderEmail, null, null);

        // gather sender/recipients info

        // senders
        Contacts internalOrigSenders = new Contacts();
        Contacts externalOrigSenders = new Contacts();
        fillContactInformation(session, mailboxManagemenetService, origSenders,
                internalOrigSenders, externalOrigSenders);
        List<String> origSendersMailboxesId = new LinkedList<String>();
        origSendersMailboxesId.addAll(internalOrigSenders.getMailboxes());

        // recipients for action
        Contacts internalOrigToRecipients = new Contacts();
        Contacts externalOrigToRecipients = new Contacts();
        fillContactInformation(session, mailboxManagemenetService,
                origToRecipients, internalOrigToRecipients,
                externalOrigToRecipients);
        Set<String> mailboxesForAction = new LinkedHashSet<String>();
        mailboxesForAction.addAll(internalOrigToRecipients.getMailboxes());

        // add sender personal mailbox
        mailboxesForAction.add(senderMailbox.getId());
        internalOrigToRecipients.add(senderContact);

        // recipients for information
        Contacts internalOrigCcRecipients = new Contacts();
        Contacts externalOrigCcRecipients = new Contacts();
        fillContactInformation(session, mailboxManagemenetService,
                origCcRecipients, internalOrigCcRecipients,
                externalOrigCcRecipients);
        Set<String> mailboxesForInformation = new LinkedHashSet<String>();
        mailboxesForInformation.addAll(internalOrigCcRecipients.getMailboxes());

        CMFDistributionInfo distributionInfo = new CMFDistributionInfo();
        distributionInfo.setForActionMailboxes(new ArrayList<String>(
                mailboxesForAction));
        distributionInfo.setForInformationMailboxes(new ArrayList<String>(
                mailboxesForInformation));

        // Create Documents
        DocumentModel mailRootdoc = session.getDocument(new PathRef(
                CaseConstants.CASE_ROOT_DOCUMENT_PATH));
        Date now = new Date();
        if (receptionDate != null) {
            now = receptionDate.getTime();
        }
        DocumentModel parent = CaseTreeHelper.getOrCreateDateTreeFolder(
                session, mailRootdoc, now, CaseConstants.CASE_TREE_TYPE);

        String parentPath = parent.getPathAsString();
        List<Blob> blobs = (List<Blob>) context.get(ATTACHMENTS_KEY);
        boolean first = true;
        Case envelope = null;
        for (Blob blob : blobs) {
            DocumentModel emailDoc = session.createDocumentModel(getCorrespondenceDocumentTypeToCreate());
            String emailTitle;
            if (first) {
                emailTitle = subject;
            } else {
                emailTitle = blob.getFilename();
                // remove extension from email title
                if (emailTitle != null) {
                    int lastDot = emailTitle.lastIndexOf(".");
                    if (lastDot != -1) {
                        emailTitle = emailTitle.substring(0, lastDot);
                    }
                }
            }
            if (emailTitle == null) {
                emailTitle = "";
            }
            String docName = IdUtils.generateId(emailTitle);
            emailDoc.setPathInfo(parentPath, docName);

            emailDoc.setPropertyValue(CaseConstants.TITLE_PROPERTY_NAME,
                    emailTitle);
            emailDoc.setPropertyValue(
                    CaseConstants.DOCUMENT_RECEIVE_DATE_PROPERTY_NAME,
                    receptionDate);
            emailDoc.setPropertyValue(
                    CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME,
                    origReceptionDate);

            // uses messageId as a reference
            emailDoc.setPropertyValue(
                    CaseConstants.DOCUMENT_REFERENCE_PROPERTY_NAME, messageId);

            // senders
            Set<Map<String, Serializable>> sendersContactsProperty = new LinkedHashSet<Map<String, Serializable>>();
            sendersContactsProperty.addAll(internalOrigSenders.getContactsData());
            sendersContactsProperty.addAll(externalOrigSenders.getContactsData());
            emailDoc.setPropertyValue(CaseConstants.CONTACTS_SENDERS,
                    (Serializable) sendersContactsProperty);

            if (origSendersMailboxesId != null) {
                emailDoc.setPropertyValue(
                        CaseConstants.DOCUMENT_SENDERS_PROPERTY_NAME,
                        origSendersMailboxesId.toArray());
            }

            // recipients
            Set<Map<String, Serializable>> recipientsContactsProperty = new LinkedHashSet<Map<String, Serializable>>();
            recipientsContactsProperty.addAll(internalOrigToRecipients.getContactsData());
            recipientsContactsProperty.addAll(externalOrigToRecipients.getContactsData());
            recipientsContactsProperty.addAll(internalOrigCcRecipients.getContactsData());
            recipientsContactsProperty.addAll(externalOrigCcRecipients.getContactsData());
            if (!recipientsContactsProperty.isEmpty()) {
                emailDoc.setPropertyValue(CaseConstants.CONTACTS_PARTICIPANTS,
                        (Serializable) recipientsContactsProperty);
            }

            emailDoc.setPropertyValue(
                    CaseConstants.DOCUMENT_ORIGIN_PROPERTY_NAME,
                    CaseConstants.MAIN_INJECTION_ORIGIN);

            emailDoc.setPropertyValue(CaseConstants.FILE_PROPERTY_NAME,
                    (Serializable) blob);
            emailDoc.setPropertyValue(CaseConstants.FILENAME_PROPERTY_NAME,
                    blob.getFilename());

            emailDoc = session.createDocument(emailDoc);

            CaseItem item = emailDoc.getAdapter(CaseItem.class);
            item.save(session);
            if (first) {
                envelope = item.createMailCase(session,
                        parent.getPathAsString(),
                        CaseLifeCycleConstants.STATE_SENT);
            } else {
                envelope.addCaseItem(item, session);
            }
            first = false;
        }

        Map<String, List<String>> recipients = distributionInfo.getAllParticipants();
        // request without sender mailbox to avoid creating a "sent" post in it
        CaseLink postRequest = new CaseLinkRequestImpl(null,
                Calendar.getInstance(),
                (String) envelope.getDocument().getPropertyValue(
                        CaseConstants.TITLE_PROPERTY_NAME), null, envelope,
                recipients, null);
        CaseDistributionService service = getCaseDistributionService();
        service.sendCase(session, postRequest, envelope.isDraft());

        envelope.save(session);

        // save changes to core
        session.save();

        return true;
    }

    protected void fillContactInformation(CoreSession session,
            MailboxManagementService mailboxService, Contacts originalContacts,
            Contacts internalContacts, Contacts externalContacts) {
        if (originalContacts != null) {
            for (Contact origContact : originalContacts) {
                String origContactEmail = origContact.getEmail();
                Mailbox origContactMailbox = mailboxService.getUserPersonalMailboxForEmail(
                        session, origContactEmail);
                if (origContactMailbox != null) {
                    Contact newOrigSender = Contact.getContactForMailbox(
                            origContactMailbox, origContactEmail, null, null);
                    if (!internalContacts.contains(newOrigSender)) {
                        internalContacts.add(newOrigSender);
                    }
                } else {
                    if (!externalContacts.contains(origContact)) {
                        externalContacts.add(origContact);
                    }
                }
            }
        }
    }

    protected String getCorrespondenceDocumentTypeToCreate() {
        return CaseConstants.CASE_ITEM_DOCUMENT_TYPE;
    }

    public void reset(ExecutionContext context) throws Exception {
        // do nothing
    }

    private CaseDistributionService getCaseDistributionService()
            throws Exception {
        CaseDistributionService distributionService = Framework.getService(CaseDistributionService.class);
        if (distributionService == null) {
            log.error("Unable to get the distribution service");
            throw new ClientException("Unable to get the distribution service");
        }
        return distributionService;
    }

}
