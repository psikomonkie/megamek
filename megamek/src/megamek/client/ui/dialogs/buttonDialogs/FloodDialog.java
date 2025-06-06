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
package megamek.client.ui.dialogs.buttonDialogs;

import java.awt.Container;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import megamek.client.ui.dialogs.buttonDialogs.AbstractButtonDialog;
import megamek.client.ui.panels.BoardEditorPanel;
import megamek.client.ui.buttons.MMToggleButton;
import static megamek.client.ui.util.UIUtil.*;
import static megamek.client.ui.Messages.*;

public final class FloodDialog extends AbstractButtonDialog {

    private BoardEditorPanel.EditorTextField txtLevelChange = new BoardEditorPanel.EditorTextField("0", 5, -5, 15);
    private MMToggleButton butRemove = new MMToggleButton(getString("FloodDialog.removeButton"));

    /** Constructs a modal LevelChangeDialog with frame as parent. */
    public FloodDialog(JFrame frame) {
        super(frame, "FloodDialog.name", "FloodDialog.title");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
        result.setBorder(new EmptyBorder(10, 30, 10, 30));

        JPanel textFieldPanel = new FixedYPanel();
        textFieldPanel.add(txtLevelChange);

        JPanel toggleButtonPanel = new FixedYPanel();
        toggleButtonPanel.add(butRemove);

        JLabel labInfo = new JLabel("<CENTER>" + getString("FloodDialog.info"), SwingConstants.CENTER);
        labInfo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel labRemoveInfo = new JLabel("<CENTER>" + getString("FloodDialog.removeInfo"), SwingConstants.CENTER);
        labRemoveInfo.setAlignmentX(CENTER_ALIGNMENT);

        result.add(Box.createVerticalGlue());
        result.add(labInfo);
        result.add(Box.createVerticalStrut(5));
        result.add(textFieldPanel);
        result.add(Box.createVerticalStrut(5));
        result.add(labRemoveInfo);
        result.add(Box.createVerticalStrut(5));
        result.add(toggleButtonPanel);
        result.add(Box.createVerticalGlue());

        return result;
    }

    /** Returns the level change entered by the user or 0, if it cannot be parsed. */
    public int getLevelChange() {
        try {
            return Integer.parseInt(txtLevelChange.getText());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Returns if all newly submerged hexes should have their terrain removed. */
    public boolean getRemoveTerrain() {
        return butRemove.isSelected();
    }

}
