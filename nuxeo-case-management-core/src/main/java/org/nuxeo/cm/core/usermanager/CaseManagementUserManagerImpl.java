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
 *     Nuxeo - initial API and implementation
 *
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.nuxeo.cm.core.usermanager;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManagerImpl;

/**
 * Specific user manager that overrides NuxeoPrincipal construction
 *
 * @author Laurent Doguin
 */
public class CaseManagementUserManagerImpl extends UserManagerImpl {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("deprecation")
    @Override
    protected NuxeoPrincipal makePrincipal(DocumentModel userEntry,
            boolean anonymous, List<String> groups) throws ClientException {
        NuxeoPrincipalImpl originalPrincipal = (NuxeoPrincipalImpl) super.makePrincipal(
                userEntry, anonymous, groups);
        CaseManagementPrincipalImpl principal = new CaseManagementPrincipalImpl(
                originalPrincipal.getName(), originalPrincipal.isAnonymous(),
                originalPrincipal.isAdministrator(), false);
        principal.setModel(originalPrincipal.getModel(), false);
        principal.setVirtualGroups(originalPrincipal.getVirtualGroups(), true);
        principal.setRoles(originalPrincipal.getRoles());
        return principal;
    }
}
