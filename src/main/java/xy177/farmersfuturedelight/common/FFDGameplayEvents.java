package xy177.farmersfuturedelight.common;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockCake;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.items.ItemHandlerHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockAmethyst;
import xy177.farmersfuturedelight.common.block.BlockCaveVines;
import xy177.farmersfuturedelight.common.block.BlockCoralPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.block.CopperWeathering;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.block.BlockKelpHead;
import xy177.farmersfuturedelight.common.block.BlockNetherVine;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockUnderwaterPlant;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;
import xy177.farmersfuturedelight.common.entity.ai.EntityAITrampleTurtleEgg;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDCustomStrippedWoods;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.common.item.INetheriteItem;
import xy177.farmersfuturedelight.common.registry.FFDPotions;
import xy177.farmersfuturedelight.common.registry.FFDRawOres;
import xy177.farmersfuturedelight.common.registry.FFDRawOreDropHooks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDGameplayEvents {
    @SubscribeEvent
    public static void onAnvilContainerOpen(PlayerContainerEvent.Open event) {
        if (!event.getEntityPlayer().world.isRemote
                && event.getContainer() instanceof ContainerRepair
                && ((ContainerRepair) event.getContainer()).canInteractWith(event.getEntityPlayer())) {
            event.getEntityPlayer().addStat(FFDStats.ANVIL_INTERACTION);
        }
    }

    @SubscribeEvent
    public static void onNetheriteItemEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityItem)) {
            return;
        }
        ItemStack stack = ((EntityItem) event.getEntity()).getItem();
        if (!stack.isEmpty() && stack.getItem() instanceof INetheriteItem) {
            ObfuscationReflectionHelper.setPrivateValue(net.minecraft.entity.Entity.class,
                    event.getEntity(), true, "field_70178_ae", "isImmuneToFire");
        }
    }

    private static final ResourceLocation DEEPER_DEPTHS_COPPER_ORE =
            new ResourceLocation("deeperdepths", "copper_ore");

    private static final String TURTLE_ZOMBIE_AI_TAG = FarmerFutureDelight.MODID + ".turtleZombieAi";
    private static final String AXOLOTL_GUARDIAN_AI_TAG =
            FarmerFutureDelight.MODID + ".axolotlGuardianAi";
    private static final String DROWNED_SUBMERGED_TICKS_TAG =
            FarmerFutureDelight.MODID + ".drownedSubmergedTicks";
    private static final String DROWNED_CONVERSION_TICKS_TAG =
            FarmerFutureDelight.MODID + ".drownedConversionTicks";
    public static final String DROWNED_CONVERTING_TAG =
            FarmerFutureDelight.MODID + ".drownedConverting";
    private static final double SLOW_FALLING_GRAVITY_COMPENSATION = 0.08D - 0.01D;
    private static Biome.SpawnListEntry dripstoneDrownedSpawn;

    private FFDGameplayEvents() {
    }

    @SubscribeEvent
    public static void onWaterLightingLoad(ChunkDataEvent.Load event) {
        FFDHeightHooks.loadWaterLighting(event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onWaterLightingSave(ChunkDataEvent.Save event) {
        FFDHeightHooks.saveWaterLighting(event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onWaterLightingTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            FFDHeightHooks.syncWaterLighting(event.world);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) {
            return;
        }
        GameRules rules = event.getWorld().getGameRules();
        FFDGameplayHooks.ensureSleepingPercentageRule(rules);
        FFDGameplayHooks.ensurePortingGameRules(rules);
        FFDPortingGameRules.ensure(rules);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCreeperDropRecord(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        FFDGameplayHooks.dropCreeperDiscs(event);
    }

    @SubscribeEvent
    public static void onPotentialSpawns(WorldEvent.PotentialSpawns event) {
        if (event.getType() != EnumCreatureType.MONSTER
                || !FFDEntities.isLocalDrownedEnabled()
                || FFDConfig.drownedDripstoneCaveSpawnWeight <= 0
                || FFDVerticalBiomeManager.getBiome(event.getWorld(), event.getPos())
                != FFDVerticalBiome.DRIPSTONE_CAVES) {
            return;
        }
        java.util.Iterator<Biome.SpawnListEntry> iterator = event.getList().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().entityClass == EntityDrowned.class) {
                iterator.remove();
            }
        }
        event.getList().add(dripstoneDrownedSpawn());
    }

    private static synchronized Biome.SpawnListEntry dripstoneDrownedSpawn() {
        if (dripstoneDrownedSpawn == null
                || dripstoneDrownedSpawn.itemWeight != FFDConfig.drownedDripstoneCaveSpawnWeight) {
            dripstoneDrownedSpawn = new Biome.SpawnListEntry(EntityDrowned.class,
                    FFDConfig.drownedDripstoneCaveSpawnWeight, 4, 4);
        }
        return dripstoneDrownedSpawn;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onNaturalHostileSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!FFDConfig.hostileMobsRequireZeroBlockLight || event.isSpawner()
                || event.getWorld().isRemote
                || !event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false)) {
            return;
        }
        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(
                event.getX(), event.getY(), event.getZ());
        if (event.getWorld().getLightFor(EnumSkyBlock.BLOCK, pos) > 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIllagerTargetBabyVillager(LivingSetAttackTargetEvent event) {
        if (!FFDConfig.illagersIgnoreBabyVillagers
                || !(event.getEntityLiving() instanceof AbstractIllager)
                || !(event.getTarget() instanceof EntityVillager)
                || !((EntityVillager) event.getTarget()).isChild()) {
            return;
        }
        ((net.minecraft.entity.EntityLiving) event.getEntityLiving()).setAttackTarget(null);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIllagerDamageBabyVillager(LivingAttackEvent event) {
        if (!FFDConfig.illagersIgnoreBabyVillagers
                || !(event.getEntityLiving() instanceof EntityVillager)
                || !((EntityVillager) event.getEntityLiving()).isChild()) {
            return;
        }
        net.minecraft.entity.Entity attacker = event.getSource().getTrueSource();
        if (!(attacker instanceof AbstractIllager)) {
            attacker = event.getSource().getImmediateSource();
        }
        if (attacker instanceof AbstractIllager) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVanillaOreHarvested(HarvestDropsEvent event) {
        if (!FFDConfig.oresDropRawMaterials || event.isSilkTouching()
                || FFDBlockLootTables.hasLootTable(event.getWorld(), event.getState())) {
            return;
        }
        ItemStack ore = blockStack(event.getState());
        FFDCustomRawOres.Entry custom = FFDCustomRawOres.findSource(ore);
        if (custom != null) {
            replaceCustomRawOreDrop(event, ore, custom);
            return;
        }
        int index = rawOreIndex(ore);
        if (index < 0 || !FFDItems.isRawOreMaterialEnabled(FFDRawOres.NAMES[index])) {
            return;
        }
        String material = FFDRawOres.NAMES[index];
        ItemStack rawStack = FFDItems.effectiveStack(FFDItems.RAW_ORE_ITEMS[index]);
        if (rawStack.isEmpty() || containsRefinedDrop(event, material)
                || !removeOreDrops(event, ore, material)) {
            return;
        }
        int count = FFDRawOres.isCopper(material)
                ? 2 + event.getWorld().rand.nextInt(4) : FFDConfig.rawOreDropAmount;
        if (FFDConfig.denseRawOreDrop && isDenseOre(ore)) {
            count *= FFDRawOreDropHooks.denseOreMultiplier(ore);
        }

        if (event.getFortuneLevel() > 0) {
            int multiplier = event.getWorld().rand.nextInt(event.getFortuneLevel() + 2) - 1;
            count *= Math.max(0, multiplier) + 1;
        }
        rawStack.setCount(count);
        event.getDrops().add(rawStack);
    }

    private static void replaceCustomRawOreDrop(HarvestDropsEvent event, ItemStack ore,
                                                 FFDCustomRawOres.Entry entry) {
        ItemStack raw = entry.rawStack();
        if (raw.isEmpty()) {
            return;
        }
        for (ItemStack drop : event.getDrops()) {
            if (entry.matchesSmeltResult(drop)) {
                return;
            }
        }
        boolean removed = false;
        java.util.Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (ItemStack.areItemsEqual(drop, ore) || entry.matchesSource(drop)) {
                iterator.remove();
                removed = true;
            }
        }
        if (!removed) {
            return;
        }
        int count = FFDConfig.rawOreDropAmount;
        if (FFDConfig.denseRawOreDrop && isDenseOre(ore)) {
            count *= FFDRawOreDropHooks.denseOreMultiplier(ore);
        }
        if (event.getFortuneLevel() > 0) {
            int multiplier = event.getWorld().rand.nextInt(event.getFortuneLevel() + 2) - 1;
            count *= Math.max(0, multiplier) + 1;
        }
        raw.setCount(count);
        event.getDrops().add(raw);
    }

    private static ItemStack blockStack(IBlockState state) {
        if (state == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(Item.getItemFromBlock(state.getBlock()), 1,
                state.getBlock().getMetaFromState(state));
    }

    private static int rawOreIndex(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            String normalized = oreName.endsWith("Dense")
                    ? oreName.substring(0, oreName.length() - "Dense".length()) : oreName;
            for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
                for (String refined : FFDRawOres.refinedOreNames(FFDRawOres.NAMES[i])) {
                    String suffix = refined.startsWith("ingot")
                            ? refined.substring("ingot".length())
                            : FFDRawOres.capitalize(FFDRawOres.NAMES[i]);
                    if (("ore" + suffix).equals(normalized)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean containsRefinedDrop(HarvestDropsEvent event, String material) {
        for (ItemStack drop : event.getDrops()) {
            for (String refined : FFDRawOres.refinedOreNames(material)) {
                int target = OreDictionary.getOreID(refined);
                for (int oreId : OreDictionary.getOreIDs(drop)) {
                    if (oreId == target) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean removeOreDrops(HarvestDropsEvent event, ItemStack ore,
                                          String material) {
        boolean removed = false;
        java.util.Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();
            if (ItemStack.areItemsEqual(drop, ore) || isMaterialOre(drop, material)) {
                iterator.remove();
                removed = true;
            }
        }
        return removed;
    }

    private static boolean isMaterialOre(ItemStack stack, String material) {
        int index = rawOreIndex(stack);
        return index >= 0 && material.equals(FFDRawOres.NAMES[index]);
    }

    private static boolean isDenseOre(ItemStack stack) {
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).endsWith("Dense")) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preventUnusableCampfireIgnition(PlayerInteractEvent.RightClickBlock event) {
        Item item = event.getItemStack().getItem();
        if (event.getFace() == null || item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof xy177.farmersfuturedelight.common.block.BlockCampfire)
                || !state.getValue(xy177.farmersfuturedelight.common.block.BlockCampfire.LIT)
                && !xy177.farmersfuturedelight.common.block.BlockCampfire.hasContainedFluid(
                        event.getWorld(), event.getPos())) {
            return;
        }
        BlockPos adjacent = event.getPos().offset(event.getFace());
        if (event.getWorld().isAirBlock(adjacent)
                && Blocks.FIRE.canPlaceBlockAt(event.getWorld(), adjacent)) {
            return;
        }
        event.setCancellationResult(EnumActionResult.FAIL);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onShovelCreatesDirtPath(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDConfig.shovelCreatesDirtPath
                || !(event.getItemStack().getItem() instanceof ItemSpade)
                || event.getFace() == EnumFacing.DOWN
                || event.getWorld().getBlockState(event.getPos().up()).getMaterial() != Material.AIR) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (!isDirtPathConvertible(state)) {
            return;
        }
        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!event.getEntityPlayer().canPlayerEdit(event.getPos().offset(face), face,
                event.getItemStack())) {
            return;
        }

        event.getWorld().playSound(event.getEntityPlayer(), event.getPos(),
                SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), Blocks.GRASS_PATH.getDefaultState(), 11);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                event.getItemStack().damageItem(1, event.getEntityPlayer());
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExternalBambooPotted(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDItems.isBlockRegistered(FFDBlocks.POTTED_BAMBOO)
                || event.getItemStack().isEmpty() || event.getItemStack().getItem() == FFDItems.BAMBOO
                || event.getUseBlock() == Event.Result.DENY || event.getUseItem() == Event.Result.DENY
                || event.getWorld().getBlockState(event.getPos()).getBlock() != Blocks.FLOWER_POT) {
            return;
        }
        ItemStack bamboo = FFDItems.effectiveStack(FFDItems.BAMBOO);
        if (bamboo.isEmpty() || !ItemStack.areItemsEqual(bamboo, event.getItemStack())) {
            return;
        }
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!(tile instanceof net.minecraft.tileentity.TileEntityFlowerPot)
                || !((net.minecraft.tileentity.TileEntityFlowerPot) tile).getFlowerItemStack().isEmpty()
                || !event.getEntityPlayer().canPlayerEdit(event.getPos(), face, event.getItemStack())) {
            return;
        }
        if (!event.getWorld().isRemote) {
            FFDItems.BAMBOO.onItemUse(event.getEntityPlayer(), event.getWorld(), event.getPos(),
                    event.getHand(), face, 0.5F, 0.5F, 0.5F);
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVanillaRedstoneOreActivated(PlayerInteractEvent.RightClickBlock event) {
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() != Blocks.REDSTONE_ORE
                && state.getBlock() != Blocks.LIT_REDSTONE_ORE) {
            return;
        }
        ItemStack held = event.getItemStack();
        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (held.getItem() instanceof ItemBlock) {
            BlockPos target = state.getBlock().isReplaceable(event.getWorld(), event.getPos())
                    ? event.getPos() : event.getPos().offset(face);
            if (event.getWorld().getBlockState(target).getBlock()
                    .isReplaceable(event.getWorld(), target)) {
                return;
            }
        }
        state.getBlock().onBlockActivated(event.getWorld(), event.getPos(), state,
                event.getEntityPlayer(), event.getHand(), face, 0.5F, 0.5F, 0.5F);
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    private static boolean isDirtPathConvertible(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.MYCELIUM) {
            return true;
        }
        return block == Blocks.DIRT
                && (state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT
                || state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT
                || state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onShearsStopPlantGrowth(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDConfig.shearsStopPlantGrowth
                || !(event.getItemStack().getItem() instanceof ItemShears)
                || event.getWorld().isRemote) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        boolean changed;
        if (state.getBlock() instanceof BlockKelpHead) {
            changed = BlockKelpHead.stopGrowth(event.getWorld(), event.getPos(), state);
        } else if (state.getBlock() instanceof BlockCaveVines) {
            changed = BlockCaveVines.stopGrowth(event.getWorld(), event.getPos());
        } else if (state.getBlock() instanceof BlockNetherVine) {
            changed = BlockNetherVine.stopGrowth(event.getWorld(), event.getPos());
        } else {
            return;
        }
        if (!changed) {
            return;
        }

        event.getWorld().playSound(null, event.getPos(), SoundEvents.ENTITY_SHEEP_SHEAR,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            event.getItemStack().damageItem(1, event.getEntityPlayer());
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSignTextDyeOrGlow(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        boolean glowInk = FFDCompat.isCompatibleGlowInkSac(held);
        EnumDyeColor dyeColor = signDyeColor(held);
        boolean dye = dyeColor != null;
        boolean honeycomb = held.getItem() == FFDItems.HONEYCOMB;
        if ((!FFDItems.isSignTextEnabled() && !honeycomb)) {
            return;
        }
        if (!glowInk && !dye && !honeycomb) {
            return;
        }
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (!(tile instanceof TileEntitySign)) {
            return;
        }

        if (event.getWorld().isRemote) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);

        TileEntitySign sign = (TileEntitySign) tile;
        if (FFDSignText.isWaxed(sign)) {
            return;
        }
        boolean changed;
        SoundEvent sound;
        boolean back = FFDSignText.isBackSide(sign, event.getEntityPlayer());
        if (honeycomb) {
            changed = FFDSignText.setWaxed(sign, true);
            sound = FFDSounds.HONEYCOMB_WAX_ON;
        } else if (glowInk) {
            changed = FFDSignText.setGlowing(sign, true, back);
            sound = FFDSounds.GLOW_INK_SAC_USE;
        } else if (held.getItem() == Items.DYE
                && held.getMetadata() == EnumDyeColor.BLACK.getDyeDamage()
                && FFDSignText.isGlowing(sign, back)) {
            changed = FFDSignText.setGlowing(sign, false, back);
            sound = FFDSounds.INK_SAC_USE;
        } else {
            changed = FFDSignText.applyDye(sign,
                    dyeColor, back);
            sound = FFDSounds.DYE_USE;
        }
        if (!changed) {
            return;
        }

        event.getWorld().notifyBlockUpdate(event.getPos(),
                event.getWorld().getBlockState(event.getPos()),
                event.getWorld().getBlockState(event.getPos()), 3);
        event.getWorld().playSound(null, event.getPos(),
                sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            held.shrink(1);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventEditingWaxedSign(PlayerInteractEvent.RightClickBlock event) {
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile instanceof TileEntitySign && FFDSignText.isWaxed((TileEntitySign) tile)) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

    private static EnumDyeColor signDyeColor(ItemStack stack) {
        if (stack.getItem() == FFDItems.WHITE_DYE) return EnumDyeColor.WHITE;
        if (stack.getItem() == FFDItems.BLACK_DYE) return EnumDyeColor.BLACK;
        if (stack.getItem() == FFDItems.BLUE_DYE) return EnumDyeColor.BLUE;
        if (stack.getItem() == FFDItems.BROWN_DYE) return EnumDyeColor.BROWN;
        if (stack.getItem() == Items.DYE
                && stack.getMetadata() >= 0
                && stack.getMetadata() < EnumDyeColor.values().length) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(stack.getMetadata());
            if (FFDConfig.legacyDyeCompatibility
                    || color != EnumDyeColor.WHITE && color != EnumDyeColor.BLACK
                    && color != EnumDyeColor.BLUE && color != EnumDyeColor.BROWN) {
                return color;
            }
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLegacySeparatedDyeSheepInteraction(EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (!FFDConfig.legacyDyeCompatibility && held.getItem() == Items.DYE
                && (held.getMetadata() == EnumDyeColor.WHITE.getDyeDamage()
                || held.getMetadata() == EnumDyeColor.BLACK.getDyeDamage()
                || held.getMetadata() == EnumDyeColor.BLUE.getDyeDamage()
                || held.getMetadata() == EnumDyeColor.BROWN.getDyeDamage())
                && (event.getTarget() instanceof EntitySheep
                || event.getTarget() instanceof EntityWolf)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCandleAddedToCake(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDItems.isCandleEnabled()) {
            return;
        }
        int candleIndex = FFDItems.getCandleIndex(event.getItemStack());
        if (candleIndex < 0
                || !FFDItems.isBlockRegistered(FFDBlocks.CANDLE_CAKES[candleIndex])) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() != Blocks.CAKE || state.getValue(BlockCake.BITES) != 0) {
            return;
        }
        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!event.getEntityPlayer().canPlayerEdit(event.getPos(), face, event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        event.getWorld().playSound(event.getEntityPlayer(), event.getPos(),
                FFDSounds.CAKE_ADD_CANDLE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(),
                    FFDBlocks.CANDLE_CAKES[candleIndex].getDefaultState(), 11);
            event.getEntityPlayer().addStat(
                    StatList.getObjectUseStats(event.getItemStack().getItem()));
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                event.getItemStack().shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHoneycombWaxInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDItems.isCopperEnabled()
                || !FFDCompat.isCompatibleHoneycomb(event.getItemStack())) {
            return;
        }
        EnumActionResult result = CopperWeathering.tryWax(event.getEntityPlayer(),
                event.getWorld(), event.getPos(), event.getHand(), event.getFace());
        if (result != EnumActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onCopperAxeInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDItems.isCopperEnabled() || !(event.getItemStack().getItem() instanceof ItemAxe)) {
            return;
        }
        IBlockState oldState = event.getWorld().getBlockState(event.getPos());
        IBlockState changedState = CopperWeathering.getPrevious(oldState);
        net.minecraft.util.SoundEvent sound = FFDSounds.AXE_SCRAPE;
        if (changedState == null) {
            changedState = CopperWeathering.getUnwaxed(oldState);
            sound = FFDSounds.AXE_WAX_OFF;
        }
        if (changedState == null) {
            return;
        }

        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!event.getEntityPlayer().canPlayerEdit(event.getPos(), face, event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), changedState, 11);
            event.getWorld().playSound(null, event.getPos(), sound,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
            CopperWeathering.spawnBlockParticles(event.getWorld(), event.getPos(), oldState);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                event.getItemStack().damageItem(1, event.getEntityPlayer());
            }
            event.getEntityPlayer().addStat(StatList.getObjectUseStats(event.getItemStack().getItem()));
            if (sound == FFDSounds.AXE_WAX_OFF
                    && event.getEntityPlayer() instanceof net.minecraft.entity.player.EntityPlayerMP) {
                FFDAdvancements.WAX_OFF.trigger(
                        (net.minecraft.entity.player.EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (FFDItems.isBlackstoneEnabled()
                && event.getRight().getItem() == FFDItems.BLACKSTONE
                && isStoneTool(event.getLeft().getItem())) {
            ItemStack output = event.getLeft().copy();
            int repair = Math.max(1, output.getMaxDamage() / 4);
            output.setItemDamage(Math.max(0, output.getItemDamage() - repair));
            event.setOutput(output);
            event.setMaterialCost(1);
            event.setCost(1);
            return;
        }
        ItemStack membrane = FFDItems.effectiveStack(FFDItems.PHANTOM_MEMBRANE);
        if (!FFDItems.isPhantomEnabled() || event.getLeft().getItem() != Items.ELYTRA
                || membrane.isEmpty()
                || !ItemStack.areItemsEqual(event.getRight(), membrane)) {
            return;
        }

        ItemStack output = event.getLeft().copy();
        int repairPerMembrane = output.getMaxDamage() / 4;
        if (output.getItemDamage() <= 0 || repairPerMembrane <= 0) {
            return;
        }

        int membranesUsed = 0;
        while (output.getItemDamage() > 0 && membranesUsed < event.getRight().getCount()) {
            output.setItemDamage(Math.max(0, output.getItemDamage() - repairPerMembrane));
            membranesUsed++;
        }

        int workCost = membranesUsed;
        String repairedName = event.getName();
        if (repairedName == null || repairedName.trim().isEmpty()) {
            if (output.hasDisplayName()) {
                output.clearCustomName();
                workCost++;
            }
        } else if (!repairedName.equals(event.getLeft().getDisplayName())) {
            output.setStackDisplayName(repairedName);
            workCost++;
        }

        int repairCost = Math.max(event.getLeft().getRepairCost(),
                event.getRight().getRepairCost());
        output.setRepairCost(repairCost * 2 + 1);
        event.setMaterialCost(membranesUsed);
        event.setCost(event.getCost() + workCost);
        event.setOutput(output);
    }

    private static boolean isStoneTool(net.minecraft.item.Item item) {
        return item == Items.STONE_SWORD || item == Items.STONE_PICKAXE
                || item == Items.STONE_AXE || item == Items.STONE_SHOVEL
                || item == Items.STONE_HOE;
    }

    @SubscribeEvent
    public static void onSlowFallingLivingUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!hasSlowFallingForUpcomingTravel(entity)) {
            return;
        }

        entity.fallDistance = 0.0F;
        if (entity.hasNoGravity() || entity.isPotionActive(MobEffects.LEVITATION)) {
            return;
        }

        if (!entity.isElytraFlying()) {
            if (!entity.isRiding() && !entity.isInWater() && !entity.isInLava()
                    && (!(entity instanceof EntityPlayer)
                            || !((EntityPlayer) entity).capabilities.isFlying)
                    && entity.motionY <= 1.0E-7D) {
                entity.motionY += SLOW_FALLING_GRAVITY_COMPENSATION;
            }
            return;
        }

        if (entity.motionY > 0.0D) {
            return;
        }

        Vec3d look = entity.getLookVec();
        float pitch = entity.rotationPitch * 0.017453292F;
        double lift = Math.cos(pitch);
        lift = lift * lift * Math.min(1.0D, look.lengthVector() / 0.4D);
        entity.motionY += 0.07D - 0.0525D * lift;
    }

    @SubscribeEvent
    public static void onZombieDrownedConversion(LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote) {
            return;
        }
        EntityLivingBase source = event.getEntityLiving();
        if (!source.isEntityAlive()
                || source instanceof net.minecraft.entity.EntityLiving
                && ((net.minecraft.entity.EntityLiving) source).isAIDisabled()) {
            return;
        }
        ResourceLocation targetId = FFDConfig.getDrownedTransformationTarget(source);
        if (targetId == null || FFDEntities.DROWNED_ID.equals(targetId)
                && !FFDEntities.isLocalDrownedEnabled()) {
            syncDrownedConverting(source, false);
            return;
        }
        net.minecraft.nbt.NBTTagCompound data = source.getEntityData();
        if (!source.isInsideOfMaterial(Material.WATER)) {
            data.setInteger(DROWNED_SUBMERGED_TICKS_TAG, -1);
            data.removeTag(DROWNED_CONVERSION_TICKS_TAG);
            syncDrownedConverting(source, false);
            return;
        }
        if (data.hasKey(DROWNED_CONVERSION_TICKS_TAG, 3)) {
            syncDrownedConverting(source, true);
            int conversionTicks = data.getInteger(DROWNED_CONVERSION_TICKS_TAG) - 1;
            if (conversionTicks < 0) {
                if (EntityDrowned.convertFrom(source, targetId)) {
                    if (!source.isSilent()) {
                        source.world.playSound(null, new BlockPos(source),
                                FFDSounds.ZOMBIE_CONVERTED_TO_DROWNED,
                                SoundCategory.HOSTILE, 1.0F, 1.0F);
                    }
                } else {
                    data.setInteger(DROWNED_CONVERSION_TICKS_TAG, conversionTicks);
                }
            } else {
                data.setInteger(DROWNED_CONVERSION_TICKS_TAG, conversionTicks);
            }
            return;
        }
        int submergedTicks = data.getInteger(DROWNED_SUBMERGED_TICKS_TAG) + 1;
        data.setInteger(DROWNED_SUBMERGED_TICKS_TAG, submergedTicks);
        if (submergedTicks >= 600) {
            data.setInteger(DROWNED_CONVERSION_TICKS_TAG, 300);
            syncDrownedConverting(source, true);
        } else {
            syncDrownedConverting(source, false);
        }
    }

    private static void syncDrownedConverting(EntityLivingBase entity, boolean converting) {
        if (entity.getEntityData().getBoolean(DROWNED_CONVERTING_TAG) != converting) {
            entity.getEntityData().setBoolean(DROWNED_CONVERTING_TAG, converting);
            xy177.farmersfuturedelight.common.network.FFDNetwork
                    .sendDrownedConverting(entity, converting);
        }
    }

    @SubscribeEvent
    public static void onTrackDrownedConversion(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof net.minecraft.entity.player.EntityPlayerMP
                && event.getTarget() instanceof EntityLivingBase) {
            xy177.farmersfuturedelight.common.network.FFDNetwork.sendDrownedConverting(
                    (net.minecraft.entity.player.EntityPlayerMP) event.getEntityPlayer(),
                    (EntityLivingBase) event.getTarget(),
                    event.getTarget().getEntityData().getBoolean(DROWNED_CONVERTING_TAG));
        }
    }

    @SubscribeEvent
    public static void onConduitPowerBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        int conduitAmplifier = FFDPotions.conduitPowerAmplifier(player);
        if (conduitAmplifier < 0) {
            return;
        }
        int hasteAmplifier = player.isPotionActive(MobEffects.HASTE)
                ? player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() : -1;
        if (conduitAmplifier <= hasteAmplifier) {
            return;
        }
        float currentMultiplier = hasteAmplifier < 0 ? 1.0F
                : 1.0F + (hasteAmplifier + 1) * 0.2F;
        float targetMultiplier = 1.0F + (conduitAmplifier + 1) * 0.2F;
        event.setNewSpeed(event.getNewSpeed() * targetMultiplier / currentMultiplier);
    }

    private static boolean hasSlowFalling(EntityLivingBase entity) {
        return FFDItems.isPhantomEnabled() && entity.isPotionActive(FFDPotions.SLOW_FALLING);
    }

    private static boolean hasSlowFallingForUpcomingTravel(EntityLivingBase entity) {
        return hasSlowFalling(entity)
                && entity.getActivePotionEffect(FFDPotions.SLOW_FALLING).getDuration() > 1;
    }

    @SubscribeEvent
    public static void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
        if (event.getSound() != SoundEvents.ENTITY_GENERIC_DRINK
                || !(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }
        ItemStack activeStack = ((EntityLivingBase) event.getEntity()).getActiveItemStack();
        if (!activeStack.isEmpty() && activeStack.getItem() == FFDItems.HONEY_BOTTLE) {
            event.setSound(FFDSounds.HONEY_BOTTLE_DRINK);
        }
    }

    @SubscribeEvent
    public static void onFillBucket(FillBucketEvent event) {
        RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        net.minecraft.util.math.BlockPos pos = target.getBlockPos();
        IBlockState state = event.getWorld().getBlockState(pos);
        if (state.getBlock() instanceof BlockUnderwaterPlant) {
            event.setCanceled(true);
            return;
        }

        IBlockState dryState = withWaterlogged(state, false);
        boolean stored = xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates
                .has(event.getWorld(), pos);
        if (stored && dryState == null) dryState = state;
        Fluid fluid = WaterloggedBlockApi.getContainedFluid(event.getWorld(), pos);
        if (dryState == null || fluid == null
                || WaterloggedBlockApi.getSourceFluid(event.getWorld(), pos) == null) {
            return;
        }
        FluidActionResult filled = FluidUtil.tryFillContainer(event.getEmptyBucket(),
                new net.minecraftforge.fluids.FluidTank(
                        new FluidStack(fluid, Fluid.BUCKET_VOLUME), Fluid.BUCKET_VOLUME),
                Fluid.BUCKET_VOLUME, null, true);
        if (!filled.isSuccess()) {
            return;
        }
        net.minecraft.entity.player.EntityPlayer player = event.getEntityPlayer();
        EnumFacing side = target.sideHit == null ? EnumFacing.UP : target.sideHit;
        if (!event.getWorld().isBlockModifiable(player, pos)
                || !player.canPlayerEdit(pos.offset(side), side, event.getEmptyBucket())) {
            event.setCanceled(true);
            return;
        }

        event.getWorld().setBlockState(pos, dryState, 11);
        WaterloggedBlockApi.recordContainedFluid(event.getWorld(), pos, null);
        if (dryState.getBlock() instanceof BlockCoralPlant) {
            ((BlockCoralPlant) dryState.getBlock()).scheduleDeath(event.getWorld(), pos);
        } else if (dryState.getBlock() instanceof BlockCoralWallFan) {
            ((BlockCoralWallFan) dryState.getBlock()).scheduleDeath(event.getWorld(), pos);
        }
        player.addStat(StatList.getObjectUseStats(Items.BUCKET));
        player.playSound(fluid.getFillSound(new FluidStack(fluid, Fluid.BUCKET_VOLUME)),
                1.0F, 1.0F);
        event.setFilledBucket(filled.getResult());
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onWaterBucketUse(PlayerInteractEvent.RightClickBlock event) {
        FluidStack contained = FluidUtil.getFluidContained(event.getItemStack());
        if (contained == null || contained.amount < Fluid.BUCKET_VOLUME
                || !FFDConfig.isFluidAllowed(event.getWorld(), contained.getFluid())) {
            return;
        }
        BlockPos clickedPos = event.getPos();
        EnumFacing side = event.getFace() == null ? EnumFacing.UP : event.getFace();
        IBlockState state = event.getWorld().getBlockState(clickedPos);
        BlockPos targetPos = clickedPos;
        IBlockState wetState = withWaterlogged(state, true);
        if (wetState == null) {
            if (tryExtraFluidBucket(event, clickedPos, contained, side)) return;
            boolean replaceable = state.getBlock().isReplaceable(event.getWorld(), clickedPos);
            targetPos = replaceable && side == EnumFacing.UP
                    ? clickedPos : clickedPos.offset(side);
            wetState = withWaterlogged(event.getWorld().getBlockState(targetPos), true);
            if (wetState == null && tryExtraFluidBucket(event, targetPos, contained, side)) return;
        }
        if (wetState == null) {
            return;
        }

        ItemStack drainedContainer = null;
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            drainedContainer = drainFluidContainer(event.getItemStack(), contained);
            if (drainedContainer == null) {
                return;
            }
        }

        if (!event.getWorld().isBlockModifiable(event.getEntityPlayer(), targetPos)
                || !event.getEntityPlayer().canPlayerEdit(targetPos, side,
                        event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (event.getWorld().isRemote) {
            return;
        }
        if (!event.getWorld().setBlockState(targetPos, wetState, 11)) {
            return;
        }
        Fluid fluid = contained.getFluid();
        WaterloggedBlockApi.recordContainedFluid(event.getWorld(), targetPos, fluid);
        event.getWorld().scheduleUpdate(targetPos, wetState.getBlock(),
                WaterloggedBlockApi.fluidTickRate(event.getWorld(), fluid));
        event.getEntityPlayer().addStat(StatList.getObjectUseStats(event.getItemStack().getItem()));
        consumeFluidContainer(event, drainedContainer);
        event.getWorld().playSound(null, targetPos, fluid.getEmptySound(contained),
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFluidloggedBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || !WaterloggedBlockApi.isWaterlogged(event.getState())) {
            return;
        }
        IBlockState replaced = event.getBlockSnapshot().getReplacedBlock();
        Fluid fluid = WaterloggedBlockApi.getFluidForBlock(replaced.getBlock());
        if (fluid != null && FFDConfig.isFluidAllowed(fluid)
                && xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid
                        .isSourceFluid(replaced)) {
            WaterloggedBlockApi.recordContainedFluid(event.getWorld(), event.getPos(), fluid);
        }
    }

    @SubscribeEvent
    public static void onModernFuel(net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent event) {
        if (event.getBurnTime() >= 0) {
            return;
        }
        Item item = event.getItemStack().getItem();
        if (item == Items.BOAT || item == Items.SPRUCE_BOAT || item == Items.BIRCH_BOAT
                || item == Items.JUNGLE_BOAT || item == Items.ACACIA_BOAT
                || item == Items.DARK_OAK_BOAT) {
            event.setBurnTime(1200);
        } else if (item == Item.getItemFromBlock(Blocks.DEADBUSH)) {
            event.setBurnTime(100);
        }
    }

    @SubscribeEvent
    public static void onVanillaLeavesHarvested(HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        if (event.getWorld().isRemote || (block != Blocks.LEAVES && block != Blocks.LEAVES2)
                || event.isSilkTouching()
                || FFDBlockLootTables.hasLootTable(event.getWorld(), event.getState())) {
            return;
        }
        EntityPlayer player = event.getHarvester();
        if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemShears) {
            return;
        }
        float[] chances = {0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
        int fortune = Math.max(0, Math.min(event.getFortuneLevel(), chances.length - 1));
        if (event.getWorld().rand.nextFloat() < chances[fortune]) {
            event.getDrops().add(new ItemStack(Items.STICK, 1 + event.getWorld().rand.nextInt(2)));
        }
    }

    private static boolean tryExtraFluidBucket(PlayerInteractEvent.RightClickBlock event,
            BlockPos pos, FluidStack fluid, EnumFacing side) {
        IBlockState state = event.getWorld().getBlockState(pos);
        if (!FFDConfig.isAdditionalWaterloggingBlock(event.getWorld(), state.getBlock())
                || WaterloggedBlockApi.isWaterlogged(event.getWorld(), pos)) return false;
        if (!event.getWorld().isBlockModifiable(event.getEntityPlayer(), pos)
                || !event.getEntityPlayer().canPlayerEdit(pos, side, event.getItemStack())) return false;
        ItemStack emptied = null;
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            emptied = drainFluidContainer(event.getItemStack(), fluid);
            if (emptied == null) return false;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.set(
                    event.getWorld(), pos, WaterloggedBlockApi.sourceState(fluid.getFluid()));
            event.getWorld().scheduleUpdate(pos, state.getBlock(),
                    WaterloggedBlockApi.fluidTickRate(event.getWorld(), fluid.getFluid()));
            event.getWorld().notifyNeighborsOfStateChange(pos, state.getBlock(), false);
            consumeFluidContainer(event, emptied);
            event.getWorld().playSound(null, pos, fluid.getFluid().getEmptySound(fluid),
                    SoundCategory.BLOCKS, 1, 1);
        }
        return true;
    }

    @Nullable
    private static ItemStack drainFluidContainer(ItemStack stack, FluidStack fluid) {
        ItemStack single = stack.copy();
        single.setCount(1);
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(single);
        if (handler == null) {
            return null;
        }
        FluidStack requested = new FluidStack(fluid.getFluid(), Fluid.BUCKET_VOLUME);
        FluidStack simulated = handler.drain(requested, false);
        if (simulated == null || simulated.amount != Fluid.BUCKET_VOLUME
                || !simulated.isFluidEqual(requested)) {
            return null;
        }
        FluidStack drained = handler.drain(requested, true);
        return drained != null && drained.amount == Fluid.BUCKET_VOLUME
                && drained.isFluidEqual(requested) ? handler.getContainer() : null;
    }

    private static void consumeFluidContainer(PlayerInteractEvent.RightClickBlock event,
                                               @Nullable ItemStack result) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.capabilities.isCreativeMode) {
            return;
        }
        ItemStack held = event.getItemStack();
        held.shrink(1);
        if (held.isEmpty()) {
            player.setHeldItem(event.getHand(), result == null ? ItemStack.EMPTY : result);
        } else if (result != null && !result.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, result);
        }
    }

    private static IBlockState withWaterlogged(IBlockState state, boolean waterlogged) {
        IBlockState result = WaterloggedBlockApi.withWaterlogged(state, waterlogged);
        return result == null || WaterloggedBlockApi.isWaterlogged(state) == waterlogged
                ? null : result;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChorusFlowerProjectileImpact(ProjectileImpactEvent event) {
        if (!event.isCanceled()) {
            FFDGameplayHooks.breakChorusFlowerOnProjectile(event.getEntity(), event.getRayTraceResult());
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.isCanceled() || event.getEntity().world.isRemote) {
            return;
        }
        if (FFDItems.isAxolotlEnabled() && event.getEntity() instanceof EntityPotion) {
            EntityPotion potion = (EntityPotion) event.getEntity();
            ItemStack stack = potion.getPotion();
            if (PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER
                    && PotionUtils.getEffectsFromStack(stack).isEmpty()) {
                AxisAlignedBB area = potion.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
                for (EntityAxolotl axolotl : potion.world.getEntitiesWithinAABB(
                        EntityAxolotl.class, area)) {
                    if (potion.getDistanceSq(axolotl) < 16.0D) {
                        axolotl.rehydrate();
                    }
                }
            }
        }
        RayTraceResult hit = event.getRayTraceResult();
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        IBlockState state = event.getEntity().world.getBlockState(hit.getBlockPos());
        if (state.getBlock() instanceof xy177.farmersfuturedelight.common.block.BlockCampfire
                && event.getEntity().isBurning()
                && !state.getValue(xy177.farmersfuturedelight.common.block.BlockCampfire.LIT)
                && !xy177.farmersfuturedelight.common.block.BlockCampfire.hasContainedFluid(
                        event.getEntity().world, hit.getBlockPos())) {
            Entity projectile = event.getEntity();
            Entity owner = projectile instanceof net.minecraft.entity.projectile.EntityArrow
                    ? ((net.minecraft.entity.projectile.EntityArrow) projectile).shootingEntity
                    : projectile instanceof net.minecraft.entity.projectile.EntityThrowable
                    ? ((net.minecraft.entity.projectile.EntityThrowable) projectile).getThrower()
                    : projectile instanceof net.minecraft.entity.projectile.EntityFireball
                    ? ((net.minecraft.entity.projectile.EntityFireball) projectile).shootingEntity : null;
            boolean mayInteract = owner instanceof EntityPlayer
                    ? ((EntityPlayer) owner).capabilities.allowEdit
                    && projectile.world.isBlockModifiable((EntityPlayer) owner, hit.getBlockPos())
                    : owner == null || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(
                            projectile.world, owner);
            if (mayInteract) {
                projectile.world.setBlockState(hit.getBlockPos(), state.withProperty(
                        xy177.farmersfuturedelight.common.block.BlockCampfire.LIT, true), 11);
            }
        }
        if (state.getBlock() instanceof xy177.farmersfuturedelight.common.block.BlockBell) {
            ((xy177.farmersfuturedelight.common.block.BlockBell) state.getBlock()).onHit(
                    event.getEntity().world, hit.getBlockPos(), state, hit.sideHit,
                    hit.hitVec.y - hit.getBlockPos().getY());
        }
        if (BlockBigDripleaf.isBigDripleaf(state)) {
            BlockBigDripleaf.tiltFully(event.getEntity().world, hit.getBlockPos(), state);
        }
        if (FFDItems.isAmethystEnabled() && state.getBlock() instanceof BlockAmethyst) {
            event.getEntity().world.playSound(null, hit.getBlockPos(), FFDSounds.AMETHYST_BLOCK_CHIME,
                    SoundCategory.BLOCKS, 1.0F, 0.5F + event.getEntity().world.rand.nextFloat() * 1.2F);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        net.minecraft.entity.EntityLivingBase entity = event.getEntityLiving();
        net.minecraft.util.math.BlockPos below = new net.minecraft.util.math.BlockPos(
                entity.posX, entity.getEntityBoundingBox().minY - 0.01D, entity.posZ);
        if (entity.world.getBlockState(below).getBlock() == FFDBlocks.HONEY_BLOCK) {
            entity.motionY *= 0.5D;
        }
    }

    @SubscribeEvent
    public static void onLogStripped(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof ItemAxe)
                || event.getFace() == null
                || !event.getEntityPlayer().canPlayerEdit(event.getPos(), event.getFace(),
                        event.getItemStack())
                || !event.getWorld().isBlockModifiable(event.getEntityPlayer(), event.getPos())) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        IBlockState strippedState = strippedVariant(state);
        if (strippedState == null) {
            return;
        }
        if (event.getWorld().isRemote) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            return;
        }
        if (!event.getWorld().setBlockState(event.getPos(), strippedState, 11)) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            held.damageItem(1, event.getEntityPlayer());
        }
        event.getWorld().playSound(null, event.getPos(), FFDSounds.AXE_STRIP,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRootedDirtTilled(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote || !(event.getItemStack().getItem() instanceof ItemHoe)
                || event.getWorld().getBlockState(event.getPos()).getBlock() != FFDBlocks.ROOTED_DIRT
                || event.getFace() == EnumFacing.DOWN
                || !event.getWorld().isAirBlock(event.getPos().up())) {
            return;
        }
        event.getWorld().setBlockState(event.getPos(), Blocks.DIRT.getDefaultState(), 11);
        ItemStack roots = FFDItems.effectiveStack(FFDItems.HANGING_ROOTS);
        if (!roots.isEmpty()) {
            Block.spawnAsEntity(event.getWorld(), event.getPos().offset(event.getFace()),
                    roots);
        }
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            event.getItemStack().damageItem(1, event.getEntityPlayer());
        }
        event.getWorld().playSound(null, event.getPos(), SoundEvents.ITEM_HOE_TILL,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    private static IBlockState strippedVariant(IBlockState state) {
        Block block = state.getBlock();
        FFDCustomStrippedWoods.Entry customEntry = FFDCustomStrippedWoods.findSource(state);
        IBlockState custom = customEntry == null ? null : customEntry.strippedState(state);
        if (custom != null) {
            return custom;
        }
        if (block == FFDBlocks.CRIMSON_STEM) {
            return strippedState(FFDItems.effectiveBlock(FFDBlocks.STRIPPED_CRIMSON_STEM),
                    state.getValue(BlockRotatedPillar.AXIS));
        }
        if (block == FFDBlocks.CRIMSON_HYPHAE) {
            return strippedState(FFDItems.effectiveBlock(FFDBlocks.STRIPPED_CRIMSON_HYPHAE),
                    state.getValue(BlockRotatedPillar.AXIS));
        }
        if (block == FFDBlocks.WARPED_STEM) {
            return strippedState(FFDItems.effectiveBlock(FFDBlocks.STRIPPED_WARPED_STEM),
                    state.getValue(BlockRotatedPillar.AXIS));
        }
        if (block == FFDBlocks.WARPED_HYPHAE) {
            return strippedState(FFDItems.effectiveBlock(FFDBlocks.STRIPPED_WARPED_HYPHAE),
                    state.getValue(BlockRotatedPillar.AXIS));
        }
        if (!FFDItems.isStrippedWoodEnabled()) {
            return null;
        }
        for (int index = 0; index < FFDBlocks.WOODS.length; index++) {
            if (block == FFDBlocks.WOODS[index]) {
                return strippedState(FFDItems.effectiveBlock(FFDBlocks.STRIPPED_WOODS[index]),
                        state.getValue(BlockRotatedPillar.AXIS));
            }
        }
        if (block != Blocks.LOG && block != Blocks.LOG2) {
            return null;
        }
        int metadata = block.getMetaFromState(state);
        int index = block == Blocks.LOG ? metadata & 3 : 4 + (metadata & 1);
        net.minecraft.block.BlockLog.EnumAxis oldAxis =
                state.getValue(net.minecraft.block.BlockLog.LOG_AXIS);
        Block target = FFDItems.effectiveBlock(oldAxis == net.minecraft.block.BlockLog.EnumAxis.NONE
                ? FFDBlocks.STRIPPED_WOODS[index] : FFDBlocks.STRIPPED_LOGS[index]);
        EnumFacing.Axis axis = oldAxis == net.minecraft.block.BlockLog.EnumAxis.X
                ? EnumFacing.Axis.X : oldAxis == net.minecraft.block.BlockLog.EnumAxis.Z
                ? EnumFacing.Axis.Z : EnumFacing.Axis.Y;
        return strippedState(target, axis);
    }

    private static IBlockState strippedState(Block block, EnumFacing.Axis axis) {
        if (block == null) {
            return null;
        }
        IBlockState state = block.getDefaultState();
        if (state.getPropertyKeys().contains(BlockRotatedPillar.AXIS)) {
            return state.withProperty(BlockRotatedPillar.AXIS, axis);
        }
        if (state.getPropertyKeys().contains(net.minecraft.block.BlockLog.LOG_AXIS)) {
            net.minecraft.block.BlockLog.EnumAxis oldAxis = axis == EnumFacing.Axis.X
                    ? net.minecraft.block.BlockLog.EnumAxis.X : axis == EnumFacing.Axis.Z
                    ? net.minecraft.block.BlockLog.EnumAxis.Z
                    : net.minecraft.block.BlockLog.EnumAxis.Y;
            return state.withProperty(net.minecraft.block.BlockLog.LOG_AXIS, oldAxis);
        }
        return state;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        if (xy177.farmersfuturedelight.common.registry.FFDEntities.isLocalCatEnabled()
                && event.getEntity().getClass()
                == net.minecraft.entity.passive.EntityOcelot.class
                && ((net.minecraft.entity.passive.EntityOcelot) event.getEntity()).isTamed()) {
            net.minecraft.nbt.NBTTagCompound legacyData = new net.minecraft.nbt.NBTTagCompound();
            event.getEntity().writeToNBT(legacyData);
            xy177.farmersfuturedelight.common.entity.EntityCat cat =
                    new xy177.farmersfuturedelight.common.entity.EntityCat(event.getWorld());
            cat.readFromNBT(legacyData);
            event.setCanceled(true);
            event.getWorld().spawnEntity(cat);
            return;
        }

        if (xy177.farmersfuturedelight.common.registry.FFDEntities.isLocalCatEnabled()
                && event.getEntity().getClass()
                == net.minecraft.entity.passive.EntityOcelot.class) {
            net.minecraft.entity.passive.EntityOcelot ocelot =
                    (net.minecraft.entity.passive.EntityOcelot) event.getEntity();
            java.util.List<net.minecraft.entity.ai.EntityAIBase> vanillaAvoidTasks =
                    new java.util.ArrayList<>();
            for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry task
                    : ocelot.tasks.taskEntries) {
                if (task.priority == 4
                        && task.action.getClass()
                        == net.minecraft.entity.ai.EntityAIAvoidEntity.class) {
                    vanillaAvoidTasks.add(task.action);
                }
            }
            for (net.minecraft.entity.ai.EntityAIBase task : vanillaAvoidTasks) {
                ocelot.tasks.removeTask(task);
            }
            boolean hasTrustAvoidTask = false;
            for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry task
                    : ocelot.tasks.taskEntries) {
                if (task.action instanceof xy177.farmersfuturedelight.common.entity.ai
                        .EntityAIOcelotAvoidUntrustedPlayer) {
                    hasTrustAvoidTask = true;
                    break;
                }
            }
            if (!hasTrustAvoidTask) {
                ocelot.tasks.addTask(4, new xy177.farmersfuturedelight.common.entity.ai
                        .EntityAIOcelotAvoidUntrustedPlayer(ocelot));
            }
        }

        if (FFDItems.isTurtleEnabled() && event.getEntity() instanceof EntityCreature
                && !(event.getEntity() instanceof EntityPigZombie)
                && (FFDConfig.isConfiguredEntity(FFDConfig.turtleEggDestroyingMobs,
                        event.getEntity())
                || FFDConfig.isConfiguredEntity(FFDConfig.turtleBabyPredatorMobs,
                        event.getEntity()))) {
            addTurtleTargeting((EntityCreature) event.getEntity());
        }
        if (FFDEntities.isLocalAxolotlEnabled()
                && event.getEntity() instanceof EntityGuardian) {
            addAxolotlTargeting((EntityGuardian) event.getEntity());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onOcelotTrustInteraction(EntityInteract event) {
        if (event.isCanceled()
                || !xy177.farmersfuturedelight.common.registry.FFDEntities.isLocalCatEnabled()
                || event.getTarget().getClass()
                != net.minecraft.entity.passive.EntityOcelot.class
                || event.getItemStack().getItem() != Items.FISH
                || event.getEntityPlayer().getDistanceSq(event.getTarget()) > 9.0D) {
            return;
        }
        net.minecraft.entity.passive.EntityOcelot ocelot =
                (net.minecraft.entity.passive.EntityOcelot) event.getTarget();
        boolean temptTaskRunning = false;
        for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry task
                : ocelot.tasks.taskEntries) {
            if (task.action instanceof net.minecraft.entity.ai.EntityAITempt
                    && ((net.minecraft.entity.ai.EntityAITempt) task.action).isRunning()) {
                temptTaskRunning = true;
                break;
            }
        }
        if (!temptTaskRunning) {
            return;
        }
        ItemStack fish = event.getItemStack();
        boolean rawCodOrSalmon = fish.getMetadata() == 0 || fish.getMetadata() == 1;
        event.setCanceled(true);
        event.setCancellationResult(rawCodOrSalmon
                ? EnumActionResult.SUCCESS : EnumActionResult.FAIL);
        if (event.getWorld().isRemote || !rawCodOrSalmon
                || xy177.farmersfuturedelight.common.entity.FFDOcelotTrust
                .isTrusting(ocelot)) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        if (!player.capabilities.isCreativeMode) {
            fish.shrink(1);
        }
        boolean trusted = ocelot.getRNG().nextInt(3) == 0
                ;
        if (trusted) {
            xy177.farmersfuturedelight.common.entity.FFDOcelotTrust.setTrusting(ocelot, true);
        }
        event.getWorld().setEntityState(ocelot, (byte) (trusted ? 7 : 6));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIronGolemRepair(EntityInteract event) {
        if (event.isCanceled()
                || !(event.getTarget() instanceof EntityIronGolem)
                || event.getItemStack().getItem() != Items.IRON_INGOT) {
            return;
        }
        EntityIronGolem golem = (EntityIronGolem) event.getTarget();
        if (golem.getHealth() >= golem.getMaxHealth()) return;
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.getEntityPlayer().swingArm(event.getHand());
        if (event.getWorld().isRemote) return;
        golem.heal(25.0F);
        golem.playSound(FFDSounds.IRON_GOLEM_REPAIR, 1.0F,
                1.0F + (golem.getRNG().nextFloat() - golem.getRNG().nextFloat()) * 0.2F);
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            event.getItemStack().shrink(1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onIronGolemDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (event.isCanceled() || event.getEntityLiving().world.isRemote
                || !(event.getEntityLiving() instanceof EntityIronGolem) || event.getAmount() <= 0.0F) return;
        EntityIronGolem golem = (EntityIronGolem) event.getEntityLiving();
        if (golemCrackStage(golem.getHealth(), golem.getMaxHealth())
                != golemCrackStage(Math.max(0.0F, golem.getHealth() - event.getAmount()),
                        golem.getMaxHealth())) {
            golem.playSound(FFDSounds.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
        }
    }

    private static int golemCrackStage(float health, float maximum) {
        float ratio = health / maximum;
        return ratio < 0.25F ? 3 : ratio < 0.5F ? 2 : ratio < 0.75F ? 1 : 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVillagerTradeListCreate(EntityInteract event) {
        if (!FFDConfig.modernVillagerSystem || event.isCanceled() || event.getWorld().isRemote
                || event.getEntityPlayer().isSneaking()
                || !(event.getTarget() instanceof EntityVillager)) {
            return;
        }
        EntityVillager villager = (EntityVillager) event.getTarget();
        ItemStack interactionItem = event.getEntityPlayer().getHeldItem(event.getHand());
        if (!villager.isEntityAlive() || villager.isTrading()
                || xy177.farmersfuturedelight.common.registry.FFDVillagerHomes.isSleeping(villager)
                || interactionItem.getItem() == Items.SPAWN_EGG || interactionItem.getItem() == Items.NAME_TAG) return;
        if (villager.isChild()) {
            xy177.farmersfuturedelight.common.registry.FFDVillagerTypes.refuseTrade(villager);
            event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        xy177.farmersfuturedelight.common.registry.FFDWorkstationLifecycle.validateWorkstation(villager);
        xy177.farmersfuturedelight.common.registry.FFDModernVillagerTrades.refreshUntradedProfile(villager);
        if (xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
            if (event.getHand() == net.minecraft.util.EnumHand.MAIN_HAND) {
                xy177.farmersfuturedelight.common.registry.FFDVillagerTypes.refuseTrade(villager);
            }
            event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        if (villager.getEntityData().getBoolean("farmers_future_delight:has_traded")) return;
        String appliedTag = xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration.getAppliedTag();
        if (villager.getEntityData().getBoolean(appliedTag)
                || !xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration.hasProfile(villager)) {
            return;
        }
        Object currentTrades = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager,
                "field_70963_i");
        boolean newTrades = currentTrades == null;
        if (!newTrades && villager.getEntityData().getBoolean(
                "farmers_future_delight:legacy_trade_history_unknown")) return;
        if (!newTrades
                && !xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration
                .shouldUpdateExistingUntradedVillagers()) {
            return;
        }
        net.minecraft.village.MerchantRecipeList recipes = villager.getRecipes(event.getEntityPlayer());
        if (recipes == null) return;
        if (villager.getEntityData().getBoolean(appliedTag)) return;
        if (!newTrades) {
            for (net.minecraft.village.MerchantRecipe recipe : recipes) {
                if (recipe.getToolUses() > 0) return;
            }
        }
        if (xy177.farmersfuturedelight.common.registry.FFDTradeConfiguration.apply(villager, recipes)) {
            villager.getEntityData().setBoolean(appliedTag, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerEmptyTradeList(EntityInteract event) {
        if (!FFDConfig.modernVillagerSystem || event.isCanceled() || event.getWorld().isRemote
                || event.getEntityPlayer().isSneaking()
                || event.getTarget().getClass() != EntityVillager.class) return;
        EntityVillager villager = (EntityVillager) event.getTarget();
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (!villager.isEntityAlive() || villager.isTrading()
                || xy177.farmersfuturedelight.common.registry.FFDVillagerHomes.isSleeping(villager)
                || held.getItem() == Items.SPAWN_EGG || held.getItem() == Items.NAME_TAG) return;
        net.minecraft.village.MerchantRecipeList offers = villager.getRecipes(event.getEntityPlayer());
        if (offers == null || !offers.isEmpty()) return;
        if (event.getHand() == net.minecraft.util.EnumHand.MAIN_HAND) {
            xy177.farmersfuturedelight.common.registry.FFDVillagerTypes.refuseTrade(villager);
        }
        event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void addTurtleTargeting(EntityCreature creature) {
        if (creature.getEntityData().getBoolean(TURTLE_ZOMBIE_AI_TAG)) {
            return;
        }
        if (FFDConfig.isConfiguredEntity(FFDConfig.turtleEggDestroyingMobs, creature)) {
            creature.tasks.addTask(4, new EntityAITrampleTurtleEgg(creature));
        }
        if (FFDConfig.isConfiguredEntity(FFDConfig.turtleBabyPredatorMobs, creature)) {
            creature.targetTasks.addTask(5,
                    new EntityAINearestAttackableTarget<EntityLivingBase>(creature,
                EntityLivingBase.class, 10, true, false, new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(EntityLivingBase turtle) {
                        return isBabyTurtleTarget(turtle);
                    }
                }));
        }
        creature.getEntityData().setBoolean(TURTLE_ZOMBIE_AI_TAG, true);
    }

    private static boolean isBabyTurtleTarget(EntityLivingBase entity) {
        return FFDConfig.isTurtleEntity(entity) && entity instanceof EntityAgeable
                && ((EntityAgeable) entity).isChild() && !entity.isInWater();
    }

    private static void addAxolotlTargeting(EntityGuardian guardian) {
        if (guardian.getEntityData().getBoolean(AXOLOTL_GUARDIAN_AI_TAG)) {
            return;
        }
        guardian.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityAxolotl>(
                guardian, EntityAxolotl.class, 10, true, false,
                new Predicate<EntityAxolotl>() {
                    @Override
                    public boolean apply(EntityAxolotl axolotl) {
                        return axolotl != null && !axolotl.isPlayingDead()
                                && axolotl.getDistanceSq(guardian) > 9.0D;
                    }
                }) {
            @Override
            public boolean shouldContinueExecuting() {
                EntityLivingBase target = guardian.getAttackTarget();
                return target instanceof EntityAxolotl
                        && !((EntityAxolotl) target).isPlayingDead()
                        && target.getDistanceSq(guardian) > 9.0D
                        && super.shouldContinueExecuting();
            }
        });
        guardian.getEntityData().setBoolean(AXOLOTL_GUARDIAN_AI_TAG, true);
    }
}
