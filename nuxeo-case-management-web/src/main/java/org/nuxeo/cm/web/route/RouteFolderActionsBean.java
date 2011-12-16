/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */

package org.nuxeo.cm.web.route;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;

/**
 * Handles route folders actions
 *
 * @author Laurent Doguin
 * @since 5.5
 */
@Name("routeFolderActions")
@Scope(ScopeType.CONVERSATION)
public class RouteFolderActionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(RouteFolderActionsBean.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    protected DocumentModelList routeFolderRoots;

    @Factory(value = "routeFolderRoots", scope = EVENT)
    public DocumentModelList getRouteFolderRoots() throws ClientException {
        if (routeFolderRoots == null) {
            routeFolderRoots = new DocumentModelListImpl();
            try {
                routeFolderRoots = documentManager.query("SELECT * FROM RouteRoot");
            } catch (ClientException e) {
                log.error(e);
            }
        }
        return routeFolderRoots;
    }

}
