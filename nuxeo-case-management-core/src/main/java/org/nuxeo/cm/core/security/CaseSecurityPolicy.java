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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.core.security;

import java.security.Principal;

import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.lifecycle.LifeCycleException;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery.Transformer;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;

/**
 * Case Security Policy. Deny Access if the case is in Archived state.
 */
public class CaseSecurityPolicy extends AbstractSecurityPolicy {

    @Override
    public Access checkPermission(Document doc, ACP mergedAcp,
            Principal principal, String permission,
            String[] resolvedPermissions, String[] additionalPrincipals) {
        String docLifeCycleState = "";
        try {
            docLifeCycleState = doc.getLifeCycleState();
        } catch (LifeCycleException e) {
            throw new RuntimeException(
                    "Could not get document life cycle state", e);
        }
        if (CaseLifeCycleConstants.STATE_ARCHIVE.equals(docLifeCycleState)) {
            for (String resolvedPermission : resolvedPermissions) {
                if (resolvedPermission.equals(SecurityConstants.WRITE)) {
                    return Access.DENY;
                }
            }
        }
        return Access.UNKNOWN;
    }

    @Override
    public Transformer getQueryTransformer() {
        return Transformer.IDENTITY;
    }
}
