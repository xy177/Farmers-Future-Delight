package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.tile.TileEntityCaveVines;

public class BlockCaveVines extends BlockCaveVinesBase {
    private static final int MAX_AGE = 25;
    private static final double GROWTH_CHANCE = 0.1D;
    private static final float BERRIES_ON_GROWTH_CHANCE = 0.11F;

    public BlockCaveVines() {
        super("cave_vines");
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !FFDItems.isGlowBerryEnabled()) {
            return;
        }
        int age = getOrInitializeAge(world, pos, random);
        if (age < MAX_AGE && random.nextDouble() < GROWTH_CHANCE && world.isAirBlock(pos.down())) {
            world.setBlockState(pos,
                    FFDBlocks.CAVE_VINES_PLANT.stateWithBerries(hasBerries(state)), 2);
            BlockPos newHeadPos = pos.down();
            world.setBlockState(newHeadPos, stateWithBerries(
                    random.nextFloat() < BERRIES_ON_GROWTH_CHANCE), 3);
            setAge(world, newHeadPos, age + 1);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos)) {
            world.destroyBlock(pos, true);
        } else if (isCaveVine(world.getBlockState(pos.down()))) {
            world.setBlockState(pos,
                    FFDBlocks.CAVE_VINES_PLANT.stateWithBerries(hasBerries(state)), 2);
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityCaveVines();
    }

    public static void setAge(World world, BlockPos pos, int age) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityCaveVines)) {
            tile = new TileEntityCaveVines();
            world.setTileEntity(pos, tile);
        }
        ((TileEntityCaveVines) tile).setAge(age);
    }

    private static int getOrInitializeAge(World world, BlockPos pos, Random random) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCaveVines && ((TileEntityCaveVines) tile).getAge() >= 0) {
            return ((TileEntityCaveVines) tile).getAge();
        }
        int age = random.nextInt(25);
        setAge(world, pos, age);
        return age;
    }
}
