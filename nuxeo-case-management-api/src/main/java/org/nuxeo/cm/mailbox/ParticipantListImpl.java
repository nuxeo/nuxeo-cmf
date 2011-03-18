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
 *
 * $Id$
 */

package org.nuxeo.cm.mailbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Mailing list implementation using a complex property value as backend.
 *
 * @author Anahide Tchertchian
 */
public class ParticipantListImpl implements ParticipantsList {

    private static final long serialVersionUID = 1L;

    protected final Map<String, Serializable> mlDoc;

    public ParticipantListImpl(Map<String, Serializable> mlDoc) {
        this.mlDoc = mlDoc;
    }

    public String getDescription() {
        return (String) mlDoc.get(MailboxConstants.MAILINGLIST_DESCRIPTION_FIELD);
    }

    public String getId() {
        return (String) mlDoc.get(MailboxConstants.PARTICIPANTLIST_ID_FIELD);
    }

    public List<String> getMailboxIds() {
        return Arrays.asList((String[]) mlDoc.get(MailboxConstants.MAILINGLIST_MAILBOX_FIELD));
    }

    public String getTitle() {
        return (String) mlDoc.get(MailboxConstants.MAILINGLIST_TITLE_FIELD);
    }

    public void setDescription(String descr) {
        mlDoc.put(MailboxConstants.MAILINGLIST_DESCRIPTION_FIELD, descr);
    }

    public void setId(String id) {
        mlDoc.put(MailboxConstants.PARTICIPANTLIST_ID_FIELD, id);
    }

    public void setMailboxIds(List<String> mailboxes) {
        ArrayList<String> serializableMailboxes = new ArrayList<String>();
        if (mailboxes != null) {
            serializableMailboxes.addAll(mailboxes);
        }
        mlDoc.put(MailboxConstants.MAILINGLIST_MAILBOX_FIELD,
                serializableMailboxes);
    }

    public void setTitle(String title) {
        mlDoc.put(MailboxConstants.MAILINGLIST_TITLE_FIELD, title);
    }

    public Map<String, Serializable> getMap() {
        return mlDoc;
    }

}
