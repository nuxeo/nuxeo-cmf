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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.cm.service.CaseManagementPersister;
import org.nuxeo.cm.service.MailboxCreator;
import org.nuxeo.cm.service.MailboxManagementService;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * @author Anahide Tchertchian
 */
public class CaseManagementComponent extends DefaultComponent {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseManagementComponent.class);

    protected static final String MAILBOX_CREATOR_EXTENSION_POINT = "mailboxCreator";

    protected static final String POST_FACTORY_EXTENSION_POINT = "postFactory";

    protected static final String MESSAGE_FACTORY_EXTENSION_POINT = "messageFactory";

    protected static final String PERSISTER_EXTENSION_POINT = "persister";

    protected MailboxManagementServiceImpl mailboxService;

    protected CaseDistributionServiceImpl distributionService;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        this.mailboxService = new MailboxManagementServiceImpl();
        this.distributionService = new CaseDistributionServiceImpl();
        distributionService.setPathSegmentService(Framework.getService(PathSegmentService.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(MailboxManagementService.class)) {
            return (T) mailboxService;
        } else if (adapter.isAssignableFrom(CaseDistributionService.class)) {
            return (T) distributionService;
        }
        return null;
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (extensionPoint.equals(MAILBOX_CREATOR_EXTENSION_POINT)) {
            CreationClassDescriptor classDesc = (CreationClassDescriptor) contribution;
            String className = classDesc.getKlass();
            // Thread context loader is not working in isolated EARs
            Object creator = CaseManagementComponent.class.getClassLoader().loadClass(
                    className).newInstance();
            if (creator instanceof MailboxCreator) {
                mailboxService.setPersonalMailboxCreator((MailboxCreator) creator);
            } else {
                log.error("Invalid contribution to personal mailbox creator: "
                        + className);
            }
        } else if (PERSISTER_EXTENSION_POINT.equals(extensionPoint)) {
            PersisterDescriptor desc = (PersisterDescriptor) contribution;
            CaseManagementPersister persister = desc.getKlass().newInstance();
            String rootPath = desc.getCaseRootPath();
            if (rootPath != null) {
                persister.setCaseRootPath(rootPath);
            }
            distributionService.setPersister(persister);
        } else {
            log.warn("Unknown extension point " + extensionPoint);
        }
    }
}
