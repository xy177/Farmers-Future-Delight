package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.block.BlockSeaPickle;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemSeaPickle extends ItemUnderwaterPlant {
    private final BlockSeaPickle seaPickle;

    public ItemSeaPickle(BlockSeaPickle block) {
        super(block);
        this.seaPickle = block;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return FFDItems.isSeaPickleEnabled();
    }

    @Override
    protected IBlockState getPlacementState(World world, BlockPos pos) {
        return seaPickle.getPlacementState(world, pos);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (isFeatureEnabled() && !player.isSneaking()) {
            EnumActionResult stacked = StackableBlockItemPlacement.tryStack(player, world, pos,
                    facing, hand, seaPickle, BlockSeaPickle.PICKLES, 4, 10);
            if (stacked != EnumActionResult.PASS) {
                return stacked;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
                                       EntityPlayer player, ItemStack stack) {
        if (isFeatureEnabled()) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == seaPickle
                    && state.getValue(BlockSeaPickle.PICKLES) < 4) {
                return true;
            }
            BlockPos target = state.getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);
            IBlockState targetState = world.getBlockState(target);
            if (targetState.getBlock() == seaPickle
                    && targetState.getValue(BlockSeaPickle.PICKLES) < 4) {
                return true;
            }
        }
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }
}
