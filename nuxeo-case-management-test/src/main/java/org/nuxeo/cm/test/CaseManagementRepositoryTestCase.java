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

import static org.junit.Assert.assertNotNull;
import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.SENDER_FIELD;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.service.CaseManagementDistributionTypeService;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Anahide Tchertchian
 */
public class CaseManagementRepositoryTestCase extends SQLRepositoryTestCase {

    protected UserManager userManager;

    protected MailboxManagementService correspMailboxService;

    protected CaseDistributionService distributionService;

    protected CaseManagementDistributionTypeService correspDistributionTypeService;

    protected CaseManagementDocumentTypeService correspDocumentTypeService;

    protected static final String administrator = "Administrator";

    protected static final String user = "user";

    protected static final String user1 = "user1";

    protected static final String user2 = "user2";

    protected static final String user3 = "user3";

    protected static final String nulrich = "nulrich";

    protected static final String ldoguin = "ldoguin";

    public static final String CASE_TITLE = "moncase";

    public static final String DC_TITLE = "dc:title";

    protected static DocumentModel mailEnvelopeModel;

    protected static DocumentModel mailEnvelopeItemModel;

    protected Case envelope1;

    public CaseManagementRepositoryTestCase() {
        super();
    }

    public CaseManagementRepositoryTestCase(String name) {
        super();
    }

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();

        // deploy search for QueryModelService for BBB
        deployBundle("org.nuxeo.ecm.platform.search.api");
        // deploy page provider service
        deployBundle("org.nuxeo.ecm.platform.query.api");

        // needed for users
        deployBundle(CaseManagementTestConstants.DIRECTORY_BUNDLE);
        deployBundle(CaseManagementTestConstants.USERMANAGER_BUNDLE);
        deployBundle(CaseManagementTestConstants.DIRECTORY_TYPES_BUNDLE);
        deployBundle(CaseManagementTestConstants.DIRECTORY_SQL_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_TEST_BUNDLE);

        // needed for default hierarchy
        deployBundle(CaseManagementTestConstants.TEMPLATE_BUNDLE);

