/*
 * MegaMek - Copyright (C) 2002-2003 Ben Mazur (bmazur@sev.org)
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
package megamek.common;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import megamek.client.ui.panels.phaseDisplay.lobby.LobbyUtility;
import megamek.common.util.BuildingTemplate;
import megamek.logging.MMLogger;
import megamek.utilities.xml.MMXMLUtility;

/**
 * MapSettings.java
 *
 * @author Ben
 * @since March 27, 2002, 1:07 PM
 */
@XmlRootElement(name = "ENVIRONMENT")
@XmlAccessorType(value = XmlAccessType.NONE)
public class MapSettings implements Serializable {
    private static final MMLogger logger = MMLogger.create(MapSettings.class);

    private static final long serialVersionUID = -6163977970758303066L;

    public static final String BOARD_RANDOM = "[RANDOM]";
    public static final String BOARD_SURPRISE = "[SURPRISE]";
    public static final String BOARD_GENERATED = "[GENERATED]";

    public static final int MOUNTAIN_PLAIN = 0;
    public static final int MOUNTAIN_VOLCANO_EXTINCT = 1;
    public static final int MOUNTAIN_VOLCANO_DORMANT = 2;
    public static final int MOUNTAIN_VOLCANO_ACTIVE = 3;
    public static final int MOUNTAIN_SNOWCAPPED = 4;
    public static final int MOUNTAIN_LAKE = 5;

    public static final int MEDIUM_GROUND = 0;
    public static final int MEDIUM_ATMOSPHERE = 1;
    public static final int MEDIUM_SPACE = 2;

    private static final String[] mediumNames = { "Ground", "Atmosphere", "Space" };

    @XmlElement(name = "WIDTH")
    private int boardWidth = 16;
    @XmlElement(name = "HEIGHT")
    private int boardHeight = 17;
    private int mapWidth = 1;
    private int mapHeight = 1;
    private int medium = MEDIUM_GROUND;

    private ArrayList<String> boardsSelected = new ArrayList<>();
    private ArrayList<String> boardsAvailable = new ArrayList<>();
    private ArrayList<BuildingTemplate> boardBuildings = new ArrayList<>();

    /**
     * Parameters for the Map Generator Parameters refer to a default map siz 16
     * x 17, with other size some of the parameters get linear transformed to
     * give good result for new size
     */

    /** how much hills there should be, Range 0..99 */
    @XmlElement(name = "HILLYNESS")
    private int hilliness = 40;
    /**
     * how much cliffs should there be, range 0-100 (% chance for each cliff candidate)
     */
    @XmlElement(name = "CLIFFS")
    private int cliffs = 0;
    /** Maximum difference between highest elevation and lowest sink */
    @XmlElement(name = "HILLELEVATIONRANGE")
    private int range = 5;
    /** Probabiltity for invertion of the map, Range 0..100 */
    @XmlElement(name = "HILLINVERTPROB")
    private int probInvert = 5;

    /** how much Lakes at least */
    @XmlElement(name = "WATERMINSPOTS")
    private int minWaterSpots = 1;
    /** how much Lakes at most */
    @XmlElement(name = "WATERMAXSPOTS")
    private int maxWaterSpots = 3;
    /** minimum size of a lake */
    @XmlElement(name = "WATERMINHEXES")
    private int minWaterSize = 5;
    /** maximum Size of a lake */
    @XmlElement(name = "WATERMAXHEXES")
    private int maxWaterSize = 10;
    /** probability for water deeper than lvl1, Range 0..100 */
    @XmlElement(name = "WATERDEEPPROB")
    private int probDeep = 33;

    /** how much forests at least */
    @XmlElement(name = "FORESTMINSPOTS")
    private int minForestSpots = 3;
    /** how much forests at most */
    @XmlElement(name = "FORESTMAXSPOTS")
    private int maxForestSpots = 8;
    /** minimum size of a forest */
    @XmlElement(name = "FORESTMINHEXES")
    private int minForestSize = 4;
    /** maximum Size of a forest */
    @XmlElement(name = "FORESTMAXHEXES")
    private int maxForestSize = 12;
    /** probability for heavy woods, Range 0..100 */
    @XmlElement(name = "FORESTHEAVYPROB")
    private int probHeavy = 30;
    /** probability for ultra woods, Range 0..100 */
    @XmlElement(name = "FORESTULTRAPROB")
    private int probUltra = 0;

    /** how much forests at least */
    @XmlElement(name = "JUNGLEMINSPOTS")
    private int minJungleSpots = 0;
    /** how much forests at most */
    @XmlElement(name = "JUNGLEMAXSPOTS")
    private int maxJungleSpots = 0;
    /** minimum size of a forest */
    @XmlElement(name = "JUNGLEMINHEXES")
    private int minJungleSize = 0;
    /** maximum Size of a forest */
    @XmlElement(name = "JUNGLEMAXHEXES")
    private int maxJungleSize = 0;
    /** probability for heavy woods, Range 0..100 */
    @XmlElement(name = "JUNGLEHEAVYPROB")
    private int probHeavyJungle = 0;
    /** probability for ultra woods, Range 0..100 */
    @XmlElement(name = "JUNGLEULTRAPROB")
    private int probUltraJungle = 0;

    /** how much foliage at least */
    @XmlElement(name = "FOLIAGEMINSPOTS")
    private int minFoliageSpots = 0;
    /** how much foliage at most */
    @XmlElement(name = "FOLIAGEMAXSPOTS")
    private int maxFoliageSpots = 0;
    /** minimum size of a forest */
    @XmlElement(name = "FOLIAGEMINHEXES")
    private int minFoliageSize = 0;
    /** maximum Size of a forest */
    @XmlElement(name = "FOLIAGEMAXHEXES")
    private int maxFoliageSize = 0;
    /** probability for heavy foliage, Range 0..100 */
    @XmlElement(name = "FOLIAGEHEAVYPROB")
    private int probFoliageHeavy = 0;

    /** how much rough spots at least */
    @XmlElement(name = "ROUGHMINSPOTS")
    private int minRoughSpots = 2;
    /** how much rough spots at most */
    @XmlElement(name = "ROUGHMAXSPOTS")
    private int maxRoughSpots = 10;
    /** minimum size of a rough spot */
    @XmlElement(name = "ROUGHMINHEXES")
    private int minRoughSize = 1;
    /** maximum Size of a rough spot */
    @XmlElement(name = "ROUGHMAXHEXES")
    private int maxRoughSize = 2;

    /** probability a rough spot is upgraded to an ultra-rough */
    @XmlElement(name = "ROUGHULTRAPROB")
    private int probUltraRough = 0;

    /** how much sand spots at least */
    @XmlElement(name = "SANDMINSPOTS")
    private int minSandSpots = 2;
    /** how much sand spots at most */
    @XmlElement(name = "SANDMAXSPOTS")
    private int maxSandSpots = 10;
    /** minimum size of a rough spot */
    @XmlElement(name = "SANDMINHEXES")
    private int minSandSize = 1;
    /** maximum Size of a rough spot */
    @XmlElement(name = "SANDMAXHEXES")
    private int maxSandSize = 2;

