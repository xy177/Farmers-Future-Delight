package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.WorldGenCoralReefs;

public final class FFDUnderwaterBonemeal {
    private FFDUnderwaterBonemeal() {
    }

    public static EnumActionResult onItemUse(EnumActionResult original, EntityPlayer player,
                                              World world, BlockPos pos, EnumHand hand,
                                              EnumFacing face) {
        if (original != EnumActionResult.PASS) {
            return original;
        }
        ItemStack stack = player.getHeldItem(hand);
        BlockPos target = pos.offset(face);
        if (stack.getItem() != Items.DYE || stack.getMetadata() != 15
                || !player.canPlayerEdit(target, face, stack)
                || !world.isBlockModifiable(player, target)
                || !world.getBlockState(pos).isSideSolid(world, pos, face)
                || !grow(stack, world, target, face)) {
            return original;
        }
        if (!world.isRemote) {
            world.playEvent(2005, target, 0);
            world.playSound(null, target, FFDSounds.BONE_MEAL_USE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return EnumActionResult.SUCCESS;
    }

    public static boolean grow(ItemStack stack, World world, BlockPos origin, EnumFacing face) {
        if (!FFDItems.isBlockRegistered(FFDBlocks.SEAGRASS)
                || !FFDItems.isSeagrassEnabled() || !world.isBlockLoaded(origin)
                || !isFullWater(world.getBlockState(origin))) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        List<IBlockState> underwaterPlants = new ArrayList<>();
        List<IBlockState> wallCorals = new ArrayList<>();
        underwaterPlants.add(FFDBlocks.SEAGRASS.getDefaultState());
        for (Block[] group : new Block[][] {
                FFDBlocks.CORALS, FFDBlocks.CORAL_FANS, FFDBlocks.CORAL_WALL_FANS}) {
            for (Block block : group) {
                if (!FFDItems.isBlockRegistered(block)) {
                    continue;
                }
                IBlockState wet = WaterloggedBlockApi.withWaterlogged(block.getDefaultState(), true);
                if (wet != null) {
                    underwaterPlants.add(wet);
                    if (block instanceof BlockCoralWallFan) {
                        wallCorals.add(wet);
                    }
                }
            }
        }
        Random random = world.rand;
        attempts:
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos target = origin;
            IBlockState growth = FFDBlocks.SEAGRASS.getDefaultState();
            for (int step = 0; step < attempt / 16; step++) {
                target = target.add(random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);
                if (!world.isBlockLoaded(target) || world.getBlockState(target).isFullCube()) {
                    continue attempts;
                }
            }
            if (WorldGenCoralReefs.isWarmOceanClimate(world, target.getX(), target.getZ())) {
                if (attempt == 0 && face != null && face.getAxis().isHorizontal()) {
                    if (!wallCorals.isEmpty()) {
                        growth = wallCorals.get(random.nextInt(wallCorals.size()))
                                .withProperty(BlockCoralWallFan.FACING, face);
                    }
                } else if (random.nextInt(4) == 0) {
                    growth = underwaterPlants.get(random.nextInt(underwaterPlants.size()));
                }
            }
            if (growth.getBlock() instanceof BlockCoralWallFan) {
                for (int rotation = 0; !canPlace(world, target, growth) && rotation < 4; rotation++) {
                    growth = growth.withProperty(BlockCoralWallFan.FACING,
                            EnumFacing.Plane.HORIZONTAL.random(random));
                }
            }
            if (!canPlace(world, target, growth)) {
                continue;
            }
            IBlockState current = world.getBlockState(target);
            if (isFullWater(current)) {
                world.setBlockState(target, growth, 3);
            } else if (current.getBlock() == FFDBlocks.SEAGRASS
                    && ((IGrowable) current.getBlock()).canGrow(world, target, current, false)
                    && random.nextInt(10) == 0) {
                ((IGrowable) current.getBlock()).grow(world, random, target, current);
            }
        }
        stack.shrink(1);
        return true;
    }

    private static boolean canPlace(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() instanceof BlockCoralWallFan
                ? state.getBlock().canPlaceBlockOnSide(world, pos, state.getValue(BlockCoralWallFan.FACING))
                : state.getBlock() instanceof BlockBush
                        ? ((BlockBush) state.getBlock()).canBlockStay(world, pos, state)
                        : state.getBlock().canPlaceBlockAt(world, pos);
    }

    private static boolean isFullWater(IBlockState state) {
        return (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)
                && (state.getValue(BlockLiquid.LEVEL) == 0 || state.getValue(BlockLiquid.LEVEL) >= 8);
    }
}
