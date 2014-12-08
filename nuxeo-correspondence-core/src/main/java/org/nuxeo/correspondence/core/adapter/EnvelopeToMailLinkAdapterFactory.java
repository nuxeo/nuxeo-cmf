/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id$
 */
package org.nuxeo.correspondence.core.adapter;

import org.nuxeo.correspondence.core.link.EnvelopeToMailLinkImpl;
import org.nuxeo.correspondence.link.CorrespondenceLinksConstants;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

/**
 * @author ldoguin
 */
public class EnvelopeToMailLinkAdapterFactory implements DocumentAdapterFactory {

    @Override
    public Object getAdapter(DocumentModel doc, @SuppressWarnings("rawtypes") Class arg1) {
        if (doc.hasFacet(CorrespondenceLinksConstants.ENVELOPE_TO_MAIL_LINK_FACET_NAME)) {
            return new EnvelopeToMailLinkImpl(doc);
        }
        return null;
    }

}
