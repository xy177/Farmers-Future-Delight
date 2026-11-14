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
import xy177.farmersfuturedelight.common.worldgen.FFDModernLootTable;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures;
import xy177.farmersfuturedelight.common.worldgen.FFDOceanStructures;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDLootEvents {
    private static final String GLOW_BERRIES_ENTRY =
            FarmerFutureDelight.MODID + ":glow_berries";
    private static final String OTHERSIDE_DUNGEON_ENTRY =
            FarmerFutureDelight.MODID + ":otherside_simple_dungeon";
    private static final String OTHERSIDE_STRONGHOLD_ENTRY =
            FarmerFutureDelight.MODID + ":otherside_stronghold_corridor";
    private static final String NAUTILUS_SHELL_ENTRY =
            FarmerFutureDelight.MODID + ":nautilus_shell_fishing_treasure";
    private static final String SUSPICIOUS_STEW_ENTRY =
            FarmerFutureDelight.MODID + ":suspicious_stew";
    private static final String NETHERITE_TEMPLATE_ENTRY =
            FarmerFutureDelight.MODID + ":netherite_upgrade_template";
    private static final net.minecraft.util.ResourceLocation NETHER_BRIDGE_LOOT =
            new net.minecraft.util.ResourceLocation("minecraft", "chests/nether_bridge");

    private FFDLootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        FFDFishing.adjust(event);
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
        if (FFDItems.isNetheriteEnabled() && NETHER_BRIDGE_LOOT.equals(event.getName())) {
            addNetheriteTemplate(event);
        }
        if ((FFDOceanStructures.isOceanLootTable(event.getName()) || FFDNetherStructures.isLootTable(event.getName()))
                && !(event.getTable() instanceof FFDModernLootTable)) {
            event.setTable(new FFDModernLootTable(event.getTable()));
        }
        if (!FFDItems.effectiveStack(FFDItems.SUSPICIOUS_STEW).isEmpty()
                && FFDOceanStructures.SHIPWRECK_SUPPLY_LOOT.equals(event.getName())) {
            addSuspiciousStew(event);
        }
        if (LootTableList.GAMEPLAY_FISHING_TREASURE.equals(event.getName())) {
            addNautilusShell(event);
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

    private static void addNautilusShell(LootTableLoadEvent event) {
        LootPool pool = event.getTable().getPool("main");
        ItemStack shell = FFDItems.effectiveStack(FFDItems.NAUTILUS_SHELL);
        if (pool == null || shell.isEmpty() || pool.getEntry(NAUTILUS_SHELL_ENTRY) != null) {
            return;
        }
        pool.addEntry(new LootEntryItem(shell.getItem(), 1, 0,
                new LootFunction[0], new LootCondition[0], NAUTILUS_SHELL_ENTRY));
    }

    private static void addSuspiciousStew(LootTableLoadEvent event) {
        LootPool pool = event.getTable().getPool("supplies");
        if (pool == null || pool.getEntry(SUSPICIOUS_STEW_ENTRY) != null) {
            return;
        }
        LootFunction[] functions = {
                new xy177.farmersfuturedelight.common.worldgen.FFDSuspiciousStewLootFunction(
                        new LootCondition[0])
        };
        pool.addEntry(new LootEntryItem(FFDItems.effectiveStack(FFDItems.SUSPICIOUS_STEW).getItem(),
                10, 0, functions,
                new LootCondition[0], SUSPICIOUS_STEW_ENTRY));
    }

    private static void addNetheriteTemplate(LootTableLoadEvent event) {
        LootPool pool = event.getTable().getPool("pool0");
        if (pool == null) {
            pool = event.getTable().getPool("main");
        }
        ItemStack template = FFDItems.effectiveStack(
                FFDItems.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        if (pool == null || template.isEmpty()
                || pool.getEntry(NETHERITE_TEMPLATE_ENTRY) != null) {
            return;
        }
        pool.addEntry(new LootEntryItem(template.getItem(), 1, 0,
                new LootFunction[0], new LootCondition[] {(random, context) -> !FFDNetherStructures.generatesBastions(context.getWorld())},
                NETHERITE_TEMPLATE_ENTRY));
    }
}
