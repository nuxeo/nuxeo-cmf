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

import org.nuxeo.cm.caselink.CaseLink;

/**
 * Correspondence event names
 *
 * @author Anahide Tchertchian
 */
public class CaseManagementEventConstants {

    public static final String DISTRIBUTION_CATEGORY = "DISTRIBUTION";

    public enum EventNames {
        /**
         * Event sent before an envelope is sent.
         */
        beforeCaseSentEvent,
        /**
         * Event sent after an envelope is sent.
         */
        afterCaseSentEvent,
        /**
         * Event sent before a case item is sent.
         */
        beforeCaseItemSentEvent,
        /**
         * Event sent after a case item is sent.
         */
        afterCaseItemSentEvent,
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
        draftUpdated,
        /**
         * Event sent after a caseItem was imported. EventContext is a
         * DocumentEventContext carrying the document.
         */
        caseItemImported,
        /**
         * Event sent before a case link is removed.
         */
        beforeCaseLinkRemovedEvent,
        /**
         * Event sent after a case link is removed.
         */
        afterCaseLinkRemovedEvent,
        /**
         * Event scheduled every day.
         */
        validateCaseLink
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
     * The envelope (of type {@link Case} )
     */
    public static final String EVENT_CONTEXT_CASE = "eventContextCase";

    /**
     * The case link (of type {@link CaseLink} )
     */
    public static final String EVENT_CONTEXT_CASE_LINK = "eventContextCaseLink";

    /**
     * The draft (of type {@link MailPost} )
     */
    public static final String EVENT_CONTEXT_DRAFT = "eventContextDraft";

    /**
     * The recipients (of type {@link Map} with key {@link String} and value a
     * {@link List} of {@link Mailbox} )
     */
    public static final String EVENT_CONTEXT_INTERNAL_PARTICIPANTS = "eventContextParticipants";

    /**
     * The list of recipient titles.
     * <p>
     * This key needs to be concatenated with the recipients type.
     */
    public static final String EVENT_CONTEXT_PARTICIPANTS_TYPE_ = "eventContextParticipants_type_";

    /**
     * Is initial (of type @ Boolean)
     */
    public static final String EVENT_CONTEXT_IS_INITIAL = "eventContextIsInitial";

    public static final String EVENT_CONTEXT_EXTERNAL_PARTICIPANTS = "eventContextExternalParticipants";

    public static final String EVENT_CONTEXT_AFFILIATED_MAILBOX_ID = "eventContextAffiliatedMailboxId";

    public static final String EVENT_CONTEXT_MAILBOX_ID = "eventContextMailboxId";

    public static final String EVENT_CASE_MANAGEMENET_IMPORT = "eventCmImport";

    public static final String EVENT_CASE_MANAGEMENET_IMPORT_CATEGORY = "cmImportCategory";

    public static final String EVENT_CASE_MANAGEMENET_CASE_IMPORT = "eventCmCaseImport";

    public static final String EVENT_CASE_MANAGEMENT_CASE_ITEM_SOURCE_PATH = "cmSourcePath";
}
