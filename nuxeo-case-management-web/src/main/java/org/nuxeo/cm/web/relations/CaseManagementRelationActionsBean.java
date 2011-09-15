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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.web.relations;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PagedDocumentsProvider;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.search.api.client.querymodel.QueryModel;
import org.nuxeo.ecm.platform.relations.api.Literal;
import org.nuxeo.ecm.platform.relations.api.Node;
import org.nuxeo.ecm.platform.relations.api.QNameResource;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Resource;
import org.nuxeo.ecm.platform.relations.api.ResourceAdapter;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.ecm.platform.relations.api.Subject;
import org.nuxeo.ecm.platform.relations.api.event.RelationEvents;
import org.nuxeo.ecm.platform.relations.api.impl.LiteralImpl;
import org.nuxeo.ecm.platform.relations.api.impl.QNameResourceImpl;
import org.nuxeo.ecm.platform.relations.api.impl.RelationDate;
import org.nuxeo.ecm.platform.relations.api.impl.ResourceImpl;
import org.nuxeo.ecm.platform.relations.api.impl.StatementImpl;
import org.nuxeo.ecm.platform.relations.api.util.RelationConstants;
import org.nuxeo.ecm.platform.relations.web.NodeInfo;
import org.nuxeo.ecm.platform.relations.web.NodeInfoImpl;
import org.nuxeo.ecm.platform.relations.web.StatementInfo;
import org.nuxeo.ecm.platform.relations.web.StatementInfoComparator;
import org.nuxeo.ecm.platform.relations.web.StatementInfoImpl;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.model.impl.SelectDataModelImpl;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;
import org.nuxeo.ecm.webapp.querymodel.QueryModelActions;
import org.nuxeo.runtime.api.Framework;

/**
 * Retrieves relations for current email.
 * 
 * @author Anahide Tchertchian
 */
