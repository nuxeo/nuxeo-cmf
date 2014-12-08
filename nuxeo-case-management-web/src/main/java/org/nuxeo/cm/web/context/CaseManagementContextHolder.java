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

import java.io.Serializable;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Holds references to current mailbox, current envelope and current email in the envelope.
 */
public interface CaseManagementContextHolder extends Serializable {

    Mailbox getCurrentMailbox() throws ClientException;

    Case getCurrentCase() throws ClientException;

    DocumentModel getCurrentCaseItem() throws ClientException;

    /**
     * @since 5.5
     */
    DocumentModel getCurrentRouteRoot() throws ClientException;

    /**
     * @since 5.5
     */
    DocumentModel getCurrentClassificationRoot() throws ClientException;

}
