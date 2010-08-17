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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.cm.web.mailbox.CaseManagementMailboxActionsBean;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.webapp.helpers.StartupHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Overwrite default StartupHelper to provide custom startup page for
 * casemanagement.
 *
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */
@Name("startupHelper")
@Scope(SESSION)
@Install(precedence = Install.DEPLOYMENT)
public class CaseManagementStartupHelper extends StartupHelper {

    private static final long serialVersionUID = -3606085944027894437L;

    private static final Log log = LogFactory.getLog(CaseManagementStartupHelper.class);

    @In(create = true)
    protected transient CaseManagementMailboxActionsBean cmMailboxActions;

    @In(create = true)
    protected transient NuxeoPrincipal currentNuxeoPrincipal;

    @Override
    public String initServerAndFindStartupPage() throws ClientException {
        String page = super.initServerAndFindStartupPage();
        initCurrentDomain();
        try {
            MailboxManagementService service = Framework.getService(MailboxManagementService.class);
            // select mailbox to display
            final String user = currentNuxeoPrincipal.getName();
            Mailbox userMailbox = service.getUserPersonalMailbox(
                    documentManager, user);
            if (userMailbox != null) {
                page = navigationContext.navigateToDocument(userMailbox.getDocument());
            }
        } catch (Exception e) {
            log.error("Could not redirect to user mailbox", e);
        }
        return page;
    }

    protected void initCurrentDomain() throws ClientException {
        // initialize framework context
        if (documentManager == null) {
            documentManager = navigationContext.getOrCreateDocumentManager();
        }
        // get the domains from selected server
        DocumentModel rootDocument = documentManager.getRootDocument();

        if (!documentManager.hasPermission(rootDocument.getRef(),
                SecurityConstants.READ_CHILDREN)) {
            // user cannot see the root => do not set current document yet
        } else {
            DocumentModelList domains = documentManager.getChildren(rootDocument.getRef());

            if (domains.size() > 0) {
                navigationContext.setCurrentDocument(domains.get(0));
            } else {
                log.warn("No domain found: cannot set current document");
            }
        }
    }

}
