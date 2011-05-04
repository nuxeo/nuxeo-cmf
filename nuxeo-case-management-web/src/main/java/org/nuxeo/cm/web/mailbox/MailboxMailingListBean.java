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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.web.mailbox;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailingList;
import org.nuxeo.cm.mailbox.ParticipantsList;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Bean used to manage the mailing list of the current Mailbox.
 *
 */
@Name("mailingListActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
@CaseManagementContextBound
public class MailboxMailingListBean extends CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 1L;

    public static final int MAXIMUM_MAIL_LIST_SIZE = 255;

    @In(create = true)
    protected transient ResourcesAccessor resourcesAccessor;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected NavigationContext navigationContext;

    protected String newTitle;

    protected MailingList currentMailingList;

    protected int getMaximumMailingListNameSize() {
        return MAXIMUM_MAIL_LIST_SIZE;
    }

    public String createMailingList() throws ClientException {
        // Add the title length check at this level, too
        if (newTitle.length() > getMaximumMailingListNameSize()) {
            Object[] params = { getMaximumMailingListNameSize() };
            FacesMessage message = FacesMessages.createFacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    resourcesAccessor.getMessages().get(
                            "feedback.mailinglist.maximum.length"), params);
            facesMessages.add(message);

            return null;
        }

        if (newTitle.length() == 0 || newTitle.equals("")) {
            FacesMessage message = FacesMessages.createFacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    resourcesAccessor.getMessages().get(
                            "feedback.mailinglist.name.empty"));
            facesMessages.add(message);
            return null;
        }

        List<MailingList> existingMls = getCurrentMailbox().getMailingLists();
        for (MailingList mailingList : existingMls) {
            if (newTitle.equals(mailingList.getTitle())) {
                Object[] params = { newTitle };
                facesMessages.add(
                        FacesMessage.SEVERITY_WARN,
                        resourcesAccessor.getMessages().get(
                                "feedback.mailinglist.create.duplicateName"),
                        params);
                facesMessages.addToControl(
                        "newMlTitle",
                        FacesMessage.SEVERITY_WARN,
                        resourcesAccessor.getMessages().get(
                                "feedback.mailinglist.create.duplicateName"),
                        params);
                newTitle = null;
                return null;
            }
        }

        currentMailingList = getCurrentMailbox().getMailingListTemplate();
        currentMailingList.setTitle(newTitle);
        currentMailingList.setId(UUID.randomUUID().toString());
        getCurrentMailbox().addMailingList(currentMailingList);
        getCurrentMailbox().save(documentManager);
        newTitle = null;
        facesMessages.add(
                FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(
                        "feedback.mailinglist.create.success"));
        return null;
    }

    public MailingList getCurrentMailingList() throws ClientException {
        if (currentMailingList == null) {
            List<MailingList> mls = getCurrentMailbox().getMailingLists();
            if (mls != null && !mls.isEmpty()) {
                currentMailingList = mls.get(0);
            }
        }
        return currentMailingList;
    }

    public String deleteMailingList(String mailingListId)
            throws ClientException {
        getCurrentMailbox().removeMailingList(mailingListId);
        getCurrentMailbox().save(documentManager);
        newTitle = null;
        currentMailingList = null;
        facesMessages.add(
                FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(
                        "feedback.mailinglist.list.deleted"));
        return null;
    }

    public String selectMailingList(String mlid) throws ClientException {
        List<MailingList> mls = getCurrentMailbox().getMailingLists();
        if (mls == null) {
            return null;
        }
        if (mlid != null) {
            for (MailingList ml : mls) {
                if (mlid.equals(ml.getId())) {
                    currentMailingList = ml;
                    break;
                }
            }
        }
        return null;
    }

    public void updateCurrentMailingList() throws ClientException {
        Mailbox mailbox = getCurrentMailbox().updateMailingList(currentMailingList);
        //xxx update doc with current mailing list
         mailbox.save(documentManager);
        facesMessages.add(
                FacesMessage.SEVERITY_INFO,
                resourcesAccessor.getMessages().get(
                        "feedback.mailinglist.list.saved"));
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

}