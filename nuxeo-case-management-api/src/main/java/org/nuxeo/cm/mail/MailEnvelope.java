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
 * $Id: MailEnvelope.java 57494 2008-09-11 17:17:23Z atchertchian $
 */

package org.nuxeo.cm.mail;

import java.io.Serializable;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Mail envelope
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */
public interface MailEnvelope extends HasRecipients, Serializable {

    /**
     * Gets the document model describing the envelope
     */
    DocumentModel getDocument();

    /**
     * get the items as document with the session of the envelope.
     *
     * @return
     */
    List<DocumentModel> getDocuments();

    /**
     * get the items as document
     *
     * @return
     */
    List<DocumentModel> getDocuments(CoreSession session);

    /**
     * Returns unmodifiable list of items.
     */
    List<MailEnvelopeItem> getMailEnvelopeItems(CoreSession session);

    /**
     * Gets the first document within this envelope
     */
    MailEnvelopeItem getFirstItem(CoreSession session);

    /**
     * Adds given item to the envelope.
     *
     * @return true if added, false if item was already present.
     */
    boolean addMailEnvelopeItem(MailEnvelopeItem item, CoreSession session);

    /**
     * Removes given item from the envelope.
     *
     * @return true if added, false if item was not present.
     */
    boolean removeMailEnvelopeItem(MailEnvelopeItem item, CoreSession session);

    /**
     * Moves up given selected items in the envelope.
     *
     * @return true if moved up, false if some could not be moved.
     */
    boolean moveUpEmailsInEnvelope(List<MailEnvelopeItem> selected,
            CoreSession session);

    /**
     * Moves down given selected items in the envelope.
     *
     * @return true if moved down, false if some could not be moved.
     */
    boolean moveDownEmailsInEnvelope(List<MailEnvelopeItem> selected,
            CoreSession session);

    /**
     * Persist the envelope.
     *
     * @param session
     */
    void save(CoreSession session);

    /**
     * Is this a draft envelope
     *
     * @param session
     */
    boolean isDraft() throws ClientException;

}
