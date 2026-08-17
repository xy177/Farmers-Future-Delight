package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockPottedNetherPlant extends Block {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(
            0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    private final Block plant;

    public BlockPottedNetherPlant(String name, Block plant) {
        super(Material.CIRCUITS);
        this.plant = plant;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(0.0F);
        setSoundType(SoundType.STONE);
        setLightOpacity(0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return super.canPlaceBlockAt(world, pos)
                && (below.isTopSolid()
                || below.getBlockFaceShape(world, pos.down(), EnumFacing.UP)
                == BlockFaceShape.SOLID);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block block, BlockPos fromPos) {
        if (!canPlaceBlockAt(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            ItemStack plantStack = plantStack();
            if (!plantStack.isEmpty() && !player.addItemStackToInventory(plantStack)) {
                player.dropItem(plantStack, false);
            }
            world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState(), 3);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.FLOWER_POT));
        ItemStack plantStack = plantStack();
        if (!plantStack.isEmpty()) {
            drops.add(plantStack);
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return plantStack();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.FLOWER_POT;
    }

    private ItemStack plantStack() {
        return FFDItems.effectiveStack(Item.getItemFromBlock(plant));
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
