# (C) Copyright 2009 Nuxeo SAS <http://nuxeo.com>
# Author: bdelbosc@nuxeo.com
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
# 02111-1307, USA.
#
"""
This modules is tied with the Nuxeo EP application.

TODO:

Folder

* emptyTrash()

* next() previous()

Dashboard

* wsRefresh()

Document

* files().add(filename, path)

* comments().add(comment)

* relation().add(uid)

* publish(section_uid)
"""
import random
import time
from urllib import quote_plus, quote
from webunit.utility import Upload
from utils import extractToken, extractJsfState, extractIframes, extractJsessionId
from funkload.utils import Data
import datetime
import re


class BasePage:
    """Base class for nuxeo ep page."""
    fl = None

    def __init__(self, fl):
        self.fl = fl

    # helpers
    def getDocUid(self):
        fl = self.fl
        uid = extractToken(fl.getBody(), "var currentDocURL='default/", "'")
        fl.assert_(uid, 'Current document uid not found.')
        return uid

    def getConversationId(self):
        fl = self.fl
        cId = extractToken(fl.getBody(), "var currentConversationId='", "'")
        fl.assert_(cId, 'Current conversation id not found')
        return cId

    def available(self):
        """Check if the server is available."""
        fl = self.fl
        fl.get(fl.server_url + '/login.jsp',
               description="Check if the server is alive")


    # pages
    def logout(self):
        """Log out the current user."""
        fl = self.fl
        fl.get(fl.server_url + '/logout',
               description="Log out")
        fl.assert_('login' in fl.getLastUrl(),
                     "Not redirected to login page.")
        fl.current_login = None
        return LoginPage(self.fl)

    def login(self, user, password):
        fl = self.fl
        fl.setHeader('Accept-Language', 'en')
        fl.post(fl.server_url + "/nxstartup.faces", params=[
            ['language', 'en_US'],
            ['user_name', user],
            ['user_password', password],
            ['form_submitted_marker', ''],
            ['requestedUrl', ''],
            ['Submit', 'Connexion']],
            description="Login " + user)
        fl.assert_('LoginFailed=true' not in fl.getLastUrl(),
                   'Login failed for %s:%s' % (user, password))
        fl.assert_(fl.listHref(content_pattern="Log out"),
                   "No log out link found on the welcome page")
        fl.current_login = user
        return FolderPage(self.fl)

    def loginInvalid(self, user, password):
        fl = self.fl
        fl.post(fl.server_url + "/nxstartup.faces", params=[
            ['user_name', user],
            ['user_password', password],
            ['form_submitted_marker', ''],
            ['Submit', 'Connexion']],
            description="Login invalid user " + user)
        fl.assert_('loginFailed=true' in fl.getLastUrl(),
                   'Invalid login expected for %s:%s.' % (user, password))
        return self

    def viewDocumentPath(self, path, description=None, raiseOn404=True,
                         outcome=None):
        """This method return None when raiseOn404 is False and the document
        does not exist"""
        fl = self.fl
        if not description:
            description = "View document path:" + path
        ok_codes = [200, 301, 302, 303, 307]
        if not raiseOn404:
            ok_codes.append(404)
        if not outcome:
            outcome = "view_documents"
        resp = fl.get(fl.server_url + "/nxpath/default/case-management/" + 
               quote(path) + "@" + outcome + '?conversationId=0NXMAIN1',
               description=description, ok_codes=ok_codes)
        if resp.code == 404:
            fl.logi('Document ' + path + ' does not exists.')
            return None
        return self

    def viewDocumentUid(self, uid, tab='', subtab='', description=None,
                        outcome=None):
        fl = self.fl
        if not description:
            description = "View document uid:" + uid + ' ' + tab + subtab
        if not outcome:
            outcome = "view_documents"

        url = '/nxdoc/default/' + uid + '/' + outcome + '?tabId=' + tab
        if subtab:
            url += "&subTabId=" + subtab
        url += '=&conversationId=0NXMAIN1'
        fl.get(fl.server_url + url,
               description=description)
        return self

    def getRootWorkspaces(self):
        return self.viewDocumentPath("workspaces")

    def getRootSections(self):
        return self.viewDocumentPath("sections")

    def memberManagement(self):
        fl = self.fl
        self.viewDocumentUid(self.getDocUid(), outcome='view_users',
                             description="View member management")
        return self

    def createUser(self, username, email, password, firstname='',
                   lastname='', company='', groups=''):
        """This method does not raise exception if user already exists"""
        fl = self.fl
        self.memberManagement()

        fl.post(fl.server_url + '/view_users.faces', params=[
            ['createUserActionsForm:createUserButton', 'createUserActionsForm:createUserButton'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['createUserActionsForm_SUBMIT', '1']],
            description="View user creation form")

        jsfState = fl.getLastJsfState()

        fl.post(fl.server_url + '/create_user.faces', params=[
            ['AJAXREQUEST', 'createUser:nxl_user:nxw_groups_ajax_region'],
            ['createUser:nxl_user:nxw_username', username],
            ['createUser:nxl_user:nxw_firstname', firstname],
            ['createUser:nxl_user:nxw_lastname', lastname],
            ['createUser:nxl_user:nxw_company', company],
            ['createUser:nxl_user:nxw_email', email],
            ['createUser:nxl_user:nxw_firstPassword', password],
            ['createUser:nxl_user:nxw_secondPassword', password],
            ['createUser:nxl_user:nxw_passwordMatcher', 'needed'],
            ['createUser:nxl_user:nxw_groups_suggest', groups],
            ['createUser:nxl_user:nxw_groups_suggestionBox_selection', ''],
            ['createUser_SUBMIT', '1'],
            ['javax.faces.ViewState', jsfState],
            ['userSuggestionSearchType', 'GROUP_TYPE'],
            ['userSuggestionMaxSearchResults', '0'],
            ['ajaxSingle', 'createUser:nxl_user:nxw_groups_suggestionBox'],
            ['createUser:nxl_user:nxw_groups_suggestionBox', 'createUser:nxl_user:nxw_groups_suggestionBox'],
            ['inputvalue', groups],
            ['AJAX:EVENTS_COUNT', '1']],
            description="Create user search group")

        fl.post(fl.server_url + '/create_user.faces', params=[
            ['AJAXREQUEST', 'createUser:nxl_user:nxw_groups_ajax_region'],
            ['createUser:nxl_user:nxw_username', username],
            ['createUser:nxl_user:nxw_firstname', firstname],
            ['createUser:nxl_user:nxw_lastname', lastname],
            ['createUser:nxl_user:nxw_company', company],
            ['createUser:nxl_user:nxw_email', email],
            ['createUser:nxl_user:nxw_firstPassword', password],
            ['createUser:nxl_user:nxw_secondPassword', password],
            ['createUser:nxl_user:nxw_passwordMatcher', 'needed'],
            ['createUser:nxl_user:nxw_groups_suggest', groups],
            ['createUser:nxl_user:nxw_groups_suggestionBox_selection', '0'],
            ['createUser_SUBMIT', '1'],
            ['javax.faces.ViewState', jsfState],
            ['createUser:nxl_user:nxw_groups_suggestionBox:nxw_groups_listRegion_select', 'createUser:nxl_user:nxw_groups_suggestionBox:nxw_groups_listRegion_select'],
            ['suggestionSelectionListId', 'nxw_groups_list'],
            ['suggestionInputSelectorId', 'nxw_groups_suggest']],
            description="Create user select group")

        fl.post(fl.server_url + "/create_user.faces", params=[
            ['createUser:nxl_user:nxw_username', username],
            ['createUser:nxl_user:nxw_firstname', firstname],
            ['createUser:nxl_user:nxw_lastname', lastname],
            ['createUser:nxl_user:nxw_company', company],
            ['createUser:nxl_user:nxw_email', email],
            ['createUser:nxl_user:nxw_firstPassword', password],
            ['createUser:nxl_user:nxw_secondPassword', password],
            ['createUser:nxl_user:nxw_passwordMatcher', 'needed'],
            ['createUser:nxl_user:nxw_groups_suggest', ''],
            ['createUser:nxl_user:nxw_groups_suggestionBox_selection', ''],
            ['createUser:button_create', 'Save'],
            ['createUser_SUBMIT', '1'],
            ['javax.faces.ViewState', jsfState]],
                description="Create user submit form")
        return self

    def dashboard(self):
        """jsf dashboard"""
        fl = self.fl
        self.viewDocumentUid(self.getDocUid(), outcome='user_dashboard',
                             description="View JSF dashboard")
        fl.assert_('My workspaces' in fl.getBody(),
                   'Not a dashboard page')
        return self

    def dashboardNew(self):
        """open social dashboard"""
        fl = self.fl
        server_url = fl.server_url
        fl.post(server_url + "/view_documents.faces", params=[
            ['userServicesForm:userServicesActionsTable:0:userServicesActionCommandLink', 'userServicesForm:userServicesActionsTable:0:userServicesActionCommandLink'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['userServicesForm_SUBMIT', '1']],
                description="Dashboard opensocial")

        ts = str(time.time())
        jid = extractJsessionId(fl)
        uid = extractToken(fl.getBody(), """return "{docRef:'""", "'")
        fl.assert_(len(uid) == 36)

        fl.get(server_url + "/org.nuxeo.opensocial.container.ContainerEntryPoint/org.nuxeo.opensocial.container.ContainerEntryPoint.nocache.js",
                 description="Get container entry point")
        data = Data('text/x-gwt-rpc; charset=utf-8', '''5|0|17|''' + server_url + '''/org.nuxeo.opensocial.container.ContainerEntryPoint/|9CCFB53A0997F1E4596C8EE4765CCBAA|org.nuxeo.opensocial.container.client.service.api.ContainerService|getContainer|java.util.Map|java.util.HashMap/962170901|java.lang.String/2004016611|docRef|''' + uid + '''|clientUrl|''' + server_url + '''/|windowWidth|10|nxBaseUrl|userLanguage|fr|locale|1|2|3|4|1|5|6|6|7|8|7|9|7|10|7|11|7|12|7|13|7|14|-5|7|15|7|16|7|17|-10|''')
        fl.post(server_url + "/gwtcontainer", data,
                description="dashboard gwt container")
        fl.assert_('//OK' in fl.getBody())

        # Extract iframes from the gwtcontainer response
        iframes = extractIframes(fl.getBody())
        fl.assert_(len(iframes))

        i = 0
        for iframe in iframes:
            i += 1
            # print "iframe: " + iframe
            fl.get(server_url + iframe,
                     description="dashboard iframe %d" % i)
            fl.assert_(fl.getBody().startswith('<html>'))

        fl.get(server_url + "/opensocial/gadgets/makeRequest?refresh=3600&url=" + quote_plus(server_url) + "%2FrestAPI%2Fdashboard%2FUSER_SITES%3Fformat%3DJSON%26page%3D0%26domain%3Ddefault-domain%26lang%3Den%26ts%3D12766046361930.9475744903817575&httpMethod=GET&headers=Cache-control%3Dno-cache%252C%2520must-revalidate%26X-NUXEO-INTEGRATED-AUTH%3D" + jid + "&postData=&authz=&st=&contentType=JSON&numEntries=3&getSummaries=false&signOwner=true&signViewer=true&gadget=" + quote_plus(server_url) + "%2Fsite%2Fgadgets%2Fuserdocuments%2Fusersites.xml&container=default&bypassSpecCache=1&nocache=0",
            description="dashboard req1: user sites")
        fl.assert_('USER_SITES' in fl.getBody())

        fl.post(server_url + "/opensocial/gadgets/makeRequest", params=[
            ['authz', ''],
            ['signOwner', 'true'],
            ['contentType', 'JSON'],
            ['nocache', '0'],
            ['postData', ''],
            ['headers', 'Cache-control=no-cache%2C%20must-revalidate&X-NUXEO-INTEGRATED-AUTH=' + jid],
            ['url', server_url + '/restAPI/workflowTasks/default?mytasks=false&format=JSON&ts=' + ts + '&lang=en&labels=workflowDirectiveValidation,workflowDirectiveOpinion,workflowDirectiveVerification,workflowDirectiveCheck,workflowDirectiveDiffusion,label.workflow.task.name,label.workflow.task.duedate,label.workflow.task.directive'],
            ['numEntries', '3'],
            ['bypassSpecCache', '1'],
            ['st', ''],
            ['httpMethod', 'GET'],
            ['signViewer', 'true'],
            ['container', 'default'],
            ['getSummaries', 'false'],
            ['gadget', server_url + '/site/gadgets/waitingfor/waitingfor.xml']],
            description="dashboard req2: other tasks")
        fl.assert_('Tasks for' in fl.getBody())

        fl.get(server_url + "/opensocial/gadgets/makeRequest?refresh=3600&url=" + quote_plus(server_url) + "%2FrestAPI%2Fdashboard%2FUSER_WORKSPACES%3Fformat%3DJSON%26page%3D0%26domain%3Ddefault-domain%26lang%3Den%26ts%3D12766046364186.08350334148753&httpMethod=GET&headers=Cache-control%3Dno-cache%252C%2520must-revalidate%26X-NUXEO-INTEGRATED-AUTH%3DEB4D8F264629C549917996193637A4F4&postData=&authz=&st=&contentType=JSON&numEntries=3&getSummaries=false&signOwner=true&signViewer=true&gadget=" + quote_plus(server_url) + "%2Fsite%2Fgadgets%2Fuserworkspaces%2Fuserworkspaces.xml&container=default&bypassSpecCache=1&nocache=0",
            description="dashboard req3: user workspaces")
        fl.assert_('USER_WORKSPACES' in fl.getBody())

        fl.post(server_url + "/opensocial/gadgets/makeRequest", params=[
            ['authz', ''],
            ['signOwner', 'true'],
            ['contentType', 'JSON'],
            ['nocache', '0'],
            ['postData', ''],
            ['headers', 'Cache-control=no-cache%2C%20must-revalidate&X-NUXEO-INTEGRATED-AUTH=EB4D8F264629C549917996193637A4F4'],
            ['url', server_url + '/restAPI/workflowTasks/default?mytasks=true&format=JSON&ts=' + ts + '&lang=en&labels=workflowDirectiveValidation,workflowDirectiveOpinion,workflowDirectiveVerification,workflowDirectiveCheck,workflowDirectiveDiffusion,label.workflow.task.name,label.workflow.task.duedate,label.workflow.task.directive'],
            ['numEntries', '3'],
            ['bypassSpecCache', '1'],
            ['st', ''],
            ['httpMethod', 'GET'],
            ['signViewer', 'true'],
            ['container', 'default'],
            ['getSummaries', 'false'],
            ['gadget', server_url + '/site/gadgets/tasks/tasks.xml']],
            description="dashboard req4: my tasks")
        fl.assert_('Tasks for' in fl.getBody())

        fl.get(server_url + "/opensocial/gadgets/makeRequest?refresh=3600&url=" + quote_plus(server_url) + "%2FrestAPI%2Fdashboard%2FRELEVANT_DOCUMENTS%3Fformat%3DJSON%26page%3D0%26domain%3Ddefault-domain%26lang%3Den%26ts%3D12766046370131.186666326174645&httpMethod=GET&headers=Cache-control%3Dno-cache%252C%2520must-revalidate%26X-NUXEO-INTEGRATED-AUTH%3DEB4D8F264629C549917996193637A4F4&postData=&authz=&st=&contentType=JSON&numEntries=3&getSummaries=false&signOwner=true&signViewer=true&gadget=" + quote_plus(server_url) + "%2Fsite%2Fgadgets%2Fuserdocuments%2Fuserdocuments.xml&container=default&bypassSpecCache=1&nocache=0",
            description="dashboard req5: relevant docs")
        fl.assert_('RELEVANT_DOCUMENTS' in fl.getBody())
        return self

    def personalWorkspace(self):
        fl = self.fl
        fl.post(fl.server_url + "/casemanagement/cm_view.faces", params=[
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['userServicesForm_SUBMIT', '1'],
            ['userServicesForm:userServicesActionsTable:1:userServicesActionCommandLink', 'userServicesForm:userServicesActionsTable:1:userServicesActionCommandLink']],
            description="View personal workspace")
        # XXX: not working: post initializes personal workspace if it does
        # not exist...
        #self.viewDocumentPath("UserWorkspaces/" + fl.current_login,
        #                      description="View personal workspace")
        return self

    def search(self, query, description=None):
        fl = self.fl
        description = description and description or 'Search ' + query

        if '/search_results_simple.faces' in fl.getBody():
            action = '/search/search_results_simple.faces'
        else:
            action = '/view_documents.faces'
        fl.post(fl.server_url + action, params=[
            ['userServicesSearchForm:simpleSearchKeywordsInput', query],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['userServicesSearchForm:simpleSearchSubmitButton', 'Search'],
            ['userServicesSearchForm_SUBMIT', '1']],
            description=description)
        fl.assert_('simple_search' in fl.getBody(),
                     'Not a search result page')
        return self


    def edit(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='TAB_EDIT',
                                   description="View edit tab")
        self.fl.assert_('document_edit' in self.fl.getBody())
        return ret

    def files(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='TAB_FILES_EDIT',
                                   description="View files tab")
        self.fl.assert_('Upload your file' in self.fl.getBody())
        return ret

    def publish(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='TAB_PUBLISH',
                                   description="View publish tab")
        self.fl.assert_('Sections' in self.fl.getBody())
        return ret

    def publishOnFirstSection(self):
        """Publish in the first section"""
        fl = self.fl
        fl.post(fl.server_url + "/view_documents.faces", params=[
            ['publishTreeForm', 'publishTreeForm'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['publishTreeForm:publishTree:publishRecursiveAdaptor:0::publishTreeNodeAjaxExpanded', 'true'],
            ['publishTreeForm:publishTree:publishRecursiveAdaptor:0::publishTreeNodeNodeExpanded', 'true'],
            ['publishTreeForm:publishSelectTreeName', 'DefaultSectionsTree-default-domain'],
            ['AJAXREQUEST', '_viewRoot'],
            ['publishTreeForm:publishTree:input', ''],
            ['autoScroll', '']],
            description="Publish view section root")

        fl.post(fl.server_url + "/view_documents.faces", params=[
            ['publishTreeForm', 'publishTreeForm'],
            ['publishTreeForm:publishSelectTreeName', 'DefaultSectionsTree-default-domain'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['AJAXREQUEST', '_viewRoot'],
            ['publishTreeForm:publishTree:input', 'publishTreeForm:publishTree:publishRecursiveAdaptor:0:publishRecursiveAdaptor:1::publishTreeNode'],
            ['autoScroll', ''],
            ['publishTreeForm:publishTree:publishRecursiveAdaptor:0:publishRecursiveAdaptor:0::publishCommandLink', 'publishTreeForm:publishTree:publishRecursiveAdaptor:0:publishRecursiveAdaptor:0::publishCommandLink']],
            description="Publish document")
        fl.assert_("Unpublish" in fl.getBody())
        return self

    def relations(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='TAB_RELATIONS',
                                   description="View relations tab")
        self.fl.assert_('Add a new relation' in self.fl.getBody()
                        or 'No incoming or outgoing relation' in self.fl.getBody())
        return ret

    def workflow(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='TAB_CONTENT_JBPM',
                                   description="View workflow tab")
        self.fl.assert_('startWorkflow' in self.fl.getBody() or
                        'No workflows are currently active' in self.fl.getBody())
        return ret

    def mySubscriptions(self):
        ret = self.viewDocumentUid(self.getDocUid(),
                                   tab='TAB_MY_SUBSCRIPTIONS',
                                   description="View my subscriptions tab")
        self.fl.assert_('notifications' in self.fl.getBody())
        return ret

    def manageSubscriptions(self):
        """Only available for manager."""
        ret = self.viewDocumentUid(self.getDocUid(),
                                   tab='TAB_MANAGE_SUBSCRIPTIONS',
                                   description="View manage subscriptions tab")
        self.fl.assert_('add_subscriptions' in self.fl.getBody())
        return ret

    def comments(self):
        ret = self.viewDocumentUid(self.getDocUid(), tab='view_comments',
                                   description="View comments tab")
        self.fl.assert_('Add a comment' in self.fl.getBody())
        return ret

    def history(self):
        ret = self.viewDocumentUid(self.getDocUid(),
                                   tab='TAB_CONTENT_HISTORY',
                                   description="View history tab")
        self.fl.assert_('Event log' in self.fl.getBody())
        return ret

    def manage(self):
        ret = self.viewDocumentUid(self.getDocUid(),
                                   tab='TAB_MANAGE',
                                   description="View manage tab")
        return ret



class LoginPage(BasePage):
    """The Login page."""
    def view(self):
        fl = self.fl
        fl.get(fl.server_url + '/login.jsp',
               description='View Login page')
        fl.assert_('user_password' in fl.getBody())
        return self

class FolderPage(BasePage):
    """Folder page"""

    def createWorkspace(self, title, description):
        fl = self.fl

        fl.post(fl.server_url + "/view_documents.faces", params=[
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['documentActionSubviewUpperListForm_SUBMIT', '1'],
            ['documentActionSubviewUpperListForm:documentActionSubviewUpperListTable:0:documentActionSubviewUpperListLink', 'documentActionSubviewUpperListForm:documentActionSubviewUpperListTable:0:documentActionSubviewUpperListLink']],
            description="Create workspace form")
        fl.assert_('nxw_title' in fl.getBody(),
                   "Workspace creation form not found.")

        fl.post(fl.server_url + "/create_workspace.faces", params=[
            ['document_create', 'document_create'],
            ['document_create:nxl_heading:nxw_title', title],
            ['document_create:nxl_heading:nxw_description', description],
            ['document_create:button_create', 'Create'],
            ['document_create_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
                description="Create workspace submit")
        fl.assert_('Workspace saved' in fl.getBody())
        return self

    def createSection(self, title, description):
        fl = self.fl
        server_url = fl.server_url
        fl.post(server_url + "/view_documents.faces", params=[
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['documentActionSubviewUpperListForm_SUBMIT', '1'],
            ['documentActionSubviewUpperListForm:documentActionSubviewUpperListTable:0:documentActionSubviewUpperListLink', 'documentActionSubviewUpperListForm:documentActionSubviewUpperListTable:0:documentActionSubviewUpperListLink']],
            description="Create a section form")
        fl.assert_('nxw_title' in fl.getBody(),
                   "Workspace creation form not found.")
        
        fl.post(server_url + "/create_document.faces", params=[
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['document_create:nxl_heading:nxw_description', description],
            ['document_create:nxl_heading:nxw_title', title],
            ['document_create:button_create', 'Create'],
            ['document_create_SUBMIT', '1']],
            description="Create a section submit")
        fl.assert_('Section saved' in fl.getBody())
        return self
        

    def createFolder(self, title, description):
        fl = self.fl
        fl.post(fl.server_url + "/view_documents.faces", params=[
            ['selectDocumentTypeForCreationForm_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:1:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink', 'selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:1:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink'],
            ['selectDocumentTypeForCreationForm:selectDocTypePanelOpenedState', '']],
            description="Create folder: New Folder")

        fl.assert_('document_create' in fl.getBody(),
                   "Folder form not found")
        fl.post(fl.server_url + "/create_document.faces", params=[
            ['document_create', 'document_create'],
            ['document_create:nxl_heading:nxw_title', title],
            ['document_create:nxl_heading:nxw_description', description],
            #['parentDocumentPath', '/default-domain/workspaces/flnxtest-page-workspace.1237992970017'],
            ['document_create:button_create', 'Create'],
            ['document_create_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
            description="Create folder: Submit")
        fl.assert_('Folder saved' in fl.getBody())
        return self

    def createFile(self, title, description, file_path=None):
        fl = self.fl
        fl.post(fl.server_url + "/view_documents.faces", params=[
            ['selectDocumentTypeForCreationForm_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:0:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:1:selectDocumentTypeForCreationCategoryTitleLink', 'selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:0:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:1:selectDocumentTypeForCreationCategoryTitleLink'],
            ['selectDocumentTypeForCreationForm:selectDocTypePanelOpenedState', '']],
            description="Create file: New document")

        fl.assert_('document_create' in fl.getBody(),
                   "File form not found")
        fl.post(fl.server_url + "/create_document.faces", params=[
            ['document_create', 'document_create'],
            ['document_create:nxl_heading:nxw_title', title],
            ['document_create:nxl_heading:nxw_description', description],
            ['document_create:nxl_file:nxw_file:nxw_file_file:choice',
             file_path and 'upload' or 'none'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:upload',
             Upload(file_path or '')],
            ['document_create:button_create', 'Create'],
            ['document_create_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
            description="Create file: Sumbit")
        fl.assert_('File saved' in fl.getBody())
        return self

    def selectItem(self, title, item_type="Workspace"):
        fl = self.fl
        conversation_id = self.getConversationId()
        folder_uid = self.getDocUid()
        html = fl.getBody()
        if item_type in ['Section', 'SectionRoot']:
            start = html.find('form id="section_content"')
        else:
            start = html.find('form id="document_content"')
        end = html.find(title, start)
        fl.assert_(end > 0, 'Item with title "%s" not found.' % title)
        start = html.rfind('<tr class', start, end)

        # seam remoting selection is now done in ajax
#        doc_uid = extractToken(html[start:end], 'docRef:', '"')
#        fl.assert_(doc_uid, 'item "%s" not found.' % title)
#        sel = 'CURRENT_SELECTION'
#        if item_type == "Section":
#            sel = 'CURRENT_SELECTION_SECTIONS'
#        xml = '''<envelope><header><context><conversationId>%s</conversationId></context></header><body><call component="documentActions" method="checkCurrentDocAndProcessSelectRow" id="0">
#<params><param><str>%s</str></param><param><str>CURRENT_DOC_CHILDREN</str></param><param><str>%s</str></param><param><bool>true</bool></param><param><str>%s</str></param></params><refs></refs></call></body></envelope>''' % (
#            conversation_id, doc_uid, sel, folder_uid)
#        #print "%s" % xml
#        fl.post(fl.server_url + "/seam/resource/remoting/execute",
#                Data('application/xml; charset=UTF-8',
#                     xml),
#                description="Select document")
#        fl.assert_(sel + "_TRASH" in fl.getBody())

        checkbox_id = extractToken(html[start:end], 'input id="', '"')
        fl.assert_(checkbox_id, 'item "%s" not found.' % title)
        checkbox_ajax_onclick_id = checkbox_id + '_ajax_onclick'
        table_name = checkbox_id.split(':', 1)[0]

        params = [
            ['AJAXREQUEST', 'contentViewAjaxRegion_0'],
            [checkbox_id, 'on'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            [checkbox_ajax_onclick_id, checkbox_ajax_onclick_id],
            [table_name + '_SUBMIT', '1']
            ]
        fl.post(fl.server_url + "/view_documents.faces", params,
            description='Select document "%s"' % title)

        return self

    def deleteItem(self, title, item_type="Workspace"):
        fl = self.fl
        folder_uid = self.getDocUid()
        state = fl.getLastJsfState()
        self.selectItem(title, item_type)
        # position of the delete button
        table_name = "document_content"
        pos = '3'
        if item_type in ['Section', 'SectionRoot']:
            table_name = "section_content"
            pos = '0'
        params = [
            [table_name + ':nxl_document_listing_ajax:nxw_listing_ajax_selection_box_with_current_document', 'on'],
            ['javax.faces.ViewState', state],
            [table_name + ':clipboardActionsTable_0_0:' + pos + ':clipboardActionsButton', 'Delete'],
            [table_name + '_SUBMIT', '1']
            ]
        fl.post(fl.server_url + "/view_documents.faces", params,
            description='Delete document "%s"' % title)

        fl.assert_('Document(s) deleted' in fl.getBody())
        return self

    def view(self):
        """Default summary tab."""
        self.viewDocumentUid(self.getDocUid())
        return self

    def rights(self):
        """Go to rights tab."""
        self.viewDocumentUid(self.getDocUid(), "TAB_MANAGE", "TAB_RIGHTS")
        self.fl.assert_('Local rights' in self.fl.getBody())
        return self

    def grant(self, permission, user):
        """Grant perm to user."""
        fl = self.fl
        fl.assert_('Local rights' in fl.getBody(),
                   'Current page is not a rights tab.')
        server_url = fl.server_url
        params = [
            ['AJAXREQUEST', 'add_rights_form:nxl_user_group_suggestion:nxw_selection_ajax_region'],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggest', user],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox_selection', ''],
            ['add_rights_form:rights_grant_select', 'Grant'],
            ['add_rights_form:rights_permission_select', 'Everything'],
            ['add_rights_form_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['userSuggestionSearchType', ''],
            ['userSuggestionMaxSearchResults', '0'],
            ['ajaxSingle', 'add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox'],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox', 'add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox'],
            ['inputvalue', user],
            ['AJAX:EVENTS_COUNT', '1']]
        fl.post(server_url + "/view_documents.faces", params,
                  description="Grant perm search user.")
        fl.assert_(user in fl.getBody(), "User not found")

        state = fl.getLastJsfState()
        params = [
            ['AJAXREQUEST', 'add_rights_form:nxl_user_group_suggestion:nxw_selection_ajax_region'],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggest', user],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox_selection', '0'],
            ['add_rights_form:rights_grant_select', 'Grant'],
            ['add_rights_form:rights_permission_select', permission],
            ['add_rights_form_SUBMIT', '1'],
            ['javax.faces.ViewState', state],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox:nxw_selection_listRegion_select', 'add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox:nxw_selection_listRegion_select'],
            ['suggestionSelectionListId', 'nxw_selection_list'],
            ['suggestionInputSelectorId', 'nxw_selection_suggest']]
        fl.post(server_url + "/view_documents.faces", params,
                  description="Grant perm select user " + user)

        params = [
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggest', ''],
            ['add_rights_form:nxl_user_group_suggestion:nxw_selection_suggestionBox_selection', ''],
            ['add_rights_form:rights_grant_select', 'Grant'],
            ['add_rights_form:rights_permission_select', permission],
            ['add_rights_form:rights_add_button', 'Add permission'],
            ['add_rights_form_SUBMIT', '1'],
            ['javax.faces.ViewState', state]]
        fl.post(server_url + "/view_documents.faces", params,
                  description="Grant perm %s to %s" % (permission, user))
        fl.assert_('Save local rights' in fl.getBody())

        params = [
            ['validate_rights:document_rights_validate_button', 'Save local rights'],
            ['validate_rights_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]]
        fl.post(server_url + "/view_documents.faces", params,
                  description="Grant perm apply")
        fl.assert_('Rights updated' in fl.getBody())
        return self

    def sort(self, column):
        self.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mb_view_action_tab_form_SUBMIT', '1'],
            ['javax.faces.ViewState', 'j_id1'],
            ['mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link', 'mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link']],
            description="Post /nuxeo/casemanageme.../mailbox_view.faces")
        fl = self.fl
        server_url = fl.server_url
        fl.assert_('document_content' in fl.getBody(),
                   'Not a folder listing page.')

        options = {'date': ['document_content:nxl_document_listing_ajax:listing_modification_date_header_sort',
                            'document_content:nxl_document_listing_ajax:listing_modification_date_header_sort'],
                   'author': ['document_content:nxl_document_listing_ajax:listing_author_header_sort',
                              'document_content:nxl_document_listing_ajax:listing_author_header_sort'],
                   'lifecycle': ['document_content:nxl_document_listing_ajax:listing_lifecycle_header_sort',
                                 'document_content:nxl_document_listing_ajax:listing_lifecycle_header_sort'],
                   'title': ['document_content:nxl_document_listing_ajax:listing_title_link_header_sort',
                             'document_content:nxl_document_listing_ajax:listing_title_link_header_sort'],
                   }
        fl.assert_(column in options.keys(), 'Invalid sort column')
        # date
        fl.post(server_url + "/view_documents.faces", params=[
            ['document_content_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            options[column]],
            description="Sort by " + column)
        return self

    def viewRandomDocument(self, pattern):
        fl = self.fl
        hrefs = fl.listHref(content_pattern=pattern,
                            url_pattern='@view_documents')
        fl.assert_(len(hrefs), "No doc found with pattern: " + pattern)
        doc_url = random.choice(hrefs)
        fl.get(doc_url, description="View a random document")
        return DocumentPage(self.fl)
    
class RouteInstancePage(LoginPage):
    def login(self, user, password):
        BasePage.login(self, user, password)
        return RouteInstancePage(self.fl)
    
    def viewRouteInstance(self, routeInstance):
        fl = self.fl
        server_url = fl.server_url
        now = datetime.datetime.now()
        p = fl.get(server_url + "/nxpath/default/case-management/document-route-instances-root/" +  now.strftime("%Y/%m/%d") + "/" + quote(routeInstance) + "@view_documents?tabId=&conversationId=0NXMAIN",
            description="view route instance")
        fl.assert_("Participating documents" in p.body)
        return RouteInstancePage(self.fl)
        
        
    def getStepsDocsIds(self, stepsDocIds):
        fl = self.fl
        server_url = fl.server_url
        i = 1
        html = fl.getBody()
        if("This folder contains no document" in html):
            return stepsDocIds
        
        #get first step
        start = html.find("orderable_document_content:nxl_document_listing_ajax:nxw_listing_ajax_selection_box_with_current_document")
        end = html.find("orderable_document_content:nxl_document_listing_ajax:nxw_listing_modification_date", start)
        stepId = extractToken(html[start:end], 'docRef:', '"')
        if ("fork.png" in html[start:end]):
            #this is a fork
            p = self.viewDocumentUid(stepId)
            stepsInForkIds = []
            stepsInForkIds = self.getStepsDocsIds(stepsInForkIds)
            stepsDocIds.extend(stepsInForkIds)
        else:
            stepsDocIds.append(stepId)
        # get the ids for the others if any
        while "orderable_document_content:nxl_document_listing_ajax_" + str(i) + ":nxw_listing_ajax_selection_box_with_current_document_" + str(i) in html:
            start = html.find("orderable_document_content:nxl_document_listing_ajax_" + str(i) + ":nxw_listing_ajax_selection_box_with_current_document_")
            end = html.find("orderable_document_content:nxl_document_listing_ajax_" + str(i) + ":nxw_listing_modification_date_", start)
            stepId = extractToken(html[start:end], 'docRef:', '"')
            #test if is fork
            if ("fork.png" in html[start:end]):
                #this is a fork
                p = self.viewDocumentUid(stepId)
                stepsInForkIds = []
                stepsInForkIds = self.getStepsDocsIds(stepsInForkIds)
                stepsDocIds.extend(stepsInForkIds)
            else:
                stepsDocIds.append(stepId)
            i += 1
            
        return stepsDocIds
    
    def verifyRouteIsDone(self, routeInstance, case):
        fl = self.fl
        fl.assert_(case in fl.getBody())
        fl.assert_("draft" not in fl.getBody())
        fl.assert_("ready" not in fl.getBody())
        fl.assert_("running" not in fl.getBody())
        fl.assert_("done" in fl.getBody())
        return RouteInstancePage(self.fl)

class CaseItemPage(BasePage):
    def login(self, user, password):
        BasePage.login(self, user, password)
        return CaseItemPage(self.fl)

    def attachRouteAndStart(self, route, routeDocId):
        fl = self.fl
        server_url = fl.server_url
        fl.post(server_url + "/casemanagement/caseitem/view_cm_case.faces", params=[
            ['AJAXREQUEST', 'document_properties:nxl_summary_current_case_layout:nxl_document_related_route:a4j_route_region'],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggest', '(COP'],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox_selection', ''],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_routeId', ''],
            ['document_properties_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox', 'document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox'],
            ['ajaxSingle', 'document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox'],
            ['inputvalue', route],
            ['AJAX:EVENTS_COUNT', '1']],
            description="Post /nuxeo/casemanageme.../view_cm_case.faces")
        p = fl.post(server_url + "/casemanagement/caseitem/view_cm_case.faces", params=[
            ['AJAXREQUEST', 'document_properties:nxl_summary_current_case_layout:nxl_document_related_route:a4j_route_region'],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggest', ''],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox_selection', '0'],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_routeId', ''],
            ['document_properties_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['suggestionSelectionDeleteId', 'nxw_document_related_route_selection_reset'],
            ['suggestionSelectionHiddenId', 'nxw_document_related_route_routeId'],
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox:nxw_document_related_route_a4jSupport', 'document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_route_suggestionBox:nxw_document_related_route_a4jSupport'],
            ['suggestionSelectionOutputId', 'nxw_document_related_route_route']],
            description="attach route")
        fl.assert_(route in p.body)
        p = fl.post(server_url + "/casemanagement/caseitem/view_cm_case.faces", params=[
            ['document_properties:nxl_summary_current_case_layout:nxl_document_related_route:nxw_document_related_route_routeId', routeDocId],
            ['document_properties:nxl_summary_current_case_layout:start_route', 'Start'],
            ['document_properties_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
            description="start route")
        fl.assert_("A route is started from this document " in p.body)
        return CaseItemPage(self.fl)
    
    def downloadFile(self, caseItemDocId ,pdfFile):
        fl = self.fl
        server_url = fl.server_url
        fl.get(server_url + "/nxfile/default/" + caseItemDocId + "/file:content/" + pdfFile,
            description="download file")
        return CaseItemPage(self.fl)
    
    def approveTask(self, case, approveLink):
        fl = self.fl
        server_url = fl.server_url
        fl.assert_(case in fl.getBody())
        if("Approve" in fl.getBody()):
            fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mailbox_inbox_content_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            [approveLink, approveLink]],
            description="Approve task")
        fl.logi("Approving case :" +case)
        fl.assert_("an unexpected error occurred" not in fl.getBody())
        return CaseItemPage(self.fl)
    
    def approveAllTasks(self):
        fl = self.fl
        server_url = fl.server_url
        while ("Approve" in fl.getBody()):
            fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mailbox_inbox_content_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['mailbox_inbox_content:nxl_cm_inbox_caselink:nxw_cm_inbox_actionable_case_link_actions:caselink_approve', 'mailbox_inbox_content:nxl_cm_inbox_caselink:nxw_cm_inbox_actionable_case_link_actions:caselink_approve']],
            description="Approve task")
        fl.assert_("an unexpected error occurred" not in fl.getBody())
        return CaseItemPage(self.fl)

    # look for the case name in the page and click on the next page button if can't find it    
    def checkCaseInAllPages(self, case):
        fl = self.fl
        server_url = fl.server_url
        html = fl.getBody() 
        caseInPage = case in html
        if(not caseInPage):
            pattern = "src=\"/nuxeo/icons/action_page_next.gif\" name=\"mailbox_inbox_content:(j_id[0-9]+)\" alt=\"Next\""
            m = re.search(pattern, html)
            if(not m):
                errorMsg = "case" + case + " not in the page";
                fl.logi(errorMsg)
                fl.logi(html)
                fl.assertFail(caseInPage, errorMsg)
                return;                
            viewStatejidPattern = "<input type=\"hidden\" name=\"javax.faces.ViewState\" id=\"javax.faces.ViewState\" value=\"(j_id[0-9]+)\""
            viewStateIdMatcher = re.search(viewStatejidPattern, html)
            fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mailbox_inbox_content:'+m.group(1)+'.x', '12'],
            ['mailbox_inbox_content:'+m.group(1)+'.y', '11'],
            ['mailbox_inbox_content_SUBMIT', '1'],
            ['javax.faces.ViewState', viewStateIdMatcher.group(1)]],
            description="Case item next page")
            self.checkCaseInAllPages(case);

    def extractTaskApproveLink(self, case):
        fl = self.fl
        server_url = fl.server_url
        html = fl.getBody()
        self.checkCaseInAllPages(case)
        if("Approve" in html):
            start = html.find("title=" + case)
            end = html.find("document.getElementById('mailbox_inbox_content')", start)
            approveLink = extractToken(html[start:end], 'a id="', '"')
            return approveLink
      
    def viewRelatedStartedRoute(self, case):
      fl = self.fl
      server_url = fl.server_url
      now = datetime.datetime.now()
      fl.get(server_url + "/nxpath/default/case-management/case-root/" + now.strftime("%Y/%m/%d")+ "/" + quote(case) + "@view_cm_case?tabId=TAB_CASE_MANAGEMENT_VIEW_RELATED_ROUTE&conversationId=0NXMAIN",
            description="view related route started on this case")
      fl.assert_("This document is <span class=\"summary_unlocked\">unlocked" in fl.getBody())
      routeInstanceName = extractToken(fl.getBody(), server_url + '/nxpath/default/case-management/document-route-instances-root/' + now.strftime("%Y/%m/%d")+ "/" , '/')
      return routeInstanceName   
    
    def refreshRelatedStartedRoute(self, case):
      fl = self.fl
      server_url = fl.server_url
      now = datetime.datetime.now()
      fl.get(server_url + "/nxpath/default/case-management/case-root/" + now.strftime("%Y/%m/%d")+ "/" + quote(case) + "@view_cm_case?tabId=TAB_CASE_MANAGEMENT_VIEW_RELATED_ROUTE&conversationId=0NXMAIN",
            description="refresh view related route started on this case")
      return CaseItemPage(self)
  
    def viewDistributionTab(self):
      fl = self.fl
      server_url = fl.server_url
      now = datetime.datetime.now()
      p = fl.get(server_url + "/nxpath/default/case-management/case-root/" +  now.strftime("%Y/%m/%d") + "/" + quote(case) + "@view_cm_case?tabId=distribute_cm_case&conversationId=0NXMAIN",
            description="view case item")
      return CaseItemPage(self)
    
    def distributeCase(self):
      fl = self.fl
      server_url = fl.server_url  
        

class MailboxPage(FolderPage):
    def login(self, user, password):
        BasePage.login(self, user, password)
        return MailboxPage(self.fl)
    
    def viewManageTab(self):
       fl = self.fl
       server_url = fl.server_url
       params=[['mb_view_action_tab_form_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]]
       if ("Draft" not in fl.getBody()):
           params.append(['mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link', 'mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link'])
       else:
           params.append(['mb_view_action_tab_form:mb_view_action_tab_list:3:mb_view_action_tab_link', 'mb_view_action_tab_form:mb_view_action_tab_list:3:mb_view_action_tab_link'])         
       p = fl.post(server_url + "/casemanagement/cm_view.faces", params,
            description="view manage tab in the inbox")
       if("Incoming Case Item Management" not in p.body):
           logi(p.body)
       fl.assert_("Incoming Case Item Management" in p.body)
       return MailboxPage(self.fl)
    
    def viewInboxTab(self):
        fl = self.fl
        server_url = fl.server_url
        fl.assert_("Inbox" in fl.getBody())
        p = fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mb_view_action_tab_form_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['mb_view_action_tab_form:mb_view_action_tab_list:0:mb_view_action_tab_link', 'mb_view_action_tab_form:mb_view_action_tab_list:0:mb_view_action_tab_link']],
            description="view inbox tab")
        return MailboxPage(self.fl)
    
    def addIncomingCaseItemManagementProfile(self):
       fl = self.fl
       server_url = fl.server_url
       fl.assert_("Incoming Case Item Management" in fl.getBody())
       fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['document_edit:nxl_cm_mailbox:nxw_mailbox_profiles', 'cellule_courrier'],
            ['document_edit:update_mailbox', 'Save'],
            ['document_edit_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
            description="add incoming caseItem management profile")
       return MailboxPage(self.fl)

    def viewDraftTab(self):
        fl = self.fl
        server_url = fl.server_url
        fl.assert_("Draft" in fl.getBody())
        p = fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mb_view_action_tab_form_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link', 'mb_view_action_tab_form:mb_view_action_tab_list:2:mb_view_action_tab_link']],
            description="view draft tab")
        fl.assert_("New" in p.body)
        return MailboxPage(self.fl)
    
    def createCaseItem(self, case, caseitem, pathToPdf):
        fl = self.fl
        server_url = fl.server_url
        # Click on create new Case
        fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['selectDocumentTypeForCreationFormFormMailbox_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['selectDocumentTypeForCreationFormFormMailbox:selectDocumentTypeForCreationTable:1:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink', 'selectDocumentTypeForCreationFormFormMailbox:selectDocumentTypeForCreationTable:1:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink']],
            description="click on create new case")
        # Create a Case
        p = fl.post(server_url + "/casemanagement/case/create_empty_case.faces", params=[
            ['document_create:nxl_cm_case:nxw_title', case],
            ['document_create_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['document_create:emptyCaseCreateActionView:emptyCaseCreateActionList:0:caseActionUpperListLink', 'document_create:emptyCaseCreateActionView:emptyCaseCreateActionList:0:caseActionUpperListLink']],
            description="create new case")
        if("New" not in p.body):
            logi(p.body)
        fl.assert_("New" in p.body) 
        # Add a Case Item
        fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['caseView:selectDocumentTypeForCreationForm_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['caseView:selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:0:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink', 'caseView:selectDocumentTypeForCreationForm:selectDocumentTypeForCreationTable:0:selectDocumentTypeForCreationCategory:0:selectDocumentTypeForCreationCategoryTable:0:selectDocumentTypeForCreationCategoryTitleLink']],
            description="click to create a new case item")
        # Save the Case Item
        fl.post(server_url + "/casemanagement/caseitem/create_cm_document.faces", params=[
            ['document_create:nxl_cm_document:nxw_title', caseitem],
            ['document_create:nxl_cm_document:nxw_document_type', '23'],
            ['document_create:nxl_cm_document:nxw_confidentiality', '4'],
            ['document_create:nxl_cm_document:nxw_body', ''],
             ['document_create:nxl_file:nxw_file:nxw_file_file:choice', 'upload'],
            ['document_create:nxl_file:nxw_file:nxw_file_file:upload', Upload(pathToPdf)],
            ['document_create_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['document_create:caseCreateBottomActionView:caseCreateBottomActionList:0:caseActionUpperListLink', 'document_create:caseCreateBottomActionView:caseCreateBottomActionList:0:caseActionUpperListLink']],
            description="create new case item")
        fl.assert_("("+caseitem+" )" in fl.getBody())
        path = pathToPdf.split("/")
        pdf_name = path[len(path) - 1]
        fl.assert_("file:content/" + pdf_name in fl.getBody())
        ids = []
        caseItemId = extractToken(fl.getBody(), "nxfile/default/", "/")
        caseId = self.getDocUid()
        ids.append(caseId)
        ids.append(caseItemId)
        return ids
    
    def viewCaseItem(self,case, caseitem, caseItemId):
       fl = self.fl
       server_url = fl.server_url
       now = datetime.datetime.now()
       p = fl.get(server_url + "/nxpath/default/case-management/case-root/" +  now.strftime("%Y/%m/%d") + "/" + quote(case) + "@view_cm_case?subTabId=&tabId=TAB_CASE_MANAGEMENT_VIEW&conversationId=0NXMAIN&currentCaseItemId=" + caseItemId,
            description="view case item")
       fl.assert_("("+caseitem+" )" in fl.getBody())
       return CaseItemPage(self.fl)
   
    #change this to use the id of the case? 
    def viewCaseItemInDraft(self, caseitem):
       fl = self.fl
       server_url = fl.server_url
       fl.logi("View case item " + caseitem)
       fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mailbox_draft_content_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['mailbox_draft_content:nxl_cm_draft_caselink:nxw_cm_mailbox_draft_listing_title_link:link_title', 'mailbox_draft_content:nxl_cm_draft_caselink:nxw_cm_mailbox_draft_listing_title_link:link_title']],
            description="view case item in draft")
       fl.assert_("("+caseitem+" )" in fl.getBody())
       return CaseItemPage(self.fl)
   
    def clickCaseItem(self,case, caseitem, caseItemId):
       fl = self.fl
       server_url = fl.server_url
       now = datetime.datetime.now()
       fl.logi("Click on caseietm" + caseitem)
       fl.post(server_url + "/casemanagement/cm_view.faces", params=[
            ['mailbox_inbox_content_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['mailbox_inbox_content:nxl_cm_inbox_caselink:nxw_cm_mailbox_inbox_listing_title_link:case_link_title', 'mailbox_inbox_content:nxl_cm_inbox_caselink:nxw_cm_mailbox_inbox_listing_title_link:case_link_title']],
            description="click on caseItem")
       fl.assert_("("+caseitem+" )" in fl.getBody())
       return CaseItemPage(self.fl)


class RoutePage(BasePage):
    
    def login(self, user, password):
        BasePage.login(self, user, password)
        return RoutePage(self.fl)
    
    def viewRouteElementsTab(self, user, route):
        fl = self.fl
        server_url = fl.server_url
        route = quote(route)
        p = fl.get(server_url + "/nxpath/default/case-management/UserWorkspaces/" + user + "/" + route + "@view_documents?tabId=TAB_DOCUMENT_ROUTE_ELEMENTS&conversationId=0NXMAIN",
            description="view route elements tab")
        return RoutePage(self.fl)
    
    def viewRouteContentTab(self, user, route):
        fl = self.fl
        server_url = fl.server_url
        self.personalWorkspace().viewDocumentPath("UserWorkspaces/"+ user+ "/"+ route)
        return RoutePage(self.fl)
    
    def getRouteModelDocId(self, user, route):
        fl = self.fl
        server_url = fl.server_url
        self.personalWorkspace().viewDocumentPath("UserWorkspaces/"+ user+ "/"+ route)
        RoutePage.routeDocId = self.getDocUid()
        return RoutePage(self.fl)
    
    def lockRoute(self, user, route):
        fl = self.fl
        server_url = fl.server_url
        route = quote(route)
        if ("This document is <span class=\"summary_locked\">locked</span>" not in fl.getBody()):
            p = fl.post(server_url + "/casemanagement/caseitem/view_cm_case.faces", params=[
            ['related_route_elements_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['related_route_elements:nxl_document_routing_route_content:lock_route', 'related_route_elements:nxl_document_routing_route_content:lock_route']],
            description="lock route")
        fl.assert_("an unexpected error occurred" not in fl.getBody())
        fl.assert_("This document is <span class=\"summary_locked\">locked</span>" in fl.getBody())
        fl.logi("Route " + route + "is  locked by" + user)
        return RoutePage(self.fl)
    
    def stepCanBeUpdated(self, stepId, routeInstance, user):
        fl = self.fl
        server_url = fl.server_url
        html = fl.getBody()
        if("an unexpected error occurred" in html):
         fl.logi("Route " + routeInstance + "is not locked!! by" + user)
        fl.assert_("an unexpected error occurred" not in html)
        if ("This document is <span class=\"summary_locked\">locked</span>" not in html):
            fl.logi(src(html))
        fl.assert_("This document is <span class=\"summary_locked\">locked</span>" in html)
        start = html.find("docRef=\"" + stepId)
        end = html.find("</tr>", start)
        if ("title=\"ready\"" in html[start:end]):
            return True
        return False
    
    def stepNeedsToBeApproved(self, stepId, routeInstance, user):
        fl = self.fl
        server_url = fl.server_url
        html = fl.getBody()
        if("an unexpected error occurred" in html):
         fl.logi("Route " + routeInstance + "is not locked!! by" + user)
        fl.assert_("an unexpected error occurred" not in html)
        fl.assert_("This document is <span class=\"summary_locked\">locked</span>" in html)
        start = html.find("docRef=\"" + stepId)
        end = html.find("</tr>", start)
        if ("step.png" in html[start:end]):
            return False
        return True
    
    def updateStepDistributionMailboxFromRouteView(self, stepId, distributionMailbox , stepRang):
        fl = self.fl
        server_url = fl.server_url
        fl.post(server_url + "/casemanagement/caseitem/view_cm_case.faces", params=[
            ['related_route_elements_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()],
            ['related_route_elements:nxl_document_routing_route_content_'+ str(stepRang) + ':nxw_dr_listing_step_actions_'+ str(stepRang) +':nxw_dr_listing_step_actions_'+ str(stepRang) +'_edit:editStepListTable:2:documentActionSubviewUpperListLink', 
             'related_route_elements:nxl_document_routing_route_content_'+ str(stepRang) +':nxw_dr_listing_step_actions_'+ str(stepRang) +':nxw_dr_listing_step_actions_' + str(stepRang) +'_edit:editStepListTable:2:documentActionSubviewUpperListLink'],
            ['stepId', stepId]],
            description="click on Modify step")
        p = fl.getBody()
        params = [['edit_step:update_step', 'Save'],
                        ['edit_step_SUBMIT', '1']]
        # request depending on the step type
        if ("nxl_all_mailboxes_routing_task" in p):
            params.append(['edit_step:nxl_all_mailboxes_routing_task:nxw_distribution_mailbox_mailboxId', distributionMailbox])
        elif("nxl_generic_mailboxes_routing_task" in p):
            params.append(['edit_step:nxl_generic_mailboxes_routing_task:nxw_distribution_mailbox_mailboxId', distributionMailbox])
        elif("nxl_personal_mailboxes_routing_task" in p):
            params.append(['edit_step:nxl_personal_mailboxes_routing_task:nxw_distribution_mailbox_mailboxId', distributionMailbox])
        if("edit_step:nxl_routing_task:nxw_type" in p):
            params.append(['edit_step:nxl_routing_task:nxw_type', '1'])
        params.append(['javax.faces.ViewState', fl.getLastJsfState()])
        fl.post(server_url + "/edit_route_element.faces", params,
                        description="save edited step")
        return RoutePage(fl)

    def validateRouteModel(self):
        fl = self.fl
        server_url = fl.server_url
        fl.assert_("Validate model" in fl.getBody())
        fl.post(server_url + "/view_documents.faces", params=[
            ['document_view:nxl_summary_document_route_layout:validate_route_model', 'Validate model'],
            ['document_view_SUBMIT', '1'],
            ['javax.faces.ViewState', fl.getLastJsfState()]],
            description="validate route model")
        fl.assert_("Validate model" not in fl.getBody())
        RoutePage.routeDocId = self.getDocUid()
        return RoutePage(fl)
    
    def routeModelValidated(self):
        fl = self.fl
        server_url = fl.server_url
        RoutePage.routeDocId = self.getDocUid()
        #if the route is not already validated
        if("validated" in fl.getBody() and "Validate model" not in fl.getBody()):
            return True
        else:
            fl.assert_("draft" in fl.getBody())
            return False
    #FIXME
    def getMailboxListForSteps(self):
        fl = self.fl
        server_url = fl.server_url
        fl.assert_("This document is <span class=\"summary_unlocked\">unlocked </span>" in fl.getBody())
        mailboxList = []
        html = fl.getBody()
        while "title=\"validated\"" in html:
            start = html.find("title=\"validated\"")
            end = html.find("</tr>", start)
            userMailbox =  extractToken(html[start:end], '<td class="  routeRowBackground\'*\'">', '<')
            i = userMailbox.find("routeRowBackground")
            userMailbox = userMailbox[i+ len("routeRowBackground")+3 + len("user-"):]
            html = html[end:]
            mailboxList.append(userMailbox)
        return mailboxList
        
