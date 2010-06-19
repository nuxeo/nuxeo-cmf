/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.cm.web.comments;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.ScopeType.EVENT;

import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBound;
import org.nuxeo.cm.web.invalidations.CaseManagementContextBoundInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.comment.web.CommentManagerActions;
import org.nuxeo.ecm.platform.comment.web.ThreadEntry;


/**
 * @author <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 */
@Name("cmCommentActions")
@Scope(CONVERSATION)
@CaseManagementContextBound
public class CaseManagementCommentActionsBean extends
CaseManagementContextBoundInstance {

    private static final long serialVersionUID = 6994714264125928209L;

    public static final String CASE_MANAGEMENT_COMMENT_ACTIONS = "CASE_MANAGEMENT_COMMENT_ACTIONS";

    @In(create = true)
    protected transient CommentManagerActions commentManagerActions;

    protected void resetCurrentEmailCache(DocumentModel cachedEmail,
            DocumentModel newEmail) throws ClientException {
        commentManagerActions.documentChanged();
    }

    public String addComment() throws ClientException {
        return commentManagerActions.createComment(getCurrentCaseItem());
    }

    @Factory(value = "caseItemThreadedComments", scope = EVENT)
    public List<ThreadEntry> getCommentsAsThread() throws ClientException {
        return commentManagerActions.getCommentsAsThread(getCurrentCaseItem());
    }

    public List<Action> getActionsForComment() {
        return commentManagerActions.getActionsForComment(CASE_MANAGEMENT_COMMENT_ACTIONS);
    }

}
