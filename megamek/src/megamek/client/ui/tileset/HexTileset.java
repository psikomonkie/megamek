/*
 * Copyright (c) 2000-2002 Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2013 Edward Cullen (eddy@obsessedcomputers.co.uk)
 * Copyright (c) 2023-2024 - The MegaMek Team. All Rights Reserved.
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
package megamek.client.ui.tileset;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import megamek.client.ui.util.ImageCache;
import megamek.common.Board;
import megamek.common.Configuration;
import megamek.common.Hex;
import megamek.common.IGame;
import megamek.common.Terrain;
import megamek.common.Terrains;
import megamek.common.event.BoardEvent;
import megamek.common.event.BoardListener;
import megamek.common.event.GameBoardChangeEvent;
import megamek.common.event.GameBoardNewEvent;
import megamek.common.event.GameListener;
import megamek.common.event.GameListenerAdapter;
import megamek.common.util.ImageUtil;
import megamek.common.util.StringUtil;
import megamek.common.util.fileUtils.MegaMekFile;
import megamek.logging.MMLogger;

/**
 * Matches each hex with an appropriate image.
 *
 * @author Ben
 */
public class HexTileset implements BoardListener {
    private static final MMLogger logger = MMLogger.create(HexTileset.class);

    /** The image width of a hex image. */
    public static final int HEX_W = 84;

    /** The image height of a hex image. */
    public static final int HEX_H = 72;

    public static final String TRANSPARENT_THEME = "transparent";

    private final List<HexEntry> bases = new ArrayList<>();
    private final List<HexEntry> supers = new ArrayList<>();
    private final List<HexEntry> orthos = new ArrayList<>();
    private final Set<String> themes = new TreeSet<>();
    private ImageCache<Hex, Image> basesCache = new ImageCache<>();
    private ImageCache<Hex, List<Image>> supersCache = new ImageCache<>();
    private ImageCache<Hex, List<Image>> orthosCache = new ImageCache<>();

    /**
     * Creates new HexTileset
     */
    public HexTileset(IGame g) {
        g.addGameListener(gameListener);
        g.getBoard().addBoardListener(this);
    }

    /** Clears the image cache for the given hex. */
    public synchronized void clearHex(Hex hex) {
        basesCache.remove(hex);
        supersCache.remove(hex);
        orthosCache.remove(hex);
    }

    /** Clears the image cache for all hexes. */
    public synchronized void clearAllHexes() {
        basesCache = new ImageCache<>();
        supersCache = new ImageCache<>();
        orthosCache = new ImageCache<>();
    }

    /**
     * This assigns images to a hex based on the best matches it can find.
     * <p>
     * First it assigns any images to be superimposed on a hex. These images must
     * have a match value of 1.0 to be added, and any time a match of this level is
     * achieved, any terrain involved in the match is removed from further
     * consideration.
     * <p>
     * Any terrain left is used to match a base image for the hex. This time, a
     * match can be any value, and the first, best image is used.
     */
    public synchronized Object[] assignMatch(Hex hex) {
        Hex hexCopy = hex.duplicate();
        List<Image> ortho = orthoFor(hexCopy);
        List<Image> supers = supersFor(hexCopy);
        Image base = baseFor(hexCopy);
        Object[] pair = new Object[] { base, supers, ortho };
        basesCache.put(hex, base);
        supersCache.put(hex, supers);
        orthosCache.put(hex, ortho);
        return pair;
    }

