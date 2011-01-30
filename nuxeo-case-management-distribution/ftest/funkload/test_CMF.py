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
    usersWithTasks = []

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

    def getRandomAdministrator(self):
        return random.choice(self.cred_admin)
    
    def updateRoute(self, user, passwd, route):
        stepsDocIds = []
        p = RoutePage(self).login(user, passwd).viewRouteContentTab(user, route)
        #get all the route steps docIds to modify the route from the route view
        stepsDocIds = p.getStepsDocsIds(stepsDocIds)
        #lock route and modify steps
        RoutePage(self).viewRouteElementsTab(user, route).lockRoute(user, route)
        for i in stepsDocIds:
            randUser = self.getRandomUser()
            CMF.usersWithTasks.append(randUser)
            RoutePage(self).updateStepDistributionMailboxFromRouteView(i, "user-" + randUser[0])
        p.logout()    
    
    def validateRouteModel(self, user, passwd, route):
        RoutePage(self).login(user, passwd).viewRouteContentTab(user, route).validateRouteModel().logout()
    
    def addIncomingMailboxProfile(self, user, passwd):
        MailboxPage(self).login(user, passwd).viewManageTab().addIncomingCaseItemManagementProfile().logout()
    
    def createCaseItem(self, user, passwd, case, caseItem, pathToPdf):
        MailboxPage(self).login(user, passwd).viewDraftTab().createCaseItem(case, caseItem, pathToPdf).logout()
    
    def attachRouteAndStart(self, user, passwd, caseitem, route):
        p = MailboxPage(self).login(user, passwd).viewDraftTab().viewCaseItemInDraft(caseitem)
        p= CaseItemPage(self).attachRouteAndStart(route , RoutePage(self).routeDocId).logout()
        
    
    def downloadFileAndApproveTaks(self, user, passwd, case, caseitem, pdf):
        caseItemId = MailboxPage(self).login(user, passwd).viewInboxTab().viewCaseItem(caseitem)
        CaseItemPage(self).downloadFile(caseItemId , pdf).logout()
        CaseItemPage(self).login(user, passwd).approveTask(case).logout()
    
    
    def verifyRouteDoneAsAdmin(self, route):
        AdminLoginPage(self).login(*self.cred_admin).viewRouteInstance(route).verifyRouteIsDone(route).logout()
    
    def test_CMF(self):
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")
        #update and validate route Model
        self.updateRoute("jdoe", "jdoe1", "(COPY) RouteDoc")
        self.validateRouteModel("jdoe", "jdoe1", "(COPY) RouteDoc")
        
        #add incoming mailbox profile to personal mailbox for random user
        randUser = self.getRandomUser()
        self.addIncomingMailboxProfile(randUser[0], randUser[1]) 
        p = self.createCaseItem(randUser[0], randUser[1], "casex", "caseitemx", "xml_importer/pdf_files/20pages.pdf")        
        self.attachRouteAndStart(randUser[0], randUser[1], "caseitemx", "(COPY) RouteDoc")
       
        #users having received tasks, loggin
        for i in CMF.usersWithTasks:
            #print "User" + i[0]
            self.downloadFileAndApproveTaks(i[0], i[1] , "casex", "caseitemx", "20pages.pdf")      
        #make sure the rute is done
        self.verifyRouteDoneAsAdmin("(COPY) RouteDoc")
    
    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