    /** how much snow spots at least */
    @XmlElement(name = "SNOWMINSPOTS")
    private int minSnowSpots = 0;
    /** how much snow spots at most */
    @XmlElement(name = "SNOWMAXSPOTS")
    private int maxSnowSpots = 0;
    /** minimum size of a snow spot */
    @XmlElement(name = "SNOWMINHEXES")
    private int minSnowSize = 0;
    /** maximum Size of a snow spot */
    @XmlElement(name = "SNOWMAXHEXES")
    private int maxSnowSize = 0;

    /** how much tundra spots at least */
    @XmlElement(name = "TUNDRAMINSPOTS")
    private int minTundraSpots = 0;
    /** how much tundra spots at most */
    @XmlElement(name = "TUNDRAMAXSPOTS")
    private int maxTundraSpots = 0;
    /** minimum size of a tundra spot */
    @XmlElement(name = "TUNDRAMINHEXES")
    private int minTundraSize = 0;
    /** maximum Size of a tundra spot */
    @XmlElement(name = "TUNDRAMAXHEXES")
    private int maxTundraSize = 0;

    /** how much planted field spots at least */
    @XmlElement(name = "PLANTEDFIELDMINSPOTS")
    private int minPlantedFieldSpots = 2;
    /** how much planted field spots at most */
    @XmlElement(name = "PLANTEDFIELDMAXSPOTS")
    private int maxPlantedFieldSpots = 10;
    /** minimum size of a planted field spot */
    @XmlElement(name = "PLANTEDFIELDMINHEXES")
    private int minPlantedFieldSize = 1;
    /** maximum size of a planted field spot */
    @XmlElement(name = "PLANTEDFIELDMAXHEXES")
    private int maxPlantedFieldSize = 2;

    /** how much swamp spots at least */
    @XmlElement(name = "SWAMPMINSPOTS")
    private int minSwampSpots = 2;
    /** how much swamp spots at most */
    @XmlElement(name = "SWAMPMAXSPOTS")
    private int maxSwampSpots = 10;
    /** minimum size of a swamp spot */
    @XmlElement(name = "SWAMPMINHEXES")
    private int minSwampSize = 1;
    /** maximum Size of a swamp spot */
    @XmlElement(name = "SWAMPMAXHEXES")
    private int maxSwampSize = 2;

    /** how much pavement spots at least */
    @XmlElement(name = "PAVEMENTMINSPOTS")
    private int minPavementSpots = 0;
    /** how much pavement spots at most */
    @XmlElement(name = "PAVEMENTMAXSPOTS")
    private int maxPavementSpots = 0;
    /** minimum size of a pavement spot */
    @XmlElement(name = "PAVEMENTMINHEXES")
    private int minPavementSize = 1;
    /** maximum Size of a pavement spot */
    @XmlElement(name = "PAVEMENTMAXHEXES")
    private int maxPavementSize = 6;

    /** how much rubble spots at least */
    @XmlElement(name = "RUBBLEMINSPOTS")
    private int minRubbleSpots = 0;
    /** how much rubble spots at most */
    @XmlElement(name = "RUBBLEMAXSPOTS")
    private int maxRubbleSpots = 0;
    /** minimum size of a rubble spot */
    @XmlElement(name = "RUBBLEMINHEXES")
    private int minRubbleSize = 1;
    /** maximum Size of a rubble spot */
    @XmlElement(name = "RUBBLEMAXHEXES")
    private int maxRubbleSize = 6;

    /** probability of a rubble ultraspot */
    @XmlElement(name = "RUBBLEULTRAPROB")
    private int probUltraRubble = 0;

    /** how much fortified spots at least */
    @XmlElement(name = "FORTIFIEDMINSPOTS")
    private int minFortifiedSpots = 0;
    /** how much fortified spots at most */
    @XmlElement(name = "FORTIFIEDMAXSPOTS")
    private int maxFortifiedSpots = 0;
    /** minimum size of a fortified spot */
    @XmlElement(name = "FORTIFIEDMINHEXES")
    private int minFortifiedSize = 1;
    /** maximum Size of a fortified spot */
    @XmlElement(name = "FORTIFIEDMAXHEXES")
    private int maxFortifiedSize = 2;

    /** how much ice spots at least */
    @XmlElement(name = "ICEMINSPOTS")
    private int minIceSpots = 0;
    /** how much ice spots at most */
    @XmlElement(name = "ICEMAXSPOTS")
    private int maxIceSpots = 0;
    /** minimum size of a ice spot */
    @XmlElement(name = "ICEMINHEXES")
    private int minIceSize = 1;
    /** maximum Size of a ice spot */
    @XmlElement(name = "ICEMAXHEXES")
    private int maxIceSize = 6;

    /** probability for a road, range 0..100 */
    @XmlElement(name = "ROADPROB")
    private int probRoad = 0;

    /** probability for a river, range 0..100 */
    @XmlElement(name = "RIVERPROB")
    private int probRiver = 0;

    /** probabilitay for Crater 0..100 */
    @XmlElement(name = "CRATEPROB")
    private int probCrater = 0;

    /** minimum Radius of the Craters */
    @XmlElement(name = "CRATERMINRADIUS")
    private int minRadius = 2;

    /** maximum Radius of the Craters */
    @XmlElement(name = "CRATERMAXRADIUS")
    private int maxRadius = 7;

    /** maximum Number of Craters on one map */
    @XmlElement(name = "CRATERMAXNUM")
    private int maxCraters = 2;

    /** minimum Number of Craters on one map */
    @XmlElement(name = "CRATERMINNUM")
    private int minCraters = 1;

    /** which landscape generation Algortihm to use */
    /* atm there are 2 different: 0= first, 1=second */
    @XmlElement(name = "ALGORITHM")
    private int algorithmToUse = 0;

    /** a tileset theme to apply */
    @XmlElement(name = "THEME")
    private String theme = "";

    /** probability of flooded map */
    @XmlElement(name = "PROBFLOOD")
    private int probFlood = 0;
    /** probability of forest fire */
    @XmlElement(name = "PROBFORESTFIRE")
    private int probForestFire = 0;
    /** probability of frozen map */
    @XmlElement(name = "PROBFREEZE")
    private int probFreeze = 0;
    /** probability of drought */
    @XmlElement(name = "PROBDROUGHT")
    private int probDrought = 0;
    /** special FX modifier */
    @XmlElement(name = "FXMOD")
    private int fxMod = 0;

    /** Parameters for the city generator */
    @XmlElement(name = "CITYBLOCKS")
    private int cityBlocks = 16;
    @XmlElement(name = "CITYTYPE")
    private String cityType = "NONE";
    @XmlElement(name = "MINCF")
    private int cityMinCF = 10;
    @XmlElement(name = "MAXCF")
    private int cityMaxCF = 100;
    @XmlElement(name = "MINFLOORS")
    private int cityMinFloors = 1;
    @XmlElement(name = "MAXFLOORS")
    private int cityMaxFloors = 6;
    @XmlElement(name = "CITYDENSITY")
    private int cityDensity = 75;
    @XmlElement(name = "TOWNSIZE")
    private int townSize = 60;

    @XmlElement(name = "INVERTNEGATIVETERRAIN")
    private int invertNegativeTerrain = 0;

