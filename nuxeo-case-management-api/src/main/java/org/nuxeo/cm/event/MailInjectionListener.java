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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.event;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mail.actionpipe.AbstractCaseManagementMailAction;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.ecm.platform.mail.action.MessageActionPipe;
import org.nuxeo.ecm.platform.mail.action.Visitor;
import org.nuxeo.ecm.platform.mail.service.MailService;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener for mail creation at import
 *
 * @author Laurent Doguin
 */
public class MailInjectionListener implements EventListener {

    private static final Log log = LogFactory.getLog(MailInjectionListener.class);

    private static final String INBOX = "INBOX";

    private static final String IMPORT_MAILBOX = "casemanagementImport";

    private static final String MAILBOX_PIPE = "casemanagementMailBox";

    public void handleEvent(Event event) throws ClientException {

        MailService mailService;
        try {
            mailService = Framework.getService(MailService.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not get Mailbox service.", e);
        }
        MessageActionPipe pipe = mailService.getPipe(MAILBOX_PIPE);

        Visitor visitor = new Visitor(pipe);
        Thread.currentThread().setContextClassLoader(
                Framework.class.getClassLoader());

        Folder rootFolder = null;

        try (CoreSession session = CoreInstance.openCoreSessionSystem(null)) {
            // initialize context
            ExecutionContext initialExecutionContext = new ExecutionContext();
            initialExecutionContext.put(
                    AbstractCaseManagementMailAction.CORE_SESSION_KEY, session);
            initialExecutionContext.put(
                    AbstractCaseManagementMailAction.MIMETYPE_SERVICE_KEY,
                    Framework.getService(MimetypeRegistry.class));
            initialExecutionContext.put(
                    AbstractCaseManagementMailAction.CASEMANAGEMENT_SERVICE_KEY,
                    Framework.getService(CaseDistributionService.class));

            // open store
            Store store = mailService.getConnectedStore(IMPORT_MAILBOX);
            rootFolder = store.getFolder(INBOX);
            rootFolder.open(Folder.READ_WRITE);
            Flags flags = new Flags();
            flags.add(Flag.SEEN);
            SearchTerm term = new FlagTerm(flags, false);
            Message[] unreadMessages = rootFolder.search(term);

            // perform import
            visitor.visit(unreadMessages, initialExecutionContext);

            // save session
            session.save();

            if (rootFolder.isOpen()) {
                try {
                    rootFolder.close(true);
                } catch (MessagingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (MessagingException e) {
            log.error(e, e);
        }
    }

}
