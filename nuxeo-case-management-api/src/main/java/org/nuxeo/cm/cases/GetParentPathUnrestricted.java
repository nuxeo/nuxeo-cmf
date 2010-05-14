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

import java.util.Date;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;


/**
 * @author arussel
 *
 */
public class GetParentPathUnrestricted extends UnrestrictedSessionRunner {
    protected String parentPath;

    public GetParentPathUnrestricted(CoreSession session) {
        super(session);
    }

    public String getParentPath() {
        return parentPath;
    }

    @Override
    public void run() throws ClientException {
        // Retrieve the MailRoot folder
        DocumentModel mailRootdoc = session.getDocument(new PathRef(
                CaseConstants.CASE_ROOT_DOCUMENT_PATH));

        // Create (or retrieve) the current MailRoot folder (/mail/YYYY/MM/DD)
        Date now = new Date();
        DocumentModel parent = CaseTreeHelper.getOrCreateDateTreeFolder(
                session, mailRootdoc, now, CaseConstants.CASE_TREE_TYPE);
        session.save();
        parentPath = parent.getPathAsString();
    }

}
