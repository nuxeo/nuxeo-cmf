/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *
 * $Id$
 */

package org.nuxeo.cm.web.actionable.caselink;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.ecm.webapp.helpers.ResourcesAccessor;

/**
 * Processing actions for an actionable case link
 *
 * @author <a href="mailto:mcedica@nuxeo.com">Mariana Cedica</a>
 * */
@Name("actionableCaseLinkActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
public class ActionableCaseLinkActionsBean extends
        CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected ResourcesAccessor resourcesAccessor;

    public String approveTask(DocumentModel caseLink) throws ClientException {
        ActionableCaseLink acl = caseLink.getAdapter(ActionableCaseLink.class);
        DocumentRef ref = caseLink.getParentRef();
        // check is task still undone
        DocumentModel doc = documentManager.getDocument(ref);
        if (doc == null
                || !doc.getCurrentLifeCycleState().equals(
                        CaseLink.CaseLinkTransistion.toDone.name())) {
            facesMessages.add(FacesMessage.SEVERITY_WARN,"actionblecaselink.gone");
            Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
                    getCurrentMailbox());
            return null;
        }
        acl.validate(documentManager);
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
                documentManager.getDocument(ref));
        return navigationContext.navigateToDocument(getCurrentMailbox().getDocument());
    }

    public String rejectTask(DocumentModel caseLink) throws ClientException {
        ActionableCaseLink acl = caseLink.getAdapter(ActionableCaseLink.class);
        DocumentRef ref = caseLink.getParentRef();
        // check is task still undone
        DocumentModel doc = documentManager.getDocument(ref);
        if (doc == null
                || !doc.getCurrentLifeCycleState().equals(
                        CaseLink.CaseLinkTransistion.toDone.name())) {
            facesMessages.add(FacesMessage.SEVERITY_WARN,"actionblecaselink.gone");
            Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
                    getCurrentMailbox());
            return null;
        }
        acl.refuse(documentManager);
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
                documentManager.getDocument(ref));
        return navigationContext.navigateToDocument(getCurrentMailbox().getDocument());
    }

    public boolean isShowAction(DocumentModel caseLink) throws ClientException {
        CaseLink cl = caseLink.getAdapter(CaseLink.class);
        if (cl.isActionnable()) {
            ActionableCaseLink acl = caseLink.getAdapter(ActionableCaseLink.class);
            return acl.isTodo();
        }
        return false;
    }
}