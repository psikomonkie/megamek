/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package megamek.common.planetaryconditions;

import megamek.common.Coords;
import megamek.common.Game;
import megamek.common.Hex;
import megamek.common.Terrains;

import java.util.Objects;

public enum IlluminationLevel {

    NONE(""),
    FIRE("\uD83D\uDD25"),
    FLARE("\uD83C\uDF86"),
    SEARCHLIGHT("\uD83D\uDD26");

    private final String indicator;

    IlluminationLevel(final String indicator) {
        this.indicator = indicator;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isFire() {
        return this == FIRE;
    }

    public boolean isFlare() {
        return this == FLARE;
    }

    public boolean isSearchlight() {
        return this == SEARCHLIGHT;
    }

    public String getIndicator() {
        return indicator;
    }

    // LEGACY replace with board ID version
    public static IlluminationLevel determineIlluminationLevel(final Game game, final Coords coords) {
        return determineIlluminationLevel(game, 0, coords);
    }

    /**
     * @return the level of illumination for a given coords. Different light sources affect how much the nighttime
     *       penalties are reduced. Note: this method should be used for determining if a Coords/Hex is illuminated, not
     *       Game::getIlluminatedPositions, as that just returns the hexes that are effected by searchlights, whereas
     *       this one considers searchlights as well as other light sources.
     */
    public static IlluminationLevel determineIlluminationLevel(final Game game, int boardId, final Coords coords) {
        // fix for NPE when recovering spacecraft while in visual range of enemy
        if (game.getBoard(boardId).isSpace()) {
            return IlluminationLevel.NONE;
        }

        // Flares happen first, because they totally negate nighttime penalties
        if (game.getFlares().stream().anyMatch(flare -> flare.illuminates(coords))) {
            return IlluminationLevel.FLARE;
        }

        // Searchlights reduce nighttime penalties by up to 3 points.
        if (game.getIlluminatedPositions().contains(coords)) {
            return IlluminationLevel.SEARCHLIGHT;
        }

        // Fires can reduce nighttime penalties by up to 2 points.
        final Hex hex = game.getBoard(boardId).getHex(coords);
        if ((hex != null) && hex.containsTerrain(Terrains.FIRE)) {
            return IlluminationLevel.FIRE;
        }

        // If we are adjacent to a burning hex, we are also illuminated
        final boolean neighbouringFire = coords.allAdjacent().stream()
                .map(adjacent -> game.getBoard(boardId).getHex(adjacent))
                .filter(Objects::nonNull)
                .anyMatch(adjacent -> adjacent.containsTerrain(Terrains.FIRE));

        return neighbouringFire ? IlluminationLevel.FIRE : IlluminationLevel.NONE;
    }
}
