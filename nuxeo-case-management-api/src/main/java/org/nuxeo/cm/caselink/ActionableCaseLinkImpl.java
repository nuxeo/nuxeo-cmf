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
 *     mcedica
 */
package org.nuxeo.cm.caselink;

import static org.nuxeo.cm.caselink.CaseLinkConstants.AUTOMATIC_VALIDATION_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.DUE_DATE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.REFUSAL_OPERATION_CHAIN_ID;
import static org.nuxeo.cm.caselink.CaseLinkConstants.TASK_TYPE_FIELD;
import static org.nuxeo.cm.caselink.CaseLinkConstants.VALIDATION_OPERATION_CHAIN_ID;

import java.util.Date;

import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:mcedica@nuxeo.com">Mariana Cedica</a>
 */
public class ActionableCaseLinkImpl extends CaseLinkImpl implements
        ActionableCaseLink {

    public ActionableCaseLinkImpl(DocumentModel doc,
            HasParticipants recipientAdapted) {
        super(doc, recipientAdapted);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Date getDueDate() {
       return getPropertyValue(DUE_DATE_FIELD);
    }

    @Override
    public String getRefusalOperationChainId() {
        return getPropertyValue(REFUSAL_OPERATION_CHAIN_ID);
    }

    @Override
    public String getTaskType() {
        return getPropertyValue(TASK_TYPE_FIELD);
    }

    @Override
    public String getvalidationOperationChainId() {
        return getPropertyValue(VALIDATION_OPERATION_CHAIN_ID);  
        }

    @Override
    public Boolean isAutomaticValidation() {
       return getPropertyValue(AUTOMATIC_VALIDATION_FIELD);
    }
    
}
