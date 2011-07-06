# -*- coding: iso-8859-15 -*-
"""CMF FunkLoad test

$Id: $
"""
import unittest
import random
import uuid
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
    usersWithTasks = []

    def setUp(self):
        """Setting up test."""
        self.logd("setUp")
        NuxeoTestCase.setUp(self)
        self.memberList =  xmlrpc_list_credentials(self.credential_host,
                                                  self.credential_port,
                                                  'members')

    def getRandomUser(self):
        return random.choice(self.memberList)

    def viewCaseinPersonalMailbox(self, user, passwd):
       MailboxPage(self).login(user, passwd).viewMailbox('members').viewManageTab().viewMailbox(user).viewDraftTab().viewFirstCaseInMailbox().logout()

    
    def test_CMF(self):
        randUser = self.getRandomUser()
        user = randUser[0]
        passwd = randUser[1]
        self.viewCaseinPersonalMailbox(user, passwd)

    
    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
