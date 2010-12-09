/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
package org.nuxeo.cm.mail.actionpipe.parser;

import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.cm.contact.Contacts;

/**
 * Various unit test of {@link DefaultEnglishMailParser}
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 *
 */
public class TestDefaultEnglishMailParser {
    Pattern pattern;

    DefaultEnglishMailParser bodyParser;

    @Before
    public void setUp() {
        bodyParser = new DefaultEnglishMailParser();
        pattern = bodyParser.getContactPattern();
    }

    /**
     * Testing the parsing of contacts with the default contact pattern that
     * should be the Thunderbird one.
     */
    @Test
    public void testParseContacts() {

        String fullContact = "\t Sun Seng David Tan <stan@nuxeo.com> test";
        Contacts contacts = bodyParser.parseContacts(fullContact);
        Assert.assertEquals(1, contacts.size());
        Assert.assertEquals("Sun Seng David Tan", contacts.get(0).getName());
        Assert.assertEquals("stan@nuxeo.com", contacts.get(0).getEmail());

        String fullContact2 = "\t \"Sun Seng David Tan (sunix)\" <stan@nuxeo.com> test";
        contacts = bodyParser.parseContacts(fullContact2);
        Assert.assertEquals(1, contacts.size());
        Assert.assertEquals("Sun Seng David Tan (sunix)",
                contacts.get(0).getName());
        Assert.assertEquals("stan@nuxeo.com", contacts.get(0).getEmail());

        String simpleContact = "\t stan@nuxeo.com >test >test";
        contacts = bodyParser.parseContacts(simpleContact);
        Assert.assertEquals(1, contacts.size());
        Assert.assertTrue("".equals(contacts.get(0).getName())
                || contacts.get(0).getName() == null);
        Assert.assertEquals("stan@nuxeo.com", contacts.get(0).getEmail());

        String multipleSimpleContacts = "\tsun@sue-fr.org, sxzhang@ead.naixtis.com, ehimarka@yaheo.fr decoooo>qsdfqsdf>";
        contacts = bodyParser.parseContacts(multipleSimpleContacts);
        Assert.assertEquals(3, contacts.size());
        Assert.assertTrue("".equals(contacts.get(0).getName())
                || contacts.get(0).getName() == null);
        Assert.assertEquals("sun@sue-fr.org", contacts.get(0).getEmail());
        Assert.assertTrue("".equals(contacts.get(1).getName())
                || contacts.get(1).getName() == null);
        Assert.assertEquals("sxzhang@ead.naixtis.com",
                contacts.get(1).getEmail());
        Assert.assertTrue("".equals(contacts.get(2).getName())
                || contacts.get(2).getName() == null);
        Assert.assertEquals("ehimarka@yaheo.fr", contacts.get(2).getEmail());

        String multipleFullContacts = "\tJean-David TANG <jdtang@gmail.com>, \"Sun Seng David Tan (sunix)\" <sun.seng.david.tan@nuxeo.com>,  Louisa Cha <cha_louisa@yahoo.fr> qsdfqsdf < mlkjlmj > mlkj";
        contacts = bodyParser.parseContacts(multipleFullContacts);
        Assert.assertEquals(3, contacts.size());
        Assert.assertEquals("Jean-David TANG", contacts.get(0).getName());
        Assert.assertEquals("jdtang@gmail.com", contacts.get(0).getEmail());
        Assert.assertEquals("Sun Seng David Tan (sunix)",
                contacts.get(1).getName());
        Assert.assertEquals("sun.seng.david.tan@nuxeo.com",
                contacts.get(1).getEmail());
        Assert.assertEquals("Louisa Cha", contacts.get(2).getName());
        Assert.assertEquals("cha_louisa@yahoo.fr", contacts.get(2).getEmail());

        String mixedContacts = "prase.jesuth@sgs.socgene.com,  Anne so Paier <annesopaier@yahoo.fr>, jubaien@hotmail.com, \"Guillaume(chacha) Chateau\" <guillaume.chateaur@eeee.fr> >mlkjmlj sdf> qsdfq>";
        contacts = bodyParser.parseContacts(mixedContacts);
        Assert.assertEquals(4, contacts.size());
        Assert.assertTrue("".equals(contacts.get(0).getName())
                || contacts.get(0).getName() == null);
        Assert.assertEquals("prase.jesuth@sgs.socgene.com",
                contacts.get(0).getEmail());
        Assert.assertEquals("Anne so Paier", contacts.get(1).getName());
        Assert.assertEquals("annesopaier@yahoo.fr", contacts.get(1).getEmail());
        Assert.assertTrue("".equals(contacts.get(2).getName())
                || contacts.get(2).getName() == null);
        Assert.assertEquals("jubaien@hotmail.com", contacts.get(2).getEmail());
        Assert.assertEquals("Guillaume(chacha) Chateau",
                contacts.get(3).getName());
        Assert.assertEquals("guillaume.chateaur@eeee.fr",
                contacts.get(3).getEmail());

    }

}
