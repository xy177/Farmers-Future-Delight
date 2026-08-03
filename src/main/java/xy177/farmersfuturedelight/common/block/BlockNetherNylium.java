package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.NetherForestFeatures;

public class BlockNetherNylium extends Block implements net.minecraft.block.IGrowable {
    private final boolean warped;

    public BlockNetherNylium(boolean warped) {
        super(Material.GRASS);
        this.warped = warped;
        String name = warped ? "warped_nylium" : "crimson_nylium";
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.4F);
        setSoundType(FFDSounds.NYLIUM);
        setHarvestLevel("pickaxe", 0);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && world.getBlockState(pos.up()).getLightOpacity(world, pos.up()) >= 15) {
            world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), 3);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.NETHERRACK);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return isEnabled() && world.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return isEnabled() && world.isAirBlock(pos.up());
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (isEnabled()) {
            NetherForestFeatures.growNyliumVegetation(world, random, pos, warped);
        }
    }

    public boolean isWarped() {
        return warped;
    }

    private boolean isEnabled() {
        return warped ? FFDItems.isWarpedEnabled() : FFDItems.isCrimsonEnabled();
    }
}
