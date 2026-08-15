package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.BiomeDictionary;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockSeaPickle extends BlockBush implements IGrowable {
    public static final PropertyInteger PICKLES = PropertyInteger.create("pickles", 1, 4);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB[] PICKLE_AABBS = {
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.375D, 0.8125D),
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.375D, 0.875D),
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.4375D, 0.875D)
    };

    public BlockSeaPickle() {
        super(Material.WATER);
        setRegistryName(FarmerFutureDelight.MODID, "sea_pickle");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".sea_pickle");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(SoundType.SLIME);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(PICKLES, 1).withProperty(WATERLOGGED, true));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, PICKLES, WATERLOGGED},
                WaterloggedPlantFluid.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedPlantFluid.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Material.WATER : Material.PLANTS;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockSeaPickle && state.getValue(WATERLOGGED);
    }

    public IBlockState getPlacementState(World world, BlockPos pos) {
        boolean waterlogged = WaterloggedPlantFluid.isSourceWater(world, pos);
        return getDefaultState().withProperty(WATERLOGGED, waterlogged).withProperty(PICKLES, 1);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getPlacementState(world, pos);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState current = world.getBlockState(pos);
        return FFDItems.isSeaPickleEnabled()
                && (current.getMaterial() == Material.WATER || current.getBlock().isReplaceable(world, pos))
                && canStayOn(world, pos.down());
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canStayOn(world, pos.down());
    }

    private boolean canStayOn(World world, BlockPos supportPos) {
        IBlockState support = world.getBlockState(supportPos);
        return support.isSideSolid(world, supportPos, EnumFacing.UP)
                || support.getBlock().canPlaceTorchOnTop(support, world, supportPos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        if (world.getBlockState(pos).getBlock() == this) {
            WaterloggedPlantFluid.onNeighborChanged(world, pos, this);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedPlantFluid.onBlockAdded(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (world.getBlockState(pos).getBlock() == this && isWaterlogged(state)) {
            WaterloggedPlantFluid.updateTick(world, pos, state);
        }
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return isWaterlogged(world.getBlockState(pos))
                ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion)
                : motion;
    }

    @Override
    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStay(world, pos, state)) {
            if (FFDItems.isSeaPickleEnabled()) {
                dropBlockAsItem(world, pos, state, 0);
            }
            world.setBlockState(pos, replacementState(state), 3);
        }
    }

    private IBlockState replacementState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, replacementState(state), world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, replacementState(state), 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return PICKLE_AABBS[state.getValue(PICKLES) - 1];
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 3 + 3 * state.getValue(PICKLES) : 0;
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return FFDItems.isSeaPickleEnabled() ? FFDItems.SEA_PICKLE : Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return FFDItems.isSeaPickleEnabled() ? state.getValue(PICKLES) : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (FFDItems.isSeaPickleEnabled()) {
            drops.add(FFDItems.effectiveStack(FFDItems.SEA_PICKLE,
                    state.getValue(PICKLES)));
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return FFDItems.isSeaPickleEnabled() && state.getValue(WATERLOGGED)
                && isValidSeaPickleBed(world, pos);
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return FFDItems.isSeaPickleEnabled();
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int zSpan = 1;
        int zOffset = 0;
        int xStart = pos.getX() - 2;
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < zSpan; z++) {
                for (int y = pos.getY() - 1; y <= pos.getY(); y++) {
                    BlockPos candidate = new BlockPos(xStart + x, y, pos.getZ() - zOffset + z);
                    if (candidate.equals(pos) || rand.nextInt(FFDConfig.seaPickleSpreadChanceRoll) != 0
                            || world.getBlockState(candidate).getBlock() != Blocks.WATER
                            || !isValidSeaPickleBed(world, candidate)) {
                        continue;
                    }
                    world.setBlockState(candidate, getDefaultState()
                            .withProperty(PICKLES, rand.nextInt(4) + 1)
                            .withProperty(WATERLOGGED, true), 3);
                }
            }
            if (x < 2) {
                zSpan += 2;
                zOffset++;
            } else {
                zSpan -= 2;
                zOffset--;
            }
        }
        world.setBlockState(pos, state.withProperty(PICKLES, 4), 2);
    }

    public static boolean isValidSeaPickleBed(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        if (!isSeaPickleBiome(biome)) {
            return false;
        }
        IBlockState support = world.getBlockState(pos.down());
        return support.isSideSolid(world, pos.down(), EnumFacing.UP)
                || support.getBlock().canPlaceTorchOnTop(support, world, pos.down());
    }

    public static boolean isSeaPickleBiome(Biome biome) {
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
                || biome.getDefaultTemperature() < 0.5F) {
            return false;
        }
        if (biome.getRegistryName() != null) {
            String path = biome.getRegistryName().getResourcePath();
            if (path.contains("cold") || path.contains("frozen")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(PICKLES) - 1;
        return state.getValue(WATERLOGGED) ? meta | 4 : meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(PICKLES, (meta & 3) + 1)
                .withProperty(WATERLOGGED, (meta & 4) != 0);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.SEA_PICKLE);
    }
}