        // deploy api and core bundles
        deployContrib(CaseManagementTestConstants.ROUTING_CORE_BUNDLE,
                "OSGI-INF/document-routing-service.xml");
        deployContrib(CaseManagementTestConstants.ROUTING_CORE_BUNDLE,
                "OSGI-INF/document-routing-core-types-contrib.xml");
        deployContrib(CaseManagementTestConstants.ROUTING_CORE_BUNDLE,
                "OSGI-INF/pageproviders-contrib.xml");
        deployContrib(CaseManagementTestConstants.CLASSIFICATION_CORE_BUNDLE,
                "OSGI-INF/classification-core-types-contrib.xml");
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_API_BUNDLE);
        deployBundle(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_BUNDLE);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        fireFrameworkStarted();

        userManager = Framework.getService(UserManager.class);
        assertNotNull(userManager);

        distributionService = Framework.getService(CaseDistributionService.class);
        assertNotNull(distributionService);

        correspMailboxService = Framework.getService(MailboxManagementService.class);
        assertNotNull(correspMailboxService);

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
        model.setPropertyValue(DC_TITLE, CASE_TITLE);
        DocumentModel doc = session.createDocument(model);
        session.save();
        return doc.getAdapter(Case.class);
    }

    public CaseItem getMailEnvelopeItem() throws Exception {
        DocumentModel model = getMailEnvelopeItemModel();
        DocumentModel doc = session.createDocument(model);
        session.save();
        return doc.getAdapter(CaseItem.class);
    }

    public DocumentModel getMailEnvelopeModel() throws Exception {
        CaseManagementDocumentTypeService correspDocumentTypeService = Framework.getService(CaseManagementDocumentTypeService.class);

        if (mailEnvelopeModel == null) {
            mailEnvelopeModel = session.createDocumentModel(
                    CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),
                    correspDocumentTypeService.getCaseType());
        }
        return mailEnvelopeModel;
    }

    public DocumentModel getMailEnvelopeItemModel() throws Exception {
        if (mailEnvelopeItemModel == null) {
            mailEnvelopeItemModel = session.createDocumentModel(
                    CaseConstants.CASE_ROOT_DOCUMENT_PATH,
                    UUID.randomUUID().toString(),
                    CaseConstants.CASE_ITEM_DOCUMENT_TYPE);
        }
        return mailEnvelopeItemModel;
    }

    public void createDraftPost(Mailbox mb, Case envelope) throws Exception {
        DocumentModel model = session.createDocumentModel(
                mb.getDocument().getPathAsString(),
                UUID.randomUUID().toString(), CASE_LINK_DOCUMENT_TYPE);
        DocumentModel doc = session.createDocument(model);

        doc.setPropertyValue(CASE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        doc.setPropertyValue(IS_DRAFT_FIELD, Boolean.TRUE);
        doc.setPropertyValue(SENDER_FIELD, mb.getId());

        session.saveDocument(doc);
        session.save();
    }

    public Mailbox getPersonalMailbox(String name) throws Exception {
        return correspMailboxService.createPersonalMailboxes(session, name).get(
                0);
    }

    public DocumentModel createDocumentModel(CoreSession session, String name,
            String type, String path) throws ClientException {
        DocumentModel route1 = session.createDocumentModel(path, name, type);
        route1.setPropertyValue(DocumentRoutingConstants.TITLE_PROPERTY_NAME,
                name);
        return session.createDocument(route1);
    }

    public DocumentRoute createComplexRoute(CoreSession session)
            throws Exception {
        createDocumentModel(session, "routes", "Folder", "/case-management");
        DocumentModel route = createDocumentModel(session, "route",
                DocumentRoutingConstants.DOCUMENT_ROUTE_DOCUMENT_TYPE,
                "/case-management/routes");
        DocumentModel step1 = createDocumentModel(session, "step1",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_STEP,
                route.getPathAsString());
        Mailbox user2Mailbox = getPersonalMailbox(user2);
        step1.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        session.saveDocument(step1);
        DocumentModel step2 = createDocumentModel(session, "step2",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK,
                route.getPathAsString());
        step2.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        session.saveDocument(step2);
        DocumentModel parallelFolder1 = createDocumentModel(session,
                "parallelFolder1",
                DocumentRoutingConstants.STEP_FOLDER_DOCUMENT_TYPE,
                route.getPathAsString());
        parallelFolder1.setPropertyValue(
                DocumentRoutingConstants.EXECUTION_TYPE_PROPERTY_NAME,
                DocumentRoutingConstants.ExecutionTypeValues.parallel.name());
        session.saveDocument(parallelFolder1);
        DocumentModel step31 = createDocumentModel(session, "step31",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK,
                parallelFolder1.getPathAsString());
        step31.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        session.saveDocument(step31);
        DocumentModel step32 = createDocumentModel(session, "step32",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK,
                parallelFolder1.getPathAsString());
        step32.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        session.saveDocument(step32);
        DocumentModel serialFolder = createDocumentModel(session,
                "serialFolder1",
                DocumentRoutingConstants.STEP_FOLDER_DOCUMENT_TYPE,
                parallelFolder1.getPathAsString());
        serialFolder.setPropertyValue(
                DocumentRoutingConstants.EXECUTION_TYPE_PROPERTY_NAME,
                DocumentRoutingConstants.ExecutionTypeValues.serial.name());
        session.saveDocument(serialFolder);
        DocumentModel step41 = createDocumentModel(session, "step41",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK,
                serialFolder.getPathAsString());
        step41.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        session.saveDocument(step41);
        DocumentModel step42 = createDocumentModel(session, "step42",
                CaseConstants.STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK,
                serialFolder.getPathAsString());
        step42.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME,
                user2Mailbox.getId());
        step42.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_DUE_DATE_PROPERTY_NAME,
                new Date());
        step42.setPropertyValue(
                CaseConstants.STEP_DISTRIBUTION_AUTOMATIC_VALIDATION_PROPERTY_NAME,
                Boolean.TRUE);
        session.saveDocument(step42);
        session.saveDocument(parallelFolder1);
        session.save();
        return route.getAdapter(DocumentRoute.class);
    }

}
