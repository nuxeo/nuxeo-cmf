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

package org.nuxeo.cm.web.mailbox;

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
import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBound;
import org.nuxeo.cm.web.invalidations.CorrespondenceContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;


/**
 * Handle tabs behaviors for Mailbox View and Mailbox Management.
 * 
 * @author nulrich
 * 
 */
@Name("correspMailboxTabsActionsBean")
@Scope(ScopeType.CONVERSATION)
@CorrespondenceContextBound
public class CorrespondenceMailboxTabsActionsBean extends
        CorrespondenceContextBoundInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String MAILBOX_VIEW = "mailbox_view";

    public static final String MAILBOX_MANAGE = "mailbox_manage";

    protected List<Action> viewMailboxActionTabs;

    protected Action currentViewMailboxAction;

    protected List<Action> manageMailboxActionTabs;

    protected Action currentManageMailboxAction;

    protected List<Action> distributionEnvelopeActionTabs;

    protected Action currentDistributionEnvelopeAction;

    @In(required = true, create = true)
    protected transient ActionManager actionManager;

    @In(create = true, required = false)
    protected transient WebActions webActions;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CorrespondenceMailboxTabsActionsBean.class);

    /**
     * @return All the Actions for Mailbox View
     */
    @Factory(value = "viewMailboxActionTabs", scope = EVENT)
    public List<Action> getViewMailboxActionTabs() {
        if (viewMailboxActionTabs == null) {
            viewMailboxActionTabs = webActions.getActionsList("VIEW_MAILBOX_ACTION_LIST");
        }
        return viewMailboxActionTabs;
    }

    /**
     * @return All the Actions for Mailbox Management
     */
    @Factory(value = "manageMailboxActionTabs", scope = EVENT)
    public List<Action> getManageMailboxActionTabs() {
        if (manageMailboxActionTabs == null) {
            manageMailboxActionTabs = webActions.getActionsList("MANAGE_MAILBOX_ACTION_LIST");
        }
        return manageMailboxActionTabs;
    }

    /**
     * @return All the Actions for Mail Distribution
     */
    @Factory(value = "distributionEnvelopeActionTabs", scope = EVENT)
    public List<Action> getDistributionEnvelopeActionTabs() {
        if (distributionEnvelopeActionTabs == null) {
            distributionEnvelopeActionTabs = webActions.getActionsList("DISTRIBUTION_ENVELOPE_ACTION_LIST");
        }
        return distributionEnvelopeActionTabs;
    }

    /**
     * @return current Action of Mailbox View
     */
    @Factory(value = "currentViewMailboxAction", scope = EVENT)
    public Action getCurrentViewMailboxAction() {
        if (currentViewMailboxAction == null) {
            List<Action> actions = getViewMailboxActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentViewMailboxAction = actions.get(0);
            }
        }
        return currentViewMailboxAction;
    }

    /**
     * @return current Action of Mailbox Management
     */
    @Factory(value = "currentDistributionEnvelopeAction", scope = EVENT)
    public Action getCurrentDistributionEnvelopeAction() {
        if (currentDistributionEnvelopeAction == null) {
            List<Action> actions = getDistributionEnvelopeActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentDistributionEnvelopeAction = actions.get(0);
            }
        }
        return currentDistributionEnvelopeAction;
    }

    /**
     * @return current Action of Envelope Distribution
     */
    @Factory(value = "currentManageMailboxAction", scope = EVENT)
    public Action getCurrentManageMailboxAction() {
        if (currentManageMailboxAction == null) {
            List<Action> actions = getManageMailboxActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentManageMailboxAction = actions.get(0);
            }
        }
        return currentManageMailboxAction;
    }

    /**
     * Set the current tab (Action) for Mailbox Management
     * 
     * @param actionId
     * @throws ClientException
     */
    public void setCurrentViewMailboxAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getViewMailboxActionTabs();
        if (actions != null && !actions.isEmpty() && actions.contains(target)) {
            currentViewMailboxAction = target;
        }

    }

    /**
     * Set the current tab (Action) for Mailbox View
     * 
     * @param actionId
     * @throws ClientException
     */
    public void setCurrentManageMailboxAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getManageMailboxActionTabs();
        if (actions != null && !actions.isEmpty() && actions.contains(target)) {
            currentManageMailboxAction = target;
        }
    }

    /**
     * Set the current tab (Action) for Distribution Envelope view
     * 
     * @param actionId
     * @throws ClientException
     */
    public void setCurrentDistributionEnvelopeAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getDistributionEnvelopeActionTabs();
        if (actions != null && !actions.isEmpty() && actions.contains(target)) {
            currentDistributionEnvelopeAction = target;
        }
    }

    /**
     * @return Mailbox Management view
     */
    public String openMailboxManage() {
        currentManageMailboxAction = null;
        return MAILBOX_MANAGE;
    }

    /**
     * @return Mailbox View view
     */
    public String openMailboxView() {
        currentViewMailboxAction = null;
        return MAILBOX_VIEW;
    }

    @Override
    protected void resetMailboxCache(CaseFolder cachedMailbox, CaseFolder newMailbox)
            throws ClientException {
        viewMailboxActionTabs = null;
        distributionEnvelopeActionTabs = null;
    }

}
