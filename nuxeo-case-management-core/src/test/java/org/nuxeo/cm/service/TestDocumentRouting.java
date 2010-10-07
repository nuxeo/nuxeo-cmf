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

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.runtime.api.Framework;

/**
 * A test running a a complex document route including step for each of the
 * chains we're using.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class TestDocumentRouting extends CaseManagementRepositoryTestCase {
    protected DocumentRoute route;

    protected List<String> docIds = new ArrayList<String>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        setRepository();
        route = createComplexRoute(session);
        routingService.validateRouteModel(route, session);
        session.saveDocument(route.getDocument());
        session.save();
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
        assertEquals("validated", route.getDocument().getCurrentLifeCycleState());
        assertEquals("validated", session.getChildren(route.getDocument().getRef()).get(0).getCurrentLifeCycleState());
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

    public void testRunRoute() throws Exception {
        NuxeoPrincipal principal2 = userManager.getPrincipal(user2);
        closeSession();
        session = openSessionAs(principal2);
        assertNotNull(routingService);
        route = routingService.createNewInstance(route, docIds, session);
        Mailbox user2Mailbox = getPersonalMailbox(user2);
        List<CaseLink> links = distributionService.getReceivedCaseLinks(
                session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        ActionableCaseLink actionableLink = null;
        for(CaseLink link : links) {
            assertEquals(link.getDocument().getPropertyValue(DC_TITLE), CASE_TITLE);
            if(link.isActionnable()) {
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
        for(CaseLink link : links) {
            if(link.isActionnable()) {
                ActionableCaseLink al = link.getDocument().getAdapter(ActionableCaseLink.class);
                if(al.isDone()) {
                    actionnableDone.add(link);
                } else if(al.isTodo()) {
                    actionnableTodo.add(link);
                }
            } else {
                nonActionnable.add(link);
            }
        }
        assertEquals(2, actionnableTodo.size());
        assertEquals(1, actionnableDone.size());
        assertEquals(1, nonActionnable.size());
        for(CaseLink link : actionnableTodo) {
            ActionableCaseLink acl = link.getDocument().getAdapter(ActionableCaseLink.class);
            acl.refuse(session);
        }
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertTrue(route.isDone());
    }
}
