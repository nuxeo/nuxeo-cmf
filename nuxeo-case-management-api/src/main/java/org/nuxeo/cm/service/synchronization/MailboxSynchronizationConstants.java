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

package org.nuxeo.cm.service.synchronization;

/**
 * @author Laurent Doguin
 * 
 */
public class MailboxSynchronizationConstants {

    public enum synchronisedState {
        synchronised, unsynchronised, doublon
    }

    public enum EventNames {
        /**
         */
        onMailboxCreated,
        /**
         */
        onMailboxUpdated,
        /**
         */
        onMailboxDeleted
    }

    public static final String NO_CHILDREN = "noChildren";

    public static final String EVENT_CONTEXT_SYNCHRONIZER_ID = "eventContextSynchronizerId";

    public static final String EVENT_CONTEXT_MAILBOX_TITLE = "eventContextMailboxTitle";

    public static final String EVENT_CONTEXT_DIRECTORY_NAME = "eventContextDirectoryName";

    public static final String EVENT_CONTEXT_SYNCHRONIZED_DATE = "eventContextSynchronizedDate";

    public static final String EVENT_CONTEXT_MAILBOX_TYPE = "eventContextMailboxType";

    public static final String EVENT_CONTEXT_MAILBOX_OWNER = "eventContextMailboxOwner";

    public static final String EVENT_CONTEXT_PARENT_SYNCHRONIZER_ID = "eventContextMailboxParentSynchronizerId";

    public static final String EVENT_CONTEXT_MAILBOX_ENTRY_ID = "eventContextSynchronizerEntryId";

    public static final String EVENT_CONTEXT_MAILBOX_ENTRY = "eventContextSynchronizerEntry";
}
