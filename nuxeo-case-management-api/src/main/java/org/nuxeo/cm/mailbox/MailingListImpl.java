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
package org.nuxeo.cm.mailbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 1.7
 */
public class MailingListImpl implements MailingList {

    private static final long serialVersionUID = 1L;

    protected final Map<String, Serializable> mlDoc;

    public MailingListImpl(Map<String, Serializable> mlDoc) {
        this.mlDoc = mlDoc;
    }

    @Override
    public String getDescription() {
        return (String) mlDoc.get(MailboxConstants.MAILINGLIST_DESCRIPTION_FIELD);
    }

    @Override
    public String getId() {
        return (String) mlDoc.get(MailboxConstants.MAILINGLIST_ID_FIELD);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getMailboxIds() {
        return (List<String>) mlDoc.get(MailboxConstants.MAILINGLIST_MAILBOXES_FIELD);
    }

    @Override
    public String getTitle() {
        return (String) mlDoc.get(MailboxConstants.MAILINGLIST_TITLE_FIELD);
    }

    @Override
    public void setDescription(String descr) {
        mlDoc.put(MailboxConstants.MAILINGLIST_DESCRIPTION_FIELD, descr);
    }

    @Override
    public void setId(String id) {
        mlDoc.put(MailboxConstants.MAILINGLIST_ID_FIELD, id);
    }

    @Override
    public void setMailboxIds(List<String> mailboxes) {
        // use hash set to remove duplicates from list.
        LinkedHashSet<String> lhs = new LinkedHashSet<String>(mailboxes);
        mailboxes = new ArrayList<String>();
        mailboxes.addAll(lhs);
        mlDoc.put(MailboxConstants.MAILINGLIST_MAILBOXES_FIELD, (Serializable) mailboxes);
    }

    @Override
    public void setTitle(String title) {
        mlDoc.put(MailboxConstants.MAILINGLIST_TITLE_FIELD, title);
    }

    @Override
    public Map<String, Serializable> getMap() {
        return mlDoc;
    }

}
