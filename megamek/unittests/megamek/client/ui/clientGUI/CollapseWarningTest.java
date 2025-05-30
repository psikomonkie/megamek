/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.clientGUI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import megamek.client.ui.clientGUI.boardview.CollapseWarning;
import org.junit.jupiter.api.Test;

import megamek.common.Board;
import megamek.common.Building;
import megamek.common.Coords;
import megamek.common.Entity;
import megamek.common.Game;
import megamek.common.enums.GamePhase;

class CollapseWarningTest {

    final private GUIPreferences GUIP = GUIPreferences.getInstance();

    @Test
    void testDefaultPreferenceForCFWarningIndicator() {
        // The default setting for Construction Factor warning indicators should be
        // true.
        assertTrue(GUIP.getDefaultBoolean(GUIPreferences.CONSTRUCTOR_FACTOR_WARNING));
    }

    @Test
    void testSetPreferenceForCFWarningIndicator() {
        // Set the preference for CF warning indicator to false and true.
        GUIP.setShowCFWarnings(false);
        assertFalse(GUIP.getShowCFWarnings());

        // Set back to true and test again.
        GUIP.setShowCFWarnings(true);
        assertTrue(GUIP.getShowCFWarnings());
    }

    @Test
    void testActionPerformedHandlerTogglesCFWarningPref() {
        // Test logic used when an action is performed.
        // When the action to toggle CF Warning is called, the state of the toggle
        // should flip.
        boolean expected = !GUIP.getShowCFWarnings();

        CollapseWarning.handleActionPerformed();

        // Expect to see the toggle state for CF Warning toggled after the handler is
        // called.
        assertEquals(expected, GUIP.getShowCFWarnings());
    }

    @Test
    void testShouldShowCFWarning() {
        // should return false if feature is disabled.
        boolean actual = CollapseWarning.shouldShow(GamePhase.DEPLOYMENT, false);
        assertFalse(actual);

        // should return true if enabled and in the deployment phase.
        actual = CollapseWarning.shouldShow(GamePhase.DEPLOYMENT, true);
        assertTrue(actual);

        // should return true if enabled and in the movement phase.
        actual = CollapseWarning.shouldShow(GamePhase.MOVEMENT, true);
        assertTrue(actual);

        // should return false if enabled and NOT in the movement phase.
        actual = CollapseWarning.shouldShow(GamePhase.FIRING, true);
        assertFalse(actual);
    }

    @Test
    void testConstructionFactorWarningFindMovementWarnings() {
        // Test happy path for finding a building collapse warning.
        Game g = mock(Game.class);

        Entity e = createMockEntityWith(new Coords(3, 3), 4, 6, 45.0, true, false);

        Coords buildingPosition = new Coords(3, 5);
        Building bld = createMockBuildingWith(buildingPosition, 20);

        Board b = createMockBoardWith(buildingPosition, bld);

        List<Coords> warnList = CollapseWarning.findCFWarningsMovement(g, e, b);

        assertTrue(warnList.size() > 0);
        assertEquals(buildingPosition, warnList.get(0));
    }

