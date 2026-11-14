package xy177.farmersfuturedelight.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDEntityLootTables {
    private static final Field ATTACKING_PLAYER =
            ObfuscationReflectionHelper.findField(EntityLivingBase.class, "field_70717_bb");
    public static final ResourceLocation DROWNED = table("drowned");
    public static final ResourceLocation COD = table("cod");
    public static final ResourceLocation SALMON = table("salmon");
    public static final ResourceLocation PUFFERFISH = table("pufferfish");
    public static final ResourceLocation TROPICAL_FISH = table("tropical_fish");
    public static final ResourceLocation DOLPHIN = table("dolphin");
    public static final ResourceLocation PHANTOM = table("phantom");
    public static final ResourceLocation TURTLE = table("turtle");
    public static final ResourceLocation GLOW_SQUID = table("glow_squid");
    public static final ResourceLocation PANDA = table("panda");
    public static final ResourceLocation CAT = table("cat");
    public static final ResourceLocation PIGLIN = table("piglin");
    public static final ResourceLocation PIGLIN_BRUTE = table("piglin_brute");
    public static final ResourceLocation PIGLIN_BARTERING = new ResourceLocation(FarmerFutureDelight.MODID, "gameplay/piglin_bartering");
    public static final ResourceLocation STRIDER = table("strider");
    public static final ResourceLocation HOGLIN = table("hoglin");
    public static final ResourceLocation ZOGLIN = table("zoglin");
    public static final ResourceLocation CAT_MORNING_GIFT =
            new ResourceLocation(FarmerFutureDelight.MODID, "gameplay/cat_morning_gift");
    public static final ResourceLocation PANDA_SNEEZE =
            new ResourceLocation(FarmerFutureDelight.MODID, "gameplay/panda_sneeze");
    public static final ResourceLocation RAID_CAPTAIN =
            new ResourceLocation(FarmerFutureDelight.MODID, "gameplay/raid_captain");
    public static final ResourceLocation CREEPER_MUSIC_DISCS =
            new ResourceLocation(FarmerFutureDelight.MODID, "gameplay/creeper_music_discs");

    private FFDEntityLootTables() { }

    private static ResourceLocation table(String name) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "entities/" + name);
    }

    public static void register() {
        for (ResourceLocation name : new ResourceLocation[]{DROWNED, COD, SALMON, PUFFERFISH,
                TROPICAL_FISH, DOLPHIN, PHANTOM, TURTLE, GLOW_SQUID, PANDA, CAT, CAT_MORNING_GIFT,
                PANDA_SNEEZE, RAID_CAPTAIN, CREEPER_MUSIC_DISCS, table("pillager"), table("ravager"),
                HOGLIN, ZOGLIN, STRIDER, PIGLIN, PIGLIN_BRUTE, PIGLIN_BARTERING}) {
            if (!LootTableList.getAll().contains(name)) LootTableList.register(name);
        }
        LootConditionManager.registerCondition(new DamageType.Serializer());
        LootConditionManager.registerCondition(new PlayerCredit.Serializer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoad(LootTableLoadEvent event) {
        if (DROWNED.equals(event.getName())) {
            addPool(event, "copper_ingot", FFDItems.effectiveStack(FFDItems.COPPER_INGOT),
                    new LootFunction[0], new LootCondition[]{new PlayerCredit(),
                            new RandomChanceWithLooting(0.11F, 0.02F)});
        } else if (PHANTOM.equals(event.getName())) {
            addPool(event, "phantom_membrane", FFDItems.effectiveStack(FFDItems.PHANTOM_MEMBRANE),
                    counted(0, 1), new LootCondition[]{new PlayerCredit()});
        } else if (GLOW_SQUID.equals(event.getName())) {
            addPool(event, "glow_ink_sac", FFDItems.effectiveStack(FFDItems.GLOW_INK_SAC),
                    counted(1, 3), new LootCondition[0]);
        } else if (TURTLE.equals(event.getName())) {
            net.minecraft.item.Item seagrass =
                    xy177.farmersfuturedelight.common.entity.EntityTurtle.getPrimarySeagrassItem();
            if (seagrass != null) addPool(event, "seagrass", new ItemStack(seagrass),
                    counted(0, 2), new LootCondition[0]);
        } else if (PANDA.equals(event.getName())) {
            addPool(event, "bamboo", FFDItems.effectiveStack(FFDItems.BAMBOO),
                    new LootFunction[0], new LootCondition[0]);
        } else if (CAT.equals(event.getName())) {
            addPool(event, "string", new ItemStack(net.minecraft.init.Items.STRING),
                    counted(0, 2), new LootCondition[0]);
        } else if (CAT_MORNING_GIFT.equals(event.getName())) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null && pool.getEntry("phantom_membrane") == null) {
                ItemStack membrane = FFDItems.effectiveStack(FFDItems.PHANTOM_MEMBRANE);
                if (membrane.isEmpty()) membrane = new ItemStack(net.minecraft.init.Items.STRING);
                pool.addEntry(entry("phantom_membrane", membrane, 2, new LootFunction[0]));
            }
        }
    }

    private static LootFunction[] counted(float min, float max) {
        return new LootFunction[]{new SetCount(new LootCondition[0], new RandomValueRange(min, max)),
                new LootingEnchantBonus(new LootCondition[0], new RandomValueRange(0, 1), 0)};
    }

    public static LootContext deathContext(WorldServer world, EntityLivingBase entity,
            DamageSource source, boolean recentlyHit) {
        LootContext.Builder context = new LootContext.Builder(world)
                .withLootedEntity(entity).withDamageSource(source);
        try {
            EntityPlayer player = (EntityPlayer) ATTACKING_PLAYER.get(entity);
            if (recentlyHit && player != null) context.withPlayer(player).withLuck(player.getLuck());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
        return context.build();
    }

    private static void addPool(LootTableLoadEvent event, String name, ItemStack stack,
            LootFunction[] functions, LootCondition[] conditions) {
        if (stack.isEmpty() || event.getTable().getPool(name) != null) return;
        event.getTable().addPool(new LootPool(new LootEntryItem[]{entry(name, stack, 1, functions)}, conditions,
                new RandomValueRange(1), new RandomValueRange(0), name));
    }

    private static LootEntryItem entry(String name, ItemStack stack, int weight, LootFunction[] functions) {
        List<LootFunction> all = new ArrayList<>();
        if (stack.getMetadata() != 0)
            all.add(new SetMetadata(new LootCondition[0], new RandomValueRange(stack.getMetadata())));
        if (stack.hasTagCompound()) all.add(new SetNBT(new LootCondition[0], stack.getTagCompound().copy()));
        all.addAll(Arrays.asList(functions));
        return new LootEntryItem(stack.getItem(), weight, 0, all.toArray(new LootFunction[0]),
                new LootCondition[0], name);
    }

    private static final class PlayerCredit implements LootCondition {
        private static final Field RECENTLY_HIT =
                ObfuscationReflectionHelper.findField(EntityLivingBase.class, "field_70718_bc");

        @Override
        public boolean testCondition(Random random, LootContext context) {
            if (context.getKillerPlayer() != null) return true;
            try {
                return context.getLootedEntity() instanceof EntityLivingBase
                        && RECENTLY_HIT.getInt(context.getLootedEntity()) > 0;
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static final class Serializer extends LootCondition.Serializer<PlayerCredit> {
            private Serializer() {
                super(new ResourceLocation(FarmerFutureDelight.MODID, "player_credit"), PlayerCredit.class);
            }

            @Override
            public void serialize(JsonObject json, PlayerCredit condition, JsonSerializationContext context) {
            }

            @Override
            public PlayerCredit deserialize(JsonObject json, JsonDeserializationContext context) {
                return new PlayerCredit();
            }
        }
    }

    private static final class DamageType implements LootCondition {
        private static final Field SOURCE = ObfuscationReflectionHelper.findField(LootContext.class, "field_186503_f");
        private final String type;
        private DamageType(String type) { this.type = type; }

        @Override
        public boolean testCondition(Random random, LootContext context) {
            try {
                DamageSource source = (DamageSource) SOURCE.get(context);
                return source != null && type.equals(source.getDamageType());
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static final class Serializer extends LootCondition.Serializer<DamageType> {
            private Serializer() {
                super(new ResourceLocation(FarmerFutureDelight.MODID, "damage_source"), DamageType.class);
            }

            @Override
            public void serialize(JsonObject json, DamageType condition, JsonSerializationContext context) {
                json.addProperty("damage_type", condition.type);
            }

            @Override
            public DamageType deserialize(JsonObject json, JsonDeserializationContext context) {
                return new DamageType(JsonUtils.getString(json, "damage_type"));
            }
        }
    }
}
