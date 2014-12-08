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
 * $Id: MailTreeHelper.java 57899 2008-10-07 12:02:44Z atchertchian $
 */

package org.nuxeo.cm.cases;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * Helper for mail tree
 * <p>
 * Emails and Mail envelopes are created within trees of folder.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class CaseTreeHelper {

    public static final String TITLE_PROPERTY_NAME = "dc:title";

    public static final String DELETED_STATE = "deleted";

    private static final Lock lock = new ReentrantLock();

    /**
     * Find or create a set of folders representing the date hierarchy
     *
     * @return the last child created (day)
     */
    public static DocumentModel getOrCreateDateTreeFolder(CoreSession session, DocumentModel root, Date date,
            String folderType) throws ClientException {
        String subPath = new SimpleDateFormat("yyyy/MM/dd").format(date);
        return getOrCreatePath(session, root, subPath, folderType);
    }

    public static DocumentModel getOrCreatePath(CoreSession session, DocumentModel root, String subPath,
            String folderType) throws ClientException {
        String[] pathSplit = subPath.split("/");
        String parentPath = root.getPathAsString();
        DocumentModel child = root;
        for (String id : pathSplit) {
            child = getOrCreate(session, parentPath, id, folderType);
            parentPath = child.getPathAsString();
        }
        return child;
    }

    public static synchronized DocumentModel getOrCreate(CoreSession session, String rootPath, String id,
            String folderType) throws ClientException {
        String path = String.format("%s/%s", rootPath, id);
        DocumentRef pathRef = new PathRef(path);
        boolean exists = session.exists(pathRef);
        if (exists) {
            DocumentModel existing = session.getDocument(pathRef);
            if (!DELETED_STATE.equals(existing.getCurrentLifeCycleState())) {
                return existing;
            }
        }
        // create it
        DocumentModel newDocument = session.createDocumentModel(rootPath, IdUtils.generateId(id), folderType);
        newDocument.setPropertyValue(TITLE_PROPERTY_NAME, id);
        newDocument = session.createDocument(newDocument);
        return newDocument;
    }

    /**
     * Find or create a set of folders representing the date hierarchy. This method is starting and stopping
     * transactions, so caller should make sure no transaction is active (commit before, start a new transaction after)
     *
     * @since 1.7
     */
    public static final DocumentModel getOrCreateTxDateTreeFolder(String repositoryName, DocumentModel root, Date date,
            String folderType) throws ClientException {
        String subPath = new SimpleDateFormat("yyyy/MM/dd").format(date);
        return getOrCreateTxPath(repositoryName, root, subPath, folderType);
    }

    public static final DocumentModel getOrCreateTxPath(String repositoryName, DocumentModel rootDocument,
            String subPath, String folderType) throws ClientException {
        lock.lock();
        try {

            TransactionHelper.startTransaction();
            try {
                UnrestrictedRootTreeCreator rootTreeCreator = new UnrestrictedRootTreeCreator(repositoryName,
                        rootDocument, subPath, folderType);
                rootTreeCreator.runUnrestricted();
                return rootTreeCreator.getChild();
            } finally {
                TransactionHelper.commitOrRollbackTransaction();
            }
        } finally {
            lock.unlock();
        }
    }

    static class UnrestrictedRootTreeCreator extends UnrestrictedSessionRunner {

        String path;

        String subPath;

        String id;

        String folderType;

        DocumentModel rootDoc;

        DocumentModel child;

        protected UnrestrictedRootTreeCreator(String repositoryName, DocumentModel rootDoc, String subPath,
                String folderType) {
            super(repositoryName);
            this.subPath = subPath;
            this.folderType = folderType;
            this.rootDoc = rootDoc;
        }

        @Override
        public void run() throws ClientException {
            String[] pathSplit = subPath.split("/");
            String parentPath = rootDoc.getPathAsString();
            child = rootDoc;
            for (String id : pathSplit) {
                child = getOrCreate(session, parentPath, id, folderType);
                parentPath = child.getPathAsString();
            }
        }

        public DocumentModel getChild() {
            return child;
        }
    }

}
