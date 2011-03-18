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

package org.nuxeo.cm.web.caseitem;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.ClientException;

/**
 * @author Anahide Tchertchian
 */
public interface CaseManagementDocumentActions extends Serializable {

    /**
     * Creates a new document from current changeable document.
     * <p>
     * Document will be created in a hierarchy yyyy/mm/dd below current
     * document (mail root)
     */
    String createCaseItemInCase() throws ClientException;

    /**
     * Return the Layout Mode for the current user. If the user can write the
     * document and the document is not locked, return Edit. Otherwise View.
     *
     * @throws ClientException
     */
    String getCaseItemLayoutMode() throws ClientException;

    /**
     * Check if the current user can edit the document (right + lock)
     *
     * @throws ClientException
     */
    boolean getCanEditCurrentCaseItem() throws ClientException;

    /**
     * Saves changes hold by the changeableDocument document model.
     */
    void updateCurrentCaseItem() throws ClientException;

}
