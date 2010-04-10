/**
 *
 */
package org.nuxeo.cm.core.adapter;

import java.util.UUID;

import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;


/**
 * @author arussel
 *
 */
public class TestAdapter extends SQLRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.cm.core");
        openSession();
    }

    protected DocumentModel createDocument(String type) throws ClientException {
        DocumentModel model = session.createDocumentModel("/",
                UUID.randomUUID().toString(), type);
        DocumentModel doc = session.createDocument(model);
        return doc;
    }

    public void testGeEnvelopeItemAdapter() throws ClientException {
        DocumentModel doc = createDocument(MailConstants.MAIL_DOCUMENT_TYPE);
        assertNotNull(doc);
        MailEnvelopeItem item = doc.getAdapter(MailEnvelopeItem.class);
        assertNotNull(item);
    }

    public void testGetEnvelopeAdapter() throws ClientException {
        DocumentModel doc = createDocument(MailConstants.MAIL_ENVELOPE_TYPE);
        assertNotNull(doc);
        MailEnvelope mailEnvelope = doc.getAdapter(MailEnvelope.class);
        assertNotNull(mailEnvelope);
    }

    public void testGetMailboxAdapter() throws ClientException {
        DocumentModel doc = createDocument(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        assertNotNull(doc);
        Mailbox mailbox = doc.getAdapter(Mailbox.class);
        assertNotNull(mailbox);
    }

    public void testGetPostAdapter() throws ClientException {
        DocumentModel doc = createDocument(CorrespondencePostConstants.POST_DOCUMENT_TYPE);
        assertNotNull(doc);
        CorrespondencePost post = doc.getAdapter(CorrespondencePost.class);
        assertNotNull(post);
    }

    @Override
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }
}
