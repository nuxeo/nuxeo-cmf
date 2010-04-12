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
package org.nuxeo.cm.mail;

import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostConstants;
import org.nuxeo.cm.post.CorrespondencePostDocumentImpl;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * @author arussel
 *
 */
public class TestCorrespondencePost extends CorrespondenceRepositoryTestCase {
    protected CorrespondencePost post;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
        DocumentModel document = createDocument(
                CorrespondencePostConstants.POST_DOCUMENT_TYPE,
                "post");

        HasParticipants adapter = document.getAdapter(HasParticipants.class);
        post = new CorrespondencePostDocumentImpl(document, adapter);
    }

    public void testGetDocument() {
        assertNotNull(post);
        DocumentModel doc = post.getDocument();
        assertNotNull(doc);
        assertEquals(doc.getType(),
                CorrespondencePostConstants.POST_DOCUMENT_TYPE);
    }
}
