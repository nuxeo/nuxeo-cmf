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
 *     Anahide Tchertchian
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.correspondence.mailbox;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Mailbox implementation using a document model as backend
 *
 * @author Anahide Tchertchian
 */
public class MailboxImpl extends org.nuxeo.cm.mailbox.MailboxImpl implements Mailbox {

    private static final long serialVersionUID = 1L;

    public MailboxImpl(DocumentModel doc) {
        super(doc);
    }

    public void setIncomingConfidentiality(Integer confidentiality) {
        setPropertyValue(MailboxConstants.INCOMING_CONFIDENTIALITY_FIELD, confidentiality);
    }

    public void setOutgoingConfidentiality(Integer confidentiality) {
        setPropertyValue(MailboxConstants.OUTGOING_CONFIDENTIALITY_FIELD, confidentiality);
    }

    public Integer getOutgoingConfidentiality() {
        return getIntegerProperty(MailboxConstants.OUTGOING_CONFIDENTIALITY_FIELD);
    }

    public Integer getIncomingConfidentiality() {
        return getIntegerProperty(MailboxConstants.INCOMING_CONFIDENTIALITY_FIELD);
    }

}
