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
    
    def getRandomRouteManager(self):
        return random.choice(self.routeManagerList)
    
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
        caseItemId = MailboxPage(self).login(user, passwd).viewDraftTab().createCaseItem(case, caseItem, pathToPdf)
        MailboxPage(self).logout()
        print "!!!CASE" + caseItemId
        return caseItemId
    
    def attachRouteAndStart(self, user, passwd, case, caseitem, caseItemId, route):
        p = MailboxPage(self).login(user, passwd).viewDraftTab().viewCaseItem(case, caseitem, caseItemId)
        p= CaseItemPage(self).attachRouteAndStart(route , RoutePage(self).routeDocId).logout()
        
    
    def downloadFileAndApproveTaks(self, user, passwd, case, caseitem, caseItemId, pdf):
        MailboxPage(self).login(user, passwd).viewInboxTab().clickCaseItem(case, caseitem, caseItemId)
        #CaseItemPage(self).downloadFile(caseItemId , pdf).logout()
        CaseItemPage(self).logout()
        CaseItemPage(self).login(user, passwd).approveTask(case).logout()
    
    
    def verifyRouteDoneAsAdmin(self, route):
        AdminLoginPage(self).login(*self.cred_admin).viewRouteInstance(route).verifyRouteIsDone(route).logout()
    
    def test_CMF(self):
        route = "(COPY) RouteDoc"
        case = "casexx"
        caseItem = "caseitemxx"
        
        routeManager = self.getRandomRouteManager()
        
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")
        #update and validate route Model
        self.updateRoute(routeManager[0], routeManager[1], route)
        self.validateRouteModel(routeManager[0], routeManager[1], route)
        
        #add incoming mailbox profile to personal mailbox for random user
        randUser = self.getRandomUser()
        self.addIncomingMailboxProfile(randUser[0], randUser[1]) 
        caseItemId = self.createCaseItem(randUser[0], randUser[1], case, caseItem, "xml_importer/pdf_files/20pages.pdf")        
        self.attachRouteAndStart(randUser[0], randUser[1], case, caseItem, caseItemId, route)
       
        #users having received tasks, loggin
        #print"ENTER"
        #sys.stdin.readline()
        for i in CMF.usersWithTasks:
            self.downloadFileAndApproveTaks(i[0], i[1] , case, caseItem, caseItemId, "20pages.pdf")      
        #make sure the rute is done
        self.verifyRouteDoneAsAdmin(route)
    
    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
