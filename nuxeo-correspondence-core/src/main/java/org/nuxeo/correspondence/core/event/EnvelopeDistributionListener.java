package org.nuxeo.correspondence.core.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseLifeCycleConstants;
import org.nuxeo.cm.event.CaseManagementEventConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Follow the send transition for a case after its initial distribution.
 * 
 * @author ldoguin
 */
public class EnvelopeDistributionListener implements EventListener {

    public static final Log log = LogFactory.getLog(EnvelopeDistributionListener.class);

    public void handleEvent(Event event) throws ClientException {
        DocumentEventContext docCtx = null;
        if (event.getContext() instanceof DocumentEventContext) {
            docCtx = (DocumentEventContext) event.getContext();
        } else {
            return;
        }
        DocumentModel dm = docCtx.getSourceDocument();
        Boolean isInitial = (Boolean) docCtx.getProperty(CaseManagementEventConstants.EVENT_CONTEXT_IS_INITIAL);
        Case env = dm.getAdapter(Case.class);
        if (env == null) {
            return;
        }
        if (isInitial) {
            // Update the lifecycle of the envelope
            env.getDocument().followTransition(CaseLifeCycleConstants.TRANSITION_SEND);
        }
    }

}
