/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 */

package org.nuxeo.correspondence.core.generators;

import static org.nuxeo.correspondence.core.utils.CorrespondenceConstants.IN_CORRESPONDENCE_DOCUMENT;
import static org.nuxeo.correspondence.core.utils.CorrespondenceConstants.OUT_CORRESPONDENCE_DOCUMENT;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.uidgen.AbstractUIDGenerator;

/**
 * Generates an UID with a specific format for a given document
 *
 * @author Mariana Cedica
 */
public class CorrespondenceUIDGenerator extends AbstractUIDGenerator {

    public static final String NXC_PREFIX = "NXC";

    public static final String NXC_PREFIX_IN = "NXC-IN";

    public static final String NXC_PREFIX_OUT = "NXC-OUT";

    public static final String NFORMAT = "%05d";

    public String getSequenceKey(DocumentModel doc) throws DocumentException {
        Calendar cal = new GregorianCalendar();
        return Integer.toString(cal.get(Calendar.YEAR));
    }

    public String createUID(DocumentModel doc) throws DocumentException {
        int index = getNext(doc);
        String n = String.format(NFORMAT, index);
        final String seqKey = getSequenceKey(doc);
        return String.format("%s-%s-%s", getPrefixFromType(doc), seqKey, n);
    }

    protected String getPrefixFromType(DocumentModel doc) {
        String prefix = NXC_PREFIX;
        if (doc.getType().equals(OUT_CORRESPONDENCE_DOCUMENT)) {
            prefix = NXC_PREFIX_OUT;
        }
        if (doc.getType().equals(IN_CORRESPONDENCE_DOCUMENT)) {
            prefix = NXC_PREFIX_IN;
        }
        return prefix;
    }
}
