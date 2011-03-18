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
package org.nuxeo.correspondence.web.mailbox;

import java.util.Collection;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;

/**
 * @author arussel
 */
@Name("contactPrinter")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class ContactPrinter {

    public String printContacts(Collection<MapProperty> contacts)
            throws PropertyException {
        if (contacts == null || contacts.size() < 1) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (MapProperty contact : contacts) {
            result.append(contact.get("name").getValue());
            result.append(" ");
            result.append(contact.get("surname").getValue());
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

}
