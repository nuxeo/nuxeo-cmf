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

import static org.nuxeo.cm.cases.CaseConstants.CASE_GROUPABLE_FACET;
import static org.nuxeo.cm.cases.CaseConstants.DISTRIBUTABLE_FACET;

import org.nuxeo.cm.cases.CaseImpl;
import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

/**
 * @author arussel
 */
public class CaseAdapterFactory implements DocumentAdapterFactory {

    public Object getAdapter(DocumentModel doc, @SuppressWarnings("rawtypes") Class arg1) {
        checkDocument(doc);
        HasParticipants adapter = doc.getAdapter(HasParticipants.class);
        return new CaseImpl(doc, adapter);
    }

    protected void checkDocument(DocumentModel doc) {
        if (!doc.hasFacet(DISTRIBUTABLE_FACET)) {
            throw new CaseManagementRuntimeException("Document should have facet " + DISTRIBUTABLE_FACET);
        }
        if (doc.hasFacet(CASE_GROUPABLE_FACET)) {
            throw new CaseManagementRuntimeException("Document should not have facet " + CASE_GROUPABLE_FACET);
        }
    }

}
