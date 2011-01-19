# -*- coding: iso-8859-15 -*-
"""nxc FunkLoad test

$Id: $
"""
import unittest
from datetime import datetime
from webunit.utility import Upload
from funkload.FunkLoadTestCase import FunkLoadTestCase
from funkload.utils import Data
from funkload.Lipsum import Lipsum
from funkload.utils import xmlrpc_get_credential
from utils import extractJsfState

class Nxc(FunkLoadTestCase):
    """Nuxeo Courrier

    This test use a configuration file Nxc.conf.
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

    def getLastJsfState(self):
        return extractJsfState(self.getBody())

    def test_available(self):
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")

    def test_nxc_admin(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        # begin of test ---------------------------------------------


        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', self.cred_admin[0]],
            ['user_password', self.cred_admin[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('You are logged as ' + self.cred_admin[0] in self.getBody())

        self.get(server_url + "/nxpath/default/correspondence/mailboxes@view_documents?tabId=&conversationId=0NXMAIN",
            description="View mailbox")

        self.get(server_url + "/nxpath/default/correspondence/mailboxes/user1@mailbox_view?tabId=&conversationId=0NXMAIN",
            description="View user 1 mailbox")

        self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
            ['j_id250', 'j_id250'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:1:j_id252', 'j_id250:j_id251:1:j_id252']],
            description="Post /corresponden.../mailbox_view.faces")

        self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
            ['j_id250', 'j_id250'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:2:j_id252', 'j_id250:j_id251:2:j_id252']],
            description="Post /corresponden.../mailbox_view.faces")

        self.get(server_url + "/logout",
            description="Log out")

        # end of test -----------------------------------------------

    def test_nxc_writer(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        # begin of test ---------------------------------------------
        lipsum = self._lipsum
        user =  xmlrpc_get_credential(self.credential_host,
                                      self.credential_port,
                                      'writers')

        receiver =  xmlrpc_get_credential(self.credential_host,
                                          self.credential_port,
                                          'readers')

        today = datetime.today().strftime('%m/%d/%y')

        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', user[0]],
            ['user_password', user[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('You are logged as ' + user[0] in self.getBody())


        self.get(server_url + "/nxpath/default/correspondence/mailboxes/mailmanagement@mailbox_view?tabId=&conversationId=0NXMAIN",
                 description="View mailmnanagement")

        self.assert_('Create an incoming mail' in self.getBody())

        self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
            ['j_id245', 'j_id245'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id245:j_id246', 'j_id245:j_id246'],            
            ['j_id245_SUBMIT', '1']],
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
            ['document_create:j_id384', 'Create'],
            ['javax.faces.ViewState', self.getLastJsfState()]],
            description="Submit new mail")

        self.assert_('Incoming mail saved' in self.getBody())


        self.post(server_url + "/correspondence/mail/view_correspondence_envelope.faces", params=[
            ['j_id359', 'j_id359'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id359_SUBMIT', '1'],
            ['j_id359:j_id360:j_id361:0:j_id362', 'j_id359:j_id360:j_id361:0:j_id362']],
            description="View distribution form")
        self.assert_('distribution_recipients' in self.getBody())

        self.post(server_url + "/correspondence/mail/distribution_correspondence_envelope.faces", params=[
            ['AJAXREQUEST', 'distribution_recipients:nxl_correspondence_recipients:j_id347'],
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
            ['AJAXREQUEST', 'distribution_recipients:nxl_correspondence_recipients:j_id347'],
            ['distribution_recipients', 'distribution_recipients'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox_selection', '0'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:comment', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox:j_id354', 'distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox:j_id354'],
            ['suggestionSelectionListId', 'nxw_action_recipients_list']],
            description="Select receiver")

        self.post(server_url + "/correspondence/mail/distribution_correspondence_envelope.faces", params=[
            ['distribution_recipients', 'distribution_recipients'],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_action_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggest', ''],
            ['distribution_recipients:nxl_correspondence_recipients:nxw_copy_recipients_suggestionBox_selection', ''],
            ['distribution_recipients:comment', lipsum.getSentence()],
            ['distribution_recipients:j_id406', 'Send'],
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
