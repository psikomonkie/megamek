/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
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
package megamek.common.actions;

import megamek.common.BipedMek;
import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.Hex;
import megamek.common.Mek;
import megamek.common.Terrains;
import megamek.common.TripodMek;
import megamek.common.enums.BuildingType;
import megamek.common.options.OptionsConstants;

import java.io.Serial;

/**
 * The entity tries to find a club.
 *
 * @author Ben
 * @since April 5, 2002, 4:00 PM
 */
public class FindClubAction extends AbstractEntityAction {
    @Serial
    private static final long serialVersionUID = -8948591442556777640L;

    public FindClubAction(int entityId) {
        super(entityId);
    }

    /**
     * @param game The current {@link Game}
     * @return whether an entity can find a club in its current location
     */
    public static boolean canMekFindClub(Game game, int entityId) {
        final Entity entity = game.getEntity(entityId);
        // Only biped and tripod 'Meks qualify at all.
        if (!(entity instanceof BipedMek || entity instanceof TripodMek)) {
            return false;
        }

        if (!game.hasBoardLocation(entity.getBoardLocation()) || entity.isShutDown() || !entity.getCrew().isActive()) {
            return false;
        }

        // Check game options
        if (game.getOptions().booleanOption(OptionsConstants.ALLOWED_NO_CLAN_PHYSICAL)
                && entity.getCrew().isClanPilot()) {
            return false;
        }

        final Hex hex = game.getHex(entity.getBoardLocation());
        // The hex must contain woods or rubble from a medium, heavy, or hardened building, or a blown off limb
        if ((hex.terrainLevel(Terrains.WOODS) < 1)
            && (hex.terrainLevel(Terrains.JUNGLE) < 1)
            && (hex.terrainLevel(Terrains.RUBBLE) < BuildingType.MEDIUM.getTypeValue())
            && (hex.terrainLevel(Terrains.ARMS) < 1)
            && (hex.terrainLevel(Terrains.LEGS) < 1)) {
            return false;
        }

        // also, need shoulders and hands; Claws can substitute as hands --Torren
        if (!entity.hasWorkingSystem(Mek.ACTUATOR_SHOULDER, Mek.LOC_RARM)
                || !entity.hasWorkingSystem(Mek.ACTUATOR_SHOULDER, Mek.LOC_LARM)
                || (!entity.hasWorkingSystem(Mek.ACTUATOR_HAND, Mek.LOC_RARM) && !((Mek) entity).hasClaw(Mek.LOC_RARM))
                || (!entity.hasWorkingSystem(Mek.ACTUATOR_HAND, Mek.LOC_LARM) && !((Mek) entity).hasClaw(Mek.LOC_LARM))) {
            return false;
        }

        // check for no/minimal arms quirk
        if (entity.hasQuirk(OptionsConstants.QUIRK_NEG_NO_ARMS)) {
            return false;
        }

        // and last, check if you already have a club, greedy
        return entity.getClubs().isEmpty();
    }
}
