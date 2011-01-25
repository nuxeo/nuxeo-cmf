# -*- coding: iso-8859-15 -*-
"""CMF FunkLoad test

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
from utils import extractJsfState
from funkload.utils import xmlrpc_list_credentials
from nuxeo.pages import *
import pdf

class CMF(FunkLoadTestCase):
    """CMF

    This test use a configuration file CMF.conf.
    """
    _lipsum = Lipsum()

    def setUp(self):
        """Setting up test."""
        self.logd("setUp")
        self.addHeader('Accept-Language', 'fr')
        self.server_url = self.conf_get('main', 'url')
        self.credential_host = self.conf_get('credential', 'host')
        self.credential_port = self.conf_getInt('credential', 'port')
        self.cred = xmlrpc_get_credential(self.credential_host, self.credential_port)
        self.memberList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'members')
        self.routeManagerList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'routeManagers')
        f = open('./import_xml/pdf-list.txt', 'r')
        self.pdf_files = f.readlines()

    def getRandomUser(self):
        return random.choice(self.memberList)

    def getLastJsfState(self):
        return extractJsfState(self.getBody())

    def test_available(self):
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")



    def loginLogout(self):
        # The description should be set in the configuration file
        server_url = self.server_url
        randUser = self.getRandomUser()
        # begin of test ---------------------------------------------
        self.post(server_url + "/nxstartup.faces", params=[
            ['user_name', randUser.username],
            ['user_password', randUser.password],
            ['requestedUrl', ''],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Log in")
        self.get(server_url + "/logout",
            description="Log out")
        # end of test -----------------------------------------------


    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
