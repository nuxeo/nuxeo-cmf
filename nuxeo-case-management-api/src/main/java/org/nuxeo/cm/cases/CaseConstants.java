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
 * Lesser General Public License for more detail.
 *
 * Contributors:
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.cases;

/**
 * @author Anahide Tchertchian
 */
public class CaseConstants {

    public static final String CASE_TREE_TYPE = "Folder";

    public static final String CASE_ROOT_DOCUMENT_PATH = "/case-management/case-root";

    public static final String CASE_TYPE = "Case";

    public static final String CASE_SCHEMA = "case";

    public static final String CASE_ITEM_DOCUMENT_SCHEMA = "case_item";

    public static final String MAILBOX_FACET = "Mailbox";

    public static final String CASE_GROUPABLE_FACET = "CaseGroupable";

    public static final String CASE_ITEM_DOCUMENT_TYPE = "CaseItem";

    public static final String TITLE_PROPERTY_NAME = "dc:title";

    public static final String DISTRIBUTABLE_FACET = "Distributable";

    public static final String DISTRIBUTION_SCHEMA = "distribution";

    public static final String DISTRIBUTION_RECIPIENT_MAILBOXES = "cmdist:particpant_mailboxes";

    public static final String DISTRIBUTION_TYPE_PARTICIPANT_TYPE = "type";

    public static final String DISTRIBUTION_TYPE_PARTICIPANTS_MAILBOXS = "participants_mailboxes";

    public static final String INITIAL_ACTION_INTERNAL_PARTICIPANTS_PROPERTY_NAME = "cmdist:initial_action_internal_participant_mailboxes";

    public static final String INITIAL_COPY_INTERNAL_PARTICIPANTS_PROPERTY_NAME = "cmdist:initial_copy_internal_participant_mailboxes";

    public static final String INITIAL_ACTION_EXTERNAL_PARTICIPANTS_PROPERTY_NAME = "cmdist:initial_action_external_participant_mailboxes";

    public static final String INITIAL_COPY_EXTERNAL_PARTICIPANTS_PROPERTY_NAME = "cmdist:initial_copy_external_participant_mailboxes";

    public static final String ALL_ACTION_PARTICIPANTS_PROPERTY_NAME = "cmdist:all_action_participant_mailboxes";

    public static final String ALL_COPY_PARTICIPANTS_PROPERTY_NAME = "cmdist:all_copy_participant_mailboxes";

    public static final String DOCUMENT_CONFIDENTIALITY = "confidentiality";

    public static final String DOCUMENT_DEFAULT_CASE_ID = "defaultCaseId";

    public static final String DOCUMENT_DATE = "document_date";

    public static final String DOCUMENT_DATE_PROPERTY_NAME = "cmdoc:document_date";

    public static final String DOCUMENT_IMPORT_DATE = "import_date";

    public static final String DOCUMENT_IMPORT_DATE_PROPERTY_NAME = "cmdoc:import_date";

    public static final String DOCUMENT_ORIGIN = "origin";

    public static final String DOCUMENT_ORIGIN_PROPERTY_NAME = "cmdoc:origin";

    public static final String DOCUMENT_RECEIVE_DATE = "receive_date";

    public static final String DOCUMENT_RECEIVE_DATE_PROPERTY_NAME = "cmdoc:receive_date";

    public static final String DOCUMENT_SENDING_DATE = "sending_date";

    public static final String DOCUMENT_SENDERS_PROPERTY_NAME = "cmdoc:senders";

    public static final String DOCUMENT_REFERENCE_PROPERTY_NAME = "cmdoc:reference";

    public static final String MAILBOX_DOCUMENTS_ID_TYPE = "documentsId";

    public static final String MAILBOX_DOCUMENTS_ID_PROPERTY_NAME = "case:documentsId";

    public static final String CONTACTS_SENDERS = "cmct:sendersContacts";

    public static final String CONTACTS_PARTICIPANTS = "cmct:participantsContacts";

    public static final String FILE_PROPERTY_NAME = "file:content";

    public static final String FILENAME_PROPERTY_NAME = "file:filename";

    public static final String MAIN_INJECTION_ORIGIN = "mail";

    public static final String DOCUMENT_DEFAULT_CASE_ID_PROPERTY_NAME = "cmdoc:defaultCaseId";

    public static final String DOCUMENT_IMPORTED_PREFIX = "imported-";

    // operation
    public static final String OPERATION_CASE_LINKS_KEY = "operation.case.links.key";

    public static final String OPERATION_CASE_LINK_KEY = "operation.case.link.key";

    public static final String CASE_MANAGEMENT_OPERATION_CATEGORY = "CaseManagement";

    public static final String OPERATION_CHAIN_DISTRIBUTION_TASK_CHAIN = "DistributionTaskChain";

    public static final String OPERATION_CHAIN_GENERIC_DISTRIBUTION_TASK_CHAIN = "GenericDistributionTaskChain";

    public static final String OPERATION_CHAIN_PERSONAL_DISTRIBUTION_TASK_CHAIN = "PersonalDistributionTaskChain";

    public static final String OPERATION_CHAIN_DISTRIBUTION_STEP_CHAIN = "DistributionStepChain";

    // step document
    public static final String STEP_DOCUMENT_TYPE_DISTRIBUTION_TASK = "DistributionTask";

    public static final String STEP_DOCUMENT_TYPE_GENERIC_DISTRIBUTION_TASK = "GenericDistributionTask";

    public static final String STEP_DOCUMENT_TYPE_PERSONAL_DISTRIBUTION_TASK = "PersonalDistributionTask";

    public static final String STEP_DOCUMENT_TYPE_DISTRIBUTION_STEP = "DistributionStep";

    public static final String STEP_DISTRIBUTION_MAILBOX_ID_PROPERTY_NAME = "rtsk:distributionMailboxId";

    public static final String STEP_DISTRIBUTION_DUE_DATE_PROPERTY_NAME = "rtsk:dueDate";

    public static final String STEP_DISTRIBUTION_AUTOMATIC_VALIDATION_PROPERTY_NAME = "rtsk:automaticValidation";

    // Constant utility class
    private CaseConstants() {
    }

}
