/*
 * MegaMek -
 * Copyright (C) 2018 The MegaMek Team
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
package megamek.common.templates;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import megamek.common.Aero;
import megamek.common.AmmoType;
import megamek.common.Entity;
import megamek.common.EntityFluff;
import megamek.common.EquipmentType;
import megamek.common.FighterSquadron;
import megamek.common.Messages;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.WeaponType;
import megamek.common.equipment.AmmoMounted;
import megamek.common.equipment.WeaponMounted;
import megamek.common.util.AeroAVModCalculator;
import megamek.common.verifier.EntityVerifier;
import megamek.common.verifier.TestAero;
import megamek.common.weapons.CLIATMWeapon;
import megamek.common.weapons.lrms.LRMWeapon;
import megamek.common.weapons.missiles.ATMWeapon;
import megamek.common.weapons.missiles.MMLWeapon;
import megamek.common.weapons.srms.SRMWeapon;

/**
 * Creates a TRO template model for aerospace and conventional fighters.
 *
 * @author Neoancient
 */
public class AeroTROView extends TROView {

    private final Aero aero;

    public AeroTROView(Aero aero) {
        this.aero = aero;
    }

    @Override
    protected String getTemplateFileName(boolean html) {
        if (html) {
            return "aero.ftlh";
        }
        return "aero.ftl";
    }

    @Override
    protected void initModel(EntityVerifier verifier) {
        setModelData("formatArmorRow",
              new FormatTableRowMethod(new int[] { 20, 10 },
                    new Justification[] { Justification.LEFT, Justification.CENTER }));
        addBasicData(aero);
        addArmorAndStructure();
        final int nameWidth = addEquipment(aero);
        setModelData("formatEquipmentRow",
              new FormatTableRowMethod(new int[] { nameWidth, 12, 8, 8, 5, 5, 5, 5, 5 },
                    new Justification[] { Justification.LEFT, Justification.CENTER, Justification.CENTER,
                                          Justification.CENTER, Justification.CENTER, Justification.CENTER,
                                          Justification.CENTER, Justification.CENTER, Justification.CENTER }));
        addFluff();
        setModelData("isOmni", aero.isOmni());
        setModelData("isConventional", aero.hasETypeFlag(Entity.ETYPE_CONV_FIGHTER));
        setModelData("isSupportVehicle", aero.isSupportVehicle());
        setModelData("isVSTOL", aero.isVSTOL());
        setModelData("isFighterSquadron", aero instanceof FighterSquadron);
        final TestAero testAero = new TestAero(aero, verifier.aeroOption, null);
        if (aero.hasEngine()) {
            setModelData("engineName", stripNotes(aero.getEngine().getEngineName()));
            setModelData("engineMass", NumberFormat.getInstance().format(testAero.getWeightEngine()));
        }
        setModelData("safeThrust", aero.getWalkMP());
        setModelData("maxThrust", aero.getRunMP());
        setModelData("si", aero.getOSI());
        if (!(aero instanceof FighterSquadron)) {
            setModelData("vstolMass", testAero.getWeightMisc());
            setModelData("hsCount",
                  aero.getHeatType() == Aero.HEAT_DOUBLE ?
                        aero.getOHeatSinks() + " [" + (aero.getOHeatSinks() * 2) + "]" :
                        aero.getOHeatSinks());
            setModelData("hsMass", NumberFormat.getInstance().format(testAero.getWeightHeatSinks()));
        }
        setModelData("fuelPoints", aero.getFuel());
        setModelData("fuelMass", aero.getFuelTonnage());
        if (aero.getCockpitType() == Aero.COCKPIT_STANDARD) {
            setModelData("cockpitType", "Cockpit");
        } else {
            setModelData("cockpitType", Aero.getCockpitTypeString(aero.getCockpitType()));
        }
        setModelData("cockpitMass", NumberFormat.getInstance().format(testAero.getWeightControls()));
        final String atName = formatArmorType(aero, true);
        if (!atName.isBlank()) {
            setModelData("armorType", " (" + atName + ")");
        } else {
            setModelData("armorType", "");
        }
        setModelData("armorFactor", aero.getTotalOArmor());
        setModelData("armorMass", NumberFormat.getInstance().format(testAero.getWeightArmor()));
        if (aero.isOmni()) {
            addFixedOmni(aero);
        }
    }

