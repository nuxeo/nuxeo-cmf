/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.core.event;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.event.CorrespondenceEventConstants.EventNames;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventListener;


/**
 * @author arussel
 *
 */
public class DraftCreationListener extends AbstractDraftListener implements
        EventListener {
    private static final Log log = LogFactory.getLog(DraftCreationListener.class);

    protected Log getLog() {
        return log;
    }

    @Override
    protected void updateDraft(DocumentModel draft, DocumentModel firstDoc,
            DocumentModel envelope, Principal principal) throws ClientException {
        super.updateDraft(draft, firstDoc, envelope, principal);
        draft.setPropertyValue("uid:uid", firstDoc.getPropertyValue("uid:uid"));
    }

    @Override
    protected String getEventName() {
        return EventNames.afterDraftCreated.name();
    }
}
