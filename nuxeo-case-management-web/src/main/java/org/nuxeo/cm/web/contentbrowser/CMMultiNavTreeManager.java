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
 *     arussel
 */
package org.nuxeo.cm.web.contentbrowser;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.ecm.webapp.tree.nav.MultiNavTreeManager;
import org.nuxeo.ecm.webapp.tree.nav.NavTreeDescriptor;
import org.nuxeo.ecm.webapp.tree.nav.NavTreeService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author arussel
 */
@Name("multiNavTreeManager")
@Scope(CONVERSATION)
public class CMMultiNavTreeManager extends MultiNavTreeManager {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected NuxeoPrincipal currentUser;

    protected List<NavTreeDescriptor> personnalNavTree;

    protected List<NavTreeDescriptor> outsideNavTree;

    protected List<NavTreeDescriptor> currentNavTree;

    @Create
    public void create() {
        outsideNavTree = getNavTree(currentUser.isAdministrator());
        personnalNavTree = getNavTree(true);
        setSelectedNavigationTree((currentUser.isAdministrator()) ? STD_NAV_TREE
                : "MAILBOXES_FOLDER");
        currentNavTree = outsideNavTree;
    }

    protected List<NavTreeDescriptor> getNavTree(boolean includeStdNav) {
        List<NavTreeDescriptor> result = new ArrayList<NavTreeDescriptor>();
        NavTreeService navTreeService = Framework.getLocalService(NavTreeService.class);
        result.addAll(navTreeService.getTreeDescriptors());
        if (!includeStdNav) {
            Iterator<NavTreeDescriptor> it = result.iterator();
            while (it.hasNext()) {
                if (it.next().getTreeId().equals(STD_NAV_TREE)) {
                    it.remove();
                    break;
                }
            }
        }
        return result;
    }

    public List<NavTreeDescriptor> getAvailableNavigationTrees() {
        return currentNavTree;
    }

    @Observer(EventNames.GO_PERSONAL_WORKSPACE)
    public void switchToPersonnal() {
        currentNavTree = personnalNavTree;
        setSelectedNavigationTree(STD_NAV_TREE);
    }

    @Observer(EventNames.GO_HOME)
    public void switchToOutside() {
        currentNavTree = outsideNavTree;
        setSelectedNavigationTree((currentUser.isAdministrator()) ? STD_NAV_TREE
                : "MAILBOXES_FOLDER");
    }
}
