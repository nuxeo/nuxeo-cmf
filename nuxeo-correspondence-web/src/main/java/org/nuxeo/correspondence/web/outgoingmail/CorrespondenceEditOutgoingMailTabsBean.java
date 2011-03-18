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

package org.nuxeo.correspondence.web.outgoingmail;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;

/**
 * Handle tabs behaviors for Outgoing Mail Tabs.
 *
 * @author Nicolas Ulrich
 */
@Name("editOutgoingMailTabsBean")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CorrespondenceEditOutgoingMailTabsBean extends
        CaseManagementContextBoundInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String ACTION_LIST = "EDIT_OUTGOING_MAIL_ACTION_LIST";

    protected List<Action> editOutgoingMailActionTabs;

    protected Action currentEditEnvelopeAction;

    @In(required = true, create = true)
    protected transient ActionManager actionManager;

    @In(create = true, required = false)
    protected transient WebActions webActions;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CorrespondenceEditOutgoingMailTabsBean.class);

    /**
     * Return All the tabs.
     */
    @Factory(value = "editOutgoingMailActionTabs", scope = EVENT)
    public List<Action> getActionTabs() {
        if (editOutgoingMailActionTabs == null) {
            editOutgoingMailActionTabs = webActions.getActionsList(ACTION_LIST);
        }
        return editOutgoingMailActionTabs;
    }

    /**
     * Return the current tab.
     */
    @Factory(value = "currentEditOutgoingMailAction", scope = EVENT)
    public Action getCurrentActionTab() {
        if (currentEditEnvelopeAction == null) {
            List<Action> actions = getActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentEditEnvelopeAction = actions.get(0);
            }
        }
        return currentEditEnvelopeAction;
    }

    /**
     * Set the current tab.
     *
     * @param actionId
     * @throws ClientException
     */
    public void setCurrentActionTab(String actionId) throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getActionTabs();
        if (actions != null && !actions.isEmpty() && actions.contains(target)) {
            currentEditEnvelopeAction = target;
        }
    }

}
