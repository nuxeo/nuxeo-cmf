/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     mcedica
 *
 * $Id$
 */

package org.nuxeo.cm.web.actionable.caselink;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Processing actions for an actionable case link
 *
 * @author <a href="mailto:mcedica@nuxeo.com">Mariana Cedica</a>
 * */
public interface ActionableCaseLinkActions {

    String approveTask();

    String rejectTask();

    boolean isActionable(DocumentModel caseLink) throws ClientException;

}