/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *      @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id$
 */

package org.nuxeo.cm.contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.contact.Contact.CONTACT_FIELD;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;



/**
 * A data structure representative for nxs:contacts complex type. It helps
 * setting/retrieving contacts data.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */
public class Contacts extends ArrayList<Contact> {

    private static final long serialVersionUID = 1L;

    public Contacts() {
        super();
    }

    @SuppressWarnings("unchecked")
    public static Contacts getContactsForDoc(DocumentModel doc,
            String propertyName) throws PropertyException {
        List<Map<String, Serializable>> data;
        try {
            data = (List<Map<String, Serializable>>) doc.getPropertyValue(propertyName);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
        return new Contacts(data);
    }

    public Contacts(List<Map<String, Serializable>> data) {
        super();
        if (data != null) {
            for (Map<String, Serializable> item : data) {
                addContact(new Contact(item));
            }
        }
    }

    public void addContact(Contact contact) {
        add(contact);
    }

    public List<Contact> getContacts() {
        return this;
    }

    public ArrayList<Map<String, Serializable>> getContactsData() {
        ArrayList<Map<String, Serializable>> res = new ArrayList<Map<String, Serializable>>();
        for (Contact contact : this) {
            res.add(contact.getContactMap());
        }
        return res;
    }

    /**
     * This method returns the list of non null emails.
     * <p>
     * TODO: filter non "valid" emails
     */
    public List<String> getEmails() {
        return getDatas(CONTACT_FIELD.email);
    }

    /**
     * This method returns the list of non null names.
     */
    public List<String> getNames() {
        return getDatas(CONTACT_FIELD.name);
    }

    /**
     * This method returns the list of non null mailbox ids
     */
    public List<String> getMailboxes() {
        return getDatas(CONTACT_FIELD.caseFolderId);
    }

    private List<String> getDatas(CONTACT_FIELD field) {
        String name = null;
        ArrayList<String> names = new ArrayList<String>();
        for (Map<String, Serializable> contact : getContactsData()) {
            name = (String) contact.get(field.name());
            if (name != null && !"".equals(name)) {
                names.add(name);
            }
        }
        return names;
    }

    public String asNameEmailString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Contact contact : this) {
            result.append(contact.getName());
            String email = contact.getEmail();
            if (email != null && !"".equals(email)) {
                result.append(" (" + email + ")");
            }
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}