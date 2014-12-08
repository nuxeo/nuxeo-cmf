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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.Lock;

public interface LockableAdapter extends Serializable {

    Lock lockDocument(CoreSession documentManager) throws ClientException;

    void unlockDocument(CoreSession documentManager) throws ClientException;

    boolean isLocked(CoreSession documentManager) throws ClientException;

    /**
     * Returns true if doc is locked by current user.
     */
    boolean isLockedByCurrentUser(CoreSession documentManager) throws ClientException;
}
