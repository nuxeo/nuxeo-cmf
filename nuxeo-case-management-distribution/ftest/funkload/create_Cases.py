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
    def createCaseItem(self, user, passwd, case, caseItem, pathToPdf):
        caseItemId = MailboxPage(self).login(user, passwd).viewDraftTab().createCaseItem(case, caseItem, pathToPdf)
        MailboxPage(self).logout()
        return caseItemId    
    
    def test_CMF(self):
        for i in range(27000):
            case = "case" + str(i) + str(random.random())
            caseItem = "caseitem" + str(i) + str(random.random())
            self.createCaseItem("jdoe", "jdoe1", case, caseItem, "20pages.pdf")    
    
    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
