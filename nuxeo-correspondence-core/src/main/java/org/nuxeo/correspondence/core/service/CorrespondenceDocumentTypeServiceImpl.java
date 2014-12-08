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
package org.nuxeo.correspondence.core.service;

import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author Mariana Cedica
 */
public class CorrespondenceDocumentTypeServiceImpl extends DefaultComponent implements
        CorrespondenceDocumentTypeService {

    private static final long serialVersionUID = 1L;

    private String outgoingDocType;

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {

        CorrespondenceDocumentTypeDescriptor distributionType = (CorrespondenceDocumentTypeDescriptor) contribution;

        if (distributionType.outgoingDocType != null) {
            outgoingDocType = distributionType.outgoingDocType;
        }

    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        outgoingDocType = null;
    }

    public String getOutgoingDocType() {
        return outgoingDocType;
    }

}
