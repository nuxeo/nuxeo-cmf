/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.cm.service;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Used to setup the core type of the Post, Envelope and Incoming Mail documents.
 *
 * @author Nicolas Ulrich
 */
public interface CaseManagementDocumentTypeService extends Serializable {

    // FIXME: this javadoc is not consistent with the class-level javadoc.
    /**
     * Gets the core type of the Post
     *
     * @return The core type.
     */
    String getCaseLinkType();

    // FIXME: this is the same as the javadoc above.
    /**
     * Gets the core type of the Post
     *
     * @return The core type.
     */
    String getCaseType();

    String getMailboxType();

    String getCaseItemType();

    /***
     * Adds the needed facets to the given document in order to be used as a caseItem
     *
     * @since 5.5
     * @param document
     */
    void markDocumentAsCaseItem(DocumentModel document);

    /***
     * Adds the needed facets to the given document in order to be used as a case
     *
     * @since 5.5
     * @param document
     */
    void markDocumentAsCase(DocumentModel document);

}
