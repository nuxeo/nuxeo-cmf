/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.cm.web.helper;

import java.security.Principal;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * CM virtual user manager actions bean.
 *
 * @author Laurent Doguin
 */
@Name("cmVirtualUserManager")
@Scope(ScopeType.SESSION)
public class CMVirtualUserManager {

    private static final long serialVersionUID = 1L;

    public Boolean isCurrentUserVirtual = null;

    @In(required = false)
    protected transient Principal currentUser;

    /**
     * Lookup currentUser directly in userDirectory to see if it is virtual or not.
     *
     * @param currentUser
     * @return true if currentUser is virtual, false otherwise.
     * @throws Exception
     */
    public boolean isCurrentUserVirtual() {
        if (isCurrentUserVirtual == null) {
            if (currentUser == null) {
                return true;
            }
            Session userDirSession = null;
            try {
                DirectoryService dirService = Framework.getService(DirectoryService.class);
                userDirSession = dirService.open("userDirectory");
                if (userDirSession.getEntry(currentUser.getName()) == null) {
                    isCurrentUserVirtual = true;
                } else {
                    isCurrentUserVirtual = false;
                }
            } catch (DirectoryException e) {
                throw new ClientException(e);
            } finally {
                if (userDirSession != null) {
                    userDirSession.close();
                }
            }
        }
        return isCurrentUserVirtual;
    }

}
