package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;

public class BlockMoss extends Block implements IGrowable {
    private static final int VERTICAL_RANGE = 5;

    public BlockMoss() {
        super(Material.GRASS);
        setRegistryName(FarmerFutureDelight.MODID, "moss_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".moss_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.1F);
        setSoundType(FFDSounds.MOSS);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isMossEnabled() && world.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isMossEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!canGrow(world, pos, state, false)) {
            return;
        }

        BlockPos origin = pos.up();
        int xRadius = 2 + random.nextInt(2);
        int zRadius = 2 + random.nextInt(2);
        for (int offsetX = -xRadius; offsetX <= xRadius; offsetX++) {
            boolean xEdge = Math.abs(offsetX) == xRadius;
            for (int offsetZ = -zRadius; offsetZ <= zRadius; offsetZ++) {
                boolean zEdge = Math.abs(offsetZ) == zRadius;
                if (xEdge && zEdge || (xEdge || zEdge) && random.nextFloat() > 0.75F) {
                    continue;
                }

                BlockPos surface = findSurface(world, origin.add(offsetX, 0, offsetZ));
                if (surface == null) {
                    continue;
                }
                BlockPos ground = surface.down();
                IBlockState groundState = world.getBlockState(ground);
                if (!groundState.isSideSolid(world, ground, EnumFacing.UP)
                        || !isMossReplaceable(groundState)) {
                    continue;
                }
                if (groundState.getBlock() != this) {
                    world.setBlockState(ground, getDefaultState(), 2);
                }
                if (random.nextFloat() < 0.6F) {
                    placeVegetation(world, random, surface);
                }
            }
        }
    }

    private static BlockPos findSurface(World world, BlockPos start) {
        BlockPos cursor = start;
        int offset = 0;
        while (world.isAirBlock(cursor) && offset++ < VERTICAL_RANGE) {
            cursor = cursor.down();
        }
        offset = 0;
        while (!world.isAirBlock(cursor) && offset++ < VERTICAL_RANGE) {
            cursor = cursor.up();
        }
        return world.isAirBlock(cursor) ? cursor : null;
    }

    private static void placeVegetation(World world, Random random, BlockPos pos) {
        if (!world.isAirBlock(pos)) {
            return;
        }
        int roll = random.nextInt(96);
        FFDLushCaveBlockProvider blocks = FFDLushCaveBlockProvider.get();
        if (roll < 4) {
            IBlockState flowering = blocks.azalea(true);
            if (flowering != null && flowering.getBlock().canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, flowering, 3);
            }
        } else if (roll < 11) {
            IBlockState azalea = blocks.azalea(false);
            if (azalea != null && azalea.getBlock().canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, azalea, 3);
            }
        } else if (roll < 36) {
            IBlockState carpet = blocks.mossCarpet();
            if (carpet != null && carpet.getBlock().canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, carpet, 3);
            }
        } else if (roll < 86) {
            world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 3);
        } else if (world.isAirBlock(pos.up())) {
            Blocks.DOUBLE_PLANT.placeAt(world, pos, BlockDoublePlant.EnumPlantType.GRASS, 3);
        }
    }

    public static boolean isMossReplaceable(IBlockState state) {
        Block block = state.getBlock();
        FFDLushCaveBlockProvider blocks = FFDLushCaveBlockProvider.get();
        return block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.MYCELIUM || blocks.isRootedDirt(state)
                || blocks.isMossBlock(state) || blocks.isCaveVine(state);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos,
                                   EnumFacing direction, IPlantable plantable) {
        if (direction == EnumFacing.UP
                && (plantable == Blocks.TALLGRASS || plantable == Blocks.DOUBLE_PLANT)) {
            return true;
        }
        return super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
