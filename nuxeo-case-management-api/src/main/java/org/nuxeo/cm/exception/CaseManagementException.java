/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: DistributionException.java 53449 2008-03-04 14:29:59Z gracinet $
 */

package org.nuxeo.cm.exception;

import org.nuxeo.ecm.core.api.ClientException;

/**
 * Distribution exception
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */
public class CaseManagementException extends ClientException {

    private static final long serialVersionUID = 5579991054260392177L;


    public CaseManagementException() {
    }

    public CaseManagementException(String message, Throwable th) {
        super(message, th);
    }

    public CaseManagementException(String message) {
        super(message);
    }

    public CaseManagementException(Throwable th) {
        super(th);
    }

}
