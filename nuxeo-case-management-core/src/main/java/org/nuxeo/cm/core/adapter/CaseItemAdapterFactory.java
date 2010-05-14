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

import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.cases.CaseItemImpl;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

import static org.nuxeo.cm.cases.CaseConstants.CASE_ITEM_DOCUMENT_SCHEMA;
import static org.nuxeo.cm.cases.CaseConstants.CASE_ITEM_FACET;
import static org.nuxeo.cm.cases.CaseConstants.DISTRIBUTION_SCHEMA;

/**
 * @author arussel
 */
public class CaseItemAdapterFactory implements DocumentAdapterFactory {

    @SuppressWarnings("unchecked")
    public Object getAdapter(DocumentModel doc, Class arg1) {
        checkDocument(doc);
        HasParticipants adapter = doc.getAdapter(HasParticipants.class);
        return new CaseItemImpl(doc, adapter);
    }

    protected void checkDocument(DocumentModel doc) {
        if (!doc.hasFacet(CASE_ITEM_FACET)) {
            throw new CaseManagementRuntimeException(
                    "Document should have facet " + CASE_ITEM_FACET);
        }
        if (!doc.hasSchema(DISTRIBUTION_SCHEMA)) {
            throw new CaseManagementRuntimeException(
                    "Document should have schema " + DISTRIBUTION_SCHEMA);
        }
        if (!doc.hasSchema(CASE_ITEM_DOCUMENT_SCHEMA)) {
            throw new CaseManagementRuntimeException(
                    "Document should have schema " + CASE_ITEM_DOCUMENT_SCHEMA);
        }
    }

}
