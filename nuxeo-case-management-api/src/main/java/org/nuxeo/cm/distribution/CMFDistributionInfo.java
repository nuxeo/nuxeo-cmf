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
 * $Id: DistributionInfo.java 56845 2008-08-12 16:43:31Z gracinet $
 */

package org.nuxeo.cm.distribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.cm.caselink.CaseLinkType;

/**
 * Distribution information.
 * <p>
 * This class is used to gather information from the interface.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class CMFDistributionInfo implements DistributionInfo {

    private static final long serialVersionUID = 6930633132089246322L;

    String mode;

    List<ParticipantItem> favoriteMailboxes;

    List<String> forActionMailboxes;

    List<String> forActionMailingLists;

    List<String> forActionGroups;

    String[] forActionFunctions;

    List<String> forInformationMailboxes;

    List<String> forInformationGroups;

    List<String> forInformationMailingLists;

    String[] forInformationFunctions;

    String comment;

    // getters & setters

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<ParticipantItem> getFavoriteMailboxes() {
        return favoriteMailboxes;
    }

    public void setFavoriteMailboxes(List<ParticipantItem> favoriteMailboxes) {
        this.favoriteMailboxes = favoriteMailboxes;
        // order them
        Collections.sort(this.favoriteMailboxes,
                new Comparator<ParticipantItem>() {
                    public int compare(ParticipantItem o1, ParticipantItem o2) {
                        int comp = 0;
                        if (o1.getType() != null) {
                            comp = o1.getType().compareTo(o2.getType());
                        } else if (o2.getType() != null) {
                            return -1;
                        }

                        if (comp == 0 && o1.getTitle() != null) {
                            comp = o1.getTitle().compareTo(o2.getTitle());
                        }
                        return comp;
                    }
                });
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getForActionMailboxes() {
        return forActionMailboxes;
    }

    public void setForActionMailboxes(List<String> forActionPersonalMailboxes) {
        this.forActionMailboxes = forActionPersonalMailboxes;
    }

    public List<String> getForInformationMailboxes() {
        return forInformationMailboxes;
    }

    public void setForInformationMailboxes(
            List<String> forInformationPersonalMailboxes) {
        this.forInformationMailboxes = forInformationPersonalMailboxes;
    }

    public void setForActionParticipantLists(List<String> forActionMailingLists) {
        this.forActionMailingLists = forActionMailingLists;
    }

    public void setForActionFunctions(String[] forActionFunctions) {
        this.forActionFunctions = forActionFunctions;
    }

    public void setForInformationParticipantLists(
            List<String> forInformationMailingLists) {
        this.forInformationMailingLists = forInformationMailingLists;
    }

    public void setForInformationFunctions(String[] forInformationFunctions) {
        this.forInformationFunctions = forInformationFunctions;
    }

    // presentation helpers

    public List<String> getAllForActionMailboxes() {
        String type = CaseLinkType.FOR_ACTION.getStringType();
        Set<String> mailboxes = new HashSet<String>();
        if (favoriteMailboxes != null && !favoriteMailboxes.isEmpty()) {
            for (ParticipantItem item : favoriteMailboxes) {
                if (type.equals(item.getType())) {
                    mailboxes.add(item.getId());
                }
            }
        }
        if (forActionMailboxes != null) {
            mailboxes.addAll(forActionMailboxes);
        }
        return new ArrayList<String>(mailboxes);
    }

    public List<String> getForActionParticipantLists() {
        return forActionMailingLists;
    }

    public List<String> getForActionFunctions() {
        if (forActionFunctions != null) {
            return Arrays.asList(forActionFunctions);
        }
        return null;
    }

    public List<String> getAllForInformationMailboxes() {
        String type = CaseLinkType.FOR_INFORMATION.getStringType();
        Set<String> mailboxes = new HashSet<String>();
        if (favoriteMailboxes != null && !favoriteMailboxes.isEmpty()) {
            for (ParticipantItem item : favoriteMailboxes) {
                if (type.equals(item.getType())) {
                    mailboxes.add(item.getId());
                }
            }
        }
        if (forInformationMailboxes != null) {
            mailboxes.addAll(forInformationMailboxes);
        }
        return new ArrayList<String>(mailboxes);
    }

    public List<String> getForInformationParticipantLists() {
        return forInformationMailingLists;
    }

    public List<String> getForInformationFunctions() {
        if (forInformationFunctions != null) {
            return Arrays.asList(forInformationFunctions);
        }
        return null;
    }

    public Map<String, List<String>> getAllParticipants() {
        // TODO: add other info (mailing lists, etc...)
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        res.put(CaseLinkType.FOR_ACTION.name(), getAllForActionMailboxes());
        res.put(CaseLinkType.FOR_INFORMATION.name(),
                getAllForInformationMailboxes());
        return res;
    }

    public boolean isEmpty(@SuppressWarnings("rawtypes")
    List list) {
        return list == null || list.isEmpty();
    }

    public boolean hasParticipants() {
        boolean res = true;
        if (isEmpty(forActionMailboxes) && isEmpty(forActionMailingLists)
                && isEmpty(getForActionFunctions()) && isEmpty(forActionGroups)
                && isEmpty(forInformationMailboxes)
                && isEmpty(forInformationMailingLists)
                && isEmpty(getForInformationFunctions())
                && isEmpty(forInformationGroups)) {
            res = false;
        }
        return res;
    }

    public boolean hasActionParticipants() {
        boolean res = true;
        if (isEmpty(forActionMailboxes) && isEmpty(forActionMailingLists)
                && isEmpty(getForActionFunctions()) && isEmpty(forActionGroups)) {
            res = false;
        }
        return res;
    }

    public List<String> getForActionGroups() {
        return forActionGroups;
    }

    public void setForActionGroups(List<String> forActionGroups) {
        this.forActionGroups = forActionGroups;
    }

    public List<String> getForInformationGroups() {
        return forInformationGroups;
    }

    public void setForInformationGroups(List<String> forInformationGroups) {
        this.forInformationGroups = forInformationGroups;
    }

}
