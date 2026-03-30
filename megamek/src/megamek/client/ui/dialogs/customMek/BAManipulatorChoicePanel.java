
package megamek.client.ui.dialogs.customMek;

import static megamek.common.battleArmor.BattleArmor.MOUNT_LOC_LEFT_ARM;
import static megamek.common.battleArmor.BattleArmor.MOUNT_LOC_RIGHT_ARM;
import static megamek.common.equipment.EquipmentTypeLookup.BA_MODULAR_EQUIPMENT_ADAPTOR;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import megamek.client.ui.Messages;
import megamek.common.annotations.Nullable;
import megamek.common.battleArmor.BattleArmor;
import megamek.common.equipment.EquipmentType;
import megamek.common.equipment.MiscMounted;
import megamek.common.equipment.MiscType;
import megamek.common.exceptions.LocationFullException;
import megamek.common.verifier.TestBattleArmor;
import megamek.common.verifier.TestEntity;
import megamek.logging.MMLogger;

import static megamek.common.verifier.TestBattleArmor.BAManipulator;

public class BAManipulatorChoicePanel extends JPanel {

    private static final MMLogger LOGGER = MMLogger.create(BAManipulatorChoicePanel.class);

    private final JCheckBox leftModularSelector = new JCheckBox("Modular Equipment Adaptor");
    private final JCheckBox rightModularSelector = new JCheckBox("Modular Equipment Adaptor");

    private final JComboBox<BAManipulator> leftManipulatorSelect = new JComboBox<>();
    private final JComboBox<BAManipulator> rightManipulatorSelect = new JComboBox<>();

    private final SpinnerNumberModel cargoLifterCapacityModel = new SpinnerNumberModel(0.5, 0.5, 80, 0.5);
    private final JSpinner cargoLifterCapacity = new JSpinner(cargoLifterCapacityModel);
    private final JLabel lblSize = createLabel("Capacity:");

    private final BattleArmor battleArmor;

    private boolean ignoreEvents = false;

    public BAManipulatorChoicePanel(BattleArmor battleArmor) {
        //        this.techManager = techManager;
        this.battleArmor = battleArmor;
        // We need to determine how much weight is free, so the user can pick legal combinations of manipulators
        double maxActualTrooperTonnage = getMaxTrooperWeight(battleArmor);
        double freeTonnage = battleArmor.getTrooperWeight() - maxActualTrooperTonnage;
        String freeWeight = Messages.getString("CustomMekDialog.freeWeight", (int) (freeTonnage * 1000));

        leftManipulatorSelect.setRenderer(new ManipulatorRenderer(leftManipulatorSelect));
        rightManipulatorSelect.setRenderer(new ManipulatorRenderer(rightManipulatorSelect));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 1, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JLabel(freeWeight, SwingConstants.CENTER), gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        add(createLabel("Left Arm:"), gbc);
        add(leftManipulatorSelect, gbc);

        gbc.gridy++;
        add(new JLabel(), gbc);
        add(leftModularSelector, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(10), gbc);

        gbc.gridy++;
        add(createLabel("Right Arm:"), gbc);
        add(rightManipulatorSelect, gbc);

        gbc.gridy++;
        add(new JLabel(), gbc);
        add(rightModularSelector, gbc);

        gbc.gridy++;
        add(Box.createVerticalStrut(10), gbc);

        gbc.gridy++;
        add(lblSize, gbc);
        add(cargoLifterCapacity, gbc);

        setValuesFromBattleArmor();

        leftManipulatorSelect.addActionListener(this::manipulatorSelected);
        rightManipulatorSelect.addActionListener(this::manipulatorSelected);
        cargoLifterCapacity.addChangeListener(this::cargoSizeEdited);

        updateAfterChange();
    }

    private static double getMaxTrooperWeight(BattleArmor battleArmor) {
        TestBattleArmor testBattleArmor = (TestBattleArmor) TestEntity.getEntityVerifier(battleArmor);
        double maxTrooperWeight = 0;
        for (int i = 1; i < battleArmor.getTroopers(); i++) {
            double trooperWeight = testBattleArmor.calculateWeight(i);
            if (trooperWeight > maxTrooperWeight) {
                maxTrooperWeight = trooperWeight;
            }
        }
        return maxTrooperWeight;
    }

    public JLabel createLabel(String text) {
        return new JLabel(text, SwingConstants.RIGHT);
    }

