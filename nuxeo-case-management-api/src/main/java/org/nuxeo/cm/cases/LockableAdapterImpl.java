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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.Lock;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

public class LockableAdapterImpl implements LockableAdapter {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(LockableAdapterImpl.class);

    protected final DocumentModel document;

    public LockableAdapterImpl(DocumentModel document) {
        this.document = document;
    }

    public Lock lockDocument(CoreSession documentManager) throws ClientException {
        return document.setLock();
    }

    public void unlockDocument(CoreSession documentManager) throws ClientException {
        Lock lock = documentManager.getLockInfo(document.getRef());

        if (lock != null) {
            DocumentRef ref = document.getRef();
            NuxeoPrincipal userName = (NuxeoPrincipal) documentManager.getPrincipal();
            if (userName.isAdministrator() || documentManager.hasPermission(ref, SecurityConstants.EVERYTHING)
                    || userName.getName().equals(lock.getOwner())) {
                if (documentManager.hasPermission(ref, SecurityConstants.WRITE_PROPERTIES)) {
                    document.removeLock();
                } else {
                    log.error("Cannot unlock document " + document.getName());
                }
            }
        }
    }

    /**
     * Returns true if doc is not locked or current user is locker
     */
    public boolean isLocked(CoreSession documentManager) throws ClientException {
        Lock lock = documentManager.getLockInfo(document.getRef());
        if (lock == null) {
            return false;
        }
        if (lock.getOwner().equals(documentManager.getPrincipal().getName())) {
            return false;
        }
        return true;
    }

    public boolean isLockedByCurrentUser(CoreSession documentManager) throws ClientException {
        Lock lock = documentManager.getLockInfo(document.getRef());
        if (lock == null) {
            return false;
        }
        if (lock.getOwner().equals(documentManager.getPrincipal().getName())) {
            return true;
        }
        return false;
    }
}
