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

import static org.nuxeo.cm.post.CaseLinkConstants.CASE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.post.CaseLinkConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.post.CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE;
import static org.nuxeo.cm.post.CaseLinkConstants.SENDER_FIELD;

import java.util.UUID;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.CaseFolder;
import org.nuxeo.cm.service.CaseManagementDistributionTypeService;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.service.CaseManagementService;

/**
 * @author Anahide Tchertchian
 *
 */
public class CorrespondenceRepositoryTestCase extends SQLRepositoryTestCase {

    protected UserManager userManager;

    protected CaseManagementService correspService;

    protected CaseManagementDistributionTypeService correspDistributionTypeService;

    protected CaseManagementDocumentTypeService correspDocumentTypeService;

    protected static final String administrator = "Administrator";

    protected static final String user = "user";

    protected static final String user1 = "user1";

    protected static final String user2 = "user2";

    protected static final String user3 = "user3";

    protected static final String nulrich = "nulrich";

    protected static final String ldoguin = "ldoguin";

    protected static DocumentModel mailEnvelopeModel;

    protected static DocumentModel mailEnvelopeItemModel;

    protected Case envelope1;

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

        correspService = Framework.getService(CaseManagementService.class);
        assertNotNull(correspService);

        correspDistributionTypeService = Framework.getService(CaseManagementDistributionTypeService.class);
        assertNotNull(correspDistributionTypeService);

        correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);
        assertNotNull(correspDocumentTypeService);

    }

    protected DocumentModel createDocument(String type, String id)
            throws Exception {
        DocumentModel document = session.createDocumentModel(type);
        document.setPathInfo("/", id);
        document = session.createDocument(document);
        return document;
    }

    public Case getMailEnvelope() throws Exception {
        DocumentModel model = getMailEnvelopeModel();
        DocumentModel doc = session.createDocument(model);
        session.saveDocument(doc);
        session.save();
        return doc.getAdapter(Case.class);
    }

    public CaseItem getMailEnvelopeItem() throws Exception {
        DocumentModel model = getMailEnvelopeItemModel();
        DocumentModel doc = session.createDocument(model);
        session.saveDocument(doc);
        session.save();
        return doc.getAdapter(CaseItem.class);
    }

    public DocumentModel getMailEnvelopeModel() throws Exception {

        CaseManagementDocumentTypeService correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);

        if (mailEnvelopeModel == null) {
            mailEnvelopeModel = session.createDocumentModel(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),
                    correspDocumentTypeService.getCaseType());
        }
        return mailEnvelopeModel;
    }

    public DocumentModel getMailEnvelopeItemModel() throws Exception {
        if (mailEnvelopeItemModel == null) {
            mailEnvelopeItemModel = session.createDocumentModel(CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),CaseConstants.CASE_ITEM_DOCUMENT_TYPE);
        }
        return mailEnvelopeItemModel;
    }

    public void createDraftPost(CaseFolder mb, Case envelope)
            throws Exception {

        DocumentModel model = session.createDocumentModel(
                mb.getDocument().getPathAsString(),
                UUID.randomUUID().toString(), CASE_LINK_DOCUMENT_TYPE);
        DocumentModel doc = session.createDocument(model);

        doc.setPropertyValue(CASE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        doc.setPropertyValue(IS_DRAFT_FIELD, true);
        doc.setPropertyValue(SENDER_FIELD, mb.getId());

        session.saveDocument(doc);
        session.save();
    }

}
