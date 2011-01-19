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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.web.context.CaseManagementContextHolderBean;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.types.SubType;
import org.nuxeo.ecm.webapp.action.TypesTool;
import org.nuxeo.runtime.api.Framework;

/**
 * @author arussel
 * 
 */
@Name("typesTool")
@Scope(CONVERSATION)
public class CorrespondenceTypesTool extends TypesTool {

    @In(create = true, required = false)
    protected transient CaseManagementContextHolderBean cmContextHolder;

    private static final String CELLULE_COURRIER = "cellule_courrier";

    private static final long serialVersionUID = 1L;

    private static Set<String> incomingTypes = null;

    private static Set<String> outgoingTypes = null;

    @Override
    protected Map<String, SubType> filterSubTypes(
            Map<String, SubType> allowedSubTypes) {
        Map<String, SubType> filteredTypes = new HashMap<String, SubType>();
        boolean isCurrentCaseItemIncomingCorrespondence = isCurrentCaseItemIncomingCorrespondence();
        boolean isCurrentCaseItemOutgoingCorrespondence = isCurrentCaseItemOutgoingCorrespondence();
        for (Map.Entry<String, SubType> entry : allowedSubTypes.entrySet()) {
            if (isCelluleCourrierMailbox()
                    || isAllowedOutsideCelluleCourrier(entry.getValue(),
                            isCurrentCaseItemIncomingCorrespondence,
                            isCurrentCaseItemOutgoingCorrespondence)) {
                filteredTypes.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredTypes;
    }

    protected boolean isAllowedOutsideCelluleCourrier(SubType subType,
            boolean isCurrentCaseItemIncomingCorrespondence,
            boolean isCurrentCaseItemOutgoingCorrespondence) {
        try {
            if (incomingTypes == null || outgoingTypes == null) {
                SchemaManager schemaManager = Framework.getService(SchemaManager.class);
                incomingTypes = schemaManager.getDocumentTypeNamesForFacet(CorrespondenceConstants.INCOMING_CORRESPONDENCE_FACET);
                outgoingTypes = schemaManager.getDocumentTypeNamesForFacet(CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET);
            }
            boolean currentTypeIsIncoming = incomingTypes.contains(subType.getName());
            boolean currentTypeIsOutgoing = outgoingTypes.contains(subType.getName());
            if (isCurrentCaseItemIncomingCorrespondence) {
                return currentTypeIsIncoming;
            } else if (isCurrentCaseItemOutgoingCorrespondence()) {
                return currentTypeIsOutgoing;
            }
            return currentTypeIsOutgoing;
        } catch (Exception e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected boolean isCelluleCourrierMailbox() {
        DocumentModel model = getCurrentItem();
        if (model.hasFacet(CaseConstants.MAILBOX_FACET)) {
            Mailbox mailbox = model.getAdapter(Mailbox.class);
            return mailbox.getProfiles().contains(CELLULE_COURRIER);
        }
        return false;
    }

    protected boolean isCurrentCaseItemIncomingCorrespondence() {
        try {
            DocumentModel currentCaseItem = cmContextHolder.getCurrentCaseItem();
            if (currentCaseItem == null) {
                return false;
            } else if (currentCaseItem.hasFacet(CorrespondenceConstants.INCOMING_CORRESPONDENCE_FACET)) {
                return true;
            } else {
                return false;
            }
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected boolean isCurrentCaseItemOutgoingCorrespondence() {
        try {
            DocumentModel currentCaseItem = cmContextHolder.getCurrentCaseItem();
            if (currentCaseItem == null) {
                return false;
            } else if (currentCaseItem.hasFacet(CorrespondenceConstants.OUTGOING_CORRESPONDENCE_FACET)) {
                return true;
            } else {
                return false;
            }
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

}