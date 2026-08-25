package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import javax.annotation.Nullable;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.WorldGenAzaleaTree;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockAzalea extends BlockBush implements IGrowable {
    private static final AxisAlignedBB AZALEA_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB STEM_AABB =
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D);
    private static final AxisAlignedBB CROWN_AABB =
            new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    private final boolean flowering;

    public BlockAzalea(boolean flowering) {
        super(Material.PLANTS);
        this.flowering = flowering;
        String name = flowering ? "flowering_azalea" : "azalea";
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(flowering ? FFDSounds.FLOWERING_AZALEA : FFDSounds.AZALEA);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isAzaleaEnabled()
                && world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && canGrowOn(world.getBlockState(pos.down()));
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return FFDItems.isAzaleaEnabled() && canGrowOn(world.getBlockState(pos.down()));
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isAzaleaEnabled()
                && pos.getY() + 6 < FFDHeightHooks.maxYExclusive(world)
                && !world.getBlockState(pos.up()).getMaterial().isLiquid();
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isAzaleaEnabled() && random.nextFloat() < 0.45F;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
        if (!WorldGenAzaleaTree.INSTANCE.generate(world, random, pos)) {
            world.setBlockState(pos, state, 4);
        }
    }

    public static boolean canGrowOn(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.MYCELIUM
                || block == Blocks.FARMLAND || block == Blocks.CLAY
                || FFDLushCaveBlockProvider.get().isMossBlock(state)
                || FFDLushCaveBlockProvider.get().isRootedDirt(state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source,
                                        BlockPos pos) {
        return AZALEA_AABB;
    }

    public boolean isFlowering() {
        return flowering;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entity, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, STEM_AABB);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CROWN_AABB);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state,
                                                  net.minecraft.world.IBlockAccess world,
                                                  BlockPos pos) {
        return AZALEA_AABB;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
