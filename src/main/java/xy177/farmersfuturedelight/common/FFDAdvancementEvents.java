package xy177.farmersfuturedelight.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.BlockJukebox;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherStructures;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDAdvancementEvents {
    private static final double WORLD_TOP = 319.0D;
    private static final double WORLD_BOTTOM = -59.0D;
    private static final double REQUIRED_FALL = 379.0D;
    private static final Map<UUID, FallState> FALLS = new HashMap<>();
    private static final Map<UUID, Integer> VILLAGER_TRADES = new HashMap<>();

    private FFDAdvancementEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJukeboxUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote || event.isCanceled()
                || !(event.getEntityPlayer() instanceof EntityPlayerMP)
                || !(event.getItemStack().getItem() instanceof ItemRecord)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (event.getWorld().getBlockState(pos).getBlock() == Blocks.JUKEBOX
                && !event.getWorld().getBlockState(pos).getValue(BlockJukebox.HAS_RECORD)
                && FFDVerticalBiomeManager.isBiome(event.getWorld(), pos,
                        FFDVerticalBiome.MEADOW)) {
            FFDAdvancements.PLAY_JUKEBOX_IN_MEADOWS.trigger(
                    (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemConsumed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        String path = itemPath(event.getItem());
        if ("sweetberries".equals(path) || "sweetberry".equals(path)) {
            FFDAdvancements.CONSUME_SWEET_BERRIES.trigger(player);
        } else if ("glowberries".equals(path) || "glowberry".equals(path)) {
            FFDAdvancements.CONSUME_GLOW_BERRIES.trigger(player);
        } else if ("honeybottle".equals(path) || "honeybottles".equals(path)) {
            FFDAdvancements.CONSUME_HONEY_BOTTLE.trigger(player);
        } else if ("driedkelp".equals(path)) {
            FFDAdvancements.CONSUME_DRIED_KELP.trigger(player);
        }
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getCausedByPlayer();
        String childPath = entityPath(event.getChild());
        if ("bee".equals(childPath)) {
            FFDAdvancements.BRED_BEE.trigger(player);
        } else if ("cat".equals(childPath)) {
            FFDAdvancements.BRED_CAT.trigger(player);
        } else if ("fox".equals(childPath)) {
            FFDAdvancements.BRED_FOX.trigger(player);
        } else if ("panda".equals(childPath)) {
            FFDAdvancements.BRED_PANDA.trigger(player);
        } else if ("turtle".equals(childPath)) {
            FFDAdvancements.BRED_TURTLE.trigger(player);
        } else if ("goat".equals(childPath)) {
            FFDAdvancements.BRED_GOAT.trigger(player);
        } else if ("axolotl".equals(childPath)) {
            FFDAdvancements.BRED_AXOLOTL.trigger(player);
        } else if ("hoglin".equals(childPath)) {
            FFDAdvancements.BRED_HOGLIN.trigger(player);
        } else if ("strider".equals(childPath)) {
            FFDAdvancements.BRED_STRIDER.trigger(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) source;
        String path = entityPath(event.getEntityLiving());
        if ("phantom".equals(path)) {
            FFDAdvancements.KILL_PHANTOM.trigger(player);
        } else if ("pillager".equals(path)) {
            FFDAdvancements.KILL_PILLAGER.trigger(player);
        } else if ("ravager".equals(path)) {
            FFDAdvancements.KILL_RAVAGER.trigger(player);
        } else if ("piglin".equals(path)) {
            FFDAdvancements.KILL_PIGLIN.trigger(player);
        } else if ("hoglin".equals(path)) {
            FFDAdvancements.KILL_HOGLIN.trigger(player);
        } else if ("zoglin".equals(path)) {
            FFDAdvancements.KILL_ZOGLIN.trigger(player);
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayerMP)) {
            return;
        }
        for (ItemStack drop : event.getDrops()) {
            if (!drop.isEmpty() && drop.getItem() == net.minecraft.init.Items.FISH) {
                FFDAdvancements.FISHY_BUSINESS.trigger(
                        (EntityPlayerMP) event.getEntityPlayer());
                return;
            }
        }
    }

    public static void containerLoot(net.minecraft.tileentity.TileEntityLockableLoot container,
                                     EntityPlayer player) {
        ResourceLocation table = container.getLootTable();
        if (player instanceof EntityPlayerMP && !player.isSpectator() && table != null
                && (FarmerFutureDelight.MODID.equals(table.getResourceDomain())
                    || "minecraft".equals(table.getResourceDomain()))
                && table.getResourcePath().startsWith("chests/bastion_")) {
            FFDAdvancements.LOOT_BASTION.trigger((EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote
                || !(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        checkGoatBoat(player);
        checkWorldHeightFall(player);
        checkWorldHeightTrade(player);
        FFDVerticalBiome vertical = FFDVerticalBiomeManager.getBiome(player.world, player.getPosition());
        FFDAdvancements.triggerVerticalBiome(player, vertical);
        if (player.ticksExisted % 20 == 0) {
            checkInventoryAdvancements(player);
            checkNetherAdvancements(player, vertical);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            rememberTradeCount(player);
            satisfyDisabledCompatibilityCriteria(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        FALLS.remove(id);
        VILLAGER_TRADES.remove(id);
    }

    private static void checkNetherAdvancements(EntityPlayerMP player, FFDVerticalBiome biome) {
        if (player.world.provider.getDimension() != -1 || !NetherGeneration.isModern(player.world)) return;
        if (biome == FFDVerticalBiome.NETHER_WASTES) FFDAdvancements.VISIT_NETHER_WASTES.trigger(player);
        if (biome == FFDVerticalBiome.SOUL_SAND_VALLEY) FFDAdvancements.VISIT_SOUL_SAND_VALLEY.trigger(player);
        if (biome == FFDVerticalBiome.CRIMSON_FOREST) FFDAdvancements.VISIT_CRIMSON_FOREST.trigger(player);
        if (biome == FFDVerticalBiome.WARPED_FOREST) FFDAdvancements.VISIT_WARPED_FOREST.trigger(player);
        if (biome == FFDVerticalBiome.BASALT_DELTAS) FFDAdvancements.VISIT_BASALT_DELTAS.trigger(player);
        if (FFDAdvancements.FIND_BASTION.hasListeners(player)
                && FFDNetherStructures.inside(player.world, player.getPosition())) {
            FFDAdvancements.FIND_BASTION.trigger(player);
        }
    }

    private static void checkInventoryAdvancements(EntityPlayerMP player) {
        if (FFDAdvancements.OBTAIN_ANCIENT_DEBRIS.hasListeners(player)
                && hasInventoryItem(player, FFDItems.ANCIENT_DEBRIS)) {
            FFDAdvancements.OBTAIN_ANCIENT_DEBRIS.trigger(player);
        }
        if (FFDAdvancements.OBTAIN_CRYING_OBSIDIAN.hasListeners(player)
                && hasInventoryItem(player, FFDItems.CRYING_OBSIDIAN)) {
            FFDAdvancements.OBTAIN_CRYING_OBSIDIAN.trigger(player);
        }
        if (FFDAdvancements.NETHERITE_ARMOR.hasListeners(player) && hasNetheriteArmor(player)) {
            FFDAdvancements.NETHERITE_ARMOR.trigger(player);
        }
        if (FFDAdvancements.OBTAIN_NETHERITE_HOE.hasListeners(player)
                && hasInventoryItem(player, FFDItems.NETHERITE_HOE)) {
            FFDAdvancements.OBTAIN_NETHERITE_HOE.trigger(player);
        }
        if (FFDAdvancements.OBTAIN_BLACKSTONE.hasListeners(player)
                && hasInventoryItem(player, Item.getItemFromBlock(FFDBlocks.BLACKSTONE))) {
            FFDAdvancements.OBTAIN_BLACKSTONE.trigger(player);
        }
    }

    private static boolean hasInventoryItem(EntityPlayer player, Item item) {
        ItemStack expected = FFDItems.effectiveStack(item);
        if (expected.isEmpty()) return false;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == expected.getItem()) return true;
        }
        for (ItemStack stack : player.inventory.armorInventory) {
            if (!stack.isEmpty() && stack.getItem() == expected.getItem()) return true;
        }
        return !player.getHeldItemOffhand().isEmpty()
                && player.getHeldItemOffhand().getItem() == expected.getItem();
    }

    private static boolean hasNetheriteArmor(EntityPlayer player) {
        Item[] required = new Item[] {FFDItems.NETHERITE_HELMET, FFDItems.NETHERITE_CHESTPLATE,
                FFDItems.NETHERITE_LEGGINGS, FFDItems.NETHERITE_BOOTS};
        for (Item requiredItem : required) {
            if (!hasInventoryItem(player, requiredItem)) return false;
        }
        return true;
    }

    private static void checkGoatBoat(EntityPlayerMP player) {
        Entity vehicle = player.getRidingEntity();
        if (!(vehicle instanceof EntityBoat)) {
            return;
        }
        for (Entity passenger : vehicle.getPassengers()) {
            if (passenger instanceof EntityGoat) {
                FFDAdvancements.RIDE_A_BOAT_WITH_A_GOAT.trigger(player);
                return;
            }
        }
    }

    private static void checkWorldHeightFall(EntityPlayerMP player) {
        UUID id = player.getUniqueID();
        FallState fall = FALLS.get(id);
        if (player.posY >= WORLD_TOP) {
            if (fall == null || player.posY > fall.startY) {
                FALLS.put(id, new FallState(player.dimension, player.posY, player.posY));
            } else {
                fall.lastY = player.posY;
            }
            return;
        }
        if (fall == null) {
            return;
        }
        if (fall.dimension != player.dimension || player.posY > fall.lastY + 1.0D) {
            FALLS.remove(id);
            return;
        }
        if (player.posY < fall.lastY) {
            fall.descending = true;
        }
        fall.lastY = player.posY;

        boolean landed = player.onGround || player.isInWater() || player.isInLava()
                || player.isRiding();
        if (!landed) {
            return;
        }
        FALLS.remove(id);
        if (fall.descending && player.isEntityAlive() && player.posY <= WORLD_BOTTOM
                && fall.startY - player.posY >= REQUIRED_FALL) {
            FFDAdvancements.FALL_FROM_WORLD_HEIGHT.trigger(player);
        }
    }

    private static void checkWorldHeightTrade(EntityPlayerMP player) {
        int trades = player.getStatFile().readStat(StatList.TRADED_WITH_VILLAGER);
        Integer previous = VILLAGER_TRADES.put(player.getUniqueID(), trades);
        if (previous != null && trades > previous && player.posY >= WORLD_TOP) {
            FFDAdvancements.TRADE_AT_WORLD_HEIGHT.trigger(player);
        }
    }

    private static void rememberTradeCount(EntityPlayerMP player) {
        VILLAGER_TRADES.put(player.getUniqueID(),
                player.getStatFile().readStat(StatList.TRADED_WITH_VILLAGER));
    }

    private static void satisfyDisabledCompatibilityCriteria(EntityPlayerMP player) {
        if (FFDConfig.hoglinMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_HOGLIN.trigger(player);
            grantDisabledMobCriterion(player, "hoglin");
        }
        if (FFDConfig.striderMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_STRIDER.trigger(player);
        }
        if (FFDConfig.piglinMode == FFDConfig.FeatureMode.DISABLED) {
            grantDisabledMobCriterion(player, "piglin");
        }
        if (FFDConfig.zoglinMode == FFDConfig.FeatureMode.DISABLED) {
            grantDisabledMobCriterion(player, "zoglin");
        }
        if (FFDConfig.catMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_CAT.trigger(player);
            FFDAdvancements.completeCatCatalogue(player);
        }
        if (FFDConfig.sweetBerryMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.CONSUME_SWEET_BERRIES.trigger(player);
        }
        if (FFDConfig.glowBerryMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.CONSUME_GLOW_BERRIES.trigger(player);
        }
        if (FFDConfig.honeyMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.CONSUME_HONEY_BOTTLE.trigger(player);
            FFDAdvancements.BRED_BEE.trigger(player);
        }
        if (FFDConfig.kelpMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.CONSUME_DRIED_KELP.trigger(player);
        }
        if (FFDConfig.turtleMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_TURTLE.trigger(player);
        }
        if (FFDConfig.foxMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_FOX.trigger(player);
        }
        if (FFDConfig.pandaMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_PANDA.trigger(player);
        }
        if (FFDConfig.pillagerMode == FFDConfig.FeatureMode.DISABLED) {
            grantDisabledMobCriterion(player, "pillager");
        }
        if (FFDConfig.ravagerMode == FFDConfig.FeatureMode.DISABLED) {
            grantDisabledMobCriterion(player, "ravager");
        }
        if (FFDConfig.goatMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_GOAT.trigger(player);
        }
        if (FFDConfig.axolotlMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.BRED_AXOLOTL.trigger(player);
        }
        if (FFDConfig.phantomMode == FFDConfig.FeatureMode.DISABLED) {
            FFDAdvancements.KILL_PHANTOM.trigger(player);
        }
    }

    private static void grantDisabledMobCriterion(EntityPlayerMP player, String criterion) {
        Advancement advancement = player.getServer().getAdvancementManager().getAdvancement(
                new ResourceLocation("minecraft", "adventure/kill_all_mobs"));
        if (advancement != null) {
            player.getAdvancements().grantCriterion(advancement, criterion);
        }
    }

    private static String itemPath(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return "";
        }
        return stack.getItem().getRegistryName().getResourcePath()
                .toLowerCase(Locale.ROOT).replace("_", "");
    }

    private static String entityPath(Entity entity) {
        if (entity == null) {
            return "";
        }
        ResourceLocation id = EntityList.getKey(entity);
        return id == null ? "" : id.getResourcePath().toLowerCase(Locale.ROOT);
    }

    private static final class FallState {
        private final int dimension;
        private final double startY;
        private double lastY;
        private boolean descending;

        private FallState(int dimension, double startY, double lastY) {
            this.dimension = dimension;
            this.startY = startY;
            this.lastY = lastY;
        }
    }
}
