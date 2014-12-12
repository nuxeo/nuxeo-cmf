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
 * $Id:$
 */

package org.nuxeo.cm.mail.actionpipe;

import javax.mail.Message;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;

/**
 * End action, erasing flag on mail.
 *
 * @author Laurent Doguin
 */
public class EndAction extends AbstractCaseManagementMailAction {

    private static final Log log = LogFactory.getLog(EndAction.class);

    public boolean execute(ExecutionContext context) {
        try {
            Message message = context.getMessage();
            // erase marker: mail has been treated
            message.setFlag(Flag.FLAGGED, false);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to execute EndAction", e);
            return false;
        }
    }

    public void reset(ExecutionContext context) {
        // do nothing
    }

}
