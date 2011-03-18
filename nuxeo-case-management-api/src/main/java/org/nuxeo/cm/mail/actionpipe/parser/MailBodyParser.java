/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.cm.mail.actionpipe.ParseMailBody;

/**
 * Parser that is going to be used by {@link ParseMailBody}
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 */
public interface MailBodyParser {

    /**
     * The header pattern used to identify a parser to be used
     *
     * @return the regexp to be used
     */
    Pattern getHeaderPattern();

    /**
     * Perform the parsing
     *
     * @param m
     * @param resultMap
     * @return
     */
    boolean parse(Matcher m, Map<String, Object> resultMap);

}
