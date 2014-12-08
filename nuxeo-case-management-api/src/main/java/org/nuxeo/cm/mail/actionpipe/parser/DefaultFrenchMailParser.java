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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.contact.Contact;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.mail.actionpipe.MailActionPipeConstants;

/**
 * @author Sun Seng David TAN <stan@nuxeo.com>
 */
public class DefaultFrenchMailParser implements MailBodyParser, MailActionPipeConstants {

    public static final Log log = LogFactory.getLog(DefaultFrenchMailParser.class);

    private static final String FRENCH_DATE_FORMAT = "EEEE dd MMM yyyy HH:mm";

    public static final String EMAIL_MATCH = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";

    public static final Pattern CONTACT_PATTERN = Pattern.compile("(.*)(\\[mailto:)(.*?)(\\])(.*)");

    // PLE-680: marker to get rid of " > " ligns

    public static final Pattern FRENCH_HEADER_PATTERN = Pattern.compile("(.*?)" + "(De :)(.*?)" + "(Envoy\u00e9 :)"
            + "((.*?)(\u00c0 :))?" + "((.*?)(Cc :))?(.*?)" + "(Objet :)(.*?)", Pattern.DOTALL);

    public static final String TRANSFER_MARKER = " > ";

    /*
     * (non-Javadoc)
     * @see org.nuxeo.cm.mail.actionpipe.parser.MailBodyParser#getRegexp()
     */
    @Override
    public Pattern getHeaderPattern() {
        return FRENCH_HEADER_PATTERN;
    }

    /*
     * (non-Javadoc)
     * @see org.nuxeo.cm.mail.actionpipe.parser.MailBodyParser#parse(java.util.regex .Matcher, java.util.Map)
     */
    @Override
    public boolean parse(Matcher m, Map<String, Object> resultMap) {
        // for debug
        for (int i = 1; i < m.groupCount() + 1; i++) {
            log.debug(i + ": " + m.group(i));
        }

        // 1: ________________________________
        // 2: De :
        // 3: Alain Escaffre [mailto:aescaffre@nuxeo.com]
        // 4: Envoyé :
        // 5: lundi 19 mai 2008 09:06 À :
        // 6: lundi 19 mai 2008 09:06
        // 7: À :
        // 8: doguin laurent Cc :
        // 9: doguin laurent
        // 10: Cc :
        // 11: Anahide Tchertchian; Oriane TIAN; Alain Escaffre
        // 12: Objet :
        // 13: [casemanagement] courriel test pour fonctionnalité "transfert de
        // courriel vers casemanagement" Ceci est un courriel de test

        Contacts origSenders = parseContacts(m.group(3));
        if (origSenders != null && !origSenders.isEmpty()) {
            // fill the sender name key
            String origSenderName = origSenders.get(0).getName();
            resultMap.put(ORIGINAL_SENDER_NAME_KEY, origSenderName);
        }
        resultMap.put(ORIGINAL_SENDERS_KEY, origSenders);
        if (m.group(6) != null) {
            resultMap.put(ORIGINAL_RECEPTION_DATE_KEY, parseDate(m.group(6)));
            if (m.group(8) != null) {
                resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY, parseContacts(m.group(9)));
                resultMap.put(ORIGINAL_CC_RECIPIENTS_KEY, parseContacts(m.group(11)));
            } else {
                // no Cc
                resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY, parseContacts(m.group(11)));
            }
        } else {
            // no To, assume no Cc neither
            resultMap.put(ORIGINAL_RECEPTION_DATE_KEY, parseDate(m.group(11)));
        }

        return true;
    }

    public static Contacts parseContacts(String contacts) {
        log.debug(String.format("Parsing contacts '%s'", contacts));
        if (contacts != null && contacts.length() > 0) {
            if (contacts.endsWith(TRANSFER_MARKER)) {
                contacts = contacts.substring(0, contacts.length() - TRANSFER_MARKER.length());
            }

            String[] split = contacts.trim().split("; ");
            Contacts res = new Contacts();
            for (String contact : split) {
                Matcher m = CONTACT_PATTERN.matcher(contact);
                Contact item = new Contact();
                if (m.matches()) {
                    // for debug
                    // for (int i = 1; i < m.groupCount() + 1; i++) {
                    // log.debug(i + ": " + m.group(i));
                    // }

                    // 1: Alain Escaffre
                    // 2: [mailto:
                    // 3: aescaffre@nuxeo.com
                    // 4: ]
                    // 5:

                    item.setName(m.group(1).trim());
                    item.setEmail(m.group(3).trim());
                } else {
                    if (contact.matches(EMAIL_MATCH)) {
                        item.setEmail(contact.trim());
                    } else {
                        item.setName(contact.trim());
                    }
                }
                res.add(item);
            }
            return res;
        }
        return null;
    }

    public static Calendar parseDate(String dateString) {
        try {
            log.debug(String.format("Parsing date '%s'", dateString));

            SimpleDateFormat sdf = new SimpleDateFormat(FRENCH_DATE_FORMAT, Locale.FRENCH);
            Date date = sdf.parse(dateString.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        } catch (ParseException e) {
            log.error("Parsing date failed : " + dateString, e);
            return null;
        }
    }

}
