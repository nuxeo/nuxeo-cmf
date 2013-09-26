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
 *     Nicolas Ulrich
 *
 * $Id$
 */
package org.nuxeo.correspondence.web.mail;

import org.nuxeo.ecm.core.api.ClientException;

public interface CorrespondenceSearchDocument {

    public abstract String getSearchKeywords();

    public abstract void setSearchKeywords(String searchKeywords);

    public abstract void searchMailDocument();

    public abstract String cancelEmailAttachmentsSearch()
            throws ClientException;

    /**
     * Adds selected email to current envelope documents
     * <p>
     * As long as envelope is not saved, relations are not updated, but current
     * envelope keeps track of its email documents.
     */
    public abstract String addSelectedEmails() throws ClientException;

}