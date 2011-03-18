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
 * Contributors:
 *     Anahide Tchertchian
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.correspondence.mailbox;

import java.io.Serializable;

/**
 * Mailbox interface
 *
 * @author Anahide Tchertchian
 */
public interface Mailbox extends Serializable, Comparable<Mailbox> {

    /**
     * Returns the default confidentiality for incoming mails for this mailbox.
     */
    Integer getIncomingConfidentiality();

    /**
     * Sets the default confidentiality for iconming mails for this mailbox.
     */
    void setIncomingConfidentiality(Integer confidentiality);

    /**
     * Returns the default confidentiality for outgoing mails for this mailbox.
     */
    Integer getOutgoingConfidentiality();

    /**
     * Sets the default confidentiality for outgoing mails for this mailbox.
     */
    void setOutgoingConfidentiality(Integer confidentiality);

}
