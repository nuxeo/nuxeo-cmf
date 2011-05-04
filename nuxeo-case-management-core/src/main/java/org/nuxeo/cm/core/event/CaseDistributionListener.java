/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */
package org.nuxeo.cm.core.event;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 *
 *
 */
public class CaseDistributionListener implements EventListener {

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }
        DocumentModel dm = docCtx.getSourceDocument();
        Boolean isInitial = (Boolean) docCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_IS_INITIAL);
        Case env = dm.getAdapter(Case.class);
        if (env == null) {
            return;
        }
        if (isInitial) {
            // Update the lifecycle of the envelope
            env.getDocument().followTransition(
                    CaseLifeCycleConstants.TRANSITION_OPEN);
        }
    }
}
