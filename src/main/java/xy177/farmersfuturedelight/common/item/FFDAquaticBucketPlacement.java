package xy177.farmersfuturedelight.common.item;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;

import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

final class FFDAquaticBucketPlacement {
    private FFDAquaticBucketPlacement() {
    }

    static PlacementResult empty(ItemBucket bucket, World world, EntityPlayer player,
                                 EnumHand hand, @Nullable RayTraceResult hit,
                                 SoundEvent waterloggedSound, SoundCategory soundCategory) {
        ItemStack held = player.getHeldItem(hand);
        ActionResult<ItemStack> eventResult = ForgeEventFactory.onBucketUse(
                player, world, held, hit);
        if (eventResult != null) {
            BlockPos spawnPos = eventResult.getType() == EnumActionResult.SUCCESS
                    ? findWaterDestination(world, hit) : null;
            return new PlacementResult(eventResult, spawnPos);
        }
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return result(EnumActionResult.PASS, held, null);
        }

        BlockPos clickedPos = hit.getBlockPos();
        EnumFacing side = hit.sideHit == null ? EnumFacing.UP : hit.sideHit;
        if (!world.isBlockModifiable(player, clickedPos)) {
            return result(EnumActionResult.FAIL, held, null);
        }

        IBlockState clickedState = world.getBlockState(clickedPos);
        IBlockState wetState = WaterloggedBlockApi.withWaterlogged(clickedState, true);
        if (wetState != null && !WaterloggedBlockApi.isWaterlogged(clickedState)) {
            if (!player.canPlayerEdit(clickedPos, side, held)) {
                return result(EnumActionResult.FAIL, held, null);
            }
            if (world.provider.doesWaterVaporize()) {
                BlockPos target = placementPos(world, hit);
                return placeNormally(bucket, world, player, held, side, target);
            }
            if (!world.setBlockState(clickedPos, wetState, 11)) {
                return result(EnumActionResult.FAIL, held, null);
            }
            world.scheduleUpdate(clickedPos, wetState.getBlock(), 5);
            world.playSound(player, clickedPos, waterloggedSound,
                    soundCategory, 1.0F, 1.0F);
            player.addStat(StatList.getObjectUseStats(bucket));
            return success(player, held, clickedPos);
        }

        BlockPos target = placementPos(world, hit);
        if (target != null) {
            IBlockState targetState = world.getBlockState(target);
            IBlockState targetWetState = WaterloggedBlockApi.withWaterlogged(targetState, true);
            if (targetWetState != null && !WaterloggedBlockApi.isWaterlogged(targetState)) {
                if (!player.canPlayerEdit(target, side, held)) {
                    return result(EnumActionResult.FAIL, held, null);
                }
                if (world.provider.doesWaterVaporize()) {
                    return placeNormally(bucket, world, player, held, side, target);
                }
                if (!world.setBlockState(target, targetWetState, 11)) {
                    return result(EnumActionResult.FAIL, held, null);
                }
                world.scheduleUpdate(target, targetWetState.getBlock(), 5);
                world.playSound(player, target, waterloggedSound,
                        soundCategory, 1.0F, 1.0F);
                player.addStat(StatList.getObjectUseStats(bucket));
                return success(player, held, target);
            }
        }

        PlacementResult fluidlogged = FluidloggedApi.tryEmpty(
                bucket, world, player, held, hit);
        if (fluidlogged != null) {
            return fluidlogged;
        }

