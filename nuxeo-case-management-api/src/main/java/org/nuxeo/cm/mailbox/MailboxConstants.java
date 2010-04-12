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

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author Anahide Tchertchian
 *
 */
public class MailboxConstants {

    public enum type {
        personal, generic
    }

    public static final String MAILBOX_DOCUMENT_TYPE = "CaseFolder";

    public static final String MAILBOX_FACET = "CaseFolder";

    public static final String MAIL_ENVELOPE_TYPE = "CorrespondenceEnvelope";

    public static final String MAILBOX_ROOT_DOCUMENT_TYPE = "CaseFolderRoot";

    public static final String MAILBOX_SCHEMA = "case_folder";

    public static final String ID_FIELD = "csfd:case_folder_id";
    
    public static final String AFFILIATED_MAILBOX_ID = "csfd:affiliated_case_folder_id";

    public static final String TITLE_FIELD = "dc:title";

    public static final String DESCRIPTION_FIELD = "dc:description";

    public static final String TYPE_FIELD = "csfd:type";

    public static final String OWNER_FIELD = "csfd:owner";

    public static final String USERS_FIELD = "csfd:users";

    public static final String NOTIFIED_USERS_FIELD = "csfd:notified_users";

    public static final String GROUPS_FIELD = "csfd:groups";

    public static final String PROFILES_FIELD = "csfd:profiles";

    public static final String FAVORITES_FIELD = "csfd:favorites";

    public static final String PARTICIPANTS_LIST_FIELD = "csfd:participantlists";

    public static final String CONFIDENTIALITY_FIELD = "csfd:defaultconfidentiality";

    public static final String MAILINGLIST_ID_FIELD = "plid";

    public static final String MAILINGLIST_TITLE_FIELD = "title";

    public static final String MAILINGLIST_DESCRIPTION_FIELD = "description";

    public static final String MAILINGLIST_CASE_FOLDER_FIELD = "casefolder";

    public static final String MAILBOX_DELETED_STATE = "deleted";

    public static final List<CaseFolderHeader> getMailboxHeaderList(List<DocumentModel> docs) {
        List<CaseFolderHeader> res = new ArrayList<CaseFolderHeader>();
        if (docs != null) {
            for (DocumentModel doc : docs) {
                res.add(doc.getAdapter(CaseFolderHeader.class));
            }
        }
        return res;
    }

    public static final List<Mailbox> getMailboxList(List<DocumentModel> docs) {
        List<Mailbox> res = new ArrayList<Mailbox>();
        if (docs != null) {
            for (DocumentModel doc : docs) {
                res.add(doc.getAdapter(Mailbox.class));
            }
        }
        return res;
    }

}
