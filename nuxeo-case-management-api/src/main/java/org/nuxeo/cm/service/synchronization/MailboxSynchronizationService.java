/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.cm.service.synchronization;

import java.io.Serializable;
import java.util.Map;

/**
 * Service that handles mailboxes creation/update/deletion by synchronizing them according to users and groups
 * directories.
 *
 * @author Laurent Doguin
 */
public interface MailboxSynchronizationService extends Serializable {

    void doSynchronize();

    Map<String, MailboxDirectorySynchronizationDescriptor> getSynchronizerMap();

    MailboxUserSynchronizationDescriptor getUserSynchronizer();

    MailboxGroupSynchronizationDescriptor getGroupSynchronizer();
}
