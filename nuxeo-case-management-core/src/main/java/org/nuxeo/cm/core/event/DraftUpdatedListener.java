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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.event.CaseManagementEventConstants.EventNames;
import org.nuxeo.ecm.core.event.EventListener;

/**
 * @author arussel
 */
public class DraftUpdatedListener extends AbstractDraftListener implements EventListener {

    static final Log log = LogFactory.getLog(DraftCreationListener.class);

    @Override
    protected Log getLog() {
        return log;
    }

    @Override
    protected String getEventName() {
        return EventNames.draftUpdated.name();
    }
}
