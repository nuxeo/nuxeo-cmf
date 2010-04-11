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
 *     nulrich
 */
package org.nuxeo.cm.cases;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;

public interface LockableAdapter extends Serializable {

    public Map<String, String> lockDocument(CoreSession documentManager)
            throws ClientException;

    public void unlockDocument(CoreSession documentManager)
            throws ClientException;

    public boolean isLocked(CoreSession documentManager)
            throws ClientException;

    /**
     * Returns true if doc is locked by current user
     *
     * @throws ClientException
     */
    public boolean isLockedByCurrentUser(CoreSession documentManager)
            throws ClientException;

    public Map<String, String> getDocumentLockDetails(
            CoreSession documentManager) throws ClientException;

}
