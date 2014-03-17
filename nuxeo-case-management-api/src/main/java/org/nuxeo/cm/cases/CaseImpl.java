/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: MailEnvelopeImpl.java 58167 2008-10-20 15:37:24Z atchertchian $
 */

package org.nuxeo.cm.cases;

import static org.nuxeo.cm.cases.CaseConstants.CASE_SCHEMA;
import static org.nuxeo.cm.cases.CaseConstants.MAILBOX_DOCUMENTS_ID_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;

/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class CaseImpl implements Case {

    private static final long serialVersionUID = 6160682333116646611L;

    boolean firstDocumentFlag;

    Boolean incoming;

    protected final DocumentModel document;

    protected final HasParticipants recipientsAdapter;

    public CaseImpl(DocumentModel envelope, HasParticipants recipientsAdapter) {
        document = envelope;
        this.recipientsAdapter = recipientsAdapter;
    }

    @Override
    public DocumentModel getDocument() {
        return document;
    }

    @Override
    public List<CaseItem> getCaseItems(CoreSession session) {
        return Collections.unmodifiableList(getItems(session));
    }

    protected List<CaseItem> getItems(CoreSession session) {
        List<CaseItem> items = new ArrayList<CaseItem>();
        try {
            for (String emailId : getItemsId()) {
                DocumentModel mailDocument = session.getDocument(new IdRef(
                        emailId));
                CaseItem item = mailDocument.getAdapter(CaseItem.class);
                items.add(item);
            }
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    protected List<String> getItemsId() {
        List<String> emailIds;
        try {
            emailIds = (List<String>) document.getProperty(CASE_SCHEMA,
                    MAILBOX_DOCUMENTS_ID_TYPE);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (emailIds == null) {
            return new ArrayList<String>();
        }
        return emailIds;
    }

    @Override
    public CaseItem getFirstItem(CoreSession session) {
        List<String> itemIds = getItemsId();
        if (itemIds == null || itemIds.isEmpty()) {
            return null;
        }
        String id = itemIds.get(0);
        DocumentModel firstItem;
        try {
            firstItem = session.getDocument(new IdRef(id));
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        if (firstItem == null) {
            return null;
        }
        return firstItem.getAdapter(CaseItem.class);
    }

    @Override
    public boolean addCaseItem(CaseItem item, CoreSession session) {
        List<String> itemsId = getItemsId();
        String newId = item.getDocument().getId();
        if (itemsId.contains(newId)) {
            return false;
        }
        itemsId.add(newId);
        saveItemsId(session, itemsId);
        return true;
    }

    protected void saveItemsId(CoreSession session, List<String> itemsId) {
        try {
            document.setProperty(CASE_SCHEMA, MAILBOX_DOCUMENTS_ID_TYPE,
                    itemsId);
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public boolean removeCaseItem(CaseItem item, CoreSession session) {
        List<String> itemsId = getItemsId();
        String newId = item.getDocument().getId();
        boolean result = itemsId.remove(newId);
        saveItemsId(session, itemsId);
        return result;
    }

    protected boolean moveEmailsInEnvelope(List<CaseItem> selected, boolean up,
            CoreSession session) {
        List<String> itemIds = getItemsId();
        boolean res = true;
        int size = itemIds.size();
        for (CaseItem item : selected) {
            String itemId = item.getDocument().getId();
            int index = itemIds.indexOf(itemId);
            if (index != -1) {
                if (up) {
                    if (index != 0) {
                        // move doc up in the list
                        itemIds.remove(index);
                        itemIds.add(index - 1, itemId);
                    } else {
                        res = false;
                    }
                } else {
                    if (index != size - 1) {
                        // move doc down in the list
                        itemIds.remove(index);
                        itemIds.add(index + 1, itemId);
                    } else {
                        res = false;
                    }
                }
            } else {
                res = false;
            }
        }
        saveItemsId(session, itemIds);
        return res;
    }

    @Override
    public boolean moveUpEmailsInCase(List<CaseItem> selected,
            CoreSession session) {
        return moveEmailsInEnvelope(selected, true, session);
    }

    @Override
    public boolean moveDownEmailsInCase(List<CaseItem> selected,
            CoreSession session) {
        return moveEmailsInEnvelope(selected, false, session);
    }

    @Override
    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public List<DocumentModel> getDocuments() {
        List<DocumentModel> result;
        CoreSession session = document.getCoreSession();
        if (session == null) {
            try (CoreSession sess = CoreInstance.openCoreSession(document.getRepositoryName())) {
                result = getDocuments(sess);
            } catch (ClientException e) {
                throw new CaseManagementRuntimeException(e);
            }
        } else {
            result = getDocuments(session);
        }
        return result;
    }

    @Override
    public List<DocumentModel> getDocuments(CoreSession session) {
        List<DocumentModel> result = new ArrayList<DocumentModel>();
        for (CaseItem item : getItems(session)) {
            result.add(item.getDocument());
        }
        return result;
    }

    @Override
    public boolean isDraft() throws ClientException {
        return CaseLifeCycleConstants.STATE_DRAFT.equals(document.getCurrentLifeCycleState());
    }

    @Override
    public boolean isEmpty() throws ClientException {
        return getItemsId().isEmpty();
    }

    @Override
    public void addInitialExternalParticipants(
            Map<String, List<String>> recipients) {
        recipientsAdapter.addInitialExternalParticipants(recipients);
    }

    @Override
    public void addInitialInternalParticipants(
            Map<String, List<String>> recipients) {
        recipientsAdapter.addInitialInternalParticipants(recipients);
    }

    @Override
    public void addParticipants(Map<String, List<String>> recipients) {
        recipientsAdapter.addParticipants(recipients);
    }

    @Override
    public Map<String, List<String>> getAllParticipants() {
        return recipientsAdapter.getAllParticipants();
    }

    @Override
    public Map<String, List<String>> getInitialExternalParticipants() {
        return recipientsAdapter.getInitialExternalParticipants();
    }

    @Override
    public Map<String, List<String>> getInitialInternalParticipants() {
        return recipientsAdapter.getInitialInternalParticipants();
    }

    @Override
    public boolean canFollowTransition(String transition) {
        try {
            if (document.getAllowedStateTransitions().contains(transition)) {
                return true;
            }
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
        return false;
    }

    @Override
    public void followTransition(String transition) {
        try {
            document.followTransition(transition);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }
}
