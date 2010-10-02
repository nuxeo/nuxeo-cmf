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

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.HasParticipants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.routing.api.DocumentRouteStep;
import org.nuxeo.ecm.platform.routing.api.helper.ActionableValidator;

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
    public String getTaskType() {
        return getPropertyValue(TASK_TYPE_FIELD);
    }

    @Override
    public Boolean isAutomaticValidation() {
        return getPropertyValue(AUTOMATIC_VALIDATION_FIELD);
    }

    @Override
    public void validate(CoreSession session) {
        ActionableValidator validator = new ActionableValidator(this, session);
        validator.validate();
        setDone(session);
    }

    @Override
    public void refuse(CoreSession session) {
        ActionableValidator validator = new ActionableValidator(this, session);
        validator.refuse();
        setDone(session);
    }

    @Override
    public String getRefuseOperationChainId() {
        return getPropertyValue(REFUSAL_OPERATION_CHAIN_ID);
    }

    @Override
    public String getValidateOperationChainId() {
        return getPropertyValue(VALIDATION_OPERATION_CHAIN_ID);
    }

    @Override
    public DocumentRouteStep getDocumentRouteStep(CoreSession session) {
        String stepId = getPropertyValue(CaseLinkConstants.STEP_DOCUMENT_ID_FIELD);
        try {
            return session.getDocument(new IdRef(stepId)).getAdapter(
                    DocumentRouteStep.class);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentModelList getAttachedDocuments(CoreSession session) {
        Case kase = getCase(session);
        DocumentModelList result = new DocumentModelListImpl();
        result.add(kase.getDocument());
        return result;
    }

    @Override
    public void setRefuseOperationChainId(String refuseChainId) {
        try {
            document.setPropertyValue(REFUSAL_OPERATION_CHAIN_ID, refuseChainId);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setValidateOperationChainId(String validateChainId) {
        try {
            document.setPropertyValue(VALIDATION_OPERATION_CHAIN_ID,
                    validateChainId);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setStepId(String id) {
        try {
            document.setPropertyValue(CaseLinkConstants.STEP_DOCUMENT_ID_FIELD,
                    id);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getStepId() {
        return getPropertyValue(CaseLinkConstants.STEP_DOCUMENT_ID_FIELD);
    }

    @Override
    public boolean isTodo() {
        try {
            return document.getCurrentLifeCycleState().equals(
                    CaseLink.CaseLinkState.todo.name());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDone() {
        try {
            return document.getCurrentLifeCycleState().equals(
                    CaseLink.CaseLinkState.done.name());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDone(CoreSession session) {
        try {
            session.followTransition(document.getRef(),
                    CaseLink.CaseLinkTransistion.toDone.name());
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
}
