/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 */
package org.nuxeo.cm.cases;

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * @author ldoguin
 */
public class TestCaseSecurity extends CaseManagementRepositoryTestCase {

    protected Case kase;

    protected CaseItem item1;

    protected CaseItem item2;

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

        DocumentModel doc = kase.getDocument();

        // Set Read rights
        ACP acp = doc.getACP();
        ACL localACL = acp.getOrCreateACL(ACL.LOCAL_ACL);
        localACL.add(new ACE("user1", SecurityConstants.READ, true));
        localACL.add(new ACE("user1", SecurityConstants.WRITE, true));
        acp.addACL(localACL);
        doc.setACP(acp, true);
        session.saveDocument(doc);
        session.save();
    }

    private void archiveDoc() throws Exception {
        kase.followTransition(CaseLifeCycleConstants.TRANSITION_OPEN);
        assertEquals(CaseLifeCycleConstants.STATE_OPEN,
                kase.getDocument().getCurrentLifeCycleState());
        kase.followTransition(CaseLifeCycleConstants.TRANSITION_PROCESS);
        assertEquals(CaseLifeCycleConstants.STATE_PROCESS,
                kase.getDocument().getCurrentLifeCycleState());
        kase.followTransition(CaseLifeCycleConstants.TRANSITION_ARCHIVE);
        assertEquals(CaseLifeCycleConstants.STATE_ARCHIVE,
                kase.getDocument().getCurrentLifeCycleState());
        session.save();
    }

    public void testCaseLifeCycle() throws Exception {
        archiveDoc();
        session = openSessionAs("user1");
        DocumentModel localDoc = session.getDocument(kase.getDocument().getRef());
        assertNotNull(localDoc);
        assertEquals(localDoc.getTitle(), "env");
        try {
            kase.save(session);
            fail();
        } catch (Exception e) {
            assertEquals(e.getCause().getClass(), DocumentSecurityException.class);
       }
    }
}
