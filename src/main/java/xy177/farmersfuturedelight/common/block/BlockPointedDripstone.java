package xy177.farmersfuturedelight.common.block;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.worldgen.FFDModernStoneProvider;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

public class BlockPointedDripstone extends BlockFalling {
    public enum Thickness implements IStringSerializable {
        TIP_MERGE,
        TIP,
        FRUSTUM,
        MIDDLE,
        BASE;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final PropertyDirection VERTICAL_DIRECTION = PropertyDirection.create(
            "vertical_direction", new Predicate<EnumFacing>() {
                @Override
                public boolean apply(@Nullable EnumFacing facing) {
                    return facing != null && facing.getAxis() == EnumFacing.Axis.Y;
                }
            });
    public static final PropertyEnum<Thickness> THICKNESS =
            PropertyEnum.create("thickness", Thickness.class);

    private static final int MAX_DRIP_SEARCH = 11;
    private static final int MAX_GROWTH_LENGTH = 7;
    private static final int MAX_STALAGMITE_SEARCH = 10;
    private static final AxisAlignedBB TIP_MERGE_SHAPE = column(6.0D, 0.0D, 16.0D);
    private static final AxisAlignedBB TIP_UP_SHAPE = column(6.0D, 0.0D, 11.0D);
    private static final AxisAlignedBB TIP_DOWN_SHAPE = column(6.0D, 5.0D, 16.0D);
    private static final AxisAlignedBB FRUSTUM_SHAPE = column(8.0D, 0.0D, 16.0D);
    private static final AxisAlignedBB MIDDLE_SHAPE = column(10.0D, 0.0D, 16.0D);
    private static final AxisAlignedBB BASE_SHAPE = column(12.0D, 0.0D, 16.0D);
    private static final AxisAlignedBB DRIP_CLEARANCE = column(4.0D, 0.0D, 16.0D);
    private static final double MAX_HORIZONTAL_OFFSET = BASE_SHAPE.minX;

