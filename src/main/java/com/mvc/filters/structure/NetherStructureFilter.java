package com.mvc.filters.structure;

import com.mvc.Config;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator;

import java.util.Optional;
import java.util.Random;

public class NetherStructureFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private CPos bastionPos;
    private CPos fortressPos;
    private NetherBiomeSource netherBiomeSource;

    public NetherStructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterStructures() {
        return hasBastion() && hasFortress() && isSSV() && hasBastionTerrainHeightCheck();
    }

    private boolean hasBastion() {
        BastionRemnant bastion = new BastionRemnant(Config.VERSION);

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                CPos curBastion = bastion.getInRegion(structureSeed, x, z, chunkRand);
                if (curBastion != null && curBastion.getMagnitude() <= Config.BASTION_DISTANCE) {
                    if (bastionPos != null) {
                        return false;
                    }
                    bastionPos = curBastion;
                }
            }
        }
        return bastionPos != null && bastion.canSpawn(bastionPos, new NetherBiomeSource(Config.VERSION, structureSeed));
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

    private boolean hasBastionTerrainAirSampling() {
        Random random = new Random();
        NetherTerrainGenerator netherTerrainGenerator = new NetherTerrainGenerator(netherBiomeSource);
        int air = 0;
        CPos spawn = new CPos(0, 0);

        while (spawn.getX() != bastionPos.getX() || spawn.getZ() != bastionPos.getZ()) {
            // move toward bastion along axis we are furthest from
            if (Math.abs(bastionPos.getX() - spawn.getX()) < Math.abs(bastionPos.getZ() - spawn.getZ())) {
                spawn = spawn.add(0, bastionPos.getZ() > 0 ? 1 : -1);
            } else {
                spawn = spawn.add(bastionPos.getX() > 0 ? 1 : -1, 0);
            }

            // sample chunk and see if it meets air threshold
            for (int s = 0; s < 25; s++) {
                int x = random.nextInt(16);
                int y = random.nextInt(16);
                int z = random.nextInt(16);

                Optional<Block> block = netherTerrainGenerator.getBlockAt(spawn.toBlockPos().getX() + x, 57 + y, spawn.toBlockPos().getZ() + z);
                if (block.isPresent() && block.get().equals(Blocks.AIR)) {
                    air++;
                }
            }

            if (air < 1) {
                return false;
            }
        }
        return true;
    }

    private boolean hasBastionTerrainHeightCheck() {
        NetherTerrainGenerator netherTerrainGenerator = new NetherTerrainGenerator(netherBiomeSource);
        BPos approxBastion = new BPos(bastionPos.toBlockPos(64));

        for (int i = 1; i <= 10; i++) {
            double t = (double) i / 10;
            int x = (int) (approxBastion.getX() * t);
            int z = (int) (approxBastion.getZ() * t);
            Block[] column = netherTerrainGenerator.getColumnAt(x, z);
            int air = 0;
            boolean lastBlockAir = false;

            for (int b = 0; b < column.length; b++) {
                if (b < 40 || b > 100) {
                    continue;
                }
                if (!lastBlockAir && column[b].equals(Blocks.AIR)) {
                    lastBlockAir = true;
                    continue;
                }
                if (lastBlockAir && column[b].equals(Blocks.AIR)) {
                    if (++air > 5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSSV() {
        netherBiomeSource = new NetherBiomeSource(Config.VERSION, structureSeed);

        return netherBiomeSource.getBiome(fortressPos.toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(-4, 0).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(4, 0).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(0, -4).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY) &&
                netherBiomeSource.getBiome(fortressPos.add(0, 4).toBlockPos()).equals(Biomes.SOUL_SAND_VALLEY);
    }
}