@Name("cmRelationActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CaseManagementContextBound
public class CaseManagementRelationActionsBean extends
        CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseManagementRelationActionsBean.class);

    public static final String CURRENT_CASE_ITEM_RELATION_SEARCH_QUERYMODEL = "CURRENT_CASE_ITEM_RELATION_SEARCH";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected RelationManager relationManager;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected transient QueryModelActions queryModelActions;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(required = false)
    protected transient Principal currentUser;

    // statements lists
    protected List<Statement> incomingStatements;

    protected List<StatementInfo> incomingStatementsInfo;

    protected List<Statement> outgoingStatements;

    protected List<StatementInfo> outgoingStatementsInfo;

    // fields for relation creation

    protected Boolean showCreateForm = false;

    protected String predicateUri;

    protected String comment;

    protected String searchKeywords;

    protected List<String> targetCreationDocuments;

    public DocumentModel getDocumentModel(Node node) throws ClientException {
        if (node.isQNameResource()) {
            QNameResource resource = (QNameResource) node;
            Map<String, Serializable> context = new HashMap<String, Serializable>();
            context.put(ResourceAdapter.CORE_SESSION_ID_CONTEXT_KEY,
                    documentManager.getSessionId());
            Object o = relationManager.getResourceRepresentation(
                    resource.getNamespace(), resource, context);
            if (o instanceof DocumentModel) {
                return (DocumentModel) o;
            }
        }
        return null;
    }

    public QNameResource getDocumentResource(DocumentModel document)
            throws ClientException {
        QNameResource documentResource = null;
        if (document != null) {
            documentResource = (QNameResource) relationManager.getResource(
                    RelationConstants.DOCUMENT_NAMESPACE, document, null);
        }
        return documentResource;
    }

    protected List<StatementInfo> getStatementsInfo(List<Statement> statements)
            throws ClientException {
        if (statements == null) {
            return null;
        }
        List<StatementInfo> infoList = new ArrayList<StatementInfo>();
        for (Statement statement : statements) {
            Subject subject = statement.getSubject();
            // TODO: filter on doc visibility (?)
            NodeInfo subjectInfo = new NodeInfoImpl(subject,
                    getDocumentModel(subject), true);
            Resource predicate = statement.getPredicate();
            Node object = statement.getObject();
            NodeInfo objectInfo = new NodeInfoImpl(object,
                    getDocumentModel(object), true);
            StatementInfo info = new StatementInfoImpl(statement, subjectInfo,
                    new NodeInfoImpl(predicate), objectInfo);
            infoList.add(info);
        }
        return infoList;
    }

    public List<StatementInfo> getIncomingStatementsInfo()
            throws ClientException {
        if (incomingStatementsInfo != null) {
            return incomingStatementsInfo;
        }
        DocumentModel currentDoc = getCurrentCaseItem();
        Resource docResource = getDocumentResource(currentDoc);
        if (docResource == null) {
            incomingStatements = Collections.emptyList();
            incomingStatementsInfo = Collections.emptyList();
        } else {
            Statement pattern = new StatementImpl(null, null, docResource);
            incomingStatements = relationManager.getStatements(
                    RelationConstants.GRAPH_NAME, pattern);
            incomingStatementsInfo = getStatementsInfo(incomingStatements);
            // sort by modification date, reverse
            Comparator<StatementInfo> comp = Collections.reverseOrder(new StatementInfoComparator());
            Collections.sort(incomingStatementsInfo, comp);
        }
        return incomingStatementsInfo;
    }

    public SelectDataModel getIncomingStatementsInfoSelectModel()
            throws ClientException {
        return new SelectDataModelImpl("cm_incoming_relations",
                getIncomingStatementsInfo(), null);
    }

    public List<StatementInfo> getOutgoingStatementsInfo()
            throws ClientException {
        if (outgoingStatementsInfo != null) {
            return outgoingStatementsInfo;
        }
        DocumentModel currentDoc = getCurrentCaseItem();
        Resource docResource = getDocumentResource(currentDoc);
        if (docResource == null) {
            outgoingStatements = Collections.emptyList();
            outgoingStatementsInfo = Collections.emptyList();
        } else {
            Statement pattern = new StatementImpl(docResource, null, null);
            outgoingStatements = relationManager.getStatements(
                    RelationConstants.GRAPH_NAME, pattern);
            outgoingStatementsInfo = getStatementsInfo(outgoingStatements);
            // sort by modification date, reverse
            Comparator<StatementInfo> comp = Collections.reverseOrder(new StatementInfoComparator());
            Collections.sort(outgoingStatementsInfo, comp);
        }
        return outgoingStatementsInfo;
    }

    public SelectDataModel getOutgoingStatementsInfoSelectModel()
            throws ClientException {
        return new SelectDataModelImpl("cm_outgoing_relations",
                getOutgoingStatementsInfo(), null);
    }

    public void resetStatements() {
        incomingStatements = null;
        incomingStatementsInfo = null;
        outgoingStatements = null;
        outgoingStatementsInfo = null;
    }

    // getters & setters for creation items

    public Boolean getShowCreateForm() {
        return showCreateForm;
    }

    public void toggleCreateForm(ActionEvent event) {
        showCreateForm = !showCreateForm;
    }

    public String getPredicateUri() {
        return predicateUri;
    }

    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public List<String> getTargetCreationDocuments() {
        if (targetCreationDocuments == null) {
            targetCreationDocuments = new ArrayList<String>();
        }
        return targetCreationDocuments;
    }

    public void setTargetCreationDocuments(List<String> targetCreationDocuments) {
        this.targetCreationDocuments = targetCreationDocuments;
    }

    protected void resetCreateFormValues() {
        showCreateForm = false;
        predicateUri = null;
        comment = null;
        searchKeywords = null;
        targetCreationDocuments = null;
    }

    protected void notifyEvent(String eventId, DocumentModel source,
            Map<String, Serializable> options, String comment) {

        EventProducer evtProducer = null;

        try {
            evtProducer = Framework.getService(EventProducer.class);
        } catch (Exception e) {
            log.error("Unable to get EventProducer to send event notification",
                    e);
        }

        DocumentEventContext docCtx = new DocumentEventContext(documentManager,
                documentManager.getPrincipal(), source);
        options.put("category", RelationEvents.CATEGORY);
        options.put("comment", comment);

        try {
            evtProducer.fireEvent(docCtx.newEvent(eventId));
        } catch (ClientException e) {
            log.error("Error while trying to send notification message", e);
        }
    }

    public DocumentModel getDocumentModel(String id) throws ClientException {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return documentManager.getDocument(new IdRef(id));
    }

    public List<DocumentModel> getDocumentRelationSuggestions(Object input)
            throws ClientException {
        try {
            String docId = navigationContext.getCurrentDocument().getId();
            QueryModel qm = queryModelActions.get(CURRENT_CASE_ITEM_RELATION_SEARCH_QUERYMODEL);
            Object[] params = { docId, input };
            PagedDocumentsProvider pageProvider = qm.getResultsProvider(
                    documentManager, params, null);
            return pageProvider.getCurrentPage();
        } catch (Exception e) {
            throw new ClientException("error searching for documents", e);
        }
    }

    public String addStatement() throws ClientException {
        DocumentModel currentDoc = getCurrentCaseItem();
        Resource documentResource = getDocumentResource(currentDoc);
        if (documentResource == null) {
            throw new ClientException(
                    "Document resource could not be retrieved");
        }

        Resource predicate = new ResourceImpl(predicateUri);
        List<String> targetCreationDocs = getTargetCreationDocuments();
        if (targetCreationDocs == null || targetCreationDocs.isEmpty()) {
            throw new ClientException("No target documents");
        }

        Literal commentLiteral = null;
        String eventComment = null;
        if (comment != null) {
            comment = comment.trim();
            if (comment.length() > 0) {
                eventComment = comment;
                commentLiteral = new LiteralImpl(eventComment);
            }
        }

        Literal now = RelationDate.getLiteralDate(new Date());
        Literal authorLiteral = null;
        if (currentUser != null) {
            authorLiteral = new LiteralImpl(currentUser.getName());
        }

        // add statements to the graph
        List<Statement> stmts = new ArrayList<Statement>();
        String repositoryName = currentDoc.getRepositoryName();
        boolean alreadySet = false;
        boolean someSet = false;
        for (String targetDocId : targetCreationDocs) {
            String localName = repositoryName + "/" + targetDocId;
            Resource object = new QNameResourceImpl(
                    RelationConstants.DOCUMENT_NAMESPACE, localName);

            Statement stmt = new StatementImpl(documentResource, predicate,
                    object);

            if (commentLiteral != null) {
                stmt.addProperty(RelationConstants.COMMENT, commentLiteral);
            }
            stmt.addProperty(RelationConstants.CREATION_DATE, now);
            stmt.addProperty(RelationConstants.MODIFICATION_DATE, now);
            if (authorLiteral != null) {
                stmt.addProperty(RelationConstants.AUTHOR, authorLiteral);
            }

            if (outgoingStatements.contains(stmt)) {
                alreadySet = true;
            } else {
                someSet = true;
                stmts.add(stmt);
            }
        }

        if (someSet) {
            Map<String, Serializable> options = new HashMap<String, Serializable>();
            String currentLifeCycleState = currentDoc.getCurrentLifeCycleState();
            options.put(CoreEventConstants.DOC_LIFE_CYCLE,
                    currentLifeCycleState);
            options.put(RelationEvents.GRAPH_NAME_EVENT_KEY,
                    RelationConstants.GRAPH_NAME);

            // before notification
            notifyEvent(RelationEvents.BEFORE_RELATION_CREATION, currentDoc,
                    options, eventComment);

            // add statement
            relationManager.add(RelationConstants.GRAPH_NAME, stmts);

            // after notification
            notifyEvent(RelationEvents.AFTER_RELATION_CREATION, currentDoc,
                    options, eventComment);

            facesMessages.add(
                    FacesMessage.SEVERITY_INFO,
                    resourcesAccessor.getMessages().get(
                            "label.relation.created"));
        }

        if (alreadySet) {
            facesMessages.add(
                    FacesMessage.SEVERITY_WARN,
                    resourcesAccessor.getMessages().get(
                            "label.relation.already.exists"));
        }

        // make sure statements will be recomputed
        resetStatements();
        resetCreateFormValues();

        return null;
    }

    public String deleteStatement(StatementInfo stmtInfo)
            throws ClientException {
        if (stmtInfo != null && outgoingStatementsInfo != null
                && outgoingStatementsInfo.contains(stmtInfo)) {
            Statement stmt = stmtInfo.getStatement();
            // notifications
            Map<String, Serializable> options = new HashMap<String, Serializable>();
            DocumentModel source = getCurrentCaseItem();
            String currentLifeCycleState = source.getCurrentLifeCycleState();
            options.put(CoreEventConstants.DOC_LIFE_CYCLE,
                    currentLifeCycleState);
            options.put(RelationEvents.GRAPH_NAME_EVENT_KEY,
                    RelationConstants.GRAPH_NAME);

            // before notification
            notifyEvent(RelationEvents.BEFORE_RELATION_REMOVAL, source,
                    options, null);

            // remove statement
            List<Statement> stmts = new ArrayList<Statement>();
            stmts.add(stmt);
            relationManager.remove(RelationConstants.GRAPH_NAME, stmts);

            // after notification
            notifyEvent(RelationEvents.AFTER_RELATION_REMOVAL, source, options,
                    null);

            // make sure statements will be recomputed
            resetStatements();

            facesMessages.add(
                    FacesMessage.SEVERITY_INFO,
                    resourcesAccessor.getMessages().get(
                            "label.relation.deleted"));
        }
        return null;
    }

    @Override
    protected void resetCurrentCaseItemCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        resetStatements();
        resetCreateFormValues();
    }
}
