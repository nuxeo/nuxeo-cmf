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

import java.io.Serializable;
import java.util.List;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.correspondence.link.CorrespondenceLink;
import org.nuxeo.correspondence.link.CorrespondenceLinksConstants;
import org.nuxeo.correspondence.link.MailToMailLink;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * 
 * @author ldoguin
 */
public class MailToMailLinkImpl extends AbstractLink implements MailToMailLink {

    public MailToMailLinkImpl(DocumentModel doc) {
        this.document = doc;
    }

    @Override
    public List<CorrespondenceLink> getMailToMailLink() {
        return getCorrespondenceLink(CorrespondenceLinksConstants.MAIL_TO_MAIL_LINK_PROPERTY_NAME);
    }

    @Override
    public void resetMailToMailLink() {
        resetCorrespondenceLink(CorrespondenceLinksConstants.MAIL_TO_MAIL_LINK_PROPERTY_NAME);
        setPropertyValue(CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME, null);
    }

    @Override
    public void addMailToMailLink(CorrespondenceLink link) {
        addCorrespondenceLink(
                CorrespondenceLinksConstants.MAIL_TO_MAIL_LINK_PROPERTY_NAME,
                link);
        try {
            String newId = link.getTargetDocId();
            List<String> itemsId = (List<String>) document.getPropertyValue(CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME);
            if (!itemsId.contains(newId)) {
                itemsId.add(newId);
            }
            document.setPropertyValue(
                    CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME,
                    (Serializable) itemsId);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public void addAllMailToMailLink(List<CorrespondenceLink> links) {
        addAllCorrespondenceLinks(
                CorrespondenceLinksConstants.MAIL_TO_MAIL_LINK_PROPERTY_NAME,
                links);
        try {
            List<String> itemsId = (List<String>) document.getPropertyValue(CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME);
            String newId;
            for (CorrespondenceLink link : links) {
                newId = link.getTargetDocId();
                if (!itemsId.contains(newId)) {
                    itemsId.add(newId);
                }
            }
            document.setPropertyValue(
                    CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME,
                    (Serializable) itemsId);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    @Override
    public CorrespondenceLink getLink(String targetDocId) {
        List<CorrespondenceLink> existingLinks = getMailToMailLink();
        for (CorrespondenceLink existingLink : existingLinks) {
            if (existingLink.getTargetDocId().equals(targetDocId)) {
                // link already exist
                return existingLink;
            }
        }
        return null;
    }

    @Override
    public void removeMailToMailLink(CorrespondenceLink link) {
        removeCorrespondenceLink(
                CorrespondenceLinksConstants.MAIL_TO_MAIL_LINK_PROPERTY_NAME,
                link);
        try {
            String docId = link.getTargetDocId();
            List<String> itemsId = (List<String>) document.getPropertyValue(CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME);
            if (itemsId.contains(docId)) {
                itemsId.remove(docId);
            }
            document.setPropertyValue(
                    CorrespondenceLinksConstants.TARGET_DOCUMENTS_ID_PROPERTY_NAME,
                    (Serializable) itemsId);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

}
