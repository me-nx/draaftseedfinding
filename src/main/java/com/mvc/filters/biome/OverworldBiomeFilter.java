package com.mvc.filters.biome;

import com.mvc.Config;
import com.seedfinding.mcbiome.layer.BiomeLayer;
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
    private final long worldSeed;
    private final OverworldBiomeSource overworldBiomeSource;
    private final IntBiomeLayer biomeLayer9;
    private final IntBiomeLayer biomeLayer11;
    private final IntBiomeLayer biomeLayer16;
    private final IntBiomeLayer biomeLayer19;
    private final IntBiomeLayer biomeLayer26;
    private final IntBiomeLayer biomeLayer31;
    private final ArrayList<CPos> mushroomPositions;
    private final ArrayList<CPos> badlandsPositions;
    private final ArrayList<CPos> junglePositions;
    private final ArrayList<CPos> megaTaigaPositions;
    private final ArrayList<CPos> snowyPositions;
    public OverworldBiomeFilter(long worldSeed, long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.worldSeed = worldSeed;
        this.overworldBiomeSource = new OverworldBiomeSource(Config.VERSION, worldSeed);
        this.biomeLayer9 = overworldBiomeSource.getLayer(9);
        this.biomeLayer11 = overworldBiomeSource.getLayer(11);
        this.biomeLayer16 = overworldBiomeSource.getLayer(16);
        this.biomeLayer19 = overworldBiomeSource.getLayer(19);
        this.biomeLayer26 = overworldBiomeSource.getLayer(26);
        this.biomeLayer31 = overworldBiomeSource.getLayer(31);
        this.mushroomPositions = new ArrayList<>();
        this.badlandsPositions = new ArrayList<>();
        this.junglePositions = new ArrayList<>();
        this.megaTaigaPositions = new ArrayList<>();
        this.snowyPositions = new ArrayList<>();
    }

    public Pair<Boolean, ArrayList<BPos>> filterOverworld() {
        if (!hasMidgame()) {
            return new Pair<>(false, null);
        }
        if (fullScaleSearch(64, false)) {
            return new Pair<>(true, null);
        }
        return new Pair<>(false, null);
    }

    private boolean hasMidgame() {
        return hasTemple() && hasVillage() && hasMonument() && hasOutpost() && hasMidgameTemples(5);
    }

    private Pair<Boolean, ArrayList<BPos>> filterBiomes() {
        if (!hasBiomeTiles()) {
            return new Pair<>(false, null);
        }
        Pair<Boolean, BPos> mushroomBiomes = hasMushroomBiomes();
        if (!mushroomBiomes.getFirst()) {
            return new Pair<>(false, null);
        }
        Pair<Boolean, BPos> jungleBiomes = hasJungleBiomes();
        if (!jungleBiomes.getFirst()) {
            return new Pair<>(false, null);
        }
        Pair<Boolean, BPos> megaTaigaBiomes = hasMegaTaigaBiomes();
        if (!megaTaigaBiomes.getFirst()) {
            return new Pair<>(false, null);
        }
        Pair<Boolean, BPos> snowyBiomes = hasSnowyBiomes();
        if (!snowyBiomes.getFirst()) {
            return new Pair<>(false, null);
        }
        Pair<Boolean, BPos> badlandsBiomes = hasBadlandsBiomes();
        if (!badlandsBiomes.getFirst()) {
            return new Pair<>(false, null);
        }

        ArrayList<BPos> coordinates = new ArrayList<>();
        coordinates.add(mushroomBiomes.getSecond());
        coordinates.add(jungleBiomes.getSecond());
        coordinates.add(megaTaigaBiomes.getSecond());
        coordinates.add(snowyBiomes.getSecond());
        coordinates.add(badlandsBiomes.getSecond());

        return new Pair<>(true, coordinates);
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

    private boolean hasBiomeTiles() {
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

        // need at least 3 special tiles for badlands, jungle, mega taiga
        if (specialPositions.size() < 3) {
            return false;
        }

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

        boolean badlands = false;
        boolean jungle = false;
        boolean megaTaiga = false;
        for (CPos pos: specialPositions) {
            if (biomeLayer9.sample(pos.getX(), 0, pos.getZ()) != 0) {
                switch (biomeLayer11.sample(pos.getX(), 0, pos.getZ())) {
                    case 1: {
                        badlands = true;
                        badlandsPositions.add(pos);
                        break;
                    }
                    case 2: {
                        jungle = true;
                        junglePositions.add(pos);
                        break;
                    }
                    case 3: {
                        megaTaiga = true;
                        megaTaigaPositions.add(pos);
                        break;
                    }
                }
            }
        }

        if (!badlands || !jungle || !megaTaiga) {
            return false;
        }

        boolean freezing = false;
        for (int x = -3; x <= 2; x++) {
            for (int z = -3; z <= 2; z++) {
                if (biomeLayer11.sample(x, 0, z) == 4) {
                    freezing = true;
                    snowyPositions.add(new CPos(x, z));
                }
            }
        }

        return freezing;
    }

    private Pair<Boolean, BPos> hasMushroomBiomes() {
        /*
        id 14 is mushroom_fields
        checking at 256:1

        id 15 is mushroom_field_shore
        checking at 16:1
        */
        for (CPos pos: mushroomPositions) {
            if (biomeLayer16.sample(pos.getX(), 0, pos.getZ()) == 14) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int x_16 = pos.getX() * 16 + x;
                        int z_16 = pos.getZ() * 16 + z;
                        if (biomeLayer31.sample(x_16, 0, z_16) == 15) {
                            return new Pair<>(true, new BPos(x_16 * 16, 0, z_16 * 16));
                        }
                    }
                }
            }
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, BPos> hasJungleBiomes() {
        /*
        id 168 is bamboo_jungle
        checking at 256:1

        id 169 is bamboo_jungle_hills
        checking at 64:1
        */
        for (CPos pos: junglePositions) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    int x_256 = pos.getX() * 4 + x;
                    int z_256 = pos.getZ() * 4 + z;
                    if (biomeLayer19.sample(x_256, 0, z_256) == 168) {
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                int x_64 = x_256 * 4 + i;
                                int z_64 = z_256 * 4 + j;
                                if (biomeLayer26.sample(x_64, 0, z_64) == 169) {
                                    return new Pair<>(true, new BPos(x_64 * 64, 0, z_64 * 64));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, BPos> hasMegaTaigaBiomes() {
        /*
        id 32 is giant_tree_taiga
        checking at 256:1

        id 33 is giant_tree_taiga_hills
        checking at 64:1
        */
        for (CPos pos: megaTaigaPositions) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    int x_256 = pos.getX() * 4 + x;
                    int z_256 = pos.getZ() * 4 + z;
                    if (biomeLayer19.sample(x_256, 0, z_256) == 32) {
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                int x_64 = x_256 * 4 + i;
                                int z_64 = z_256 * 4 + j;
                                if (biomeLayer26.sample(x_64, 0, z_64) == 33) {
                                    return new Pair<>(true, new BPos(x_64 * 64, 0, z_64 * 64));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, BPos> hasBadlandsBiomes() {
        /*
        id 38 is wooded_badlands_plateau
        checking at 64:1

        id 39 is badlands_plateau
        checking at 64:1
        */
        boolean woodedBadlandsPlateau;
        boolean badlandsPlateau;
        for (CPos pos: badlandsPositions) {
            woodedBadlandsPlateau = false;
            badlandsPlateau = false;
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    int x_64 = pos.getX() * 16 + x;
                    int z_64 = pos.getZ() * 16 + z;
                    if (biomeLayer26.sample(x_64, 0, z_64) == 38) {
                        woodedBadlandsPlateau = true;
                        if (badlandsPlateau) {
                            return new Pair<>(true, new BPos(x_64 * 64, 0, z_64 * 64));
                        }
                    } else if (biomeLayer26.sample(x_64, 0, z_64) == 39) {
                        badlandsPlateau = true;
                        if (woodedBadlandsPlateau) {
                            return new Pair<>(true, new BPos(x_64 * 64, 0, z_64 * 64));
                        }
                    }
                }
            }
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, BPos> hasSnowyBiomes() {
        /*
        id 30 is snowy_taiga
        checking at 256:1

        id 31 is snowy_taiga_hills
        checking at 64:1
        */
        for (CPos pos: snowyPositions) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    int x_256 = pos.getX() * 4 + x;
                    int z_256 = pos.getZ() * 4 + z;
                    if (biomeLayer19.sample(x_256, 0, z_256) == 30) {
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                int x_64 = x_256 * 4 + i;
                                int z_64 = z_256 * 4 + j;
                                if (biomeLayer26.sample(x_64, 0, z_64) == 31) {
                                    return new Pair<>(true, new BPos(x_64 * 64, 0, z_64 * 64));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Pair<>(false, null);
    }

    private boolean fullScaleSearch(int resolution, boolean checkerboard) {
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

        int minX = -3072 / resolution;
        int minZ = -3072 / resolution;
        int maxX = (3072 / resolution) - 1;
        int maxZ = (3072 / resolution) - 1;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
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
