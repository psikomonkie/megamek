/*
 * MegaAero - Copyright (C) 2007 Jay Lawson
 * Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MegaMek was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */
package megamek.common;

import java.io.Serial;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import megamek.client.ui.clientGUI.calculationReport.CalculationReport;
import megamek.common.cost.DropShipCostCalculator;
import megamek.common.equipment.AmmoMounted;
import megamek.common.equipment.WeaponMounted;
import megamek.common.options.OptionsConstants;
import megamek.common.planetaryconditions.Atmosphere;
import megamek.common.planetaryconditions.PlanetaryConditions;
import megamek.common.util.ConditionalStringJoiner;

/**
 * @author Jay Lawson
 * @since Jun 17, 2007
 */
public class Dropship extends SmallCraft {

    @Serial
    private static final long serialVersionUID = 1528728632696989565L;

    // ASEW Missile Effects, per location
    // Values correspond to Locations: NOS, Left, Right, AFT
    private final int[] asewAffectedTurns = { 0, 0, 0, 0 };

    /**
     * Sets the number of rounds a specified firing arc is affected by an ASEW missile
     * @param arc - integer representing the desired firing arc
     * @param turns - integer specifying the number of end phases that the effects last through
     * Technically, about 1.5 turns elapse per the rules for ASEW missiles in TO
     */
    public void setASEWAffected(int arc, int turns) {
        if (arc < asewAffectedTurns.length) {
            asewAffectedTurns[arc] = turns;
        }
    }

    /**
     * Returns the number of rounds a specified firing arc is affected by an ASEW missile
     * @param arc - integer representing the desired firing arc
     */
    public int getASEWAffected(int arc) {
        if (arc < asewAffectedTurns.length) {
            return asewAffectedTurns[arc];
        }
        return 0;
    }

    /**
     * Primitive DropShips may be constructed with no docking collar, or with a pre-boom collar.
     */
    public static final int COLLAR_STANDARD  = 0;
    public static final int COLLAR_PROTOTYPE = 1;
    public static final int COLLAR_NO_BOOM   = 2;

    private static final String[] COLLAR_NAMES = {
            "KF-Boom", "Prototype KF-Boom", "No Boom"
    };

    // Likewise, you can have a prototype or standard K-F Boom
    public static final int BOOM_STANDARD  = 0;
    public static final int BOOM_PROTOTYPE = 1;

    // what needs to go here?
    // loading and unloading of units?
    private boolean dockCollarDamaged = false;
    private boolean kfBoomDamaged = false;
    private int collarType = COLLAR_STANDARD;
    private int boomType = BOOM_STANDARD;

    @Override
    public boolean tracksHeat() {
        // While large craft perform heat calculations, they are not considered heat-tracking units
        // because they cannot generate more heat than they can dissipate in the same turn.
        return false;
    }

    @Override
    public int getUnitType() {
        return UnitType.DROPSHIP;
    }

    @Override
    public boolean isSmallCraft() {
        return false;
    }

    @Override
    public boolean isDropShip() {
        return true;
    }

    @Override
    public CrewType defaultCrewType() {
        return CrewType.VESSEL;
    }

    //Docking Collar Stuff
    public boolean isDockCollarDamaged() {
        return dockCollarDamaged;
    }

    public int getCollarType() {
        return collarType;
    }

    public void setCollarType(int collarType) {
        this.collarType = collarType;
    }

    public String getCollarName() {
        return COLLAR_NAMES[collarType];
    }

    public static String getCollarName(int type) {
        return COLLAR_NAMES[type];
    }

    public static TechAdvancement getCollarTA() {
        return new TechAdvancement(TechBase.ALL).setAdvancement(2458, 2470, 2500)
                .setPrototypeFactions(Faction.TH).setProductionFactions(Faction.TH).setTechRating(TechRating.C)
                .setAvailability(AvailabilityValue.C, AvailabilityValue.C, AvailabilityValue.C, AvailabilityValue.C)
                .setStaticTechLevel(SimpleTechLevel.STANDARD);
    }

    //KF Boom Stuff
    public boolean isKFBoomDamaged() {
        return kfBoomDamaged;
    }

    public int getBoomType() {
        return boomType;
    }

    public void setBoomType(int boomType) {
        this.boomType = boomType;
    }

