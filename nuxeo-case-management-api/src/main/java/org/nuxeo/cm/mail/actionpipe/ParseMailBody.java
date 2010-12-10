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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.mail.actionpipe.parser.DefaultEnglishMailParser;
import org.nuxeo.cm.mail.actionpipe.parser.GmailMailParser;
import org.nuxeo.cm.mail.actionpipe.parser.DefaultFrenchMailParser;
import org.nuxeo.cm.mail.actionpipe.parser.MailBodyParser;
import org.nuxeo.cm.mail.actionpipe.parser.ThunderbirdFrenchMailParser;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;

/**
 * Parser for mail body.
 *
 * @author Laurent Doguin
 * @author Sun Seng David TAN <stan@nuxeo.com>
 */
public class ParseMailBody extends AbstractCaseManagementMailAction {

    private static final Log log = LogFactory.getLog(ParseMailBody.class);

    /**
     * The group used to find the index of the header in the body
     */
    public static final Integer GROUP_TO_FIND_INDEX = 2;

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

    @Override
    public boolean execute(ExecutionContext context) throws Exception {
        Object content = context.get(BODY_KEY);
        if (content != null) {
            parse(content, context);
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
            // All regexp should use Pattern.DOTALL option
            // toParse = toParse.replaceAll("(\n|\r)", " ");

            log.debug("Trying to parse " + content);

            // Call the right parser, according to the pattern that matches
            // If the two patterns matches, takes the one which matches the
            // first header
            List<MailBodyParser> mailBodyParsers = new ArrayList<MailBodyParser>(
                    2);
            mailBodyParsers.add(new DefaultEnglishMailParser());
            mailBodyParsers.add(new DefaultFrenchMailParser());
            mailBodyParsers.add(new ThunderbirdFrenchMailParser());
            mailBodyParsers.add(new GmailMailParser());

            MailBodyParser finalMailBodyParser = null;
            int startMatcher = Integer.MAX_VALUE;
            Matcher finalMatcher = null;

            for (MailBodyParser mailBodyParser : mailBodyParsers) {
                Matcher matcher = mailBodyParser.getHeaderPattern().matcher(
                        toParse);
                if (matcher.matches()) {
                    int start = matcher.start(GROUP_TO_FIND_INDEX);
                    if (start < startMatcher) {
                        startMatcher = start;
                        finalMailBodyParser = mailBodyParser;
                        finalMatcher = matcher;
                    }
                }
            }

            if (finalMailBodyParser == null) {
                throw new IllegalArgumentException(
                        "Content cannot be matched : " + toParse);
            }

            finalMailBodyParser.parse(finalMatcher, resultMap);

        } catch (Exception e) {
            log.error("Cannot parse mail body", e);
        }
    }

    @Override
    public void reset(ExecutionContext context) throws Exception {
        // do nothing
    }

}