    /**
     * Enter values from the BattleArmor. Not to be called after construction.
     */
    void setValuesFromBattleArmor() {
        try {
            ignoreEvents = true;

            Vector<BAManipulator> validManipulators = new Vector<>(Arrays.asList(BAManipulator.values()));
            if (battleArmor.countMisc(BA_MODULAR_EQUIPMENT_ADAPTOR) == 1) {
                validManipulators.removeIf(m -> m.pairMounted);
            }
            leftManipulatorSelect.setModel(new DefaultComboBoxModel<>(validManipulators));
            rightManipulatorSelect.setModel(new DefaultComboBoxModel<>(validManipulators));

            BAManipulator leftManipulator = BAManipulator.getManipulator(battleArmor.getLeftManipulatorName());
            if (leftManipulator != null) {
                leftManipulatorSelect.setSelectedItem(leftManipulator);
            }

            BAManipulator rightManipulator = BAManipulator.getManipulator(battleArmor.getRightManipulatorName());
            if (rightManipulator != null) {
                rightManipulatorSelect.setSelectedItem(rightManipulator);
            }

            leftModularSelector.setSelected(
                  battleArmor.hasMiscInMountLocation(BA_MODULAR_EQUIPMENT_ADAPTOR, MOUNT_LOC_LEFT_ARM));
            rightModularSelector.setSelected(
                  battleArmor.hasMiscInMountLocation(BA_MODULAR_EQUIPMENT_ADAPTOR, MOUNT_LOC_RIGHT_ARM));

            leftModularSelector.setEnabled(false);
            rightModularSelector.setEnabled(false);
        } finally {
            ignoreEvents = false;
        }
    }

    /**
     * Updates the GUI elements (enable/visible) after a selection change.
     */
    private void updateAfterChange() {
        leftManipulatorSelect.setEnabled(leftModularSelector.isSelected());
        BAManipulator leftManipulatorItem = selectedManipulatorItem(leftManipulatorSelect);
        rightManipulatorSelect.setEnabled(!leftManipulatorItem.pairMounted && rightModularSelector.isSelected());
        cargoLifterCapacity.setVisible(leftManipulatorItem == BAManipulator.CARGO_LIFTER);
        lblSize.setVisible(cargoLifterCapacity.isVisible());
    }

    public void manipulatorSelected(ActionEvent event) {
        if (ignoreEvents) {
            return;
        }
        JComboBox<BAManipulator> thisManipulatorSelector =
              (event.getSource() == leftManipulatorSelect) ? leftManipulatorSelect : rightManipulatorSelect;
        BAManipulator selectedManipulatorItem = selectedManipulatorItem(thisManipulatorSelector);

        JComboBox<BAManipulator> otherManipulatorSelector =
              (thisManipulatorSelector == leftManipulatorSelect) ? rightManipulatorSelect : leftManipulatorSelect;
        BAManipulator otherManipulatorItem = selectedManipulatorItem(otherManipulatorSelector);

        try {
            ignoreEvents = true;
            if (selectedManipulatorItem.pairMounted) {
                // the new manipulator is pair-mounted, therefore set the other selector
                otherManipulatorSelector.setSelectedItem(selectedManipulatorItem);
            } else if (otherManipulatorItem.pairMounted) {
                // the new manipulator is not pair-mounted but the previous was, therefore reset the other selector
                otherManipulatorSelector.setSelectedIndex(0);
            }
            updateAfterChange();
        } finally {
            ignoreEvents = false;
        }
    }

    /**
     * Returns the selected manipulator item from the given selector JComboBox or BAManipulator.NONE, if it cannot be
     * parsed or nothing is selected.
     *
     * @param manipulatorSelector The left or right arm manipulator selector
     *
     * @return The selected BAManipulator item or NONE as error fallback
     */
    @Nullable
    private BAManipulator selectedManipulatorItem(JComboBox<BAManipulator> manipulatorSelector) {
        return Objects.requireNonNullElse((BAManipulator) manipulatorSelector.getSelectedItem(), BAManipulator.NONE);
    }

    public void cargoSizeEdited(ChangeEvent event) {
        if (ignoreEvents) {
            return;
        }
//        setManipulatorSize(BattleArmor.MOUNT_LOC_LEFT_ARM, cargoLifterCapacityModel.getNumber().doubleValue());
//        setManipulatorSize(BattleArmor.MOUNT_LOC_RIGHT_ARM, cargoLifterCapacityModel.getNumber().doubleValue());
    }

