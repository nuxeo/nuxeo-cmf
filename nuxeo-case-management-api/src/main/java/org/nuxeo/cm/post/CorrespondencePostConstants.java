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
 *     nulrich
 */
package org.nuxeo.cm.post;

/**
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class CorrespondencePostConstants {

    /**
     * The he document type.
     */
    public final static String POST_DOCUMENT_TYPE = "CorrespondencePost";

    public final static String POST_FACET = "CorrespondencePost";

    public final static String POST_SCHEMA = "correspondence_post";

    /**
     * The xpath of the postId.
     */
    public final static String ID_FIELD = "cpost:postId";

    /**
     * The xpath of the envelope repository name.
     */
    public final static String ENVELOPE_REPOSITORY_NAME_FIELD = "cpost:envelopeRepositoryName";

    /**
     * The xpath of the envelope document id.
     */
    public final static String ENVELOPE_DOCUMENT_ID_FIELD = "cpost:envelopeDocumentId";

    /**
     * The xpath of the subject.
     */
    public final static String SUBJECT_FIELD = "dc:title";

    /**
     * The xpath the sender mailbox id.
     */
    public final static String SENDER_MAILBOX_ID_FIELD = "cpost:senderMailboxId";

    /**
     * The xpath of the sender.
     */
    public final static String SENDER_FIELD = "cpost:sender";

    /**
     * The xpath the date.
     */
    public final static String DATE_FIELD = "cpost:date";

    /**
     * The xpath the comment.
     */
    public final static String COMMENT_FIELD = "cpost:comment";

    /**
     * The xpath of the sending date.
     */
    public final static String SENT_DATE_FIELD = "cpost:sentDate";

    /**
     * The xpath of the type.
     */
    public final static String TYPE_FIELD = "cpost:type";

    /**
     * The xpath of the isRead indicator.
     */
    public final static String IS_READ_FIELD = "cpost:isRead";

    /**
     * The xpath of the isSent indicator.
     */
    public final static String IS_SENT_FIELD = "cpost:isSent";

    /**
     * The xpath of the envelope custom id indicator.
     */
    public final static String ENVELOPE_ID_FIELD = "cpost:envelopeId";

    /**
     * The xpath of the isDraft indicator
     */
    public final static String IS_DRAFT_FIELD = "cpost:draft";

    /**
     * The xpath of the isAnswered indicator 
     */
    public final static String IS_ANSWERED = "cpost:isAnswered";

}
