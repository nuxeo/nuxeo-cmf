package org.nuxeo.cm.operation;

import java.util.List;

import org.nuxeo.cm.caselink.ActionableCaseLink;
import org.nuxeo.cm.caselink.CaseLink;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.service.CaseDistributionService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

@Operation(id = ApproveCaseLinkOperation.ID, category = CaseConstants.CASE_MANAGEMENT_OPERATION_CATEGORY, label = "Approve Case Links from Mailboxes", description = "Approve case link; this operation takes as an input a list of docs where the first doc is the mailbox and the second is the case to approve")
public class ApproveCaseLinkOperation {

    public static final String ID = "Case.Management.Approve.CaseLink";

    @Context
    protected OperationContext context;

    @Context
    protected CoreSession session;

    @Context
    protected CaseDistributionService caseDistributionService;

    @OperationMethod
    public DocumentModel approveCaseLink(DocumentModelList docs) {
        DocumentModel mailboxDoc = docs.get(0);
        DocumentModel kaseDoc = docs.get(1);
        Case kase = kaseDoc.getAdapter(Case.class);
        Mailbox mailbox = mailboxDoc.getAdapter(Mailbox.class);
        if (kase == null || mailbox == null) {
            return null;
        }
        List<CaseLink> caseLinks = caseDistributionService.getCaseLinks(
                session, mailbox, kase);
        for (CaseLink caseLink : caseLinks) {
            ActionableCaseLink acl = (caseLink.getDocument()).getAdapter(ActionableCaseLink.class);
            acl.validate(session);
        }
        return mailboxDoc;
    }
}
