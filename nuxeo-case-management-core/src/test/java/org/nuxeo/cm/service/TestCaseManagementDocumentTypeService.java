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
 *
 * $Id$
 */

package org.nuxeo.cm.service;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.ecm.core.api.ClientException;

/**
 * @author Nicolas Ulrich
 */
public class TestCaseManagementDocumentTypeService extends CaseManagementRepositoryTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetAllProperty() throws ClientException {
        assertEquals("Case", correspDocumentTypeService.getCaseType());
        assertEquals("CaseLink", correspDocumentTypeService.getCaseLinkType());
    }

}
