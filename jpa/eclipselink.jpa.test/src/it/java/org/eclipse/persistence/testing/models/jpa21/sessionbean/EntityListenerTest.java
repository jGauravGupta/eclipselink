/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
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
//     tware - initial contribution for Bug 366748 - JPA 2.1 Injectable Entity Listeners
package org.eclipse.persistence.testing.models.jpa21.sessionbean;

import jakarta.ejb.Remote;

@Remote
public interface EntityListenerTest {

    boolean triggerInjection();

    boolean triggerPreDestroy();
}
