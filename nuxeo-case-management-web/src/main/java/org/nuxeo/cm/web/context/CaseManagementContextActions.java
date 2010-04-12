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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.web.context;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Correspondence context manager.
 * <p>
 * Primary interface to the actual bean holding context variables.
 *
 * @author Anahide Tchertchian
 */
public interface CaseManagementContextActions {

    /**
     * Sets current email identifier
     */
    void setCurrentEmailId(String id) throws ClientException;

    /**
     * Returns current email identifier
     */
    String getCurrentEmailId() throws ClientException;

    /**
     * Sets current context according to current document information
     */
    void currentDocumentChanged(DocumentModel newDocument)
            throws ClientException;

}