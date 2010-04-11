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
 *
 * $Id$
 */

package org.nuxeo.cm.test;

import static org.nuxeo.cm.post.CorrespondencePostConstants.ENVELOPE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.POST_DOCUMENT_TYPE;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_FIELD;

import java.util.UUID;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.MailEnvelope;
import org.nuxeo.cm.cases.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CorrespondenceDistributionTypeService;
import org.nuxeo.cm.service.CorrespondenceDocumentTypeService;
import org.nuxeo.cm.service.CorrespondenceService;

/**
 * @author Anahide Tchertchian
 *
 */
public class CorrespondenceRepositoryTestCase extends SQLRepositoryTestCase {

    protected UserManager userManager;

    protected CorrespondenceService correspService;

    protected CorrespondenceDistributionTypeService correspDistributionTypeService;

    protected CorrespondenceDocumentTypeService correspDocumentTypeService;

    protected static final String administrator = "Administrator";

    protected static final String user = "user";

    protected static final String user1 = "user1";

    protected static final String user2 = "user2";

    protected static final String user3 = "user3";

    protected static final String nulrich = "nulrich";

    protected static final String ldoguin = "ldoguin";

    protected static DocumentModel mailEnvelopeModel;

    protected static DocumentModel mailEnvelopeItemModel;

    protected MailEnvelope envelope1;

    public CorrespondenceRepositoryTestCase() {
        super(null);
    }

    public CorrespondenceRepositoryTestCase(String name) {
        super(name);
    }

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

        userManager = Framework.getService(UserManager.class);
        assertNotNull(userManager);

        correspService = Framework.getService(CorrespondenceService.class);
        assertNotNull(correspService);

        correspDistributionTypeService = Framework.getService(CorrespondenceDistributionTypeService.class);
        assertNotNull(correspDistributionTypeService);

        correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        assertNotNull(correspDocumentTypeService);

    }

    protected DocumentModel createDocument(String type, String id)
            throws Exception {
        DocumentModel document = session.createDocumentModel(type);
        document.setPathInfo("/", id);
        document = session.createDocument(document);
        return document;
    }

    public MailEnvelope getMailEnvelope() throws Exception {
        DocumentModel model = getMailEnvelopeModel();
        DocumentModel doc = session.createDocument(model);
        session.saveDocument(doc);
        session.save();
        return doc.getAdapter(MailEnvelope.class);
    }

    public MailEnvelopeItem getMailEnvelopeItem() throws Exception {
        DocumentModel model = getMailEnvelopeItemModel();
        DocumentModel doc = session.createDocument(model);
        session.saveDocument(doc);
        session.save();
        return doc.getAdapter(MailEnvelopeItem.class);
    }

    public DocumentModel getMailEnvelopeModel() throws Exception {

        CorrespondenceDocumentTypeService correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);

        if (mailEnvelopeModel == null) {
            mailEnvelopeModel = session.createDocumentModel(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),
                    correspDocumentTypeService.getEnvelopeType());
        }
        return mailEnvelopeModel;
    }

    public DocumentModel getMailEnvelopeItemModel() throws Exception {
        if (mailEnvelopeItemModel == null) {
            mailEnvelopeItemModel = session.createDocumentModel(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),"CorrespondenceDocument");
        }
        return mailEnvelopeItemModel;
    }

    public void createDraftPost(Mailbox mb, MailEnvelope envelope)
            throws Exception {

        DocumentModel model = session.createDocumentModel(
                mb.getDocument().getPathAsString(),
                UUID.randomUUID().toString(), POST_DOCUMENT_TYPE);
        DocumentModel doc = session.createDocument(model);

        doc.setPropertyValue(ENVELOPE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        doc.setPropertyValue(IS_DRAFT_FIELD, true);
        doc.setPropertyValue(SENDER_FIELD, mb.getId());

        session.saveDocument(doc);
        session.save();
    }

}
