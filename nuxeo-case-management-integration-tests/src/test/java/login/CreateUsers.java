/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Sun Seng David TAN <stan@nuxeo.com>
 */
package login;

import static org.junit.Assert.assertEquals;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.nuxeo.functionaltests.pages.UsersGroupsBasePage;
import org.nuxeo.functionaltests.pages.tabs.UsersTabSubPage;

@RunWith(ConcordionRunner.class)
public class CreateUsers extends LoginOk {

    /**
     * <ul>
     * <li>Creation of each user is done on the users & groups tab</li>
     * <li>For each user, we check that the user is not already by typing its
     * name and searching</li>
     * <li>Then we add the user by clicking on Create a new user with the
     * following informations:</li>
     * </ul>
     *
     * @throws ElementNotFoundException
     *
     */
    public String createUser(String username, String firstname,
            String lastname, String company, String email, String password,
            String group) {
        UsersTabSubPage usersTab = documentBasePage.getHeaderLinks().goToUserManagementPage().getUsersTab();
        usersTab = usersTab.searchUser(username);
        if (usersTab.isUserFound(username)) {
            return "Created";
        }

        UsersGroupsBasePage page = usersTab.getUserCreatePage().createUser(
                username, firstname, lastname, company, email, password, group);
        assertEquals(page.getFeedbackMessage(), "User created");
        usersTab = page.getUsersTab(true);

        if (usersTab.isUserFound(username)) {
            return "Created";
        }
        return "Not Created";
    }

}
