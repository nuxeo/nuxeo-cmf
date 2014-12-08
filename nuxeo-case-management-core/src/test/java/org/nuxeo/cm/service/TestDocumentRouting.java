/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.core.caselink.ValidateDueCaseLinkUnrestricted;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.runtime.api.Framework;

/**
 * A test running a a complex document route including step for each of the chains we're using.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public class TestDocumentRouting extends CaseManagementRepositoryTestCase {

    protected DocumentRoutingService routingService;

    protected DocumentRoute route;

    protected List<String> docIds = new ArrayList<String>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle(CaseManagementTestConstants.ROUTING_CORE_BUNDLE);
        deployBundle(CaseManagementTestConstants.AUTOMATION_CORE_BUNDLE);

        routingService = Framework.getService(DocumentRoutingService.class);
        assertNotNull(routingService);

        openSession();
        setRepository();
        route = createComplexRoute(session);
        routingService.lockDocumentRoute(route, session);
        route = routingService.validateRouteModel(route, session);
        session.saveDocument(route.getDocument());
        session.save();
        routingService.unlockDocumentRoute(route, session);
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
        assertEquals("validated", route.getDocument().getCurrentLifeCycleState());
        assertEquals("validated", session.getChildren(route.getDocument().getRef()).get(0).getCurrentLifeCycleState());
    }

    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    protected EventService getEventService() throws Exception {
        return Framework.getService(EventService.class);
    }

    public void setRepository() throws Exception {
        Mailbox initialSender = getPersonalMailbox(user1);
        Case envelope = getMailEnvelope();
        CaseItem envelopeItem = getMailEnvelopeItem();
        envelope.addCaseItem(envelopeItem, session);
        envelope.save(session);
        createDraftPost(initialSender, envelope);
        docIds.add(envelope.getDocument().getId());
    }

    @Test
    public void testRunRouteWithUndo() throws Exception {
        NuxeoPrincipal principal2 = userManager.getPrincipal(user2);
        closeSession();
        session = openSessionAs(principal2);
        assertNotNull(routingService);
        route = routingService.createNewInstance(route, docIds, session);
        Mailbox user2Mailbox = getPersonalMailbox(user2);
        List<CaseLink> links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        ActionableCaseLink actionableLink = null;
        for (CaseLink link : links) {
            assertEquals(link.getDocument().getPropertyValue(DC_TITLE), CASE_TITLE);
            if (link.isActionnable()) {
                // find the step and undo it
                DocumentModel linkDoc = link.getDocument();
                String stepId = (String) linkDoc.getPropertyValue("acslk:stepDocumentId");
                DocumentModel stepDoc = session.getDocument(new IdRef(stepId));
                DocumentRouteStep step = stepDoc.getAdapter(DocumentRouteStep.class);
                step.undo(session);
            }
        }
        // assert actionable case link was removed
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(1, links.size());
        // rerun the route
        closeSession();
        session = openSessionAs("administrators");
        route.run(session);
        closeSession();
        session = openSessionAs(principal2);
        // retest that task 2 is recreated
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        actionableLink = null;
        for (CaseLink link : links) {
            assertEquals(link.getDocument().getPropertyValue(DC_TITLE), CASE_TITLE);
            if (link.isActionnable()) {
                actionableLink = (ActionableCaseLink) link;
                actionableLink.validate(session);
            }
        }
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertFalse(route.isDone());
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(4, links.size());
        List<CaseLink> actionnableTodo = new ArrayList<CaseLink>();
        List<CaseLink> actionnableDone = new ArrayList<CaseLink>();
        List<CaseLink> nonActionnable = new ArrayList<CaseLink>();
        for (CaseLink link : links) {
            if (link.isActionnable()) {
                ActionableCaseLink al = link.getDocument().getAdapter(ActionableCaseLink.class);
                if (al.isDone()) {
                    actionnableDone.add(link);
                } else if (al.isTodo()) {
                    actionnableTodo.add(link);
                }
            } else {
                nonActionnable.add(link);
            }
        }
        assertEquals(3, actionnableTodo.size());
        assertEquals(0, actionnableDone.size());
        assertEquals(1, nonActionnable.size());
        for (CaseLink link : actionnableTodo) {
            ActionableCaseLink acl = link.getDocument().getAdapter(ActionableCaseLink.class);
            acl.refuse(session);
        }
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        actionnableTodo = new ArrayList<CaseLink>();
        actionnableDone = new ArrayList<CaseLink>();
        nonActionnable = new ArrayList<CaseLink>();
        for (CaseLink link : links) {
            if (link.isActionnable()) {
                ActionableCaseLink al = link.getDocument().getAdapter(ActionableCaseLink.class);
                if (al.isDone()) {
                    actionnableDone.add(link);
                } else if (al.isTodo()) {
                    actionnableTodo.add(link);
                }
            } else {
                nonActionnable.add(link);
            }
        }
        assertEquals(1, actionnableTodo.size());
        assertEquals(0, actionnableDone.size());
        assertEquals(1, nonActionnable.size());
        for (CaseLink link : actionnableTodo) {
            ActionableCaseLink acl = link.getDocument().getAdapter(ActionableCaseLink.class);
            acl.refuse(session);
        }
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertTrue(route.isDone());
    }

    @Test
    public void testRunRoute() throws Exception {
        NuxeoPrincipal principal2 = userManager.getPrincipal(user2);
        closeSession();
        session = openSessionAs(principal2);
        assertNotNull(routingService);
        route = routingService.createNewInstance(route, docIds, session);
        Mailbox user2Mailbox = getPersonalMailbox(user2);
        List<CaseLink> links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        ActionableCaseLink actionableLink = null;
        for (CaseLink link : links) {
            assertEquals(link.getDocument().getPropertyValue(DC_TITLE), CASE_TITLE);
            if (link.isActionnable()) {
                actionableLink = (ActionableCaseLink) link;
                actionableLink.validate(session);
            }
        }
        assertNotNull(actionableLink);
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertFalse(route.isDone());
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(4, links.size());
        List<CaseLink> actionnableTodo = new ArrayList<CaseLink>();
        List<CaseLink> actionnableDone = new ArrayList<CaseLink>();
        List<CaseLink> nonActionnable = new ArrayList<CaseLink>();
        for (CaseLink link : links) {
            if (link.isActionnable()) {
                ActionableCaseLink al = link.getDocument().getAdapter(ActionableCaseLink.class);
                if (al.isDone()) {
                    actionnableDone.add(link);
                } else if (al.isTodo()) {
                    actionnableTodo.add(link);
                }
            } else {
                nonActionnable.add(link);
            }
        }
        assertEquals(3, actionnableTodo.size());
        assertEquals(0, actionnableDone.size());
        assertEquals(1, nonActionnable.size());
        for (CaseLink link : actionnableTodo) {
            ActionableCaseLink acl = link.getDocument().getAdapter(ActionableCaseLink.class);
            acl.refuse(session);
        }
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        actionnableTodo = new ArrayList<CaseLink>();
        actionnableDone = new ArrayList<CaseLink>();
        nonActionnable = new ArrayList<CaseLink>();
        for (CaseLink link : links) {
            if (link.isActionnable()) {
                ActionableCaseLink al = link.getDocument().getAdapter(ActionableCaseLink.class);
                if (al.isDone()) {
                    actionnableDone.add(link);
                } else if (al.isTodo()) {
                    actionnableTodo.add(link);
                }
            } else {
                nonActionnable.add(link);
            }
        }
        assertEquals(1, actionnableTodo.size());
        assertEquals(0, actionnableDone.size());
        assertEquals(1, nonActionnable.size());
        ValidateDueCaseLinkUnrestricted runner = new ValidateDueCaseLinkUnrestricted(session);
        runner.runUnrestricted();
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertTrue(route.isDone());
    }

    @Test
    public void testCancelRoute() throws Exception {
        NuxeoPrincipal principal2 = userManager.getPrincipal(user2);
        closeSession();
        session = openSessionAs(principal2);
        assertNotNull(routingService);
        route = routingService.createNewInstance(route, docIds, session);
        // user has already a personal mailbox
        Mailbox user2Mailbox = correspMailboxService.getUserPersonalMailbox(session, user2);
        List<CaseLink> links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        ActionableCaseLink actionableLink = null;
        for (CaseLink link : links) {
            assertEquals(link.getDocument().getPropertyValue(DC_TITLE), CASE_TITLE);
            if (link.isActionnable()) {
                actionableLink = (ActionableCaseLink) link;
                actionableLink.validate(session);
            }
        }
        assertNotNull(actionableLink);
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertFalse(route.isDone());
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        assertEquals(4, links.size());
        closeSession();
        session = openSessionAs("administrators");
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        route.cancel(session);
        assertTrue(route.isCanceled());
        closeSession();
        session = openSessionAs(principal2);
        links = distributionService.getReceivedCaseLinks(session, user2Mailbox, 0, 0);
        for (CaseLink link : links) {
            if (link.isActionnable()) {
                fail();
            }
        }
    }
}