    @Override
    public String getCritDamageString() {
        ConditionalStringJoiner conditionalStringJoiner = new ConditionalStringJoiner();
        conditionalStringJoiner.add(super.getCritDamageString());
        conditionalStringJoiner.add(isDockCollarDamaged(), () -> Messages.getString("Dropship.collarDamageString"));
        conditionalStringJoiner.add(isKFBoomDamaged(), () -> Messages.getString("Dropship.kfBoomDamageString"));
        return conditionalStringJoiner.toString();
    }

    @Override
    public boolean isLocationProhibited(Coords c, int testBoardId, int currElevation) {
        if (!game.hasBoardLocation(c, testBoardId)) {
            return true;
        }

        Hex hex = game.getHex(c, testBoardId);
        if (currElevation != 0) {
            return hex.containsTerrain(Terrains.IMPASSABLE);
        }
        // Check prohibited terrain
        // treat grounded Dropships like wheeled tanks,
        // plus buildings are prohibited
        boolean isProhibited = taxingAeroProhibitedTerrains(hex);
        // Also check for any crushable entities
        var currentEntitiesIter = game.getEntities(c);
        while (currentEntitiesIter.hasNext()) {
            Entity entity = currentEntitiesIter.next();
            isProhibited = isProhibited || !this.equals(entity);
            if (isProhibited) {
                return true;
            }
        }

        Map<Integer, Integer> elevations = new HashMap<>();
        elevations.put(hex.getLevel(), 1);
        boolean secondaryHexPresent;
        Coords secondaryCoords;
        for (int dir = 0; dir < 6; dir++) {
            secondaryCoords = c.translated(dir);
            Hex secondaryHex = game.getBoard(testBoardId).getHex(secondaryCoords);
            currentEntitiesIter = game.getEntities(secondaryCoords);
            secondaryHexPresent = secondaryHex != null;
            isProhibited = isProhibited || !secondaryHexPresent || taxingAeroProhibitedTerrains(secondaryHex);
            while (!isProhibited && currentEntitiesIter.hasNext()) {
                Entity entity = currentEntitiesIter.next();
                isProhibited = !this.equals(entity);
            }

            if (secondaryHexPresent) {
                int elev = secondaryHex.getLevel();
                if (elevations.containsKey(elev)) {
                    elevations.put(elev, elevations.get(elev) + 1);
                } else {
                    elevations.put(elev, 1);
                }
            }
        }
        /*
         * As of 8/2013 there aren't clear restrictions for landed dropships. We
         * are going to assume that Dropships need to be on fairly level
         * terrain. This means, it can only be on at most 2 different elevations
         * that are at most 1 elevation apart. Additionally, at least half of
         * the dropships hexes round down must be on one elevation
         */
        // Whole DS is on one elevation
        if (elevations.size() == 1) {
            return isProhibited;
        }
        // DS on more than 2 different elevations
        // or not on an elevation, what?
        if ((elevations.size() > 2) || elevations.isEmpty()) {
            return true;
        }

        final Integer[] elevationsKeys = new Integer[elevations.size()];
        elevations.keySet().toArray(elevationsKeys);
        int elevDifference = Math.abs(elevationsKeys[0] - elevationsKeys[1]);
        int elevMinCount = 2;
        // Check elevation difference and make sure that the counts of different
        // elevations will allow for a legal deployment to exist
        // TODO: get updated ruling; this code causes a hex with one single lower- or higher-level neighbor
        // to be disqualified, but it would seem that a single lower-level neighbor should be fine.
        if ((elevDifference > 1) || (elevations.get(elevationsKeys[0]) < elevMinCount)
                || (elevations.get(elevationsKeys[1]) < elevMinCount)) {
            return true;
        }

        // It's still possible we have a legal deployment, we now have to check
        // the arrangement of hexes
        // The way this is done is we start at the hex directly above the
        // central hex and then move around clockwise and compare the two hexes
        // to see if they share an elevation. We need to have a number of these
        // adjacency equal to the number of secondary elevation hexes - 1.
        int numAdjacencies = 0;
        int centralElev = hex.getLevel();
        int secondElev = centralElev;
        Hex currHex = game.getBoard(testBoardId).getHex(c.translated(5));
        // Ensure we aren't trying to deploy off the board
        if (currHex == null) {
            return true;
        }
        for (int dir = 0; dir < 6; dir++) {
            if (currHex.getLevel() != centralElev) {
                secondElev = currHex.getLevel();
            }
            Hex nextHex = game.getBoard(testBoardId).getHex(c.translated(dir));
            // Ensure we aren't trying to deploy off the board
            if (nextHex == null) {
                return true;
            }
            if ((currHex.getLevel() != centralElev) && (currHex.getLevel() == nextHex.getLevel())) {
                numAdjacencies++;
            }
            currHex = nextHex;
        }
        if (numAdjacencies < (elevations.get(secondElev) - 1)) {
            return true;
        }

        return isProhibited;
    }

