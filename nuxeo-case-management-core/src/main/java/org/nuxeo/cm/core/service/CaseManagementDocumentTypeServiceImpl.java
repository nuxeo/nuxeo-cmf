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

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.service.CaseManagementDocumentTypeService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author Nicolas Ulrich
 */
public class CaseManagementDocumentTypeServiceImpl extends DefaultComponent
        implements CaseManagementDocumentTypeService {

    private static final long serialVersionUID = 1L;

    private String mailboxDocType;

    private String caseItemDocType;

    private String postDocType;

    private String envelopeDocType;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {

        CaseManagementDocumentTypeDescriptor distributionType = (CaseManagementDocumentTypeDescriptor) contribution;

        if (distributionType.postDocType != null) {
            postDocType = distributionType.postDocType;
        }

        if (distributionType.envelopeDocType != null) {
            envelopeDocType = distributionType.envelopeDocType;
        }

        if (distributionType.mailboxDocType != null) {
            mailboxDocType = distributionType.mailboxDocType;
        }

        if (distributionType.caseItemDocType != null) {
            caseItemDocType = distributionType.caseItemDocType;
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        envelopeDocType = null;
        postDocType = null;
    }

    public String getCaseType() {
        return envelopeDocType;
    }

    public String getCaseLinkType() {
        return postDocType;
    }

    public String getMailboxType() {
        return mailboxDocType;
    }

    public String getCaseItemType() {
        return caseItemDocType;
    }

    @Override
    public void markDocumentAsCase(DocumentModel document) {
        document.addFacet(CaseConstants.DISTRIBUTABLE_FACET);
    }

    @Override
    public void markDocumentAsCaseItem(DocumentModel document) {
        document.addFacet(CaseConstants.CASE_GROUPABLE_FACET);
        document.addFacet(CaseConstants.DISTRIBUTABLE_FACET);
    }
}