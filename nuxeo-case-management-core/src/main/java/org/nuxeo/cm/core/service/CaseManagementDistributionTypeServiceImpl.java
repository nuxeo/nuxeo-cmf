/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nicolas Ulrich
 */
package org.nuxeo.cm.core.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.CaseManagementDistributionTypeService;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


/**
 * @author Nicolas Ulrich
 *
 */
public class CaseManagementDistributionTypeServiceImpl extends DefaultComponent
        implements CaseManagementDistributionTypeService {

    private static final long serialVersionUID = 1L;

    private static final int ALL_PROPERTY = 0;

    private static final int EXTERNAL_PROPERTY = 1;

    private static final int INTERNAL_PROPERTY = 2;

    private final Map<String, List<String>> values = new HashMap<String, List<String>>();

    @Override
    public void activate(ComponentContext context) throws Exception {
        // TODO Auto-generated method stub
        super.activate(context);
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {

        CaseManagementDistributionTypeDescriptor distributionType = ((CaseManagementDistributionTypeDescriptor) contribution);

        List<String> properties = Arrays.asList(
                distributionType.allRecipientsProperty,
                distributionType.externalRecipientsProperty,
                distributionType.internalRecipientsProperty);

        values.put(distributionType.name, properties);

    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {

        CaseManagementDistributionTypeDescriptor distributionType = ((CaseManagementDistributionTypeDescriptor) contribution);
        values.remove(distributionType.name);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nuxeo.correspondence.service.CorrespondenceDistributionTypeService
     * #getAllProperty(java.lang.String)
     */
    public String getAllProperty(String distributionType)
            throws CaseManagementException {

        checkValue(distributionType, ALL_PROPERTY);
        return values.get(distributionType).get(ALL_PROPERTY);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nuxeo.correspondence.service.CorrespondenceDistributionTypeService
     * #getDistributionTypes()
     */
    public Set<String> getDistributionTypes() {
        return values.keySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nuxeo.correspondence.service.CorrespondenceDistributionTypeService
     * #getExternalProperty(java.lang.String)
     */
    public String getExternalProperty(String distributionType)
            throws CaseManagementException {
        checkValue(distributionType, EXTERNAL_PROPERTY);
        return values.get(distributionType).get(EXTERNAL_PROPERTY);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.nuxeo.correspondence.service.CorrespondenceDistributionTypeService
     * #getInternalProperty(java.lang.String)
     */
    public String getInternalProperty(String distributionType)
            throws CaseManagementException {
        checkValue(distributionType, INTERNAL_PROPERTY);
        return values.get(distributionType).get(INTERNAL_PROPERTY);
    }

    protected void checkValue(String distributionType, int distributionProperty)
            throws CaseManagementException {

        if (!values.containsKey(distributionType)) {
            throw new CaseManagementException(
                    String.format("Unknow distribution type (%s). Check your DistributionTypeService contributions", distributionType));
        }

        if (values.get(distributionType).get(distributionProperty) == null) {
            throw new CaseManagementException(
                    String.format("'%s' property is undefined. Check your DistributionTypeService contributions", distributionProperty));
        }

    }

}
