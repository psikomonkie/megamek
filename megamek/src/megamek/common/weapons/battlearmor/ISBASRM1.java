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

import megamek.common.weapons.srms.SRMWeapon;

/**
 * @author Sebastian Brocks
 */
public class ISBASRM1 extends SRMWeapon {
    private static final long serialVersionUID = -3895466103659984643L;

    public ISBASRM1() {
        super();
        name = "SRM 1";
        setInternalName("ISBASRM1");
        addLookupName("IS BA SRM-1");
        addLookupName("IS BA SRM 1");
        rackSize = 1;
        shortRange = 3;
        mediumRange = 6;
        longRange = 9;
        extremeRange = 12;
        bv = 15;
        cost = 5000;
        tonnage = 0.06;
        criticals = 1;
        flags = flags.or(F_NO_FIRES).or(F_BA_WEAPON).andNot(F_MEK_WEAPON).andNot(F_TANK_WEAPON)
                .andNot(F_AERO_WEAPON).andNot(F_PROTO_WEAPON);
        rulesRefs = "261, TM";
        techAdvancement.setTechBase(TechBase.IS)
                .setIntroLevel(false)
                .setUnofficial(false)
                .setTechRating(TechRating.E)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.D, AvailabilityValue.B)
                .setISAdvancement(3050, 3050, 3051, DATE_NONE, DATE_NONE)
                .setISApproximate(true, false, false, false, false)
                .setPrototypeFactions(Faction.FS, Faction.LC)
                .setProductionFactions(Faction.FS, Faction.LC);
    }
}
