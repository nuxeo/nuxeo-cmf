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

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.ecm.platform.mail.action.MessageAction;

/**
 * Helper for casemanagement actions
 *
 * @author Laurent Doguin
 */
public abstract class AbstractCaseManagementMailAction implements
        MessageAction, MailActionPipeConstants {

    protected CoreSession getCoreSession(ExecutionContext context)
            throws Exception {
        return (CoreSession) context.getInitialContext().get(CORE_SESSION_KEY);
    }

}
