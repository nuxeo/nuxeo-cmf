/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.cases;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.ecm.core.api.ClientException;


/**
 * @author arussel
 *
 */
public interface HasParticipants extends Serializable {

    /**
     * Add to the list of recipients.
     *
     * @param recipients A map keyed with the message type and valued with a
     *            list of {@link CaseFolder}
     */
    void addParticipants(Map<String, List<String>> recipients);

    /**
     * Add to the list of initial internal recipients.
     *
     * @param recipients A map keyed with the message type and valued with a
     *            list of {@link CaseFolder}
     */
    void addInitialInternalParticipants(Map<String, List<String>> recipients);

    /**
     * Add to the list of initial external recipients.
     *
     * @param recipients A map keyed with the message type and valued with a
     *            list of {@link CaseFolder}
     */
    void addInitialExternalParticipants(Map<String, List<String>> recipients);

    /**
     * Get the list of all recipients keyed by type.
     *
     * @return
     */
    Map<String, List<String>> getAllParticipants();

    /**
     * get the list of initial internal recipients keyed by type.
     *
     * @return
     * @throws ClientException
     */
    Map<String, List<String>> getInitialInternalParticipants();

    /**
     * get the list of initial external recipients keyed by type.
     *
     * @return
     * @throws ClientException
     */
    Map<String, List<String>> getInitialExternalParticipants();

}
