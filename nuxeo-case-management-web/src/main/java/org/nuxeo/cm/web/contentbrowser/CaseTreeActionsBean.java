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
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.Sorter;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModel;
import org.nuxeo.ecm.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
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

    public List<DocumentTreeNode> getCaseTreeRoots(String treeName,
            DocumentModel currentDocument) throws ClientException {
        if (treeInvalidator.needsInvalidation()) {
            reset();
            treeInvalidator.invalidationDone();
        }
        List<DocumentTreeNode> currentTree = trees.get(treeName);
        if (currentTree == null) {
            currentTree = new ArrayList<DocumentTreeNode>();
            DocumentModel firstAccessibleParent = null;
            if (currentDocument != null) {
                firstAccessibleParent = currentDocument;
            }
            if (firstAccessibleParent != null) {
                Filter filter = null;
                Filter leafFilter = null;
                Sorter sorter = null;
                String pageProvider = null;
                QueryModel queryModel = null;
                QueryModel orderableQueryModel = null;
                try {
                    TreeManager treeManager = Framework.getService(TreeManager.class);
                    filter = treeManager.getFilter(treeName);
                    leafFilter = treeManager.getLeafFilter(treeName);
                    sorter = treeManager.getSorter(treeName);
                    pageProvider = treeManager.getPageProviderName(treeName);
                    QueryModelDescriptor queryModelDescriptor = treeManager.getQueryModelDescriptor(treeName);
                    queryModel = queryModelDescriptor == null ? null
                            : new QueryModel(queryModelDescriptor);
                    QueryModelDescriptor orderableQueryModelDescriptor = treeManager.getOrderableQueryModelDescriptor(treeName);
                    orderableQueryModel = orderableQueryModelDescriptor == null ? null
                            : new QueryModel(orderableQueryModelDescriptor);
                } catch (Exception e) {
                    log.error("Could not fetch filter or sorter for tree ", e);
                }
                DocumentTreeNode treeRoot = null;
                if (pageProvider == null) {
                    // compatibility code
                    treeRoot = new DocumentTreeNodeImpl(
                            documentManager.getSessionId(),
                            firstAccessibleParent, filter, leafFilter, sorter,
                            queryModel, orderableQueryModel);
                } else {
                    treeRoot = new DocumentTreeNodeImpl(
                            documentManager.getSessionId(),
                            firstAccessibleParent, filter, leafFilter, sorter,
                            pageProvider);
                }
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

}
