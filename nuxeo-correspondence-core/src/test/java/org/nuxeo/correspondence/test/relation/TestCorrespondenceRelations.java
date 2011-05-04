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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.correspondence.test.relation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseImpl;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.cases.CaseItemImpl;
import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.correspondence.relation.CorrespondenceRelation;
import org.nuxeo.correspondence.relation.CorrespondenceStatement;
import org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author Laurent Doguin
 */
public class TestCorrespondenceRelations extends
        CaseManagementRepositoryTestCase {

    protected Case kase;

    protected CaseItem item1;

    protected CaseItem item2;

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();
        deployBundle("org.nuxeo.ecm.platform.routing.core");
        deployBundle(CorrespondenceTestConstants.CORRESPONDENCE_API_BUNDLE);
        deployBundle(CorrespondenceTestConstants.CORRESPONDENCE_CORE_BUNDLE);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = createDocument(CaseConstants.CASE_TYPE, "env");
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        kase = new CaseImpl(document, adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i1");
        item1 = new CaseItemImpl(document, adapter);
        document = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE, "i2");
        item2 = new CaseItemImpl(document, adapter);
    }

    public void testRelation() throws Throwable {
        CorrespondenceRelation cRelation = kase.getDocument().getAdapter(
                CorrespondenceRelation.class);
        assertNotNull(cRelation);
        Calendar creationDate = Calendar.getInstance();
        CorrespondenceStatement isEnvelopeResourceOfStmt = new CorrespondenceStatement(
                item1.getDocument().getId(), "isEnvelopeResourceOf comment", 0l,
                creationDate);
        cRelation.addIsEnvelopeOfResourceRelation(isEnvelopeResourceOfStmt);

        Map<String, Serializable> stmtMap = cRelation.getIsEnvelopeOfResourceRelation().get(0);
        assertNotNull(stmtMap);
        isEnvelopeResourceOfStmt = new CorrespondenceStatement(stmtMap);
        assertNotNull(isEnvelopeResourceOfStmt);
        assertEquals("isEnvelopeResourceOf comment", isEnvelopeResourceOfStmt.getComment());
        assertEquals(item1.getDocument().getId(), isEnvelopeResourceOfStmt.getTargetDocId());
        assertEquals(new Long(0), isEnvelopeResourceOfStmt.getOrder());
        assertEquals(creationDate,  isEnvelopeResourceOfStmt.getCreationDate());

        CorrespondenceStatement emailIsAnswerToStmt = new CorrespondenceStatement(
                item1.getDocument().getId(), "emailIsAnswerTo comment", 0l,
                Calendar.getInstance(), Calendar.getInstance(), user1);

    }
}
