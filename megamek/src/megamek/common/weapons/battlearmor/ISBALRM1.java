/*
 * Copyright (c) 2005 - Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common.weapons.battlearmor;

import megamek.common.weapons.lrms.LRMWeapon;


/**
 * @author Sebastian Brocks
 */
public class ISBALRM1 extends LRMWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -5976936994611000430L;

    /**
     *
     */
    public ISBALRM1() {
        super();
        name = "LRM 1";
        setInternalName("ISBALRM1");
        addLookupName("IS BA LRM-1");
        addLookupName("IS BA LRM 1");
        rackSize = 1;
        minimumRange = 6;
        bv = 14;
        cost = 6000;
        tonnage = .06;
        criticals = 2;
        flags = flags.or(F_NO_FIRES).or(F_BA_WEAPON).andNot(F_MEK_WEAPON).andNot(F_TANK_WEAPON).andNot(F_AERO_WEAPON).andNot(F_PROTO_WEAPON);
        rulesRefs = "261, TM";
        techAdvancement.setTechBase(TechBase.IS)
    	.setIntroLevel(false)
    	.setUnofficial(false)
        .setTechRating(TechRating.E)
        .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.E, AvailabilityValue.D)
        .setISAdvancement(3055, 3057, 3060, DATE_NONE, DATE_NONE)
        .setISApproximate(true, false, false, false, false)
        .setPrototypeFactions(Faction.FS)
        .setProductionFactions(Faction.FS);
    }
}
