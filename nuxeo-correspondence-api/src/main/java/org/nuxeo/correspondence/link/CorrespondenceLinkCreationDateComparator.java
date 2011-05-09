/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id: PleiadeEnvelopeStatementsComparator.java 13483 2009-03-19 12:52:45Z atchertchian $
 */

package org.nuxeo.correspondence.link;

import java.util.Comparator;

/**
 * Comparator on link using the creation date metadata.
 *
 * @author ldoguin
 *
 */
public class CorrespondenceLinkCreationDateComparator implements
        Comparator<CorrespondenceLink> {

    private static final long serialVersionUID = 1L;

    public int compare(CorrespondenceLink st1, CorrespondenceLink st2) {
        String v1 = st1.getCreationDate().toString();
        String v2 = st2.getCreationDate().toString();
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        }
        return v1.compareTo(v2);
    }

}
