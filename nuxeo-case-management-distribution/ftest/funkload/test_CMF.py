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
    
    def getPasswordForUser(self, user):
        for i in self.memberList:
            if cmp(i[0],user):
                return i[1]

    def extractRouteStepsIds(self, user, passwd, routeInstanceName):
        stepsDocIds = []
        p = RouteInstancePage(self).login(user, passwd).viewRouteInstance(routeInstanceName)
        #get all the route steps docIds to modify the route from the route view
        stepsDocIds = p.getStepsDocsIds(stepsDocIds)
        p.logout()
        return stepsDocIds
    
    def updateRoute(self, user, passwd, case, route, stepsDocIds):
        #lock route and modify steps
        CaseItemPage(self).login(user, passwd).viewRelatedStartedRoute(case)
        p = RoutePage(self).lockRoute(user, route)
        j = 0
        for i in stepsDocIds:
            randUser = self.getRandomUser()
            if RoutePage(self).stepCanBeUpdated(i) is True:
                CMF.usersWithTasks.append(randUser)
                CaseItemPage(self).refreshRelatedStartedRoute(case)
                RoutePage(self).updateStepDistributionMailboxFromRouteView(i, "user-" + randUser[0], j)
            j = j + 1     
        p.logout()    
   
    def extractRouteModelId(self, routeMan, routeManPass, route):
        RoutePage(self).login(routeMan, routeManPass).getRouteModelDocId(routeMan, route).logout()
    
    def addIncomingMailboxProfile(self, user, passwd):
        MailboxPage(self).login(user, passwd).viewManageTab().addIncomingCaseItemManagementProfile().logout()
    
    def createCaseItem(self, user, passwd, case, caseItem, pathToPdf):
        caseItemId = MailboxPage(self).login(user, passwd).viewDraftTab().createCaseItem(case, caseItem, pathToPdf)
        MailboxPage(self).logout()
        return caseItemId
    
    def attachRouteAndStart(self, user, passwd, case, caseitem, caseItemId, route):
        p = MailboxPage(self).login(user, passwd).viewDraftTab().viewCaseItem(case, caseitem, caseItemId)
        routeInstanceName = CaseItemPage(self).attachRouteAndStart(route , RoutePage(self).routeDocId).viewRelatedStartedRoute(case)
        p.logout()
        return routeInstanceName
    
    def downloadFileAndApproveTaks(self, user, passwd, case, caseitem, caseItemId, pdf):
        MailboxPage(self).login(user, passwd).viewInboxTab().clickCaseItem(case, caseitem, caseItemId)
        CaseItemPage(self).downloadFile(caseItemId , pdf).logout()
        CaseItemPage(self).logout()
        approveLink = CaseItemPage(self).login(user, passwd).extractTaskApproveLink(case)
        if(approveLink is not None):
            CaseItemPage(self).approveTask(case, approveLink)
        CaseItemPage(self).logout()
    
    def verifyRouteDoneAsAdmin(self, routeInstanceName, case):
        RouteInstancePage(self).login(*self.cred_admin).viewRouteInstance(routeInstanceName).verifyRouteIsDone(routeInstanceName, case).logout()
    
    def test_CMF(self):
        route = "FunkloadRouteDoc"
        case = "case" + str(self.thread_id) + str(random.random())
        caseItem = "caseitem" + str(self.thread_id) + str(random.random())
  
        routeManager = self.getRandomRouteManager()
        randUser = self.getRandomUser()
        
        user =  randUser[0]
        passwd = randUser[1]
        
        server_url = self.server_url
        self.get(server_url,
                 description="Check if the server is alive")
        
        self.extractRouteModelId(routeManager[0], routeManager[1], route)
        
        
        
        self.addIncomingMailboxProfile(routeManager[0], routeManager[1])
        caseItemId = self.createCaseItem(routeManager[0], routeManager[1], case, caseItem, "xml_importer/pdf_files/20pages.pdf")        
        routeInstanceName = self.attachRouteAndStart(routeManager[0], routeManager[1], case, caseItem, caseItemId, route)
        
        stepsDocIds = self.extractRouteStepsIds(routeManager[0], routeManager[1], routeInstanceName) 
        self.updateRoute(routeManager[0], routeManager[1], case ,route, stepsDocIds)
       
        #FIXME : approve the first already running task ( this step couldn't be modified)
        self.downloadFileAndApproveTaks("lbramard", "lbramard1" , case, caseItem, caseItemId, "20pages.pdf")
        #users having received tasks, loggin
        for i in CMF.usersWithTasks:
            self.downloadFileAndApproveTaks(i[0], i[1] , case, caseItem, caseItemId, "20pages.pdf")      
        #make sure the rute is done
        self.verifyRouteDoneAsAdmin(routeInstanceName, case)
        
    
    def tearDown(self):
        """Setting up test."""
        self.logd("tearDown.\n")



if __name__ in ('main', '__main__'):
    unittest.main()
