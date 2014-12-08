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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CaseManagementRuntimeException;
import org.nuxeo.correspondence.link.CommonLink;
import org.nuxeo.correspondence.link.CorrespondenceLink;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

/**
 * AbstractLink provides CRUD methods for CorrespondenceLink.
 *
 * @author ldoguin
 */
public abstract class AbstractLink implements CommonLink {

    protected DocumentModel document;

    @Override
    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected List<CorrespondenceLink> getCorrespondenceLink(String xPath) {
        List<Map<String, Serializable>> relations = getPropertyValue(xPath);
        return wrapRelationMap(relations);
    }

    protected void resetCorrespondenceLink(String xPath) {
        setPropertyValue(xPath, null);
    }

    protected List<CorrespondenceLink> wrapRelationMap(List<Map<String, Serializable>> relations) {
        List<CorrespondenceLink> statements = new ArrayList<CorrespondenceLink>();
        for (Map<String, Serializable> map : relations) {
            statements.add(new CorrespondenceLink(map));
        }
        return statements;
    }

    protected DocumentModel getDocument() {
        return document;
    }

    protected void addCorrespondenceLink(String xpath, CorrespondenceLink link) {
        List<Map> links = getPropertyValue(xpath);
        if (links == null) {
            links = new ArrayList<Map>();
        }
        if (!links.contains(link)) {
            links.add(link);
            setPropertyValue(xpath, (Serializable) links);
        }
    }

    protected void addAllCorrespondenceLinks(String xPath, List<CorrespondenceLink> links) {
        List<Map> existingLinks = getPropertyValue(xPath);
        if (existingLinks == null) {
            existingLinks = new ArrayList<Map>();
        }
        for (CorrespondenceLink link : links) {
            if (existingLinks.contains(link)) {
                links.remove(link);
            }
        }
        existingLinks.addAll(links);
        setPropertyValue(xPath, (Serializable) existingLinks);
    }

    public void removeCorrespondenceLink(String xPath, CorrespondenceLink link) {
        List<CorrespondenceLink> existingLinks = getPropertyValue(xPath);
        if (existingLinks != null && existingLinks.contains(link)) {
            existingLinks.remove(link);
            setPropertyValue(xPath, (Serializable) existingLinks);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getPropertyValue(String value) {
        try {
            return (T) document.getPropertyValue(value);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

    protected void setPropertyValue(String xPath, Serializable object) {
        try {
            document.setPropertyValue(xPath, object);
        } catch (PropertyException e) {
            throw new CaseManagementRuntimeException(e);
        } catch (ClientException e) {
            throw new CaseManagementRuntimeException(e);
        }
    }

}
