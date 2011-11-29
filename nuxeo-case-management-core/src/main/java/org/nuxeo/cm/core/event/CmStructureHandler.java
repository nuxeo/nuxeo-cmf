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
import org.nuxeo.ecm.platform.content.template.service.PostContentCreationHandler;

/**
 * @author <a href="mailto:vpasquier@nuxeo.com">Vladimir Pasquier</a>
 * @since 5.5
 */
public class CmStructureHandler implements PostContentCreationHandler {

    public static final String DC_TITLE = "dc:title";

    @Override
    public void execute(CoreSession session) {
        try {
            PathRef domainPath = new PathRef("case-management/");
            if (!session.exists(domainPath)) {
                DocumentModel caseContainer = session.createDocumentModel("/",
                        "Case Management", "Domain");
                caseContainer.setPropertyValue(DC_TITLE, "Case Management");
                session.createDocument(caseContainer);
            } else {
                PathRef casePath = new PathRef("case-management/case-root");
                PathRef mailboxPath = new PathRef(
                        "case-management/mailbox-root");
                PathRef sectionsPath = new PathRef("case-management/sections");
                if (!session.exists(casePath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "case-management/", "Case", "CaseRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Case");
                    session.createDocument(caseContainer);
                }
                if (!session.exists(mailboxPath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "case-management/", "Mailbox", "MailboxRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Mailbox");
                    session.createDocument(caseContainer);
                }
                if (!session.exists(sectionsPath)) {
                    DocumentModel caseContainer = session.createDocumentModel(
                            "case-management/", "Sections", "SectionRoot");
                    caseContainer.setPropertyValue(DC_TITLE, "Sections");
                    session.createDocument(caseContainer);
                }
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

}
