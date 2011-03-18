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
 *     Laurent Doguin
 */
package org.nuxeo.cm.core.caselink;

import java.util.Calendar;

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.query.sql.model.DateLiteral;

/**
 * Fetch and validate due {@link CaseLink}.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class ValidateDueCaseLinkUnrestricted extends UnrestrictedSessionRunner {

    public static final String FETCH_DUE_ACTIONCASELINK_QUERY = "Select * FROM"
            + " CaseLink WHERE ecm:currentLifeCycleState = 'todo' AND"
            + " acslk:automaticValidation = 1 AND acslk:dueDate < TIMESTAMP '%s' ";

    public ValidateDueCaseLinkUnrestricted(CoreSession session) {
        super(session);
    }

    @Override
    public void run() throws ClientException {
        String dateLiteral = DateLiteral.dateTimeFormatter.print(Calendar.getInstance().getTimeInMillis());
        String query = String.format(FETCH_DUE_ACTIONCASELINK_QUERY,
                dateLiteral);
        DocumentModelList dueCaseLinks = session.query(query);
        ActionableCaseLink acl;
        for (DocumentModel caseLinkdoc : dueCaseLinks) {
            acl = caseLinkdoc.getAdapter(ActionableCaseLink.class);
            acl.validate(session);
        }
    }

}