    public synchronized Image getBase(Hex hex) {
        Image i = basesCache.get(hex);
        if (i == null) {
            Object[] pair = assignMatch(hex);
            return (Image) pair[0];
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Image> getSupers(Hex hex) {
        List<Image> l = supersCache.get(hex);
        if (l == null) {
            Object[] pair = assignMatch(hex);
            return (List<Image>) pair[1];
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<Image> getOrtho(Hex hex) {
        List<Image> o = orthosCache.get(hex);

        if (o == null) {
            Object[] pair = assignMatch(hex);
            return (List<Image>) pair[2];
        }

        return o;
    }

    /**
     * Returns a list of orthographic images to be tiled above the hex. As noted
     * above, all matches must be 1.0, and if such a match is achieved, all terrain
     * elements from the tileset hex are removed from the hex. Thus you want to pass
     * a copy of the original to this function.
     */
    private List<Image> orthoFor(Hex hex) {
        ArrayList<Image> matches = new ArrayList<>();

        // find orthographic image matches
        for (HexEntry entry : orthos) {
            if (orthoMatch(hex, entry.getHex()) >= 1.0) {
                Image img = entry.getImage(hex.getCoords().hashCode());
                if (img != null) {
                    matches.add(img);
                } else {
                    matches.add(ImageUtil.createAcceleratedImage(HEX_W, HEX_H));
                }
                // remove involved terrain from consideration
                for (int terr : entry.getHex().getTerrainTypes()) {
                    if (entry.getHex().containsTerrain(terr)) {
                        hex.removeTerrain(terr);
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Returns a list of images to be superimposed on the hex. As noted above, all
     * matches must be 1.0, and if such a match is achieved, all terrain elements
     * from the tileset hex are removed from the hex. Thus you want to pass a copy
     * of the original to this function.
     */
    private List<Image> supersFor(Hex hex) {
        ArrayList<Image> matches = new ArrayList<>();

        // find superimposed image matches
        for (HexEntry entry : supers) {
            if (superMatch(hex, entry.getHex()) >= 1.0) {
                Image img = entry.getImage(hex.getCoords().hashCode());
                if (img != null) {
                    matches.add(img);
                } else {
                    matches.add(ImageUtil.createAcceleratedImage(HEX_W, HEX_H));
                }
                // remove involved terrain from consideration
                for (int terr : entry.getHex().getTerrainTypes()) {
                    if (entry.getHex().containsTerrain(terr)) {
                        hex.removeTerrain(terr);
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Returns the best matching base image for this hex. This works best if any
     * terrain with a "super" image is removed.
     */
    private Image baseFor(Hex hex) {
        HexEntry bestMatch = null;
        double match = -1;

        // match a base image to the hex
        for (HexEntry entry : bases) {

            // Metal deposits don't count for visual
            if (entry.getHex().containsTerrain(Terrains.METAL_CONTENT)) {
                hex.removeTerrain(Terrains.METAL_CONTENT);
            }

            double thisMatch = baseMatch(hex, entry.getHex());
            // stop if perfect match
            if (thisMatch == 1.0) {
                bestMatch = entry;
                break;
            }
            // compare match with best
            if (thisMatch > match) {
                bestMatch = entry;
                match = thisMatch;
            }
        }

        Random random = new Random(hex.getCoords().hashCode());
        Image img = bestMatch.getImage(Math.abs(random.nextInt() * random.nextInt()));
        if (img == null) {
            img = ImageUtil.createAcceleratedImage(HEX_W, HEX_H);
        }
        return img;
    }

    // perfect match
    // all but theme
    // all but elevation
    // all but elevation & theme

    /** Recursion depth counter to prevent freezing from circular includes */
    public int incDepth = 0;

    public void loadFromFile(String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        // make input stream for board
        Reader r = new BufferedReader(new FileReader(new MegaMekFile(Configuration.hexesDir(), filename).getFile()));
        // read board, looking for "size"
        StreamTokenizer st = new StreamTokenizer(r);
        st.eolIsSignificant(true);
        st.commentChar('#');
        st.quoteChar('"');
        st.wordChars('_', '_');
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
            int elevation = 0;
            // int levity = 0;
            String terrain = null;
            String theme = null;
            String imageName = null;
            if ((st.ttype == StreamTokenizer.TT_WORD)
                    && (st.sval.equals("base") || st.sval.equals("super") || st.sval.equals("ortho"))) {
                boolean bas = st.sval.equals("base");
                boolean sup = st.sval.equals("super");
                boolean ort = st.sval.equals("ortho");

                if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
                    elevation = (int) st.nval;
                } else {
                    if (st.ttype == 62) { // ">"
                        if (st.nextToken() == StreamTokenizer.TT_NUMBER) { // e.g. ">4"
                            elevation = (int) (Terrain.ATLEAST + st.nval);
                        }
                    } else {
                        elevation = Terrain.WILDCARD;
                    }
                }
                st.nextToken();
                terrain = st.sval;
                st.nextToken();
                theme = st.sval;
                themes.add(theme);
                st.nextToken();
                imageName = st.sval;
                // add to list
                if (bas) {
                    bases.add(new HexEntry(new Hex(elevation, terrain, theme), imageName));
                }
                if (sup) {
                    supers.add(new HexEntry(new Hex(elevation, terrain, theme), imageName));
                }
                if (ort) {
                    orthos.add(new HexEntry(new Hex(elevation, terrain, theme), imageName));
                }
            } else if ((st.ttype == StreamTokenizer.TT_WORD) && st.sval.equals("include")) {
                st.nextToken();
                incDepth++;
                if (incDepth < 100) {
                    String incFile = st.sval;
                    logger.debug("Including " + incFile);
                    loadFromFile(incFile);
                }
            }
        }
        r.close();
        themes.add(TRANSPARENT_THEME);
        long endTime = System.currentTimeMillis();

        String loadInfo = String.format("Loaded %o base images, %o super images and %o ortho images",
                bases.size(), supers.size(), orthos.size());
        logger.debug(loadInfo);

        if (incDepth == 0) {
            logger.info("HexTileset " + filename + " loaded in " + (endTime - startTime) + "ms.");
        }
        incDepth--;
    }

    public Set<String> getThemes() {
        return new TreeSet<>(themes);
    }

    /**
     * Match the two hexes using the "ortho" super* formula. All matches must be
     * exact, however the match only depends on the original hex matching all the
     * elements of the comparison, not vice versa.
     * <p>
     * EXCEPTION: a themed original matches any unthemed comparison.
     */
    private double orthoMatch(Hex org, Hex com) {
        // exact elevation
        if ((com.getLevel() != Terrain.WILDCARD) && (com.getLevel() < Terrain.ATLEAST)
                && (org.getLevel() != com.getLevel())) {
            return 0;
        }

        // greater than elevation (e.g. >4), the "-100" to allow >-3
        if ((com.getLevel() != Terrain.WILDCARD) && (com.getLevel() >= Terrain.ATLEAST - 100)
                && (org.getLevel() < com.getLevel() - Terrain.ATLEAST)) {
            return 0;
        }

        // A themed original matches any unthemed comparison.
        if ((com.getTheme() != null) && !com.getTheme().equalsIgnoreCase(org.getTheme())) {
            return 0.0;
        }

        // org terrains must match com terrains
        if (org.terrainsPresent() < com.terrainsPresent()) {
            return 0.0;
        }

        // check terrain
        int[] cTerrainTypes = com.getTerrainTypes();
        for (int i = 0; i < cTerrainTypes.length; i++) {
            int cTerrType = cTerrainTypes[i];
            Terrain cTerr = com.getTerrain(cTerrType);
            Terrain oTerr = org.getTerrain(cTerrType);
            if (cTerr == null) {
                continue;
            } else if ((oTerr == null)
                    || ((cTerr.getLevel() != Terrain.WILDCARD) && (oTerr.getLevel() != cTerr.getLevel()))
                    || (cTerr.hasExitsSpecified() && (oTerr.getExits() != cTerr.getExits()))) {
                return 0;
            }
        }

        return 1.0;
    }

    /**
     * Match the two hexes using the "super" formula. All matches must be exact,
     * however the match only depends on the original hex matching all the elements
     * of the comparison, not vice versa.
     * <p>
     * EXCEPTION: a themed original matches any unthemed comparison.
     */
    private double superMatch(Hex org, Hex com) {
        // exact elevation
        if ((com.getLevel() != Terrain.WILDCARD) && (com.getLevel() < Terrain.ATLEAST)
                && (org.getLevel() != com.getLevel())) {
            return 0;
        }

        // greater than elevation (e.g. >4), the "-100" to allow >-3
        if ((com.getLevel() != Terrain.WILDCARD) && (com.getLevel() >= Terrain.ATLEAST - 100)
                && (org.getLevel() < com.getLevel() - Terrain.ATLEAST)) {
            return 0;
        }

        // A themed original matches any unthemed comparison.
        if ((com.getTheme() != null) && !com.getTheme().equalsIgnoreCase(org.getTheme())) {
            return 0.0;
        }

        // org terrains must match com terrains
        if (org.terrainsPresent() < com.terrainsPresent()) {
            return 0.0;
        }

        // check terrain
        int[] cTerrainTypes = com.getTerrainTypes();
        for (int i = 0; i < cTerrainTypes.length; i++) {
            int cTerrType = cTerrainTypes[i];
            Terrain cTerr = com.getTerrain(cTerrType);
            Terrain oTerr = org.getTerrain(cTerrType);
            if (cTerr == null) {
                continue;
            } else if ((oTerr == null)
                    || ((cTerr.getLevel() != Terrain.WILDCARD) && (oTerr.getLevel() != cTerr.getLevel()))
                    || (cTerr.hasExitsSpecified() && (oTerr.getExits() != cTerr.getExits()))) {
                return 0;
            }
        }

        return 1.0;
    }

    /**
     * Match the two hexes using the "base" formula.
     *
     * @return a value indicating how close of a match the original hex is to the
     *         comparison hex. 0 means no match, 1 means perfect match.
     */
    private double baseMatch(Hex org, Hex com) {
        double elevation;
        double terrain;
        double theme;

        // check elevation
        if (com.getLevel() == Terrain.WILDCARD) {
            elevation = 1.0;
        } else if (com.getLevel() >= Terrain.ATLEAST - 100) {
            if (org.getLevel() >= com.getLevel() - Terrain.ATLEAST) {
                elevation = 1.0;
            } else {
                elevation = 1.01 / (Math.abs(org.getLevel() - com.getLevel() - Terrain.ATLEAST) + 1.01);
            }
        } else {
            elevation = 1.01 / (Math.abs(org.getLevel() - com.getLevel()) + 1.01);
        }

        // Determine maximum number of terrain matches.
        // Bug 732188: Have a non-zero minimum terrain match.
        double maxTerrains = Math.max(org.terrainsPresent(), com.terrainsPresent());
        double matches = 0.0;

        int[] orgTerrains = org.getTerrainTypes();

        for (int i = 0; i < orgTerrains.length; i++) {
            int terrType = orgTerrains[i];
            Terrain cTerr = com.getTerrain(terrType);
            Terrain oTerr = org.getTerrain(terrType);
            if ((cTerr == null) || (oTerr == null)) {
                continue;
            }
            double thisMatch = 0;

            if (cTerr.getLevel() == Terrain.WILDCARD) {
                thisMatch = 1.0;
            } else if (cTerr.getLevel() >= Terrain.ATLEAST - 100) {
                if (oTerr.getLevel() >= com.getLevel() - Terrain.ATLEAST) {
                    thisMatch = 1.0;
                } else {
                    thisMatch = 1.0 / (Math.abs(oTerr.getLevel() - cTerr.getLevel() - Terrain.ATLEAST) + 1.0);
                }
            } else {
                thisMatch = 1.0 / (Math.abs(oTerr.getLevel() - cTerr.getLevel()) + 1.0);
            }
            // without exit match, terrain counts... um, half?
            if (cTerr.hasExitsSpecified() && (oTerr.getExits() != cTerr.getExits())) {
                thisMatch *= 0.5;
            }
            // add up match value
            matches += thisMatch;
        }
        if (maxTerrains == 0) {
            terrain = 1.0;
        } else {
            terrain = matches / maxTerrains;
        }

        // check theme
        if ((com.getTheme() == org.getTheme())
                || ((com.getTheme() != null) && com.getTheme().equalsIgnoreCase(org.getTheme()))) {
            theme = 1.0;
        } else if ((org.getTheme() != null) && (com.getTheme() == null)) {
            // If no precise themed match, slightly favor unthemed comparisons
            theme = 0.001;
        } else {
            // also don't throw a match entirely out because the theme is off
            theme = 0.0001;
        }

        return elevation * terrain * theme;
    }

    private static class HexEntry {
        private final Hex hex;
        private Vector<Image> images;
        private final Vector<String> filenames;

        public HexEntry(Hex hex, String imageFile) {
            this.hex = hex;
            filenames = StringUtil.splitString(imageFile, ";");
        }

        public Hex getHex() {
            return hex;
        }

        public Image getImage(int seed) {
            if ((null == images) || images.isEmpty()) {
                loadImage();
            }
            if (images.isEmpty()) {
                return null;
            }
            if (images.size() > 1) {
                int rand = (seed % images.size());
                return images.elementAt(rand);
            }
            return images.firstElement();
        }

        public void loadImage() {
            images = new Vector<>();
            for (String filename : filenames) {
                File imgFile = new MegaMekFile(Configuration.hexesDir(), filename).getFile();
                Image image = ImageUtil.loadImageFromFile(imgFile.toString());
                if (null != image) {
                    images.add(image);
                } else {
                    logger.error("Received null image from ImageUtil.loadImageFromFile! File: " + imgFile);
                }
            }
        }

        @Override
        public String toString() {
            return "HexTileset: " + hex.toString();
        }
    }

    // The Board and Game listeners
    // The HexTileSet caches images with the hex object as key. Therefore it
    // must listen to Board events to clear changed (but not replaced)
    // hexes from the cache.
    // It must listen to Game events to catch when a board is entirely replaced
    // to be able to register itself to the new board.
    private final GameListener gameListener = new GameListenerAdapter() {

        @Override
        public void gameBoardNew(GameBoardNewEvent e) {
            clearAllHexes();
            replacedBoard(e);
        }

        @Override
        public void gameBoardChanged(GameBoardChangeEvent e) {
            clearAllHexes();
        }

    };

    private void replacedBoard(GameBoardNewEvent e) {
        if (e.getOldBoard() != null) {
            e.getOldBoard().removeBoardListener(this);
        }
        e.getNewBoard().addBoardListener(this);
    }

    @Override
    public void boardNewBoard(BoardEvent b) {
        clearAllHexes();
    }

    @Override
    public void boardChangedHex(BoardEvent b) {
        clearHex(((Board) b.getSource()).getHex(b.getCoords()));
    }

    @Override
    public void boardChangedAllHexes(BoardEvent b) {
        clearAllHexes();
    }
}
