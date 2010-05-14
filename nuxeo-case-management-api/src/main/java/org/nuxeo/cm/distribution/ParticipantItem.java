/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id:
 */

package org.nuxeo.cm.distribution;

import java.io.Serializable;

import org.nuxeo.cm.casefolder.CaseFolderHeaderImpl;


/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
public class ParticipantItem extends CaseFolderHeaderImpl {

    private static final long serialVersionUID = -1803303464066013026L;

    protected int hash;

    String messageType;

    public ParticipantItem(String id, String title, String type) {
        super(id, title, type);
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
        calculateHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        ParticipantItem other = (ParticipantItem) obj;
        return safeStrEq(type, other.type) && safeStrEq(id, other.id)
                && safeStrEq(title, other.title) && safeStrEq(type, other.type);
    }

    protected boolean safeStrEq(String first, String second) {
        if (first == null) {
            return false;
        }
        return first.equals(second);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    protected void calculateHash() {
        hash = (type != null ? type.hashCode() * 3 : 0)
                + (id != null ? id.hashCode() * 5 : 0)
                + (title != null ? title.hashCode() * 7 : 0)
                + (messageType != null ? messageType.hashCode() * 11 : 0);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ParticipantItem.class.getSimpleName());
        buf.append("{message type: ");
        buf.append(messageType);
        buf.append(", mbName: ");
        buf.append(id);
        buf.append(", mbTitle: ");
        buf.append(title);
        buf.append(", itemType: ");
        buf.append(type);
        buf.append("}");
        return buf.toString();
    }
}
