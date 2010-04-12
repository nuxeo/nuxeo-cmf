/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: DistributionType.java 53781 2008-03-13 13:47:02Z atchertchian $
 */

package org.nuxeo.cm.post;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */
public enum CaseLinkType {

    ANY, FOR_ACTION, FOR_INFORMATION, REFUSAL, NONE;

    private static final Log log = LogFactory.getLog(CaseLinkType.class);

    /**
     * String type to use in html pages.
     */
    public String getStringType() {
        return name();
    }

    public static CaseLinkType valueOfString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException err) {
            log.error(String.format("Illegal value '%s', "
                    + "should be one of %s", name, CaseLinkType.values()));
            return NONE;
        }
    }

}
