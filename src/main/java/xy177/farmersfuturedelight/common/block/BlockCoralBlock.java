package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockCoralBlock extends Block {
    private final int coralIndex;
    private final BlockCoralBlock deadVariant;

    public BlockCoralBlock(String name, int coralIndex, BlockCoralBlock deadVariant) {
        super(Material.ROCK, mapColor(coralIndex, deadVariant != null));
        this.coralIndex = coralIndex;
        this.deadVariant = deadVariant;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(1.5F);
        setResistance(10.0F);
        setHarvestLevel("pickaxe", 0);
        setSoundType(deadVariant == null ? SoundType.STONE : FFDSounds.CORAL);
    }

    private static MapColor mapColor(int coralIndex, boolean alive) {
        if (!alive) {
            return MapColor.GRAY;
        }
        switch (coralIndex) {
            case 0:
                return MapColor.BLUE;
            case 1:
                return MapColor.PINK;
            case 2:
                return MapColor.PURPLE;
            case 3:
                return MapColor.RED;
            case 4:
                return MapColor.YELLOW;
            default:
                return MapColor.GRAY;
        }
    }

    public boolean isAlive() {
        return deadVariant != null;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        scheduleDeath(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        scheduleDeath(world, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && isAlive() && !hasWater(world, pos)) {
            world.setBlockState(pos, deadVariant.getDefaultState(), 2);
        }
    }

    private void scheduleDeath(World world, BlockPos pos) {
        if (!world.isRemote && isAlive() && !hasWater(world, pos)) {
            world.scheduleUpdate(pos, this, 60 + world.rand.nextInt(40));
        }
    }

    private static boolean hasWater(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (WaterloggedBlockApi.containsWater(world, pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return isAlive() ? FFDItems.DEAD_CORAL_BLOCK_ITEMS[coralIndex]
                : FFDItems.DEAD_CORAL_BLOCK_ITEMS[coralIndex];
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state,
                                  EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return FFDItems.effectiveStack(isAlive() ? FFDItems.CORAL_BLOCK_ITEMS[coralIndex]
                : FFDItems.DEAD_CORAL_BLOCK_ITEMS[coralIndex]);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return getSilkTouchDrop(state);
    }
}
