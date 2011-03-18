/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseManagementDistributionTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.runtime.api.Framework;

/**
 * @author arussel
 */
public class HasParticipantsImpl implements HasParticipants {

    private static final long serialVersionUID = 1L;

    protected DocumentModel document;

    protected CaseManagementDistributionTypeService distributionType;

    public HasParticipantsImpl(DocumentModel document) {
        this.document = document;
        try {
            distributionType = Framework.getService(CaseManagementDistributionTypeService.class);
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public void addInitialInternalParticipants(
            Map<String, List<String>> recipients) {

        if (recipients == null) {
            return;
        }

        for (String key : recipients.keySet()) {
            try {
                String schemaProperty = distributionType.getInternalProperty(key);
                addRecipients(recipients.get(key), schemaProperty);
            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }
    }

    public void addInitialExternalParticipants(
            Map<String, List<String>> recipients) {

        if (recipients == null) {
            return;
        }

        for (String key : recipients.keySet()) {
            try {
                String schemaProperty = distributionType.getExternalProperty(key);
                addRecipients(recipients.get(key), schemaProperty);
            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }
    }

    public void addParticipants(Map<String, List<String>> recipients) {

        if (recipients == null) {
            return;
        }

        for (String key : recipients.keySet()) {
            try {
                String schemaProperty = distributionType.getAllProperty(key);
                addRecipients(recipients.get(key), schemaProperty);
            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }

    }

    public Map<String, List<String>> getAllParticipants() {

        Map<String, List<String>> values = new HashMap<String, List<String>>();

        for (String key : distributionType.getDistributionTypes()) {
            try {
                List<String> recipients = getRecipients(distributionType.getAllProperty(key));
                values.put(key, recipients);

            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }

        return values;

    }

    public Map<String, List<String>> getInitialInternalParticipants() {

        Map<String, List<String>> values = new HashMap<String, List<String>>();

        for (String key : distributionType.getDistributionTypes()) {
            try {
                List<String> recipients = getRecipients(distributionType.getInternalProperty(key));
                values.put(key, recipients);
            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }

        return values;

    }

    public Map<String, List<String>> getInitialExternalParticipants() {

        Map<String, List<String>> values = new HashMap<String, List<String>>();

        for (String key : distributionType.getDistributionTypes()) {
            try {
                List<String> recipients = getRecipients(distributionType.getExternalProperty(key));
                values.put(key, recipients);
            } catch (CaseManagementException e) {
                throw new CaseManagementRuntimeException(e);
            }
        }

        return values;

    }

    protected void addRecipients(List<String> recipients, String xpath) {

        // get list of old + new
        List<String> oldIds = getRecipients(xpath);
        if (oldIds == null) {
            oldIds = new ArrayList<String>();
        }
        for (String newId : recipients) {
            if (!oldIds.contains(newId)) {
                oldIds.add(newId);
            }
        }

        try {
            document.setPropertyValue(xpath, (Serializable) oldIds);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" })
    protected List<String> getRecipients(String recipientsXpath) {

        List<String> recipients = null;
        try {
            recipients = (List) document.getPropertyValue(recipientsXpath);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }

        return recipients;
    }

    protected List<String> getMailboxIds(List<Mailbox> list) {
        List<String> result = new ArrayList<String>();
        for (Mailbox mailbox : list) {
            result.add(mailbox.getId());
        }
        return result;
    }

}
