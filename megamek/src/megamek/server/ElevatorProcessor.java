/*
 * MegaMek -
 * Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import megamek.common.*;
import megamek.server.totalwarfare.TWGameManager;

/**
 * This is for simulating the vertically moving walls in the Solaris 7
 * colloseum.
 */
public class ElevatorProcessor extends DynamicTerrainProcessor {

    private ElevatorInfo[] elevators = null;

    public ElevatorProcessor(TWGameManager gameManager) {
        super(gameManager);
    }

    @Override
    public void doEndPhaseChanges(Vector<Report> vPhaseReport) {
        // 1st time, find elevators on board
        if (elevators == null || gameManager.getGame().getRoundCount() == 1) {
            elevators = new ElevatorInfo[6];
            for (int i = 0; i < 6; i++) {
                elevators[i] = new ElevatorInfo();
            }
            findElevators();
        }

        int roll = Compute.d6() - 1;
        if (elevators[roll].positions.isEmpty()) {
            return;
        }

        Report r = new Report(5290, Report.PUBLIC);
        vPhaseReport.add(r);

        for (BoardLocation c : elevators[roll].positions) {
            Hex hex = gameManager.getGame().getHex(c);
            Terrain terr = hex.getTerrain(Terrains.ELEVATOR);
            // Swap the elevator and hex elevations
            // Entity elevations are not adjusted. This makes sense for
            // everything except possibly
            // VTOLs - lets assume they take an updraft and remain at the same
            // height relative to the hex
            int elevation = hex.getLevel();
            hex.setLevel(terr.getLevel());
            hex.removeTerrain(Terrains.ELEVATOR);
            hex.addTerrain(new Terrain(Terrains.ELEVATOR, elevation, true, terr.getExits()));
            markHexUpdate(c.coords(), c.boardId());
        }
    }

    private void findElevators() {
        for (Board b : gameManager.getGame().getBoards().values()) {
            if (b.isLowAltitude() || b.isSpace()) {
                continue;
            }
            int height = b.getHeight();
            int width = b.getWidth();
            int exits;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (b.getHex(x, y).containsTerrain(Terrains.ELEVATOR)) {
                        exits = b.getHex(x, y).getTerrain(Terrains.ELEVATOR).getExits();
                        // add the elevator to each list it belongs in.
                        // exits are abused to hold which d6 roll(s) move this
                        // elevator
                        for (int z = 0; z < 6; z++) {
                            if ((exits & 1) == 1) {
                                elevators[z].positions.add(BoardLocation.of(new Coords(x, y), b.getBoardId()));
                            }
                            exits >>= 1;
                        }
                    }
                }
            }
        }
    }

    private static class ElevatorInfo {
        List<BoardLocation> positions = new ArrayList<>();
    }
}
