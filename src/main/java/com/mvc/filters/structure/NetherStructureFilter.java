package com.mvc.filters.structure;

import com.mvc.Config;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.Fortress;

public class NetherStructureFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private CPos fortressPos;

    public NetherStructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterStructures() {
        return hasBastion() && hasFortress() && isSSV();
    }

    private boolean hasBastion() {
        BastionRemnant bastion = new BastionRemnant(Config.VERSION);

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                CPos bastionPos = bastion.getInRegion(structureSeed, x, z, chunkRand);
                if (bastionPos != null && bastionPos.getMagnitude() <= Config.BASTION_DISTANCE) {
                    if (bastion.canSpawn(bastionPos, new NetherBiomeSource(Config.VERSION, structureSeed))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasFortress() {
        Fortress fortress = new Fortress(Config.VERSION);

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                fortressPos = fortress.getInRegion(structureSeed, x, z, chunkRand);
                if (fortressPos != null && fortressPos.getMagnitude() <= Config.FORTRESS_DISTANCE) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSSV() {
        NetherBiomeSource netherBiomeSource = new NetherBiomeSource(Config.VERSION, structureSeed);

        return netherBiomeSource.getBiome(fortressPos.toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(-4, 0).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(4, 0).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(0, -4).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(0, 4).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY);
    }
}
