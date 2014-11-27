/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.cm.service.synchronization;

import org.nuxeo.cm.service.MailboxTitleGenerator;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("groupToMailbox")
public class MailboxGroupSynchronizationDescriptor {

    @XNode("@enabled")
    protected Boolean enabled;

    @XNode("titleGenerator")
    protected Class<MailboxTitleGenerator> titleGenerator;

    public Boolean isEnabled() {
        if (enabled == null) {
            return Boolean.TRUE;
        }
        return enabled;
    }

    public MailboxTitleGenerator getTitleGenerator() {
        if (titleGenerator == null) {
            return null;
        }
        try {
            return titleGenerator.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
