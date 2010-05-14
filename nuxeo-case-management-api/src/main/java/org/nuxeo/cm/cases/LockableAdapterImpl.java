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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

public class LockableAdapterImpl implements LockableAdapter {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(LockableAdapterImpl.class);

    protected DocumentModel document;

    public LockableAdapterImpl(DocumentModel document) {
        this.document = document;
    }

    /**
     * Locks document and returns new lock details
     */
    public Map<String, String> lockDocument(CoreSession documentManager)
            throws ClientException {
        StringBuilder result = new StringBuilder();
        result.append(documentManager.getPrincipal().getName()).append(':').append(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));
        String lockKey = result.toString();
        // unlock on doc otherwise it does not get updated
        document.setLock(lockKey);
        // save on session, assuming it it the same than client's...
        documentManager.save();
        return getDocumentLockDetails(documentManager);
    }

    public void unlockDocument(CoreSession documentManager)
            throws ClientException {

        Map<String, String> lockDetails = getDocumentLockDetails(documentManager);

        if (lockDetails != null && !lockDetails.isEmpty()) {
            DocumentRef ref = document.getRef();
            NuxeoPrincipal userName = (NuxeoPrincipal) documentManager.getPrincipal();
            if (userName.isAdministrator()
                    || documentManager.hasPermission(ref,
                            SecurityConstants.EVERYTHING)
                    || userName.getName().equals(
                            lockDetails.get("LockActions.LOCKER"))) {
                if (documentManager.hasPermission(ref,
                        SecurityConstants.WRITE_PROPERTIES)) {
                    // unlock on doc otherwise it does not get updated
                    document.unlock();
                    // save on session, assuming it it the same than
                    // client's...
                    documentManager.save();
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

        Map<String, String> lockDetails = getDocumentLockDetails(documentManager);

        if (lockDetails != null && !lockDetails.isEmpty()) {
            NuxeoPrincipal userName = (NuxeoPrincipal) documentManager.getPrincipal();
            if (userName.getName().equals(lockDetails.get("LockActions.LOCKER"))) {
                return false;
            }
            return true;
        }

        return false;
    }

    public boolean isLockedByCurrentUser(CoreSession documentManager) throws ClientException {

        Map<String, String> lockDetails = getDocumentLockDetails(documentManager);

        if (lockDetails != null && !lockDetails.isEmpty()) {
            NuxeoPrincipal userName = (NuxeoPrincipal) documentManager.getPrincipal();
            if (userName.getName().equals(lockDetails.get("LockActions.LOCKER"))) {
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getDocumentLockDetails(
            CoreSession documentManager) throws ClientException {
        Map<String, String> lockDetails = new HashMap<String, String>();
        if (document != null) {
            DocumentRef ref = document.getRef();
            String documentKey = documentManager.getLock(ref);
            if (documentKey != null) {
                String[] values = documentKey.split(":");
                lockDetails.put("LockActions.LOCKER", values[0]);
                lockDetails.put("LockActions.LOCK_TIME", values[1]);
            }
        }
        return lockDetails;
    }

}
