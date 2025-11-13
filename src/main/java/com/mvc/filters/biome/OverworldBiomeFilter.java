package com.mvc.filters.biome;

import com.mvc.Config;

import com.seedfinding.mcbiome.layer.BiomeLayer;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.Monument;
import com.seedfinding.mcfeature.structure.PillagerOutpost;
import com.seedfinding.mcfeature.structure.Village;

import java.util.ArrayList;

public class OverworldBiomeFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private final long worldSeed;
    private final OverworldBiomeSource overworldBiomeSource;
    public OverworldBiomeFilter(long worldSeed, long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.worldSeed = worldSeed;
        this.overworldBiomeSource = new OverworldBiomeSource(Config.VERSION, worldSeed);
    }

    public boolean hasMidgame() {
        return hasVillage() && hasTemple() && hasMonument() && hasOutpost() && hasMidgameTemples(5);
    }

    public boolean filterBiomes() {
        return hasJungleBiomes();
    }

    private boolean hasVillage() {
        Village village = new Village(Config.VERSION);
        CPos villagePos = village.getInRegion(structureSeed, 0, 0, chunkRand);

        return village.isValidBiome(overworldBiomeSource.getBiome(villagePos.toBlockPos()));
    }

    private boolean hasTemple() {
        DesertPyramid temple = new DesertPyramid(Config.VERSION);
        CPos templePos = temple.getInRegion(structureSeed, 0, 0, chunkRand);

        return temple.isValidBiome(overworldBiomeSource.getBiome(templePos.toBlockPos()));
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

                if (temple.isValidBiome(overworldBiomeSource.getBiome(templePos.toBlockPos()))) {
                    count++;
                }
            }
        }

        return count >= minCount;
    }

    private boolean hasMonument() {
        Monument mm = new Monument(Config.VERSION);

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 2; z++) {
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
            for (int z = -2; z <= 2; z++) {
                CPos poPos = po.getInRegion(structureSeed, x, z, chunkRand);

                if (poPos != null && po.canSpawn(poPos.getX(), poPos.getZ(), overworldBiomeSource)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasBiomes() {
        /*
        TODO: add checks for individual biomes
        mushroom_fields - layer 16 256:1
        mushroom_field_shore - layer 31 16:1

        jungle - layer 18 256:1
        jungle_hills - layer 26 64:1
        jungle_edge - layer 31 16:1
        bamboo_jungle - layer 19 256:1
        bamboo_jungle_hills - layer 26 64:1

        badlands - layer 22 64:1
        badlands_plateau - layer 18 256:1
        wooded_badlands_plateau - layer 18 256:1

        giant_tree_taiga - layer 18 256:1
        giant_tree_taiga_hills - layer 26 64:1

        snowy_tundra - layer 18 256:1
        snowy_mountains - layer 26 64:1
        snowy_taiga - layer 18 256:1
        snowy_taiga_hills - layer 26 64:1
        frozen_river - layer 41 4:1
        */

        ArrayList<CPos> specialPositions = new ArrayList<>();
        long specialLayerSeed = BiomeLayer.getLayerSeed(worldSeed, 3);

        // 53% to have 3 special tiles
        for (int x = -3; x <= 2; x++) {
            for (int z = -3; z <= 2; z++) {
                long specialLocalSeed = BiomeLayer.getLocalSeed(specialLayerSeed, x, z);

                // 1 in 13 for a 1024x1024 tile to be special
                if (Math.floorMod(specialLocalSeed >> 24, 13) == 0) {
                    specialPositions.add(new CPos(x, z));
                }
            }
        }

        // need at least 3 special tiles for mesa, jungle, mega taiga
        if (specialPositions.size() < 3) {
            return false;
        }

        ArrayList<CPos> mushroomPositions = new ArrayList<>();
        long mushroomLayerSeed = BiomeLayer.getLayerSeed(worldSeed, 5);

        // 76% to have 1 mushroom tile
        for (int x = -12; x <= 11; x++) {
            for (int z = -12; z <= 11; z++) {
                long mushroomLocalSeed = BiomeLayer.getLocalSeed(mushroomLayerSeed, x, z);

                // 1 in 100 for a 256x256 tile to be mushroom
                if (Math.floorMod(mushroomLocalSeed >> 24, 100) == 0) {
                    mushroomPositions.add(new CPos(x, z));
                }
            }
        }

        if (mushroomPositions.isEmpty()) {
            return false;
        }

        boolean mesa = false;
        boolean jungle = false;
        boolean megaTaiga = false;
        for (CPos pos: specialPositions) {
            if (overworldBiomeSource.getLayer(9).sample(pos.getX(), 0, pos.getZ()) != 0) {
                switch (overworldBiomeSource.getLayer(11).sample(pos.getX(), 0, pos.getZ())) {
                    case 1: {
                        mesa = true;
                        break;
                    }
                    case 2: {
                        jungle = true;
                        break;
                    }
                    case 3: {
                        megaTaiga = true;
                        break;
                    }
                }
            }
        }

        if (!mesa || !jungle || !megaTaiga) {
            return false;
        }

        boolean freezing = false;
        for (int x = -3; x <= 2; x++) {
            for (int z = -3; z <= 2; z++) {
                if (overworldBiomeSource.getLayer(11).sample(x, 0, z) == 4) {
                    freezing = true;
                }
            }
        }

        if (!freezing) {
            return false;
        }

        for (CPos pos: mushroomPositions) {
            if (overworldBiomeSource.getLayer(16).sample(pos.getX(), 0, pos.getZ()) == 14) {
                return true;
            }
        }

        return false;
    }

    private boolean hasJungleBiomes() {
        long specialLayerSeed = BiomeLayer.getLayerSeed(worldSeed, 3);
        long specialLocalSeed = BiomeLayer.getLocalSeed(specialLayerSeed, 0, 0);
        if (Math.floorMod(specialLocalSeed >> 24, 13) != 0) {
            return false;
        }

        // id 0 is ocean
        if (overworldBiomeSource.getLayer(9).sample(0, 0, 0) == 0) {
            return false;
        }

        // id 2 is desert -> jungle
        if (overworldBiomeSource.getLayer(11).sample(0, 0, 0) != 2) {
            return false;
        }

        /*
        id 168 is bamboo_jungle
        checking at 256:1

        id 169 is bamboo_jungle_hills
        checking at 64:1
        */
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                if (overworldBiomeSource.getLayer(19).sample(x, 0, z) == 168) {
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            if (overworldBiomeSource.getLayer(26).sample(i, 0, j) == 169) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
