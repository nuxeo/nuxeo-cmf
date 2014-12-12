/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.core.persister;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.core.event.CreateMailboxRouteRootUnrestricted;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.routing.core.impl.DocumentRoutingTreePersister;
import org.nuxeo.runtime.api.Framework;

/**
 * Override default persister to create route copy in the personal Route Root.
 *
 * @author ldoguin
 * @since 5.5
 */
public class CaseManagementDocRoutingTreePersister extends DocumentRoutingTreePersister {

    protected static final Log log = LogFactory.getLog(CaseManagementDocRoutingTreePersister.class);

    public static final String QUERY_PERSONAL_ROUTE_ROOT = "SELECT * FROM RouteRoot WHERE ecm:parentId = '%s' AND dc:title= '%s'";

    @Override
    public DocumentModel getParentFolderForNewModel(CoreSession session, DocumentModel instance) {
        MailboxManagementService service = Framework.getService(MailboxManagementService.class);
        Mailbox userMailbox = service.getUserPersonalMailbox(session, session.getPrincipal().getName());
        String name = CreateMailboxRouteRootUnrestricted.getRouteRootNamePrefix() + userMailbox.getTitle();
        DocumentModelList children = session.query(String.format(QUERY_PERSONAL_ROUTE_ROOT,
                userMailbox.getDocument().getId(), name));
        if (children != null && !children.isEmpty()) {
            return children.get(0);
        } else {
            return null;
        }
    }

}
