package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDBubbleColumnEvents;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class BlockBubbleColumn extends Block implements IWaterloggableBlock {
    public static final PropertyBool DRAG = PropertyBool.create("drag");

    public BlockBubbleColumn() {
        super(Material.WATER);
        setRegistryName(FarmerFutureDelight.MODID, "bubble_column");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".bubble_column");
        setHardness(100.0F);
        setResistance(500.0F);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(DRAG, true));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new net.minecraft.block.properties.IProperty<?>[] {DRAG},
                WaterloggedBlockApi.extendedProperties());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DRAG) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DRAG, (meta & 1) != 0);
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return state.getBlock() == this;
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return waterlogged ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, tickRate(world));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (!world.isRemote) {
            world.scheduleUpdate(pos, this, tickRate(world));
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        refreshColumn(world, pos);
    }

    @Override
    public int tickRate(World world) {
        return 5;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state,
                                          Entity entity) {
        boolean drag = state.getValue(DRAG);
        boolean surface = isOpenAbove(world, pos);
        if (entity instanceof EntityBoat && surface) {
            FFDBubbleColumnEvents.touchBoat((EntityBoat) entity, drag);
            return;
        }
        if (surface) {
            entity.motionY = drag ? Math.max(-0.9D, entity.motionY - 0.03D)
                    : Math.min(1.8D, entity.motionY + 0.1D);
            if (world.isRemote) {
                spawnSurfaceParticles(world, pos);
            }
        } else {
            entity.motionY = drag ? Math.max(-0.3D, entity.motionY - 0.03D)
                    : Math.min(0.7D, entity.motionY + 0.06D);
            entity.fallDistance = 0.0F;
        }
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (state.getValue(DRAG)) {
            FarmerFutureDelight.proxy.spawnBubbleColumnParticle(world,
                    x + 0.5D, y + 0.8D, z, 0.0D, 0.0D, 0.0D, true);
            if (random.nextInt(200) == 0) {
                world.playSound(x, y, z, FFDSounds.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                        SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F,
                        0.9F + random.nextFloat() * 0.15F, false);
            }
        } else {
            FarmerFutureDelight.proxy.spawnBubbleColumnParticle(world,
                    x + 0.5D, y, z + 0.5D, 0.0D, 0.04D, 0.0D, false);
            FarmerFutureDelight.proxy.spawnBubbleColumnParticle(world,
                    x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(),
                    0.0D, 0.04D, 0.0D, false);
            if (random.nextInt(200) == 0) {
                world.playSound(x, y, z, FFDSounds.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                        SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F,
                        0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    @Override
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d motion) {
        return WaterloggedBlockApi.modifyAcceleration(world, pos, entity, motion);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world,
                                                  BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return hitIfLiquid;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
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

    public static void refreshFromChange(World world, BlockPos pos) {
        if (world == null || world.isRemote || !FFDItems.isBlockRegistered(FFDBlocks.BUBBLE_COLUMN)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (isActivator(state)) {
            refreshColumn(world, pos.up());
        } else {
            refreshColumn(world, pos);
            refreshColumn(world, pos.up());
        }
    }

    public static void refreshColumn(World world, BlockPos start) {
        if (world == null || world.isRemote || !FFDItems.isBlockRegistered(FFDBlocks.BUBBLE_COLUMN)
                || !world.isBlockLoaded(start)) {
            return;
        }
        Boolean drag = directionFromBelow(world.getBlockState(start.down()));
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos(start);
        for (int checked = 0; checked < 1024 && world.isBlockLoaded(current); checked++) {
            IBlockState state = world.getBlockState(current);
            if (drag == null) {
                if (state.getBlock() != FFDBlocks.BUBBLE_COLUMN) {
                    break;
                }
                world.setBlockState(current, Blocks.WATER.getDefaultState(), 2);
                current.setY(current.getY() + 1);
                continue;
            }
            if (state.getBlock() == FFDBlocks.BUBBLE_COLUMN) {
                if (state.getValue(DRAG) != drag) {
                    world.setBlockState(current, state.withProperty(DRAG, drag), 2);
                }
            } else if (isVanillaWaterSource(state)) {
                world.setBlockState(current, FFDBlocks.BUBBLE_COLUMN.getDefaultState()
                        .withProperty(DRAG, drag), 2);
            } else {
                break;
            }
            current.setY(current.getY() + 1);
        }
    }

    private static Boolean directionFromBelow(IBlockState state) {
        if (state.getBlock() == FFDBlocks.BUBBLE_COLUMN) {
            return state.getValue(DRAG);
        }
        if (state.getBlock() == Blocks.SOUL_SAND) {
            return Boolean.FALSE;
        }
        if (state.getBlock() == Blocks.MAGMA) {
            return Boolean.TRUE;
        }
        return null;
    }

    private static boolean isActivator(IBlockState state) {
        return state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.MAGMA;
    }

    private static boolean isVanillaWaterSource(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)
                && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static boolean isOpenAbove(World world, BlockPos pos) {
        BlockPos abovePos = pos.up();
        IBlockState above = world.getBlockState(abovePos);
        return above.getCollisionBoundingBox(world, abovePos) == NULL_AABB
                && !above.getMaterial().isLiquid()
                && !WaterloggedBlockApi.containsWater(above);
    }

    private static void spawnSurfaceParticles(World world, BlockPos pos) {
        for (int i = 0; i < 2; i++) {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                    pos.getX() + world.rand.nextFloat(), pos.getY() + 1.0D,
                    pos.getZ() + world.rand.nextFloat(), 0.0D, 0.01D, 0.0D);
            world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
                    pos.getX() + world.rand.nextFloat(), pos.getY() + 1.0D,
                    pos.getZ() + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }
}
