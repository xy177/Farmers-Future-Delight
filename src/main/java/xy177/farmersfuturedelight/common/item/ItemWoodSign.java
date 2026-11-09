package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockWoodStandingSign;
import xy177.farmersfuturedelight.common.block.BlockWoodWallSign;

public class ItemWoodSign extends ItemSign {
    private final BlockWoodStandingSign standingSign;
    private final BlockWoodWallSign wallSign;

    public ItemWoodSign(BlockWoodStandingSign standingSign, BlockWoodWallSign wallSign) {
        this.standingSign = standingSign;
        this.wallSign = wallSign;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        IBlockState clickedState = world.getBlockState(pos);
        boolean replaceable = clickedState.getBlock().isReplaceable(world, pos);

        if (facing == EnumFacing.DOWN
                || (!clickedState.getMaterial().isSolid() && !replaceable)) {
            return EnumActionResult.FAIL;
        }
        BlockPos placementPos = replaceable ? pos : pos.offset(facing);
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(placementPos, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        EnumFacing placementFacing = facing;
        net.minecraft.block.Block target = placementFacing == EnumFacing.UP ? standingSign : wallSign;
        if (!target.canPlaceBlockOnSide(world, placementPos, placementFacing)) {
            if (!replaceable || !standingSign.canPlaceBlockOnSide(world, placementPos, EnumFacing.UP)) {
                return EnumActionResult.FAIL;
            }
            placementFacing = EnumFacing.UP;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        pos = placementPos;
        IBlockState placeState;
        if (placementFacing == EnumFacing.UP) {
            int rotation = MathHelper.floor((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
            placeState = standingSign.getDefaultState().withProperty(BlockStandingSign.ROTATION, rotation);
        } else {
            placeState = wallSign.getDefaultState().withProperty(BlockWallSign.FACING, placementFacing);
        }
        if (!world.setBlockState(pos, placeState, 11)) {
            return EnumActionResult.FAIL;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(world, player, pos, stack)) {
            player.openEditSign((TileEntitySign) tile);
        }
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
