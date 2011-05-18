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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.mailbox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.cm.service.synchronization.MailboxSynchronizationConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.Type;

/**
 * Mailbox implementation using a document model as backend.
 *
 * @author Anahide Tchertchian
 */
public class MailboxImpl implements Mailbox {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(Mailbox.class);

    protected final DocumentModel doc;

    public MailboxImpl(DocumentModel doc) {
        this.doc = doc;
    }

    public DocumentModel getDocument() {
        return doc;
    }

    protected String getStringProperty(String property) {
        try {
            return (String) doc.getPropertyValue(property);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringListProperty(String property) {
        try {
            List<String> res = null;
            Object propValue = doc.getPropertyValue(property);
            if (propValue instanceof List) {
                res = (List<String>) propValue;
            } else if (propValue instanceof String[]) {
                res = Arrays.asList((String[]) propValue);
            } else if (propValue != null) {
                throw new ClientRuntimeException(String.format(
                        "Unexpected non-list value for prop %s: %s", property,
                        propValue));
            }
            return res;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected Calendar getDateProperty(String property) {
        try {
            return (Calendar) doc.getPropertyValue(property);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected Integer getIntegerProperty(String property) {
        try {
            Object value = doc.getPropertyValue(property);
            if (value instanceof Long) {
                // FIXME: this is extremely confusing.
                return Integer.valueOf(((Long) value).toString());
            }
            return null;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected void setPropertyValue(String property, Serializable value) {
        try {
            doc.setPropertyValue(property, value);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public MailingList getMailingListTemplate() {
        try {
            Object value = null;
            Property prop = doc.getProperty(MailboxConstants.MAILING_LIST_FIELD);
            Field field = prop.getField();
            if (field != null) {
                Type type = field.getType();
                if (type.isListType()) {
                    Type itemType = ((ListType) type).getFieldType();
                    value = itemType.newInstance();
                }
            }
            if (!(value instanceof Map)) {
                throw new ClientRuntimeException(
                        "Cannot get default template for mailing list");
            }
            Map<String, Serializable> map = (Map<String, Serializable>) value;
            return new MailingListImpl(map);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void addMailingList(MailingList ml) {
        try {
            ArrayList<Map<String, Serializable>> mls = new ArrayList<Map<String, Serializable>>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(MailboxConstants.MAILING_LIST_FIELD);
            if (mailinglists != null) {
                mls.addAll(mailinglists);
            }
            mls.add(ml.getMap());
            setPropertyValue(MailboxConstants.MAILING_LIST_FIELD, mls);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public List<String> getUsers() {
        return getStringListProperty(MailboxConstants.USERS_FIELD);
    }

    public String getDescription() {
        return getStringProperty(MailboxConstants.DESCRIPTION_FIELD);
    }

    public List<String> getFavorites() throws CaseManagementException {
        return getStringListProperty(MailboxConstants.FAVORITES_FIELD);
    }

    public List<String> getGroups() {
        return getStringListProperty(MailboxConstants.GROUPS_FIELD);
    }

    public String getId() {
        return getStringProperty(MailboxConstants.ID_FIELD);
    }

    public void setId(String id) {
        setPropertyValue(MailboxConstants.ID_FIELD, id);
    }

    public List<String> getMailingListIds() {
        List<String> mlids = new ArrayList<String>();
        List<MailingList> mls = getMailingLists();
        if (mls != null) {
            for (MailingList ml : mls) {
                mlids.add(ml.getId());
            }
        }
        return mlids;
    }

    @SuppressWarnings("unchecked")
    public List<MailingList> getMailingLists() {
        try {
            List<MailingList> mls = new ArrayList<MailingList>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(MailboxConstants.MAILING_LIST_FIELD);
            if (mailinglists != null) {
                for (Map<String, Serializable> mailinglist : mailinglists) {
                    mls.add(new MailingListImpl(mailinglist));
                }
            }
            return mls;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mailbox updateMailingList(MailingList currentMailingList) {
        List<Map<String, Serializable>> mailinglists;
        try {
            // get all existing mailing lists
            mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(MailboxConstants.MAILING_LIST_FIELD);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        // update current mailing list info
        for (Map<String, Serializable> map : mailinglists) {
            String mlid = (String) map.get(MailboxConstants.MAILINGLIST_ID_FIELD);
            if (currentMailingList.getId().equals(mlid)) {
                map.put(MailboxConstants.MAILINGLIST_TITLE_FIELD,
                        currentMailingList.getTitle());
                map.put(MailboxConstants.MAILINGLIST_DESCRIPTION_FIELD,
                        currentMailingList.getDescription());
                map.put(MailboxConstants.MAILINGLIST_MAILBOX_FIELD,
                        (Serializable) currentMailingList.getMailboxIds());
            }
        }
        try {
            // save all mailing lists + updated current one
            doc.setPropertyValue(MailboxConstants.MAILING_LIST_FIELD,
                    (Serializable) mailinglists);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public List<String> getNotifiedUsers() {
        return getStringListProperty(MailboxConstants.NOTIFIED_USERS_FIELD);
    }

    public String getOwner() {
        return getStringProperty(MailboxConstants.OWNER_FIELD);
    }

    public List<String> getProfiles() {
        return getStringListProperty(MailboxConstants.PROFILES_FIELD);
    }

    public String getTitle() {
        return getStringProperty(MailboxConstants.TITLE_FIELD);
    }

    public String getType() {
        return getStringProperty(MailboxConstants.TYPE_FIELD);
    }

    public List<String> getAllUsers() {
        List<String> delegates = new ArrayList<String>();
        List<String> users = getUsers();
        if (users != null) {
            delegates.addAll(users);
        }
        String owner = getOwner();
        if (owner != null && !delegates.contains(owner)) {
            delegates.add(0, owner);
        }
        return delegates;
    }

    public boolean hasProfile(String profile) {
        List<String> profiles = getProfiles();
        return profiles != null && profiles.contains(profile);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeMailingList(String mailingListId) {
        try {
            ArrayList<Map<String, Serializable>> mls = new ArrayList<Map<String, Serializable>>();
            List<Map<String, Serializable>> mailinglists = (List<Map<String, Serializable>>) doc.getPropertyValue(MailboxConstants.MAILING_LIST_FIELD);
            if (mailinglists != null) {
                for (Map<String, Serializable> ml : mailinglists) {
                    if (!mailingListId.equals(ml.get(MailboxConstants.MAILINGLIST_ID_FIELD))) {
                        mls.add(ml);
                    }
                }
            }
            setPropertyValue(MailboxConstants.MAILING_LIST_FIELD, mls);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    public void setUsers(List<String> users) {
        ArrayList<String> serializableUsers = new ArrayList<String>();
        if (users != null) {
            serializableUsers.addAll(users);
        }
        setPropertyValue(MailboxConstants.USERS_FIELD, serializableUsers);
    }

    public void setDescription(String description) {
        setPropertyValue(MailboxConstants.DESCRIPTION_FIELD, description);
    }

    public void setFavorites(List<String> favorites)
            throws CaseManagementException {
        ArrayList<String> serializableFavorites = new ArrayList<String>();
        if (favorites != null) {
            serializableFavorites.addAll(favorites);
        }
        setPropertyValue(MailboxConstants.FAVORITES_FIELD,
                serializableFavorites);
    }

    public void setGroups(List<String> groups) {
        ArrayList<String> serializableGroups = new ArrayList<String>();
        if (groups != null) {
            serializableGroups.addAll(groups);
        }
        setPropertyValue(MailboxConstants.GROUPS_FIELD, serializableGroups);
    }

    public void setNotifiedUsers(List<String> users) {
        ArrayList<String> serializableUsers = new ArrayList<String>();
        if (users != null) {
            serializableUsers.addAll(users);
        }
        setPropertyValue(MailboxConstants.NOTIFIED_USERS_FIELD,
                serializableUsers);
    }

    public void setOwner(String owner) {
        setPropertyValue(MailboxConstants.OWNER_FIELD, owner);
    }

    public void setProfiles(List<String> profiles) {
        ArrayList<String> serializableProfiles = new ArrayList<String>();
        if (profiles != null) {
            serializableProfiles.addAll(profiles);
        }
        setPropertyValue(MailboxConstants.PROFILES_FIELD, serializableProfiles);
    }

    public void setTitle(String title) {
        setPropertyValue(MailboxConstants.TITLE_FIELD, title);
    }

    public void setType(String type) {
        setPropertyValue(MailboxConstants.TYPE_FIELD, type);
    }

    public void setConfidentiality(Integer confidentiality) {
        setPropertyValue(MailboxConstants.CONFIDENTIALITY_FIELD,
                confidentiality);
    }

    public Integer getConfidentiality() {
        return getIntegerProperty(MailboxConstants.CONFIDENTIALITY_FIELD);
    }

    public int compareTo(Mailbox other) {
        // sort by type and then by title
        if (getType().equals(other.getType())) {
            // sort by title
            return getTitle().compareTo(other.getTitle());
        } else if (MailboxConstants.type.personal.name().equals(getType())) {
            return -1;
        } else {
            return 1;
        }
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(doc);
            session.save();
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    public String getParentId(CoreSession session) {
        try {
            if (session.hasPermission(doc.getParentRef(),
                    SecurityConstants.READ)) {
                DocumentModel parent = session.getDocument(doc.getParentRef());
                Mailbox parentMailbox = parent.getAdapter(Mailbox.class);
                if (parentMailbox != null) {
                    return parentMailbox.getId();
                }
            }
        } catch (ClientException e) {
            log.error("Unable to retrieve parent mailbox id", e);
        }
        return null;
    }

    public List<String> getChildrenIds(CoreSession session) {
        List<String> res = new ArrayList<String>();
        try {
            if (session.hasPermission(doc.getRef(),
                    SecurityConstants.READ_CHILDREN)) {
                DocumentModelList children = session.getChildren(doc.getRef());
                if (children != null) {
                    for (DocumentModel child : children) {
                        Mailbox childMailbox = child.getAdapter(Mailbox.class);
                        if (childMailbox != null) {
                            res.add(childMailbox.getId());
                        }
                    }
                }
            }
        } catch (ClientException e) {
            log.error("Unable to retrieve child mailbox ids", e);
        }
        return res;
    }

    public List<String> getAllUsersAndGroups() {
        List<String> total = new ArrayList<String>();
        List<String> users = getAllUsers();
        if (users != null) {
            total.addAll(users);
        }

        List<String> groups = getGroups();
        if (groups != null) {
            total.addAll(groups);
        }
        return total;
    }

    public String getAffiliatedMailboxId() {
        return getStringProperty(MailboxConstants.AFFILIATED_MAILBOX_ID);
    }

    public String getSynchronizeState() {
        return getStringProperty(MailboxConstants.SYNCHRONIZED_STATE_FIELD);
    }

    public void setSynchronizeState(String state) {
        setPropertyValue(MailboxConstants.SYNCHRONIZED_STATE_FIELD, state);
    }

    public Boolean isSynchronized() {
        if (MailboxSynchronizationConstants.synchronisedState.synchronised.toString().equals(
                getSynchronizeState())) {
            return true;
        } else {
            return false;
        }
    }

    public String getSynchronizerId() {
        return getStringProperty(MailboxConstants.SYNCHRONIZER_ID_FIELD);
    }

    public void setSynchronizerId(String synchronizerId) {
        setPropertyValue(MailboxConstants.SYNCHRONIZER_ID_FIELD, synchronizerId);
    }

    public Calendar getLastSyncUpdate() {
        return getDateProperty(MailboxConstants.LAST_SYNC_UPDATE_FIELD);
    }

    public void setLastSyncUpdate(Calendar now) {
        setPropertyValue(MailboxConstants.LAST_SYNC_UPDATE_FIELD, now);
    }

    public String getOrigin() {
        return getStringProperty(MailboxConstants.ORIGIN_FIELD);
    }

    public void setOrigin(String origin) {
        setPropertyValue(MailboxConstants.ORIGIN_FIELD, origin);
    }

    @Override
    public List<String> getParticipantListIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ParticipantsList> getParticipantLists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParticipantsList getParticipantListTemplate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addParticipantList(ParticipantsList mailinglist) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeParticipantList(String mailinglistId) {
        throw new UnsupportedOperationException();
    }
}
