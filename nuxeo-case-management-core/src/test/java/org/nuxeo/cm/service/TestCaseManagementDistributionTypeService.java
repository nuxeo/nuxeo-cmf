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

import java.util.Set;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.cm.test.CaseManagementRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;

/**
 * @author Nicolas Ulrich
 */
public class TestCaseManagementDistributionTypeService extends CaseManagementRepositoryTestCase {

    private static final String FOR_ACTION = "FOR_ACTION";

    private static final String FOR_INFORMATION = "FOR_INFORMATION";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployContrib(CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-distribution-type-with-error-corresp-contrib.xml");
        openSession();
    }

    @After
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }

    @Test
    public void testGetDistributionTypes() {
        Set<String> types = correspDistributionTypeService.getDistributionTypes();
        assertTrue(types.contains(FOR_INFORMATION));
        assertTrue(types.contains(FOR_ACTION));
    }

    @Test
    public void testGetAllProperty() throws ClientException {
        assertEquals("all_action_participant_mailboxes", correspDistributionTypeService.getAllProperty(FOR_ACTION));
        assertEquals("all_copy_participant_mailboxes", correspDistributionTypeService.getAllProperty(FOR_INFORMATION));
    }

    @Test
    public void testGetExternalProperty() throws ClientException {
        assertEquals("initial_action_external_participant_mailboxes",
                correspDistributionTypeService.getExternalProperty(FOR_ACTION));
        assertEquals("initial_copy_external_participant_mailboxes",
                correspDistributionTypeService.getExternalProperty(FOR_INFORMATION));
    }

    @Test
    public void testGetInternalProperty() throws ClientException {
        assertEquals("initial_action_internal_participant_mailboxes",
                correspDistributionTypeService.getInternalProperty(FOR_ACTION));
        assertEquals("initial_copy_internal_participant_mailboxes",
                correspDistributionTypeService.getInternalProperty(FOR_INFORMATION));
    }

    @Test
    public void testDistributionTypeError() {
        try {
            correspDistributionTypeService.getInternalProperty("FAKE");
            fail("No Exception throwed");
        } catch (ClientException e) {
        }
    }

    @Test
    public void testPropertyError() {
        try {
            correspDistributionTypeService.getInternalProperty("TEST_ERROR");
            fail("No Exception throwed");
        } catch (ClientException e) {
        }
    }

}
