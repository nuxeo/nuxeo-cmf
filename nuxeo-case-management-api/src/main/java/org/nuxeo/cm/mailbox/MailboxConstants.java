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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author Anahide Tchertchian
 */
public class MailboxConstants {

    public enum type {
        personal, generic
    }

    public enum updatePolicy {
        merge, override
    }

    public static final String MAILBOX_DOCUMENT_TYPE = "Mailbox";

    public static final String MAILBOX_FACET = "Mailbox";

    /**
     * @deprecated since 1.7: use {@link CaseConstants#CASE_TYPE}
     */
    @Deprecated
    public static final String CASE_TYPE = CaseConstants.CASE_TYPE;

    public static final String MAILBOX_ROOT_DOCUMENT_PATH = CaseConstants.CASE_DOMAIN_PATH
            + "/mailbox-root";

    public static final String MAILBOX_ROOT_DOCUMENT_TYPE = "MailboxRoot";

    public static final String MAILBOX_SCHEMA = "mailbox";

    public static final String ID_FIELD = "mlbx:mailbox_id";

    public static final String AFFILIATED_MAILBOX_ID = "mlbx:affiliated_mailbox_id";

    public static final String TITLE_FIELD = "dc:title";

    public static final String DESCRIPTION_FIELD = "dc:description";

    public static final String TYPE_FIELD = "mlbx:type";

    public static final String OWNER_FIELD = "mlbx:owner";

    public static final String USERS_FIELD = "mlbx:users";

    public static final String NOTIFIED_USERS_FIELD = "mlbx:notified_users";

    public static final String GROUPS_FIELD = "mlbx:groups";

    public static final String PROFILES_FIELD = "mlbx:profiles";

    public static final String FAVORITES_FIELD = "mlbx:favorites";

    public static final String MAILING_LIST_FIELD = "mlbx:mailinglists";

    public static final String CONFIDENTIALITY_FIELD = "mlbx:defaultconfidentiality";

    public static final String LAST_SYNC_UPDATE_FIELD = "mlbx:lastSyncUpdate";

    public static final String ORIGIN_FIELD = "mlbx:origin";

    public static final String SYNCHRONIZER_ID_FIELD = "mlbx:synchronizerId";

    public static final String SYNCHRONIZED_STATE_FIELD = "mlbx:synchronizedState";

    public static final String MAILINGLIST_ID_FIELD = "mlid";

    public static final String MAILINGLIST_TITLE_FIELD = "title";

    public static final String MAILINGLIST_DESCRIPTION_FIELD = "description";

    public static final String MAILINGLIST_MAILBOXES_FIELD = "mailbox_ids";

    public static final String MAILBOX_DELETED_STATE = "deleted";

    public static final String GROUP_UPDATE_SYNC_POLICY_PROPERTY = "cm.onGroupDirectoryUpdate";

    public static final String SYNC_BATCH_SIZE_PROPERTY = "cm.mailboxes.updateBatchSize";

    public static final String MAILBOX_CASE_CREATION_PROFILE = "cellule_courrier";

    public static final String SYNC_DELETE_MAILBOXES_PROPERTY = "cm.syncro.delete.mailboxes";
    
    public static List<MailboxHeader> getMailboxHeaderList(
            List<DocumentModel> docs) {
        List<MailboxHeader> res = new ArrayList<MailboxHeader>();
        if (docs != null) {
            for (DocumentModel doc : docs) {
                res.add(doc.getAdapter(MailboxHeader.class));
            }
        }
        return res;
    }

    public static List<Mailbox> getMailboxList(List<DocumentModel> docs) {
        List<Mailbox> res = new ArrayList<Mailbox>();
        if (docs != null) {
            for (DocumentModel doc : docs) {
                res.add(doc.getAdapter(Mailbox.class));
            }
        }
        return res;
    }

    private MailboxConstants() {
    }

}
