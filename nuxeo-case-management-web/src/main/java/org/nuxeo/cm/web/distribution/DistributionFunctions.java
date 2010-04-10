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
 * $Id: DistributionFunctions.java 59336 2008-12-12 12:16:04Z atchertchian $
 */

package org.nuxeo.cm.web.distribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.mailbox.MailboxHeader;
import org.nuxeo.cm.service.CorrespondenceService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;


/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */
public class DistributionFunctions {

    public static final int NUMBER_OF_WORDS = 50;

    public static String join(Collection<Object> collection, String separator) {
        if (collection == null) {
            return null;
        }
        return StringUtils.join(collection.iterator(), separator);
    }

    public static String getMailboxTitle(String mailboxId) {
        MailboxHeader mailboxHeader = getMailboxHeader(mailboxId);
        if (mailboxHeader != null) {
            return mailboxHeader.getTitle();
        }
        return mailboxId;
    }

    public static String getMailboxTitles(List<String> mailboxIds,
            String separator, Boolean sorted) {
        List<MailboxHeader> mailboxes = getMailboxesHeaders(mailboxIds);
        if (mailboxes != null) {
            List<String> titles = new ArrayList<String>();
            for (MailboxHeader mb : mailboxes) {
                titles.add(mb.getTitle());
            }
            if (sorted) {
                Collections.sort(titles);
            }
            return StringUtils.join(titles.toArray(new String[] {}), separator);
        }
        return null;
    }

    public static Mailbox getMailbox(DocumentModel mailboxDoc) {
        return mailboxDoc.getAdapter(Mailbox.class);
    }

    public static Mailbox getMailbox(String mailboxId) {
        try {
            CorrespondenceService service = Framework.getService(CorrespondenceService.class);
            return service.getMailbox(mailboxId);
        } catch (Exception e) {
            return null;
        }
    }

    public static MailboxHeader getMailboxHeader(String mailboxId) {
        try {
            CorrespondenceService service = Framework.getService(CorrespondenceService.class);
            return service.getMailboxHeader(mailboxId);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Mailbox> getMailboxes(List<String> mailboxIds) {
        try {
            CorrespondenceService service = Framework.getService(CorrespondenceService.class);
            return service.getMailboxes(mailboxIds);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<MailboxHeader> getMailboxesHeaders(List<String> mailboxIds) {
        try {
            CorrespondenceService service = Framework.getService(CorrespondenceService.class);
            return service.getMailboxesHeaders(mailboxIds);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatStringForView(String summary) {
        // Remove HTML tags
        String[] words = summary.split(" ");
        if (words.length > NUMBER_OF_WORDS) {
            // Display first lines
            String[] wordsToReturn = new String[NUMBER_OF_WORDS];
            for (int i = 0; i < NUMBER_OF_WORDS; i++) {
                wordsToReturn[i] = words[i];
            }
            return StringUtils.join(wordsToReturn, " ") + " ...";
        } else {
            return StringUtils.join(words, " ");
        }
    }

}
