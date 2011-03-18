/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id:$
 */

package org.nuxeo.correspondence.core.mail.actionpipe;

import org.nuxeo.cm.mail.actionpipe.CreateAndDistributeDocuments;
import org.nuxeo.correspondence.core.utils.CorrespondenceConstants;

/**
 * Transforms received email in a set of document models and distribute them.
 *
 * @author Laurent Doguin
 */
public class CorrespondenceCreateAndDistributeDocuments extends
        CreateAndDistributeDocuments {

    @Override
    protected String getCorrespondenceDocumentTypeToCreate() {
        return CorrespondenceConstants.IN_CORRESPONDENCE_DOCUMENT;
    }
}
