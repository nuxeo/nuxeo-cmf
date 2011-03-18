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
 *     arussel
 */
package org.nuxeo.cm.cases;

import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.caselink.CaseLinkImpl;
import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author arussel
 */
public class TestCaseLink extends CaseManagementRepositoryTestCase {
    protected CaseLink post;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = createDocument(
                CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE, "post");

        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        post = new CaseLinkImpl(document, adapter);
    }

    public void testGetDocument() {
        assertNotNull(post);
        DocumentModel doc = post.getDocument();
        assertNotNull(doc);
        assertEquals(CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE, doc.getType());
    }
}