        return placeNormally(bucket, world, player, held, side, placementPos(world, hit));
    }

    private static PlacementResult placeNormally(ItemBucket bucket, World world,
                                                  EntityPlayer player, ItemStack held,
                                                  EnumFacing side, @Nullable BlockPos target) {
        if (target == null || !world.isBlockModifiable(player, target)
                || !player.canPlayerEdit(target, side, held)) {
            return result(EnumActionResult.FAIL, held, null);
        }
        if (!bucket.tryPlaceContainedLiquid(player, world, target)) {
            return result(EnumActionResult.FAIL, held, null);
        }
        player.addStat(StatList.getObjectUseStats(bucket));
        return success(player, held, target);
    }

    @Nullable
    private static BlockPos placementPos(World world, RayTraceResult hit) {
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
        return replaceable && hit.sideHit == EnumFacing.UP ? pos : pos.offset(hit.sideHit);
    }

    @Nullable
    private static BlockPos findWaterDestination(World world, @Nullable RayTraceResult hit) {
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        BlockPos clickedPos = hit.getBlockPos();
        if (containsWater(world, clickedPos)) {
            return clickedPos;
        }
        BlockPos target = placementPos(world, hit);
        return target != null && containsWater(world, target) ? target : null;
    }

    private static boolean containsWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getMaterial() == net.minecraft.block.material.Material.WATER
                || WaterloggedBlockApi.containsWater(world, pos)) {
            return true;
        }
        return FluidloggedApi.containsWater(world, pos);
    }

    private static PlacementResult success(EntityPlayer player, ItemStack held,
                                           BlockPos spawnPos) {
        ItemStack result = player.capabilities.isCreativeMode
                ? held : new ItemStack(Items.BUCKET);
        return result(EnumActionResult.SUCCESS, result, spawnPos);
    }

    private static PlacementResult result(EnumActionResult type, ItemStack stack,
                                          @Nullable BlockPos spawnPos) {
        return new PlacementResult(new ActionResult<>(type, stack), spawnPos);
    }

    static final class PlacementResult {
        final ActionResult<ItemStack> action;
        @Nullable
        final BlockPos spawnPos;

        private PlacementResult(ActionResult<ItemStack> action, @Nullable BlockPos spawnPos) {
            this.action = action;
            this.spawnPos = spawnPos;
        }
    }

    private static final class FluidloggedApi {
        private static boolean unavailable;
        private static Method placeFluid;
        private static Method getFluidState;
        private static Method getFluid;

        private FluidloggedApi() {
        }

        @Nullable
        private static PlacementResult tryEmpty(ItemBucket bucket, World world,
                                                EntityPlayer player, ItemStack held,
                                                RayTraceResult hit) {
            if (unavailable || !Loader.isModLoaded("fluidlogged_api")) {
                return null;
            }
            try {
                initialize();
                ItemStack carrier = new ItemStack(Items.WATER_BUCKET);
                @SuppressWarnings("unchecked")
                ActionResult<ItemStack> external = (ActionResult<ItemStack>) placeFluid.invoke(
                        null, world, player, Blocks.FLOWING_WATER, carrier, hit, bucket);
                if (external.getType() != EnumActionResult.SUCCESS) {
                    return new PlacementResult(
                            new ActionResult<>(external.getType(), held), null);
                }

                BlockPos spawnPos = findWaterDestination(world, hit);
                if (spawnPos == null) {
                    return result(EnumActionResult.FAIL, held, null);
                }
                return success(player, held, spawnPos);
            } catch (ReflectiveOperationException | LinkageError exception) {
                unavailable = true;
                return null;
            }
        }

        private static void initialize() throws ReflectiveOperationException {
            if (placeFluid != null) {
                return;
            }
            Class<?> hooks = Class.forName(
                    "git.jbredwards.fluidlogged_api.mod.asm.plugins.vanilla.item."
                            + "PluginItemBucket$Hooks");
            placeFluid = hooks.getMethod("placeFluid", World.class, EntityPlayer.class,
                    net.minecraft.block.Block.class, ItemStack.class,
                    RayTraceResult.class, net.minecraft.item.Item.class);

            Class<?> utils = Class.forName(
                    "git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils");
            Class<?> fluidState = Class.forName(
                    "git.jbredwards.fluidlogged_api.api.util.FluidState");
            getFluidState = utils.getMethod("getFluidState",
                    net.minecraft.world.IBlockAccess.class, BlockPos.class);
            getFluid = fluidState.getMethod("getFluid");
        }

        private static boolean containsExternalWater(World world, BlockPos pos)
                throws ReflectiveOperationException {
            Object state = getFluidState.invoke(null, world, pos);
            Object fluid = getFluid.invoke(state);
            return fluid == net.minecraftforge.fluids.FluidRegistry.WATER;
        }

        private static boolean containsWater(World world, BlockPos pos) {
            if (unavailable || !Loader.isModLoaded("fluidlogged_api")) {
                return false;
            }
            try {
                initialize();
                return containsExternalWater(world, pos);
            } catch (ReflectiveOperationException | LinkageError exception) {
                unavailable = true;
                return false;
            }
        }
    }
}
