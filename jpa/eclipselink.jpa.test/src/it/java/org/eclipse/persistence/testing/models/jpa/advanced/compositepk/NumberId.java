/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.persistence.testing.models.jpa.advanced.compositepk;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;

@Embeddable
public class NumberId implements Serializable {

    private static final long serialVersionUID = 4263409730381544959L;

    @Column(name = "ID_NUMBER")
    @SequenceGenerator(name = "ID_SEQ", sequenceName = "ID_SEQUENCE", allocationSize = 50, initialValue = 1000)
    @GeneratedValue(generator = "ID_SEQ")
    private Long value;

    public NumberId() {
        this(0L);
    }

    public NumberId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public final boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return Objects.equals(this.value, ((NumberId) other).value);
    }

    public final int hashCode() {
        return Objects.hash(this.value);
    }

    public final String toString() {
        return getClass().getSimpleName() + "[value=" + this.value + "]";
    }
}
