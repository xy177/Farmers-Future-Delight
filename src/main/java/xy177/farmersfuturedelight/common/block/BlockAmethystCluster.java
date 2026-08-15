package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockAmethystCluster extends BlockAmethyst {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private final AxisAlignedBB[] shapes = new AxisAlignedBB[6];
    private final boolean mature;

    public BlockAmethystCluster(String name, float height, float width, int lightValue,
                                SoundType soundType, boolean mature) {
        super(name, soundType);
        this.mature = mature;
        setLightLevel(lightValue / 15.0F);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(WATERLOGGED, false));

        double halfWidth = width / 32.0D;
        double min = 0.5D - halfWidth;
        double max = 0.5D + halfWidth;
        double length = height / 16.0D;
        shapes[EnumFacing.UP.getIndex()] = new AxisAlignedBB(min, 0.0D, min, max, length, max);
        shapes[EnumFacing.DOWN.getIndex()] = new AxisAlignedBB(min, 1.0D - length, min, max, 1.0D, max);
        shapes[EnumFacing.NORTH.getIndex()] = new AxisAlignedBB(min, min, 1.0D - length, max, max, 1.0D);
        shapes[EnumFacing.SOUTH.getIndex()] = new AxisAlignedBB(min, min, 0.0D, max, max, length);
        shapes[EnumFacing.WEST.getIndex()] = new AxisAlignedBB(1.0D - length, min, min, 1.0D, max, max);
        shapes[EnumFacing.EAST.getIndex()] = new AxisAlignedBB(0.0D, min, min, length, max, max);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, FACING, WATERLOGGED},
                WaterloggedPlantFluid.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedPlantFluid.getExtendedState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(WATERLOGGED) ? 8 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, EnumFacing.getFront(meta & 7))
                .withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterlogged(state) ? Material.WATER : Material.ROCK;
    }

    public static boolean isWaterlogged(IBlockState state) {
        return state.getBlock() instanceof BlockAmethystCluster && state.getValue(WATERLOGGED);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, net.minecraft.util.EnumHand hand) {
        return getDefaultState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(FACING, facing)
                .withProperty(WATERLOGGED, WaterloggedPlantFluid.isSourceWater(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canAttach(world, pos, facing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return canAttach(world, pos, side);
    }

    private static boolean canAttach(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        BlockPos support = pos.offset(facing.getOpposite());
        return world.getBlockState(support).isSideSolid(world, support, facing);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedPlantFluid.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canAttach(world, pos, state.getValue(FACING))) {
            world.setBlockState(pos, replacementState(state), 3);
            return;
        }
        WaterloggedPlantFluid.onNeighborChanged(world, pos, this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && isWaterlogged(state)) {
            WaterloggedPlantFluid.updateTick(world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        return world.setBlockState(pos, replacementState(state), world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
        world.setBlockState(pos, replacementState(state), 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    private static IBlockState replacementState(IBlockState state) {
        return isWaterlogged(state) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        if (!mature || !FFDItems.isAmethystEnabled()) {
            return;
        }
        EntityPlayer player = harvesters.get();
        ItemStack tool = player == null ? ItemStack.EMPTY : player.getHeldItemMainhand();
        boolean pickaxe = !tool.isEmpty() && tool.getItem().getToolClasses(tool).contains("pickaxe");
        int count = pickaxe ? 4 : 2;
        if (pickaxe && fortune > 0) {
            Random random = world instanceof World ? ((World) world).rand : new Random();
            int multiplier = random.nextInt(fortune + 2) - 1;
            count *= Math.max(0, multiplier) + 1;
        }
        drops.add(FFDItems.effectiveStack(FFDItems.AMETHYST_SHARD, count));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return isWaterlogged(world.getBlockState(pos))
                ? Blocks.WATER.modifyAcceleration(world, pos, entity, motion) : motion;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return shapes[state.getValue(FACING).getIndex()];
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
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
