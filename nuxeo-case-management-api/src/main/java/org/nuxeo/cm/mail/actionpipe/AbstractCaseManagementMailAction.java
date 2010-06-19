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
 * $Id: AbstractcasemanagementMailAction.java 11688 2008-11-10 10:13:13Z cbaican $
 */

package org.nuxeo.cm.mail.actionpipe;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.ecm.platform.mail.action.MessageAction;

/**
 * Helper for casemanagement actions
 *
 * @author Laurent Doguin
 */
public abstract class AbstractCaseManagementMailAction implements MessageAction {

    public static final String CORE_SESSION_ID_KEY = "sessionId";

    public static final String MIMETYPE_SERVICE_KEY = "mimetypeService";

    public static final String CASEMANAGEMENT_SERVICE_KEY = "casemanagementService";

    public static final String MESSAGE_ID_KEY = "messageId";

    public static final String RECEPTION_DATE_KEY = "receptionDate";

    public static final String SENDER_KEY = "sender";

    public static final String SENDER_EMAIL_KEY = "senderEmail";

    public static final String SUBJECT_KEY = "subject";

    public static final String BODY_KEY = "body";

    public static final String ATTACHMENTS_KEY = "attachments";

    public static final String ORIGINAL_SENDER_NAME_KEY = "originalSenderName";

    public static final String ORIGINAL_SENDERS_KEY = "originalSenders";

    public static final String ORIGINAL_RECEPTION_DATE_KEY = "originalReceptionDate";

    public static final String ORIGINAL_TO_RECIPIENTS_KEY = "originalToRecipients";

    public static final String ORIGINAL_CC_RECIPIENTS_KEY = "originalCcRecipients";

    protected CoreSession getCoreSession(ExecutionContext context)
            throws Exception {
        ExecutionContext initialContext = context.getInitialContext();
        String sessionId = (String) initialContext.get(CORE_SESSION_ID_KEY);
        if (sessionId != null) {
            return CoreInstance.getInstance().getSession(sessionId);
        }
        return null;
    }

}
