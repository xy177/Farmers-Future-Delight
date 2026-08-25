package xy177.farmersfuturedelight.common;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockCake;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
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
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.util.SoundCategory;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockAbstractCandle;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockAmethyst;
import xy177.farmersfuturedelight.common.block.BlockCaveVines;
import xy177.farmersfuturedelight.common.block.CopperWeathering;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockKelpHead;
import xy177.farmersfuturedelight.common.block.BlockNetherVine;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockUnderwaterPlant;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.entity.ai.EntityAITrampleTurtleEgg;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDPotions;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDGameplayEvents {
    private static final ResourceLocation DEEPER_DEPTHS_COPPER_ORE =
            new ResourceLocation("deeperdepths", "copper_ore");

    private static final String TURTLE_ZOMBIE_AI_TAG = FarmerFutureDelight.MODID + ".turtleZombieAi";
    private static final String AXOLOTL_GUARDIAN_AI_TAG =
            FarmerFutureDelight.MODID + ".axolotlGuardianAi";
    private static final double SLOW_FALLING_GRAVITY_COMPENSATION = 0.08D - 0.01D;

    private FFDGameplayEvents() {
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) {
            return;
        }
        GameRules rules = event.getWorld().getGameRules();
        FFDGameplayHooks.ensureSleepingPercentageRule(rules);
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVanillaOreHarvested(HarvestDropsEvent event) {
        if (!FFDConfig.oresDropRawMaterials || event.isSilkTouching()) {
            return;
        }
        net.minecraft.item.Item raw;
        int count;
        if (event.getState().getBlock() == Blocks.IRON_ORE) {
            if (!FFDItems.isRawOreEnabled()) {
                return;
            }
            raw = FFDItems.RAW_IRON;
            count = 1;
        } else if (event.getState().getBlock() == Blocks.GOLD_ORE) {
            if (!FFDItems.isRawOreEnabled()) {
                return;
            }
            raw = FFDItems.RAW_GOLD;
            count = 1;
        } else if (DEEPER_DEPTHS_COPPER_ORE.equals(
                event.getState().getBlock().getRegistryName())) {
            if (!FFDItems.isCopperEnabled()) {
                return;
            }
            raw = FFDItems.RAW_COPPER;
            count = 2 + event.getWorld().rand.nextInt(4);
        } else {
            return;
        }

        if (event.getFortuneLevel() > 0) {
            int multiplier = event.getWorld().rand.nextInt(event.getFortuneLevel() + 2) - 1;
            count *= Math.max(0, multiplier) + 1;
        }
        event.getDrops().clear();
        ItemStack rawStack = FFDItems.effectiveStack(raw, count);
        if (!rawStack.isEmpty()) {
            event.getDrops().add(rawStack);
        }
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
        if (!FFDItems.isSignTextEnabled()) {
            return;
        }
        ItemStack held = event.getItemStack();
        boolean glowInk = FFDCompat.isCompatibleGlowInkSac(held);
        boolean dye = held.getItem() == Items.DYE && held.getMetadata() >= 0
                && held.getMetadata() < EnumDyeColor.values().length;
        if (!glowInk && !dye) {
            return;
        }
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (!(tile instanceof TileEntitySign)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (event.getWorld().isRemote) {
            return;
        }

        TileEntitySign sign = (TileEntitySign) tile;
        boolean changed;
        if (glowInk) {
            changed = FFDSignText.setGlowing(sign, true);
        } else if (held.getMetadata() == EnumDyeColor.BLACK.getDyeDamage()
                && FFDSignText.isGlowing(sign)) {
            // In 1.17+, a normal ink sac removes the glowing flag instead of
            // changing an already glowing sign to black text.
            changed = FFDSignText.setGlowing(sign, false);
        } else {
            changed = FFDSignText.applyDye(sign,
                    EnumDyeColor.byDyeDamage(held.getMetadata()));
        }
        if (!changed) {
            return;
        }

        event.getWorld().notifyBlockUpdate(event.getPos(),
                event.getWorld().getBlockState(event.getPos()),
                event.getWorld().getBlockState(event.getPos()), 3);
        event.getWorld().playSound(null, event.getPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            held.shrink(1);
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
        if (!FFDItems.isPhantomEnabled() || event.getLeft().getItem() != Items.ELYTRA
                || event.getRight().getItem() != FFDItems.PHANTOM_MEMBRANE) {
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
        if (dryState == null || event.getEmptyBucket().getItem() != Items.BUCKET) {
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
        player.addStat(StatList.getObjectUseStats(Items.BUCKET));
        player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
        event.setFilledBucket(new ItemStack(Items.WATER_BUCKET));
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onWaterBucketUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != Items.WATER_BUCKET) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        IBlockState wetState = withWaterlogged(state, true);
        if (wetState == null) {
            return;
        }

        EnumFacing side = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!event.getWorld().isBlockModifiable(event.getEntityPlayer(), event.getPos())
                || !event.getEntityPlayer().canPlayerEdit(event.getPos(), side,
                        event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), wetState, 11);
            event.getWorld().scheduleUpdate(event.getPos(), wetState.getBlock(), 5);
            event.getEntityPlayer().addStat(StatList.getObjectUseStats(Items.WATER_BUCKET));
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                event.getEntityPlayer().setHeldItem(event.getHand(), new ItemStack(Items.BUCKET));
            }
        }
        event.getWorld().playSound(event.getEntityPlayer(), event.getPos(),
                SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private static IBlockState withWaterlogged(IBlockState state, boolean waterlogged) {
        IBlockState result = WaterloggedPlantFluid.withWaterlogged(state, waterlogged);
        return result == null || WaterloggedPlantFluid.isWaterlogged(state) == waterlogged
                ? null : result;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().world.isRemote) {
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
        if (FFDItems.isCandleEnabled() && event.getEntity() instanceof EntityArrow
                && event.getEntity().isBurning()
                && state.getBlock() instanceof BlockAbstractCandle) {
            BlockAbstractCandle candle = (BlockAbstractCandle) state.getBlock();
            if (candle.canLight(state)) {
                candle.setLit(event.getEntity().world, hit.getBlockPos(), state, true);
            }
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
    public static void onNetherStemStripped(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote || !(event.getItemStack().getItem() instanceof ItemAxe)) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block stripped = strippedVariant(state.getBlock());
        if (stripped == null) {
            return;
        }
        IBlockState strippedState = stripped.getDefaultState().withProperty(BlockRotatedPillar.AXIS,
                state.getValue(BlockRotatedPillar.AXIS));
        event.getWorld().setBlockState(event.getPos(), strippedState, 11);
        ItemStack held = event.getItemStack();
        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
            held.damageItem(1, event.getEntityPlayer());
        }
        event.getWorld().playSound(null, event.getPos(), FFDSounds.AXE_STRIP,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        event.setCanceled(true);
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

    private static Block strippedVariant(Block block) {
        if (block == FFDBlocks.CRIMSON_STEM) {
            return FFDItems.effectiveBlock(FFDBlocks.STRIPPED_CRIMSON_STEM);
        }
        if (block == FFDBlocks.CRIMSON_HYPHAE) {
            return FFDItems.effectiveBlock(FFDBlocks.STRIPPED_CRIMSON_HYPHAE);
        }
        if (block == FFDBlocks.WARPED_STEM) {
            return FFDItems.effectiveBlock(FFDBlocks.STRIPPED_WARPED_STEM);
        }
        if (block == FFDBlocks.WARPED_HYPHAE) {
            return FFDItems.effectiveBlock(FFDBlocks.STRIPPED_WARPED_HYPHAE);
        }
        return null;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        if (FFDEntities.isLocalTurtleEnabled() && event.getEntity() instanceof EntityZombie
                && !(event.getEntity() instanceof EntityPigZombie)) {
            addTurtleTargeting((EntityZombie) event.getEntity());
        }
        if (FFDEntities.isLocalAxolotlEnabled()
                && event.getEntity() instanceof EntityGuardian) {
            addAxolotlTargeting((EntityGuardian) event.getEntity());
        }
    }

    private static void addTurtleTargeting(EntityZombie zombie) {
        if (zombie.getEntityData().getBoolean(TURTLE_ZOMBIE_AI_TAG)) {
            return;
        }
        zombie.tasks.addTask(4, new EntityAITrampleTurtleEgg(zombie));
        zombie.targetTasks.addTask(5, new EntityAINearestAttackableTarget<EntityTurtle>(zombie,
                EntityTurtle.class, 10, true, false, new Predicate<EntityTurtle>() {
                    @Override
                    public boolean apply(EntityTurtle turtle) {
                        return turtle.isChild() && !turtle.isInWater();
                    }
                }));
        zombie.getEntityData().setBoolean(TURTLE_ZOMBIE_AI_TAG, true);
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
