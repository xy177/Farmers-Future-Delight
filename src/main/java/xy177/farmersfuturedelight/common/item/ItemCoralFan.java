package xy177.farmersfuturedelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.block.BlockCoralPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class ItemCoralFan extends ItemBlock {
    private final BlockCoralPlant floorFan;
    private final BlockCoralWallFan wallFan;

    public ItemCoralFan(BlockCoralPlant floorFan, BlockCoralWallFan wallFan) {
        super(floorFan);
        this.floorFan = floorFan;
        this.wallFan = wallFan;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!FFDItems.isCoralEnabled() || stack.isEmpty()) {
            return EnumActionResult.FAIL;
        }
        IBlockState clicked = world.getBlockState(pos);
        BlockPos target = clicked.getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        if (!player.canPlayerEdit(target, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        IBlockState placement = getPlacementState(world, target, facing);
        if (placement == null) {
            return EnumActionResult.FAIL;
        }

        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, target);
        if (!world.setBlockState(target, placement, 11)) {
            return EnumActionResult.FAIL;
        }
        if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, facing, hand).isCanceled()) {
            snapshot.restore(true, false);
            return EnumActionResult.FAIL;
        }
        world.setBlockState(target, placement, 11);
        SoundType sound = placement.getBlock().getSoundType(placement, world, target, player);
        world.playSound(player, target, sound.getPlaceSound(), SoundCategory.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, target, stack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    private IBlockState getPlacementState(World world, BlockPos pos, EnumFacing facing) {
        boolean waterlogged = WaterloggedBlockApi.isWaterSource(world, pos);
        if (facing == EnumFacing.UP && floorFan.canPlaceBlockAt(world, pos)) {
            return floorFan.getDefaultState()
                    .withProperty(BlockCoralPlant.WATERLOGGED, waterlogged);
        }
        if (facing.getAxis().isHorizontal() && wallFan.canPlaceBlockOnSide(world, pos, facing)) {
            return wallFan.getDefaultState()
                    .withProperty(BlockCoralWallFan.FACING, facing)
                    .withProperty(BlockCoralWallFan.WATERLOGGED, waterlogged);
        }
        return null;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side,
                                       EntityPlayer player, ItemStack stack) {
        IBlockState clicked = world.getBlockState(pos);
        BlockPos target = clicked.getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);
        return FFDItems.isCoralEnabled() && getPlacementState(world, target, side) != null;
    }
}
