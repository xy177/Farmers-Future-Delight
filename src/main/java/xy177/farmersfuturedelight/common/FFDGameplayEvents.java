package xy177.farmersfuturedelight.common;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockBigDripleaf;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockGlowLichen;
import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockUnderwaterPlant;
import xy177.farmersfuturedelight.common.block.WaterloggedPlantFluid;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.entity.ai.EntityAITrampleTurtleEgg;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDGameplayEvents {
    private static final String TURTLE_ZOMBIE_AI_TAG = FarmerFutureDelight.MODID + ".turtleZombieAi";

    private FFDGameplayEvents() {
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
        RayTraceResult hit = event.getRayTraceResult();
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        IBlockState state = event.getEntity().world.getBlockState(hit.getBlockPos());
        if (BlockBigDripleaf.isBigDripleaf(state)) {
            BlockBigDripleaf.tiltFully(event.getEntity().world, hit.getBlockPos(), state);
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
        if (FFDItems.isHangingRootsEnabled()) {
            Block.spawnAsEntity(event.getWorld(), event.getPos().offset(event.getFace()),
                    new ItemStack(FFDItems.HANGING_ROOTS));
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
            return FFDBlocks.STRIPPED_CRIMSON_STEM;
        }
        if (block == FFDBlocks.CRIMSON_HYPHAE) {
            return FFDBlocks.STRIPPED_CRIMSON_HYPHAE;
        }
        if (block == FFDBlocks.WARPED_STEM) {
            return FFDBlocks.STRIPPED_WARPED_STEM;
        }
        if (block == FFDBlocks.WARPED_HYPHAE) {
            return FFDBlocks.STRIPPED_WARPED_HYPHAE;
        }
        return null;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !FFDEntities.isTurtleEnabled()
                || !(event.getEntity() instanceof EntityZombie)
                || event.getEntity() instanceof EntityPigZombie) {
            return;
        }

        EntityZombie zombie = (EntityZombie) event.getEntity();
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
}
