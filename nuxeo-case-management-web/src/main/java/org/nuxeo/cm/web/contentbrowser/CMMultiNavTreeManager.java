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
import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.virtualnavigation.action.MultiNavTreeManager;
import org.nuxeo.ecm.virtualnavigation.action.NavTreeDescriptor;
import org.nuxeo.ecm.virtualnavigation.service.NavTreeService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author arussel
 *
 */
@Name("multiNavTreeManager")
@Scope(CONVERSATION)
public class CMMultiNavTreeManager extends MultiNavTreeManager {

    private static final long serialVersionUID = 1L;

    protected List<NavTreeDescriptor> availableNavigationTrees;

    @In(create = true, required = false)
    protected NuxeoPrincipal currentUser;

    @Create
    public void create() {
        setSelectedNavigationTree(currentUser.isAdministrator() ? STD_NAV_TREE : "TAG_CLOUD");
    }

    @Override
    public List<NavTreeDescriptor> getAvailableNavigationTrees() {
        if (availableNavigationTrees == null) {
            availableNavigationTrees = new ArrayList<NavTreeDescriptor>();
            if (currentUser.isAdministrator()) {
                availableNavigationTrees.add(new NavTreeDescriptor(
                        STD_NAV_TREE, STD_NAV_TREE_LABEL));
            }
            // add registred additional tress
            NavTreeService navTreeService = Framework.getLocalService(NavTreeService.class);
            availableNavigationTrees.addAll(navTreeService.getTreeDescriptors());

        }
        return availableNavigationTrees;
    }

}
