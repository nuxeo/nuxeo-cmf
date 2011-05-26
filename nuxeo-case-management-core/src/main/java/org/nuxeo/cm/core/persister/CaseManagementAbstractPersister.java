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
package org.nuxeo.cm.core.persister;

import java.util.Date;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseTreeHelper;
import org.nuxeo.cm.service.CaseManagementPersister;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public abstract class CaseManagementAbstractPersister implements
        CaseManagementPersister {

    protected String caseRootPath;

    @Override
    public String getCaseRootPath() {
        if (caseRootPath == null) {
            // default value
            return CaseConstants.CASE_ROOT_DOCUMENT_PATH;
        }
        return caseRootPath;
    }

    @Override
    public void setCaseRootPath(String caseRootPath) {
        this.caseRootPath = caseRootPath;
    }

    @Override
    public DocumentModel getParentDocumentForCase(CoreSession session) {
        return getParentDocumentForCase(session, null);
    }

    @Override
    public String getParentDocumentPathForCase(CoreSession session) {
        return getParentDocumentPathForCase(session, null);
    }

    @Override
    public DocumentModel getParentDocumentForCase(CoreSession session, Date date) {
        GetParentPathUnrestricted runner = new GetParentPathUnrestricted(
                session, date);
        try {
            runner.runUnrestricted();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return runner.getParentDocument();
    }

    @Override
    public String getParentDocumentPathForCase(CoreSession session, Date date) {
        GetParentPathUnrestricted runner = new GetParentPathUnrestricted(
                session, date);
        try {
            runner.runUnrestricted();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return runner.getParentPath();
    }

    @Override
    public String getParentDocumentPathForCaseItem(CoreSession session,
            Case kase) {
        return getParentDocumentPathForCase(session);
    }

    public class GetParentPathUnrestricted extends UnrestrictedSessionRunner {

        protected String parentPath;

        protected DocumentModel parent;

        protected Date date;

        public GetParentPathUnrestricted(CoreSession session, Date date) {
            super(session);
            if (date != null) {
                this.date = date;
            } else {
                this.date = new Date();
            }
        }

        public String getParentPath() {
            return parentPath;
        }

        public DocumentModel getParentDocument() {
            return parent;
        }

        @Override
        public void run() throws ClientException {
            // Retrieve the MailRoot folder
            DocumentModel mailRootdoc = session.getDocument(new PathRef(
                    getCaseRootPath()));
            // Create (or retrieve) the current MailRoot folder
            // (/mail/YYYY/MM/DD)
            parent = CaseTreeHelper.getOrCreateDateTreeFolder(session,
                    mailRootdoc, date, CaseConstants.CASE_TREE_TYPE);
            parentPath = parent.getPathAsString();
        }

    }
}