    public void setDamageDockCollar(boolean b) {
        dockCollarDamaged = b;
    }

    public void setDamageKFBoom(boolean b) {
        kfBoomDamaged = b;
    }

    @Override
    public double getFuelPointsPerTon() {
        double ppt;
        if (getWeight() < 400) {
            ppt = 80;
        } else if (getWeight() < 800) {
            ppt = 70;
        } else if (getWeight() < 1200) {
            ppt = 60;
        } else if (getWeight() < 1900) {
            ppt = 50;
        } else if (getWeight() < 3000) {
            ppt = 40;
        } else if (getWeight() < 20000) {
            ppt = 30;
        } else if (getWeight() < 40000) {
            ppt = 20;
        } else {
            ppt = 10;
        }
        if (isPrimitive()) {
            return ppt / primitiveFuelFactor();
        }
        return ppt;
    }

    @Override
    public double getStrategicFuelUse() {
        double fuelUse = 1.84; // default for military designs and civilian < 1000
        if ((getDesignType() == CIVILIAN) || isPrimitive()) {
            if (getWeight() >= 70000) {
                fuelUse = 8.83;
            } else if (getWeight() >= 50000) {
                fuelUse = 8.37;
            } else if (getWeight() >= 40000) {
                fuelUse = 7.71;
            } else if (getWeight() >= 30000) {
                fuelUse = 6.52;
            } else if (getWeight() >= 20000) {
                fuelUse = 5.19;
            } else if (getWeight() >= 9000) {
                fuelUse = 4.22;
            } else if (getWeight() >= 4000) {
                fuelUse = 2.82;
            }
        }
        if (isPrimitive()) {
            return fuelUse * primitiveFuelFactor();
        }
        return fuelUse;
    }

    @Override
    public double primitiveFuelFactor() {
        int year = getOriginalBuildYear();
        if (year >= 2500) {
            return 1.0;
        } else if (year >= 2400) {
            return 1.1;
        } else if (year >= 2351) {
            return 1.3;
        } else if (year >= 2251) {
            return 1.4;
        } else if (year >= 2201) {
            return 1.6;
        } else if (year >= 2151) {
            return 1.8;
        } else {
            return 2.0;
        }
    }

    protected static final TechAdvancement TA_DROPSHIP = new TechAdvancement(TechBase.ALL)
            .setAdvancement(DATE_NONE, 2470, 2490).setISApproximate(false, true, false)
            .setProductionFactions(Faction.TH).setTechRating(TechRating.D)
            .setAvailability(AvailabilityValue.D, AvailabilityValue.E, AvailabilityValue.D, AvailabilityValue.D)
            .setStaticTechLevel(SimpleTechLevel.STANDARD);
    protected static final TechAdvancement TA_DROPSHIP_PRIMITIVE = new TechAdvancement(TechBase.IS)
            .setISAdvancement(DATE_ES, 2200, DATE_NONE, 2500)
            .setISApproximate(false, true, false, false)
            .setProductionFactions(Faction.TA).setTechRating(TechRating.D)
            .setAvailability(AvailabilityValue.D, AvailabilityValue.X, AvailabilityValue.X, AvailabilityValue.X)
            .setStaticTechLevel(SimpleTechLevel.STANDARD);

    @Override
    public TechAdvancement getConstructionTechAdvancement() {
        return isPrimitive() ? TA_DROPSHIP_PRIMITIVE : TA_DROPSHIP;
    }

