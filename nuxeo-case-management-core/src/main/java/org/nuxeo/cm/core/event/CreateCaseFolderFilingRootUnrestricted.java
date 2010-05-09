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

package org.nuxeo.cm.core.event;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.classification.api.ClassificationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

/**
 * Create a filing root document as a child of the mailBox, in unrestricted
 * mode.
 *
 * @author ldoguin
 *
 */
public class CreateCaseFolderFilingRootUnrestricted extends
        UnrestrictedSessionRunner {

    // Mailbox document model
    final protected DocumentModel doc;

    public CreateCaseFolderFilingRootUnrestricted(CoreSession session,
            DocumentModel doc) {
        super(session);
        this.doc = doc;
    }

    @Override
    public void run() throws ClientException {
        CaseFolder mb = doc.getAdapter(CaseFolder.class);
        String filingRootName = getFilingRootNamePrefix() + mb.getTitle();
        String filingFolderId = IdUtils.generateId(filingRootName);
        DocumentModel filingRoot = session.createDocumentModel(
                doc.getPathAsString(), filingFolderId,
                ClassificationConstants.CLASSIFICATION_ROOT);
        filingRoot.setPropertyValue(CaseFolderConstants.TITLE_FIELD,
                filingRootName);
        filingRoot = session.createDocument(filingRoot);
    }

    protected String getFilingRootNamePrefix() {
        return "Cl - ";
    }

}
