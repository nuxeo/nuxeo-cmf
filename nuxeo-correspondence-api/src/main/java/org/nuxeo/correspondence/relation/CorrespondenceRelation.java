/*
 * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     ldoguin
 *
 * $Id$
 */

package org.nuxeo.correspondence.relation;

import java.util.List;

/**
 * Correspondence Relation interface
 * 
 * @author ldoguin
 */
public interface CorrespondenceRelation {

    /**
     * 
     * @return the list of correpsondenceStatement from
     *         isEnvelopeOfResourceRelation metadata.
     */
    List<CorrespondenceStatement> getIsEnvelopeOfResourceRelation();

    /**
     * Remove all isEnvelopeOfResource relation.
     */
    void resetIsEnvelopeOfResourceRelation();

    /**
     * Add the given statement to the isEnvelopeOfResource relation
     * 
     * @param CorrespondenceStatement
     */
    void addIsEnvelopeOfResourceRelation(CorrespondenceStatement stmt);

    /**
     * Add the list of given statement to the isEnvelopeOfResource relation.
     * 
     * @param relations
     */
    void addAllIsEnvelopeOfResourceRelation(
            List<CorrespondenceStatement> relations);

    /**
     * 
     @return the list of correpsondenceStatement from isAnswerTo metadata.
     */
    List<CorrespondenceStatement> getEmailIsAnswerToRelation();

    /**
     * Add the given statement to the isAnswerTo relation.
     * 
     * @param stmt
     */
    void addEmailIsAnswerToRelation(CorrespondenceStatement stmt);

    /**
     * Add the list of given statement to the isAnswerTo relation.
     * 
     * @param relations
     */
    void addAllEmailIsAnswerToRelation(List<CorrespondenceStatement> relations);
}