    private void addFluff() {
        addMekVeeAeroFluff(aero);
        setModelData("frameDesc",
              formatSystemFluff(EntityFluff.System.CHASSIS,
                    aero.getFluff(),
                    () -> Messages.getString("TROView.Unknown")));
    }

    private static final int[][] AERO_ARMOR_LOCS = { { Aero.LOC_NOSE }, { Aero.LOC_RWING, Aero.LOC_LWING },
                                                     { Aero.LOC_AFT } };

    private void addArmorAndStructure() {
        setModelData("armorValues", addArmorStructureEntries(aero, Entity::getOArmor, AERO_ARMOR_LOCS));
        if (aero.hasPatchworkArmor()) {
            setModelData("patchworkByLoc", addPatchworkATs(aero, AERO_ARMOR_LOCS));
        }
    }

    /**
     * Adds the details for all weapon bays, including heat by bay and location and rows for each bay.
     *
     * @param arcSets A two-dimensional array that groups arcs that should appear on the same line (e.g. left/right).
     *                Only the first arc in any group is actually evaluated, since the rules require left/right arcs be
     *                identical, but both arcs are combined in the name.
     *
     * @return The width of the longest value for bay/weapon name, for use in laying out plain text.
     */
    protected int addWeaponBays(String[][] arcSets) {
        int nameWidth = 1;
        final Map<String, List<WeaponMounted>> baysByLoc = aero.getWeaponBayList()
                                                                 .stream()
                                                                 .collect(Collectors.groupingBy(this::getArcAbbr));
        final List<String> bayArcs = new ArrayList<>();
        final Map<String, Integer> heatByLoc = new HashMap<>();
        final Map<String, List<Map<String, Object>>> bayDetails = new HashMap<>();
        for (final String[] arcSet : arcSets) {
            final List<WeaponMounted> bayList = baysByLoc.get(arcSet[0]);
            if (null != bayList) {
                final List<Map<String, Object>> rows = new ArrayList<>();
                int heat = 0;
                for (final WeaponMounted bay : bayList) {
                    final Map<String, Object> row = createBayRow(bay);
                    heat += ((Number) row.get("heat")).intValue();
                    rows.add(row);
                    nameWidth = Math.max(nameWidth,
                          ((List<?>) row.get("weapons")).stream().mapToInt(w -> ((String) w).length()).max().orElse(0) +
                                1);
                }
                final String arcName = String.join("/", arcSet).replaceAll("\\s+(Fwd|Aft)/", "/");
                bayArcs.add(arcName);
                heatByLoc.put(arcName, heat);
                bayDetails.put(arcName, rows);
            }
        }
        setModelData("weaponBayArcs", bayArcs);
        setModelData("weaponBayHeat", heatByLoc);
        setModelData("weaponBays", bayDetails);
        return nameWidth;
    }

    

