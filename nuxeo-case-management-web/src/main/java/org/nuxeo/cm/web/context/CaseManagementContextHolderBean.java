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
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.cases.Case;
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

    protected CaseFolder currentMailbox;

    protected Case currentEnvelope;

    protected DocumentModel currentEmail;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @Factory(value = "currentCaseFolder", scope = ScopeType.EVENT)
    public CaseFolder getCurrentCaseFolder() throws ClientException {
        return currentMailbox;
    }

    @Factory(value = "currentCase", scope = ScopeType.EVENT)
    public Case getCurrentCase() throws ClientException {
        return currentEnvelope;
    }

    @Factory(value = "currentCaseItem", scope = ScopeType.EVENT)
    public DocumentModel getCurrentCaseItem() throws ClientException {
        if (currentEmail == null && currentEnvelope != null) {
            // lazily fetch current email
            currentEmail = currentEnvelope.getFirstItem(documentManager).getDocument();
        }
        return currentEmail;
    }

    public void setCurrentCaseFolder(CaseFolder currentMailbox) {
        this.currentMailbox = currentMailbox;
    }

    public void setCurrentCase(Case currentEnvelope) {
        this.currentEnvelope = currentEnvelope;
    }

    public void setCurrentCaseItem(DocumentModel currentEmail) {
        this.currentEmail = currentEmail;
    }

}
