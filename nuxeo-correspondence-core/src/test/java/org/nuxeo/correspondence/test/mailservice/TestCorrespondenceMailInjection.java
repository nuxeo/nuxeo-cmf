/*
 * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */

package org.nuxeo.correspondence.test.mailservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.contact.Contact;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.mail.actionpipe.MailActionPipeConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.ecm.platform.mail.action.MessageActionPipe;
import org.nuxeo.ecm.platform.mail.action.Visitor;
import org.nuxeo.ecm.platform.mail.service.MailService;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Laurent Doguin
 * @author Sun Seng David TAN <stan@nuxeo.com>
 */
public class TestCorrespondenceMailInjection extends
        CaseManagementRepositoryTestCase implements MailActionPipeConstants {

    protected MailService mailService;

    protected String incomingDocumentType;

    protected final SimpleDateFormat emailDateParser = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();
        deployBundle("org.nuxeo.ecm.platform.mail");
        deployBundle("org.nuxeo.ecm.platform.mimetype.api");
        deployBundle("org.nuxeo.ecm.platform.mimetype.core");
        deployBundle(CorrespondenceTestConstants.CORRESPONDENCE_CORE_BUNDLE);
        deployBundle(CorrespondenceTestConstants.CORRESPONDENCE_CORE_TEST_BUNDLE);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mailService = Framework.getService(MailService.class);
        assertNotNull(mailService);
        correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        assertNotNull(correspDocumentTypeService);
        String postType = correspDocumentTypeService.getCaseLinkType();
        assertNotNull(postType);
        incomingDocumentType = CorrespondenceConstants.IN_CORRESPONDENCE_DOCUMENT;
        openSession();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    /**
     * Test match Email of ccRecipient
     *
     * @throws Exception
     */
    @Test
    public void testParseMailEnglishThunderbird() throws Exception {
        // initialize mailboxes
        Mailbox forwarderMailbox = getPersonalMailbox(nulrich);
        Mailbox origSenderMailbox = getPersonalMailbox(ldoguin);

        String filePath = "data/test_mail_en_thunderbird.eml";
        injectEmail(filePath);

        DocumentRef dayRef = new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH
                + "/2010/02/25");
        assertTrue(session.exists(dayRef));
        DocumentModelList envelopes = session.getChildren(dayRef,
                MailConstants.MAIL_ENVELOPE_TYPE);
        assertEquals(1, envelopes.size());

        DocumentModel envelopeDoc = envelopes.get(0);
        Case envelope = envelopeDoc.getAdapter(Case.class);
        List<DocumentModel> linkedDocs = envelope.getDocuments();
        DocumentModel firstDoc = linkedDocs.get(0);
        Calendar receptionDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_RECEIVE_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Thu, 25 Feb 2010 15:14:35 +0100").getTime(),
                receptionDate.getTime().getTime());
        Calendar importDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Thu, 25 Feb 2010 15:15:25 +0100").getTime(),
                importDate.getTime().getTime());
        String reference = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_REFERENCE_PROPERTY_NAME);
        assertEquals("<14A8EDFD-E93E-4E05-B1F3-7F2EED488BCB@nuxeo.com>",
                reference);
        String origin = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_ORIGIN_PROPERTY_NAME);
        assertEquals("mail", origin);
        String object = (String) firstDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        assertEquals("Fwd: correspondence test sample", object);

        Contacts senders = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_SENDERS);
        assertNotNull(senders);
        assertEquals(1, senders.size());
        Contact sender = senders.get(0);
        assertEquals("laurent O'doguin (nuxeo/reponseD)", sender.getName());
        assertEquals("ldoguin@nuxeo.com", sender.getEmail());
        assertEquals("user-ldoguin", sender.getMailboxIdd());

        Contacts recipients = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_PARTICIPANTS);
        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        Contact recipient = recipients.get(0);
        assertEquals("nicolas ulrich (nuxeo/starship)", recipient.getName());
        assertEquals("nulrich@nuxeo.com", recipient.getEmail());
        assertEquals("user-nulrich", recipient.getMailboxIdd());

        List<CaseLink> forwarderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderReceivedPosts);
        assertEquals(1, forwarderReceivedPosts.size());

        List<CaseLink> forwarderSentPosts = distributionService.getSentCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderSentPosts);
        assertEquals(0, forwarderSentPosts.size());

        // original sender does not have any message, even if he has a
        // mailbox...
        List<CaseLink> origSenderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderReceivedPosts);
        assertEquals(0, origSenderReceivedPosts.size());

        List<CaseLink> origSenderSentPosts = distributionService.getSentCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderSentPosts);
        assertEquals(0, origSenderSentPosts.size());
    }

    /**
     * copy of the previous test but with a mail forwarded using Thunderbird
     * (French)
     *
     * @throws Exception
     */
    @Test
    public void testParseMailFrenchThunderbird() throws Exception {
        // initialize mailboxes
        Mailbox forwarderMailbox = getPersonalMailbox(nulrich);
        Mailbox origSenderMailbox = getPersonalMailbox(ldoguin);

        String filePath = "data/test_mail_fr_thunderbird.eml";
        injectEmail(filePath);

        DocumentRef dayRef = new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH
                + "/2010/12/06");
        assertTrue(session.exists(dayRef));
        DocumentModelList envelopes = session.getChildren(dayRef,
                MailConstants.MAIL_ENVELOPE_TYPE);
        assertEquals(1, envelopes.size());

        DocumentModel envelopeDoc = envelopes.get(0);
        Case envelope = envelopeDoc.getAdapter(Case.class);
        List<DocumentModel> linkedDocs = envelope.getDocuments();
        DocumentModel firstDoc = linkedDocs.get(0);
        // initial message reception
        Calendar receptionDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_RECEIVE_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Mon, 06 Dec 2010 18:08:41 +0100").getTime(),
                receptionDate.getTime().getTime());
        // date of the initial message
        Calendar importDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Wed, 22 Sep 2010 12:49:11 +0200").getTime(),
                importDate.getTime().getTime());
        String reference = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_REFERENCE_PROPERTY_NAME);
        assertEquals("<4CFD1899.9070005@nuxeo.com>", reference);
        String origin = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_ORIGIN_PROPERTY_NAME);
        assertEquals("mail", origin);
        String object = (String) firstDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        assertEquals("Fwd: correspondence thunderbird fr test sample", object);

        Contacts senders = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_SENDERS);
        assertNotNull(senders);
        assertEquals(1, senders.size());
        Contact sender = senders.get(0);
        assertEquals("laurent O'doguin (nuxeo/reponseD)", sender.getName());
        assertEquals("ldoguin@nuxeo.com", sender.getEmail());
        assertEquals("user-ldoguin", sender.getMailboxIdd());

        Contacts recipients = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_PARTICIPANTS);
        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        Contact recipient = recipients.get(0);
        assertEquals("nicolas ulrich (nuxeo/starship)", recipient.getName());
        assertEquals("nulrich@nuxeo.com", recipient.getEmail());
        assertEquals("user-nulrich", recipient.getMailboxIdd());

        List<CaseLink> forwarderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderReceivedPosts);
        assertEquals(1, forwarderReceivedPosts.size());

        List<CaseLink> forwarderSentPosts = distributionService.getSentCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderSentPosts);
        assertEquals(0, forwarderSentPosts.size());

        // original sender does not have any message, even if he has a
        // mailbox...
        List<CaseLink> origSenderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderReceivedPosts);
        assertEquals(0, origSenderReceivedPosts.size());

        List<CaseLink> origSenderSentPosts = distributionService.getSentCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderSentPosts);
        assertEquals(0, origSenderSentPosts.size());
    }

    /**
     * copy of the previous test but with a mail forwarded using gmail
     *
     * @throws Exception
     */
    @Test
    public void testParseMailGmail() throws Exception {
        // initialize mailboxes
        Mailbox forwarderMailbox = getPersonalMailbox(nulrich);
        Mailbox origSenderMailbox = getPersonalMailbox(ldoguin);

        String filePath = "data/test_mail_gmail.eml";
        injectEmail(filePath);

        DocumentRef dayRef = new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH
                + "/2010/12/08");
        assertTrue(session.exists(dayRef));
        DocumentModelList envelopes = session.getChildren(dayRef,
                MailConstants.MAIL_ENVELOPE_TYPE);
        assertEquals(1, envelopes.size());

        DocumentModel envelopeDoc = envelopes.get(0);
        Case envelope = envelopeDoc.getAdapter(Case.class);
        List<DocumentModel> linkedDocs = envelope.getDocuments();
        DocumentModel firstDoc = linkedDocs.get(0);
        // initial message reception
        Calendar receptionDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_RECEIVE_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Wed, 8 Dec 2010 18:25:20 +0100").getTime(),
                receptionDate.getTime().getTime());
        // date of the initial message
        Calendar importDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME);
        assertEquals(
                new SimpleDateFormat("yyyy/M/d").parse("2010/12/8").getTime(),
                importDate.getTime().getTime());
        String reference = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_REFERENCE_PROPERTY_NAME);
        assertEquals(
                "<AANLkTinYj41ADFjFqb=kBYnKzM6jdNMV1OQYDEodMahJ@mail.gmail.com>",
                reference);
        String origin = (String) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_ORIGIN_PROPERTY_NAME);
        assertEquals("mail", origin);
        String object = (String) firstDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        assertEquals("Fwd: correspondence gmail en test sample", object);

        Contacts senders = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_SENDERS);
        assertNotNull(senders);
        assertEquals(1, senders.size());
        Contact sender = senders.get(0);
        assertEquals("laurent O'doguin (nuxeo/reponseD)", sender.getName());
        assertEquals("ldoguin@nuxeo.com", sender.getEmail());
        assertEquals("user-ldoguin", sender.getMailboxIdd());

        Contacts recipients = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_PARTICIPANTS);
        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        Contact recipient = recipients.get(0);
        assertEquals("nicolas ulrich (nuxeo/starship)", recipient.getName());
        assertEquals("nulrich@nuxeo.com", recipient.getEmail());
        assertEquals("user-nulrich", recipient.getMailboxIdd());

        List<CaseLink> forwarderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderReceivedPosts);
        assertEquals(1, forwarderReceivedPosts.size());

        List<CaseLink> forwarderSentPosts = distributionService.getSentCaseLinks(
                session, forwarderMailbox, 0, 0);
        assertNotNull(forwarderSentPosts);
        assertEquals(0, forwarderSentPosts.size());

        // original sender does not have any message, even if he has a
        // mailbox...
        List<CaseLink> origSenderReceivedPosts = distributionService.getReceivedCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderReceivedPosts);
        assertEquals(0, origSenderReceivedPosts.size());

        List<CaseLink> origSenderSentPosts = distributionService.getSentCaseLinks(
                session, origSenderMailbox, 0, 0);
        assertNotNull(origSenderSentPosts);
        assertEquals(0, origSenderSentPosts.size());
    }

    @Test
    public void testActionPipe() {
        MessageActionPipe pipe = mailService.getPipe("casemanagementMailBox");
        assertNotNull(pipe);
        assertEquals(5, pipe.size());
    }

    private String getTestMailSource(String filePath) {
        return FileUtils.getResourcePathFromContext(filePath);
    }

    private Message getSampleMessage(String filePath) throws Exception {
        InputStream stream = new FileInputStream(getTestMailSource(filePath));
        MimeMessage msg = new MimeMessage((Session) null, stream);
        return msg;
    }

    private void injectEmail(String filePath) throws Exception {
        MessageActionPipe pipe = mailService.getPipe("casemanagementMailBox");
        assertNotNull(pipe);
        Visitor visitor = new Visitor(pipe);
        ExecutionContext initialExecutionContext = new ExecutionContext();
        initialExecutionContext.put(
                MailActionPipeConstants.CORE_SESSION_ID_KEY,
                session.getSessionId());
        initialExecutionContext.put(
                MailActionPipeConstants.MIMETYPE_SERVICE_KEY,
                Framework.getService(MimetypeRegistry.class));
        initialExecutionContext.put(
                MailActionPipeConstants.CASEMANAGEMENT_SERVICE_KEY,
                distributionService);

        Message[] messages = new Message[] { getSampleMessage(filePath) };

        visitor.visit(messages, initialExecutionContext);

        DocumentModel mailFolderRef = session.getDocument(new PathRef(
                CaseConstants.CASE_ROOT_DOCUMENT_PATH));
        assertNotNull(mailFolderRef);
        assertTrue(session.hasChildren(mailFolderRef.getRef()));
    }

    /**
     * Test English then French forwarded mail
     *
     * @throws Exception
     */
    @Test
    public void testEmailEnglishForwardEnFr() throws Exception {
        String filePath = "data/test_double-forward_en_fr.eml";
        emailEnglishAssertions(filePath);
    }

    /**
     * Test English twice forwarded mail
     *
     * @throws Exception
     */
    @Test
    public void testEmailEnglishForwardEnEn() throws Exception {
        String filePath = "data/test_double-forward_en_en.eml";
        emailEnglishAssertions(filePath);
    }

    private void emailEnglishAssertions(String filePath) throws Exception {
        // initialize mailboxes
        getPersonalMailbox("jdoe");

        injectEmail(filePath);
        DocumentRef dayRef = new PathRef(CaseConstants.CASE_ROOT_DOCUMENT_PATH
                + "/2009/03/17");
        assertTrue(session.exists(dayRef));
        DocumentModelList envelopes = session.getChildren(dayRef,
                MailConstants.MAIL_ENVELOPE_TYPE);
        assertEquals(1, envelopes.size());

        DocumentModel envelopeDoc = envelopes.get(0);
        Case envelope = envelopeDoc.getAdapter(Case.class);
        List<DocumentModel> linkedDocs = envelope.getDocuments();
        assertEquals(2, linkedDocs.size());
        DocumentModel firstDoc = linkedDocs.get(0);

        String title = (String) firstDoc.getPropertyValue(CaseConstants.TITLE_PROPERTY_NAME);
        assertEquals("[Fwd: RENOUVELLEMENT DE SUPPORT ANNUEL NUXEO]", title);
        Calendar originalReceptionDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_IMPORT_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Wed, 14 Jan 2009 15:15:25 +0100").getTime(),
                originalReceptionDate.getTime().getTime());
        Calendar receptionDate = (Calendar) firstDoc.getPropertyValue(CaseConstants.DOCUMENT_RECEIVE_DATE_PROPERTY_NAME);
        assertEquals(
                emailDateParser.parse("Tue, 17 Mar 2009 13:39:05 +0100").getTime(),
                receptionDate.getTime().getTime());

        Contacts senders = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_SENDERS);
        assertNotNull(senders);
        assertEquals(1, senders.size());
        Contact sender = senders.get(0);
        assertEquals("Sylvie KAPCOM", sender.getName());
        assertEquals("sylvie.kapcom@cs.nuxeo.test.fr", sender.getEmail());

        Contacts recipients = Contacts.getContactsForDoc(firstDoc,
                CaseConstants.CONTACTS_PARTICIPANTS);
        assertNotNull(recipients);
        assertEquals(2, recipients.size());

        assertEquals("jdoe@nuxeo.com", recipients.get(0).getEmail());

        assertTrue((recipients.get(1).getName() == null)
                || ("".equals(recipients.get(1).getName().trim())));
        assertEquals("jean-paul.roger@nuxeo.test.fr",
                recipients.get(1).getEmail());

        // testing content
        DocumentModel secondDoc = linkedDocs.get(1);
        Blob fileBlob = (Blob) secondDoc.getPropertyValue(CaseConstants.FILE_PROPERTY_NAME);

        assertEquals("The file blob filename is", "testpdf.pdf",
                fileBlob.getFilename());
        assertEquals("the file blob length is", 24016, fileBlob.getLength());

        String fileNamePropertyValue = (String) secondDoc.getPropertyValue(CaseConstants.FILENAME_PROPERTY_NAME);
        assertEquals("The filename property value is", "testpdf.pdf",
                fileNamePropertyValue);

    }

}
