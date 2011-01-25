# -*- coding: iso-8859-15 -*-
"""create user FunkLoad test

$Id: $
"""
import unittest
from funkload.FunkLoadTestCase import FunkLoadTestCase
from webunit.utility import Upload
from funkload.utils import Data
from funkload.utils import xmlrpc_get_credential
from utils import extractJsfState

class CreateUser(FunkLoadTestCase):
    """XXX

    This test use a configuration file CreateUser.conf.
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

    def test_createUser(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        # begin of test ---------------------------------------------

        user =  xmlrpc_get_credential(self.credential_host,
                                      self.credential_port,
                                      'writers')

        receiver =  xmlrpc_get_credential(self.credential_host,
                                          self.credential_port,
                                          'readers')


        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', self.cred_admin[0]],
            ['user_password', self.cred_admin[1]],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.assert_('You are logged as ' + self.cred_admin[0] in self.getBody())

        self.get(server_url + "/nxpath/default/correspondence@view_users?subTabId=&tabId=TAB_CONTENT&conversationId=0NXMAIN&currentEmailId=",
            description="Get /nuxeo/nxpath/defau...pondence@view_users")
        self.assert_('Create a new user' in self.getBody())


        self.post(server_url + "/view_users.faces", params=[
            ['j_id216_SUBMIT', '1'],
            ['j_id216:j_id218', 'j_id216:j_id218'],
            ['javax.faces.ViewState', self.getLastJsfState()]],
            description="Post /nuxeo/view_users.faces")


        self.post(server_url + "/create_user.faces", params=[
            ['createUser:nxl_user:nxw_lastname', ''],
            ['createUser_SUBMIT', '1'],
            ['createUser:nxl_user:nxw_firstPassword', user[1]],
            ['createUser:button_create', 'Save'],
            ['createUser:nxl_user:nxw_secondPassword', user[1]],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['createUser:nxl_user:nxw_firstname', ''],
            ['createUser:nxl_user:nxw_company', ''],
            ['createUser:nxl_user:nxw_username', user[0]],
            ['createUser:nxl_user:nxw_groups_suggestionBox_selection', ''],
            ['createUser:nxl_user:nxw_groups_suggest', ''],
            ['createUser:nxl_user:nxw_email', user[0]],
            ['createUser:nxl_user:nxw_passwordMatcher', 'needed']],
            description="Post /nuxeo/create_user.faces")

        first_login = receiver[0]
        while True:
            # create user

            self.get(server_url + "/nxpath/default/correspondence@view_users?subTabId=&tabId=TAB_CONTENT&conversationId=0NXMAIN&currentEmailId=",
                description="Get /nuxeo/nxpath/defau...pondence@view_users")

            self.assert_('Create a new user' in self.getBody())

            self.post(server_url + "/view_users.faces", params=[
                ['j_id216_SUBMIT', '1'],
                ['j_id216:j_id218', 'j_id216:j_id218'],
                ['javax.faces.ViewState', self.getLastJsfState()]],
                description="Post /nuxeo/view_users.faces")

            self.post(server_url + "/create_user.faces", params=[
                ['createUser:nxl_user:nxw_lastname', ''],
                ['createUser_SUBMIT', '1'],
                ['createUser:nxl_user:nxw_firstPassword', receiver[1]],
                ['createUser:button_create', 'Save'],
                ['createUser:nxl_user:nxw_secondPassword', receiver[1]],
                ['javax.faces.ViewState', self.getLastJsfState()],
                ['createUser:nxl_user:nxw_firstname', ''],
                ['createUser:nxl_user:nxw_company', ''],
                ['createUser:nxl_user:nxw_username', receiver[0]],
                ['createUser:nxl_user:nxw_groups_suggestionBox_selection', ''],
                ['createUser:nxl_user:nxw_groups_suggest', ''],
                ['createUser:nxl_user:nxw_email', receiver[0]],
                ['createUser:nxl_user:nxw_passwordMatcher', 'needed']],
                description="Post /nuxeo/create_user.faces")


            receiver =  xmlrpc_get_credential(self.credential_host,
                                              self.credential_port,
                                              'readers')
            if receiver[0] == first_login:
                break

        self.get(server_url + "/logout",
            description="Get /nuxeo/logout")


        # end of test -----------------------------------------------

    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
