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
package org.nuxeo.correspondence.core.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.caselink.CaseLinkConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.correspondence.mail.MailConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.relations.api.Literal;
import org.nuxeo.ecm.platform.relations.api.QNameResource;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Resource;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.ecm.platform.relations.api.impl.LiteralImpl;
import org.nuxeo.ecm.platform.relations.api.impl.RelationDate;
import org.nuxeo.ecm.platform.relations.api.impl.ResourceImpl;
import org.nuxeo.ecm.platform.relations.api.impl.StatementImpl;
import org.nuxeo.ecm.platform.relations.api.util.RelationConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * <p>
 * If the sent mail is a response, this listener updates the replied mail:
 * <ul>
 * <li>Adding the id of the response</li>
 * <li>Setting the date of the first reply action</li>
 * </ul>
 * </p>
 *
 * @author Nicolas Ulrich
 */
public class ReplySentListener implements EventListener {
    private static final String REPLY_TO = "replyTo";

    public static final Log log = LogFactory.getLog(ReplySentListener.class);

    protected RelationManager relationManager;

    public RelationManager getRelationManager() {
        if (relationManager == null) {
            try {
                relationManager = Framework.getService(RelationManager.class);
            } catch (Exception e) {
                throw new CaseManagementRuntimeException("Relation manager should be deployed.", e);
            }
        }
        return relationManager;
    }

    public void handleEvent(Event event) throws ClientException {

        if (!CaseManagementEventConstants.EventNames.afterCaseSentEvent.name().equals(event.getName())) {
            return;
        }

        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        }

        CoreSession session = docCtx.getCoreSession();
        DocumentModel dm = docCtx.getSourceDocument();

        // Check if the document model is an envelope
        if (!dm.hasFacet(MailConstants.MAIL_ENVELOPE_FACET)) {
            return;
        }
        Case env = dm.getAdapter(Case.class);
        if (env == null) {
            return;
        }

        // Retrieve the sent document
        CaseItem item = env.getFirstItem(session);
        if (item == null) {
            return;
        }

        DocumentModel itemDoc = item.getDocument();

        // If the sent document is an outgoing mail and is a response
        Object repliedDocumentIdObject = itemDoc.getPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID);
        if (itemDoc.hasFacet(CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET) && repliedDocumentIdObject != null) {

            // Retrieve the replied document
            String repliedDocumentId = (String) repliedDocumentIdObject;
            DocumentModel repliedDocument = session.getDocument(new IdRef(repliedDocumentId));

            // Get the existing responses of the replied mail
            String[] responses = (String[]) repliedDocument.getPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_RESPONSE_DOCUMENT_IDS);

            // Set the date of response of the replied mail
            if (responses == null || responses.length == 0) {
                repliedDocument.setPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_FIRST_RESPONSE_DATE,
                        GregorianCalendar.getInstance().getTime());
            }

            // Update the list of responses of the replied mail
            List<String> newResponses = new ArrayList<String>();
            newResponses.addAll(Arrays.asList(responses));
            newResponses.add(itemDoc.getId());
            repliedDocument.setPropertyValue(MailConstants.CORRESPONDENCE_DOCUMENT_RESPONSE_DOCUMENT_IDS,
                    (Serializable) newResponses);

            session.saveDocument(repliedDocument);

            // set the Post isAnswered
            String repliedEnvelopeId = (String) repliedDocument.getProperty(MailConstants.MAIL_DOCUMENT_SCHEMA,
                    CaseConstants.DOCUMENT_DEFAULT_CASE_ID);
            DocumentModelList list = session.query("SELECT * FROM Document WHERE ecm:mixinType ='"
                    + CaseLinkConstants.CASE_LINK_FACET + "' AND" + " cslk:envelopeDocumentId='" + repliedEnvelopeId
                    + "'");
            for (DocumentModel post : list) {
                post.setPropertyValue(MailConstants.IS_ANSWERED, true);
                session.saveDocument(post);
            }

            // add relation between document replied and document
            QNameResource docResource = (QNameResource) getRelationManager().getResource(
                    RelationConstants.DOCUMENT_NAMESPACE, repliedDocument, null);
            QNameResource replyResource = (QNameResource) getRelationManager().getResource(
                    RelationConstants.DOCUMENT_NAMESPACE, itemDoc, null);
            Resource resource = new ResourceImpl(RelationConstants.METADATA_NAMESPACE + REPLY_TO);
            Statement stmt = new StatementImpl(docResource, resource, replyResource);
            Literal now = RelationDate.getLiteralDate(new Date());
            stmt.addProperty(RelationConstants.CREATION_DATE, now);
            stmt.addProperty(RelationConstants.AUTHOR, new LiteralImpl(session.getPrincipal().getName()));
            getRelationManager().add(RelationConstants.GRAPH_NAME, Collections.singletonList(stmt));

        }

    }
}
