package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.api.IWaterloggableBlock;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.api.WorkstationRecipes;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.fluid.FFDFluidloggedApiAccess;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.tile.TileEntityCampfire;

public class BlockCampfire extends BlockContainer implements IWaterloggableBlock {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyBool SIGNAL_FIRE = PropertyBool.create("signal_fire");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0, 0, 0, 1, 0.4375D, 1);

    public BlockCampfire() {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, "campfire");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".campfire");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.0F);
        setResistance(2.0F / 3.0F);
        setLightLevel(0.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(LIT, true).withProperty(SIGNAL_FIRE, false).withProperty(WATERLOGGED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCampfire();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
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
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos,
                                         @Nullable EntityLiving entity) {
        return isLit(state, world, pos) ? PathNodeType.DAMAGE_FIRE : null;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                             BlockPos pos, EnumFacing face) {
        return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                      @Nullable Entity entity, boolean actualState) {
        addCollisionBoxToList(pos, entityBox, boxes, SHAPE);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote && isLit(state, world, pos)
                && entity instanceof EntityLivingBase) {
            entity.attackEntityFrom(net.minecraft.util.DamageSource.HOT_FLOOR, 1.0F);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        ItemStack held = player.getHeldItem(hand);
        if (tile instanceof TileEntityCampfire && !held.isEmpty()
                && WorkstationRecipes.find(WorkstationRecipes.Type.CAMPFIRE_COOKING,
                        java.util.Collections.singletonList(held)) != null) {
            if (!world.isRemote && player.canPlayerEdit(pos, facing, held)) {
                boolean placed = ((TileEntityCampfire) tile).placeFood(player, held);
                if (placed) {
                    player.addStat(FFDStats.CAMPFIRE_INTERACTION);
                }
                return placed;
            }
            return true;
        }
        boolean waterlogged = hasContainedFluid(world, pos);
        if (!state.getValue(LIT) && !waterlogged
                && (held.getItem() == net.minecraft.init.Items.FLINT_AND_STEEL
                || held.getItem() == net.minecraft.init.Items.FIRE_CHARGE)) {
            if (!world.isRemote && player.canPlayerEdit(pos, facing, held)) {
                world.setBlockState(pos, state.withProperty(LIT, true), 11);
                world.playSound(null, pos,
                        held.getItem() == net.minecraft.init.Items.FLINT_AND_STEEL
                        ? net.minecraft.init.SoundEvents.ITEM_FLINTANDSTEEL_USE
                        : net.minecraft.init.SoundEvents.ITEM_FIRECHARGE_USE,
                        net.minecraft.util.SoundCategory.BLOCKS, 1.0F,
                        held.getItem() == net.minecraft.init.Items.FLINT_AND_STEEL
                        ? world.rand.nextFloat() * 0.4F + 0.8F
                        : (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
                if (held.getItem() == net.minecraft.init.Items.FLINT_AND_STEEL) {
                    held.damageItem(1, player);
                } else if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                player.addStat(FFDStats.CAMPFIRE_INTERACTION);
            }
            return true;
        }
        if (state.getValue(LIT) && !waterlogged
                && !held.isEmpty() && held.getItem().getToolClasses(held).contains("shovel")) {
            if (player.canPlayerEdit(pos, facing, held)) {
                if (!world.isRemote) world.playEvent(null, 1009, pos, 0);
                if (tile instanceof TileEntityCampfire) {
                    ((TileEntityCampfire) tile).extinguish();
                }
                world.setBlockState(pos, state.withProperty(LIT, false), 3);
                if (!world.isRemote) {
                    held.damageItem(1, player);
                    player.addStat(FFDStats.CAMPFIRE_INTERACTION);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            EntityLivingBase placer) {
        boolean water = WaterloggedBlockApi.containsWater(world, pos)
                || FFDFluidloggedApiAccess.getFluid(world, pos)
                        == net.minecraftforge.fluids.FluidRegistry.WATER;
        boolean smoke = world.getBlockState(pos.down()).getBlock() == net.minecraft.init.Blocks.HAY_BLOCK;
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing())
                .withProperty(SIGNAL_FIRE, smoke).withProperty(WATERLOGGED, water).withProperty(LIT, !water);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        WaterloggedBlockApi.onBlockAdded(world, pos, this);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block blockIn, BlockPos fromPos) {
        if (!world.isRemote) {
            WaterloggedBlockApi.onNeighborChanged(world, pos, this);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(SIGNAL_FIRE,
                world.getBlockState(pos.down()).getBlock() == net.minecraft.init.Blocks.HAY_BLOCK);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && WaterloggedBlockApi.isWaterlogged(world, pos)) {
            WaterloggedBlockApi.updateTick(world, pos, state);
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
        dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCampfire) {
            ((TileEntityCampfire) tile).dropContents();
        }
        WaterloggedBlockApi.restoreFluid(world, pos, state, 3);
        onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCampfire) ((TileEntityCampfire) tile).dropContents();
        super.breakBlock(world, pos, state);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!isLit(state, world, pos)) return;
        if (random.nextInt(5) == 0) {
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    random.nextFloat() / 2.0F, 0.00005D, random.nextFloat() / 2.0F);
        }
        if (random.nextInt(10) == 0) {
            world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    FFDSounds.CAMPFIRE_CRACKLE, net.minecraft.util.SoundCategory.BLOCKS,
                    0.5F + random.nextFloat(), 0.6F + random.nextFloat() * 0.7F, false);
        }
    }

    public static void makeSmokeParticles(World world, BlockPos pos, boolean signalFire,
                                          boolean smoking) {
        if (!world.isRemote) return;
        Random random = world.rand;
        FarmerFutureDelight.proxy.spawnCampfireSmoke(world,
                pos.getX() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1),
                pos.getY() + random.nextDouble() + random.nextDouble(),
                pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1),
                signalFire);
        if (smoking) {
            world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5D + random.nextDouble() / 4.0D * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + 0.4D,
                    pos.getZ() + 0.5D + random.nextDouble() / 4.0D * (random.nextBoolean() ? 1 : -1),
                    0.0D, 0.005D, 0.0D);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.getExtendedState(state, world, pos);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return isLit(state, world, pos) ? 15 : 0;
    }

    public static boolean isLit(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getBlock() instanceof BlockCampfire && state.getValue(LIT)
                && !hasContainedFluid(world, pos);
    }

    public static boolean hasContainedFluid(IBlockAccess world, BlockPos pos) {
        return WaterloggedBlockApi.isWaterlogged(world, pos)
                || FFDFluidloggedApiAccess.getFluid(world, pos) != null;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public net.minecraft.item.Item getItemDropped(IBlockState state, Random random, int fortune) {
        return net.minecraft.init.Items.COAL;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 1;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public IBlockState setWaterloggedState(IBlockState state, boolean waterlogged) {
        return state.withProperty(WATERLOGGED, waterlogged).withProperty(LIT, !waterlogged && state.getValue(LIT));
    }

    @Override
    public boolean isWaterloggedState(IBlockState state) {
        return state.getBlock() == this && state.getValue(WATERLOGGED);
    }

    @Override
    public net.minecraft.block.material.Material getMaterial(IBlockState state) {
        return state.getValue(WATERLOGGED) ? Material.WATER : Material.ROCK;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT || state.getValue(WATERLOGGED)
                && layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(LIT) ? 4 : 0)
                | (state.getValue(WATERLOGGED) ? 8 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(metadata & 3))
                .withProperty(LIT, (metadata & 4) != 0 && (metadata & 8) == 0)
                .withProperty(WATERLOGGED, (metadata & 8) != 0);
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
    protected BlockStateContainer createBlockState() {
        return new net.minecraftforge.common.property.ExtendedBlockState(this,
                new IProperty<?>[] {FACING, LIT, SIGNAL_FIRE, WATERLOGGED},
                WaterloggedBlockApi.extendedProperties());
    }
}
