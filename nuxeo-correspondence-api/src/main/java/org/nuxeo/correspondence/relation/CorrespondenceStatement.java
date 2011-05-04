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
package org.nuxeo.correspondence.relation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ldoguin
 */
public class CorrespondenceStatement extends HashMap<String, Serializable> {

    public CorrespondenceStatement(Map<String, Serializable> stmt) {
        this.putAll(stmt);
    }
            
    public CorrespondenceStatement(String targetDocId, String comment,
            Long order, Calendar creationDate, Calendar modificationDate,
            String author) {
        setTargetDocId(targetDocId);
        setComment(comment);
        setOrder(order);
        setCreationDate(creationDate);
        setModificationDate(modificationDate);
        setAuthor(author);
    }

    public CorrespondenceStatement(String targetDocId, String comment,
            Long order, Calendar creationDate) {
        setTargetDocId(targetDocId);
        setComment(comment);
        setOrder(order);
        setCreationDate(creationDate);
    }

    public String getTargetDocId() {
        return (String) get(CorrespondenceRelationConstants.TARGET_DOC_ID_PROPERTY_NAME);
    }

    public void setTargetDocId(String targetDocId) {
        put(CorrespondenceRelationConstants.TARGET_DOC_ID_PROPERTY_NAME,
                targetDocId);
    }

    public String getAuthor() {
        return (String) get(CorrespondenceRelationConstants.AUTHOR_PROPERTY_NAME);
    }

    public void setAuthor(String author) {
        if (author != null) {
            put(CorrespondenceRelationConstants.AUTHOR_PROPERTY_NAME, author);
        }
    }

    public String getComment() {
        return (String) get(CorrespondenceRelationConstants.COMMENT_PROPERTY_NAME);
    }

    public void setComment(String comment) {
        if (comment != null) {
            comment = comment.trim();
            if (comment.length() > 0) {
                put(CorrespondenceRelationConstants.COMMENT_PROPERTY_NAME,
                        comment);
            }
        }
    }

    public Long getOrder() {
        return (Long) get(CorrespondenceRelationConstants.ORDER_PROPERTY_NAME);
    }

    public void setOrder(Long order) {
        if (order != null) {
            put(CorrespondenceRelationConstants.ORDER_PROPERTY_NAME, order);
        }
    }

    public Calendar getModificationDate() {
        return (Calendar) get(CorrespondenceRelationConstants.MODIFICATION_DATE_PROPERTY_NAME);
    }

    public void setModificationDate(Calendar modificationDate) {
        if (modificationDate != null) {
            put(CorrespondenceRelationConstants.MODIFICATION_DATE_PROPERTY_NAME, modificationDate);
        }
    }

    public Calendar getCreationDate() {
        return (Calendar) get(CorrespondenceRelationConstants.CREATION_DATE_PROPERTY_NAME);
    }

    public void setCreationDate(Calendar creationDate) {
        if (creationDate != null) {
            put(CorrespondenceRelationConstants.CREATION_DATE_PROPERTY_NAME, creationDate);
        }
    }

}
