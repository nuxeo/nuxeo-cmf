/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */
package org.nuxeo.cm.web.contentbrowser;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.web.context.CaseManagementContextHolderBean;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.Sorter;
import org.nuxeo.ecm.virtualnavigation.action.MultiNavTreeManager;
import org.nuxeo.ecm.webapp.tree.DocumentTreeNode;
import org.nuxeo.ecm.webapp.tree.DocumentTreeNodeImpl;
import org.nuxeo.ecm.webapp.tree.TreeActionsBean;
import org.nuxeo.ecm.webapp.tree.TreeManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Temporary CaseManagement navigation tree handler.
 *
 * @author Laurent Doguin
 */
@Scope(CONVERSATION)
@Name("treeActions")
@Install(precedence = APPLICATION)
public class CaseTreeActionsBean extends TreeActionsBean {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseTreeActionsBean.class);

    @In(create = true)
    protected transient MultiNavTreeManager multiNavTreeManager;

    @In(create = true, required = false)
    protected transient CaseManagementContextHolderBean cmContextHolder;

    protected enum SupportedNavigationTrees {
        MAILBOXES_FOLDER, CLASSIFICATION_FOLDER, ROUTE_FOLDER
    }

    private boolean renderChildTree;

    private String treeName;

    private String docView;

    private DocumentModel rootDocument;

    public boolean getRenderChildTree() throws ClientException {
        if (!renderChildTree) {
            for (SupportedNavigationTrees navTree : SupportedNavigationTrees.values()) {
                if (navTree.toString().equals(
                        multiNavTreeManager.getSelectedNavigationTree())) {
                    treeName = multiNavTreeManager.getSelectedNavigationTree();
                    setNavigationChildTreeContext();
                    return true;
                }
            }
        }
        return false;
    }

    public void resetChildTree() {
        reset();
        treeName = null;
        rootDocument = null;
        renderChildTree = false;
        docView = null;
    }

    protected void setNavigationChildTreeContext() throws ClientException {
        String navTree = multiNavTreeManager.getSelectedNavigationTree();
        if (navTree.equals(SupportedNavigationTrees.MAILBOXES_FOLDER.toString())) {
            if (cmContextHolder.getCurrentCase() != null) {
                rootDocument = cmContextHolder.getCurrentCase().getDocument();
                docView = "cm_view";
            }
        } else if (navTree.equals(SupportedNavigationTrees.CLASSIFICATION_FOLDER.toString())) {
            rootDocument = cmContextHolder.getCurrentClassificationRoot();
            docView = "view_documents";
        } else if (navTree.equals(SupportedNavigationTrees.ROUTE_FOLDER.toString())) {
            rootDocument = cmContextHolder.getCurrentRouteRoot();
            docView = "view_documents";
        }
    }

    public List<DocumentTreeNode> getChildrenTreeRoots() throws ClientException {
        if (treeInvalidator.needsInvalidation()) {
            reset();
            treeInvalidator.invalidationDone();
        }
        List<DocumentTreeNode> currentTree = trees.get(treeName);
        if (currentTree == null) {
            currentTree = new ArrayList<DocumentTreeNode>();
            DocumentModel firstAccessibleParent = null;
            if (rootDocument != null) {
                firstAccessibleParent = rootDocument;
            }
            if (firstAccessibleParent != null) {
                Filter filter = null;
                Filter leafFilter = null;
                Sorter sorter = null;
                String pageProvider = null;
                try {
                    TreeManager treeManager = Framework.getService(TreeManager.class);
                    filter = treeManager.getFilter(treeName);
                    leafFilter = treeManager.getLeafFilter(treeName);
                    sorter = treeManager.getSorter(treeName);
                    pageProvider = treeManager.getPageProviderName(treeName);
                } catch (Exception e) {
                    log.error("Could not fetch filter or sorter for tree ", e);
                }
                DocumentTreeNode treeRoot = new DocumentTreeNodeImpl(
                        documentManager.getSessionId(), firstAccessibleParent,
                        filter, leafFilter, sorter, pageProvider);
                currentTree.add(treeRoot);
                log.debug("Tree initialized with document: "
                        + firstAccessibleParent.getId());
            } else {
                log.debug("Could not initialize the navigation tree: no parent"
                        + " found for current document");
            }
            trees.put(treeName, currentTree);
        }
        return trees.get(treeName);
    }

    public DocumentModel getRootDocument() {
        return rootDocument;
    }

    public String getDocView() {
        return docView;
    }
}
