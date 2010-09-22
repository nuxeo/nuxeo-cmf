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
 *     Alexandre Russel
 *
 * $Id$
 */
package org.nuxeo.cm.core.adapter;

import org.nuxeo.cm.caselink.ActionableCaseLinkImpl;
import org.nuxeo.cm.caselink.CaseLinkImpl;
import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;
import org.nuxeo.ecm.core.api.model.PropertyException;

import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_LINK_FACET;
import static org.nuxeo.cm.caselink.CaseLinkConstants.CASE_LINK_SCHEMA;
import static org.nuxeo.cm.cases.CaseConstants.DISTRIBUTION_SCHEMA;
import static org.nuxeo.cm.caselink.CaseLinkConstants.IS_ACTIONABLE_FIELD;

/**
 * @author arussel
 */
public class CaseLinkAdapterFactory implements DocumentAdapterFactory {

    public Object getAdapter(DocumentModel doc,
            @SuppressWarnings("rawtypes") Class arg1) {
        checkDocument(doc);
        HasParticipants adapter = doc.getAdapter(HasParticipants.class);
        if (isActionable(doc)){
            return new ActionableCaseLinkImpl(doc, adapter);
        }
        return new CaseLinkImpl(doc, adapter);
    }

    protected void checkDocument(DocumentModel doc) {
        if (!doc.hasFacet(CASE_LINK_FACET)) {
            throw new CaseManagementRuntimeException(
                    "Document should have facet " + CASE_LINK_FACET);
        }
        if (!doc.hasSchema(DISTRIBUTION_SCHEMA)) {
            throw new CaseManagementRuntimeException(
                    "Document should contain schema " + DISTRIBUTION_SCHEMA);
        }
        if (!doc.hasSchema(CASE_LINK_SCHEMA)) {
            throw new CaseManagementRuntimeException(
                    "Document should contain schema " + CASE_LINK_SCHEMA);
        }
    }

    protected boolean isActionable(DocumentModel doc) {
        try {
            Boolean actionable = (Boolean) doc.getPropertyValue(IS_ACTIONABLE_FIELD);
            if (actionable == null) {
                return false;
            }
            return actionable.booleanValue();
        } catch (PropertyException e) {
            throw new ClientRuntimeException(e);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }
}
