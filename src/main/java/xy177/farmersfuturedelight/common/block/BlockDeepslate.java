package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockDeepslate extends BlockRotatedPillar {
    public BlockDeepslate() {
        this("deepslate");
    }

    protected BlockDeepslate(String name) {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.0F);
        setResistance(6.0F / 3.0F);
        setSoundType(FFDSounds.DEEPSLATE);
        setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        Item item = FFDItems.effectiveItem(FFDItems.COBBLED_DEEPSLATE);
        return item == null ? Item.getItemFromBlock(net.minecraft.init.Blocks.AIR) : item;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }
}
