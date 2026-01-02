package com.mvc.filters.structure;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.*;

public class OverworldStructureFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;

    public OverworldStructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterStructures() {
        return hasOutpost() && hasVillage() && hasTemple();
    }

    private boolean hasVillage() {
        Village village = new Village(Config.VERSION);
        CPos villagePos = village.getInRegion(structureSeed, 0, 0, chunkRand);

        return villagePos.getMagnitude() <= Config.VILLAGE_DISTANCE;
    }

    private boolean hasTemple() {
        DesertPyramid temple = new DesertPyramid(Config.VERSION);
        CPos templePos = temple.getInRegion(structureSeed, 0, 0, chunkRand);

        return templePos.getMagnitude() <= Config.TEMPLE_DISTANCE;
    }

    private boolean hasOutpost() {
        PillagerOutpost po = new PillagerOutpost(Config.VERSION);

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 1; z++) {
                CPos poPos = po.getInRegion(structureSeed, x, z, chunkRand);

                if (poPos != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public CPos[] getStrongholds() {
        Stronghold stronghold = new Stronghold(Config.VERSION);
        return stronghold.getStarts(new OverworldBiomeSource(Config.VERSION, structureSeed), 3, chunkRand);
    }
}
