package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public abstract class BlockCaveVinesBase extends Block implements IGrowable {
    public static final PropertyBool BERRIES = PropertyBool.create("berries");
    private static final AxisAlignedBB VINE_AABB =
            new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 1.0D, 0.9375D);

    protected BlockCaveVinesBase(String name) {
        super(Material.VINE);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setHardness(0.0F);
        setSoundType(FFDSounds.CAVE_VINES);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(BERRIES, false));
    }

    public IBlockState stateWithBerries(boolean berries) {
        return getDefaultState().withProperty(BERRIES, berries);
    }

    public static boolean isCaveVine(IBlockState state) {
        return state.getBlock() == FFDBlocks.CAVE_VINES
                || state.getBlock() == FFDBlocks.CAVE_VINES_PLANT;
    }

    protected static boolean hasBerries(IBlockState state) {
        return state.getValue(BERRIES);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BERRIES);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BERRIES) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return stateWithBerries((meta & 1) != 0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isGlowBerryEnabled() && canStay(world, pos);
    }

    protected static boolean canStay(World world, BlockPos pos) {
        IBlockState above = world.getBlockState(pos.up());
        return isCaveVine(above) || above.isSideSolid(world, pos.up(), EnumFacing.DOWN);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!world.isRemote && !canStay(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    net.minecraft.entity.player.EntityPlayer player,
                                    net.minecraft.util.EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!hasBerries(state)) {
            return false;
        }
        if (!world.isRemote) {
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.GLOW_BERRIES));
            world.setBlockState(pos, stateWithBerries(false), 2);
            world.playSound(null, pos, FFDSounds.CAVE_VINES_PICK_BERRIES,
                    SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
        }
        return true;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isGlowBerryEnabled() && !hasBerries(state);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return canGrow(world, pos, state, world.isRemote);
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!hasBerries(state)) {
            world.setBlockState(pos, stateWithBerries(true), 2);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return hasBerries(state) && FFDItems.isGlowBerryEnabled()
                ? FFDItems.GLOW_BERRIES : Item.getItemFromBlock(net.minecraft.init.Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (hasBerries(state) && FFDItems.isGlowBerryEnabled()) {
            drops.add(FFDItems.effectiveStack(FFDItems.GLOW_BERRIES));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.GLOW_BERRIES);
    }

    @Override
    public int getLightValue(IBlockState state) {
        return hasBerries(state) ? 14 : 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return VINE_AABB;
    }

    @Override
    @Nullable
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos,
                            EntityLivingBase entity) {
        return true;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
    }
}
