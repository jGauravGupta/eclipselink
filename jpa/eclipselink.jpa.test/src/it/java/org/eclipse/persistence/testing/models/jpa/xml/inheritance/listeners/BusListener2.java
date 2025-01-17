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
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.testing.models.jpa.xml.inheritance.listeners;

import org.eclipse.persistence.testing.models.jpa.xml.inheritance.Bus;

/**
 * A listener for the Bus entity.
 *
 * It implements the following annotations:
 * - None
 *
 * It overrides the following annotations:
 * - PrePersist from ListenerSuperclass
 * - PostPersist from FueledVehicleListener
 *
 * It inherits the following annotations:
 * - PostLoad from Vehicle.
 */
public class BusListener2 extends ListenerSuperclass {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;

    @Override
    public void prePersist(Object bus) {
        PRE_PERSIST_COUNT++;
        ((Bus) bus).addPrePersistCalledListener(this.getClass());
    }

    public void postPersist(Object bus) {
        POST_PERSIST_COUNT++;
        ((Bus) bus).addPostPersistCalledListener(this.getClass());
    }
}
