package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockSmallDripleaf extends Block implements IGrowable, IWaterloggableBlock {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF = BlockDoublePlant.HALF;
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB SHAPE =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.8125D, 0.875D);

    public BlockSmallDripleaf() {
        super(Material.PLANTS);
        setRegistryName(FarmerFutureDelight.MODID, "small_dripleaf");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".small_dripleaf");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(FFDSounds.SMALL_DRIPLEAF);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
                .withProperty(WATERLOGGED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, FACING, HALF, WATERLOGGED},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Material.WATER : Material.PLANTS;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockSmallDripleaf && state.getValue(WATERLOGGED);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return isWaterlogged(state);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return state.withProperty(WATERLOGGED, waterlogged);
    }


    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            meta |= 4;
        }

        if (state.getValue(WATERLOGGED)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(HALF, (meta & 4) == 0
                        ? BlockDoublePlant.EnumBlockHalf.LOWER : BlockDoublePlant.EnumBlockHalf.UPPER)
                .withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, net.minecraft.util.EnumHand hand) {
        return getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER)
                .withProperty(WATERLOGGED, WaterloggedBlockApi.containsWater(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isDripleafEnabled() && canPlantReplace(world, pos)
                && canPlantReplace(world, pos.up()) && canStayLower(world, pos,
                getDefaultState().withProperty(WATERLOGGED,
                        WaterloggedBlockApi.containsWater(world, pos)));
    }

    private static boolean canPlantReplace(World world, BlockPos pos) {
        return world.isAirBlock(pos) || WaterloggedBlockApi.containsWater(world, pos);
    }

    private static boolean canStayLower(World world, BlockPos pos, IBlockState lower) {
        IBlockState ground = world.getBlockState(pos.down());
        return DripleafPlacement.isSmallDripleafGround(ground)
                || lower.getValue(WATERLOGGED) && DripleafPlacement.isBigDripleafGround(ground);
    }

    private static boolean canStay(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            IBlockState lower = world.getBlockState(pos.down());
            return lower.getBlock() == state.getBlock()
                    && lower.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER;
        }
        IBlockState upper = world.getBlockState(pos.up());
        return upper.getBlock() == state.getBlock()
                && upper.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER
                && canStayLower(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
            boolean upperWaterlogged = WaterloggedBlockApi.containsWater(world, pos.up());
            world.setBlockState(pos.up(), getDefaultState()
                    .withProperty(FACING, state.getValue(FACING))
                    .withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER)
                    .withProperty(WATERLOGGED, upperWaterlogged), 3);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos, state)) {
            restoreBoth(world, pos, state);
            return;
        }
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canStay(world, pos, state)) {
            restoreBoth(world, pos, state);
            return;
        }
        if (isWaterlogged(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    private static void restoreBoth(World world, BlockPos pos, IBlockState state) {
        BlockPos lowerPos = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? pos.down() : pos;
        BlockPos upperPos = lowerPos.up();
        IBlockState lower = world.getBlockState(lowerPos);
        IBlockState upper = world.getBlockState(upperPos);
        if (lower.getBlock() instanceof BlockSmallDripleaf) {
            WaterloggedBlockApi.restoreFluid(world, lowerPos, lower, 2);
        }
        if (upper.getBlock() instanceof BlockSmallDripleaf) {
            WaterloggedBlockApi.restoreFluid(world, upperPos, upper, 3);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        restoreBoth(world, pos, state);
        return true;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        restoreBoth(world, pos, state);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isDripleafEnabled();
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return FFDItems.isDripleafEnabled();
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos lowerPos = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? pos.down() : pos;
        IBlockState lower = world.getBlockState(lowerPos);
        if (lower.getBlock() == this && lower.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
            DripleafPlacement.placeWithRandomHeight(world, random, lowerPos, lower.getValue(FACING));
        }
    }

    public static boolean isValidGround(IBlockState state) {
        return DripleafPlacement.isBigDripleafGround(state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (!world.isRemote && FFDItems.isDripleafEnabled() && tool.getItem() == Items.SHEARS) {
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.SMALL_DRIPLEAF));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.SMALL_DRIPLEAF);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
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
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || state.getValue(WATERLOGGED) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (state.getValue(WATERLOGGED)
                && world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    private static boolean isWaterloggedPair(IBlockAccess source, BlockPos pos, IBlockState state) {
        return isWaterlogged(state)
                || isWaterlogged(source.getBlockState(pos.down()))
                || isWaterlogged(source.getBlockState(pos.up()));
    }

    @Override
    public Vec3d getOffset(IBlockState state, IBlockAccess source, BlockPos pos) {
        return isWaterloggedPair(source, pos, state) ? Vec3d.ZERO : super.getOffset(state, source, pos);
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XYZ;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
