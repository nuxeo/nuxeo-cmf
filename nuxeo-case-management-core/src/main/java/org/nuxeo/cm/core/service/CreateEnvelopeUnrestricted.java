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
package org.nuxeo.cm.core.service;

import java.util.List;

import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mail.MailEnvelopeItem;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.security.CorrespondenceSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;


/**
 * @author arussel
 * 
 */
public class CreateEnvelopeUnrestricted extends UnrestrictedSessionRunner {

    protected MailEnvelopeItem item;

    protected DocumentRef ref;
    protected String parentPath;
    protected List<Mailbox> mailboxes;

    public CreateEnvelopeUnrestricted(CoreSession session, MailEnvelopeItem item, String parentPath, List<Mailbox> mailboxes) {
        super(session);
        this.item = item;
        this.parentPath = parentPath;
        this.mailboxes = mailboxes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nuxeo.ecm.core.api.UnrestrictedSessionRunner#run()
     */
    @Override
    public void run() throws ClientException {
        MailEnvelope env = item.createMailEnvelope(session, parentPath, null);
        DocumentModel doc = env.getDocument();
        ACP acp = doc.getACP();
        ACL acl = acp.getOrCreateACL(CorrespondenceSecurityConstants.ACL_MAILBOX_PREFIX);
        for (Mailbox mailbox : mailboxes) {
            acl.add(new ACE(CorrespondenceSecurityConstants.MAILBOX_PREFIX
                    + mailbox.getId(), SecurityConstants.READ_WRITE, true));
        }
        acp.addACL(acl);
        session.setACP(doc.getRef(), acp, true);
        ref = doc.getRef();
    }

    public DocumentRef getDocumentRef() {
        return ref;
    }

}
