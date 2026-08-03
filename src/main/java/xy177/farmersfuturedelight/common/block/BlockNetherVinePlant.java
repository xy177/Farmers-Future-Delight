package xy177.farmersfuturedelight.common.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherVinePlant extends Block implements IGrowable, IShearable {
    private static final AxisAlignedBB TWISTING_BODY_AABB =
            new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);
    private static final AxisAlignedBB WEEPING_BODY_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    private final boolean growsUpward;

    public BlockNetherVinePlant(String name, boolean growsUpward) {
        super(Material.VINE);
        this.growsUpward = growsUpward;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(0.0F);
        setSoundType(growsUpward ? FFDSounds.TWISTING_VINES : FFDSounds.WEEPING_VINES);
        setLightOpacity(0);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos)) {
            world.destroyBlock(pos, true);
            return;
        }
        if (!BlockNetherVine.isVineSegment(world.getBlockState(pos.offset(growthDirection())),
                growsUpward)) {
            world.setBlockState(pos, headBlock().getDefaultState(), 2);
            BlockNetherVine.setAge(world, pos, world.rand.nextInt(25));
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos headPos = findHead(world, pos);
        return headPos != null && headBlock().canGrow(world, headPos,
                world.getBlockState(headPos), isClient);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return findHead(world, pos) != null;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos headPos = findHead(world, pos);
        if (headPos != null) {
            IBlockState headState = world.getBlockState(headPos);
            headBlock().grow(world, random, headPos, headState);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(headBlock());
    }

    @Override
    public int quantityDropped(Random random) {
        return BlockNetherVine.shouldDrop(random, 0) ? 1 : 0;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return BlockNetherVine.shouldDrop(random, fortune) ? 1 : 0;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(headBlock()));
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos,
                                    int fortune) {
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(headBlock())));
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(headBlock()));
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos,
                            EntityLivingBase entity) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return growsUpward ? TWISTING_BODY_AABB : WEEPING_BODY_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
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

    private BlockPos findHead(World world, BlockPos start) {
        BlockPos cursor = start;
        int limit = world.getActualHeight();
        for (int distance = 0; distance < limit; distance++) {
            IBlockState state = world.getBlockState(cursor);
            if (state.getBlock() == headBlock()) {
                return cursor;
            }
            if (state.getBlock() != this) {
                return null;
            }
            cursor = cursor.offset(growthDirection());
        }
        return null;
    }

    private boolean canStay(World world, BlockPos pos) {
        BlockPos anchorPos = pos.offset(growthDirection().getOpposite());
        IBlockState anchor = world.getBlockState(anchorPos);
        return BlockNetherVine.isVineSegment(anchor, growsUpward)
                || anchor.isSideSolid(world, anchorPos, growthDirection());
    }

    private EnumFacing growthDirection() {
        return growsUpward ? EnumFacing.UP : EnumFacing.DOWN;
    }

    private BlockNetherVine headBlock() {
        return growsUpward ? FFDBlocks.TWISTING_VINES : FFDBlocks.WEEPING_VINES;
    }
}
