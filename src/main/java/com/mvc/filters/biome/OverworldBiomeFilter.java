package com.mvc.filters.biome;

import com.mvc.Config;
import com.seedfinding.mcbiome.layer.IntBiomeLayer;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.Monument;
import com.seedfinding.mcfeature.structure.PillagerOutpost;
import com.seedfinding.mcfeature.structure.Village;

import java.util.ArrayList;

public class OverworldBiomeFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private final OverworldBiomeSource overworldBiomeSource;
    public OverworldBiomeFilter(long worldSeed, long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.overworldBiomeSource = new OverworldBiomeSource(Config.VERSION, worldSeed);
    }

    public Pair<Boolean, ArrayList<BPos>> filterOverworld() {
        if (!hasMidgame()) {
            return new Pair<>(false, null);
        }
        if (fullScaleSearch(3, 64, true)) {
            return new Pair<>(true, null);
        }
        return new Pair<>(false, null);
    }

    private boolean hasMidgame() {
        return hasTemple() && hasVillage() && hasMonument() && hasOutpost() && hasMidgameTemples(5);
    }

    private boolean hasVillage() {
        Village village = new Village(Config.VERSION);
        CPos villagePos = village.getInRegion(structureSeed, 0, 0, chunkRand);

        return village.canSpawn(villagePos, overworldBiomeSource);
    }

    private boolean hasTemple() {
        DesertPyramid temple = new DesertPyramid(Config.VERSION);
        CPos templePos = temple.getInRegion(structureSeed, 0, 0, chunkRand);

        return temple.canSpawn(templePos, overworldBiomeSource);
    }

    private boolean hasMidgameTemples(int minCount) {
        DesertPyramid temple = new DesertPyramid(Config.VERSION);
        int count = 0;

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 1; z++) {
                //already checked spawn temple exists
                if (x == 0 && z == 0) {
                    count++;
                    break;
                }

                CPos templePos = temple.getInRegion(structureSeed, x, z, chunkRand);

                if (temple.canSpawn(templePos, overworldBiomeSource)) {
                    count++;
                }
            }
        }

        return count >= minCount;
    }

    private boolean hasMonument() {
        Monument mm = new Monument(Config.VERSION);

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 1; z++) {
                CPos mmPos = mm.getInRegion(structureSeed, x, z, chunkRand);

                if (mm.canSpawn(mmPos.getX(), mmPos.getZ(), overworldBiomeSource)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasOutpost() {
        PillagerOutpost po = new PillagerOutpost(Config.VERSION);

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 1; z++) {
                CPos poPos = po.getInRegion(structureSeed, x, z, chunkRand);

                if (poPos != null && po.canSpawn(poPos.getX(), poPos.getZ(), overworldBiomeSource)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean fullScaleSearch(int radius, int resolution, boolean checkerboard) {
        if (((resolution & (resolution - 1)) != 0) || resolution > 256 || resolution < 4) {
            throw new RuntimeException("Please use a resolution of 256, 128, 64, 32, 16, 8, 4");
        }
        IntBiomeLayer layer;
        switch (resolution) {
            case 4: {
                layer = this.overworldBiomeSource.getLayer(49);
                break;
            }
            case 8: {
                // no frozen river
                layer = this.overworldBiomeSource.getLayer(32);
                break;
            }
            case 16: {
                layer = this.overworldBiomeSource.getLayer(31);
                break;
            }
            case 32: {
                // no snowy beach
                // no mushroom field shore
                // no jungle edge
                layer = this.overworldBiomeSource.getLayer(29);
                break;
            }
            case 64: {
                layer = this.overworldBiomeSource.getLayer(27);
                break;
            }
            case 128: {
                // no snowy mountains
                // no snowy taiga hills
                // no jungle hills
                // no bamboo jungle hills
                // no mega taiga hills
                // no badlands
                layer = this.overworldBiomeSource.getLayer(20);
                break;
            }
            case 256: {
                layer = this.overworldBiomeSource.getLayer(19);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + resolution);
        }

        boolean frozenRiver = false; // 11
        boolean snowyTundra = false; // 12
        boolean snowyMountains = false; // 13
        boolean snowyBeach = false; // 26
        boolean snowyTaiga = false; // 30
        boolean snowyTaigaHills = false; // 31

        boolean mushroomFields = false; // 14
        boolean mushroomFieldShore = false; // 15

        boolean jungle = false; // 21
        boolean jungleHills = false; // 22
        boolean jungleEdge = false; // 23
        boolean bambooJungle = false; // 168
        boolean bambooJungleHills = false; // 169

        boolean megaTaiga = false; // 32
        boolean megaTaigaHills = false; // 33

        boolean badlands = false; // 37
        boolean woodedBadlandsPlateau = false; // 38
        boolean badlandsPlateau = false; // 39

        int minX = -(radius * 1024) / resolution;
        int minZ = -(radius * 1024) / resolution;
        int maxX = ((radius * 1024) / resolution) - 1;
        int maxZ = ((radius * 1024) / resolution) - 1;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                if (checkerboard) {
                    if (!((x % 2 == 0 && z % 2 == 0) || (x % 2 != 0 && z % 2 != 0))) {
                        continue;
                    }
                }
                switch (layer.sample(x, 0, z)) {
                    case 11: {
                        frozenRiver = true;
                        break;
                    }
                    case 12: {
                        snowyTundra = true;
                        break;
                    }
                    case 13: {
                        snowyMountains = true;
                        break;
                    }
                    case 14: {
                        mushroomFields = true;
                        break;
                    }
                    case 15: {
                        mushroomFieldShore = true;
                        break;
                    }
                    case 21: {
                        jungle = true;
                        break;
                    }
                    case 22: {
                        jungleHills = true;
                        break;
                    }
                    case 23: {
                        jungleEdge = true;
                        break;
                    }
                    case 26: {
                        snowyBeach = true;
                        break;
                    }
                    case 30: {
                        snowyTaiga = true;
                        break;
                    }
                    case 31: {
                        snowyTaigaHills = true;
                        break;
                    }
                    case 32: {
                        megaTaiga = true;
                        break;
                    }
                    case 33: {
                        megaTaigaHills = true;
                        break;
                    }
                    case 37: {
                        badlands = true;
                        break;
                    }
                    case 38: {
                        woodedBadlandsPlateau = true;
                        break;
                    }
                    case 39: {
                        badlandsPlateau = true;
                        break;
                    }
                    case 168: {
                        bambooJungle = true;
                        break;
                    }
                    case 169: {
                        bambooJungleHills = true;
                        break;
                    }
                }
            }
        }

        if (resolution == 4 &&
                frozenRiver && snowyTundra && snowyMountains && snowyBeach && snowyTaiga && snowyTaigaHills &&
                mushroomFields && mushroomFieldShore &&
                jungle && jungleHills && jungleEdge && bambooJungle && bambooJungleHills &&
                megaTaiga && megaTaigaHills &&
                badlands && woodedBadlandsPlateau && badlandsPlateau
        ) {
            return true;
        } else if ((resolution == 8 || resolution == 16) &&
                snowyTundra && snowyMountains && snowyBeach && snowyTaiga && snowyTaigaHills &&
                mushroomFields && mushroomFieldShore &&
                jungle && jungleHills && jungleEdge && bambooJungle && bambooJungleHills &&
                megaTaiga && megaTaigaHills &&
                badlands && woodedBadlandsPlateau && badlandsPlateau
        ) {
            return true;
        } else if ((resolution == 32 || resolution == 64) &&
                snowyTundra && snowyMountains && snowyTaiga && snowyTaigaHills &&
                mushroomFields &&
                jungle && jungleHills && bambooJungle && bambooJungleHills &&
                megaTaiga && megaTaigaHills &&
                badlands && woodedBadlandsPlateau && badlandsPlateau
        ) {
            return true;
        } else return (resolution == 128 || resolution == 256) &&
                    snowyTundra && snowyTaiga &&
                    mushroomFields &&
                    jungle && bambooJungle &&
                    megaTaiga &&
                    woodedBadlandsPlateau && badlandsPlateau;
    }
}
