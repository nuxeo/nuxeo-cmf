/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id:$
 */

package org.nuxeo.cm.mail.actionpipe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.contact.Contacts;
import org.nuxeo.cm.contact.Contact;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;


/**
 * Parser for mail body
 *
 * @author Laurent Doguin
 *
 */
public class ParseMailBody extends AbstractCaseManagementMailAction {

    private static final String ENGLISH_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";

    private static final String FRENCH_DATE_FORMAT = "EEEE dd MMM yyyy HH:mm";

    private static final Log log = LogFactory.getLog(ParseMailBody.class);

    // De : Alain Escaffre [mailto:aescaffre@nuxeo.com]=20
    // Envoyé : lundi 19 mai 2008 09:06
    // À : doguin laurent
    // Cc : Anahide Tchertchian; Oriane TIAN; Alain Escaffre
    // Objet : [TR] ...

    /**
     * The group used to find the index of the header in the body
     */
    public static final Integer GROUP_TO_FIND_INDEX = 2;

    public static final Pattern HEADER_PATTERN = Pattern.compile("(.*?)"
            + "(De :)(.*?)" + "(Envoy\u00e9 :)" + "((.*?)(\u00c0 :))?"
            + "((.*?)(Cc :))?(.*?)" + "(Objet :)(.*?)");

    public static final Pattern ENGLISH_HEADER_PATTERN = Pattern.compile("(.*?)Original Message(.*?)"
            + "(Subject:)(.*?)"
            + "(Date:)(.*?)"
            + "(From:)(.*?)"
            + "(To:)(.*?)" + "((Cc:)(.*?))?");

    public static final Pattern CONTACT_PATTERN = Pattern.compile("(.*)(\\[mailto:)(.*?)(\\])(.*)");

    public static final String EMAIL_MATCH = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";

    public static final Pattern THUNDERBIDRD_ENGLISH_CONTACT_PATTERN = Pattern.compile("(.*?)<(.*?)>(.*?)");

    // PLE-680: marker to get rid of " > " ligns
    public static final String TRANSFER_MARKER = " > ";

    public static final Map<String, Integer> monthMap = new HashMap<String, Integer>();

    public static final void initializeMonthMap() {
        if (monthMap.isEmpty()) {
            // java.util.Calendar month is 0-based....
            monthMap.put("janvier", 0);
            monthMap.put("f\u00e9vrier", 1);
            monthMap.put("mars", 2);
            monthMap.put("avril", 3);
            monthMap.put("mai", 4);
            monthMap.put("juin", 5);
            monthMap.put("juillet", 6);
            monthMap.put("ao\u00fbt", 7);
            monthMap.put("septembre", 8);
            monthMap.put("octobre", 9);
            monthMap.put("novembre", 10);
            monthMap.put("d\u00e9cembre", 11);
        }
    }

    public boolean execute(ExecutionContext context) throws Exception {
        Object content = context.get(BODY_KEY);
        if (content != null) {
            parse(content, context);
        }
        return true;
    }

    /**
     * @param toParse
     * @param resultMap
     * @return true if the parsing is successful, otherwise false
     */
    protected static boolean parseEnglishBody(Matcher m,
            Map<String, Object> resultMap) {

        // for debug
        if (log.isDebugEnabled()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                log.debug(i + ": " + m.group(i));
            }
        }

