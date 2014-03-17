/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Laurent Doguin
 *
 * $Id$
 */

package org.nuxeo.cm.core.event;

import org.nuxeo.cm.core.caselink.ValidateDueCaseLinkUnrestricted;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;

/**
 * Listener that fetch all ActionCaseLink with automatic validation and date
 * passed and not done to validate them.
 *
 * @author Laurent Doguin
 */
public class AutomaticActionCaseLinkValidatorListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        try (CoreSession session = CoreInstance.openCoreSession(null)) {
            ValidateDueCaseLinkUnrestricted runner = new ValidateDueCaseLinkUnrestricted(
                    session);
            runner.runUnrestricted();
        }
    }

}
