/*
 * MegaMek - Copyright (C) 2004 Ben Mazur (bmazur@sev.org)
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
package megamek.client.ui.swing.phaseDisplay;

import static megamek.client.ui.swing.util.UIUtil.uiLightViolet;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import megamek.client.event.BoardViewEvent;
import megamek.client.ui.Messages;
import megamek.client.ui.swing.ClientGUI;
import megamek.client.ui.swing.util.KeyCommandBind;
import megamek.client.ui.swing.util.UIUtil;
import megamek.client.ui.swing.widget.MegaMekButton;
import megamek.client.ui.swing.widget.SkinSpecification;
import megamek.common.Board;
import megamek.common.Coords;
import megamek.common.Game;
import megamek.common.Player;
import megamek.common.SpecialHexDisplay;
import megamek.common.containers.PlayerIDAndList;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.GameTurnChangeEvent;
import megamek.common.options.OptionsConstants;

public class SelectArtyAutoHitHexDisplay extends StatusBarPhaseDisplay {

    private static final long serialVersionUID = -4948184589134809323L;

    /**
     * This enumeration lists all the possible ActionCommands that can be carried out during the select arty auto hit
     * phase.  Each command has a string for the command plus a flag that determines what unit type it is appropriate
     * for.
     *
     * @author arlith
     */
    public enum ArtyAutoHitCommand implements PhaseCommand {
        SET_HIT_HEX("setAutoHitHex");

        String cmd;

        /**
         * Priority that determines this buttons order
         */
        private int priority;

        ArtyAutoHitCommand(String c) {
            cmd = c;
        }

        @Override
        public String getCmd() {
            return cmd;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            return Messages.getString("SelectArtyAutoHitHexDisplay." + getCmd());
        }

        public String getHotKeyDesc() {
            return "";
        }
    }

    // buttons
    protected Map<ArtyAutoHitCommand, MegaMekButton> buttons;

    private Player p;
    private PlayerIDAndList<Coords> artyAutoHitHexes = new PlayerIDAndList<>();

    private int startingHexes;

    protected final ClientGUI clientgui;

    /**
     * Creates and lays out a new select designated hex phase display for the specified clientgui.getClient().
     */
    public SelectArtyAutoHitHexDisplay(ClientGUI clientgui) {
        super(clientgui);
        this.clientgui = clientgui;
        clientgui.getClient().getGame().addGameListener(this);

        clientgui.getBoardView().addBoardViewListener(this);

        setupStatusBar(Messages.getString("SelectArtyAutoHitHexDisplay.waitingArtillery"));

        p = clientgui.getClient().getLocalPlayer();

        artyAutoHitHexes.setPlayerID(p.getId());

        setButtons();
        setButtonsTooltips();

        butDone.setText(Messages.getString("SelectArtyAutoHitHexDisplay.Done"));
        butDone.setEnabled(false);

        setupButtonPanel();

        registerKeyCommands();
    }

    @Override
    protected void setButtons() {
        buttons = new HashMap<>((int) (ArtyAutoHitCommand.values().length * 1.25 + 0.5));
        for (ArtyAutoHitCommand cmd : ArtyAutoHitCommand.values()) {
            String title = Messages.getString("SelectArtyAutoHitHexDisplay." + cmd.getCmd());
            MegaMekButton newButton = new MegaMekButton(title,
                  SkinSpecification.UIComponents.PhaseDisplayButton.getComp());
            newButton.addActionListener(this);
            newButton.setActionCommand(cmd.getCmd());
            newButton.setEnabled(false);
            buttons.put(cmd, newButton);
        }
        numButtonGroups = (int) Math.ceil((buttons.size() + 0.0) / buttonsPerGroup);
    }

    @Override
    protected void setButtonsTooltips() {
        for (ArtyAutoHitCommand cmd : ArtyAutoHitCommand.values()) {
            String ttKey = "SelectArtyAutoHitHexDisplay." + cmd.getCmd() + ".tooltip";
            String tt = cmd.getHotKeyDesc();
            if (!tt.isEmpty()) {
                String title = Messages.getString("SelectArtyAutoHitHexDisplay." + cmd.getCmd());
                tt = UIUtil.fontHTML(uiLightViolet()) + title + ": " + tt + "</FONT>";
                tt += "<BR>";
            }
            if (Messages.keyExists(ttKey)) {
                String msg_key = Messages.getString(ttKey);
                tt += msg_key;
            }
            if (!tt.isEmpty()) {
                String b = "<BODY>" + tt + "</BODY>";
                String h = "<HTML>" + b + "</HTML>";
                buttons.get(cmd).setToolTipText(h);
            }
        }
    }

    @Override
    protected ArrayList<MegaMekButton> getButtonList() {
        ArrayList<MegaMekButton> buttonList = new ArrayList<>();
        ArtyAutoHitCommand[] commands = ArtyAutoHitCommand.values();
        CommandComparator comparator = new CommandComparator();
        Arrays.sort(commands, comparator);
        for (ArtyAutoHitCommand cmd : commands) {
            buttonList.add(buttons.get(cmd));
        }
        return buttonList;
    }

    /**
     * Enables relevant buttons and sets up for your turn.
     */
    private void beginMyTurn() {
        // Make sure we've got the correct local player
        p = clientgui.getClient().getLocalPlayer();
        // By default, we should get 5 hexes per 4 mapsheets (4 mapsheets is
        // 16*17*4 hexes, so 1088)
        Game game = clientgui.getClient().getGame();
        Board board = game.getBoard();
        int preDesignateArea = game.getOptions().intOption(OptionsConstants.ADVCOMBAT_MAP_AREA_PREDESIGNATE);
        int hexesPer = game.getOptions().intOption(OptionsConstants.ADVCOMBAT_NUM_HEXES_PREDESIGNATE);
        double mapArea = board.getWidth() * board.getHeight();
        startingHexes = (int) Math.ceil((mapArea) / preDesignateArea) * hexesPer;
        artyAutoHitHexes.clear();
        setArtyEnabled(startingHexes);
        butDone.setEnabled(true);

        startTimer();
    }

    /**
     * Clears out old data and disables relevant buttons.
     */
    private void endMyTurn() {
        stopTimer();

        // end my turn, then.
        disableButtons();
        clientgui.getBoardView().select(null);
        clientgui.getBoardView().highlight(null);
        clientgui.getBoardView().cursor(null);
    }

    /**
     * Disables all buttons in the interface
     */
    private void disableButtons() {
        setArtyEnabled(0);

        butDone.setEnabled(false);
    }

    private void addArtyAutoHitHex(Coords coords) {
        if (!clientgui.getClient().getGame().getBoard().contains(coords)) {
            return;
        }
        if (!artyAutoHitHexes.contains(coords) &&
                  (artyAutoHitHexes.size() < startingHexes) &&
                  clientgui.doYesNoDialog(Messages.getString(
                              "SelectArtyAutoHitHexDisplay.setArtilleryTargetDialog.title"),
                        Messages.getString("SelectArtyAutoHitHexDisplay.setArtilleryTargetDialog.message",
                              coords.getBoardNum()))) {
            artyAutoHitHexes.addElement(coords);
            setArtyEnabled(startingHexes - artyAutoHitHexes.size());
            p.addArtyAutoHitHex(coords);
            clientgui.getClient()
                  .getGame()
                  .getBoard()
                  .addSpecialHexDisplay(coords,
                        new SpecialHexDisplay(SpecialHexDisplay.Type.ARTILLERY_AUTOHIT,
                              SpecialHexDisplay.NO_ROUND,
                              p,
                              "Artilery autohit, for player " + p.getName(),
                              SpecialHexDisplay.SHD_OBSCURED_TEAM));
            clientgui.getBoardView().refreshDisplayables();
        }
    }

    //
    // BoardListener
    //
    @Override
    public void hexMoused(BoardViewEvent b) {

        // Are we ignoring events?
        if (isIgnoringEvents()) {
            return;
        }

        if (b.getType() != BoardViewEvent.BOARD_HEX_DRAGGED) {
            return;
        }

        // ignore buttons other than 1
        if (!clientgui.getClient().isMyTurn() || (b.getButton() != MouseEvent.BUTTON1)) {
            return;
        }

        // check for a deployment
        clientgui.getBoardView().select(b.getCoords());
        addArtyAutoHitHex(b.getCoords());
    }

    //
    // GameListener
    //
    @Override
    public void gameTurnChange(GameTurnChangeEvent e) {
        // Are we ignoring events?
        if (isIgnoringEvents()) {
            return;
        }

        endMyTurn();

        if (clientgui.getClient().isMyTurn()) {
            beginMyTurn();
            setStatusBarText(Messages.getString("SelectArtyAutoHitHexDisplay.its_your_turn"));
            clientgui.bingMyTurn();
        } else {
            String playerName;
            if (e.getPlayer() != null) {
                playerName = e.getPlayer().getName();
            } else {
                playerName = "Unknown";
            }
            setStatusBarText(Messages.getString("SelectArtyAutoHitHexDisplay.its_others_turn", playerName));
            clientgui.bingOthersTurn();
        }
    }

    /**
     * called when the game changes phase.
     *
     * @param e ignored parameter
     */
    @Override
    public void gamePhaseChange(final GamePhaseChangeEvent e) {
        // Are we ignoring events?
        if (isIgnoringEvents()) {
            return;
        }

        if (clientgui.getClient().isMyTurn() &&
                  !clientgui.getClient().getGame().getPhase().isSetArtilleryAutohitHexes()) {
            endMyTurn();
        }

        if (clientgui.getClient().getGame().getPhase().isSetArtilleryAutohitHexes()) {
            setStatusBarText(Messages.getString("SelectArtyAutoHitHexDisplay.waitingMinefieldPhase"));
        }
    }

    //
    // ActionListener
    //
    @Override
    public void actionPerformed(ActionEvent ev) {
        // Are we ignoring events?
        if (isIgnoringEvents()) {
            return;
        }

        if (!clientgui.getClient().isMyTurn()) {
            // odd...
            return;
        }
    }

    @Override
    public void clear() {
        artyAutoHitHexes.clear();
        p.removeArtyAutoHitHexes();
        setArtyEnabled(startingHexes);
    }

    @Override
    public void ready() {
        endMyTurn();
        clientgui.getClient().sendArtyAutoHitHexes(artyAutoHitHexes);
        clientgui.getClient().sendPlayerInfo();
    }

    private void setArtyEnabled(int nbr) {
        buttons.get(ArtyAutoHitCommand.SET_HIT_HEX)
              .setText(Messages.getString("SelectArtyAutoHitHexDisplay." + ArtyAutoHitCommand.SET_HIT_HEX.getCmd(),
                    nbr));
        buttons.get(ArtyAutoHitCommand.SET_HIT_HEX).setEnabled(nbr > 0);
    }

    @Override
    public void removeAllListeners() {
        clientgui.getClient().getGame().removeGameListener(this);
        clientgui.getBoardView().removeBoardViewListener(this);
    }

    private void toggleShowDeployment() {
        clientgui.getBoardView().showAllDeployment = !clientgui.getBoardView().showAllDeployment;
        clientgui.getBoardView().getPanel().repaint();
    }

    private void registerKeyCommands() {
        clientgui.controller.registerCommandAction(KeyCommandBind.AUTO_ARTY_DEPLOYMENT_ZONE,
              this,
              this::toggleShowDeployment);
    }
}