    @XmlElement(name = "MOUNTPEAKS")
    private int mountainPeaks = 0;
    @XmlElement(name = "MOUNTWIDTHMIN")
    private int mountainWidthMin = 7;
    @XmlElement(name = "MOUNTWIDTHMAX")
    private int mountainWidthMax = 20;
    @XmlElement(name = "MOUNTHEIGHTMIN")
    private int mountainHeightMin = 5;
    @XmlElement(name = "MOUNTHEIGHTMAX")
    private int mountainHeightMax = 8;
    @XmlElement(name = "MOUNTSTYLE")
    private int mountainStyle = MOUNTAIN_PLAIN;

    /** end Map Generator Parameters */

    /**
     * Creates and returns a new default instance of MapSettings.
     *
     * @return a MapSettings with default settings values
     */
    public static MapSettings getInstance() {
        return new MapSettings();
    }

    /**
     * Creates and returns a clone of the given MapSettings.
     *
     * @param other the MapSettings to clone
     *
     * @return a MapSettings with the cloned settings values
     */
    public static MapSettings getInstance(final MapSettings other) {
        return new MapSettings(other);
    }

    /**
     * Creates and returns a new instance of MapSettings with default values loaded from the given input stream.
     *
     * @param is the input stream that contains an XML representation of the map settings
     *
     * @return a MapSettings with the values from XML
     */
    public static MapSettings getInstance(final InputStream is) {
        MapSettings ms = null;

        try {
            JAXBContext jc = JAXBContext.newInstance(MapSettings.class);

            Unmarshaller um = jc.createUnmarshaller();
            ms = (MapSettings) um.unmarshal(MMXMLUtility.createSafeXmlSource(is));
        } catch (Exception e) {
            logger.error("Error loading XML for map settings: " + e.getMessage(), e);
        }

        return ms;
    }

    /** Creates new MapSettings */
    private MapSettings() {
        this(megamek.common.preference.PreferenceManager.getClientPreferences().getBoardWidth(),
              megamek.common.preference.PreferenceManager.getClientPreferences().getBoardHeight(),
              megamek.common.preference.PreferenceManager.getClientPreferences().getMapWidth(),
              megamek.common.preference.PreferenceManager.getClientPreferences().getMapHeight());
    }

    /** Create new MapSettings with all size settings specified */
    private MapSettings(int boardWidth, int boardHeight, int mapWidth, int mapHeight) {
        setBoardSize(boardWidth, boardHeight);
        setMapSize(mapWidth, mapHeight);
    }

