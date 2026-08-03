package xy177.farmersfuturedelight.common.item;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.block.BlockCaveVines;
import xy177.farmersfuturedelight.common.block.BlockCaveVinesBase;

public class ItemGlowBerries extends ItemFood {
    public ItemGlowBerries() {
        super(2, 0.1F, false);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.DOWN) {
            return EnumActionResult.PASS;
        }
        BlockPos vinePos = pos.down();
        ItemStack stack = player.getHeldItem(hand);
        if (!FFDItems.isGlowBerryEnabled() || !player.canPlayerEdit(vinePos, facing, stack)
                || !world.isAirBlock(vinePos)
                || !FFDBlocks.CAVE_VINES.canPlaceBlockAt(world, vinePos)) {
            return EnumActionResult.FAIL;
        }
        if (!world.isRemote) {
            boolean body = BlockCaveVinesBase.isCaveVine(world.getBlockState(vinePos.down()));
            IBlockState state = body ? FFDBlocks.CAVE_VINES_PLANT.getDefaultState()
                    : FFDBlocks.CAVE_VINES.getDefaultState();
            world.setBlockState(vinePos, state, 3);
            if (!body) {
                BlockCaveVines.setAge(world, vinePos, world.rand.nextInt(25));
            }
            SoundType sound = state.getBlock().getSoundType(state, world, vinePos, player);
            world.playSound(null, vinePos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                    (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (FFDItems.isGlowBerryEnabled() && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }
}
