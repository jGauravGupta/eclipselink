/*
 * Copyright (c) 1998, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     James Sutherland - initial impl
package org.eclipse.persistence.testing.tests.jpa.jpql;

import java.util.List;

import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.QueryRedirector;
import org.eclipse.persistence.sessions.DataRecord;
import org.eclipse.persistence.sessions.Session;

public class ArrayQueryRedirector implements QueryRedirector {
    @Override
    public Object invokeQuery(DatabaseQuery query, DataRecord dataRecord, Session session) {
        List result = (List)session.executeQuery(query);
        return result.toArray(new Object[result.size()]);
    }
}
