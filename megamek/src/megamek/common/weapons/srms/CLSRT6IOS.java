/**
 * MegaMek - Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package megamek.common.weapons.srms;

import megamek.common.SimpleTechLevel;

/**
 * @author Sebastian Brocks
 */
public class CLSRT6IOS extends SRTWeapon {

    /**
     *
     */
    private static final long serialVersionUID = -4262996818773684373L;

    /**
     *
     */
    public CLSRT6IOS() {
        super();
        name = "SRT 6 (I-OS)";
        setInternalName("CLSRT6 (IOS)");
        addLookupName("Clan IOS SRT-6");
        addLookupName("Clan SRT 6 (IOS)");
        addLookupName("CLSRT6IOS");
        heat = 4;
        rackSize = 6;
        waterShortRange = 3;
        waterMediumRange = 6;
        waterLongRange = 9;
        waterExtremeRange = 12;
        tonnage = 1;
        criticals = 1;
        bv = 12;
        flags = flags.or(F_NO_FIRES).or(F_ONESHOT).andNot(F_PROTO_WEAPON);
        cost = 64000;
        rulesRefs = "327, TO";
        //Tech Progression tweaked to combine IntOps with TRO Prototypes/3145 NTNU RS
        techAdvancement.setTechBase(TechBase.CLAN)
        	.setIntroLevel(false)
        	.setUnofficial(false)
            .setTechRating(TechRating.B)
            .setAvailability(AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.F, AvailabilityValue.E)
            .setClanAdvancement(DATE_NONE, 3058, 3081, DATE_NONE, DATE_NONE)
            .setClanApproximate(false, false, true, false, false)
            .setPrototypeFactions(Faction.CNC)
            .setProductionFactions(Faction.CNC)
            .setStaticTechLevel(SimpleTechLevel.STANDARD);
    }
}
