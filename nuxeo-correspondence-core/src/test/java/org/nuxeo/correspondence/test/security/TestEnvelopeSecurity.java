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

package org.nuxeo.correspondence.test.security;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkRequestImpl;
import org.nuxeo.cm.caselink.CaseLinkType;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.test.CaseManagementSecurityTestCase;
import org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.DocumentSecurityException;

/**
 * @author Laurent Doguin
 */
public class TestEnvelopeSecurity extends CaseManagementSecurityTestCase {

    @Override
    protected void deployRepositoryContrib() throws Exception {
        super.deployRepositoryContrib();
        deployBundle("org.nuxeo.ecm.platform.routing.core");
        deployBundle(CorrespondenceTestConstants.CORRESPONDENCE_CORE_BUNDLE);
        fireFrameworkStarted();
    }

    public void testEnvelopeSend() throws Throwable {
        Mailbox senderMailbox = createPersonalMailbox(user1);
        assertNotNull(senderMailbox);
        String sender1MailboxId = senderMailbox.getDocument().getId();
        Mailbox receiver1Mailbox = createPersonalMailbox(user2);
        String receiver1MailboxId = receiver1Mailbox.getDocument().getId();
        assertNotNull(receiver1Mailbox);
        Mailbox receiver2Mailbox = createPersonalMailbox(user3);
        String receiver2MailboxId = receiver2Mailbox.getDocument().getId();
        assertNotNull(receiver2Mailbox);
        Mailbox forgottenUserMailbox = createPersonalMailbox(user);
        String forgottenUserMailboxId = forgottenUserMailbox.getDocument().getId();
        assertNotNull(receiver2Mailbox);
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put(CaseLinkType.FOR_ACTION.name(),
                Collections.singletonList(receiver1Mailbox.getId()));
        recipients.put(CaseLinkType.FOR_INFORMATION.name(),
                Collections.singletonList(receiver2Mailbox.getId()));
        setMailRootRigts();
        closeSession();
        session = openSessionAs(user1);
        Case envelope = createMailDocumentInEnvelope(senderMailbox);
        session.save();
        DocumentModel docModel = envelope.getDocument();
        assertNotNull(docModel);
        CaseLink clk = caseDistributionService.getDraftCaseLink(session,
                senderMailbox, docModel.getId());
        assertNotNull(clk);
        assertTrue(clk.isDraft());
        CaseLink postRequest = new CaseLinkRequestImpl(senderMailbox.getId(),
                Calendar.getInstance(), "Check this out", "it is a bit boring",
                envelope, recipients, recipients);
        CaseLink post = caseDistributionService.sendCase(session, postRequest,
                true);
        DocumentModel kase = session.getDocument(envelope.getDocument().getRef());
        assertEquals(CaseLifeCycleConstants.STATE_SENT,
                kase.getCurrentLifeCycleState());
        assertNotNull(post);
        assertFalse(post.isDraft());
        assertEquals(user1, post.getSender());
        assertFalse(post.getAllParticipants().get(
                CaseLinkType.FOR_ACTION.name()).isEmpty());
        assertFalse(post.getAllParticipants().get(
                CaseLinkType.FOR_INFORMATION.name()).isEmpty());
        assertTrue(post.getAllParticipants().get(CaseLinkType.FOR_ACTION.name()).contains(
                receiver1Mailbox.getId()));
        assertTrue(post.getAllParticipants().get(
                CaseLinkType.FOR_INFORMATION.name()).contains(
                receiver2Mailbox.getId()));
        DocumentModelList docList = session.query("select * from Document where ecm:mixinType = 'CaseLink' AND ecm:parentId = '"
                + sender1MailboxId + "'");
        assertEquals(1, docList.size());
        DocumentModel message = docList.get(0);
        checkReadRight(message.getRef());
        closeSession();
        session = openSessionAs(user2);
        docList = session.query("select * from Document where ecm:mixinType = 'CaseLink' AND ecm:parentId = '"
                + receiver1MailboxId + "'");
        assertEquals(1, docList.size());
        message = docList.get(0);
        checkReadRight(message.getRef());
        closeSession();
        session = openSessionAs(user3);
        docList = session.query("select * from Document where ecm:mixinType = 'CaseLink' AND ecm:parentId = '"
                + receiver2MailboxId + "'");
        assertEquals(1, docList.size());
        message = docList.get(0);
        checkReadRight(message.getRef());
        closeSession();
        session = openSessionAs(user);
        docList = session.query("select * from Document where ecm:mixinType = 'CaseLink' AND ecm:parentId = '"
                + forgottenUserMailboxId + "'");
        assertEquals(0, docList.size());
        boolean documentSecurityException = false;
        try {
            session.getDocument(message.getRef());
            fail();
        } catch (DocumentSecurityException e) {
            documentSecurityException = true;
        } finally {
            assertTrue(documentSecurityException);
        }
    }

    protected void checkReadRight(DocumentRef ref) throws ClientException {
        DocumentModel dm = session.getDocument(ref);
        assertNotNull(dm);
    }
}
