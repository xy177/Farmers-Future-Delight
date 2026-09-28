package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNoFacePumpkin extends Block {
    public BlockNoFacePumpkin() {
        super(Material.GOURD, MapColor.ADOBE);
        setRegistryName(FarmerFutureDelight.MODID, "pumpkin");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".pumpkin");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(1.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() != Items.SHEARS || !player.canPlayerEdit(pos, facing, stack)
                || !world.isBlockModifiable(player, pos)) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        EnumFacing direction = facing.getAxis() == EnumFacing.Axis.Y
                ? player.getHorizontalFacing().getOpposite() : facing;
        if (!world.setBlockState(pos, Blocks.PUMPKIN.getDefaultState()
                .withProperty(BlockPumpkin.FACING, direction), 11)) {
            return false;
        }
        world.playSound(null, pos, FFDSounds.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        EntityItem seeds = new EntityItem(world,
                pos.getX() + 0.5D + direction.getFrontOffsetX() * 0.65D, pos.getY() + 0.1D,
                pos.getZ() + 0.5D + direction.getFrontOffsetZ() * 0.65D,
                new ItemStack(Items.PUMPKIN_SEEDS, 4));
        seeds.motionX = direction.getFrontOffsetX() * 0.05D + world.rand.nextDouble() * 0.02D;
        seeds.motionY = 0.05D;
        seeds.motionZ = direction.getFrontOffsetZ() * 0.05D + world.rand.nextDouble() * 0.02D;
        world.spawnEntity(seeds);
        stack.damageItem(1, player);
        player.addStat(StatList.getObjectUseStats(Items.SHEARS));
        return true;
    }
}
