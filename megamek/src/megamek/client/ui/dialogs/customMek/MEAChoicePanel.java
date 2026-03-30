/*
 * Copyright (C) 2026 The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.dialogs.customMek;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import megamek.common.battleArmor.BattleArmor;
import megamek.common.units.Entity;
import megamek.common.equipment.EquipmentType;
import megamek.common.exceptions.LocationFullException;
import megamek.common.equipment.MiscType;
import megamek.common.equipment.Mounted;
import megamek.common.equipment.MiscMounted;
import megamek.logging.MMLogger;

/**
 * A panel that houses a label and a combo box that allows for selecting which manipulator is mounted in a modular
 * equipment adaptor.
 *
 * @author arlith
 */
public class MEAChoicePanel extends JPanel {

    private static final MMLogger LOGGER = MMLogger.create(MEAChoicePanel.class);

    private final Entity entity;
    private final List<MiscType> manipulators;
    private final JComboBox<String> comboChoices = new JComboBox<>();

    /**
     * The BattleArmor mount location of the modular equipment adaptor.
     */
    private final int battleArmorMountLocation;

    /**
     * The manipulator currently mounted by a modular equipment adaptor.
     */
    private Mounted<?> mountedManipulator;

    public MEAChoicePanel(BattleArmor battleArmor, int mountLoc, Mounted<?> mounted, List<MiscType> manipulators) {
        this.entity = battleArmor;
        this.manipulators = manipulators;

        mountedManipulator = mounted;
        battleArmorMountLocation = mountLoc;
        EquipmentType equipmentType = null;

        if (mounted != null) {
            equipmentType = mounted.getType();
        }

        comboChoices.addItem("None");
        for (MiscType manipulator : manipulators) {
            String manipulatorName =
                  "%s (%d kg)".formatted(manipulator.getShortName(), (int) (manipulator.getTonnage(this.entity) * 1000));
            comboChoices.addItem(manipulatorName);
            if (equipmentType != null &&
                  Objects.equals(manipulator.getInternalName(), equipmentType.getInternalName())) {
                comboChoices.setSelectedItem(manipulatorName);
            }
        }

        String labelDescription = BattleArmor.MOUNT_LOC_NAMES[battleArmorMountLocation] + ":";
        JLabel labelLocation = new JLabel(labelDescription);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 1, 2);
        add(labelLocation, gbc);
        add(comboChoices, gbc);
    }

    public void applyChoice() {
        int selectedIndex = comboChoices.getSelectedIndex();

        // If there's no selection, there's nothing we can do
        if (selectedIndex == -1) {
            return;
        }

        MiscType manipulatorType = null;
        if (selectedIndex > 0 && selectedIndex <= manipulators.size()) {
            // Need to account for the "None" selection
            manipulatorType = manipulators.get(selectedIndex - 1);
        }

        int location = 0;

        if (mountedManipulator != null) {
            location = mountedManipulator.getLocation();
            entity.getEquipment().remove(mountedManipulator);

            if (mountedManipulator instanceof MiscMounted miscMounted) {
                entity.getMisc().remove(miscMounted);
            }
        }

        // Was no manipulator selected?
        if (selectedIndex == 0) {
            return;
        }

        // Add the newly mounted manipulator
        // Adjusts to use the location variable with a default of a location of 0 to account for when the
        // mountedManipulator is null at this point.
        try {
            mountedManipulator = entity.addEquipment(manipulatorType, location);
            mountedManipulator.setBaMountLoc(battleArmorMountLocation);
        } catch (LocationFullException ex) {
            // This shouldn't happen for BA...
            LOGGER.error(ex, "Location Full Exception");
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboChoices.setEnabled(enabled);
    }
}
