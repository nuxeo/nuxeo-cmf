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
from xml_importer.pdf import *
from nuxeo.testcase import NuxeoTestCase

class CMF(NuxeoTestCase):
    """CMF

    This test use a configuration file CMF.conf.
    """
    _lipsum = Lipsum()

    def setUp(self):
        """Setting up test."""
        self.logd("setUp")
        NuxeoTestCase.setUp(self)
        self.memberList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'members')
        self.routeManagerList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'routeManagers')
        f = open('./xml_importer/pdf-list.txt', 'r')
        self.pdf_files = f.readlines()

    def getRandomUser(self):
        return random.choice(self.memberList)

    def test_available(self):
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")
        randUser = self.getRandomUser()
        p = LoginPage(self).login(randUser[0], randUser[1])
        Mailbox(self).addIncomingCaseItemManagementProfile()
        #CreateCase(self).createCase()
        p.logout()   

    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
