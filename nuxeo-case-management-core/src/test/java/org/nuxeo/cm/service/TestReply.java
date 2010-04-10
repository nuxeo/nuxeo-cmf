package org.nuxeo.cm.service;

import static org.nuxeo.cm.post.CorrespondencePostConstants.ENVELOPE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_FIELD;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nuxeo.cm.contact.Contact;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostRequestImpl;
import org.nuxeo.cm.post.CorrespondencePostType;
import org.nuxeo.cm.service.CorrespondenceDocumentTypeService;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * Test the reply process. First it create an incoming mail, then the receiver
 * reply to this mail and send the response.
 *
 * @author Nicolas Ulrich
 *
 */
public class TestReply extends SQLRepositoryTestCase {

    protected UserManager userManager;

    protected CorrespondenceService correspService;

    protected CorrespondenceDocumentTypeService correspDocumentTypeService;

    protected static final String user1 = "user1";

    protected static final String user2 = "user2";

    @Override
    public void setUp() throws Exception {

        super.setUp();

        // deploy repository manager
        deployBundle("org.nuxeo.ecm.core.api");

        // deploy search
        deployBundle("org.nuxeo.ecm.platform.search.api");

        // deploy api and core bundles
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_API_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);

        // needed for users
        deployBundle("org.nuxeo.ecm.directory");
        deployBundle("org.nuxeo.ecm.platform.usermanager");
        deployBundle("org.nuxeo.ecm.directory.types.contrib");
        deployBundle("org.nuxeo.ecm.directory.sql");
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_TEST_BUNDLE);

        // needed for default hierarchy
        deployBundle("org.nuxeo.ecm.platform.content.template");

        correspService = Framework.getService(CorrespondenceService.class);
        assertNotNull(correspService);

        correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        assertNotNull(correspDocumentTypeService);

        openSession();
    }

    public void testReply() throws Exception {

        // #####################
        // Send an incoming mail
        // #####################

        // Initialize mailboxes
        Mailbox sender = getPersonalMailbox(user1);
        Mailbox receiverMailbox = getPersonalMailbox(user2);

        // Create sender list
        Contact senderItem = new Contact();
        senderItem.setName("name1");
        senderItem.setSurname("surname1");
        senderItem.setService("service1");
        senderItem.setEmail("email");
        senderItem.setMailboxId("mbid");
        Contacts senders = new Contacts();
        senders.add(senderItem);

        // Create an envelope
        MailEnvelope envelope = getMailEnvelope();
        MailEnvelopeItem envelopeItem = getMailEnvelopeItem();
        envelopeItem.setTitle("Hello User2, how are you?");
        envelopeItem.getDocument().setPropertyValue(
                MailConstants.CORRESPONDENCE_CONTACTS_SENDERS,
                senders.getContactsData());
        envelopeItem.save(session);
        envelope.addMailEnvelopeItem(envelopeItem, session);
        createSampleDraftPost(sender, envelope);

        assertNotNull(correspService.getDraftPost(session, sender,
                envelope.getDocument().getId()));

        // Create initial recipients list
        Map<String, List<String>> initialRecipients = new HashMap<String, List<String>>();
        initialRecipients.put(CorrespondencePostType.FOR_ACTION.toString(),
                Collections.singletonList(receiverMailbox.getId()));

        // Create a post request
        CorrespondencePost postRequest = new CorrespondencePostRequestImpl(
                sender.getId(), Calendar.getInstance(), "Check this out",
                "it is a bit boring", envelope, initialRecipients, null);

        // Check mailboxes of initial recipient and sender
        assertEquals(1,
                correspService.getDraftPosts(session, sender, 0, 0).size());
        assertEquals(0, correspService.getReceivedPosts(session,
                receiverMailbox, 0, 0).size());

        assertTrue(envelope.isDraft());

        // Initial sending
        correspService.sendEnvelope(session, postRequest, true);

        // ##################
        // Create a response
        // ##################

        // Get received mail
        CorrespondencePost post = correspService.getReceivedPosts(session,
                receiverMailbox, 0, 0).get(0);
        DocumentModel doc = post.getMailEnvelope(session).getFirstItem(session).getDocument();

        // Reply to the first item of the envelope
        DocumentModel response = correspService.getReplyDocument(session,
                receiverMailbox, doc);

        response.setPathInfo(MailConstants.MAIL_ROOT_DOCUMENT_PATH, "");
        response = session.createDocument(response);

        // Check title of the response
        assertEquals("Rep: Hello User2, how are you?", response.getAdapter(
                MailEnvelopeItem.class).getTitle());

        // Check Recipients of the response
        assertEquals(
                senders.getContactsData(),
                response.getPropertyValue(MailConstants.CORRESPONDENCE_CONTACTS_RECIPIENTS));

        // Check the id of the answered document
        assertEquals(
                doc.getId(),
                response.getPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID));

        // Create an envelope for the response
        MailEnvelope envelopeResponse = getMailEnvelope();
        envelopeResponse.addMailEnvelopeItem(
                response.getAdapter(MailEnvelopeItem.class), session);
        envelopeResponse.save(session);
        createSampleDraftPost(receiverMailbox, envelopeResponse);

        // Create initial recipients list
        Map<String, List<String>> responseRecipients = new HashMap<String, List<String>>();
        responseRecipients.put(CorrespondencePostType.FOR_ACTION.toString(),
                Collections.singletonList(sender.getId()));

        // Create a response post request
        CorrespondencePost postResponseRequest = new CorrespondencePostRequestImpl(
                receiverMailbox.getId(), Calendar.getInstance(),
                "Check this out", "This is an answer", envelopeResponse,
                responseRecipients, null);

        // Send the response
        correspService.sendEnvelope(session, postResponseRequest, true);

        // ###########################
        // Check the received response
        // ###########################

        // Get received response
        CorrespondencePost postResponse = correspService.getReceivedPosts(
                session, sender, 0, 0).get(0);

        DocumentModel mailResponse = postResponse.getMailEnvelope(session).getFirstItem(
                session).getDocument();

        CorrespondencePost postOrginal = correspService.getSentPosts(session,
                sender, 0, 0).get(0);

        DocumentModel mailOriginal = postOrginal.getMailEnvelope(session).getFirstItem(
                session).getDocument();

        // Check that the original sent mail contains the id of the response
        String[] responses = (String[]) mailOriginal.getPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_RESPONSE_DOCUMENT_IDS);
        assertTrue(Arrays.asList(responses).contains(mailResponse.getId()));

        // Check that the received mail is a response to the original mail
        assertEquals(
                mailResponse.getPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID),
                mailOriginal.getId());

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void createSampleDraftPost(Mailbox mb, MailEnvelope envelope)
            throws Exception {

        DocumentModel model = session.createDocumentModel(correspDocumentTypeService.getPostType());
        model.setPathInfo(mb.getDocument().getPathAsString(),
                UUID.randomUUID().toString());
        DocumentModel doc = session.createDocument(model);

        doc.setPropertyValue(ENVELOPE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        doc.setPropertyValue(IS_DRAFT_FIELD, true);
        doc.setPropertyValue(SENDER_FIELD, mb.getId());

        session.saveDocument(doc);
        session.save();
    }

    private Mailbox getPersonalMailbox(String name) throws Exception {
        return correspService.createPersonalMailbox(session, name).get(0);
    }

    public MailEnvelope getMailEnvelope() throws Exception {

        DocumentModel mailEnvelopeModel = session.createDocumentModel(
                MailConstants.MAIL_ROOT_DOCUMENT_PATH,
                UUID.randomUUID().toString(),
                correspDocumentTypeService.getEnvelopeType());

        DocumentModel doc = session.createDocument(mailEnvelopeModel);

        session.saveDocument(doc);
        session.save();

        return doc.getAdapter(MailEnvelope.class);
    }

    public MailEnvelopeItem getMailEnvelopeItem() throws Exception {

        DocumentModel model = session.createDocumentModel(
                MailConstants.MAIL_ROOT_DOCUMENT_PATH,
                UUID.randomUUID().toString(),
                correspDocumentTypeService.getResponseOutgoingDocType());

        DocumentModel doc = session.createDocument(model);

        session.saveDocument(doc);
        session.save();

        return doc.getAdapter(MailEnvelopeItem.class);
    }
}
