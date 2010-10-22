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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.web.cases;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.web.distribution.CaseManagementDistributionActionsBean;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.mailbox.CaseManagementAbstractActionsBean;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Nicolas Ulrich
 */
@Name("cmCaseActions")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
@Install(precedence = Install.FRAMEWORK)
public class CaseManagementCaseActionsBean extends
        CaseManagementAbstractActionsBean {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CaseManagementDistributionActionsBean.class);

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(required = true, create = true)
    protected NavigationContext navigationContext;

    /**
     * @return true if this envelope is still in draft
     */
    public boolean isInitialCase() throws ClientException {
        Case env = getCurrentCase();

        if (env != null) {
            return getCurrentCase().isDraft();
        } else {
            return false;
        }
    }

    /**
     * Removes a mail from the current envelope.
     *
     * @param doc the mail to remove
     */
    public void removeCaseItem(DocumentModel doc) throws ClientException {
        Case currentEnvelope = getCurrentCase();
        CaseItem item = doc.getAdapter(CaseItem.class);
        currentEnvelope.removeCaseItem(item, documentManager);
    }

    public DocumentRoutingService getDocumentRoutingService() {
        try {
            return Framework.getService(DocumentRoutingService.class);

        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    protected void resetCaseCache(Case cachedEnvelope, Case newEnvelope)
            throws ClientException {
        super.resetCaseCache(cachedEnvelope, newEnvelope);
    }

    /**
     * Returns true if we have an empty case
     * */
    public boolean isEmptyCase() throws ClientException {
        return getCurrentCase().isEmpty();
    }

}