    /**
     * Adds and removes manipulator MiscMounteds on the unit so that the manipulator on the given mountLoc arm is the
     * one given as newManipulator (which may be none). Also updates the other arm if necessary.
     *
     * @param newManipulator The new manipulator type
     * @param mountLoc       one of the two arm locations (MOUNT_LOC_x_ARM)
     */
    private void setManipulators(TestBattleArmor.BAManipulator newManipulator, int mountLoc) {
        MiscMounted currentManipulator = battleArmor.getManipulator(mountLoc);
        if (currentManipulator != null) {
            //            UnitUtil.removeMounted(battleArmor, currentManipulator);
        }
        setManipulator(newManipulator, mountLoc);

        if (newManipulator.pairMounted) {
            setManipulator(newManipulator, otherArm(mountLoc));

        } else if (currentManipulator != null && isPairedManipulator(currentManipulator.getType())) {
            // when the previous manipulator was pair-mounted but the new one is not, remove the old on the other arm
            MiscMounted secondManipulator = battleArmor.getManipulator(otherArm(mountLoc));
            if (secondManipulator != null) {
                //                UnitUtil.removeMounted(battleArmor, currentManipulator);
            }
        }
    }

    /**
     * Adds and removes manipulator MiscMounteds on the unit so that the manipulator on the given mountLoc arm is the
     * one given as newManipulator (which may be none). Does not touch the other arm. Should only be called from
     * setManipulators().
     *
     * @param newManipulator The new manipulator type
     * @param mountLoc       one of the two arm locations (MOUNT_LOC_x_ARM)
     */
    private void setManipulator(TestBattleArmor.BAManipulator newManipulator, int mountLoc) {
        Optional<MiscMounted> currentManipulator = getManipulator(mountLoc);
        //        currentManipulator.ifPresent(miscMounted -> UnitUtil.removeMounted(battleArmor, miscMounted));
        if (newManipulator != TestBattleArmor.BAManipulator.NONE) {
            MiscMounted newMount = new MiscMounted(battleArmor, getMisc(newManipulator));
            newMount.setBaMountLoc(mountLoc);
            try {
                battleArmor.addEquipment(newMount, BattleArmor.LOC_SQUAD, false);
            } catch (LocationFullException ex) {
                LOGGER.error("Could not mount {}", newManipulator, ex);
            }
        }
    }

    private MiscType getMisc(TestBattleArmor.BAManipulator baManipulator) {
        return (MiscType) EquipmentType.get(baManipulator.internalName);
    }

    private int otherArm(int armLocation) {
        return armLocation == MOUNT_LOC_LEFT_ARM ? MOUNT_LOC_RIGHT_ARM : MOUNT_LOC_LEFT_ARM;
    }

    private Optional<MiscMounted> getManipulator(int mountLoc) {
        return battleArmor.getMisc().stream()
              .filter(m -> m.getBaMountLoc() == mountLoc)
              .filter(m -> m.getType().hasFlag(MiscType.F_BA_MANIPULATOR))
              .findFirst();
    }

    private boolean isPairedManipulator(EquipmentType eq) {
        TestBattleArmor.BAManipulator manipulator = TestBattleArmor.BAManipulator.getManipulator(eq.getInternalName());
        return manipulator != null && manipulator.pairMounted;
    }

    private void setManipulatorSize(int mountLoc, double size) {
        getManipulator(mountLoc).ifPresent(manipulator -> manipulator.setSize(size));
    }

    private class ManipulatorRenderer extends DefaultListCellRenderer {

        private final JComboBox<BAManipulator> comboBox;

        public ManipulatorRenderer(JComboBox<BAManipulator> comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
              boolean cellHasFocus) {

            String text = "Error";
            if (value instanceof BAManipulator baManipulator) {
                String tonnage = "0 kg";
                String name = "None";
                var type = EquipmentType.get(baManipulator.internalName);
                if (type != null) {
                    name = type.getShortName();
                    if (baManipulator == BAManipulator.CARGO_LIFTER) {
                        tonnage = "variable";
                    } else {
                        tonnage = (int) (type.getTonnage(battleArmor) * 1000) + " kg";
                    }
                }
                if (comboBox.isEnabled()) {
                    text = "%s (%s)".formatted(name, tonnage);
                } else {
                    // when disabled, this item is either the second arm of a pair-mounted (no need to show the
                    // weight twice) or has no modular adaptor anyway and its weight is already part of the trooper
                    text = name;
                }
            }
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}