    private Map<String, Object> createBayRow(WeaponMounted bay) {
        final Map<EquipmentKey, Integer> weaponCount = new HashMap<>();
        int heat = 0;
        int baysrv = 0;
        int srv = 0;
        int mrv = 0;
        int lrv = 0;
        int erv = 0;
        final boolean isCapital = bay.getType().isCapital();
        final int multiplier = isCapital ? 10 : 1;
        Mounted<?> linker = null;
        // FIXME: Consider new AmmoType::equals / BombType::equals
        final Map<AmmoType, Integer> shotsByAmmoType = bay.getBayAmmo()
                                                             .stream()
                                                             .collect(Collectors.groupingBy(AmmoMounted::getType,
                                                                   Collectors.summingInt(Mounted::getBaseShotsLeft)));
        for (final WeaponMounted wMount : bay.getBayWeapons()) {
            final WeaponType wtype = wMount.getType();
            if ((wMount.getLinkedBy() != null) && (wMount.getLinkedBy().getType() instanceof MiscType)) {
                linker = wMount.getLinkedBy();
            }
            weaponCount.merge(new EquipmentKey(wtype, wMount.getSize()), 1, Integer::sum);
            int bonus = 0;
            heat += wtype.getHeat();
            int av = (int) (wtype.getShortAV() * multiplier) + bonus;
            if (!isCapital) {
                if (wtype instanceof ATMWeapon || wtype instanceof CLIATMWeapon) {
                    if (wtype instanceof CLIATMWeapon) {
                        av = (int) wtype.getShortAV() * multiplier;
                    } else {
                        av = (int) Math.ceil(wtype.getShortAV() * multiplier * 0.5);
                    }
                    baysrv += (int) Math.round(Math.ceil(av * 1.5) / 10.0);
                    srv += (int) Math.ceil(av * 1.5);
                    mrv += av;
                    lrv += (int) Math.ceil(av * 0.5);
                    erv += (int) Math.ceil(av * 0.5);
                    continue;
                }
                if (linker != null) {
                    bonus = AeroAVModCalculator.calculateBonus(wtype, linker.getType(), true);
                }
                if (wtype instanceof MMLWeapon) {
                    av *= 2; // SRM ammo, this is simulating the 2x damage of the MML when using SRM ammo at short range
                }
            }
            baysrv += (int) Math.round(av / 10.0);
            srv += (int) (wtype.getShortAV() * multiplier) + bonus;
            mrv += (int) (wtype.getMedAV() * multiplier) + bonus;
            lrv += (int) (wtype.getLongAV() * multiplier) + bonus;
            erv += (int) (wtype.getExtAV() * multiplier) + bonus;
        }
        final Map<String, Object> retVal = new HashMap<>();
        final List<String> weapons = new ArrayList<>();
        for (final Map.Entry<EquipmentKey, Integer> entry : weaponCount.entrySet()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(entry.getValue()).append(" ").append(entry.getKey().name());
            if (null != linker) {
                sb.append("+").append(linker.getName().replace(" FCS", ""));
            }
            weapons.add(sb.toString());
        }
        shotsByAmmoType.forEach((at, count) -> weapons.add(String.format("%s (%d %s)",
              at.getName(),
              count,
              Messages.getString("TROView.shots"))));
        retVal.put("weapons", weapons);
        retVal.put("heat", heat);
        retVal.put("srv", baysrv + "(" + srv + ")");
        retVal.put("mrv", Math.round(mrv / 10.0) + "(" + mrv + ")");
        retVal.put("lrv", Math.round(lrv / 10.0) + "(" + lrv + ")");
        retVal.put("erv", Math.round(erv / 10.0) + "(" + erv + ")");
        retVal.put("class", bay.getName().replaceAll("\\s+Bay", ""));
        return retVal;
    }

    /**
     * Firing arc abbreviation, which may be different from mounting location for side arcs on small craft and
     * dropships
     *
     * @param m The weapon mount
     *
     * @return The arc abbreviation.
     */
    protected String getArcAbbr(Mounted<?> m) {
        return aero.getLocationAbbr(m.getLocation());
    }

    /**
     * Adds ammo data used by large craft
     */
    protected void addAmmo() {
        final Map<String, List<AmmoMounted>> ammoByType = aero.getAmmo()
                                                                .stream()
                                                                .collect(Collectors.groupingBy(Mounted::getName));
        final List<Map<String, Object>> ammo = new ArrayList<>();
        for (final List<AmmoMounted> aList : ammoByType.values()) {
            final Map<String, Object> ammoEntry = new HashMap<>();
            ammoEntry.put("name", aList.get(0).getName().replaceAll("\\s+Ammo", ""));
            ammoEntry.put("shots", aList.stream().mapToInt(Mounted::getUsableShotsLeft).sum());
            ammoEntry.put("tonnage", aList.stream().mapToDouble(Mounted::getSize).sum());
            ammo.add(ammoEntry);
        }
        setModelData("ammo", ammo);
    }

    /**
     * Convenience method to add the number of crew in a category to a list, and choose the singular or plural form. The
     * localized string property should be provided for both singular and plural entries, even if they are the same
     * (such as enlisted/non-rated and bay personnel in English).
     * <p>
     * The model needs to have a "crew" entry initialized to a {@code List<String>} before calling this method.
     *
     * @param stringKey The key for the string property in the singular form. A "TROView." prefix will be added, and if
     *                  the plural form is needed "s" will be appended.
     * @param count     The number of crew in the category
     *
     * @throws NullPointerException If the "crew" property in the model has not been initialized
     * @throws ClassCastException   If the crew property of the model is not a {@code List<String>}
     */
    @SuppressWarnings("unchecked")
    protected void addCrewEntry(String stringKey, int count) {
        List<String> crew = (List<String>) getModelData("crew");

        if (count > 1) {
            crew.add(String.format(Messages.getString("TROView." + stringKey + "s"), count));
        } else {
            crew.add(String.format(Messages.getString("TROView." + stringKey), count));
        }
    }

}
