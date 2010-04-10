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

package org.nuxeo.cm.mail;

/**
 * @author Anahide Tchertchian
 *
 */
public class MailConstants {

    public static final String MAIL_TREE_TYPE = "Folder";

    public static final String MAIL_ROOT_DOCUMENT_PATH = "/correspondence/mail";

    public static final String MAIL_ENVELOPE_TYPE = "CorrespondenceEnvelope";

    public static final String MAIL_ENVELOPE_SCHEMA = "correspondence_envelope";

    public static final String MAIL_DOCUMENT_SCHEMA = "correspondence_document";

    public static final String MAIL_ENVELOPE_FACET = "CorrespondenceEnvelope";

    public static final String MAIL_FACET = "CorrespondenceDocument";

    public static final String MAIL_DOCUMENT_TYPE = "CorrespondenceDocument";

    public static final String MAIN_INJECTION_ORIGIN = "Courriel";

    public static final String INCOMING_MAIL_FACET = "IncomingCorrespondence";

    public static final String OUTGOING_MAIL_FACET = "OutgoingCorrespondence";

    public static final String TITLE_PROPERTY_NAME = "dc:title";

    public static final String DISTRIBUTION_SCHEMA = "correspondence_distribution";

    public static final String DISTRIBUTION_RECIPIENT_MAILBOXES = "cdist:recipient_mailboxes";

    public static final String DISTRIBUTION_TYPE_RECIPIENT_TYPE = "type";

    public static final String DISTRIBUTION_TYPE_RECIPIENT_RECIPIENT_MAILBOXES = "recipient_mailboxes";

    public static final String INITIAL_ACTION_INTERNAL_RECIPIENTS_PROPERTY_NAME = "cdist:initial_action_internal_recipient_mailboxes";

    public static final String INITIAL_COPY_INTERNAL_RECIPIENTS_PROPERTY_NAME = "cdist:initial_copy_internal_recipient_mailboxes";

    public static final String INITIAL_ACTION_EXTERNAL_RECIPIENTS_PROPERTY_NAME = "cdist:initial_action_external_recipient_mailboxes";

    public static final String INITIAL_COPY_EXTERNAL_RECIPIENTS_PROPERTY_NAME = "cdist:initial_copy_external_recipient_mailboxes";

    public static final String ALL_ACTION_RECIPIENTS_PROPERTY_NAME = "cdist:all_action_recipient_mailboxes";

    public static final String ALL_COPY_RECIPIENTS_PROPERTY_NAME = "cdist:all_copy_recipient_mailboxes";

    public static final String CORRESPONDENCE_DOCUMENT_CONFIDENTIALITY = "confidentiality";

    public static final String CORRESPONDENCE_DOCUMENT_DEFAULT_ENVELOPE_ID = "defaultEnvelopeId";

    public static final String CORRESPONDENCE_DOCUMENT_DATE = "document_date";

    public static final String CORRESPONDENCE_DOCUMENT_DATE_PROPERTY_NAME = "cdoc:document_date";

    public static final String CORRESPONDENCE_DOCUMENT_IMPORT_DATE = "import_date";

    public static final String CORRESPONDENCE_DOCUMENT_IMPORT_DATE_PROPERTY_NAME  = "cdoc:import_date";

    public static final String CORRESPONDENCE_DOCUMENT_ORIGIN = "origin";

    public static final String CORRESPONDENCE_DOCUMENT_ORIGIN_PROPERTY_NAME  = "cdoc:origin";

    public static final String CORRESPONDENCE_DOCUMENT_RECEIVE_DATE = "receive_date";

    public static final String CORRESPONDENCE_DOCUMENT_RECEIVE_DATE_PROPERTY_NAME  = "cdoc:receive_date";

    public static final String CORRESPONDENCE_DOCUMENT_SENDING_DATE = "sending_date";

    public static final String CORRESPONDENCE_DOCUMENT_SENDERS_PROPERTY_NAME = "cdoc:senders";

    public static final String CORRESPONDENCE_DOCUMENT_REFERENCE_PROPERTY_NAME = "cdoc:reference";

    public static final String CORRESPONDENCE_ENVELOPE_DOCUMENTS_ID_TYPE = "documentsId";

    public static final String CORRESPONDENCE_DOCUMENT_FIRST_RESPONSE_DATE = "cdoc:firstResponseDate";

    public static final String CORRESPONDENCE_DOCUMENT_RESPONSE_DOCUMENT_IDS = "cdoc:responseDocumentIds";

    public static final String CORRESPONDENCE_DOCUMENT_REPLIED_DOCUMENT_ID = "cdoc:repliedDocumentId";

    public static final String CORRESPONDENCE_CONTACTS_SENDERS = "ccont:sendersContacts";

    public static final String CORRESPONDENCE_CONTACTS_RECIPIENTS = "ccont:recipientsContacts";

    public static final String FILE_PROPERTY_NAME = "file:content";

    public static final String FILENAME_PROPERTY_NAME = "file:filename";
}
