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

package org.nuxeo.cm.web.context;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Minimal context holder.
 * <p>
 * Has to stay light-weight to be easily injected in other components.
 *
 * @author Anahide Tchertchian
 */
@Name("cmContextHolder")
@Scope(ScopeType.CONVERSATION)
public class CaseManagementContextHolderBean implements
        CaseManagementContextHolder {

    public static final String SEAM_COMPONENT_NAME = "cmContextHolder";

    private static final long serialVersionUID = 1L;

    protected Mailbox currentMailbox;

    protected Case currentEnvelope;

    protected DocumentModel currentEmail;

    protected DocumentModel currentClassificationRoot;

    protected DocumentModel currentRouteRoot;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @Override
    @Factory(value = "currentMailbox", scope = ScopeType.EVENT)
    public Mailbox getCurrentMailbox() throws ClientException {
        return currentMailbox;
    }

    @Override
    @Factory(value = "currentCase", scope = ScopeType.EVENT)
    public Case getCurrentCase() throws ClientException {
        return currentEnvelope;
    }

    @Override
    @Factory(value = "currentCaseItem", scope = ScopeType.EVENT)
    public DocumentModel getCurrentCaseItem() throws ClientException {
        if (currentEmail == null && currentEnvelope != null
                && !currentEnvelope.isEmpty()) {
            // lazily fetch current email
            currentEmail = currentEnvelope.getFirstItem(documentManager).getDocument();
        }
        return currentEmail;
    }

    @Override
    @Factory(value = "currentClassificationRoot", scope = ScopeType.EVENT)
    public DocumentModel getCurrentClassificationRoot() throws ClientException {
        return currentClassificationRoot;
    }

    @Override
    @Factory(value = "currentRouteRoot", scope = ScopeType.EVENT)
    public DocumentModel getCurrentRouteRoot() throws ClientException {
        return currentRouteRoot;
    }

    public void setCurrentMailbox(Mailbox currentMailbox) {
        this.currentMailbox = currentMailbox;
        this.currentEnvelope = null;
        this.currentEmail = null;
    }

    public void setCurrentCase(Case currentEnvelope) {
        this.currentEnvelope = currentEnvelope;
    }

    public void setCurrentCaseItem(DocumentModel currentEmail) {
        this.currentEmail = currentEmail;
    }

    public void setCurrentClassificationRoot(DocumentModel currentClassificationRoot) {
        this.currentClassificationRoot = currentClassificationRoot;
    }

    public void setCurrentRouteRoot(DocumentModel currentRouteRoot) {
        this.currentRouteRoot = currentRouteRoot;
    }

}
