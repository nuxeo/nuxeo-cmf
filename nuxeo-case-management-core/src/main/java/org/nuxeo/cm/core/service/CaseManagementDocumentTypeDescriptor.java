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
package org.nuxeo.cm.core.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author Nicolas Ulrich
 */
@XObject("documentType")
public class CaseManagementDocumentTypeDescriptor {

    @XNode("envelopeDocType")
    protected String envelopeDocType;

    @XNode("postDocType")
    protected String postDocType;

    @XNode("caseFolderDocType")
    protected String caseFolderDocType;

    @XNode("caseItemDocType")
    protected String caseItemDocType;

}
