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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.event;

import java.util.Map;

import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mailbox.Mailbox;


/**
 * Correspondence event names
 *
 * @author Anahide Tchertchian
 *
 */
public class CorrespondenceEventConstants {

    public static final String DISTRIBUTION_CATEGORY = "DISTRIBUTION";

    public enum EventNames {
        /**
         * Event sent before an envelope is sent.
         */
        beforeEnvelopeSentEvent,
        /**
         * Event sent after an envelope is sent.
         */
        afterEnvelopeSentEvent,
        /**
         * Event sent before a draft is created. EventContext is a
         * DocumentEventContext carrying the Envelope.
         */
        beforeDraftCreated,
        /**
         * Event sent after a draft was created. EventContext is a
         * DocumentEventContext carrying the Envelope. The draft is available as
         * EVENT_CONTEXT_DRAFT.
         */
        afterDraftCreated,
        /**
         * Event sent after a draft was updated. EventContext is a
         * DocumentEventContext carrying the Envelope. The draft is available as
         * EVENT_CONTEXT_DRAFT.
         */
        draftUpdated
    }

    // event context
    /**
     * The sender (of type {@link Mailbox} )
     */
    public static final String EVENT_CONTEXT_SENDER_MAILBOX = "eventContextSender";

    /**
     * The subject (of type @ String} )
     */
    public static final String EVENT_CONTEXT_SUBJECT = "eventContextSubject";

    /**
     * The comment (of type @ String} )
     */
    public static final String EVENT_CONTEXT_COMMENT = "comment";

    /**
     * The envelope (of type {@link MailEnvelope} )
     */
    public static final String EVENT_CONTEXT_ENVELOPE = "eventContextEnvelope";

    /**
     * The draft (of type {@link MailPost} )
     */
    public static final String EVENT_CONTEXT_DRAFT = "eventContextDraft";

    /**
     * The recipients (of type {@link Map} with key {@link String} and value a
     * {@link List} of {@link Mailbox} )
     */
    public static final String EVENT_CONTEXT_INTERNAL_RECIPIENTS = "eventContextRecipients";

    /**
     * The list of recipient titles.
     * <p>
     * This key needs to be concatenated with the recipients type.
     */
    public static final String EVENT_CONTEXT_RECIPIENTS_TYPE_ = "eventContextRecipients_type_";

    /**
     * Is initial (of type @ Boolean)
     */
    public static final String EVENT_CONTEXT_IS_INITIAL = "eventContextIsInitial";

    public static final String EVENT_CONTEXT_EXTERNAL_RECIPIENTS = "eventContextExternalRecipients";

    public static final String EVENT_CONTEXT_AFFILIATED_MAILBOX_ID = "eventContextAffiliatedMailboxId";

    public static final String EVENT_CONTEXT_MAILBOX_ID = "eventContextMailboxId";

}
