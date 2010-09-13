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

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Distribution information.
 * <p>
 * This class is used to gather information from the interface.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public interface DistributionInfo extends Serializable {

    public String getMode();

    public void setMode(String mode);

    public List<ParticipantItem> getFavoriteMailboxes();

    public void setFavoriteMailboxes(List<ParticipantItem> favoriteMailboxes);

    public String getComment();

    public void setComment(String comment);

    public Map<String, List<String>> getAllParticipants();

    public boolean isEmpty(List list);

    public boolean hasParticipants();


}
