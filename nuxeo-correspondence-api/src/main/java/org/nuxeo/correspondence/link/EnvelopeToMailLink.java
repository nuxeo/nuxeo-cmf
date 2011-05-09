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
 * $Id$
 */

package org.nuxeo.correspondence.link;

import java.util.List;

/**
 * Correspondence EnvelopeToMailLink interface.
 * 
 * @author ldoguin
 */
public interface EnvelopeToMailLink extends CommonLink {

    /**
     * 
     * @return the list of envelope to mail CorrespondenceLink.
     */
    List<CorrespondenceLink> getEnvelopeToMailLink();

    /**
     * Remove all envelope to mail links.
     */
    void resetEnvelopeToMailLink();

    /**
     * Add the given envelope to mail link.
     * 
     * @param CorrespondenceLink
     */
    void addEnvelopeToMailLink(CorrespondenceLink link);

    /**
     * Add the list of given envelope to mail links.
     * 
     * @param relations
     */
    void addAllEnvelopeToMailLink(
            List<CorrespondenceLink> links);
}