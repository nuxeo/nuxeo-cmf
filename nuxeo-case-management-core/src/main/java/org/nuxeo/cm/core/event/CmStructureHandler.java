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

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.content.template.service.PostContentCreationHandler;

/**
 * @author <a href="mailto:vpasquier@nuxeo.com">Vladimir Pasquier</a>
 * @since 5.5
 */
public class CmStructureHandler implements PostContentCreationHandler {

    public static final String DC_TITLE = "dc:title";

    /**
     * This handler execute commands to create CMF content structure if doesn't
     * exist
     */
    @Override
    public void execute(CoreSession session) {
        try {
            PathRef domainPath = new PathRef("/case-management");
            if (!session.exists(domainPath)) {
                DocumentModel caseContainer = session.createDocumentModel("/",
                        "case-management", "Domain");
                caseContainer.setPropertyValue(DC_TITLE, "Case Management");

                // CMF rights management
                // Block inherited ACL
                ACP acp = new ACPImpl();
                ACL inheritedACL = new ACLImpl(ACL.INHERITED_ACL);
                ACE inheritedACE = new ACE("Everyone", "Everything", false);
                inheritedACL.add(inheritedACE);
                acp.addACL(inheritedACL);

                // Add new local ACL
                ACL localACL = new ACLImpl(ACL.LOCAL_ACL);
                ACE localAce1 = new ACE("administrators", "Everything", true);
                ACE localAce2 = new ACE("Administrator", "Everything", true);
                localACL.add(localAce1);
                localACL.add(localAce2);
                acp.addACL(localACL);

                DocumentModel domain = session.createDocument(caseContainer);
                domain.setACP(acp, true);
            } else {
                PathRef casePath = new PathRef("/case-management/case-root");
                PathRef mailboxPath = new PathRef(
                        "/case-management/mailbox-root");
                PathRef sectionsPath = new PathRef("/case-management/sections");
                if (!session.exists(casePath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "/case-management", "case-root", "CaseRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Case");
                    session.createDocument(caseContainer);
                }
                if (!session.exists(mailboxPath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "/case-management", "mailbox-root", "MailboxRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Mailbox");
                    session.createDocument(caseContainer);
                }
                if (!session.exists(sectionsPath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "/case-management", "sections", "SectionRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Sections");
                    session.createDocument(caseContainer);
                }
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
