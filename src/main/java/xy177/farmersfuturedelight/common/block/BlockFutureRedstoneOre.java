package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockFutureRedstoneOre extends BlockFutureOre {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockFutureRedstoneOre() {
        super("deepslate_redstone_ore", Drop.REDSTONE, 2);
        setDefaultState(blockState.getBaseState().withProperty(LIT, false));
        setTickRandomly(true);
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        activate(world, pos);
        super.onBlockClicked(world, pos, player);
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!entity.isSneaking()) {
            activate(world, pos);
        }
        super.onEntityWalk(world, pos, entity);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        activate(world, pos);
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemBlock) {
            BlockPos target = state.getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
            if (world.getBlockState(target).getBlock().isReplaceable(world, target)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (state.getValue(LIT)) {
            world.setBlockState(pos, state.withProperty(LIT, false), 3);
        }
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        spawnParticles(world, pos);
    }

    private static void spawnParticles(World world, BlockPos pos) {
        Random random = world.rand;
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacent = pos.offset(facing);
            if (world.getBlockState(adjacent).isOpaqueCube()) {
                continue;
            }
            int offsetX = facing.getDirectionVec().getX();
            int offsetY = facing.getDirectionVec().getY();
            int offsetZ = facing.getDirectionVec().getZ();
            double x = pos.getX() + (offsetX == 0 ? random.nextDouble()
                    : 0.5D + offsetX * 0.5625D);
            double y = pos.getY() + (offsetY == 0 ? random.nextDouble()
                    : 0.5D + offsetY * 0.5625D);
            double z = pos.getZ() + (offsetZ == 0 ? random.nextDouble()
                    : 0.5D + offsetZ * 0.5625D);
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE,
                    x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(LIT) ? 9 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }

    private void activate(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        spawnParticles(world, pos);
        if (!state.getValue(LIT)) {
            world.setBlockState(pos, state.withProperty(LIT, true), 3);
        }
    }
}
