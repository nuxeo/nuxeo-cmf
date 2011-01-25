# -*- coding: iso-8859-15 -*-
"""createMailbox FunkLoad test

$Id: $
"""
import unittest
from funkload.FunkLoadTestCase import FunkLoadTestCase
from webunit.utility import Upload
from funkload.utils import Data
from funkload.utils import xmlrpc_get_credential
from utils import extractJsfState

class Createmailbox(FunkLoadTestCase):
    """XXX

    This test use a configuration file Createmailbox.conf.
    """
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

    def test_createMailbox(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        # begin of test ---------------------------------------------

        user =  xmlrpc_get_credential(self.credential_host,
                                      self.credential_port,
                                      'writers')

        receiver =  xmlrpc_get_credential(self.credential_host,
                                          self.credential_port,
                                          'readers')

 # create personal mailbox when user log in  ---------------------------------------------

        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', user[0]],
            ['user_password', user[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('Cl - ' + user[0] in self.getBody())

        self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id226:j_id227', 'j_id226:j_id227'],
            ['j_id226_SUBMIT', '1']],
            description="Post /nuxeo/corresponden.../mailbox_view.faces")


        self.post(server_url + "/correspondence/mailbox/mailbox_manage.faces", params=[
            ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggest', ''],
            ['document_edit:nxl_correspondence_mailbox:nxw_description', ''],
            ['document_edit:j_id434', 'Save'],
            ['document_edit:j_id432', ''],
            ['document_edit_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['document_edit:nxl_correspondence_mailbox:nxw_title', user[0]],
            ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggestionBox_selection', ''],
            ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggestionBox_selection', ''],
            ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_profiles', 'cellule_courrier'],
            ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation', ''],
            ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggest', ''],
            ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggest', ''],
            ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggestionBox_selection', '']],
            description="Post /nuxeo/corresponden...ailbox_manage.faces")
        self.assert_('Create an incoming mail' in self.getBody())

        self.get(server_url + "/logout",
            description="Get /nuxeo/logout")

        first_login = receiver[0]
        while True:
            self.post(server_url + "/nxstartup.faces", params=[
                ['user_name', receiver[0]],
                ['user_password', receiver[1]],
                ['requestedUrl', ''],
                ['form_submitted_marker', ''],
                ['Submit', 'Connexion']],
                description="Log in")
            self.assert_('Cl - ' + receiver[0] in self.getBody())

            self.post(server_url + "/correspondence/mailbox/mailbox_view.faces", params=[
                ['javax.faces.ViewState', self.getLastJsfState()],
                ['j_id226:j_id227', 'j_id226:j_id227'],
                ['j_id226_SUBMIT', '1']],
                description="Post /nuxeo/corresponden.../mailbox_view.faces")

            self.post(server_url + "/correspondence/mailbox/mailbox_manage.faces", params=[
                ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggest', ''],
                ['document_edit:nxl_correspondence_mailbox:nxw_description', ''],
                ['document_edit_SUBMIT', '1'],
                ['javax.faces.ViewState', self.getLastJsfState()],
                ['document_edit:j_id434', 'Save'],
                ['document_edit:j_id432', ''],
                ['document_edit:nxl_correspondence_mailbox:nxw_title', receiver[0]],
                ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggestionBox_selection', ''],
                ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggestionBox_selection', ''],
                ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_profiles', 'cellule_courrier'],
                ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation', ''],
                ['document_edit:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggest', ''],
                ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggest', ''],
                ['document_edit:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggestionBox_selection', '']],
                description="Post /nuxeo/corresponden...ailbox_manage.faces")
    	    self.assert_('Create an incoming mail' in self.getBody())


            self.get(server_url + "/logout",
                description="Get /nuxeo/logout")

            receiver =  xmlrpc_get_credential(self.credential_host,
                                              self.credential_port,
                                              'readers')
            if receiver[0] == first_login:
                break

 # create generic mailbox ---------------------------------------------
        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', self.cred_admin[0]],
            ['user_password', self.cred_admin[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('You are logged as ' + self.cred_admin[0] in self.getBody())


        self.get(server_url + "/nxpath/default/correspondence/mailboxes@view_documents?tabId=&conversationId=0NXMAIN",
            description="Get /nuxeo/nxpath/defau...oxes@view_documents")

        self.post(server_url + "/view_documents.faces", params=[
            ['j_id272_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id272:j_id273:j_id275:0:j_id276', 'j_id272:j_id273:j_id275:0:j_id276']],
            description="Post /nuxeo/view_documents.faces")

        self.post(server_url + "/correspondence/mailbox/mailbox_create.faces", params=[
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_mailbox_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_type', 'generic'],
            ['suggestionSelectionHiddenId', 'nxw_mailbox_owner'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner', ''],
            ['suggestionSelectionOutputId', 'nxw_mailbox_owner_selectionOutput'],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggest', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_mailbox_suggest', ''],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggest', 'user1'],
            ['AJAXREQUEST', 'document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_ajax_region'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggestionBox:nxw_mailbox_owner_listRegion_select', 'document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggestionBox:nxw_mailbox_owner_listRegion_select'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggest', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_parentMailboxId', ''],
            ['suggestionInputSelectorId', 'nxw_mailbox_owner_suggest'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_validate_personal_owner', 'needed'],
            ['document_create:nxl_correspondence_mailbox:nxw_description', ''],
            ['suggestionSelectionDeleteId', 'nxw_mailbox_owner_selectionReset'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggest', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggestionBox_selection', '0'],
            ['document_create:nxl_correspondence_mailbox:nxw_title', 'mailmanagement'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation', ''],
            ['document_create_SUBMIT', '1']],
            description="Post /nuxeo/corresponden...ailbox_create.faces")

        self.post(server_url + "/correspondence/mailbox/mailbox_create.faces", params=[
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_parentMailboxId', ''],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_mailbox_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_parent_mailbox_suggest', ''],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggest', ''],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_users_suggest', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_type', 'generic'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner_suggestionBox_selection', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggestionBox_selection', ''],
            ['document_create_SUBMIT', '1'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_validate_personal_owner', 'needed'],
            ['document_create:nxl_correspondence_mailbox:nxw_title', 'mailmanagement'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_owner', 'user1'],
            ['document_create:nxl_correspondence_mailbox:nxw_description', ''],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_profiles', 'cellule_courrier'],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggest', ''],
            ['document_create:nxl_correspondence_mailbox_managers:nxw_mailbox_groups_suggestionBox_selection', ''],
            ['document_create:button_create', 'Create'],
            ['document_create:nxl_correspondence_mailbox:nxw_mailbox_affiliation_mailbox_suggest', '']],
            description="Post /nuxeo/corresponden...ailbox_create.faces")

        self.get(server_url + "/logout",
            description="Get /nuxeo/logout")

        # end of test -----------------------------------------------

    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
