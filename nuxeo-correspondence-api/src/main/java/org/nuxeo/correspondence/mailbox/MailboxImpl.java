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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Mailbox implementation using a document model as backend
 * 
 * @author Anahide Tchertchian
 * 
 */
public class MailboxImpl implements Mailbox {

    private static final long serialVersionUID = 1L;

    protected DocumentModel doc;

    public MailboxImpl(DocumentModel doc) {
        this.doc = doc;
    }

    public DocumentModel getDocument() {
        return doc;
    }

    protected String getStringProperty(String property) {
        try {
            return (String) doc.getPropertyValue(property);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringListProperty(String property) {
        try {
            List<String> res = null;
            Object propValue = doc.getPropertyValue(property);
            if (propValue instanceof List) {
                res = (List<String>) propValue;
            } else if (propValue instanceof String[]) {
                res = Arrays.asList((String[]) propValue);
            } else if (propValue != null) {
                throw new ClientRuntimeException(String.format(
                        "Unexpected non-list value for prop %s: %s", property,
                        propValue));
            }
            return res;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected Integer getIntegerProperty(String property) {
        try {
            Object value = doc.getPropertyValue(property);
            if (value instanceof Long) {
                return Integer.valueOf(((Long) value).toString());
            }
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected void setPropertyValue(String property, Serializable value) {
        try {
            doc.setPropertyValue(property, value);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public void setIncomingConfidentiality(Integer confidentiality) {
        setPropertyValue(MailboxConstants.INCOMING_CONFIDENTIALITY_FIELD,
                confidentiality);
    }

    public void setOutgoingConfidentiality(Integer confidentiality) {
        setPropertyValue(MailboxConstants.OUTGOING_CONFIDENTIALITY_FIELD,
                confidentiality);
    }

    public Integer getOutgoingConfidentiality() {
        return getIntegerProperty(MailboxConstants.OUTGOING_CONFIDENTIALITY_FIELD);
    }

    public Integer getIncomingConfidentiality() {
        return getIntegerProperty(MailboxConstants.INCOMING_CONFIDENTIALITY_FIELD);
    }

    // TODO implement this!
    public int compareTo(Mailbox other) {
        return 0;
    }

}
