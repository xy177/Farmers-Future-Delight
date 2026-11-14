package xy177.farmersfuturedelight.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public final class FFDFishing {
    private static final Field CONDITIONS = ObfuscationReflectionHelper.findField(LootEntry.class, "field_186366_e");
    private static final Field CATCHABLE = ObfuscationReflectionHelper.findField(EntityFishHook.class, "field_146045_ax");
    private static final Field APPROACH = ObfuscationReflectionHelper.findField(EntityFishHook.class, "field_146038_az");
    private static final Field STATE = ObfuscationReflectionHelper.findField(EntityFishHook.class, "field_190627_av");
    private static final String OPEN = "FFDOpenWater";
    private static final String OUT = "FFDOutOfWater";

    private FFDFishing() {
    }

    public static void register() {
        LootConditionManager.registerCondition(new OpenWater.Serializer());
    }

    public static void update(EntityFishHook hook) {
        if (hook.world.isRemote) return;
        try {
            if (!"BOBBING".equals(STATE.get(hook).toString())) return;
            int out = hook.getEntityData().getInteger(OUT);
            boolean active = CATCHABLE.getInt(hook) > 0 || APPROACH.getInt(hook) > 0;
            boolean open = !hook.getEntityData().hasKey(OPEN) || hook.getEntityData().getBoolean(OPEN);
            open = !active || open && out < 10 && calculate(hook.world, hook.getPosition());
            boolean water = WaterloggedBlockApi.containsWater(hook.world, hook.getPosition());
            hook.getEntityData().setInteger(OUT, water ? Math.max(0, out - 1) : Math.min(10, out + 1));
            hook.getEntityData().setBoolean(OPEN, open);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to update fishing water", exception);
        }
    }

    public static boolean isOpen(EntityFishHook hook) {
        return (!hook.getEntityData().hasKey(OPEN) || hook.getEntityData().getBoolean(OPEN))
                && calculate(hook.world, hook.getPosition());
    }

    public static boolean calculate(World world, BlockPos center) {
        int previous = -1;
        for (int offsetY = -1; offsetY <= 2; offsetY++) {
            int layer = -1;
            for (BlockPos pos : BlockPos.getAllInBox(center.add(-2, offsetY, -2), center.add(2, offsetY, 2))) {
                if (!world.isBlockLoaded(pos)) return false;
                IBlockState state = world.getBlockState(pos);
                int type = state.getBlock().isAir(state, world, pos) || state.getBlock() == Blocks.WATERLILY ? 0
                        : WaterloggedBlockApi.isWaterSource(world, pos)
                          && state.getCollisionBoundingBox(world, pos) == null ? 1 : -1;
                if (type == -1 || layer != -1 && layer != type) return false;
                layer = type;
            }
            if (layer == 0 && previous == -1 || layer == 1 && previous == 0) return false;
            previous = layer;
        }
        return true;
    }

    public static void adjust(LootTableLoadEvent event) {
        LootPool pool = event.getTable().getPool("main");
        if (pool == null) return;
        if (LootTableList.GAMEPLAY_FISHING.equals(event.getName())) {
            LootEntry entry = pool.getEntry("minecraft:gameplay/fishing/treasure");
            if (entry == null) return;
            try {
                LootCondition[] conditions = (LootCondition[]) CONDITIONS.get(entry);
                for (LootCondition condition : conditions) if (condition instanceof OpenWater) return;
                LootCondition[] updated = Arrays.copyOf(conditions, conditions.length + 1);
                updated[conditions.length] = new OpenWater();
                CONDITIONS.set(entry, updated);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Unable to extend fishing treasure conditions", exception);
            }
        } else if (LootTableList.GAMEPLAY_FISHING_TREASURE.equals(event.getName())) {
            pool.removeEntry("minecraft:waterlily");
        } else if (LootTableList.GAMEPLAY_FISHING_JUNK.equals(event.getName())
                && pool.getEntry("minecraft:waterlily") == null) {
            pool.addEntry(new LootEntryItem(Item.getItemFromBlock(Blocks.WATERLILY), 17, 0,
                    new LootFunction[0], new LootCondition[0], "minecraft:waterlily"));
        }
    }

    public static final class OpenWater implements LootCondition {
        @Override
        public boolean testCondition(Random random, LootContext context) {
            return context.getLootedEntity() instanceof EntityFishHook
                    && isOpen((EntityFishHook) context.getLootedEntity());
        }

        public static final class Serializer extends LootCondition.Serializer<OpenWater> {
            public Serializer() {
                super(new ResourceLocation(FarmerFutureDelight.MODID, "open_water"), OpenWater.class);
            }

            @Override
            public void serialize(JsonObject object, OpenWater condition, JsonSerializationContext context) {
            }

            @Override
            public OpenWater deserialize(JsonObject object, JsonDeserializationContext context) {
                return new OpenWater();
            }
        }
    }
}
