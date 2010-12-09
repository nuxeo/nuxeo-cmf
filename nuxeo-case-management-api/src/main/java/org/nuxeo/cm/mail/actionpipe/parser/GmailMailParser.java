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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.cm.contact.Contacts;

/**
 * This is the Gmail English Mail Parser which is from a forwarded mail using
 * English Gmail (web interface). The header pattern is a bit different (not in
 * the same order) and the date is not in the same format.
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 *
 */
public class GmailMailParser extends DefaultEnglishMailParser {

    public static final Pattern GMAIL_ENGLISH_HEADER_PATTERN = Pattern.compile("(.*?)Forwarded message(.*?)"
            + "(From\\w*:)(.*?)"
            + "(Date\\w*:)(.*?)"
            + "(Subject\\w*:)(.*?)"
            + "(To\\w*:)(.*?)" + "((Cc\\w*:)(.*?))?");

    public static final String GMAIL_DATE_FORMAT = "yyyy/MM/d";

    @Override
    public Pattern getHeaderPattern() {
        return GMAIL_ENGLISH_HEADER_PATTERN;
    }

    @Override
    protected DateFormat getDateFormat() {
        return new SimpleDateFormat(GMAIL_DATE_FORMAT, Locale.ENGLISH);
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
        Contacts origSenders = parseContacts(m.group(4));
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

}
