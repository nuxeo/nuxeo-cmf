# -*- coding: iso-8859-15 -*-
"""nxc FunkLoad test

$Id: $
"""
import unittest
import random
from datetime import datetime
from webunit.utility import Upload
from funkload.FunkLoadTestCase import FunkLoadTestCase
from funkload.utils import Data
from funkload.Lipsum import Lipsum
from funkload.utils import xmlrpc_get_credential
from funkload.utils import xmlrpc_list_credentials
from utils import extractJsfState

class Distribution(FunkLoadTestCase):
    """Nuxeo Courrier

    This test use a configuration file Distribution.conf.
    """
    _lipsum = Lipsum()

    def setUp(self):
        """Setting up test."""
        self.logd("setUp")
        self.addHeader('Accept-Language', 'en')
        self.server_url = self.conf_get('main', 'url')
        self.credential_host = self.conf_get('credential', 'host')
        self.credential_port = self.conf_getInt('credential', 'port')
        self.cred_admin = xmlrpc_get_credential(self.credential_host,
                                                self.credential_port,
                                                'admin')
        self.cred_member =  xmlrpc_get_credential(self.credential_host,
                                                  self.credential_port,
                                                  'members')

        self.senderList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'writers')

        self.receiverList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'readers')

    def getLastJsfState(self):
        return extractJsfState(self.getBody())

    def getRandReceiver(self):
	index = random.randint(0,len(self.receiverList) - 1)
        return self.receiverList[index]

    def getRandSender(self):
	index = random.randint(0,len(self.senderList) - 1)
        return self.senderList[index]

    def test_distribution(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        # begin of test ---------------------------------------------
        lipsum = self._lipsum
        today = datetime.today().strftime('%m/%d/%y')
	randSender = self.getRandSender()
	receiver = self.getRandReceiver()

        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', randSender[0]],
            ['user_password', randSender[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('You are logged as ' + randSender[0] in self.getBody())

        self.get(server_url + "/nxpath/default/correspondence/mailboxes/" + randSender[0] + "@mailbox_view?tabId=&conversationId=0NXMAIN",
                 description="View personal mailbox")

        self.assert_('Create an incoming mail' in self.getBody())


        """Create incoming mail."""
        self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
            ['j_id245', 'j_id245'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id226:j_id227', 'j_id226:j_id227'],
            ['j_id226_SUBMIT', '1']],
            description="View mail form")

        self.assert_('nxl_head_correspondence_incoming_document' in self.getBody())

        self.post(server_url + "/correspondence/mail/create_correspondence_document.faces", params=[
            ['AJAXREQUEST', 'document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_region'],
            ['document_create', 'document_create'],
            ['document_create:nxl_correspondence_document:nxw_title', ''],
            ['document_create:nxl_correspondence_document:nxw_document_type', '2'],
            ['document_create:nxl_correspondence_document:nxw_sending_date', ''],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_sending_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_receive_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_document_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_head_correspondence_incoming_document:nxw_scan_date:trigger'],
            ['document_create:nxl_correspondence_document:nxw_receive_date', ''],
            ['document_create:nxl_correspondence_document:nxw_document_date', ''],
            ['document_create:nxl_correspondence_document:nxw_confidentiality', '4'],
            ['document_create:nxl_correspondence_document:nxw_origin', ''],
            ['document_create:nxl_correspondence_document:nxw_reference', ''],
            ['document_create:nxl_correspondence_document:nxw_body', ''],
            ['mce_editor_0_styleSelect', ''],
            ['mce_editor_0_formatSelect', ''],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_site', ''],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_date', ''],
            ['document_create:nxl_file:nxw_file:nxw_file_file:choice', 'none'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:upload', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['type', 'add'],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_add', 'document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_add'],
            ['for', 'nxw_contactsRecipients_list']],
            description="Add recipient")

        self.post(server_url + "/correspondence/mail/create_correspondence_document.faces", params=[
            ['AJAXREQUEST', 'document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_region'],
            ['document_create', 'document_create'],
            ['document_create:nxl_correspondence_document:nxw_title', ''],
            ['document_create:nxl_correspondence_document:nxw_document_type', '2'],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_name', ''],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_surname', ''],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_service', ''],
            ['document_create:nxl_correspondence_document:nxw_sending_date', ''],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_sending_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_receive_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_correspondence_document:nxw_document_date:trigger'],
            ['document_create_link_hidden_', 'document_create:nxl_head_correspondence_incoming_document:nxw_scan_date:trigger'],
            ['document_create:nxl_correspondence_document:nxw_receive_date', ''],
            ['document_create:nxl_correspondence_document:nxw_document_date', ''],
            ['document_create:nxl_correspondence_document:nxw_confidentiality', '4'],
            ['document_create:nxl_correspondence_document:nxw_origin', ''],
            ['document_create:nxl_correspondence_document:nxw_reference', ''],
            ['document_create:nxl_correspondence_document:nxw_body', ''],
            ['mce_editor_0_styleSelect', ''],
            ['mce_editor_0_formatSelect', ''],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_site', ''],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_date', ''],
            ['document_create:nxl_file:nxw_file:nxw_file_file:choice', 'none'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:upload', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_add', 'document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_add'],
            ['type', 'add'],
            ['for', 'nxw_contactsSenders_list']],
            description="Add sender")

        self.post(server_url + "/correspondence/mail/create_correspondence_document.faces", params=[
            ['document_create', 'document_create'],
            ['document_create:nxl_correspondence_document:nxw_title', lipsum.getSubject()],
            ['document_create:nxl_correspondence_document:nxw_document_type', '2'],

            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_name', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_surname', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_contactsRecipients:nxw_contactsRecipients_list:0:nxw_contactsRecipients_service', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_sending_date', today + ' 1:00 PM'],
            ['document_create:nxl_correspondence_document:nxw_receive_date', today + ' 2:00 PM'],
            ['document_create:nxl_correspondence_document:nxw_document_date', today + ' 11:00 AM'],
            ['document_create:nxl_correspondence_document:nxw_confidentiality', '4'],
            ['document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_list:0:nxw_contactsSenders_name', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_list:0:nxw_contactsSenders_surname', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_contactsSenders:nxw_contactsSenders_list:0:nxw_contactsSenders_service', lipsum.getWord()],
            ['document_create:nxl_correspondence_document:nxw_origin', lipsum.getAddress()],
            ['document_create:nxl_correspondence_document:nxw_reference', lipsum.getUniqWord()],
            ['document_create:nxl_correspondence_document:nxw_body', lipsum.getParagraph()],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_site', 'nuxeo'],
            ['document_create:nxl_head_correspondence_incoming_document:nxw_scan_date', today + ' 10:00 AM'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:choice', 'upload'],
            ['document_create_SUBMIT', '1'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:upload', Upload("hello.pdf")],
            ['document_create:j_id365', 'Create'],
            ['javax.faces.ViewState', self.getLastJsfState()]],
            description="Submit new mail")

        self.assert_('Incoming mail saved' in self.getBody())


        self.post(server_url + "/correspondence/mail/view_correspondence_envelope.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id340_SUBMIT', '1'],
            ['j_id340:j_id341:j_id342:0:j_id343', 'j_id340:j_id341:j_id342:0:j_id343']],
            description="View distribution form")
        self.assert_('distribution_recipients' in self.getBody())

        self.post(server_url + "/correspondence/mail/distribution_correspondence_envelope.faces", params=[
            ['AJAXREQUEST', 'distribution_recipients:nxl_correspondence_recipients:j_id328'],
            ['distribution_recipients', 'distribution_recipients'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggest', receiver[0]],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:comment', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['ajaxSingle', 'distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox'],
            ['mailboxSuggestionSearchType', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox', 'distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox'],
            ['inputvalue', receiver[0]],
            ['AJAX:EVENTS_COUNT', '1']],
            description="Search receiver")

        self.assert_(receiver[0] in self.getBody())

        self.post(server_url + "/correspondence/mail/distribution_correspondence_envelope.faces", params=[
            ['AJAXREQUEST', 'distribution_recipients:nxl_correspondence_recipients:j_id328'],
            ['distribution_recipients', 'distribution_recipients'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox_selection', '0'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:comment', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox:j_id335', 'distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox:j_id335'],
            ['suggestionSelectionListId', 'nxw_action_recipients_list']],
            description="Select receiver")

        self.post(server_url + "/correspondence/mail/distribution_correspondence_envelope.faces", params=[
            ['distribution_recipients', 'distribution_recipients'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:comment', lipsum.getSentence()],
            ['distribution_recipients:j_id389', 'Send'],
            ['distribution_recipients_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()]],
            description="Send mail")

        self.assert_('The distribution is done' in self.getBody())



        self.get(server_url + "/logout",
            description="Log out")

        # end of test -----------------------------------------------


    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
