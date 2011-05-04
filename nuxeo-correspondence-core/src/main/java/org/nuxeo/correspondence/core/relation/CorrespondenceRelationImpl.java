/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.correspondence.core.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.correspondence.relation.CorrespondenceRelation;
import org.nuxeo.correspondence.relation.CorrespondenceRelationConstants;
import org.nuxeo.correspondence.relation.CorrespondenceStatement;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * @author ldoguin
 */
public class CorrespondenceRelationImpl implements CorrespondenceRelation {

    protected DocumentModel document;

    public CorrespondenceRelationImpl(DocumentModel doc) {
        document = doc;
    }

    @Override
    public List<CorrespondenceStatement> getIsEnvelopeOfResourceRelation() {
        List<Map<String, Serializable>> relations = getPropertyValue(
                CorrespondenceRelationConstants.IS_ENVELOPE_OF_RESOURCE_PROPERTY_NAME);
        return wrapRelationMap(relations);
    }

    @Override
    public void resetIsEnvelopeOfResourceRelation() {
        try {
            document.setPropertyValue(
                    CorrespondenceRelationConstants.IS_ENVELOPE_OF_RESOURCE_PROPERTY_NAME,
                    null);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public List<CorrespondenceStatement> getEmailIsAnswerToRelation() {
        List<Map<String, Serializable>> relations = getPropertyValue(
                CorrespondenceRelationConstants.EMAIL_IS_ANSWER_TO_PROPERTY_NAME);
        return wrapRelationMap(relations);
    }

    protected List<CorrespondenceStatement> wrapRelationMap(
            List<Map<String, Serializable>> relations) {
        List<CorrespondenceStatement> statements = new ArrayList<CorrespondenceStatement>();
        for (Map<String, Serializable> map : relations) {
            statements.add(new CorrespondenceStatement(map));
        }
        return statements;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getPropertyValue(String value) {
        try {
            return (T) document.getPropertyValue(value);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public DocumentModel getDocument() {
        return document;
    }

    @Override
    public void addIsEnvelopeOfResourceRelation(CorrespondenceStatement stmt) {
        addStatement(
                CorrespondenceRelationConstants.IS_ENVELOPE_OF_RESOURCE_PROPERTY_NAME,
                stmt);
    }

    @Override
    public void addEmailIsAnswerToRelation(CorrespondenceStatement stmt) {
        addStatement(
                CorrespondenceRelationConstants.EMAIL_IS_ANSWER_TO_PROPERTY_NAME,
                stmt);
    }

    protected void addStatement(String xpath, CorrespondenceStatement stmt) {
        List<CorrespondenceStatement> relations = getPropertyValue(xpath);
        if (relations == null) {
            relations = new ArrayList<CorrespondenceStatement>();
        }
        relations.add(stmt);
        try {
            document.setPropertyValue(xpath, (Serializable) relations);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public void addAllIsEnvelopeOfResourceRelation(
            List<CorrespondenceStatement> relations) {
        addAllRelationList(
                CorrespondenceRelationConstants.IS_ENVELOPE_OF_RESOURCE_PROPERTY_NAME,
                relations);
    }

    @Override
    public void addAllEmailIsAnswerToRelation(
            List<CorrespondenceStatement> relations) {
        addAllRelationList(
                CorrespondenceRelationConstants.EMAIL_IS_ANSWER_TO_PROPERTY_NAME,
                relations);
    }

    private void addAllRelationList(String xPath,
            List<CorrespondenceStatement> relations) {
        List<CorrespondenceStatement> relationList = getPropertyValue(xPath);
        if (relationList == null) {
            relationList = new ArrayList<CorrespondenceStatement>();
        }
        relationList.addAll(relationList);
        try {
            document.setPropertyValue(xPath, (Serializable) relations);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        
    }
}
