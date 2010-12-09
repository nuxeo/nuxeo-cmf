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

/**
 * This is the Thunderbird French Mail Parser which is from a forwarded mail
 * using French Thunderbird. The header pattern is a bit different: labels are
 * in French.
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 *
 */
public class ThunderbirdFrenchMailParser extends DefaultEnglishMailParser {

    public static final Pattern THUNDERBIRD_FRENCH_HEADER_PATTERN = Pattern.compile("(.*?)Message original(.*?)"
            + "(Sujet *:)(.*?)"
            + "(Date *:)(.*?)"
            + "(De *:)(.*?)"
            + "(Pour *:)(.*?)" + "((Cc:)(.*?))?");

    @Override
    public Pattern getHeaderPattern() {
        return THUNDERBIRD_FRENCH_HEADER_PATTERN;
    }

}
