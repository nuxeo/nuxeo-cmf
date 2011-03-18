/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     btatar
 *
 * $Id$
 */

package org.nuxeo.cm.web.helper;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceManagerActions;
import org.nuxeo.ecm.platform.userworkspace.web.ejb.UserWorkspaceManagerActionsBean;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * Personal user workspace manager actions bean.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
@Name("userWorkspaceManagerActions")
@Scope(ScopeType.CONVERSATION)
public class CMUserWorkspaceManagerActionsBean extends
        UserWorkspaceManagerActionsBean implements UserWorkspaceManagerActions {

    private static final long serialVersionUID = 1L;

    public String navigateToOverallWorkspace() throws ClientException {
        if (!initialized) {
            initialize();
        }
        showingPersonalWorkspace = false;
        navigationContext.setCurrentDocument(lastAccessedDocument);
        Events.instance().raiseEvent(EventNames.GO_HOME);
        return navigationContext.navigateToDocument(lastAccessedDocument);
    }
}
