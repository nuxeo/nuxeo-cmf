/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *    mcedica
 */
package org.nuxeo.cm.core.service.caseimporter;

import org.nuxeo.cm.core.service.caseimporter.sourcenodes.CaseSourceNode;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.importer.source.SourceNode;
import org.nuxeo.ecm.platform.importer.threading.DefaultMultiThreadingPolicy;

/**
 * Default thread policy for CMF : make sure that the case, and all the
 * caseItems are created by the same thread
 * */
public class CaseManagementImporterThreadingPolicy extends
        DefaultMultiThreadingPolicy {

    @Override
    public boolean needToCreateThreadAfterNewFolderishNode(
            DocumentModel parent, SourceNode node, long uploadedSources,
            int batchSize, int scheduledTasks) {
        if (node instanceof CaseSourceNode) {
            return false;
        }
        return super.needToCreateThreadAfterNewFolderishNode(parent, node,
                uploadedSources, batchSize, scheduledTasks);
    }
}
