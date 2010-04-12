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

package org.nuxeo.cm.core.adapter;

import org.nuxeo.cm.mailbox.CaseFolderConstants;
import org.nuxeo.cm.mailbox.CaseFolderHeader;
import org.nuxeo.cm.mailbox.CaseFolderHeaderImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;


/**
 * @author ldoguin
 *
 */
public class CaseFolderHeaderAdapterFactory implements DocumentAdapterFactory {

    @SuppressWarnings("unchecked")
    public Object getAdapter(DocumentModel doc, Class arg1) {
        if (CaseFolderAdapterFactory.checkDocument(doc)) {
            return getMailboxHeader(doc);
        } else {
            return null;
        }
    }

    protected CaseFolderHeader getMailboxHeader(DocumentModel doc) {
        try {
            String id = (String) doc.getPropertyValue(CaseFolderConstants.ID_FIELD);
            String title = (String) doc.getPropertyValue(CaseFolderConstants.TITLE_FIELD);
            String type = (String) doc.getPropertyValue(CaseFolderConstants.TYPE_FIELD);
            return new CaseFolderHeaderImpl(id, title, type);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
