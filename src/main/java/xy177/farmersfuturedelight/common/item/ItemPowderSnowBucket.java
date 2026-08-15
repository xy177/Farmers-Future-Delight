package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemPowderSnowBucket extends Item {
    public ItemPowderSnowBucket() {
        setMaxStackSize(1);
        setContainerItem(Items.BUCKET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        Block powderSnow = FFDItems.effectiveBlock(FFDBlocks.POWDER_SNOW);
        if (powderSnow == null) {
            return EnumActionResult.FAIL;
        }
        IBlockState clickedState = world.getBlockState(pos);
        if (clickedState.getBlock() == powderSnow
                || !clickedState.getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty() || !player.canPlayerEdit(pos, facing, stack)
                || !world.mayPlace(powderSnow, pos, true, facing, player)) {
            return EnumActionResult.FAIL;
        }

        IBlockState placementState = powderSnow.getStateForPlacement(world, pos, facing,
                hitX, hitY, hitZ, stack.getMetadata(), player, hand);
        if (!world.setBlockState(pos, placementState, 11)) {
            return EnumActionResult.FAIL;
        }
        IBlockState placedState = world.getBlockState(pos);
        if (placedState.getBlock() == powderSnow) {
            powderSnow.onBlockPlacedBy(world, pos, placedState, player, stack);
        }

        world.playSound(player, pos, FFDSounds.BUCKET_EMPTY_POWDER_SNOW,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (!world.isRemote && !player.capabilities.isCreativeMode) {
            player.setHeldItem(hand, new ItemStack(Items.BUCKET));
        }
        return EnumActionResult.SUCCESS;
    }

}
