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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.web.invalidations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.web.context.CaseManagementContextHolder;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * Base class for Seam beans that would like to invalidate some cached
 * information based on correspondence context changes.
 * <p>
 * Subclasses have to override its methods to invalidated their fields
 * accordingly.
 *
 * @author Anahide Tchertchian
 */
public abstract class CaseManagementContextBoundInstance implements
        CaseManagementContextHolder {

    private static final long serialVersionUID = 3402178528822538781L;

    private static final Log log = LogFactory.getLog(CaseManagementContextBoundInstance.class);

    // XXX AT: injected here to get getters on context objects, see if it's an
    // optim problem.
    @In(create = true, required = false)
    protected transient CaseManagementContextHolder cmContextHolder;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    protected Mailbox cachedMailbox;

    protected Case cachedEnvelope;

    protected DocumentModel cachedEmail;

    @CaseManagementContextChecker
    public void onMailboxContextChange(
            CaseManagementContextHolder correspContextHolder)
            throws ClientException {
        if (correspContextHolder == null) {
            log.error("Cannot check context: instance is null");
            return;
        }
        Mailbox currentMailbox = correspContextHolder.getCurrentMailbox();
        if (hasCacheKeyChanged(generateMailboxCacheKey(cachedMailbox),
                generateMailboxCacheKey(currentMailbox))) {
            resetMailboxCache(cachedMailbox, currentMailbox);
            cachedMailbox = currentMailbox;
        }
        Case currentEnvelope = correspContextHolder.getCurrentCase();
        if (hasCacheKeyChanged(generateCaseCacheKey(cachedEnvelope),
                generateCaseCacheKey(currentEnvelope))) {
            resetCaseCache(cachedEnvelope, currentEnvelope);
            cachedEnvelope = currentEnvelope;
        }
        DocumentModel currentEmail = correspContextHolder.getCurrentCaseItem();
        if (hasCacheKeyChanged(generateCurrentCaseItemCacheKey(cachedEmail),
                generateCurrentCaseItemCacheKey(currentEmail))) {
            resetCurrentCaseItemCache(cachedEmail, currentEmail);
            cachedEmail = currentEmail;
        }
    }

    protected boolean hasCacheKeyChanged(String cachedKey, String newKey)
            throws ClientException {
        if (cachedKey == null && newKey != null || cachedKey != null
                && newKey == null) {
            return true;
        }
        if (cachedKey == null && newKey == null) {
            return false;
        }
        return !cachedKey.equals(newKey);
    }

    protected String generateDocumentModelKey(DocumentModel doc)
            throws ClientException {
        String key = null;
        if (doc != null) {
            key = doc.getCacheKey();
        }
        return key;
    }

    protected String generateMailboxCacheKey(Mailbox mailbox)
            throws ClientException {
        String key = null;
        if (mailbox != null) {
            key = generateDocumentModelKey(mailbox.getDocument());
        }
        return key;
    }

    protected String generateCaseCacheKey(Case envelope)
            throws ClientException {
        String key = null;
        if (envelope != null && documentManager != null) {
            // FIXME: assumes envelope doc model is modified when its content
            // has changed => test also first doc key for now
            key = generateDocumentModelKey(envelope.getDocument())
                    + generateDocumentModelKey(envelope.getFirstItem(
                            documentManager).getDocument());
        }
        return key;
    }

    protected String generateCurrentCaseItemCacheKey(DocumentModel currentEmail)
            throws ClientException {
        return generateDocumentModelKey(currentEmail);
    }

    public DocumentModel getCachedCaseItem() throws ClientException {
        return cachedEmail;
    }

    public Case getCachedCase() throws ClientException {
        return cachedEnvelope;
    }

    public Mailbox getCachedMailbox() throws ClientException {
        return cachedMailbox;
    }

    public DocumentModel getCurrentCaseItem() throws ClientException {
        return cmContextHolder.getCurrentCaseItem();
    }

    public Case getCurrentCase() throws ClientException {
        return cmContextHolder.getCurrentCase();
    }

    public Mailbox getCurrentMailbox() throws ClientException {
        return cmContextHolder.getCurrentMailbox();
    }

    protected void resetMailboxCache(Mailbox cachedMailbox, Mailbox newMailbox)
            throws ClientException {
        // do nothing: to implement in subclasses
    }

    protected void resetCaseCache(Case cachedEnvelope,
            Case newEnvelope) throws ClientException {
        // do nothing: to implement in subclasses
    }

    protected void resetCurrentCaseItemCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        // do nothing: to implement in subclasses
    }

}
