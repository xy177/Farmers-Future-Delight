package xy177.farmersfuturedelight.common;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDLootEvents {
    private static final String GLOW_BERRIES_ENTRY =
            FarmerFutureDelight.MODID + ":glow_berries";
    private static final String OTHERSIDE_DUNGEON_ENTRY =
            FarmerFutureDelight.MODID + ":otherside_simple_dungeon";
    private static final String OTHERSIDE_STRONGHOLD_ENTRY =
            FarmerFutureDelight.MODID + ":otherside_stronghold_corridor";

    private FFDLootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (FFDItems.isGlowBerryEnabled()
                && LootTableList.CHESTS_ABANDONED_MINESHAFT.equals(event.getName())) {
            addGlowBerries(event);
        } else if (FFDItems.isOthersideEnabled()
                && LootTableList.CHESTS_SIMPLE_DUNGEON.equals(event.getName())) {
            addOtherside(event, 2, OTHERSIDE_DUNGEON_ENTRY);
        } else if (FFDItems.isOthersideEnabled()
                && LootTableList.CHESTS_STRONGHOLD_CORRIDOR.equals(event.getName())) {
            addOtherside(event, 1, OTHERSIDE_STRONGHOLD_ENTRY);
        }
    }

    private static void addGlowBerries(LootTableLoadEvent event) {
        LootPool pool = event.getTable().getPool("pool1");
        ItemStack berries = FFDItems.effectiveStack(FFDItems.GLOW_BERRIES);
        if (pool == null || berries.isEmpty()
                || pool.getEntry(GLOW_BERRIES_ENTRY) != null) {
            return;
        }

        LootFunction[] functions = {
                new SetCount(new LootCondition[0], new RandomValueRange(3.0F, 6.0F))
        };
        pool.addEntry(new LootEntryItem(berries.getItem(), 15, 0, functions,
                new LootCondition[0], GLOW_BERRIES_ENTRY));
    }

    private static void addOtherside(LootTableLoadEvent event, int weight, String entryName) {
        LootPool pool = event.getTable().getPool("pool1");
        ItemStack disc = FFDItems.effectiveStack(FFDItems.MUSIC_DISC_OTHERSIDE);
        if (pool == null || disc.isEmpty() || pool.getEntry(entryName) != null) {
            return;
        }
        pool.addEntry(new LootEntryItem(disc.getItem(), weight, 0,
                new LootFunction[0], new LootCondition[0], entryName));
    }
}
