package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

public class BlockCoralPlant extends Block implements IWaterloggableBlock {
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB CORAL_AABB =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.9375D, 0.875D);
    private static final AxisAlignedBB FAN_AABB =
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

    private final int coralIndex;
    private final boolean fan;
    private final BlockCoralPlant deadVariant;

    public BlockCoralPlant(String name, int coralIndex, boolean fan,
                           BlockCoralPlant deadVariant) {
        super(Material.ROCK, mapColor(coralIndex, deadVariant != null));
        this.coralIndex = coralIndex;
        this.fan = fan;
        this.deadVariant = deadVariant;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(deadVariant == null ? SoundType.STONE : FFDSounds.WET_GRASS);
        if (deadVariant == null) {
            setHarvestLevel("pickaxe", 0);
        }
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, true));
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

    public boolean isFan() {
        return fan;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {
                        BlockLiquid.LEVEL, WATERLOGGED},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WATERLOGGED) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, (meta & 1) != 0);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return isWaterloggedState(state) ? Material.WATER : Material.ROCK;
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return state.getBlock() == this && state.getValue(WATERLOGGED);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return state.withProperty(WATERLOGGED, waterlogged);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(BlockLiquid.LEVEL, 0)
                .withProperty(WATERLOGGED, WaterloggedBlockApi.isWaterSource(world, pos));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        BlockPos support = pos.down();
        return world.getBlockState(support).isSideSolid(world, support, EnumFacing.UP);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP && canPlaceBlockAt(world, pos);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
        scheduleDeath(world, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canPlaceBlockAt(world, pos)) {
            dropBlockAsItem(world, pos, state, 0);
            WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
            return;
        }
        WaterloggedBlockApi.onNeighborChanged(world, pos, this);
        scheduleDeath(world, pos, state);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || world.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (isAlive() && !hasWater(world, pos, state)) {
            world.setBlockState(pos, deadVariant.getDefaultState()
                    .withProperty(BlockLiquid.LEVEL, 0)
                    .withProperty(WATERLOGGED, false), 2);
            return;
        }
        if (isWaterloggedState(state)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
        }
    }

    public void scheduleDeath(World world, BlockPos pos) {
        if (!world.isRemote) {
            scheduleDeath(world, pos, world.getBlockState(pos));
        }
    }

    private void scheduleDeath(World world, BlockPos pos, IBlockState state) {
        if (isAlive() && state.getBlock() == this && !hasWater(world, pos, state)) {
            world.scheduleUpdate(pos, this, 60 + world.rand.nextInt(40));
        }
    }

    private static boolean hasWater(IBlockAccess world, BlockPos pos, IBlockState state) {
        if (WaterloggedBlockApi.isWaterlogged(state)) {
            return true;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (WaterloggedBlockApi.containsWater(world, pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                   EntityPlayer player, boolean willHarvest) {
        return WaterloggedBlockApi.restoreFluid(world, pos, state, world.isRemote ? 11 : 3);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos,
                                net.minecraft.world.Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
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
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state,
                                  EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = fan
                ? (isAlive() ? FFDItems.CORAL_FAN_ITEMS[coralIndex]
                        : FFDItems.DEAD_CORAL_FAN_ITEMS[coralIndex])
                : (isAlive() ? FFDItems.CORAL_ITEMS[coralIndex]
                        : FFDItems.DEAD_CORAL_ITEMS[coralIndex]);
        return FFDItems.effectiveStack(item);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return getSilkTouchDrop(state);
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return fan ? FAN_AABB : CORAL_AABB;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world,
                                                  BlockPos pos) {
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

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT
                || isWaterloggedState(state) && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (isWaterloggedState(state)
                && WaterloggedBlockApi.containsWater(world, pos.offset(side))) {
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
        return isAlive() ? EnumPushReaction.DESTROY : EnumPushReaction.NORMAL;
    }
}
