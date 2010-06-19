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
package org.nuxeo.cm.web.service;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.Serializable;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.runtime.api.Framework;
/**
 * @author arussel
 *
 */
@Name("caseDistributionService")
@Scope(APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class CaseDistributionServiceBusinessDelegate implements
        Serializable {

    private static final long serialVersionUID = 1L;

    protected CaseDistributionService service;

    /**
     * Acquires a new {@link CaseDistributionService} reference. The related EJB
     * may be deployed on a local or remote AppServer.
     */
    @Unwrap
    public CaseDistributionService getDistributionService()
            throws ClientException {
        if (service == null) {
            try {
                service = Framework.getService(CaseDistributionService.class);
            } catch (Exception e) {
                throw new ClientException(
                        "Error connecting to casemanagement service", e);
            }
            if (service == null) {
                throw new ClientException("Correspondence service not bound");
            }
        }
        return service;
    }

}
