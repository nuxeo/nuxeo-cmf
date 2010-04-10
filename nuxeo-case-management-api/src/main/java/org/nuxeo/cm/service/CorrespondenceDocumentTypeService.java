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

/**
 * Used to setup the core type of the Post, Envelope, Outgoing Mail and Incoming
 * Mail documents.
 *
 * @author Nicolas Ulrich
 *
 */
public interface CorrespondenceDocumentTypeService extends Serializable {

    /**
     * Get the core type of the Post
     *
     * @return The core type.
     */
    public String getPostType();

    /**
     * Get the core type of the Post
     *
     * @return The core type.
     */
    public String getEnvelopeType();

    /**
     * Get the core type of the Outgoing document used by default for response
     *
     * @return The core type
     */
    public String getResponseOutgoingDocType();

}