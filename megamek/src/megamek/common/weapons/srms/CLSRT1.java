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
package megamek.common.weapons.srms;

/**
 * @author Sebastian Brocks
 */
public class CLSRT1 extends SRTWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -6695135847410932636L;

    /**
     *
     */
    public CLSRT1() {
        super();
        name = "SRT 1";
        setInternalName("CLSRT1");
        addLookupName("Clan SRT-1");
        addLookupName("Clan SRT 1");
        addLookupName("CLSRT1");
        rackSize = 1;
        waterShortRange = 3;
        waterMediumRange = 6;
        waterLongRange = 9;
        waterExtremeRange = 12;
        tonnage = 0.25;
        criticals = 1;
        bv = 15;
        flags = flags.or(F_NO_FIRES).andNot(F_AERO_WEAPON).andNot(F_BA_WEAPON)
        		.andNot(F_MEK_WEAPON).andNot(F_TANK_WEAPON);
        cost = 10000;
        rulesRefs = "230, TM";
        techAdvancement.setTechBase(TechBase.CLAN)
        	.setIntroLevel(false)
        	.setUnofficial(false)
            .setTechRating(TechRating.C)
            .setAvailability(AvailabilityValue.X, AvailabilityValue.C, AvailabilityValue.C, AvailabilityValue.C)
            .setClanAdvancement(2820, 2824, 2825, DATE_NONE, DATE_NONE)
            .setClanApproximate(true, false, false,false, false)
            .setPrototypeFactions(Faction.CSF)
            .setProductionFactions(Faction.CSF);
    }
}
