/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 */
package org.nuxeo.cm.core.event;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.mailbox.MailboxConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.content.template.service.PostContentCreationHandler;

/**
 * @author <a href="mailto:vpasquier@nuxeo.com">Vladimir Pasquier</a>
 * @since 5.5
 */
public class CaseManagementStructureHandler implements
        PostContentCreationHandler {

    public static final String DC_TITLE = "dc:title";

    /**
     * This handler execute commands to create CMF content structure if doesn't
     * exist
     */
    @Override
    public void execute(CoreSession session) {
        try {
            DocumentModel cmfDomain;
            PathRef cmfDomainPath = new PathRef(CaseConstants.CASE_DOMAIN_PATH);
            if (!session.exists(cmfDomainPath)) {
                DocumentModel caseContainer = session.createDocumentModel(
                        CaseConstants.CASE_DOMAIN_PARENT_PATH,
                        CaseConstants.CASE_DOMAIN_NAME,
                        CaseConstants.CASE_DOMAIN_TYPE);
                caseContainer.setPropertyValue(DC_TITLE, "Case Management");
                cmfDomain = session.createDocument(caseContainer);

                // CMF rights management
                ACP acp = cmfDomain.getACP();
                ACL localACL = acp.getOrCreateACL();
                // Add new local ACL
                ACE admins = new ACE(SecurityConstants.ADMINISTRATORS,
                        SecurityConstants.EVERYTHING, true);
                ACE admin = new ACE(SecurityConstants.ADMINISTRATOR,
                        SecurityConstants.EVERYTHING, true);
                localACL.add(admins);
                localACL.add(admin);
                // Block inherited ACL
                ACE blockInheritance = new ACE(SecurityConstants.EVERYONE,
                        SecurityConstants.EVERYTHING, false);
                localACL.add(blockInheritance);

                cmfDomain.setACP(acp, true);
            } else {
                cmfDomain = session.getDocument(cmfDomainPath);
            }
            // create case root if missing
            PathRef casePath = new PathRef(CaseConstants.CASE_DOMAIN_PATH + "/"
                    + CaseConstants.CASE_ROOT_DOCUMENT_NAME);
            if (!session.exists(casePath)) {
                DocumentModel caseContainer = session.createDocumentModel(
                        CaseConstants.CASE_DOMAIN_PATH,
                        CaseConstants.CASE_ROOT_DOCUMENT_NAME,
                        CaseConstants.CASE_ROOT_TYPE);
                caseContainer.setPropertyValue(DC_TITLE, "Case");
                session.createDocument(caseContainer);
            }
            // create mailboxes root if missing
            PathRef mailboxPath = new PathRef(CaseConstants.CASE_DOMAIN_PATH
                    + "/" + MailboxConstants.MAILBOX_ROOT_DOCUMENT_NAME);
            if (!session.exists(mailboxPath)) {
                DocumentModel caseContainer = session.createDocumentModel(
                        CaseConstants.CASE_DOMAIN_PATH,
                        MailboxConstants.MAILBOX_ROOT_DOCUMENT_NAME,
                        MailboxConstants.MAILBOX_ROOT_DOCUMENT_TYPE);
                caseContainer.setPropertyValue(DC_TITLE, "Mailbox");
                session.createDocument(caseContainer);
            }
            // create CMF sections root if missing
            PathRef sectionsPath = new PathRef(CaseConstants.CASE_DOMAIN_PATH
                    + "/sections");
            if (!session.exists(sectionsPath)) {
                DocumentModel caseContainer = session.createDocumentModel(
                        CaseConstants.CASE_DOMAIN_PATH, "sections",
                        "SectionRoot");
                caseContainer.setPropertyValue(DC_TITLE, "Sections");
                session.createDocument(caseContainer);
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
