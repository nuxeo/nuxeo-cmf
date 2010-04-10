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

import org.nuxeo.cm.exception.CorrespondenceRuntimeException;
import org.nuxeo.cm.mail.HasRecipients;
import org.nuxeo.cm.mail.MailConstants;
import org.nuxeo.cm.mail.MailEnvelopeItemImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;


/**
 * @author arussel
 *
 */
public class MailEnvelopeItemAdapterFactory implements DocumentAdapterFactory {

    @SuppressWarnings("unchecked")
    public Object getAdapter(DocumentModel doc, Class arg1) {
        checkDocument(doc);
        HasRecipients adapter = doc.getAdapter(HasRecipients.class);
        return new MailEnvelopeItemImpl(doc, adapter);
    }

    protected void checkDocument(DocumentModel doc) {
        if (!doc.hasFacet(MailConstants.MAIL_FACET)) {
            throw new CorrespondenceRuntimeException(
                    "Document should have facet " + MailConstants.MAIL_FACET);
        }
        if (!doc.hasSchema(MailConstants.DISTRIBUTION_SCHEMA)) {
            throw new CorrespondenceRuntimeException(
                    "Document should have schema "
                            + MailConstants.DISTRIBUTION_SCHEMA);
        }
        if (!doc.hasSchema(MailConstants.MAIL_DOCUMENT_SCHEMA)) {
            throw new CorrespondenceRuntimeException(
                    "Docuemnt should have schema "
                            + MailConstants.MAIL_DOCUMENT_SCHEMA);
        }
    }

}
