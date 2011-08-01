/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Contributors: Sun Seng David TAN <stan@nuxeo.com>, Mariana Cedica <mcedica@nuxeo.com>
 */
package org.nuxeo.cm.cases;

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Mail tree creator used in {@link TestMailTreeHelper}
 */
public class MailTreeCreator implements Runnable {
    public static final Log log = LogFactory.getLog(MailTreeCreator.class);

    CoreSession coreSession;

    DocumentModel mailFolderDocument;

    public MailTreeCreator(CoreSession coreSession,
            DocumentModel mailFolderDocumentModel) {
        this.coreSession = coreSession;
        mailFolderDocument = mailFolderDocumentModel;
    }

    @Override
    public void run() {
        log.info("Starting thread" + Thread.currentThread().getName());
        try {
            TransactionHelper.commitOrRollbackTransaction();
            DocumentModel rootRef = CaseTreeHelper.getOrCreateTxDateTreeFolder(
                    coreSession, mailFolderDocument,
                    Calendar.getInstance().getTime(),
                    CaseConstants.CASE_TREE_TYPE);
            assertNotNull(rootRef);
        } catch (Exception e) {
            log.error("Unable to create mail tree", e);

        } finally {
            TransactionHelper.startTransaction();
        }

    }
}