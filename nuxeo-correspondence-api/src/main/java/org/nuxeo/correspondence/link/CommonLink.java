/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id$
 */

package org.nuxeo.correspondence.link;

import org.nuxeo.ecm.core.api.CoreSession;

/**
 * Provide common methods for all Link.
 * 
 * @author ldoguin
 */
public interface CommonLink {

    /**
     * @param targetDocId is the linked mail doc id.
     * @return the existing link or null if it doesn't exist.
     */
    CorrespondenceLink getLink(String targetDocId);

    /**
     * Save the adapter's document.
     * 
     * @param session
     */
    void save(CoreSession session);
}