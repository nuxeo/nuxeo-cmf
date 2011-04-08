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
 *     Mariana Cedica
 *
 * $Id$
 */
package org.nuxeo.cm.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.distribution.DistributionInfo;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

@Stateless
@Local(LocalCaseDistributionService.class)
@Remote(RemoteCaseDistributionService.class)
public class CaseDistributionBean implements CaseDistributionService {

    private static final long serialVersionUID = -6359405896496460937L;

    private static final Log log = LogFactory.getLog(CaseDistributionBean.class);

    protected CaseDistributionService caseDistributionService;

    @Override
    public Case createCase(CoreSession session, DocumentModel emailDoc) {
        return getCaseDistributionService().createCase(session, emailDoc);
    }

    @Override
    public Case createCase(CoreSession session, DocumentModel emailDoc,
            List<Mailbox> mailboxes) {
        return getCaseDistributionService().createCase(session, emailDoc,
                mailboxes);
    }

    public CaseLink createDraftCaseLink(CoreSession session, Mailbox mailbox,
            Case envelope) {
        return getCaseDistributionService().createDraftCaseLink(session,
                mailbox, envelope);
    }

    public CaseLink getDraftCaseLink(CoreSession session, Mailbox mailbox,
            String envelopeId) {
        return getCaseDistributionService().getDraftCaseLink(session, mailbox,
                envelopeId);
    }

    public List<CaseLink> getDraftCaseLinks(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        return getCaseDistributionService().getDraftCaseLinks(coreSession,
                mailbox, offset, limit);
    }

    public List<CaseLink> getReceivedCaseLinks(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        return getCaseDistributionService().getReceivedCaseLinks(coreSession,
                mailbox, offset, limit);
    }

    public List<CaseLink> getSentCaseLinks(CoreSession coreSession,
            Mailbox mailbox, long offset, long limit) {
        return getCaseDistributionService().getSentCaseLinks(coreSession,
                mailbox, offset, limit);
    }

    public void notify(CoreSession session, String name,
            DocumentModel document, Map<String, Serializable> eventProperties) {
        getCaseDistributionService().notify(session, name, document,
                eventProperties);

    }

    public CaseLink sendCase(CoreSession session, CaseLink postRequest,
            boolean initial) {
        return getCaseDistributionService().sendCase(session, postRequest,
                initial);
    }

    public CaseLink sendCase(CoreSession session, CaseLink postRequest,
            boolean initial, boolean isActionable) {
        return getCaseDistributionService().sendCase(session, postRequest,
                initial, isActionable);
    }

    private CaseDistributionService getCaseDistributionService() {
        caseDistributionService = Framework.getLocalService(CaseDistributionService.class);
        if (caseDistributionService == null) {
            log.error("Unable to retreive CaseDistributionService");
            throw new ClientRuntimeException(
                    "Unable to retreive CaseDistributionService");
        }
        return caseDistributionService;
    }

    @Override
    public CaseItem addCaseItemToCase(CoreSession session, Case kase,
            DocumentModel emailDoc) {
        return getCaseDistributionService().addCaseItemToCase(session, kase,
                emailDoc);
    }

    public List<CaseLink> getCaseLinks(CoreSession session, Mailbox mailbox,
            Case kase) {
        return getCaseDistributionService().getCaseLinks(session, mailbox, kase);
    }

    @Override
    public void removeCaseLink(CaseLink link, CoreSession sessiion) {
        getCaseDistributionService().removeCaseLink(link, sessiion);
    }

    @Override
    public Case createEmptyCase(CoreSession session, DocumentModel caseDoc,
            Mailbox mailbox) {
        return getCaseDistributionService().createEmptyCase(session, caseDoc,
                mailbox);
    }

    @Override
    public DocumentModel getParentDocumentForCase(CoreSession session) {
        return getCaseDistributionService().getParentDocumentForCase(session);
    }

    @Override
    public String getParentDocumentPathForCase(CoreSession session) {
        return getCaseDistributionService().getParentDocumentPathForCase(
                session);
    }

    @Override
    public Case createCaseFromExistingCaseItem(CaseItem adapter,
            CoreSession documentManager) {
        return getCaseDistributionService().createCaseFromExistingCaseItem(
                adapter, documentManager);
    }

    @Override
    public String getParentDocumentPathForCaseItem(CoreSession session,
            Case kase) {
        return getCaseDistributionService().getParentDocumentPathForCaseItem(
                session, kase);
    }

    @Override
    public Case createEmptyCase(CoreSession session, DocumentModel caseDoc,
            List<Mailbox> mailboxes) {
        return getCaseDistributionService().createEmptyCase(session, caseDoc,
                mailboxes);
    }

    @Override
    public Case createEmptyCase(CoreSession session, String title, String id,
            List<Mailbox> mailboxes) {
        return createEmptyCase(session, title, id, mailboxes);
    }

    @Override
    public Case createEmptyCase(CoreSession session, String title, String id,
            Mailbox mailbox) {
        return createEmptyCase(session, title, id, mailbox);
    }

    @Override
    public CaseLink sendCase(CoreSession session, Case kase,
            DistributionInfo initialDistribution) {
        // TODO Auto-generated method stub
        return null;
    }

}
