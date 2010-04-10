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
 *     Nicolas Ulrich
 */
package org.nuxeo.cm.post;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.nuxeo.cm.mail.HasRecipients;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * A CorrespondencePost represent a sent {@link MailEnvelope} in a
 * {@link Mailbox}.
 *
 * When a Mailbox send a MailEnvelope, a CorrespondencePost is created in its
 * Mailbox and a {@link CorrepsondenceMessage} is created in each of the
 * recipients Mailbox.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public interface CorrespondencePost extends HasRecipients, Serializable {

    /**
     * get the document model.
     */
    DocumentModel getDocument();

    /**
     * The id of the post.
     *
     * @return
     */
    String getId();

    /**
     * The subject.
     *
     * @return
     * @throws ClientException
     */
    String getSubject();

    /**
     * The comment.
     *
     * @return
     * @throws ClientException
     */
    String getComment();

    /**
     *The date.
     *
     * @return
     * @throws ClientException
     */
    Calendar getDate();

    /**
     * The sender.
     *
     * @return
     * @throws ClientException
     */
    String getSender();

    /**
     * The sender mailbox id.
     *
     * @return
     * @throws ClientException
     */
    String getSenderMailboxId();

    /**
     * The mail envelope sent.
     *
     * @return
     */
    MailEnvelope getMailEnvelope(CoreSession session);

    /**
     * The send date of the post.
     *
     * @return
     * @throws ClientException
     */
    Date getSentDate();

    /**
     * The type of the message.
     *
     * @return
     * @throws ClientException
     */
    String getType();

    /**
     * If this message has been read
     *
     * @return
     * @throws ClientException
     */
    boolean isRead();

    /**
     * Persist the post
     */
    void save(CoreSession session);

    /**
     * Is it a draft
     */
    boolean isDraft();

}
