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

package org.nuxeo.correspondence.test.link;

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
import org.nuxeo.correspondence.link.CorrespondenceLink;
import org.nuxeo.correspondence.link.EnvelopeToMailLink;
import org.nuxeo.correspondence.link.MailToMailLink;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.correspondence.test.utils.CorrespondenceTestConstants;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author Laurent Doguin
 */
public class TestCorrespondenceLink extends
        CaseManagementRepositoryTestCase {

    protected Case envelope;

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
        DocumentModel document = createDocument(MailConstants.MAIL_ENVELOPE_TYPE, "env");
        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        envelope = new CaseImpl(document, adapter);
        document = createDocument(MailConstants.OUTGOING_DOCUMENT_TYPE, "i1");
        item1 = new CaseItemImpl(document, adapter);
        document = createDocument(MailConstants.OUTGOING_DOCUMENT_TYPE, "i2");
        item2 = new CaseItemImpl(document, adapter);
        envelope.addCaseItem(item1, session);
        envelope.addCaseItem(item2, session);
    }

    public void testLink() throws Throwable {
        EnvelopeToMailLink envelopeToMailLinks = envelope.getDocument().getAdapter(
                EnvelopeToMailLink.class);
        assertNotNull(envelopeToMailLinks);
        Calendar creationDate = Calendar.getInstance();
        CorrespondenceLink correspondenceLink = new CorrespondenceLink(envelope.getDocument().getRef(),
                item1.getDocument().getId(), "isEnvelopeResourceOf comment", 0l,
                creationDate);
        envelopeToMailLinks.addEnvelopeToMailLink(correspondenceLink);
        envelopeToMailLinks.save(session);

        Map<String, Serializable> link = envelopeToMailLinks.getEnvelopeToMailLink().get(0);
        assertNotNull(link);
        correspondenceLink = new CorrespondenceLink(link);
        assertNotNull(correspondenceLink);
        assertEquals("isEnvelopeResourceOf comment", correspondenceLink.getComment());
        assertEquals(item1.getDocument().getId(), correspondenceLink.getTargetDocId());
        assertEquals(new Long(0), correspondenceLink.getOrder());
        assertEquals(creationDate,  correspondenceLink.getCreationDate());

        MailToMailLink mailToMailLinks = envelope.getDocuments(session).get(0).getAdapter(
                MailToMailLink.class);
        CorrespondenceLink mailToMailLink = new CorrespondenceLink(
                item1.getDocument().getRef(), item1.getDocument().getId(),
                "emailIsAnswerTo comment", 0l, creationDate, creationDate,
                user1);
        mailToMailLinks.addMailToMailLink(mailToMailLink);
        mailToMailLinks.save(session);
        link = mailToMailLinks.getMailToMailLink().get(0);
        assertNotNull(link);
        correspondenceLink = new CorrespondenceLink(link);
        assertNotNull(correspondenceLink);
        assertEquals("emailIsAnswerTo comment", correspondenceLink.getComment());
        assertEquals(item1.getDocument().getId(), correspondenceLink.getTargetDocId());
        assertEquals(new Long(0), correspondenceLink.getOrder());
        assertEquals(creationDate,  correspondenceLink.getCreationDate());

    }
}
