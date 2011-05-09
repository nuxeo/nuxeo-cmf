/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.correspondence.core.link;

import java.util.List;

import org.nuxeo.correspondence.link.CorrespondenceLink;
import org.nuxeo.correspondence.link.CorrespondenceLinksConstants;
import org.nuxeo.correspondence.link.EnvelopeToMailLink;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 *
 * @author ldoguin
 */
public class EnvelopeToMailLinkImpl extends AbstractLink implements EnvelopeToMailLink {

    public EnvelopeToMailLinkImpl(DocumentModel doc) {
        this.document = doc;
    }

    @Override
    public List<CorrespondenceLink> getEnvelopeToMailLink() {
        return getCorrespondenceLink(CorrespondenceLinksConstants.ENVELOPE_TO_MAIL_LINK_PROPERTY_NAME);
    }

    @Override
    public void resetEnvelopeToMailLink() {
        resetCorrespondenceLink(CorrespondenceLinksConstants.ENVELOPE_TO_MAIL_LINK_PROPERTY_NAME);

    }

    @Override
    public void addEnvelopeToMailLink(CorrespondenceLink link) {
        addCorrespondenceLink(
                CorrespondenceLinksConstants.ENVELOPE_TO_MAIL_LINK_PROPERTY_NAME,
                link);

    }

    @Override
    public void addAllEnvelopeToMailLink(List<CorrespondenceLink> links) {
        addAllCorrespondenceLinks(
                CorrespondenceLinksConstants.ENVELOPE_TO_MAIL_LINK_PROPERTY_NAME,
                links);
    }

    @Override
    public CorrespondenceLink getLink(String targetDocId) {
        List<CorrespondenceLink> existingLinks = getEnvelopeToMailLink();
        for (CorrespondenceLink existingLink : existingLinks) {
            if (existingLink.getTargetDocId().equals(targetDocId)) {
                // link already exist
                return existingLink;
            }
        }
        return null;
    }
}
