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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id$
 */

package org.nuxeo.cm.caselink;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.cases.Case;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public class CaseLinkRequestImpl implements CaseLink {

    private static final long serialVersionUID = -2454486043183207094L;

    protected final String sender;

    protected final Calendar date;

    protected final String subject;

    protected final String comment;

    protected final Case envelope;

    final Map<String, List<String>> internalRecipients;

    final Map<String, List<String>> externalRecipients;

    public CaseLinkRequestImpl(String sender, Calendar date, String subject,
            String comment, Case envelope,
            Map<String, List<String>> internalRecipients,
            Map<String, List<String>> externalRecipients) {
        this.sender = sender;
        this.date = date;
        this.subject = subject;
        this.comment = comment;
        this.envelope = envelope;
        this.internalRecipients = internalRecipients;
        this.externalRecipients = externalRecipients;
    }

    public Map<String, List<String>> getAllParticipants() {
        return internalRecipients;
    }

    public String getComment() {
        return comment;
    }

    public Date getDate() {
        return date.getTime();
    }

    public Case getCase(CoreSession session) {
        return envelope;
    }

    public String getId() {
        throw new UnsupportedOperationException("Post request have no id.");
    }

    public Map<String, List<String>> getInitialInternalParticipants() {
        return internalRecipients;
    }

    public Map<String, List<String>> getInitialExternalParticipants() {
        return externalRecipients;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getSenderMailboxId() {
        return null;
    }

    public Date getSentDate() {
        throw new UnsupportedOperationException(
                "Post request have no sending date.");
    }

    public String getSubjet() {
        throw new UnsupportedOperationException("Post request have no subject.");
    }

    public String getType() {
        throw new UnsupportedOperationException("Post request have no type.");
    }

    public boolean isRead() {
        throw new UnsupportedOperationException(
                "Post request have no read marker.");
    }

    public void save(CoreSession session) {
        throw new UnsupportedOperationException(
                "Post request can not be saved.");
    }

    public DocumentModel getDocument() {
        throw new UnsupportedOperationException(
                "Post request have no document.");
    }

    public boolean isDraft() {
        throw new UnsupportedOperationException(
                "Post request have no draft status.");
    }

    public void addInitialExternalParticipants(
            Map<String, List<String>> recipients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addInitialInternalParticipants(
            Map<String, List<String>> recipients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addParticipants(Map<String, List<String>> recipients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setActionnable(boolean actionnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActionnable() {
        throw new UnsupportedOperationException();
    }

}
