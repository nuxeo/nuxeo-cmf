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

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.nuxeo.functionaltests.AbstractTest;
import org.nuxeo.functionaltests.pages.DocumentBasePage;
import org.nuxeo.functionaltests.pages.DocumentBasePage.UserNotConnectedException;
import org.nuxeo.functionaltests.pages.LoginPage;

@RunWith(ConcordionRunner.class)
public class LoginOk extends AbstractTest {

    LoginPage loginPage;

    DocumentBasePage documentBasePage;

    String username;

    public String connectToLoginPage(String nuxeourl) {
        loginPage = get(nuxeourl, LoginPage.class);

        if (loginPage != null) {
            return "connects";
        }
        return "doesn't connect";
    }

    public String login(String username, String password, String language) {
        loginPage.login(username, password, language);
        documentBasePage = asPage(DocumentBasePage.class);
        this.username = username;
        return "login";
    }

    public String isConnected() {
        try {
            documentBasePage.checkUserConnected(username);
        } catch (UserNotConnectedException e) {
            return "not connected";
        }
        return "connected";
    }

}
