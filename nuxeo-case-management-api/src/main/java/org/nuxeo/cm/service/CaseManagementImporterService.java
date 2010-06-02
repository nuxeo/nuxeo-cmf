package org.nuxeo.cm.service;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.ClientException;

public interface CaseManagementImporterService extends Serializable {
    
    public void importDocuments() throws ClientException;

}
