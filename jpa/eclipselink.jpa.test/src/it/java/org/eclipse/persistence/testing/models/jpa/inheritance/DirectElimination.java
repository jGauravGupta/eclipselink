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
//     12/12/2008-1.1 Guy Pelletier
//       - 249860: Implement table per class inheritance support.
package org.eclipse.persistence.testing.models.jpa.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.IdValidation;
import org.eclipse.persistence.annotations.PrimaryKey;

@Entity
@Table(name="TPC_DIR_ELIMINATION")
@PrimaryKey(validation=IdValidation.NULL)
public class DirectElimination extends Elimination {
    @OneToOne
    @JoinColumn(name="WEAPON_ID")
    private DirectWeapon directWeapon;

    public DirectElimination() {}

    public DirectWeapon getDirectWeapon() {
        return directWeapon;
    }

    @Override
    public boolean isDirectElimination() {
        return true;
    }

    @Override
    public void setAssassin(Assassin assassin) {
        super.setAssassin(assassin);
        setDirectWeapon((DirectWeapon) assassin.getWeapon());
    }

    public void setDirectWeapon(DirectWeapon directWeapon) {
        this.directWeapon = directWeapon;
    }
}

