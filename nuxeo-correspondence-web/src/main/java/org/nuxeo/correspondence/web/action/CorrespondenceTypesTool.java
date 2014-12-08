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
package org.nuxeo.correspondence.web.action;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.web.context.CaseManagementContextHolderBean;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.webapp.action.TypesTool;
import org.nuxeo.runtime.api.Framework;

/**
 * @author arussel
 */
@Name("typesTool")
@Scope(CONVERSATION)
public class CorrespondenceTypesTool extends TypesTool {

    @In(create = true, required = false)
    protected transient CaseManagementContextHolderBean cmContextHolder;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    private static final String CELLULE_COURRIER = "cellule_courrier";

    private static final String CASE_DOCUMENT_CATEGORY = "CaseDocument";

    private static final long serialVersionUID = 1L;

    private static Set<String> incomingTypes = null;

    private static Set<String> outgoingTypes = null;

    @Override
    protected Map<String, List<Type>> filterTypeMap(Map<String, List<Type>> docTypeMap) {
        // If not null, parent doc is envelope so no need to filter
        // This should be remove and parent Doc should be given as parameter
        if (docTypeMap.get(CASE_DOCUMENT_CATEGORY) != null) {
            return docTypeMap;
        }

        boolean isCurrentCaseItemIncomingCorrespondence = hasCurrentCaseItemGivenFacet(CorrespondenceConstants.INCOMING_CORRESPONDENCE_FACET);
        boolean isCurrentCaseItemOutgoingCorrespondence = hasCurrentCaseItemGivenFacet(CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET);

        for (List<Type> types : docTypeMap.values()) {
            for (Iterator<Type> it = types.iterator(); it.hasNext();) {
                Type type = it.next();
                if (!isAllowed(type, isCurrentCaseItemIncomingCorrespondence, isCurrentCaseItemOutgoingCorrespondence)) {
                    it.remove();
                }
            }
        }
        return docTypeMap;
    }

    protected boolean isAllowed(Type type, boolean isCurrentCaseItemIncomingCorrespondence,
            boolean isCurrentCaseItemOutgoingCorrespondence) {
        try {
            if (incomingTypes == null || outgoingTypes == null) {
                SchemaManager schemaManager = Framework.getService(SchemaManager.class);
                incomingTypes = schemaManager.getDocumentTypeNamesForFacet(CorrespondenceConstants.INCOMING_CORRESPONDENCE_FACET);
                outgoingTypes = schemaManager.getDocumentTypeNamesForFacet(CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET);
            }
            boolean currentTypeIsIncoming = incomingTypes.contains(type.getId());
            boolean currentTypeIsOutgoing = outgoingTypes.contains(type.getId());
            if (isCurrentCaseItemIncomingCorrespondence) {
                return currentTypeIsIncoming;
            } else if (isCurrentCaseItemOutgoingCorrespondence) {
                return currentTypeIsOutgoing;
            }
            if (isCelluleCourrierMailbox()) {
                return true;
            } else {
                return currentTypeIsOutgoing;
            }
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected boolean isCelluleCourrierMailbox() {
        try {
            Mailbox mailbox = cmContextHolder.getCurrentMailbox();
            if (mailbox != null) {
                return mailbox.getProfiles().contains(CELLULE_COURRIER);
            }
            return false;
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected boolean hasCurrentCaseItemGivenFacet(String facet) {
        try {
            DocumentModel caseItemDocument = cmContextHolder.getCurrentCaseItem();
            if (caseItemDocument == null) {
                Case kase = cmContextHolder.getCurrentCase();
                if (kase != null) {
                    CaseItem caseItem = kase.getFirstItem(documentManager);
                    if (caseItem == null) {
                        return false;
                    } else {
                        caseItemDocument = caseItem.getDocument();
                    }
                } else {
                    return false;
                }
            }
            return caseItemDocument.hasFacet(facet);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

}