    /** Creates new MapSettings that is a duplicate of another */
    private MapSettings(MapSettings other) {
        boardWidth = other.getBoardWidth();
        boardHeight = other.getBoardHeight();
        mapWidth = other.getMapWidth();
        mapHeight = other.getMapHeight();

        medium = other.getMedium();

        boardsSelected = new ArrayList<>(other.getBoardsSelectedVector());
        boardsAvailable = new ArrayList<>(other.getBoardsAvailableVector());

        invertNegativeTerrain = other.getInvertNegativeTerrain();
        mountainHeightMin = other.getMountainHeightMin();
        mountainHeightMax = other.getMountainHeightMax();
        mountainPeaks = other.getMountainPeaks();
        mountainStyle = other.getMountainStyle();
        mountainWidthMin = other.getMountainWidthMin();
        mountainWidthMax = other.getMountainWidthMax();
        hilliness = other.getHilliness();
        cliffs = other.getCliffs();
        range = other.getRange();
        probInvert = other.getProbInvert();
        minWaterSpots = other.getMinWaterSpots();
        maxWaterSpots = other.getMaxWaterSpots();
        minWaterSize = other.getMinWaterSize();
        maxWaterSize = other.getMaxWaterSize();
        probDeep = other.getProbDeep();
        minForestSpots = other.getMinForestSpots();
        maxForestSpots = other.getMaxForestSpots();
        minForestSize = other.getMinForestSize();
        maxForestSize = other.getMaxForestSize();
        probHeavy = other.getProbHeavy();
        probUltra = other.getProbUltra();
        minJungleSpots = other.getMinJungleSpots();
        maxJungleSpots = other.getMaxJungleSpots();
        minJungleSize = other.getMinJungleSize();
        maxJungleSize = other.getMaxJungleSize();
        probHeavyJungle = other.getProbHeavyJungle();
        probUltraJungle = other.getProbUltraJungle();
        minFoliageSpots = other.getMinFoliageSpots();
        maxFoliageSpots = other.getMaxFoliageSpots();
        minFoliageSize = other.getMinFoliageSize();
        maxFoliageSize = other.getMaxFoliageSize();
        probFoliageHeavy = other.getProbFoliageHeavy();
        minRoughSpots = other.getMinRoughSpots();
        maxRoughSpots = other.getMaxRoughSpots();
        minRoughSize = other.getMinRoughSize();
        maxRoughSize = other.getMaxRoughSize();
        probUltraRough = other.getProbUltraRough();
        minSandSpots = other.getMinSandSpots();
        maxSandSpots = other.getMaxSandSpots();
        minSandSize = other.getMinSandSize();
        maxSandSize = other.getMaxSandSize();
        minSnowSpots = other.getMinSnowSpots();
        maxSnowSpots = other.getMaxSnowSpots();
        minSnowSize = other.getMinSnowSize();
        maxSnowSize = other.getMaxSnowSize();
        minTundraSpots = other.getMinTundraSpots();
        maxTundraSpots = other.getMaxTundraSpots();
        minTundraSize = other.getMinTundraSize();
        maxTundraSize = other.getMaxTundraSize();
        minPlantedFieldSpots = other.getMinPlantedFieldSpots();
        maxPlantedFieldSpots = other.getMaxPlantedFieldSpots();
        minPlantedFieldSize = other.getMinPlantedFieldSize();
        maxPlantedFieldSize = other.getMaxPlantedFieldSize();
        minSwampSpots = other.getMinSwampSpots();
        maxSwampSpots = other.getMaxSwampSpots();
        minSwampSize = other.getMinSwampSize();
        maxSwampSize = other.getMaxSwampSize();
        minPavementSpots = other.getMinPavementSpots();
        maxPavementSpots = other.getMaxPavementSpots();
        minPavementSize = other.getMinPavementSize();
        maxPavementSize = other.getMaxPavementSize();
        minRubbleSpots = other.getMinRubbleSpots();
        maxRubbleSpots = other.getMaxRubbleSpots();
        minRubbleSize = other.getMinRubbleSize();
        maxRubbleSize = other.getMaxRubbleSize();
        probUltraRubble = other.getProbUltraRubble();
        minFortifiedSpots = other.getMinFortifiedSpots();
        maxFortifiedSpots = other.getMaxFortifiedSpots();
        minFortifiedSize = other.getMinFortifiedSize();
        maxFortifiedSize = other.getMaxFortifiedSize();
        minIceSpots = other.getMinIceSpots();
        maxIceSpots = other.getMaxIceSpots();
        minIceSize = other.getMinIceSize();
        maxIceSize = other.getMaxIceSize();
        probRoad = other.getProbRoad();
        probRiver = other.getProbRiver();
        probCrater = other.getProbCrater();
        minRadius = other.getMinRadius();
        maxRadius = other.getMaxRadius();
        minCraters = other.getMinCraters();
        maxCraters = other.getMaxCraters();
        algorithmToUse = other.getAlgorithmToUse();
        theme = other.getTheme();
        probFlood = other.getProbFlood();
        probForestFire = other.getProbForestFire();
        probFreeze = other.getProbFreeze();
        probDrought = other.getProbDrought();
        fxMod = other.getFxMod();
        cityBlocks = other.getCityBlocks();
        cityType = other.getCityType();
        cityMinCF = other.getCityMinCF();
        cityMaxCF = other.getCityMaxCF();
        cityMinFloors = other.getCityMinFloors();
        cityMaxFloors = other.getCityMaxFloors();
        cityDensity = other.getCityDensity();
        boardBuildings = other.getBoardBuildings();
        townSize = other.getTownSize();
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void setBoardSize(int boardWidth, int boardHeight) {
        if ((boardWidth <= 0) || (boardHeight <= 0)) {
            throw new IllegalArgumentException("Total board area must be positive");
        }

        // change only if actually different
        if ((this.boardWidth != boardWidth) || (this.boardHeight != boardHeight)) {
            this.boardWidth = boardWidth;
            this.boardHeight = boardHeight;

            boardsAvailable.clear();
        }
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String th) {
        theme = th;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public BoardDimensions getBoardSize() {
        return new BoardDimensions(boardWidth, boardHeight);
    }

    public void setMapSize(int newWidth, int newHeight) {
        if ((mapWidth <= 0) || (mapHeight <= 0)) {
            throw new IllegalArgumentException("Map width and height must be at least 1!");
        }

        // If the map size doesn't correspond to the size of boardsselected, correct
        // that first to be safe, as the changes below are always relative changes.
        // This happens with MapSettings construction
        if (boardsSelected.size() != mapWidth * mapHeight) {
            boardsSelected.clear();
            boardsSelected.addAll(Collections.nCopies(mapWidth * mapHeight, null));
        }

        // Change in height
        if (newHeight > mapHeight) {
            boardsSelected.addAll(Collections.nCopies(mapWidth * newHeight - boardsSelected.size(), null));
        } else if (newHeight < mapHeight) {
            boardsSelected.subList(mapWidth * newHeight, boardsSelected.size()).clear();
        }

        mapHeight = newHeight;

        // Change in width
        if (newWidth > mapWidth) {
            // Add empty boards at the end of each row
            for (int row = mapHeight - 1; row >= 0; row--) {
                boardsSelected.addAll(mapWidth * (row + 1), Collections.nCopies(newWidth - mapWidth, null));
            }
        } else if (newWidth < mapWidth) {
            // Remove boards at the end of each row
            for (int row = mapHeight - 1; row >= 0; row--) {
                boardsSelected.subList(mapWidth * row + newWidth, mapWidth * (row + 1)).clear();
            }
        }

        mapWidth = newWidth;
    }

    public List<String> getBoardsSelectedVector() {
        return boardsSelected;
    }

    public void setBoardsSelectedVector(List<String> newBoards) {
        boardsSelected.clear();
        boardsSelected.addAll(newBoards);
    }

    /**
     * Fills in all nulls in the boards selected list with the specified board
     */
    public void setNullBoards(String board) {
        for (int i = 0; i < boardsSelected.size(); i++) {
            if (boardsSelected.get(i) == null) {
                boardsSelected.set(i, board);
            }
        }
    }

    public ArrayList<BuildingTemplate> getBoardBuildings() {
        return boardBuildings;
    }

    public void setBoardBuildings(ArrayList<BuildingTemplate> buildings) {
        boardBuildings = buildings;
    }

    /**
     * Replaces all "Surprise..." boards with a random one of the chosen boards (which are appended after the "Surprise"
     * string in the board name.)
     */
    public void chooseSurpriseBoards() {
        for (int i = 0; i < boardsSelected.size(); i++) {
            if (boardsSelected.get(i).startsWith(BOARD_SURPRISE)) {
                List<String> boards = LobbyUtility.extractSurpriseMaps(boardsSelected.get(i));
                int rnd = (int) (Math.random() * boards.size());
                boardsSelected.set(i, boards.get(rnd));
            }
        }
    }

    /**
     * Replaces the specified type of board with random boards
     */
    public void replaceBoardWithRandom(String board) {
        if (board == null) {
            return;
        }
        for (int i = 0; i < boardsSelected.size(); i++) {
            if (board.equals(boardsSelected.get(i))) {
                int rindex = 0;
                boolean nonFound = true;
                while (nonFound) {
                    // if we have no boards, set rindex to 0, so the generated
                    // board
                    // gets selected
                    // Default boards are GENERATED, RANDOM and SUPPRISE
                    if (boardsAvailable.size() < 4) {
                        rindex = 0;
                        nonFound = false;
                    } else {
                        rindex = Compute.randomInt(boardsAvailable.size() - 3) + 3;
                        // validate that the selected map is legal
                        Board b = new Board(16, 17);
                        String boardSelected = boardsAvailable.get(rindex);
                        if (!MapSettings.BOARD_GENERATED.equals(boardSelected) &&
                                  !MapSettings.BOARD_RANDOM.equals(boardSelected) &&
                                  !MapSettings.BOARD_SURPRISE.equals(boardSelected)) {
                            b.load(new File(Configuration.boardsDir(), boardSelected + ".board"));
                            if (b.isValid()) {
                                nonFound = false;
                            } else {
                                boardsAvailable.remove(rindex);
                            }
                        }
                    }
                }
                // Do a one pi rotation half of the time.
                if (0 == Compute.randomInt(2)) {
                    boardsSelected.set(i, Board.BOARD_REQUEST_ROTATION + boardsAvailable.get(rindex));
                } else {
                    boardsSelected.set(i, boardsAvailable.get(rindex));
                }
            }
        }
    }

    /**
     * Removes selected boards that aren't listed in the available boards
     */
    public void removeUnavailable() {
        for (int i = 0; i < boardsSelected.size(); i++) {
            String boardName = boardsSelected.get(i);
            if (boardName != null) {
                boardName = boardName.replace(Board.BOARD_REQUEST_ROTATION, "");

                if (boardName.startsWith(MapSettings.BOARD_SURPRISE)) {
                    List<String> boards = LobbyUtility.extractSurpriseMaps(boardName);
                    ArrayList<String> remainingBoards = new ArrayList<>();
                    for (String board : boards) {
                        if (boardsAvailable.contains(board)) {
                            remainingBoards.add(board);
                        }
                    }
                    if (remainingBoards.isEmpty()) {
                        boardsSelected.set(i, null);
                    } else if (remainingBoards.size() == 1) {
                        boardsSelected.set(i, remainingBoards.get(0));
                    } else {
                        String remBoards = String.join("\n", boards);
                        boardsSelected.set(i, MapSettings.BOARD_SURPRISE + remBoards);
                    }
                } else {
                    if (!boardsAvailable.contains(boardName)) {
                        boardsSelected.set(i, null);
                    }
                }
            }
        }
    }

    public ArrayList<String> getBoardsAvailableVector() {
        return boardsAvailable;
    }

    public void setBoardsAvailableVector(List<String> newBoards) {
        boardsAvailable.clear();
        boardsAvailable.addAll(newBoards);
    }

    /**
     * Checks, if the Mapgenerator parameters are all valid. If not they are changed to valid values.
     */
    public void validateMapGenParameters() {
        if (hilliness < 0) {
            hilliness = 0;
        }
        if (hilliness > 99) {
            hilliness = 99;
        }
        if (cliffs < 0) {
            cliffs = 0;
        }
        if (cliffs > 100) {
            cliffs = 100;
        }
        if (range < 0) {
            range = 0;
        }
        if (minWaterSpots < 0) {
            minWaterSpots = 0;
        }
        if (maxWaterSpots < minWaterSpots) {
            maxWaterSpots = minWaterSpots;
        }
        if (minWaterSize < 0) {
            minWaterSize = 0;
        }
        if (maxWaterSize < minWaterSize) {
            maxWaterSize = minWaterSize;
        }
        if (probDeep < 0) {
            probDeep = 0;
        }
        if (probDeep > 100) {
            probDeep = 100;
        }
        if (minForestSpots < 0) {
            minForestSpots = 0;
        }
        if (maxForestSpots < minForestSpots) {
            maxForestSpots = minForestSpots;
        }
        if (minForestSize < 0) {
            minForestSize = 0;
        }
        if (maxForestSize < minForestSize) {
            maxForestSize = minForestSize;
        }
        if (probHeavy < 0) {
            probHeavy = 0;
        }
        if (probHeavy > 100) {
            probHeavy = 100;
        }
        if (probUltra < 0) {
            probUltra = 0;
        }
        if (probUltra > 100) {
            probUltra = 100;
        }

        if (minJungleSpots < 0) {
            minJungleSpots = 0;
        }
        if (maxJungleSpots < minJungleSpots) {
            maxJungleSpots = minJungleSpots;
        }
        if (minJungleSize < 0) {
            minJungleSize = 0;
        }
        if (maxJungleSize < minJungleSize) {
            maxJungleSize = minJungleSize;
        }
        if (probHeavyJungle < 0) {
            probHeavyJungle = 0;
        }
        if (probHeavyJungle > 100) {
            probHeavyJungle = 100;
        }
        if (probUltraJungle < 0) {
            probUltraJungle = 0;
        }
        if (probUltraJungle > 100) {
            probUltraJungle = 100;
        }

        if (minFoliageSpots < 0) {
            minFoliageSpots = 0;
        }
        if (maxFoliageSpots < minFoliageSpots) {
            maxFoliageSpots = minFoliageSpots;
        }
        if (minFoliageSize < 0) {
            minFoliageSize = 0;
        }
        if (maxFoliageSize < minFoliageSize) {
            maxFoliageSize = minFoliageSize;
        }
        if (probFoliageHeavy < 0) {
            probFoliageHeavy = 0;
        }
        if (probFoliageHeavy > 100) {
            probFoliageHeavy = 100;
        }
        if (minRoughSpots < 0) {
            minRoughSpots = 0;
        }
        if (maxRoughSpots < minRoughSpots) {
            maxRoughSpots = minRoughSpots;
        }
        if (minRoughSize < 0) {
            minRoughSize = 0;
        }
        if (maxRoughSize < minRoughSize) {
            maxRoughSize = minRoughSize;
        }
        if (probUltraRough < 0) {
            probUltraRough = 0;
        }
        if (probUltraRough > 100) {
            probUltraRough = 100;
        }
        if (minSandSpots < 0) {
            minSandSpots = 0;
        }
        if (maxSandSpots < minSandSpots) {
            maxSandSpots = minSandSpots;
        }
        if (minSandSize < 0) {
            minSandSize = 0;
        }
        if (maxSandSize < minSandSize) {
            maxSandSize = minSandSize;
        }
        if (minSnowSpots < 0) {
            minSnowSpots = 0;
        }
        if (maxSnowSpots < minSnowSpots) {
            maxSnowSpots = minSnowSpots;
        }
        if (minSnowSize < 0) {
            minSnowSize = 0;
        }
        if (maxSnowSize < minSnowSize) {
            maxSnowSize = minSnowSize;
        }
        if (minTundraSpots < 0) {
            minTundraSpots = 0;
        }
        if (maxTundraSpots < minTundraSpots) {
            maxTundraSpots = minTundraSpots;
        }
        if (minTundraSize < 0) {
            minTundraSize = 0;
        }
        if (maxTundraSize < minTundraSize) {
            maxTundraSize = minTundraSize;
        }
        if (minPlantedFieldSpots < 0) {
            minPlantedFieldSpots = 0;
        }
        if (maxPlantedFieldSpots < minPlantedFieldSpots) {
            maxPlantedFieldSpots = minPlantedFieldSpots;
        }
        if (minPlantedFieldSize < 0) {
            minPlantedFieldSize = 0;
        }
        if (maxPlantedFieldSize < minPlantedFieldSize) {
            maxPlantedFieldSize = minPlantedFieldSize;
        }
        if (minSwampSpots < 0) {
            minSwampSpots = 0;
        }
        if (maxSwampSpots < minSwampSpots) {
            maxSwampSpots = minSwampSpots;
        }
        if (minSwampSize < 0) {
            minSwampSize = 0;
        }
        if (maxSwampSize < minSwampSize) {
            maxSwampSize = minSwampSize;
        }
        if (minPavementSpots < 0) {
            minPavementSpots = 0;
        }
        if (maxPavementSpots < minPavementSpots) {
            maxPavementSpots = minPavementSpots;
        }
        if (minPavementSize < 0) {
            minPavementSize = 0;
        }
        if (maxPavementSize < minPavementSize) {
            maxPavementSize = minPavementSize;
        }
        if (minRubbleSpots < 0) {
            minRubbleSpots = 0;
        }
        if (maxRubbleSpots < minRubbleSpots) {
            maxRubbleSpots = minRubbleSpots;
        }
        if (minRubbleSize < 0) {
            minRubbleSize = 0;
        }
        if (maxRubbleSize < minRubbleSize) {
            maxRubbleSize = minRubbleSize;
        }
        if (probUltraRubble < 0) {
            probUltraRubble = 0;
        }
        if (probUltraRubble > 100) {
            probUltraRubble = 100;
        }
        if (minFortifiedSpots < 0) {
            minFortifiedSpots = 0;
        }
        if (maxFortifiedSpots < minFortifiedSpots) {
            maxFortifiedSpots = minFortifiedSpots;
        }
        if (minFortifiedSize < 0) {
            minFortifiedSize = 0;
        }
        if (maxFortifiedSize < minFortifiedSize) {
            maxFortifiedSize = minFortifiedSize;
        }
        if (minIceSpots < 0) {
            minIceSpots = 0;
        }
        if (maxIceSpots < minIceSpots) {
            maxIceSpots = minIceSpots;
        }
        if (minIceSize < 0) {
            minIceSize = 0;
        }
        if (maxIceSize < minIceSize) {
            maxIceSize = minIceSize;
        }
        if (probRoad < 0) {
            probRoad = 0;
        }
        if (probRoad > 100) {
            probRoad = 100;
        }
        if (probInvert < 0) {
            probInvert = 0;
        }
        if (probInvert > 100) {
            probInvert = 100;
        }
        if (probRiver < 0) {
            probRiver = 0;
        }
        if (probRiver > 100) {
            probRiver = 100;
        }
        if (probCrater < 0) {
            probCrater = 0;
        }
        if (probCrater > 100) {
            probCrater = 100;
        }
        if (minRadius < 0) {
            minRadius = 0;
        }
        if (maxRadius < minRadius) {
            maxRadius = minRadius;
        }
        if (minCraters < 0) {
            minCraters = 0;
        }
        if (maxCraters < minCraters) {
            maxCraters = minCraters;
        }
        if (algorithmToUse < 0) {
            algorithmToUse = 0;
        }
        if (algorithmToUse > 2) {
            algorithmToUse = 2;
        }
    } /* validateMapGenParameters */

    /**
     * Returns true if the this Mapsetting has the same mapgenerator settings and size as the parameter.
     *
     * @param other The Mapsetting to which compare.
     *
     * @return True if settings are the same.
     */
    public boolean equalMapGenParameters(MapSettings other) {
        return (boardWidth == other.getBoardWidth()) &&
                     (boardHeight == other.getBoardHeight()) &&
                     (mapWidth == other.getMapWidth()) &&
                     (mapHeight == other.getMapHeight()) &&
                     (invertNegativeTerrain == other.getInvertNegativeTerrain()) &&
                     (hilliness == other.getHilliness()) &&
                     (cliffs == other.getCliffs()) &&
                     (range == other.getRange()) &&
                     (minWaterSpots == other.getMinWaterSpots()) &&
                     (maxWaterSpots == other.getMaxWaterSpots()) &&
                     (minWaterSize == other.getMinWaterSize()) &&
                     (maxWaterSize == other.getMaxWaterSize()) &&
                     (probDeep == other.getProbDeep()) &&
                     (minForestSpots == other.getMinForestSpots()) &&
                     (maxForestSpots == other.getMaxForestSpots()) &&
                     (minForestSize == other.getMinForestSize()) &&
                     (maxForestSize == other.getMaxForestSize()) &&
                     (probHeavy == other.getProbHeavy()) &&
                     (probUltra == other.getProbUltra()) &&
                     (minJungleSpots == other.getMinJungleSpots()) &&
                     (maxJungleSpots == other.getMaxJungleSpots()) &&
                     (minJungleSize == other.getMinJungleSize()) &&
                     (maxJungleSize == other.getMaxJungleSize()) &&
                     (probHeavyJungle == other.getProbHeavyJungle()) &&
                     (probUltraJungle == other.getProbUltraJungle()) &&
                     (minFoliageSpots == other.getMinFoliageSpots()) &&
                     (maxFoliageSpots == other.getMaxFoliageSpots()) &&
                     (minFoliageSize == other.getMinFoliageSize()) &&
                     (maxFoliageSize == other.getMaxFoliageSize()) &&
                     (probFoliageHeavy == other.getProbFoliageHeavy()) &&
                     (minRoughSpots == other.getMinRoughSpots()) &&
                     (maxRoughSpots == other.getMaxRoughSpots()) &&
                     (minRoughSize == other.getMinRoughSize()) &&
                     (maxRoughSize == other.getMaxRoughSize() && (probUltraRough == other.getProbUltraRough())) &&
                     (minSandSpots == other.getMinSandSpots()) &&
                     (maxSandSpots == other.getMaxSandSpots()) &&
                     (minSandSize == other.getMinSandSize()) &&
                     (maxSandSize == other.getMaxSandSize()) &&
                     (minSnowSpots == other.getMinSnowSpots()) &&
                     (maxSnowSpots == other.getMaxSnowSpots()) &&
                     (minSnowSize == other.getMinSnowSize()) &&
                     (maxSnowSize == other.getMaxSnowSize()) &&
                     (minTundraSpots == other.getMinTundraSpots()) &&
                     (maxTundraSpots == other.getMaxTundraSpots()) &&
                     (minTundraSize == other.getMinTundraSize()) &&
                     (maxTundraSize == other.getMaxTundraSize()) &&
                     (minPlantedFieldSpots == other.getMinPlantedFieldSpots()) &&
                     (maxPlantedFieldSpots == other.getMaxPlantedFieldSpots()) &&
                     (minPlantedFieldSize == other.getMinPlantedFieldSize()) &&
                     (maxPlantedFieldSize == other.getMaxPlantedFieldSize()) &&
                     (minSwampSpots == other.getMinSwampSpots()) &&
                     (maxSwampSpots == other.getMaxSwampSpots()) &&
                     (minSwampSize == other.getMinSwampSize()) &&
                     (maxSwampSize == other.getMaxSwampSize()) &&
                     (minPavementSpots == other.getMinPavementSpots()) &&
                     (maxPavementSpots == other.getMaxPavementSpots()) &&
                     (minPavementSize == other.getMinPavementSize()) &&
                     (maxPavementSize == other.getMaxPavementSize()) &&
                     (minRubbleSpots == other.getMinRubbleSpots()) &&
                     (maxRubbleSpots == other.getMaxRubbleSpots()) &&
                     (minRubbleSize == other.getMinRubbleSize()) &&
                     (maxRubbleSize == other.getMaxRubbleSize()) &&
                     (minFortifiedSpots == other.getMinFortifiedSpots() &&
                            (probUltraRubble == other.getProbUltraRubble())) &&
                     (maxFortifiedSpots == other.getMaxFortifiedSpots()) &&
                     (minFortifiedSize == other.getMinFortifiedSize()) &&
                     (maxFortifiedSize == other.getMaxFortifiedSize()) &&
                     (minIceSpots == other.getMinIceSpots()) &&
                     (maxIceSpots == other.getMaxIceSpots()) &&
                     (minIceSize == other.getMinIceSize()) &&
                     (maxIceSize == other.getMaxIceSize()) &&
                     (probRoad == other.getProbRoad()) &&
                     (probInvert == other.getProbInvert()) &&
                     (probRiver == other.getProbRiver()) &&
                     (probCrater == other.getProbCrater()) &&
                     (minRadius == other.getMinRadius()) &&
                     (maxRadius == other.getMaxRadius()) &&
                     (minCraters == other.getMinCraters()) &&
                     (maxCraters == other.getMaxCraters()) &&
                     (theme.equals(other.getTheme())) &&
                     (fxMod == other.getFxMod()) &&
                     (cityBlocks == other.getCityBlocks()) &&
                     (cityType.equals(other.getCityType())) &&
                     (cityMinCF == other.getCityMinCF()) &&
                     (cityMaxCF == other.getCityMaxCF()) &&
                     (cityMinFloors == other.getCityMinFloors()) &&
                     (cityMaxFloors == other.getCityMaxFloors()) &&
                     (cityDensity == other.getCityDensity()) &&
                     (probFlood == other.getProbFlood()) &&
                     (probForestFire == other.getProbForestFire()) &&
                     (probFreeze == other.getProbFreeze()) &&
                     (probDrought == other.getProbDrought()) &&
                     (algorithmToUse == other.getAlgorithmToUse()) &&
                     (mountainHeightMin == other.getMountainHeightMin()) &&
                     (mountainHeightMax == other.getMountainHeightMax()) &&
                     (mountainPeaks == other.getMountainPeaks()) &&
                     (mountainStyle == other.getMountainStyle()) &&
                     (mountainWidthMin == other.getMountainWidthMin()) &&
                     (mountainWidthMax == other.getMountainWidthMax()) &&
                     (boardBuildings.equals(other.getBoardBuildings()) && (medium == other.medium));
    }

    public int getInvertNegativeTerrain() {
        return invertNegativeTerrain;
    }

    public int getHilliness() {
        return hilliness;
    }

    public int getCliffs() {
        return cliffs;
    }

    public int getRange() {
        return range;
    }

    public int getProbInvert() {
        return probInvert;
    }

    public int getMinWaterSpots() {
        return minWaterSpots;
    }

    public int getMaxWaterSpots() {
        return maxWaterSpots;
    }

    public int getMinWaterSize() {
        return minWaterSize;
    }

    public int getMaxWaterSize() {
        return maxWaterSize;
    }

    public int getProbDeep() {
        return probDeep;
    }

    public int getMinForestSpots() {
        return minForestSpots;
    }

    public int getMaxForestSpots() {
        return maxForestSpots;
    }

    public int getMinForestSize() {
        return minForestSize;
    }

    public int getMaxForestSize() {
        return maxForestSize;
    }

    public int getProbHeavy() {
        return probHeavy;
    }

    public int getProbUltra() {
        return probUltra;
    }

    public int getMinJungleSpots() {
        return minJungleSpots;
    }

    public int getMaxJungleSpots() {
        return maxJungleSpots;
    }

    public int getMinJungleSize() {
        return minJungleSize;
    }

    public int getMaxJungleSize() {
        return maxJungleSize;
    }

    public int getProbHeavyJungle() {
        return probHeavyJungle;
    }

    public int getProbUltraJungle() {
        return probUltraJungle;
    }

    public int getMinFoliageSpots() {
        return minFoliageSpots;
    }

    public int getMaxFoliageSpots() {
        return maxFoliageSpots;
    }

    public int getMinFoliageSize() {
        return minFoliageSize;
    }

    public int getMaxFoliageSize() {
        return maxFoliageSize;
    }

    public int getProbFoliageHeavy() {
        return probFoliageHeavy;
    }

    public int getMinRoughSpots() {
        return minRoughSpots;
    }

    public int getMaxRoughSpots() {
        return maxRoughSpots;
    }

    public int getMinRoughSize() {
        return minRoughSize;
    }

    public int getMaxRoughSize() {
        return maxRoughSize;
    }

    public int getProbUltraRough() {
        return probUltraRough;
    }

    public int getMinSandSpots() {
        return minSandSpots;
    }

    public void setMinSandSpots(int minSandSpots) {
        this.minSandSpots = minSandSpots;
    }

    public int getMaxSandSpots() {
        return maxSandSpots;
    }

    public void setMaxSandSpots(int maxSandSpots) {
        this.maxSandSpots = maxSandSpots;
    }

    public int getMinSandSize() {
        return minSandSize;
    }

    public void setMinSandSize(int minSandSize) {
        this.minSandSize = minSandSize;
    }

    public int getMaxSandSize() {
        return maxSandSize;
    }

    public void setMaxSandSize(int maxSandSize) {
        this.maxSandSize = maxSandSize;
    }

    public int getMinSnowSpots() {
        return minSnowSpots;
    }

    public void setMinSnowSpots(int minSnowSpots) {
        this.minSnowSpots = minSnowSpots;
    }

    public int getMaxSnowSpots() {
        return maxSnowSpots;
    }

    public void setMaxSnowSpots(int maxSnowSpots) {
        this.maxSnowSpots = maxSnowSpots;
    }

    public int getMinSnowSize() {
        return minSnowSize;
    }

    public void setMinSnowSize(int minSnowSize) {
        this.minSnowSize = minSnowSize;
    }

    public int getMaxSnowSize() {
        return maxSnowSize;
    }

    public void setMaxSnowSize(int maxSnowSize) {
        this.maxSnowSize = maxSnowSize;
    }

    public int getMinTundraSpots() {
        return minTundraSpots;
    }

    public void setMinTundraSpots(int minTundraSpots) {
        this.minTundraSpots = minTundraSpots;
    }

    public int getMaxTundraSpots() {
        return maxTundraSpots;
    }

    public void setMaxTundraSpots(int maxTundraSpots) {
        this.maxTundraSpots = maxTundraSpots;
    }

    public int getMinTundraSize() {
        return minTundraSize;
    }

    public void setMinTundraSize(int minTundraSize) {
        this.minTundraSize = minTundraSize;
    }

    public int getMaxTundraSize() {
        return maxTundraSize;
    }

    public void setMaxTundraSize(int maxTundraSize) {
        this.maxTundraSize = maxTundraSize;
    }

    public int getMinPlantedFieldSpots() {
        return minPlantedFieldSpots;
    }

    public void setMinPlantedFieldSpots(int minPlantedFieldSpots) {
        this.minPlantedFieldSpots = minPlantedFieldSpots;
    }

    public int getMaxPlantedFieldSpots() {
        return maxPlantedFieldSpots;
    }

    public void setMaxPlantedFieldSpots(int maxPlantedFieldSpots) {
        this.maxPlantedFieldSpots = maxPlantedFieldSpots;
    }

    public int getMinPlantedFieldSize() {
        return minPlantedFieldSize;
    }

    public void setMinPlantedFieldSize(int minPlantedFieldSize) {
        this.minPlantedFieldSize = minPlantedFieldSize;
    }

    public int getMaxPlantedFieldSize() {
        return maxPlantedFieldSize;
    }

    public void setMaxPlantedFieldSize(int maxPlantedFieldSize) {
        this.maxPlantedFieldSize = maxPlantedFieldSize;
    }

    public int getMinSwampSpots() {
        return minSwampSpots;
    }

    public int getMaxSwampSpots() {
        return maxSwampSpots;
    }

    public int getMinSwampSize() {
        return minSwampSize;
    }

    public int getMaxSwampSize() {
        return maxSwampSize;
    }

    public int getMinPavementSpots() {
        return minPavementSpots;
    }

    public int getMaxPavementSpots() {
        return maxPavementSpots;
    }

    public int getMinPavementSize() {
        return minPavementSize;
    }

    public int getMaxPavementSize() {
        return maxPavementSize;
    }

    public int getMinRubbleSpots() {
        return minRubbleSpots;
    }

    public int getMaxRubbleSpots() {
        return maxRubbleSpots;
    }

    public int getMinRubbleSize() {
        return minRubbleSize;
    }

    public int getMaxRubbleSize() {
        return maxRubbleSize;
    }

    public int getProbUltraRubble() {
        return probUltraRubble;
    }

    public int getMinFortifiedSpots() {
        return minFortifiedSpots;
    }

    public int getMaxFortifiedSpots() {
        return maxFortifiedSpots;
    }

    public int getMinFortifiedSize() {
        return minFortifiedSize;
    }

    public int getMaxFortifiedSize() {
        return maxFortifiedSize;
    }

    public int getMinIceSpots() {
        return minIceSpots;
    }

    public int getMaxIceSpots() {
        return maxIceSpots;
    }

    public int getMinIceSize() {
        return minIceSize;
    }

    public int getMaxIceSize() {
        return maxIceSize;
    }

    public int getProbRoad() {
        return probRoad;
    }

    public int getProbRiver() {
        return probRiver;
    }

    public int getProbCrater() {
        return probCrater;
    }

    public int getMinRadius() {
        return minRadius;
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    public int getMinCraters() {
        return minCraters;
    }

    public int getMaxCraters() {
        return maxCraters;
    }

    public int getAlgorithmToUse() {
        return algorithmToUse;
    }

    public int getProbFlood() {
        return probFlood;
    }

    public int getProbForestFire() {
        return probForestFire;
    }

    public int getProbFreeze() {
        return probFreeze;
    }

    public int getProbDrought() {
        return probDrought;
    }

    public int getFxMod() {
        return fxMod;
    }

    public int getCityBlocks() {
        return cityBlocks;
    }

    public String getCityType() {
        return cityType;
    }

    public int getCityMinCF() {
        return cityMinCF;
    }

    public int getCityMaxCF() {
        return cityMaxCF;
    }

    public int getCityMinFloors() {
        return cityMinFloors;
    }

    public int getCityMaxFloors() {
        return cityMaxFloors;
    }

    public int getCityDensity() {
        return cityDensity;
    }

    public int getTownSize() {
        return townSize;
    }

    public int getMountainHeightMin() {
        return mountainHeightMin;
    }

    public int getMountainHeightMax() {
        return mountainHeightMax;
    }

    public int getMountainPeaks() {
        return mountainPeaks;
    }

    public int getMountainStyle() {
        return mountainStyle;
    }

    public int getMountainWidthMin() {
        return mountainWidthMin;
    }

    public int getMountainWidthMax() {
        return mountainWidthMax;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setElevationParams(int hill, int newRange, int prob) {
        hilliness = hill;
        range = newRange;
        probInvert = prob;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setWaterParams(int minSpots, int maxSpots, int minSize, int maxSize, int prob) {
        minWaterSpots = minSpots;
        maxWaterSpots = maxSpots;
        minWaterSize = minSize;
        maxWaterSize = maxSize;
        probDeep = prob;
    }

    /**
     * set the forest parameters for the Map Generator
     */
    public void setForestParams(int minSpots, int maxSpots, int minSize, int maxSize, int probHeavy, int probUltra) {
        minForestSpots = minSpots;
        maxForestSpots = maxSpots;
        minForestSize = minSize;
        maxForestSize = maxSize;
        this.probHeavy = probHeavy;
        this.probUltra = probUltra;
    }

    /**
     * set the jungle parameters for the Map Generator
     */
    public void setJungleParams(int minSpots, int maxSpots, int minSize, int maxSize, int probHeavy, int probUltra) {
        minJungleSpots = minSpots;
        maxJungleSpots = maxSpots;
        minJungleSize = minSize;
        maxJungleSize = maxSize;
        probHeavyJungle = probHeavy;
        probUltraJungle = probUltra;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setFoliageParams(int minSpots, int maxSpots, int minSize, int maxSize, int prob) {
        minFoliageSpots = minSpots;
        maxFoliageSpots = maxSpots;
        minFoliageSize = minSize;
        maxFoliageSize = maxSize;
        probFoliageHeavy = prob;
    }

    /**
     * set rough terrain parameters for the Map Generator
     */
    public void setRoughParams(int minSpots, int maxSpots, int minSize, int maxSize, int probUltra) {
        minRoughSpots = minSpots;
        maxRoughSpots = maxSpots;
        minRoughSize = minSize;
        maxRoughSize = maxSize;
        probUltraRough = probUltra;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setSandParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minSandSpots = minSpots;
        maxSandSpots = maxSpots;
        minSandSize = minSize;
        maxSandSize = maxSize;
    }

    /**
     * set the snow parameters for the Map Generator
     */
    public void setSnowParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minSnowSpots = minSpots;
        maxSnowSpots = maxSpots;
        minSnowSize = minSize;
        maxSnowSize = maxSize;
    }

    /**
     * set the tundra parameters for the Map Generator
     */
    public void setTundraParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minTundraSpots = minSpots;
        maxTundraSpots = maxSpots;
        minTundraSize = minSize;
        maxTundraSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setPlantedFieldParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minPlantedFieldSpots = minSpots;
        maxPlantedFieldSpots = maxSpots;
        minPlantedFieldSize = minSize;
        maxPlantedFieldSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setSwampParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minSwampSpots = minSpots;
        maxSwampSpots = maxSpots;
        minSwampSize = minSize;
        maxSwampSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setPavementParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minPavementSpots = minSpots;
        maxPavementSpots = maxSpots;
        minPavementSize = minSize;
        maxPavementSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setRubbleParams(int minSpots, int maxSpots, int minSize, int maxSize, int probUltra) {
        minRubbleSpots = minSpots;
        maxRubbleSpots = maxSpots;
        minRubbleSize = minSize;
        maxRubbleSize = maxSize;
        probUltraRubble = probUltra;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setFortifiedParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minFortifiedSpots = minSpots;
        maxFortifiedSpots = maxSpots;
        minFortifiedSize = minSize;
        maxFortifiedSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setIceParams(int minSpots, int maxSpots, int minSize, int maxSize) {
        minIceSpots = minSpots;
        maxIceSpots = maxSpots;
        minIceSize = minSize;
        maxIceSize = maxSize;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setRiverParam(int prob) {
        probRiver = prob;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setRoadParam(int prob) {
        probRoad = prob;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setCliffParam(int prob) {
        cliffs = prob;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setCraterParam(int prob, int minCrat, int maxCrat, int minRad, int maxRad) {

        probCrater = prob;
        maxCraters = maxCrat;
        minCraters = minCrat;
        minRadius = minRad;
        maxRadius = maxRad;
    }

    /**
     * set the Parameters for the Map Generator
     */
    public void setInvertNegativeTerrain(int invert) {
        invertNegativeTerrain = invert;
    }

    /**
     * set Map generator parameters
     */
    public void setSpecialFX(int modifier, int fire, int freeze, int flood, int drought) {
        fxMod = modifier;
        probForestFire = fire;
        probFreeze = freeze;
        probFlood = flood;
        probDrought = drought;
    }

    public void setAlgorithmToUse(int alg) {
        algorithmToUse = alg;
    }

    public void setCityParams(int cityBlocks, String cityType, int cityMinCF, int cityMaxCF, int cityMinFloors,
          int cityMaxFloors, int cityDensity, int townSize) {
        this.cityBlocks = cityBlocks;
        this.cityType = cityType;
        this.cityMinCF = cityMinCF;
        this.cityMaxCF = cityMaxCF;
        this.cityMinFloors = cityMinFloors;
        this.cityMaxFloors = cityMaxFloors;
        this.cityDensity = cityDensity;
        this.townSize = townSize;
    }

    public void setMountainParams(int mountainPeaks, int mountainWidthMin, int mountainWidthMax, int mountainHeightMin,
          int mountainHeightMax, int mountainStyle) {
        this.mountainHeightMax = mountainHeightMax;
        this.mountainHeightMin = mountainHeightMin;
        this.mountainWidthMin = mountainWidthMin;
        this.mountainWidthMax = mountainWidthMax;
        this.mountainPeaks = mountainPeaks;
        this.mountainStyle = mountainStyle;
    }

    public void setMedium(int m) {
        medium = m;
    }

    public int getMedium() {
        return medium;
    }

    public static String getMediumName(int m) {
        return mediumNames[m];
    }

    public void save(final OutputStream os) {
        try {
            JAXBContext jc = JAXBContext.newInstance(MapSettings.class);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // The default header has the encoding and standalone properties
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty("org.glassfish.jaxb.xmlHeaders", "<?xml version=\"1.0\"?>");
            JAXBElement<MapSettings> element = new JAXBElement<>(new QName("ENVIRONMENT"), MapSettings.class, this);
            marshaller.marshal(element, os);
        } catch (Exception ex) {
            logger.error("Failed to write map settings xml", ex);
        }
    }
}
