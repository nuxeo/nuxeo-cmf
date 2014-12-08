/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.cm.core.service.synchronization;

import org.nuxeo.cm.service.MailboxTitleGenerator;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

public class DefaultPersonalMailboxTitleGenerator implements MailboxTitleGenerator {

    protected String getUserFirstNameProperty() {
        return "user:firstName";
    }

    protected String getUserLastNameProperty() {
        return "user:lastName";
    }

    protected String getUserCompanyProperty() {
        return "user:company";
    }

    public String getMailboxTitle(DocumentModel userModel) throws PropertyException, ClientException {

        String res = "";
        String first = null;
        if (getUserFirstNameProperty() != null) {
            first = (String) userModel.getPropertyValue(getUserFirstNameProperty());
        }
        String last = null;
        if (getUserLastNameProperty() != null) {
            last = (String) userModel.getPropertyValue(getUserLastNameProperty());
        }
        if (first == null || first.length() == 0) {
            if (last == null || last.length() == 0) {
                res = userModel.getId();
            } else {
                res = last;
            }
        } else {
            if (last == null || last.length() == 0) {
                res = first;
            } else {
                res = first + ' ' + last;
            }
        }
        String company = null;
        if (getUserCompanyProperty() != null) {
            company = (String) userModel.getPropertyValue(getUserCompanyProperty());
        }
        if (company != null) {
            res += " (" + company + ")";
        }
        return res;
    }

}
