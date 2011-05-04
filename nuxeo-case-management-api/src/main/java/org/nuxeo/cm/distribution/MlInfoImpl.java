/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
package org.nuxeo.cm.distribution;

import java.io.Serializable;

import org.nuxeo.cm.mailbox.MailingList;

public class MlInfoImpl implements MlInfo, Serializable {

    private static final long serialVersionUID = 1L;

    protected String type;

    protected MailingList list;

    public MlInfoImpl(String type, MailingList list) {
        this.list = list;
        this.type = type;
    }

    @Override
    public MailingList getMailingList() {
        return list;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