    @Override
    protected void addSystemTechAdvancement(CompositeTechLevel ctl) {
        super.addSystemTechAdvancement(ctl);
        if (collarType != COLLAR_NO_BOOM) {
            ctl.addComponent(getCollarTA());
        }
    }

    @Override
    public double getCost(CalculationReport calcReport, boolean ignoreAmmo) {
        return DropShipCostCalculator.calculateCost(this, calcReport, ignoreAmmo);
    }

    @Override
    public double getPriceMultiplier() {
        return isSpheroid() ? 28.0 : 36.0;
    }

    /**
     * need to check bay location before loading ammo
     */
    @Override
    public boolean loadWeapon(WeaponMounted mounted, AmmoMounted mountedAmmo) {
        boolean success = false;
        WeaponType wtype = mounted.getType();
        AmmoType atype = mountedAmmo.getType();

        if (mounted.getLocation() != mountedAmmo.getLocation()) {
            return success;
        }

        // for large craft, ammo must be in the same bay
        WeaponMounted bay = whichBay(getEquipmentNum(mounted));
        if ((bay != null) && !bay.ammoInBay(getEquipmentNum(mountedAmmo))) {
            return success;
        }

        if (mountedAmmo.isAmmoUsable() && !wtype.hasFlag(WeaponType.F_ONESHOT)
                && (atype.getAmmoType() == wtype.getAmmoType()) && (atype.getRackSize() == wtype.getRackSize())) {
            mounted.setLinked(mountedAmmo);
            success = true;
        }
        return success;
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.Entity#getIniBonus()
     */
    @Override
    public int getHQIniBonus() {
        // large craft are considered to have > 7 tons comm equipment
        // hence they get +2 ini bonus as a mobile hq
        return 2;
    }

    /**
     * All military dropships automatically have ECM if in space
     */
    @Override
    public boolean hasActiveECM() {
        if (isActiveOption(OptionsConstants.ADVAERORULES_STRATOPS_ECM) && isSpaceborne()) {
            return getECMRange() > Entity.NONE;
        } else {
            return super.hasActiveECM();
        }
    }

    /**
     * What's the range of the ECM equipment?
     *
     * @return the <code>int</code> range of this unit's ECM. This value will be
     *         <code>Entity.NONE</code> if no ECM is active.
     */
    @Override
    public int getECMRange() {
        if (!isActiveOption(OptionsConstants.ADVAERORULES_STRATOPS_ECM) || !isSpaceborne()) {
            return super.getECMRange();
        }
        if (!isMilitary()) {
            return Entity.NONE;
        }
        int range = 1;
        // the range might be affected by sensor/FCS damage
        range = range - getFCSHits() - getSensorHits();
        return range;
    }

    /**
     * Return the height of this dropship above the terrain.
     */
    @Override
    public int height() {
        if (isAirborne()) {
            return 0;
        }
        if (isSpheroid()) {
            return 9;
        }
        return 4;
    }

    @Override
    public int getWalkMP(MPCalculationSetting mpCalculationSetting) {
        // A grounded dropship with the center hex in level 1 water is immobile.
        if ((game != null) && !isSpaceborne() && !isAirborne()) {
            Hex hex = game.getHexOf(this);
            if ((hex != null) && (hex.containsTerrain(Terrains.WATER, 1) && !hex.containsTerrain(Terrains.ICE))) {
                return 0;
            }
        }
        return super.getWalkMP(mpCalculationSetting);
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.Entity#setPosition(megamek.common.Coords)
     */
    @Override

    public void setPosition(Coords position) {
        // When a Dropship changes from being 1 hex to 7 getOccupiedCoords will return
        // its changed secondary hexes. Instead, let's grab the cached ones for this to
        // make sure we properly set out new positions.
        HashSet<Coords> oldPositions = getOccupiedCoords();
        if (game != null) {
            oldPositions = game.getEntityPositions(this);
        }

        super.setPosition(position, false);
        if ((getAltitude() == 0) && (null != game) && !isSpaceborne() && (position != null)) {
            secondaryPositions.put(0, position);
            secondaryPositions.put(1, position.translated(getFacing()));
            secondaryPositions.put(2, position.translated((getFacing() + 1) % 6));
            secondaryPositions.put(3, position.translated((getFacing() + 2) % 6));
            secondaryPositions.put(4, position.translated((getFacing() + 3) % 6));
            secondaryPositions.put(5, position.translated((getFacing() + 4) % 6));
            secondaryPositions.put(6, position.translated((getFacing() + 5) % 6));
        }
        if (game != null) {
            game.updateEntityPositionLookup(this, oldPositions);
        }
    }

    @Override
    public void setAltitude(int altitude) {
        super.setAltitude(altitude);
        if ((getAltitude() == 0) && (game != null) && !isSpaceborne() && (getPosition() != null)) {
            secondaryPositions.put(0, getPosition());
            secondaryPositions.put(1, getPosition().translated(getFacing()));
            secondaryPositions.put(2, getPosition().translated((getFacing() + 1) % 6));
            secondaryPositions.put(3, getPosition().translated((getFacing() + 2) % 6));
            secondaryPositions.put(4, getPosition().translated((getFacing() + 3) % 6));
            secondaryPositions.put(5, getPosition().translated((getFacing() + 4) % 6));
            secondaryPositions.put(6, getPosition().translated((getFacing() + 5) % 6));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see megamek.common.Entity#setFacing(int)
     */
    @Override
    public void setFacing(int facing) {
        super.setFacing(facing);
        setPosition(getPosition());
    }

    @Override
    public int getLandingLength() {
        return 15;
    }

    @Override
    public String hasRoomForVerticalLanding() {
        // dropships can land just about anywhere they want, unless it is off
        // the map
        Vector<Coords> positions = new Vector<>();
        positions.add(getPosition());
        for (int i = 0; i < 6; i++) {
            positions.add(getPosition().translated(i));
        }
        for (Coords pos : positions) {
            Hex hex = game.getHex(pos, getBoardId());
            // if the hex is null, then we are offboard. Don't let units
            // land offboard.
            if (null == hex) {
                return "landing area not on the map";
            }
            if (hex.containsTerrain(Terrains.WATER)) {
                return "cannot land on water";
            }
        }
        // TODO: what about other terrain (like jungles)?
        return null;
    }

    @Override
    public boolean usesWeaponBays() {
        if (null == game) {
            return true;
        }
        return (isAirborne() || isSpaceborne() || game.getPhase().isLounge());
    }

    @Override
    public HitData rollHitLocation(int table, int side) {
        if ((table == ToHitData.HIT_KICK) || (table == ToHitData.HIT_PUNCH)) {
            // we don't really have any good rules on how to apply this,
            // I have a rules question posted about it:
            // http://bg.battletech.com/forums/index.php/topic,24077.new.html#new
            // in the meantime lets make up our own hit table (fun!)
            int roll = Compute.d6(2);
            if (side == ToHitData.SIDE_LEFT) {
                // normal left-side hits
                switch (roll) {
                    case 2:
                        setPotCrit(CRIT_GEAR);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 3:
                        setPotCrit(CRIT_LIFE_SUPPORT);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 4:
                        setPotCrit(CRIT_DOCK_COLLAR);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 5:
                        setPotCrit(CRIT_LEFT_THRUSTER);
                        return new HitData(LOC_LWING, false, HitData.EFFECT_NONE);
                    case 6:
                        setPotCrit(CRIT_CARGO);
                        return new HitData(LOC_LWING, false, HitData.EFFECT_NONE);
                    case 7:
                        setPotCrit(CRIT_WEAPON);
                        return new HitData(LOC_LWING, false, HitData.EFFECT_NONE);
                    case 8:
                        setPotCrit(CRIT_DOOR);
                        return new HitData(LOC_LWING, false, HitData.EFFECT_NONE);
                    case 9:
                        setPotCrit(CRIT_LEFT_THRUSTER);
                        return new HitData(LOC_LWING, false, HitData.EFFECT_NONE);
                    case 10:
                        setPotCrit(CRIT_AVIONICS);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 11:
                        setPotCrit(CRIT_ENGINE);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 12:
                        setPotCrit(CRIT_WEAPON);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                }
            } else {
                switch (roll) {
                    case 2:
                        setPotCrit(CRIT_GEAR);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 3:
                        setPotCrit(CRIT_LIFE_SUPPORT);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 4:
                        setPotCrit(CRIT_DOCK_COLLAR);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 5:
                        setPotCrit(CRIT_RIGHT_THRUSTER);
                        return new HitData(LOC_RWING, false, HitData.EFFECT_NONE);
                    case 6:
                        setPotCrit(CRIT_CARGO);
                        return new HitData(LOC_RWING, false, HitData.EFFECT_NONE);
                    case 7:
                        setPotCrit(CRIT_WEAPON);
                        return new HitData(LOC_RWING, false, HitData.EFFECT_NONE);
                    case 8:
                        setPotCrit(CRIT_DOOR);
                        return new HitData(LOC_RWING, false, HitData.EFFECT_NONE);
                    case 9:
                        setPotCrit(CRIT_RIGHT_THRUSTER);
                        return new HitData(LOC_RWING, false, HitData.EFFECT_NONE);
                    case 10:
                        setPotCrit(CRIT_AVIONICS);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 11:
                        setPotCrit(CRIT_ENGINE);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                    case 12:
                        setPotCrit(CRIT_WEAPON);
                        return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
                }
            }
            return new HitData(LOC_AFT, false, HitData.EFFECT_NONE);
        } else {
            return super.rollHitLocation(table, side);
        }
    }

    @Override
    public String getLocationAbbr(int loc) {
        if (loc == Entity.LOC_NONE) {
            return "System Wide";
        } else {
            return super.getLocationAbbr(loc);
        }
    }

    @Override
    public long getEntityType() {
        return Entity.ETYPE_AERO | Entity.ETYPE_SMALL_CRAFT | Entity.ETYPE_DROPSHIP;
    }

    @Override
    public boolean canChangeSecondaryFacing() {
        // flying dropships can execute the "ECHO" maneuver (stratops 113), aka a torso twist,
        // if they have the MP for it
        return isAirborne() && !isEvading() && (mpUsed <= getRunMP() - 2);
    }

    /**
     * Can this dropship "torso twist" in the given direction?
     */
    @Override
    public boolean isValidSecondaryFacing(int dir) {
        int rotate = dir - getFacing();
        if (canChangeSecondaryFacing()) {
            return (rotate == 0) || (rotate == 1) || (rotate == -1)
                    || (rotate == -5) || (rotate == 5);
        }
        return rotate == 0;
    }

    /**
     * Return the nearest valid direction to "torso twist" in
     */
    @Override
    public int clipSecondaryFacing(int dir) {
        if (isValidSecondaryFacing(dir)) {
            return dir;
        }

        // can't twist without enough MP
        if (!canChangeSecondaryFacing()) {
            return getFacing();
        }

        // otherwise, twist once in the appropriate direction
        final int rotate = (dir + (6 - getFacing())) % 6;

        return rotate >= 3 ? (getFacing() + 5) % 6 : (getFacing() + 1) % 6;
    }

    @Override
    public void newRound(int roundNumber) {
        super.newRound(roundNumber);

        if (getGame().useVectorMove()) {
            setFacing(getSecondaryFacing());
        }

        setSecondaryFacing(getFacing());
    }

    /**
     * Utility function that handles situations where a facing change
     * has some kind of permanent effect on the entity.
     */
    @Override
    public void postProcessFacingChange() {
        mpUsed += 2;
    }

    /**
     * Depsite being VSTOL in other respects, aerodyne dropships are
     * explicitely forbidden from vertical landings in atmosphere.
     */
    @Override
    public boolean canLandVertically() {
        PlanetaryConditions conditions = game.getPlanetaryConditions();
        return isSpheroid()
                || conditions.getAtmosphere().isLighterThan(Atmosphere.THIN);
    }

    /**
     * Depsite being VSTOL in other respects, aerodyne dropships are
     * explicitely forbidden from vertical takeoff in atmosphere.
     */
    @Override
    public boolean canTakeOffVertically() {
        PlanetaryConditions conditions = game.getPlanetaryConditions();
        boolean spheroidOrLessThanThin = isSpheroid()
                || conditions.getAtmosphere().isLighterThan(Atmosphere.THIN);
        return spheroidOrLessThanThin && (getCurrentThrust() > 2);
    }

    @Override
    public int getGenericBattleValue() {
        return (int) Math.round(Math.exp(6.5266 + 0.2497*Math.log(getWeight())));
    }

}
