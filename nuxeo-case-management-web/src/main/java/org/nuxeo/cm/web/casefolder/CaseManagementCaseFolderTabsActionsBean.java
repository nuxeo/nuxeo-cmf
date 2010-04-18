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

package org.nuxeo.cm.web.casefolder;

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
import org.nuxeo.cm.web.CaseManagementWebConstants;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
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
@Name("cmCaseFolderTabsActionsBean")
@Scope(ScopeType.CONVERSATION)
@CaseManagementContextBound
public class CaseManagementCaseFolderTabsActionsBean extends
        CaseManagementContextBoundInstance implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private static final Log log = LogFactory.getLog(CaseManagementCaseFolderTabsActionsBean.class);

    /**
     * @return All the Actions for Mailbox View
     */
    @Factory(value = "viewCaseFolderActionTabs", scope = EVENT)
    public List<Action> getViewCaseFolderActionTabs() {
        if (viewMailboxActionTabs == null) {
            viewMailboxActionTabs = webActions.getActionsList(CaseManagementWebConstants.VIEW_CASE_FOLDER_ACTION_LIST);
        }
        return viewMailboxActionTabs;
    }

    /**
     * @return All the Actions for Mailbox Management
     */
    @Factory(value = "manageCaseFolderActionTabs", scope = EVENT)
    public List<Action> getManageCaseFolderActionTabs() {
        if (manageMailboxActionTabs == null) {
            manageMailboxActionTabs = webActions.getActionsList(CaseManagementWebConstants.MANAGE_CASE_FOLDER_ACTION_LIST);
        }
        return manageMailboxActionTabs;
    }

    /**
     * @return All the Actions for Mail Distribution
     */
    @Factory(value = "distributionCaseActionTabs", scope = EVENT)
    public List<Action> getDistributionCaseActionTabs() {
        if (distributionEnvelopeActionTabs == null) {
            distributionEnvelopeActionTabs = webActions.getActionsList(CaseManagementWebConstants.DISTRIBUTION_CASE_ACTION_LIST);
        }
        return distributionEnvelopeActionTabs;
    }

    /**
     * @return current Action of Mailbox View
     */
    @Factory(value = "currentViewCaseFolderAction", scope = EVENT)
    public Action getCurrentViewCaseFolderAction() {
        if (currentViewMailboxAction == null) {
            List<Action> actions = getViewCaseFolderActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentViewMailboxAction = actions.get(0);
            }
        }
        return currentViewMailboxAction;
    }

    /**
     * @return current Action of Mailbox Management
     */
    @Factory(value = "currentDistributionCaseAction", scope = EVENT)
    public Action getCurrentDistributionCaseAction() {
        if (currentDistributionEnvelopeAction == null) {
            List<Action> actions = getDistributionCaseActionTabs();
            if (actions != null && !actions.isEmpty()) {
                currentDistributionEnvelopeAction = actions.get(0);
            }
        }
        return currentDistributionEnvelopeAction;
    }

    /**
     * @return current Action of Envelope Distribution
     */
    @Factory(value = "currentManageCaseFolderAction", scope = EVENT)
    public Action getCurrentManageCaseFolderAction() {
        if (currentManageMailboxAction == null) {
            List<Action> actions = getManageCaseFolderActionTabs();
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
    public void setCurrentViewCaseFolderAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getViewCaseFolderActionTabs();
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
    public void setCurrentManageCaseFolderAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getManageCaseFolderActionTabs();
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
    public void setCurrentDistributionCaseAction(String actionId)
            throws ClientException {
        Action target = actionManager.getAction(actionId);
        List<Action> actions = getDistributionCaseActionTabs();
        if (actions != null && !actions.isEmpty() && actions.contains(target)) {
            currentDistributionEnvelopeAction = target;
        }
    }

    /**
     * @return Mailbox Management view
     */
    public String openCaseFolderManage() {
        currentManageMailboxAction = null;
        return CaseManagementWebConstants.CASE_FOLDER_MANAGE;
    }

    /**
     * @return Mailbox View view
     */
    public String openCaseFolderView() {
        currentViewMailboxAction = null;
        return CaseManagementWebConstants.CASE_FOLDER_VIEW;
    }

    @Override
    protected void resetCaseFolderCache(CaseFolder cachedMailbox, CaseFolder newMailbox)
            throws ClientException {
        viewMailboxActionTabs = null;
        distributionEnvelopeActionTabs = null;
    }

}