    @Test
    void testConstructionFactorWarningFindMovementWarningsOffBoard() {
        // Test where entity is an off board entity, should not create a warning.
        Game g = mock(Game.class);

        // Set off board status to true.
        Entity e = createMockEntityWith(new Coords(3, 3), 5, 3, 45.0, true, true);

        Coords buildingPosition = new Coords(3, 5);
        Building bld = createMockBuildingWith(buildingPosition, 20);

        Board b = createMockBoardWith(buildingPosition, bld);

        List<Coords> warnList = CollapseWarning.findCFWarningsMovement(g, e, b);

        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindMovementWarningsNullEntityPos() {
        // Test happy path for finding a Building collapse warning.
        Game g = mock(Game.class);

        // Set entity position to null coordinate.
        Entity e = createMockEntityWith(null, 5, 3, 45.0, true, false);

        Coords buildingPosition = new Coords(3, 5);
        Building bld = createMockBuildingWith(buildingPosition, 20);

        Board b = createMockBoardWith(buildingPosition, bld);

        List<Coords> warnList = CollapseWarning.findCFWarningsMovement(g, e, b);

        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindMovementWarningsLightEntity() {
        // Test where the unit is lighter than the building CF
        Game g = mock(Game.class);

        // Set entity to happy path - building CF will be set to larger value.
        Entity e = createMockEntityWith(new Coords(3, 5), 5, 3, 45.0, true, false);

        Coords buildingPosition = new Coords(3, 5);
        Building bld = createMockBuildingWith(buildingPosition, 90);

        Board b = createMockBoardWith(buildingPosition, bld);

        List<Coords> warnList = CollapseWarning.findCFWarningsMovement(g, e, b);

        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindMovementWarningsExceptionPath() {
        // Test an internal null condition that should be caught and return an empty
        // list.
        Game g = null;

        // Set entity to happy path.
        Entity e = createMockEntityWith(new Coords(3, 5), 5, 3, 45.0, true, false);

        Coords buildingPosition = new Coords(3, 5);
        Building bld = createMockBuildingWith(buildingPosition, 20);

        Board b = createMockBoardWith(buildingPosition, bld);

        List<Coords> warnList = CollapseWarning.findCFWarningsMovement(g, e, b);

        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindDeploymentWarnings() {
        // Test happy bath for handler when in deployment phase for a eligible
        // deployment hex.
        Coords expectedHex = new Coords(3, 6);
        Vector<Building> buildings = new Vector<Building>();
        Building bld = createMockBuildingWith(expectedHex, 20);
        buildings.add(bld);

        Entity e = createMockEntityWith(null, 5, 3, 70, true, false);

        // Set the mock board to return a building and say it's a deployable hex for the
        // entity.
        Board b = mock(Board.class);
        when(b.getBuildings()).thenReturn(buildings.elements());
        when(b.isLegalDeployment(expectedHex, e)).thenReturn(true);

        Game g = mock(Game.class);

        List<Coords> warnList = CollapseWarning.findCFWarningsDeployment(g, e, b);

        assertNotNull(warnList);
        assertEquals(expectedHex, warnList.get(0));
    }

    @Test
    void testConstructionFactorWarningFindDeploymentWarningsNotLegalHex() {
        // Test happy bath for handler when in deployment phase for a eligible
        // deployment hex.
        Coords expectedHex = new Coords(3, 6);
        Vector<Building> buildings = new Vector<Building>();
        Building bld = createMockBuildingWith(expectedHex, 20);
        buildings.add(bld);

        Entity e = createMockEntityWith(null, 5, 3, 70, true, false);

        // Set the mock board to return a building and say it's not a deployable hex for
        // the entity.
        Board b = mock(Board.class);
        when(b.getBuildings()).thenReturn(buildings.elements());
        // This is not a legal deployment hex for the selected entity - no warning.
        when(b.isLegalDeployment(expectedHex, e)).thenReturn(false);

        Game g = mock(Game.class);

        List<Coords> warnList = CollapseWarning.findCFWarningsDeployment(g, e, b);

        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindDeploymentWarningsNotGroundUnit() {
        // Test happy bath for handler when in deployment phase for a eligible
        // deployment hex.
        Coords expectedHex = new Coords(3, 6);
        Vector<Building> buildings = new Vector<Building>();
        Building bld = createMockBuildingWith(expectedHex, 20);
        buildings.add(bld);

        // Entity is not a ground unit.
        Entity e = createMockEntityWith(null, 5, 3, 70, false, false);

        // Set the mock board to return a building and say it's a deployable hex for the
        // entity.
        Board b = mock(Board.class);
        when(b.getBuildings()).thenReturn(buildings.elements());
        when(b.isLegalDeployment(expectedHex, e)).thenReturn(true);

        Game g = mock(Game.class);

        List<Coords> warnList = CollapseWarning.findCFWarningsDeployment(g, e, b);

        // Entity is not a ground unit - no warning.
        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindDeploymentWarningsLightEntity() {
        // Test happy bath for handler when in deployment phase for a eligible
        // deployment hex.
        Coords expectedHex = new Coords(3, 6);
        Vector<Building> buildings = new Vector<Building>();
        Building bld = createMockBuildingWith(expectedHex, 90);
        buildings.add(bld);

        // Entity is lighter than the CF of the building in a legal deploy hex, no
        // warning.
        Entity e = createMockEntityWith(null, 5, 3, 35.0, true, false);

        // Set the mock board to return a building and say it's a deployable hex for the
        // entity.
        Board b = mock(Board.class);
        when(b.getBuildings()).thenReturn(buildings.elements());
        when(b.isLegalDeployment(expectedHex, e)).thenReturn(true);

        Game g = mock(Game.class);

        List<Coords> warnList = CollapseWarning.findCFWarningsDeployment(g, e, b);

        // No warning for a unit lighter than the CF.
        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningFindDeploymentWarningsException() {
        // Test a null pointer exception occurring in the handler - return an empty list
        // and log an error.
        List<Coords> warnList = CollapseWarning.findCFWarningsDeployment(null, null, null);

        // On exception return an non-null empty list so no warnings sprites are
        // displayed.
        assertNotNull(warnList);
        assertEquals(0, warnList.size());
    }

    @Test
    void testConstructionFactorWarningWhiteList() {
        Entity e = mock(Entity.class);

        when(e.isGround()).thenReturn(true);
        when(e.isOffBoard()).thenReturn(false);
        assertTrue(CollapseWarning.entityTypeIsInWhiteList(e));

        when(e.isGround()).thenReturn(true);
        when(e.isOffBoard()).thenReturn(true);
        assertFalse(CollapseWarning.entityTypeIsInWhiteList(e));

        when(e.isGround()).thenReturn(false);
        when(e.isOffBoard()).thenReturn(true);
        assertFalse(CollapseWarning.entityTypeIsInWhiteList(e));
    }

    @Test
    void testConstructionFactorWarningCalcTotalWeightWithUnit() {
        double entityWeight = 35.0;
        double onBuildingWeight = 25.0;

        Game g = mock(Game.class);
        Entity e = createMockEntityWith(new Coords(3, 3), 5, 3, entityWeight, true, false);

        // Mock a 25 ton entity already on the building hex.
        Entity onBuilding = this.createMockEntityWith(new Coords(3, 7), 4, 6, onBuildingWeight, true, false);
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(onBuilding);

        when(g.getEntitiesVector(new Coords(3, 7), true)).thenReturn(entities);

        double totalWeight = CollapseWarning.calculateTotalTonnage(g, e, new Coords(3, 7));

        assertEquals(entityWeight + onBuildingWeight, totalWeight);
    }

    @Test
    void testConstructionFactorWarningCalcTotalWeightEntityOnBuilding() {
        // This test simulates the selected entity on a building. When
        // calculating the weight we don't want to double count ourselves. (we
        // are already accounting for our own weigh as the selected entity)
        double entityWeight = 35.0;

        Game g = mock(Game.class);
        Entity e = createMockEntityWith(new Coords(3, 3), 5, 3, entityWeight, true, false);

        // The selected entity will show up in the entities vector for the building.
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(e);

        when(g.getEntitiesVector(new Coords(3, 3), true)).thenReturn(entities);

        double totalWeight = CollapseWarning.calculateTotalTonnage(g, e, new Coords(3, 3));

        assertEquals(entityWeight, totalWeight);
    }

    @Test
    void testConstructionFactorWarningCalcTotalWeightVTOLOverHex() {
        // This tests a VTOL flying over a building the selected entity could enter.
        double entityWeight = 35.0;

        Game g = mock(Game.class);
        Entity e = createMockEntityWith(new Coords(3, 3), 5, 3, entityWeight, true, false);
        Entity vtol = createMockEntityWith(new Coords(3, 7), 10, 10, 20.0, true, false);
        when(vtol.isAirborneVTOLorWIGE()).thenReturn(true);

        // An airborne VTOL is in the entity vector over the building not contributing
        // to
        // the total weight.
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(vtol);

        when(g.getEntitiesVector(new Coords(3, 7), true)).thenReturn(entities);

        double totalWeight = CollapseWarning.calculateTotalTonnage(g, e, new Coords(3, 7));

        assertEquals(entityWeight, totalWeight);
    }

    @Test
    void testConstructionFactorWarningCalcTotalWeightAeroOverHex() {
        // This tests a VTOL flying over a building the selected entity could enter.
        double entityWeight = 35.0;

        Game g = mock(Game.class);
        Entity e = createMockEntityWith(new Coords(3, 3), 5, 3, entityWeight, true, false);
        // Say isGround() is false. (same as isAerospace()).
        Entity aero = createMockEntityWith(new Coords(3, 7), 10, 10, 50.0, false, false);

        // Mock an aerospace unit flying over the building not contributing to the total
        // weight.
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(aero);

        when(g.getEntitiesVector(new Coords(3, 7), true)).thenReturn(entities);

        double totalWeight = CollapseWarning.calculateTotalTonnage(g, e, new Coords(3, 7));

        assertEquals(entityWeight, totalWeight);
    }

    // Helper function to setup a mock entity with various attributes.
    private Entity createMockEntityWith(Coords pos, int run, int jump, double weight, boolean ground,
            boolean offBoard) {
        Entity e = mock(Entity.class);
        when(e.getPosition()).thenReturn(pos);
        when(e.isGround()).thenReturn(ground);
        when(e.isOffBoard()).thenReturn(offBoard);
        when(e.getRunMP()).thenReturn(run);
        when(e.getJumpMP()).thenReturn(jump);
        when(e.getWeight()).thenReturn(weight);

        return e;
    }

    // Helper function to setup mock building with position and construction factor.
    private Building createMockBuildingWith(Coords pos, int cf) {
        List<Coords> hexes = new ArrayList<Coords>();
        Building building = mock(Building.class);
        hexes.add(pos);
        when(building.getCurrentCF(pos)).thenReturn(cf);
        when(building.getCoordsList()).thenReturn(hexes);
        return building;
    }

    // Helper function to create a mock board
    private Board createMockBoardWith(Coords pos, Building bld) {
        Board b = mock(Board.class);
        when(b.getBuildingAt(pos)).thenReturn(bld);
        return b;
    }
}
