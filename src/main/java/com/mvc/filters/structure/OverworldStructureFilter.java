package com.mvc.filters.structure;

import com.mvc.Config;
import com.mvc.filters.LootTables;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.LootTable;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.Monument;
import com.seedfinding.mcfeature.structure.PillagerOutpost;
import com.seedfinding.mcfeature.structure.Village;

import java.util.List;

public class OverworldStructureFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    private CPos templePos;

    public OverworldStructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterStructures() {
        return hasVillage() && hasTemple() && hasMonument() && hasOutpost() && hasTempleLoot();
    }

    private boolean hasVillage() {
        Village village = new Village(Config.VERSION);
        CPos villagePos = village.getInRegion(structureSeed, 0, 0, chunkRand);

        return villagePos.getMagnitude() <= Config.VILLAGE_DISTANCE;
    }

    private boolean hasTemple() {
        DesertPyramid temple = new DesertPyramid(Config.VERSION);
        templePos = temple.getInRegion(structureSeed, 0, 0, chunkRand);

        return templePos.getMagnitude() <= Config.TEMPLE_DISTANCE;
    }

    private boolean hasMonument() {
        Monument mm = new Monument(Config.VERSION);

        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 2; z++) {
                CPos mmPos = mm.getInRegion(structureSeed, x, z, chunkRand);

                if (mmPos.getMagnitude() <= Config.MONUMENT_DISTANCE) {
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

                if (poPos != null && poPos.getMagnitude() <= Config.OUTPOST_DISTANCE) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasTempleLoot() {
        chunkRand.setDecoratorSeed(structureSeed, templePos.getX() << 4, templePos.getZ() << 4, 40003, Config.VERSION);
        LootTable lootTable = LootTables.DESERT_PYRAMID_CHEST;
        lootTable.apply(Config.VERSION);

        int gunpowder = 0;

        for (int i = 0; i < 4; i++) {
            LootContext lootContext = new LootContext(chunkRand.nextLong(), Config.VERSION);
            List<ItemStack> chest = lootTable.generate(lootContext);

            for (ItemStack itemStack : chest) {
                Item item = itemStack.getItem();

                if (item.equals(Items.GUNPOWDER)) {
                    gunpowder += itemStack.getCount();
                }
            }
        }

        return gunpowder >= 10;
    }
}
