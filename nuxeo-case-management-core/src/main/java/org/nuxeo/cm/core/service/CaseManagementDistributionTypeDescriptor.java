package org.nuxeo.cm.core.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("distributionType")
public class CaseManagementDistributionTypeDescriptor {

    @XNode("name")
    protected String name;

    @XNode("allRecipientsProperty")
    protected String allRecipientsProperty;

    @XNode("externalRecipientsProperty")
    protected String externalRecipientsProperty;

    @XNode("internalRecipientsProperty")
    protected String internalRecipientsProperty;

}
