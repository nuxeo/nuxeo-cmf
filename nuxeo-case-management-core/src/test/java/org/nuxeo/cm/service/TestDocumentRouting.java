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
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;

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
        setRepository();
        route = createComplexRoute(session);
        routingService.validateRouteModel(route, session);
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
        assertNotNull(routingService);
        route = routingService.createNewInstance(route, docIds, session);
        Mailbox user2Mailbox = getPersonalMailbox(user2);
        List<CaseLink> links = distributionService.getReceivedCaseLinks(
                session, user2Mailbox, 0, 0);
        assertEquals(2, links.size());
        assertFalse(route.isDone());
        ActionableCaseLink actionableLink = null;
        for(CaseLink link : links) {
            if(link.isActionnable()) {
                actionableLink = (ActionableCaseLink) link;
                actionableLink.validate(session);
            }
        }
        assertNotNull(actionableLink);
        route = session.getDocument(route.getDocument().getRef()).getAdapter(DocumentRoute.class);
        assertTrue(route.isDone());
    }
}
