package xy177.farmersfuturedelight.common.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
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
import xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockGlowLichen extends Block implements IGrowable, IWaterloggableBlock {
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");

    private static final int FACE_MASK = 0x3F;
    private static final int WATER_BIT = 0x40;
    private static final AxisAlignedBB DOWN_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);
    private static final AxisAlignedBB UP_AABB =
            new AxisAlignedBB(0.0D, 0.9375D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB NORTH_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0625D);
    private static final AxisAlignedBB SOUTH_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.9375D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB WEST_AABB =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0625D, 1.0D, 1.0D);
    private static final AxisAlignedBB EAST_AABB =
            new AxisAlignedBB(0.9375D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private final int variantIndex;

    public BlockGlowLichen(String registryName, int variantIndex) {
        super(Material.VINE);
        this.variantIndex = variantIndex;
        setRegistryName(FarmerFutureDelight.MODID, registryName);
        setUnlocalizedName(FarmerFutureDelight.MODID + ".glow_lichen");
        setHardness(0.2F);
        setSoundType(SoundType.PLANT);
        setLightOpacity(0);
        setLightLevel(7.0F / 15.0F);
        setTickRandomly(true);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(DOWN, false)
                .withProperty(UP, false)
                .withProperty(NORTH, false)
                .withProperty(SOUTH, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, DOWN, UP, NORTH, SOUTH},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterlogged(state) ? Material.WATER : Material.VINE;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(DOWN)) {
            meta |= faceBit(EnumFacing.DOWN);
        }
        if (state.getValue(UP)) {
            meta |= faceBit(EnumFacing.UP);
        }
        if (state.getValue(NORTH)) {
            meta |= faceBit(EnumFacing.NORTH);
        }
        if (state.getValue(SOUTH)) {
            meta |= faceBit(EnumFacing.SOUTH);
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(DOWN, (meta & faceBit(EnumFacing.DOWN)) != 0)
                .withProperty(UP, (meta & faceBit(EnumFacing.UP)) != 0)
                .withProperty(NORTH, (meta & faceBit(EnumFacing.NORTH)) != 0)
                .withProperty(SOUTH, (meta & faceBit(EnumFacing.SOUTH)) != 0);
    }

    public static boolean isGlowLichen(IBlockState state) {
        return state.getBlock() instanceof BlockGlowLichen;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return isGlowLichen(state) && ((((BlockGlowLichen) state.getBlock()).variantIndex & 4) != 0);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return isWaterlogged(state);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return stateFor(getFaceMask(state), waterlogged);
    }

    public static int getFaceMask(IBlockState state) {
        if (!isGlowLichen(state)) {
            return 0;
        }
        BlockGlowLichen block = (BlockGlowLichen) state.getBlock();
        int mask = (block.variantIndex & 3) << 4;
        if (state.getValue(DOWN)) {
            mask |= faceBit(EnumFacing.DOWN);
        }
        if (state.getValue(UP)) {
            mask |= faceBit(EnumFacing.UP);
        }
        if (state.getValue(NORTH)) {
            mask |= faceBit(EnumFacing.NORTH);
        }
        if (state.getValue(SOUTH)) {
            mask |= faceBit(EnumFacing.SOUTH);
        }
        return mask;
    }

    public static boolean hasFace(IBlockState state, EnumFacing face) {
        return (getFaceMask(state) & faceBit(face)) != 0;
    }

    public static int faceBit(EnumFacing face) {
        return 1 << face.getIndex();
    }

    public static IBlockState stateFor(int faceMask, boolean waterlogged) {
        int normalizedMask = faceMask & FACE_MASK;
        if (normalizedMask == 0) {
            return waterlogged ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        }
        int packed = normalizedMask | (waterlogged ? WATER_BIT : 0);
        BlockGlowLichen block = FFDBlocks.GLOW_LICHEN_VARIANTS[packed >>> 4];
        return block.getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(DOWN, (normalizedMask & faceBit(EnumFacing.DOWN)) != 0)
                .withProperty(UP, (normalizedMask & faceBit(EnumFacing.UP)) != 0)
                .withProperty(NORTH, (normalizedMask & faceBit(EnumFacing.NORTH)) != 0)
                .withProperty(SOUTH, (normalizedMask & faceBit(EnumFacing.SOUTH)) != 0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        if (!FFDItems.isGlowLichenEnabled() || !canOccupy(world, pos)) {
            return false;
        }
        return findPlacementFace(world, pos, null) != null;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return FFDItems.isGlowLichenEnabled() && canOccupy(world, pos)
                && findPlacementFace(world, pos, side.getOpposite()) != null;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        IBlockState oldState = world.getBlockState(pos);
        EnumFacing face = findPlacementFace(world, pos, facing.getOpposite());
        if (face == null) {
            return oldState;
        }
        int mask = getFaceMask(oldState) | faceBit(face);
        boolean waterlogged = isGlowLichen(oldState)
                ? isWaterlogged(oldState) : WaterloggedBlockApi.containsWater(oldState);
        return stateFor(mask, waterlogged);
    }

    private static boolean canOccupy(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.AIR || isGlowLichen(state)
                || WaterloggedBlockApi.containsWater(state);
    }

    @Nullable
    private static EnumFacing findPlacementFace(IBlockAccess world, BlockPos pos,
                                                 @Nullable EnumFacing preferred) {
        IBlockState state = world.getBlockState(pos);
        int mask = getFaceMask(state);
        if (preferred != null && (mask & faceBit(preferred)) == 0
                && canAttachTo(world, pos, preferred)) {
            return preferred;
        }
        for (EnumFacing face : EnumFacing.values()) {
            if (face != preferred && (mask & faceBit(face)) == 0 && canAttachTo(world, pos, face)) {
                return face;
            }
        }
        return null;
    }

    public static boolean canAttachTo(IBlockAccess world, BlockPos pos, EnumFacing face) {
        BlockPos supportPos = pos.offset(face);
        IBlockState support = world.getBlockState(supportPos);
        EnumFacing supportFace = face.getOpposite();
        return support.isSideSolid(world, supportPos, supportFace)
                || support.getBlockFaceShape(world, supportPos, supportFace) == BlockFaceShape.SOLID;
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
        refreshSupport(world, pos);
        IBlockState current = world.getBlockState(pos);
        WaterloggedBlockApi.onNeighborChanged(world, pos, current.getBlock());
    }

    public static void refreshSupport(World world, BlockPos pos) {
        if (world.isRemote || !world.isBlockLoaded(pos)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (!isGlowLichen(state)) {
            return;
        }
        int oldMask = getFaceMask(state);
        int validMask = oldMask;
        for (EnumFacing face : EnumFacing.values()) {
            if ((validMask & faceBit(face)) == 0) {
                continue;
            }
            BlockPos supportPos = pos.offset(face);
            if (supportPos.getY() < FFDHeightHooks.minY(world)
                    || supportPos.getY() >= FFDHeightHooks.maxYExclusive(world)
                    || world.isBlockLoaded(supportPos) && !canAttachTo(world, pos, face)) {
                validMask &= ~faceBit(face);
            }
        }
        if (validMask == 0) {
            WaterloggedBlockApi.restoreFluid(world, pos, state, 2);
        } else if (validMask != oldMask) {
            IBlockState fluid = FFDStoredFluidStates.get(world, pos);
            if (world.setBlockState(pos, stateFor(validMask, isWaterlogged(state)), 2)
                    && fluid != null) {
                FFDStoredFluidStates.set(world, pos, fluid);
            }
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        refreshSupport(world, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        refreshSupport(world, pos);
        if (!world.isRemote && isWaterlogged(world.getBlockState(pos))) {
            WaterloggedBlockApi.updateTick(world, pos, world.getBlockState(pos));
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        onBlockHarvested(world, pos, state, player);
        return WaterloggedBlockApi.restoreFluid(world, pos, state, world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
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
        if (!world.isRemote && FFDItems.isGlowLichenEnabled() && tool.getItem() == Items.SHEARS) {
            spawnAsEntity(world, pos,
                    new ItemStack(FFDItems.GLOW_LICHEN, Integer.bitCount(getFaceMask(state))));
        }
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(FFDItems.GLOW_LICHEN);
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return canSpreadFromAnyFace(world, pos, state);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        spreadFromRandomFace(world, pos, state, random);
    }

    public static boolean spreadFromRandomFace(World world, BlockPos pos, IBlockState state, Random random) {
        List<EnumFacing> sourceFaces = presentFaces(state);
        Collections.shuffle(sourceFaces, random);
        for (EnumFacing sourceFace : sourceFaces) {
            List<EnumFacing> directions = allFaces();
            Collections.shuffle(directions, random);
            for (EnumFacing direction : directions) {
                if (direction.getAxis() == sourceFace.getAxis()) {
                    continue;
                }
                if (trySpread(world, pos, sourceFace, direction, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canSpreadFromAnyFace(IBlockAccess world, BlockPos pos, IBlockState state) {
        for (EnumFacing sourceFace : presentFaces(state)) {
            for (EnumFacing direction : EnumFacing.values()) {
                if (direction.getAxis() != sourceFace.getAxis()
                        && trySpread(world, pos, sourceFace, direction, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean trySpread(IBlockAccess world, BlockPos pos, EnumFacing sourceFace,
                                     EnumFacing direction, boolean place) {
        IBlockState sourceState = world.getBlockState(pos);
        if (direction.getAxis() == sourceFace.getAxis() || hasFace(sourceState, direction)) {
            return false;
        }
        if (tryPlaceFace(world, pos, direction, place)) {
            return true;
        }
        if (tryPlaceFace(world, pos.offset(direction), sourceFace, place)) {
            return true;
        }
        return tryPlaceFace(world, pos.offset(direction).offset(sourceFace),
                direction.getOpposite(), place);
    }

    private static boolean tryPlaceFace(IBlockAccess world, BlockPos pos, EnumFacing face,
                                        boolean place) {
        IBlockState oldState = world.getBlockState(pos);
        if (!canOccupy(world, pos) || hasFace(oldState, face) || !canAttachTo(world, pos, face)) {
            return false;
        }
        if (place && world instanceof World) {
            boolean waterlogged = isGlowLichen(oldState)
                    ? isWaterlogged(oldState) : WaterloggedBlockApi.containsWater(oldState);
            ((World) world).setBlockState(pos,
                    stateFor(getFaceMask(oldState) | faceBit(face), waterlogged), 2);
        }
        return true;
    }

    private static List<EnumFacing> presentFaces(IBlockState state) {
        List<EnumFacing> faces = new ArrayList<>();
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                faces.add(face);
            }
        }
        return faces;
    }

    private static List<EnumFacing> allFaces() {
        List<EnumFacing> faces = new ArrayList<>();
        Collections.addAll(faces, EnumFacing.values());
        return faces;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        int rotatedMask = 0;
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                rotatedMask |= faceBit(rotate(face, rotation));
            }
        }
        return stateFor(rotatedMask, isWaterlogged(state));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        int mirroredMask = 0;
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                mirroredMask |= faceBit(mirror(face, mirror));
            }
        }
        return stateFor(mirroredMask, isWaterlogged(state));
    }

    private static EnumFacing rotate(EnumFacing face, Rotation rotation) {
        if (!face.getAxis().isHorizontal()) {
            return face;
        }
        switch (rotation) {
            case CLOCKWISE_90:
                return face.rotateY();
            case CLOCKWISE_180:
                return face.getOpposite();
            case COUNTERCLOCKWISE_90:
                return face.rotateYCCW();
            default:
                return face;
        }
    }

    private static EnumFacing mirror(EnumFacing face, Mirror mirror) {
        if (mirror == Mirror.LEFT_RIGHT && face.getAxis() == EnumFacing.Axis.Z
                || mirror == Mirror.FRONT_BACK && face.getAxis() == EnumFacing.Axis.X) {
            return face.getOpposite();
        }
        return face;
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB result = null;
        for (EnumFacing face : EnumFacing.values()) {
            if (hasFace(state, face)) {
                AxisAlignedBB faceBox = boxFor(face);
                result = result == null ? faceBox : result.union(faceBox);
            }
        }
        return result == null ? FULL_BLOCK_AABB : result;
    }

    private static AxisAlignedBB boxFor(EnumFacing face) {
        switch (face) {
            case DOWN:
                return DOWN_AABB;
            case UP:
                return UP_AABB;
            case NORTH:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
            default:
                return EAST_AABB;
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterlogged(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (isWaterlogged(state)
                && world.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 15;
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
}
