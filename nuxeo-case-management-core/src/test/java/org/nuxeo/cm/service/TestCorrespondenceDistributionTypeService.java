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

import junit.framework.Assert;

import org.nuxeo.cm.test.CorrespondenceRepositoryTestCase;
import org.nuxeo.cm.test.CaseManagementTestConstants;
import org.nuxeo.ecm.core.api.ClientException;


/**
 * @author Nicolas Ulrich
 *
 */
public class TestCorrespondenceDistributionTypeService extends
        CorrespondenceRepositoryTestCase {

    private static final String FOR_ACTION = "FOR_ACTION";

    private static final String FOR_INFORMATION = "FOR_INFORMATION";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        deployContrib(
                CaseManagementTestConstants.CASE_MANAGEMENT_CORE_TEST_BUNDLE,
                "test-distribution-type-with-error-corresp-contrib.xml");
        openSession();
    }

    public void testGetDistributionTypes() {
        Set<String> types = correspDistributionTypeService.getDistributionTypes();
        assertTrue(types.contains(FOR_INFORMATION));
        assertTrue(types.contains(FOR_ACTION));
    }

    public void testGetAllProperty() throws ClientException {
        assertEquals("all_action_recipient_mailboxes",
                correspDistributionTypeService.getAllProperty(FOR_ACTION));

        assertEquals("all_copy_recipient_mailboxes",
                correspDistributionTypeService.getAllProperty(FOR_INFORMATION));
    }

    public void testGetExternalProperty() throws ClientException {
        assertEquals("initial_action_external_recipient_mailboxes",
                correspDistributionTypeService.getExternalProperty(FOR_ACTION));

        assertEquals(
                "initial_copy_external_recipient_mailboxes",
                correspDistributionTypeService.getExternalProperty(FOR_INFORMATION));
    }

    public void testGetInternalProperty() throws ClientException {

        assertEquals("initial_action_internal_recipient_mailboxes",
                correspDistributionTypeService.getInternalProperty(FOR_ACTION));

        assertEquals(
                "initial_copy_internal_recipient_mailboxes",
                correspDistributionTypeService.getInternalProperty(FOR_INFORMATION));
    }

    public void testDistributionTypeError() {
        try {
            correspDistributionTypeService.getInternalProperty("FAKE");
            Assert.fail("No Exception throwed");
        } catch (ClientException e) {
        }
    }

    public void testPropertyError() {
        try {
            correspDistributionTypeService.getInternalProperty("TEST_ERROR");
            Assert.fail("No Exception throwed");
        } catch (ClientException e) {
        }
    }
}