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

import java.text.DateFormat;
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
 * This is the default English Mail Parser which is from a forwarded mail using
 * English Thunderbird.
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 *
 */
public class DefaultEnglishMailParser implements MailBodyParser,
        MailActionPipeConstants {

    public static final Log log = LogFactory.getLog(DefaultEnglishMailParser.class);

    public static final Pattern DEFAULT_CONTACT_PATTERN = Pattern.compile("\\s*\"?"
            + "([^@<>\",]*?)" // the name
            + "\"?\\s*(<?)" + "([^\"@<> ,]+@.+\\.[a-z]+)" // the email
            + "(>?).*?");

    public static final Pattern THUNDERBIRD_ENGLISH_HEADER_PATTERN = Pattern.compile(
            "(.*?)Original Message(.*?)" + "(Subject:)([^\r\n]+)[\r\n\\s]+"
                    + "(Date:)([^\r\n]+)[\r\n\\s]+"
                    + "(From:)([^\r\n]+)[\r\n\\s]+"
                    + "(To:)([^\r\n]+)[\r\n\\s]+"
                    + "((Cc:)([^\r\n]+))?[\r\n\\s]+.*", Pattern.DOTALL);

    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";

    @Override
    public Pattern getHeaderPattern() {
        return THUNDERBIRD_ENGLISH_HEADER_PATTERN;
    }

    @Override
    public boolean parse(Matcher m, Map<String, Object> resultMap) {
        // for debug
        if (log.isDebugEnabled()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                log.debug(i + ": " + m.group(i));
            }
        }

        // Set the senders
        Contacts origSenders = parseContacts(m.group(8));
        if (origSenders != null && !origSenders.isEmpty()) {
            // fill the sender name key
            String origSenderName = origSenders.get(0).getName();
            resultMap.put(ORIGINAL_SENDER_NAME_KEY, origSenderName);
        }
        resultMap.put(ORIGINAL_SENDERS_KEY, origSenders);
        // Set the reception date
        if (m.group(6) != null) {
            resultMap.put(ORIGINAL_RECEPTION_DATE_KEY, parseDate(m.group(6)));
        }
        // TO
        if (m.group(10) != null) {
            resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY,
                    parseContacts(m.group(10)));
        }
        // Cc
        Contacts ccContacts = new Contacts();
        if (m.group(13) != null) {
            // Cc
            ccContacts = parseContacts(m.group(13));
        }
        resultMap.put(ORIGINAL_CC_RECIPIENTS_KEY, ccContacts);

        return true;
    }

    /**
     * @param contacts
     * @return the contacts parsed from the contacts param
     */
    protected Contacts parseContacts(String contacts) {
        log.debug(String.format("Parsing contacts '%s'", contacts));
        if (contacts != null && contacts.length() > 0) {
            String[] split = contacts.trim().split("[,;]\\w*");
            Contacts res = new Contacts();
            for (String contact : split) {
                Matcher m = getContactPattern().matcher(contact);
                Contact item = new Contact();
                if (m.matches()) {
                    // for debug
                    if (log.isDebugEnabled()) {
                        for (int i = 1; i < m.groupCount() + 1; i++) {
                            log.debug(i + ": " + m.group(i));
                        }
                    }

                    String name = m.group(1).trim();
                    String email = m.group(3).trim();

                    item.setName(name);
                    item.setEmail(email);

                    res.add(item);

                }
            }
            return res;
        }
        return null;
    }

    /**
     * Return the contact pattern to be used when parsing contacts.
     *
     * @return
     */
    protected Pattern getContactPattern() {
        return DEFAULT_CONTACT_PATTERN;
    }

    /**
     * Parse the date giving the date format and return a calendar object.
     *
     * @param dateString
     * @return the date parsed from the dateString
     * @throws ParseException
     */
    protected Calendar parseDate(String dateString) {
        try {
            log.debug(String.format("Parsing date '%s'", dateString));
            DateFormat sdf = getDateFormat();
            Date date = sdf.parse(dateString.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        } catch (ParseException e) {
            log.error("Parsing date failed : " + dateString, e);
            return null;
        }
    }

    /**
     * return the dateformat to be used when parsing the Date
     *
     * @return
     */
    protected DateFormat getDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
    }

}
