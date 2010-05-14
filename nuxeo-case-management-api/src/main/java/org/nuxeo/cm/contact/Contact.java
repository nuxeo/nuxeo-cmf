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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id:$
 */

package org.nuxeo.cm.contact;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.cm.casefolder.CaseFolder;

/**
 * Wrapper for contact item.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class Contact implements Serializable, Comparable<Contact> {

    public enum CONTACT_FIELD {
        name, email, surname, service, caseFolderId
    }

    private static final CONTACT_FIELD[] FIELDS_FOR_FULLTEXT_INDEXING = {
            CONTACT_FIELD.name, CONTACT_FIELD.email, CONTACT_FIELD.surname,
            CONTACT_FIELD.service, CONTACT_FIELD.caseFolderId };

    private static final long serialVersionUID = 1L;

    protected String name;

    protected String email;

    protected String surname;

    protected String service;

    protected String caseFolderId;

    public Contact() {
    }

    public Contact(Map<? extends String, ? extends Serializable> m) {
        if (m != null) {
            name = (String) m.get(CONTACT_FIELD.name.name());
            email = (String) m.get(CONTACT_FIELD.email.name());
            surname = (String) m.get(CONTACT_FIELD.surname.name());
            service = (String) m.get(CONTACT_FIELD.service.name());
            caseFolderId = (String) m.get(CONTACT_FIELD.caseFolderId.name());
        }
    }

    public String asStringForIndexing() {
        return asStringFor(FIELDS_FOR_FULLTEXT_INDEXING);
    }

    private String asStringFor(CONTACT_FIELD[] fields) {
        StringBuilder sb = new StringBuilder();
        boolean empty = true;
        Map<String, Serializable> map = getContactMap();
        for (CONTACT_FIELD field : fields) {
            Serializable v = map.get(field.name());
            if (v == null) {
                continue;
            }
            if (!empty) {
                sb.append(" ");
            }
            empty = false;
            sb.append(v);
        }
        return empty ? null : sb.toString();
    }

    public HashMap<String, Serializable> getContactMap() {
        HashMap<String, Serializable> m = new HashMap<String, Serializable>();
        if (name != null) {
            m.put(CONTACT_FIELD.name.name(), name);
        }
        if (email != null) {
            m.put(CONTACT_FIELD.email.name(), email);
        }
        if (surname != null) {
            m.put(CONTACT_FIELD.surname.name(), surname);
        }
        if (service != null) {
            m.put(CONTACT_FIELD.service.name(), service);
        }
        if (caseFolderId != null) {
            m.put(CONTACT_FIELD.caseFolderId.name(),caseFolderId);
        }
        return m;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCaseFolderIdd() {
        return caseFolderId;
    }

    public void setCaseFolderId(String caseFolderId) {
        this.caseFolderId = caseFolderId;
    }

    @Override
    public String toString() {
        return getContactMap().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Contact) {
            return this.compareTo((Contact) obj) == 0;
        }
        return false;
    }

    public int compareTo(Contact other) {
        if (other == null) {
            return 1;
        }
        return this.asStringForIndexing().compareTo(other.asStringForIndexing());
    }

    public static Contact getContactForMailbox(CaseFolder caseFolder, String email,
            String service, String surname) {
        if (caseFolder == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.caseFolderId = caseFolder.getId();
        contact.name = caseFolder.getTitle();
        contact.email = email;
        contact.service = service;
        contact.surname = surname;
        return contact;
    }

}
