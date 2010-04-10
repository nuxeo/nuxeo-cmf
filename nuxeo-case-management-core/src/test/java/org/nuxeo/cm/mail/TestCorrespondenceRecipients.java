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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.post.CorrespondencePostConstants;
import org.nuxeo.cm.post.CorrespondencePostType;
import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class TestCorrespondenceRecipients extends CorrespondenceRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        openSession();
    }

    public void testRecipientsMethods() throws Exception {
        Map<String, List<String>> recipients1 = new HashMap<String, List<String>>();
        List<String> actionList1 = new ArrayList<String>();
        actionList1.add("action1");
        actionList1.add("action2");
        recipients1.put(CorrespondencePostType.FOR_ACTION.toString(), actionList1);
        List<String> reviewList1 = new ArrayList<String>();
        reviewList1.add("review1");
        reviewList1.add("review2");
        recipients1.put(CorrespondencePostType.FOR_INFORMATION.toString(), reviewList1);
        DocumentModel model = session.createDocumentModel("/",
                UUID.randomUUID().toString(),
                CorrespondencePostConstants.POST_DOCUMENT_TYPE);
        DocumentModel doc = session.createDocument(model);
        assertNotNull(doc);
        CorrespondencePost post = doc.getAdapter(CorrespondencePost.class);
        post.addInitialInternalRecipients(recipients1);
        post.addRecipients(recipients1);

        Map<String, List<String>> recipients2 = new HashMap<String, List<String>>();
        recipients2.put(CorrespondencePostType.FOR_ACTION.toString(), actionList1);
        List<String> reviewList2 = new ArrayList<String>();
        reviewList2.add("review3");
        reviewList2.add("review4");
        recipients2.put(CorrespondencePostType.FOR_INFORMATION.toString(), reviewList2);
        post.addRecipients(recipients2);

        Map<String, List<String>> allRecipients = post.getAllRecipients();
        assertNotNull(allRecipients);
        assertEquals(2, allRecipients.size());
        assertEquals(2, allRecipients.get(CorrespondencePostType.FOR_ACTION.toString()).size());
        assertEquals(4, allRecipients.get(CorrespondencePostType.FOR_INFORMATION.toString()).size());
        Map<String, List<String>> initialRecipients = post.getInitialInternalRecipients();
        assertNotNull(initialRecipients);
        assertEquals(2, initialRecipients.size());
        assertEquals(2, initialRecipients.get(CorrespondencePostType.FOR_ACTION.toString()).size());
        assertEquals(2, initialRecipients.get(CorrespondencePostType.FOR_INFORMATION.toString()).size());
    }

}
