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
 *     ldoguin
 *
 * $Id$
 */

package org.nuxeo.cm.web.helper;

import static org.jboss.seam.ScopeType.SESSION;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.cm.web.mailbox.CaseManagementMailboxActionsBean;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.webapp.helpers.StartupHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Overwrite default StartupHelper to provide custom startup page for
 * casemanagement.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 */
@Name("startupHelper")
@Scope(SESSION)
@Install(precedence = Install.APPLICATION)
public class CaseManagementStartupHelper extends StartupHelper {

    private static final long serialVersionUID = -3606085944027894437L;

    private static final Log log = LogFactory.getLog(CaseManagementStartupHelper.class);

    public static final String CMF_TAB = "MAIN_TABS:cmf";

    @In(create = true)
    protected transient CaseManagementMailboxActionsBean cmMailboxActions;

    @In(create = true)
    protected transient NuxeoPrincipal currentNuxeoPrincipal;

    @Override
    @Begin(id = "#{conversationIdGenerator.nextMainConversationId}", join = true)
    public String initDomainAndFindStartupPage(String domainTitle, String viewId) {
        String page = super.initDomainAndFindStartupPage(domainTitle, viewId);

        try {
            MailboxManagementService service = Framework.getService(MailboxManagementService.class);
            // select mailbox to display
            final String user = currentNuxeoPrincipal.getName();
            Mailbox userMailbox = service.getUserPersonalMailbox(
                    documentManager, user);
            if (userMailbox != null) {
                page = navigationContext.navigateToDocument(userMailbox.getDocument());
                webActions.setCurrentTabIds(CMF_TAB);
            }
        } catch (Exception e) {
            log.error("Could not redirect to user mailbox", e);
        }
        return page;
    }

}
