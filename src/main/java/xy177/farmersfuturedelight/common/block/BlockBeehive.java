package xy177.farmersfuturedelight.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;

public class BlockBeehive extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyInteger HONEY_LEVEL = PropertyInteger.create("honey_level", 0, 5);

    private final boolean naturalNest;

    public BlockBeehive(boolean naturalNest) {
        super(Material.WOOD);
        this.naturalNest = naturalNest;
        String name = naturalNest ? "bee_nest" : "beehive";
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(naturalNest ? 0.3F : 0.6F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(HONEY_LEVEL, 0));
    }

    public boolean isNaturalNest() {
        return naturalNest;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityBeehive();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(HONEY_LEVEL, 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HONEY_LEVEL);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityBeehive
                ? state.withProperty(HONEY_LEVEL, ((TileEntityBeehive) tile).getHoneyLevel())
                : state;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ, int meta,
                                             EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(HONEY_LEVEL, 0);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBeehive) {
            ((TileEntityBeehive) tile).loadFromItem(stack);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityBeehive
                && ((TileEntityBeehive) tile).harvest(player, hand);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityBeehive
                ? ((TileEntityBeehive) tile).getHoneyLevel()
                : blockState.getValue(HONEY_LEVEL);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos,
                                  java.util.Random random) {
        TileEntity tile = world.getTileEntity(pos);
        int honeyLevel = tile instanceof TileEntityBeehive
                ? ((TileEntityBeehive) tile).getHoneyLevel()
                : state.getValue(HONEY_LEVEL);
        if (honeyLevel < 5 || random.nextFloat() < 0.3F) {
            return;
        }
        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        if (belowState.getMaterial().isLiquid() || belowState.isFullCube()) {
            return;
        }
        FarmerFutureDelight.proxy.spawnHoneyDripParticle(world,
                pos.getX() + 0.1D + random.nextDouble() * 0.8D,
                pos.getY() - 0.05D,
                pos.getZ() + 0.1D + random.nextDouble() * 0.8D);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, blockIn, fromPos);
        if (world.isRemote) {
            return;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBeehive && ((TileEntityBeehive) tile).isFireNearby()) {
            ((TileEntityBeehive) tile).releaseAll(
                    TileEntityBeehive.ReleaseStatus.EMERGENCY, null);
        }
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBeehive) {
            TileEntityBeehive hive = (TileEntityBeehive) tile;
            ItemStack tool = player.getHeldItemMainhand();
            if (player.capabilities.isCreativeMode) {
                if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops")
                        && (!hive.isEmpty() || hive.getHoneyLevel() > 0)) {
                    spawnAsEntity(world, pos, hive.createItemStack());
                }
                hive.preserveForSilkTouch();
            } else if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
                hive.preserveForSilkTouch();
            } else {
                hive.angerBees(player);
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity tile, ItemStack tool) {
        if (!world.isRemote && !player.capabilities.isCreativeMode
                && tile instanceof TileEntityBeehive
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            TileEntityBeehive hive = (TileEntityBeehive) tile;
            spawnAsEntity(world, pos, hive.createItemStack());
            player.addStat(StatList.getBlockStats(this));
            player.addExhaustion(0.005F);
            return;
        }
        super.harvestBlock(world, player, pos, state, tile, tool);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBeehive) {
            TileEntityBeehive hive = (TileEntityBeehive) tile;
            boolean preserve = hive.consumePreserveOnBreak();
            boolean suppressRelease = hive.consumeSuppressReleaseOnBreak();
            if (!preserve && !suppressRelease) {
                hive.releaseAll(TileEntityBeehive.ReleaseStatus.EMERGENCY, null);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getTileEntity(pos);
        if (!world.isRemote && tile instanceof TileEntityBeehive) {
            TileEntityBeehive hive = (TileEntityBeehive) tile;
            if (releasesBees(explosion)) {
                hive.releaseAll(TileEntityBeehive.ReleaseStatus.EMERGENCY, null);
            } else {
                hive.suppressReleaseOnBreak();
            }
            hive.angerNearbyBees();
        }
        super.onBlockExploded(world, pos, explosion);
    }

    private static boolean releasesBees(Explosion explosion) {
        Entity exploder;
        try {
            exploder = ReflectionHelper.getPrivateValue(Explosion.class, explosion,
                    "exploder", "field_77283_e");
        } catch (RuntimeException ignored) {
            return false;
        }
        return exploder instanceof EntityTNTPrimed || exploder instanceof EntityMinecartTNT
                || exploder instanceof EntityCreeper || exploder instanceof EntityWither
                || exploder instanceof EntityWitherSkull;
    }

    @Override
    public Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
        return naturalNest ? Items.AIR : Item.getItemFromBlock(this);
    }
}
