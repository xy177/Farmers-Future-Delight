package xy177.farmersfuturedelight.common.registry;

import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.text.TextComponentTranslation;

public final class FFDStats {
    public static final StatBase BARREL_OPENED = register("open_barrel");
    public static final StatBase BLAST_FURNACE_INTERACTION = register("interact_with_blast_furnace");
    public static final StatBase SMOKER_INTERACTION = register("interact_with_smoker");
    public static final StatBase CAMPFIRE_INTERACTION = register("interact_with_campfire");
    public static final StatBase CARTOGRAPHY_TABLE_INTERACTION = register("interact_with_cartography_table");
    public static final StatBase COMPOSTER_INTERACTION = register("interact_with_composter");
    public static final StatBase GRINDSTONE_INTERACTION = register("interact_with_grindstone");
    public static final StatBase ANVIL_INTERACTION = register("interact_with_anvil");
    public static final StatBase LECTERN_INTERACTION = register("interact_with_lectern");
    public static final StatBase LOOM_INTERACTION = register("interact_with_loom");
    public static final StatBase SMITHING_TABLE_INTERACTION = register("interact_with_smithing_table");
    public static final StatBase STONECUTTER_INTERACTION = register("interact_with_stonecutter");
    public static final StatBase BELL_RING = register("ring_bell");

    private FFDStats() {
    }

    public static void init() {
    }

    private static StatBase register(String name) {
        return new StatBasic("stat.farmers_future_delight." + name,
                new TextComponentTranslation("stat.farmers_future_delight." + name))
                .registerStat();
    }
}
