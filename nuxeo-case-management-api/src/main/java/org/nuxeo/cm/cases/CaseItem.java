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
 * $Id: MailEnvelopeItem.java 57494 2008-09-11 17:17:23Z atchertchian $
 */

package org.nuxeo.cm.cases;

import java.io.Serializable;
import java.util.Calendar;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Mail envelope item.
 * <p>
 * Represents a document placed in an envelope.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 */
public interface CaseItem extends HasParticipants, Serializable {

    /**
     * Gets the title of this item.
     */
    String getTitle();

    void setTitle(String title);

    /**
     * Gets the date the item was sent.
     */
    Calendar getSendingDate();

    void setSendingDate(Calendar date);

    /**
     * Gets the date item was received.
     */
    Calendar getReceiveDate();

    void setReceiveDate(Calendar date);

    /**
     * Sets the item the item was imported into Nuxeo.
     */
    Calendar getImportDate();

    void setImportDate(Calendar date);

    /**
     * Gets the date on the item.
     */
    Calendar getDocumentDate();

    void setDocumentDate(Calendar date);

    /**
     * Gets the confidentiality of the document. The default is 4.
     */
    String getConfidentiality();

    void setConfidentiality(String cdf);

    /**
     * Gets the origin of the document (email, scan ...).
     */
    String getOrigin();

    void setOrigin(String origin);

    /**
     * Get the type of document.
     */
    String getType();

    void setType(String type);

    /**
     * Gets the envelope this document is in. This value only is only set when it
     * is coming from an envelope in the current session.
     *
     * @return the envelope or null if the document is not associated with an
     *         envelope.
     */
    Case getCase();

    void setCase(Case theCase);

    /**
     * Gets the envelope in which this item should be shown if not associated
     * with an envelope.
     * <p>
     * If you find this item from a search, we still have to show it in an
     * envelope. This method tell us in which envelope to put it.
     */
    String getDefaultCaseId();

    void setDefaultCase(String mailEnvelopeId);

    /**
     * Returns the mail document.
     */
    DocumentModel getDocument();

    /**
     * Creates a mail envelope with this item inside.
     *
     * @param documentManager an open session.
     * @param parentPath the path to the folder in which the envelope will be created.
     * @return The created mail envelope.
     */
    Case createMailCase(CoreSession documentManager,
            String parentPath, String initialLifeCycleState);

    /**
     * Persists the item.
     */
    void save(CoreSession session);

}
