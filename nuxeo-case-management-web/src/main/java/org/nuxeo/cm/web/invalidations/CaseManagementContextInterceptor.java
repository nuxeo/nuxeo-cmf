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

package org.nuxeo.cm.web.invalidations;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.BijectionInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.nuxeo.cm.web.context.CaseManagementContextHolder;
import org.nuxeo.cm.web.context.CaseManagementContextHolderBean;


/**
 * Interceptor used to invalidate components annotated by
 * {@link CaseManagementContextBound}.
 *
 * Every method on this class
 */
@Interceptor(stateless = true, within = BijectionInterceptor.class)
public class CaseManagementContextInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CaseManagementContextInterceptor.class);

    public Object aroundInvoke(InvocationContext invocationContext)
            throws Exception {
        beforeInvocation(invocationContext);
        return invocationContext.proceed();
    }

    protected void beforeInvocation(InvocationContext invocationContext) {
        Object target = invocationContext.getTarget();
        for (Method meth : target.getClass().getMethods()) {
            if (meth.isAnnotationPresent(CaseManagementContextChecker.class)) {
                try {
                    meth.invoke(target, getCaseManagementContextHolder());
                } catch (Exception e) {
                    log.error("Error during Invalidation method call", e);
                }
            }
        }
    }

    protected CaseManagementContextHolder getCaseManagementContextHolder() {
        return (CaseManagementContextHolder) Component.getInstance(
                CaseManagementContextHolderBean.SEAM_COMPONENT_NAME,
                ScopeType.CONVERSATION);
    }

    @Override
    public boolean isInterceptorEnabled() {
        return true;
    }
}
