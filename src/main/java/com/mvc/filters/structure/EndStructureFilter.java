package com.mvc.filters.structure;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.EndBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.generator.Generator;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EndStructureFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private BPos gatewayPos;

    public EndStructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterStructures() {
        firstGatewayPos();
        return hasCity();
    }

    private void firstGatewayPos() {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            gateways.add(i);
        }
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * (-1 * Math.PI + 0.15707963267948966 * (gateways.removeLast()));
        int x = (int) (1024.0 * Math.cos(angle));
        int z = (int) (1024.0 * Math.sin(angle));

        gatewayPos = new BPos(x, 0, z);
    }

    private boolean hasCity() {
        RPos gatewayRegion = gatewayPos.toRegionPos(20 << 4);
        EndCity city = new EndCity(Config.VERSION);
        CPos cityPos = city.getInRegion(structureSeed, gatewayRegion.getX(), gatewayRegion.getZ(), chunkRand);

        if (!(cityPos.distanceTo(gatewayPos.toChunkPos(), DistanceMetric.EUCLIDEAN) <= Config.END_CITY_DISTANCE)) {
            return false;
        }

        EndCityGenerator ecg = new EndCityGenerator(Config.VERSION);
        EndBiomeSource endBiomeSource = new EndBiomeSource(Config.VERSION, structureSeed);
        EndTerrainGenerator endTerrainGenerator = new EndTerrainGenerator(endBiomeSource);

        if (!ecg.generate(endTerrainGenerator, cityPos, chunkRand)) {
            return false;
        }

        for (Pair<Generator.ILootType, BPos> e : ecg.getChestsPos()) {
            if (e.getFirst().equals(EndCityGenerator.LootType.SHIP_ELYTRA)) {
                if (e.getSecond().toChunkPos().distanceTo(cityPos, DistanceMetric.EUCLIDEAN) > 9) {
                    System.out.println(structureSeed + ": cut off ship found at /execute in minecraft:the_end run tp @s " + e.getSecond().getX() + " ~ " + e.getSecond().getZ());
                    return false;
                }
            }
        }

        // ship room + 4 other chests
        return ecg.hasShip() && ecg.getLootPos().size() >= 6;
    }
}
