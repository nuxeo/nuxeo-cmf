/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     mcedica
 *
 * $Id$
 */

package org.nuxeo.cm.web.actionable.caselink;

import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_ACTIONABLE_FIELD;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * Processing actions for an actionable case link
 *
 * @author <a href="mailto:mcedica@nuxeo.com">Mariana Cedica</a>
 * */
@Name("actionableCaseLinkActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class ActionableCaseLinkActionsBean implements ActionableCaseLinkActions {

    @Override
    public String approveTask() {
        return null;
    }

    @Override
    public String rejectTask() {
        return null;
    }

    @Override
    public boolean isActionable(DocumentModel caseLink) throws ClientException {
        try {
            Boolean actionable = (Boolean) caseLink.getPropertyValue(IS_ACTIONABLE_FIELD);
            if (actionable == null) {
                return false;
            }
            return actionable.booleanValue();
        } catch (PropertyException e) {
            throw new ClientRuntimeException(e);
        }
    }
}