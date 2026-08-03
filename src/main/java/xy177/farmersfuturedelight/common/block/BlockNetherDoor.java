package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherDoor extends BlockDoor {
    public BlockNetherDoor(String name) {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        BlockPos lowerPos = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState lowerState = pos.equals(lowerPos) ? state : world.getBlockState(lowerPos);
        if (lowerState.getBlock() != this) {
            return false;
        }

        lowerState = lowerState.cycleProperty(OPEN);
        world.setBlockState(lowerPos, lowerState, 10);
        world.markBlockRangeForRenderUpdate(lowerPos, pos);
        playDoorSound(player, world, pos, lowerState.getValue(OPEN));
        return true;
    }

    @Override
    public void toggleDoor(World world, BlockPos pos, boolean open) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return;
        }

        BlockPos lowerPos = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState lowerState = pos.equals(lowerPos) ? state : world.getBlockState(lowerPos);
        if (lowerState.getBlock() == this && lowerState.getValue(OPEN) != open) {
            world.setBlockState(lowerPos, lowerState.withProperty(OPEN, open), 10);
            world.markBlockRangeForRenderUpdate(lowerPos, pos);
            playDoorSound(null, world, pos, open);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block changedBlock, BlockPos fromPos) {
        if (state.getValue(HALF) == EnumDoorHalf.UPPER) {
            BlockPos lowerPos = pos.down();
            IBlockState lowerState = world.getBlockState(lowerPos);
            if (lowerState.getBlock() != this) {
                world.setBlockToAir(pos);
            } else if (changedBlock != this) {
                lowerState.neighborChanged(world, lowerPos, changedBlock, fromPos);
            }
            return;
        }

        boolean removed = false;
        BlockPos upperPos = pos.up();
        IBlockState upperState = world.getBlockState(upperPos);
        if (upperState.getBlock() != this) {
            world.setBlockToAir(pos);
            removed = true;
        }

        if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)) {
            world.setBlockToAir(pos);
            removed = true;
            if (upperState.getBlock() == this) {
                world.setBlockToAir(upperPos);
            }
        }

        if (removed) {
            if (!world.isRemote) {
                dropBlockAsItem(world, pos, state, 0);
            }
            return;
        }

        boolean powered = world.isBlockPowered(pos) || world.isBlockPowered(upperPos);
        if (changedBlock != this
                && (powered || changedBlock.getDefaultState().canProvidePower())
                && powered != upperState.getValue(POWERED)) {
            world.setBlockState(upperPos, upperState.withProperty(POWERED, powered), 2);
            if (powered != state.getValue(OPEN)) {
                world.setBlockState(pos, state.withProperty(OPEN, powered), 2);
                world.markBlockRangeForRenderUpdate(pos, pos);
                playDoorSound(null, world, pos, powered);
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : getDoorItem();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(getDoorItem());
    }

    private Item getDoorItem() {
        Item item = Item.REGISTRY.getObject(getRegistryName());
        return item == null ? Items.AIR : item;
    }

    private static void playDoorSound(EntityPlayer player, World world, BlockPos pos, boolean open) {
        world.playSound(player, pos,
                open ? FFDSounds.NETHER_WOOD_DOOR_OPEN : FFDSounds.NETHER_WOOD_DOOR_CLOSE,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }
}
