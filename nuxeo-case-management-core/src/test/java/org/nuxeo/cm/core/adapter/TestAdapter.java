/**
 *
 */
package org.nuxeo.cm.core.adapter;

import java.util.UUID;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;

/**
 * @author arussel
 */
public class TestAdapter extends SQLRepositoryTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.cm.core");
        deployBundle("org.nuxeo.ecm.platform.classification.core");
        deployBundle("org.nuxeo.ecm.platform.routing.core");
        openSession();
    }

    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    protected DocumentModel createDocument(String type) throws ClientException {
        DocumentModel model = session.createDocumentModel("/",
                UUID.randomUUID().toString(), type);
        return session.createDocument(model);
    }

    @Test
    public void testGetCaseItemAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE);
        assertNotNull(doc);
        CaseItem item = doc.getAdapter(CaseItem.class);
        assertNotNull(item);
    }

    @Test
    public void testGetCaseAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseConstants.CASE_TYPE);
        assertNotNull(doc);
        Case mailEnvelope = doc.getAdapter(Case.class);
        assertNotNull(mailEnvelope);
    }

    @Test
    public void testGetMailboxAdapter() throws ClientException {
        DocumentModel doc = createDocument(MailboxConstants.MAILBOX_DOCUMENT_TYPE);
        assertNotNull(doc);
        Mailbox mailbox = doc.getAdapter(Mailbox.class);
        assertNotNull(mailbox);
    }

    @Test
    public void testGetCaseLinktAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE);
        assertNotNull(doc);
        CaseLink post = doc.getAdapter(CaseLink.class);
        assertNotNull(post);
    }

}
