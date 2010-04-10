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

package org.nuxeo.cm.mail;

import java.io.Serializable;
import java.util.Calendar;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Mail envelope item
 * <p>
 * Represents a document placed in an envelope.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public interface MailEnvelopeItem extends HasRecipients, Serializable {

    /**
     * the title of this item.
     *
     * @return
     */
    String getTitle();

    void setTitle(String title);

    /**
     * get the item was sent.
     *
     * @return
     */
    Calendar getSendingDate();

    void setSendingDate(Calendar date);

    /**
     * get the item was received.
     *
     * @return
     */
    Calendar getReceiveDate();

    void setReceiveDate(Calendar date);

    /**
     * get the item the item was imported into Nuxeo.
     *
     * @return
     */
    Calendar getImportDate();

    void setImportDate(Calendar date);

    /**
     * get the date on the item.
     *
     * @return
     */
    Calendar getDocumentDate();

    void setDocumentDate(Calendar date);

    /**
     * get the confidentiality of the document. The default is 4.
     *
     * @return
     */
    String getConfidentiality();

    void setConfidentiality(String cdf);

    /**
     * get the origin of the document (email, scan ...)
     *
     * @return
     */
    String getOrigin();

    void setOrigin(String origin);

    /**
     * get the type of document.
     *
     * @return
     */
    String getType();

    void setType(String type);

    /**
     * get the envelope this document is in. This value only is only set when it
     * is coming from an envelope in the current session.
     *
     * @return the envelope or null if the document is not associated with an
     *         envelope.
     */
    MailEnvelope getEnvelope();

    void setEnvelope(MailEnvelope envelope);

    /**
     * get the envelope in which this item should be shown if not associated
     * with an envelope.
     *
     * If you find this item from a search, we still have to show it in an
     * envelope. This method tell us in which envelope to put it.
     *
     * @return
     */
    String getDefaultEnvelopeId();

    void setDefaultEnvelope(String mailEnvelopeId);

    /**
     * Returns the mail document.
     */
    DocumentModel getDocument();

    /**
     * Create a mail envelope with this item inside.
     *
     * @param documentManager an open session.
     * @param parent The folder in which the envelope will be created.
     * @return The created mail envelope.
     */
    MailEnvelope createMailEnvelope(CoreSession documentManager,
            String parentPath, String initialLifeCycleState);

    /**
     * persist the item.
     */
    void save(CoreSession session);
}
