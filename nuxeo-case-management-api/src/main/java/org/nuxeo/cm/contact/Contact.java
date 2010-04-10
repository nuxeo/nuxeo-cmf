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

import org.nuxeo.cm.mailbox.Mailbox;


/**
 * Wrapper for contact item
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */
public class Contact implements Serializable, Comparable<Contact> {

    public enum CONTACT_FIELD {
        name, email, surname, service, mailboxId
    }

    private static final CONTACT_FIELD[] FIELDS_FOR_FULLTEXT_INDEXING = new CONTACT_FIELD[] {
            CONTACT_FIELD.name, CONTACT_FIELD.email, CONTACT_FIELD.surname,
            CONTACT_FIELD.service, CONTACT_FIELD.mailboxId };

    private static final long serialVersionUID = 1L;

    protected String name;

    protected String email;

    protected String surname;

    protected String service;

    protected String mailboxId;

    public Contact() {
        super();
    }

    public Contact(Map<? extends String, ? extends Serializable> m) {
        if (m != null) {
            setName((String) m.get(CONTACT_FIELD.name.name()));
            setEmail((String) m.get(CONTACT_FIELD.email.name()));
            setSurname((String) m.get(CONTACT_FIELD.surname.name()));
            setService((String) m.get(CONTACT_FIELD.service.name()));
            setMailboxId((String) m.get(CONTACT_FIELD.mailboxId.name()));
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
            m.put(CONTACT_FIELD.name.name(), getName());
        }
        if (email != null) {
            m.put(CONTACT_FIELD.email.name(), getEmail());
        }
        if (surname != null) {
            m.put(CONTACT_FIELD.surname.name(), getSurname());
        }
        if (service != null) {
            m.put(CONTACT_FIELD.service.name(), getService());
        }
        if (mailboxId != null) {
            m.put(CONTACT_FIELD.mailboxId.name(), mailboxId);
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

    public String getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(String mailboxId) {
        this.mailboxId = mailboxId;
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

    public static Contact getContactForMailbox(Mailbox mailbox, String email,
            String service, String surname) {
        if (mailbox == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setMailboxId(mailbox.getId());
        contact.setName(mailbox.getTitle());
        contact.setEmail(email);
        contact.setService(service);
        contact.setSurname(surname);
        return contact;
    }

}
