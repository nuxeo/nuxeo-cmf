# -*- coding: iso-8859-15 -*-
"""deleteUser FunkLoad test

$Id: $
"""
import unittest
from funkload.FunkLoadTestCase import FunkLoadTestCase
from webunit.utility import Upload
from funkload.utils import Data
from funkload.utils import xmlrpc_get_credential
from utils import extractJsfState

class Deleteuser(FunkLoadTestCase):
    """XXX

    This test use a configuration file Deleteuser.conf.
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
    def getLastJsfState(self):
        return extractJsfState(self.getBody())

    def test_deleteUser(self):
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

        self.get(server_url + "/nxpath/default/correspondence@view_users?subTabId=&tabId=TAB_CONTENT&conversationId=0NXMAIN&currentEmailId=",
            description="Get /nuxeo/nxpath/defau...pondence@view_users")
        self.assert_('Create a new user' in self.getBody())

        self.post(server_url + "/view_users.faces", params=[
            ['searchForm_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['searchForm:searchText', '*'],
            ['searchForm:searchButton', 'Search']],
            description="Post /nuxeo/view_users.faces")
        self.assert_('user1' in self.getBody())
        self.assert_('generic2' in self.getBody())

        self.post(server_url + "/view_users.faces", params=[
            ['j_id250_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:0:j_id254', 'j_id250:j_id251:0:j_id254']],
            description="Post /nuxeo/view_users.faces")

        self.post(server_url + "/view_user.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['deleteUserForm:j_id248', 'deleteUserForm:j_id248'],
            ['deleteUserForm_SUBMIT', '1']],
            description="Post /nuxeo/view_user.faces")

        self.post(server_url + "/view_users.faces", params=[
            ['j_id250_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:0:j_id254', 'j_id250:j_id251:0:j_id254']],
            description="Post /nuxeo/view_users.faces")

        self.post(server_url + "/view_user.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['deleteUserForm:j_id248', 'deleteUserForm:j_id248'],
            ['deleteUserForm_SUBMIT', '1']],
            description="Post /nuxeo/view_user.faces")

        self.post(server_url + "/view_users.faces", params=[
            ['j_id250_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:0:j_id254', 'j_id250:j_id251:0:j_id254']],
            description="Post /nuxeo/view_users.faces")

        self.post(server_url + "/view_user.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['deleteUserForm:j_id248', 'deleteUserForm:j_id248'],
            ['deleteUserForm_SUBMIT', '1']],
            description="Post /nuxeo/view_user.faces")

        self.post(server_url + "/view_users.faces", params=[
            ['j_id250_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:0:j_id254', 'j_id250:j_id251:0:j_id254']],
            description="Post /nuxeo/view_users.faces")

        self.post(server_url + "/view_user.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['deleteUserForm:j_id248', 'deleteUserForm:j_id248'],
            ['deleteUserForm_SUBMIT', '1']],
            description="Post /nuxeo/view_user.faces")

        self.post(server_url + "/view_users.faces", params=[
            ['j_id250_SUBMIT', '1'],
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['j_id250:j_id251:0:j_id254', 'j_id250:j_id251:0:j_id254']],
            description="Post /nuxeo/view_users.faces")

        self.post(server_url + "/view_user.faces", params=[
            ['javax.faces.ViewState', self.getLastJsfState()],
            ['deleteUserForm:j_id248', 'deleteUserForm:j_id248'],
            ['deleteUserForm_SUBMIT', '1']],
            description="Post /nuxeo/view_user.faces")
        self.assert_('No user matching the entered criteria' in self.getBody())

        # end of test -----------------------------------------------

    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