        // Set the senders
        Contacts origSenders = parseEnglishContacts(m.group(8));
        if (origSenders != null && !origSenders.isEmpty()) {
            // fill the sender name key
            String origSenderName = origSenders.get(0).getName();
            resultMap.put(ORIGINAL_SENDER_NAME_KEY, origSenderName);
        }
        resultMap.put(ORIGINAL_SENDERS_KEY, origSenders);
        // Set the reception date
        if (m.group(6) != null) {
            resultMap.put(ORIGINAL_RECEPTION_DATE_KEY,
                    parseEnglishDate(m.group(6)));
        }
        // TO
        if (m.group(10) != null) {
            resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY,
                    parseEnglishContacts(m.group(10)));
        }
        // Cc
        Contacts ccContacts = new Contacts();
        if (m.group(13) != null) {
            // Cc
            ccContacts = parseEnglishContacts(m.group(13));
        }
        resultMap.put(ORIGINAL_CC_RECIPIENTS_KEY, ccContacts);

        return true;
    }

    /**
     * @param toParse
     * @param resultMap
     * @return true if the parsing is successful, otherwise false
     */
    protected static boolean parseFrenchBody(Matcher m,
            Map<String, Object> resultMap) {

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
        // 13: [correspondence] courriel test pour fonctionnalité "transfert de
        // courriel vers correspondence" Ceci est un courriel de test

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
                resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY,
                        parseContacts(m.group(9)));
                resultMap.put(ORIGINAL_CC_RECIPIENTS_KEY,
                        parseContacts(m.group(11)));
            } else {
                // no Cc
                resultMap.put(ORIGINAL_TO_RECIPIENTS_KEY,
                        parseContacts(m.group(11)));
            }
        } else {
            // no To, assume no Cc neither
            resultMap.put(ORIGINAL_RECEPTION_DATE_KEY, parseDate(m.group(11)));
        }

        return true;
    }

    public static void parse(Object content, Map<String, Object> resultMap)
    throws Exception {
        try {
            if (!(content instanceof String)) {
                log.error("Cannot parse non-string mail body");
                return;
            }
            String toParse = (String) content;
            // get rid of lines in case header was garbled
            toParse = toParse.replaceAll("(\n|\r)", " ");

            log.debug("Trying to parse " + content);

            // Try to parse the content, using the french or english pattern
            Matcher matcherEnglish = ENGLISH_HEADER_PATTERN.matcher(toParse);
            Matcher matcherFrench = HEADER_PATTERN.matcher(toParse);

            // Call the right parser, according to the pattern that matches
            // If the two patterns matches, takes the one which matches the
            // first header
            if (matcherEnglish.matches() && !matcherFrench.matches()) {
                parseEnglishBody(matcherEnglish, resultMap);
            } else if (!matcherEnglish.matches() && matcherFrench.matches()) {
                parseFrenchBody(matcherFrench, resultMap);
            } else if (matcherEnglish.matches() && matcherFrench.matches()) {
                int startMatcherEnglish = matcherEnglish.start(GROUP_TO_FIND_INDEX);
                int startMatcherFrench = matcherFrench.start(GROUP_TO_FIND_INDEX);
                if (startMatcherEnglish < startMatcherFrench) {
                    parseEnglishBody(matcherEnglish, resultMap);
                } else {
                    parseFrenchBody(matcherEnglish, resultMap);
                }
            } else {
                throw new IllegalArgumentException(
                        "Content cannot be matched : " + toParse);
            }

        } catch (Exception e) {
            log.error("Cannot parse mail body", e);
        }
    }

    public static Contacts parseContacts(String contacts) {
        log.debug(String.format("Parsing contacts '%s'", contacts));
        if (contacts != null && contacts.length() > 0) {
            if (contacts.endsWith(TRANSFER_MARKER)) {
                contacts = contacts.substring(0, contacts.length()
                        - TRANSFER_MARKER.length());
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
                    //                    }

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

    /**
     * @param contacts
     * @return the contacts parsed from the contacts param
     */
    protected static Contacts parseEnglishContacts(String contacts) {
        log.debug(String.format("Parsing contacts '%s'", contacts));
        if (contacts != null && contacts.length() > 0) {
            String[] split = contacts.trim().split("; ");
            Contacts res = new Contacts();
            for (String contact : split) {
                Matcher m = THUNDERBIDRD_ENGLISH_CONTACT_PATTERN.matcher(contact);
                Contact item = new Contact();
                if (m.matches()) {
                    // for debug
                    if (log.isDebugEnabled()) {
                        for (int i = 1; i < m.groupCount() + 1; i++) {
                            log.debug(i + ": " + m.group(i));
                        }
                    }

                    // 1: Laurent Doguin
                    // 2: ldoguin@nuxeo.com
                    // 3:

                    item.setName(m.group(1).trim());
                    item.setEmail(m.group(2).trim());
                    res.add(item);
                }
            }
            return res;
        }
        return null;
    }

    /**
     * @param dateString
     * @return the date parsed from the dateString
     * @throws ParseException
     */
    public static Calendar parseEnglishDate(String dateString) {
        try {
            log.debug(String.format("Parsing date '%s'", dateString));
            SimpleDateFormat sdf = new SimpleDateFormat(ENGLISH_DATE_FORMAT,
                    Locale.ENGLISH);
            Date date;
            date = sdf.parse(dateString.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        } catch (ParseException e) {
            log.error("Parsing date failed : " + dateString, e);
            return null;
        }
    }

    public static Calendar parseDate(String dateString) {
        try {
            log.debug(String.format("Parsing date '%s'", dateString));

            SimpleDateFormat sdf = new SimpleDateFormat(FRENCH_DATE_FORMAT,
                    Locale.FRENCH);
            Date date = sdf.parse(dateString.trim());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal;
        } catch (ParseException e) {
            log.error("Parsing date failed : " + dateString, e);
            return null;
        }
    }

    public void reset(ExecutionContext context) throws Exception {
        // do nothing
    }

}
