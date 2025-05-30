/*
 * MegaMek - Copyright (C) 2004, 2005 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package megamek.common.weapons.lasers;

import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.Mounted;

/**
 * @author Andrew Hunter
 * @since Sep 12, 2004
 */
public class CLERLaserMicro extends LaserWeapon {
    private static final long serialVersionUID = -445880139385652098L;

    public CLERLaserMicro() {
        super();
        name = "ER Micro Laser";
        setInternalName("CLERMicroLaser");
        addLookupName("Clan ER Micro Laser");
        sortingName = "Laser ER A";
        heat = 1;
        damage = 2;
        shortRange = 1;
        mediumRange = 2;
        longRange = 4;
        extremeRange = 6;
        waterShortRange = 1;
        waterMediumRange = 2;
        waterLongRange = 2;
        waterExtremeRange = 2;
        tonnage = 0.25;
        criticals = 1;
        flags = flags.or(F_NO_FIRES);
        bv = 7;
        cost = 10000;
        shortAV = 2;
        maxRange = RANGE_SHORT;
        rulesRefs = "226, TM";
        techAdvancement.setTechBase(TechBase.CLAN)
                .setIntroLevel(false)
                .setUnofficial(false)
                .setTechRating(TechRating.F)
                .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.D, AvailabilityValue.C)
                .setClanAdvancement(3059, 3060, 3061, DATE_NONE, DATE_NONE)
                .setClanApproximate(true, false, false, false, false)
                .setPrototypeFactions(Faction.CSJ)
                .setProductionFactions(Faction.CSJ);
    }

    @Override
    public boolean isAlphaStrikePointDefense() {
        return true;
    }

    @Override
    public double getBattleForceDamage(int range, Mounted<?> fcs) {
        return (range <= AlphaStrikeElement.SHORT_RANGE) ? 0.2 : 0;
    }
}
