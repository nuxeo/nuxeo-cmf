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
 *     Nicolas Ulrich
 */
package org.nuxeo.cm.service;

import java.io.Serializable;
import java.util.Set;

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.ecm.core.api.ClientException;


/**
 * <p>
 * This service is used to add Distribution Type.
 * </p>
 *
 * <p>
 * A distribution type is declared with an identifier and 3 schema properties
 * for
 * </p>
 * <ul>
 * <li>Internal initial recipients</li>
 * <li>External initial recipients</li>
 * <li>All recipients</li>
 * </ul>
 *
 * @author Nicolas Ulrich
 *
 */
public interface CaseManagementDistributionTypeService extends Serializable {

    Set<String> getDistributionTypes();

    /**
     * Get the <b>initial internal</b> recipients property corresponding to the
     * distribution type.
     *
     * @param distributionType A Distribution Type.
     * @return The corresponding schema property name.
     * @throws ClientException
     */
    String getInternalProperty(final String distributionType) throws CaseManagementException;

    /**
     * Get the <b>initial external</b> recipients property corresponding to the
     * distribution type.
     *
     * @param distributionType A Distribution Type.
     * @return The corresponding schema property name.
     * @throws CaseManagementException
     */
    String getExternalProperty(final String distributionType) throws CaseManagementException;

    /**
     * Get the <b>all</b> recipients property corresponding to the distribution
     * type.
     *
     * @param distributionType A Distribution Type.
     * @return The corresponding schema property name.
     * @throws CaseManagementException
     */
    String getAllProperty(final String distributionType) throws CaseManagementException;

}
