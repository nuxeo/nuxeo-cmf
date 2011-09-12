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

    public CaseLinkRequestImpl(String sender, Date date, String subject,
            String comment, Case envelope,
            Map<String, List<String>> internalRecipients,
            Map<String, List<String>> externalRecipients) {
        this.sender = sender;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        this.date = cal;
        this.subject = subject;
        this.comment = comment;
        this.envelope = envelope;
        this.internalRecipients = internalRecipients;
        this.externalRecipients = externalRecipients;
    }

    @Override
    public Map<String, List<String>> getAllParticipants() {
        return internalRecipients;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Date getDate() {
        return date.getTime();
    }

    @Override
    public Case getCase(CoreSession session) {
        return envelope;
    }

    @Override
    public String getCaseId() {
        DocumentModel envelopeDoc = envelope.getDocument();
        if (envelopeDoc == null) {
            return null;
        }
        return envelopeDoc.getId();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Post request have no id.");
    }

    @Override
    public Map<String, List<String>> getInitialInternalParticipants() {
        return internalRecipients;
    }

    @Override
    public Map<String, List<String>> getInitialExternalParticipants() {
        return externalRecipients;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getSenderMailboxId() {
        return null;
    }

    @Override
    public Date getSentDate() {
        throw new UnsupportedOperationException(
                "Post request have no sending date.");
    }

    public String getSubjet() {
        throw new UnsupportedOperationException("Post request have no subject.");
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Post request have no type.");
    }

    @Override
    public boolean isRead() {
        throw new UnsupportedOperationException(
                "Post request have no read marker.");
    }

    @Override
    public void save(CoreSession session) {
        throw new UnsupportedOperationException(
                "Post request can not be saved.");
    }

    @Override
    public DocumentModel getDocument() {
        throw new UnsupportedOperationException(
                "Post request have no document.");
    }

    @Override
    public boolean isDraft() {
        throw new UnsupportedOperationException(
                "Post request have no draft status.");
    }

    @Override
    public void addInitialExternalParticipants(
            Map<String, List<String>> recipients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addInitialInternalParticipants(
            Map<String, List<String>> recipients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
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