    public BlockPointedDripstone() {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, "pointed_dripstone");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".pointed_dripstone");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(1.5F);
        setResistance(1.0F);
        setSoundType(FFDSounds.POINTED_DRIPSTONE);
        setTickRandomly(true);
        setLightOpacity(0);
        setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState()
                .withProperty(VERTICAL_DIRECTION, EnumFacing.UP)
                .withProperty(THICKNESS, Thickness.TIP));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VERTICAL_DIRECTION, THICKNESS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int direction = state.getValue(VERTICAL_DIRECTION) == EnumFacing.DOWN ? 1 : 0;
        return state.getValue(THICKNESS).ordinal() * 2 + direction;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int thickness = Math.max(0, Math.min(Thickness.values().length - 1, meta / 2));
        return getDefaultState()
                .withProperty(VERTICAL_DIRECTION, (meta & 1) == 0 ? EnumFacing.UP : EnumFacing.DOWN)
                .withProperty(THICKNESS, Thickness.values()[thickness]);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        EnumFacing direction = chooseDirection(world, pos, facing, placer);
        if (direction == null) {
            direction = EnumFacing.UP;
        }
        return getDefaultState()
                .withProperty(VERTICAL_DIRECTION, direction)
                .withProperty(THICKNESS,
                        calculateThickness(world, pos, direction, !placer.isSneaking()));
    }

    @Nullable
    private EnumFacing chooseDirection(IBlockAccess world, BlockPos pos, EnumFacing clickedFace,
                                       EntityLivingBase placer) {
        EnumFacing preferred = clickedFace.getAxis() == EnumFacing.Axis.Y
                ? clickedFace
                : placer.rotationPitch < 0.0F ? EnumFacing.DOWN : EnumFacing.UP;
        if (isValidPlacement(world, pos, preferred)) {
            return preferred;
        }
        EnumFacing opposite = preferred.getOpposite();
        return isValidPlacement(world, pos, opposite) ? opposite : null;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isValidPlacement(world, pos, EnumFacing.UP)
                || isValidPlacement(world, pos, EnumFacing.DOWN);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return side.getAxis() == EnumFacing.Axis.Y && isValidPlacement(world, pos, side)
                || canPlaceBlockAt(world, pos);
    }

    private boolean isValidPlacement(IBlockAccess world, BlockPos pos, EnumFacing direction) {
        BlockPos supportPos = pos.offset(direction.getOpposite());
        IBlockState support = world.getBlockState(supportPos);
        return support.isSideSolid(world, supportPos, direction)
                || isPointedWithDirection(support, direction);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            refreshState(world, pos);
            refreshState(world, pos.up());
            refreshState(world, pos.down());
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (!world.isRemote) {
            refreshState(world, pos.up());
            refreshState(world, pos.down());
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        EnumFacing direction = state.getValue(VERTICAL_DIRECTION);
        if (!isValidPlacement(world, pos, direction)) {
            world.scheduleUpdate(pos, this, direction == EnumFacing.DOWN ? 2 : 1);
            return;
        }
        refreshState(world, pos);
    }

    private void refreshState(World world, BlockPos pos) {
        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() != this) {
            return;
        }
        EnumFacing direction = current.getValue(VERTICAL_DIRECTION);
        Thickness thickness = calculateThickness(world, pos, direction,
                current.getValue(THICKNESS) == Thickness.TIP_MERGE);
        if (current.getValue(THICKNESS) != thickness) {
            world.setBlockState(pos, current.withProperty(THICKNESS, thickness), 2);
        }
    }

    private Thickness calculateThickness(IBlockAccess world, BlockPos pos,
                                         EnumFacing direction, boolean mergeOpposingTips) {
        IBlockState inFront = world.getBlockState(pos.offset(direction));
        if (isPointedWithDirection(inFront, direction.getOpposite())) {
            return mergeOpposingTips || inFront.getValue(THICKNESS) == Thickness.TIP_MERGE
                    ? Thickness.TIP_MERGE : Thickness.TIP;
        }
        if (!isPointedWithDirection(inFront, direction)) {
            return Thickness.TIP;
        }
        Thickness frontThickness = inFront.getValue(THICKNESS);
        if (frontThickness == Thickness.TIP || frontThickness == Thickness.TIP_MERGE) {
            return Thickness.FRUSTUM;
        }
        IBlockState behind = world.getBlockState(pos.offset(direction.getOpposite()));
        return isPointedWithDirection(behind, direction) ? Thickness.MIDDLE : Thickness.BASE;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        state = world.getBlockState(pos);
        EnumFacing direction = state.getValue(VERTICAL_DIRECTION);
        if (!isValidPlacement(world, pos, direction)) {
            if (direction == EnumFacing.UP) {
                world.destroyBlock(pos, true);
            } else {
                spawnFallingStalactite(world, pos);
            }
            return;
        }
        if (isFreeHangingStalactite(state)) {
            fillCauldronFromTip(world, pos);
        }
    }

    private void spawnFallingStalactite(World world, BlockPos start) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(start);
        List<BlockPos> positions = new ArrayList<>();
        List<IBlockState> states = new ArrayList<>();
        while (isPointedWithDirection(world.getBlockState(cursor), EnumFacing.DOWN)) {
            positions.add(cursor.toImmutable());
            states.add(world.getBlockState(cursor));
            cursor.move(EnumFacing.DOWN);
        }
        int size = states.size();
        for (BlockPos pos : positions) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }
        for (int index = 0; index < size; index++) {
            BlockPos pos = positions.get(index);
            IBlockState fallingState = states.get(index);
            EntityFallingBlock falling = new EntityFallingBlock(world,
                    pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, fallingState);
            falling.fallTime = 1;
            if (index == size - 1) {
                configureFallingDamage(falling, Math.max(1, size));
            }
            world.spawnEntity(falling);
        }
        for (BlockPos pos : positions) {
            world.notifyNeighborsOfStateChange(pos, this, false);
        }
    }

    private static void configureFallingDamage(EntityFallingBlock falling, int size) {
        NBTTagCompound data = falling.writeToNBT(new NBTTagCompound());
        data.setBoolean("HurtEntities", true);
        data.setFloat("FallHurtAmount", Math.max(6, size));
        data.setInteger("FallHurtMax", 40);
        falling.readFromNBT(data);
    }

    @Override
    public void onEndFalling(World world, BlockPos pos, IBlockState fallingState,
                             IBlockState hitState) {
        EnumFacing direction = fallingState.getValue(VERTICAL_DIRECTION);
        if (isValidPlacement(world, pos, direction)) {
            return;
        }
        world.setBlockToAir(pos);
        if (!world.getGameRules().getBoolean("doEntityDrops")) {
            return;
        }
        ItemStack drop = new ItemStack(Item.getItemFromBlock(this), 1,
                damageDropped(fallingState));
        if (!drop.isEmpty()) {
            EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5D,
                    pos.getY() + 0.5D, pos.getZ() + 0.5D, drop);
            entityItem.setDefaultPickupDelay();
            world.spawnEntity(entityItem);
        }
        world.playSound(null, pos, FFDSounds.POINTED_DRIPSTONE_LAND,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !isStalactiteStart(state, world, pos)) {
            return;
        }
        float transferRoll = random.nextFloat();
        maybeScheduleFluidTransfer(world, pos, state, transferRoll);
        if (random.nextFloat() < FFDConfig.dripstoneGrowthChance) {
            growStalactiteOrStalagmite(world, pos, state, random);
        }
    }

    private void maybeScheduleFluidTransfer(World world, BlockPos start, IBlockState state,
                                            float roll) {
        if (!FFDConfig.modernCauldronFeatures) {
            return;
        }
        FluidKind fluid = getFluidAboveStalactite(world, start);
        float chance = fluid == FluidKind.WATER ? FFDConfig.dripstoneWaterTransferChance
                : fluid == FluidKind.LAVA ? FFDConfig.dripstoneLavaTransferChance : 0.0F;
        if (roll >= chance) {
            return;
        }
        BlockPos tip = findTip(world, start, state.getValue(VERTICAL_DIRECTION),
                MAX_DRIP_SEARCH, false);
        if (tip == null) {
            return;
        }
        BlockPos cauldron = findFillableCauldronBelow(world, tip, fluid);
        if (cauldron == null) {
            return;
        }
        spawnDripParticle(world, tip, fluid);
        world.scheduleUpdate(tip, this, 50 + tip.getY() - cauldron.getY());
    }

    private void fillCauldronFromTip(World world, BlockPos tip) {
        if (!FFDConfig.modernCauldronFeatures) {
            return;
        }
        FluidKind fluid = getFluidAboveStalactite(world, tip);
        if (fluid == FluidKind.NONE) {
            return;
        }
        BlockPos cauldron = findFillableCauldronBelow(world, tip, fluid);
        if (cauldron == null) {
            return;
        }
        IBlockState state = world.getBlockState(cauldron);
        if (state.getBlock() == Blocks.CAULDRON) {
            int level = state.getValue(BlockCauldron.LEVEL);
            if (fluid == FluidKind.WATER && level < 3) {
                world.setBlockState(cauldron,
                        state.withProperty(BlockCauldron.LEVEL, level + 1), 3);
            } else if (fluid == FluidKind.LAVA && level == 0) {
                world.setBlockState(cauldron, FFDBlocks.LAVA_CAULDRON.getDefaultState(), 3);
            } else {
                return;
            }
        } else if (state.getBlock() instanceof BlockFutureCauldron) {
            if (!((BlockFutureCauldron) state.getBlock()).receiveDrip(world, cauldron, state,
                    fluid == FluidKind.WATER)) {
                return;
            }
        } else {
            return;
        }
        world.updateComparatorOutputLevel(cauldron, world.getBlockState(cauldron).getBlock());
        world.playSound(null, cauldron,
                fluid == FluidKind.WATER ? FFDSounds.POINTED_DRIPSTONE_DRIP_WATER_CAULDRON
                        : FFDSounds.POINTED_DRIPSTONE_DRIP_LAVA_CAULDRON,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Nullable
    private BlockPos findFillableCauldronBelow(World world, BlockPos tip, FluidKind fluid) {
        for (int distance = 1; distance < MAX_DRIP_SEARCH; distance++) {
            BlockPos current = tip.down(distance);
            IBlockState state = world.getBlockState(current);
            if (state.getBlock() == Blocks.CAULDRON) {
                int level = state.getValue(BlockCauldron.LEVEL);
                if (fluid == FluidKind.WATER && level < 3
                        || fluid == FluidKind.LAVA && level == 0) {
                    return current;
                }
                return null;
            }
            if (state.getBlock() instanceof BlockFutureCauldron) {
                return ((BlockFutureCauldron) state.getBlock()).canReceiveDrip(state,
                        fluid == FluidKind.WATER) ? current : null;
            }
            if (!canDripThrough(world, current, state)) {
                return null;
            }
        }
        return null;
    }

    private static boolean canDripThrough(IBlockAccess world, BlockPos pos, IBlockState state) {
        if (state.getMaterial() == Material.AIR) {
            return true;
        }
        if (state.getMaterial().isLiquid()) {
            return false;
        }
        AxisAlignedBB collision = state.getCollisionBoundingBox(world, pos);
        return collision == null || !collision.intersects(DRIP_CLEARANCE);
    }

    private FluidKind getFluidAboveStalactite(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (!isPointedWithDirection(state, EnumFacing.DOWN)) {
            return FluidKind.NONE;
        }
        BlockPos cursor = pos;
        for (int distance = 0; distance < MAX_DRIP_SEARCH; distance++) {
            BlockPos above = cursor.up();
            if (isPointedWithDirection(world.getBlockState(above), EnumFacing.DOWN)) {
                cursor = above;
                continue;
            }
            IBlockState fluidState = world.getBlockState(above.up());
            if (fluidState.getBlock() == Blocks.WATER
                    || fluidState.getBlock() == Blocks.FLOWING_WATER) {
                return FluidKind.WATER;
            }
            if (fluidState.getBlock() == Blocks.LAVA
                    || fluidState.getBlock() == Blocks.FLOWING_LAVA) {
                return FluidKind.LAVA;
            }
            return FluidKind.NONE;
        }
        return FluidKind.NONE;
    }

    private boolean canGrow(World world, BlockPos start) {
        if (!FFDModernStoneProvider.get().isDripstoneBlock(world.getBlockState(start.up()))) {
            return false;
        }
        IBlockState source = world.getBlockState(start.up(2));
        return (source.getBlock() == Blocks.WATER || source.getBlock() == Blocks.FLOWING_WATER)
                && source.getBlock().getMetaFromState(source) == 0;
    }

    private void growStalactiteOrStalagmite(World world, BlockPos start, IBlockState startState,
                                            Random random) {
        if (!canGrow(world, start)) {
            return;
        }
        BlockPos tip = findTip(world, start, startState.getValue(VERTICAL_DIRECTION),
                MAX_GROWTH_LENGTH, false);
        if (tip == null) {
            return;
        }
        IBlockState tipState = world.getBlockState(tip);
        if (!isFreeHangingStalactite(tipState) || !canTipGrow(world, tip, EnumFacing.DOWN)) {
            return;
        }
        if (random.nextBoolean()) {
            grow(world, tip, EnumFacing.DOWN);
        } else {
            growStalagmiteBelow(world, tip);
        }
    }

    @Nullable
    private BlockPos findTip(World world, BlockPos start, EnumFacing direction,
                             int maxLength, boolean includeMerged) {
        BlockPos cursor = start;
        for (int step = 0; step < maxLength; step++) {
            IBlockState state = world.getBlockState(cursor);
            Thickness thickness = state.getBlock() == this ? state.getValue(THICKNESS) : null;
            if (thickness == Thickness.TIP
                    || includeMerged && thickness == Thickness.TIP_MERGE) {
                return cursor;
            }
            BlockPos next = cursor.offset(direction);
            if (!isPointedWithDirection(world.getBlockState(next), direction)) {
                return null;
            }
            cursor = next;
        }
        return null;
    }

    private boolean canTipGrow(World world, BlockPos tip, EnumFacing direction) {
        IBlockState target = world.getBlockState(tip.offset(direction));
        return target.getMaterial() == Material.AIR
                || isUnmergedTipWithDirection(target, direction.getOpposite());
    }

    private void grow(World world, BlockPos from, EnumFacing direction) {
        BlockPos target = from.offset(direction);
        IBlockState existing = world.getBlockState(target);
        if (isUnmergedTipWithDirection(existing, direction.getOpposite())) {
            world.setBlockState(from, world.getBlockState(from)
                    .withProperty(THICKNESS, Thickness.TIP_MERGE), 2);
            world.setBlockState(target, existing.withProperty(THICKNESS, Thickness.TIP_MERGE), 2);
        } else if (existing.getMaterial() == Material.AIR) {
            world.setBlockState(target, getDefaultState()
                    .withProperty(VERTICAL_DIRECTION, direction)
                    .withProperty(THICKNESS, Thickness.TIP), 3);
            refreshState(world, from);
        }
    }

    private void growStalagmiteBelow(World world, BlockPos stalactiteTip) {
        for (int distance = 1; distance <= MAX_STALAGMITE_SEARCH; distance++) {
            BlockPos current = stalactiteTip.down(distance);
            IBlockState state = world.getBlockState(current);
            if (state.getMaterial().isLiquid()) {
                return;
            }
            if (isUnmergedTipWithDirection(state, EnumFacing.UP)
                    && canTipGrow(world, current, EnumFacing.UP)) {
                grow(world, current, EnumFacing.UP);
                return;
            }
            if (state.getMaterial() == Material.AIR
                    && isValidPlacement(world, current, EnumFacing.UP)
                    && world.getBlockState(current.down()).getMaterial() != Material.WATER) {
                world.setBlockState(current, getDefaultState()
                        .withProperty(VERTICAL_DIRECTION, EnumFacing.UP)
                        .withProperty(THICKNESS, Thickness.TIP), 3);
                return;
            }
            if (!canDripThrough(world, current, state)) {
                return;
            }
        }
    }

    private boolean isUnmergedTipWithDirection(IBlockState state, EnumFacing direction) {
        return isPointedWithDirection(state, direction)
                && state.getValue(THICKNESS) == Thickness.TIP;
    }

    private static boolean isStalactiteStart(IBlockState state, IBlockAccess world, BlockPos pos) {
        return isPointedWithDirection(state, EnumFacing.DOWN)
                && !isPointedWithDirection(world.getBlockState(pos.up()), EnumFacing.DOWN);
    }

    public static boolean isFreeHangingStalactite(IBlockState state) {
        return isPointedWithDirection(state, EnumFacing.DOWN)
                && state.getValue(THICKNESS) == Thickness.TIP;
    }

    public static boolean isPointedWithDirection(IBlockState state, EnumFacing direction) {
        return state.getBlock() instanceof BlockPointedDripstone
                && state.getValue(VERTICAL_DIRECTION) == direction;
    }

    private void spawnDripParticle(World world, BlockPos tip, FluidKind fluid) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        IBlockState state = world.getBlockState(tip);
        Vec3d offset = getOffset(state, world, tip);
        FFDNetwork.sendDripstoneParticle((WorldServer) world,
                tip.getX() + 0.5D + offset.x, tip.getY() + 0.25D,
                tip.getZ() + 0.5D + offset.z, fluid == FluidKind.LAVA);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!isFreeHangingStalactite(state)) {
            return;
        }
        float roll = random.nextFloat();
        if (roll > 0.12F) {
            return;
        }
        FluidKind fluid = getFluidAboveStalactite(world, pos);
        if (fluid != FluidKind.NONE) {
            Vec3d offset = getOffset(state, world, pos);
            FarmerFutureDelight.proxy.spawnDripstoneParticle(world,
                    pos.getX() + 0.5D + offset.x, pos.getY() + 0.25D,
                    pos.getZ() + 0.5D + offset.z, fluid == FluidKind.LAVA);
        }
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this
                && state.getValue(VERTICAL_DIRECTION) == EnumFacing.UP
                && state.getValue(THICKNESS) == Thickness.TIP) {
            entity.fall(fallDistance + 2.5F, 2.0F);
        } else {
            super.onFallenUpon(world, pos, entity, fallDistance);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB shape;
        switch (state.getValue(THICKNESS)) {
            case TIP_MERGE:
                shape = TIP_MERGE_SHAPE;
                break;
            case TIP:
                shape = state.getValue(VERTICAL_DIRECTION) == EnumFacing.DOWN
                        ? TIP_DOWN_SHAPE : TIP_UP_SHAPE;
                break;
            case FRUSTUM:
                shape = FRUSTUM_SHAPE;
                break;
            case MIDDLE:
                shape = MIDDLE_SHAPE;
                break;
            case BASE:
            default:
                shape = BASE_SHAPE;
                break;
        }
        Vec3d offset = getOffset(state, source, pos);
        return shape.offset(offset.x, 0.0D, offset.z);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public Vec3d getOffset(IBlockState state, IBlockAccess world, BlockPos pos) {
        long seed = MathHelper.getCoordinateRandom(pos.getX(), 0, pos.getZ());
        double x = clamp((((double) (seed & 15L) / 15.0D) - 0.5D) * 0.5D,
                -MAX_HORIZONTAL_OFFSET, MAX_HORIZONTAL_OFFSET);
        double z = clamp((((double) (seed >> 8 & 15L) / 15.0D) - 0.5D) * 0.5D,
                -MAX_HORIZONTAL_OFFSET, MAX_HORIZONTAL_OFFSET);
        return new Vec3d(x, 0.0D, z);
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

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    private static AxisAlignedBB column(double width, double minY, double maxY) {
        double inset = (16.0D - width) / 32.0D;
        return new AxisAlignedBB(inset, minY / 16.0D, inset,
                1.0D - inset, maxY / 16.0D, 1.0D - inset);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum FluidKind {
        NONE,
        WATER,
        LAVA
    }
}
