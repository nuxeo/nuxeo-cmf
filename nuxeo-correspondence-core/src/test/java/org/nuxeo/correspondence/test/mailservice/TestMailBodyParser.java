/*
 * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */

package org.nuxeo.correspondence.test.mailservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.nuxeo.cm.contact.Contact;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.mail.actionpipe.MailActionPipeConstants;
import org.nuxeo.cm.mail.actionpipe.ParseMailBody;

/**
 * @author Anahide Tchertchian
 */
public class TestMailBodyParser {

    public void matchContact(String expectedName, String expectedEmail,
            Contact actual) throws Exception {
        assertEquals("Actual name is", expectedName, actual.getName());
        assertEquals("Actual email is", expectedEmail, actual.getEmail());
    }

    /**
     * Check the correctness of the parsing of the content
     *
     * @param content
     * @throws Exception
     */
    private void emailEnglishAssertions(String content) throws Exception {
        Map<String, Object> context = new HashMap<>();

        ParseMailBody.parse(content, context);
        assertFalse(context.isEmpty());

        String fromName = "Anahide TCHERTCHIAN";
        String fromEmail = "at@nuxeo.com";
        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(0);
        date.set(2009, 0, 14, 15, 15, 25);
        String to = "arussel@nuxeo.com";

        Contacts actualSenders = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_SENDERS_KEY);
        assertEquals(1, actualSenders.size());
        matchContact(fromName, fromEmail, actualSenders.get(0));
        assertEquals(
                date.getTime(),
                ((Calendar) context.get(MailActionPipeConstants.ORIGINAL_RECEPTION_DATE_KEY)).getTime());
        Contacts actualTo = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_TO_RECIPIENTS_KEY);
        assertEquals(1, actualTo.size());
        matchContact("", to, actualTo.get(0));
        Contacts actualCc = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_CC_RECIPIENTS_KEY);
        assertEquals(0, actualCc.size());
    }

    /**
     * Check the correctness of the parsing of the content
     *
     * @param content
     * @throws Exception
     */
    private void emailEnglishWithCcAssertions(String content) throws Exception {
        Map<String, Object> context = new HashMap<>();

        ParseMailBody.parse(content, context);
        assertFalse(context.isEmpty());

        String fromName = "Anahide TCHERTCHIAN";
        String fromEmail = "at@nuxeo.com";
        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(0);
        date.set(2009, 0, 14, 15, 15, 25);
        String to = "arussel@nuxeo.com";

        Contacts actualSenders = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_SENDERS_KEY);
        assertEquals(1, actualSenders.size());
        matchContact(fromName, fromEmail, actualSenders.get(0));
        assertEquals(
                date.getTime(),
                ((Calendar) context.get(MailActionPipeConstants.ORIGINAL_RECEPTION_DATE_KEY)).getTime());
        Contacts actualTo = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_TO_RECIPIENTS_KEY);
        assertEquals(2, actualTo.size());
        matchContact("", to, actualTo.get(0));
        matchContact("Jean Dupuis", "jean.dupuis@mail.fr", actualTo.get(1));

        Contacts actualCc = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_CC_RECIPIENTS_KEY);
        assertEquals(2, actualCc.size());
        matchContact("Sebastien Blanc", "s.b@mail.fr", actualCc.get(0));
        matchContact("benoit perrier", "b.l@mail.fr", actualCc.get(1));
    }

    /**
     * Test contents with the same header but with different bodies, that must
     * give the same result
     *
     * @throws Exception
     */
    @Test
    public void testEmailEnglish() throws Exception {
        // Body content part from email forwarded through Thunderbird mailer
        String content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com>\n\n\nObjet : [correspondence] courriel test pour fonctionnalit\u00e9 \n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\nAssistante Commerciale\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishAssertions(content);

        // Body content part from email forwarded twice : french then english
        content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com>\n\n----French header----\n\n\nDe : Alain Escaffre [mailto:aescaffre@nuxeo.com]\nEnvoy\u00e9 : lundi 19 mai 2008 09:06\n\u00c0 : doguin laurent\nCc : Anahide Tchertchian; Oriane TIAN; Alain Escaffre\nObjet : [correspondence] courriel test pour fonctionnalit\u00e9 \n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\nSales Assistant\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishAssertions(content);

        // Body content part from email forwarded twice : english then english
        content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com>\n\n----English header----Subject: \tRENOUVELLEMENT2 DE SUPPORT ANNUEL\nDate: \tWed, 10 Jan 2009 15:15:25 +0100\nFrom: \t2Anahide TCHERTCHIAN <2at@nuxeo.com>\nTo: \t<2arussel@nuxeo.com>\n\n\n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\nAssistante Commerciale\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishAssertions(content);
    }

    /**
     * Test the parsing of an english body containing various "TO" and "CC"
     *
     * @throws Exception
     */
    @Test
    public void testEmailEnglishWithCc() throws Exception {
        // Another body content with two "TO" and two "CC"
        String content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com> ; Jean Dupuis <jean.dupuis@mail.fr>\nCc: \tSebastien Blanc <s.b@mail.fr>; benoit perrier <b.l@mail.fr>\n\n\n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\n\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishWithCcAssertions(content);

        // Body content part from email forwarded twice : french then english
        content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com> ; Jean Dupuis <jean.dupuis@mail.fr>\nCc: \tSebastien Blanc <s.b@mail.fr>; benoit perrier <b.l@mail.fr>\n\n----French header----\n\n\nDe : Alain Escaffre [mailto:aescaffre@nuxeo.com]\nEnvoy\u00e9 : lundi 19 mai 2008 09:06\n\u00c0 : doguin laurent\nCc : Anahide Tchertchian; Oriane TIAN; Alain Escaffre\nObjet : [correspondence] courriel test pour fonctionnalit\u00e9 \n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\n\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishWithCcAssertions(content);

        // Body content part from email forwarded twice : english then english
        content = "\n\n-------- Original Message --------\nSubject: \tRENOUVELLEMENT DE SUPPORT ANNUEL\nDate: \tWed, 14 Jan 2009 15:15:25 +0100\nFrom: \tAnahide TCHERTCHIAN <at@nuxeo.com>\nTo: \t<arussel@nuxeo.com> ; Jean Dupuis <jean.dupuis@mail.fr>\nCc: \tSebastien Blanc <s.b@mail.fr>; benoit perrier <b.l@mail.fr>\n\n----English header----Subject: \tRENOUVELLEMENT2 DE SUPPORT ANNUEL\nDate: \tWed, 10 Jan 2009 15:15:25 +0100\nFrom: \t2Anahide TCHERTCHIAN <2at@nuxeo.com>\nTo: \t<2arussel@nuxeo.com>\n\n\n\nBonjour,\n\nVeuillez trouver ci-joint un devis pour le renouvellement de votre support\nannuel pour la p�riode du 26/02/09 AU 26/02/10.\n\n\nMerci de bien vouloir nous faire parvenir un bon de commande.\n\nNous restons � votre disposition pour tout compl�ment d'information,\n\n\nBien cordialement,\n\nAnahide TCHERTCHIAN\n\ne-mail : at@nuxeo.com\n\n\n";
        emailEnglishWithCcAssertions(content);

    }

    @Test
    public void testBodyParser() throws Exception {
        Map<String, Object> context = new HashMap<>();
        String content = "\n________________________________\n\nDe : Alain Escaffre [mailto:aescaffre@nuxeo.com]\nEnvoy\u00e9 : lundi 19 mai 2008 09:06\n\u00c0 : doguin laurent\nCc : Anahide Tchertchian; Oriane TIAN; Alain Escaffre\nObjet : [correspondence] courriel test pour fonctionnalit\u00e9 \"transfert de courriel vers correspondence\"\n\nCeci est un courriel de test";

        ParseMailBody.parse(content, context);
        assertFalse(context.isEmpty());

        String fromName = "Alain Escaffre";
        String fromEmail = "aescaffre@nuxeo.com";
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(0);
        date.set(2008, 4, 19, 9, 6);

        String to = "doguin laurent";
        String cc1 = "Anahide Tchertchian";
        String cc2 = "Oriane TIAN";
        String cc3 = "Alain Escaffre";

        Contacts actualSenders = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_SENDERS_KEY);
        assertEquals(1, actualSenders.size());
        matchContact(fromName, fromEmail, actualSenders.get(0));
        assertEquals(
                date.getTime(),
                ((Calendar) context.get(MailActionPipeConstants.ORIGINAL_RECEPTION_DATE_KEY)).getTime());
        Contacts actualTo = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_TO_RECIPIENTS_KEY);
        assertEquals(1, actualTo.size());
        matchContact(to, null, actualTo.get(0));
        Contacts actualCc = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_CC_RECIPIENTS_KEY);
        assertEquals(3, actualCc.size());
        matchContact(cc1, null, actualCc.get(0));
        matchContact(cc2, null, actualCc.get(1));
        matchContact(cc3, null, actualCc.get(2));

        // again without copy and not "mailto" from
        context = new HashMap<>();
        content = "\n________________________________\n\nDe : Alain Escaffre\nEnvoy\u00e9 : lundi 19 mai 2008 09:06\n\u00c0 : doguin laurent\nObjet : [correspondence] courriel test pour fonctionnalit\u00e9 \"transfert de courriel vers correspondence\"\n\nCeci est un courriel de test";
        ParseMailBody.parse(content, context);
        assertFalse(context.isEmpty());

        actualSenders = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_SENDERS_KEY);
        assertEquals(1, actualSenders.size());
        matchContact(fromName, null, actualSenders.get(0));
        assertEquals(
                date.getTime(),
                ((Calendar) context.get(MailActionPipeConstants.ORIGINAL_RECEPTION_DATE_KEY)).getTime());
        actualTo = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_TO_RECIPIENTS_KEY);
        assertEquals(1, actualTo.size());
        matchContact(to, null, actualTo.get(0));
        actualCc = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_CC_RECIPIENTS_KEY);
        assertNull(actualCc);

        // again without action or copy
        context = new HashMap<>();
        content = "\n________________________________\n\nDe : GUITTER Solen [mailto:solen.guitter@nuxeo.com]\nEnvoy\u00e9 : jeudi 4 f\u00e9vrier 2008 16:46\nObjet : Tirez pleinement parti du format XML !";
        ParseMailBody.parse(content, context);
        assertFalse(context.isEmpty());

        actualSenders = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_SENDERS_KEY);
        assertEquals(1, actualSenders.size());
        matchContact("GUITTER Solen", "solen.guitter@nuxeo.com",
                actualSenders.get(0));
        date = Calendar.getInstance();
        date.setTimeInMillis(0);
        date.set(2008, 1, 4, 16, 46);
        assertEquals(
                date.getTime(),
                ((Calendar) context.get(MailActionPipeConstants.ORIGINAL_RECEPTION_DATE_KEY)).getTime());
        actualTo = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_TO_RECIPIENTS_KEY);
        assertNull(actualTo);
        actualCc = (Contacts) context.get(MailActionPipeConstants.ORIGINAL_CC_RECIPIENTS_KEY);
        assertNull(actualCc);

    }
}
