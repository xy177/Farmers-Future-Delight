package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.network.FFDGuiHandler;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;

public class BlockCookingFurnace extends BlockContainer {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool LIT = PropertyBool.create("lit");
    private final boolean smoking;

    public BlockCookingFurnace(boolean smoking) {
        super(Material.ROCK);
        this.smoking = smoking;
        String name = smoking ? "smoker" : "blast_furnace";
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.5F);
        setResistance(3.5F / 3.0F);
        setHarvestLevel("pickaxe", 0);
        setSoundType(SoundType.STONE);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(LIT, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCookingFurnace(smoking);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityCookingFurnace && !player.isSpectator()) {
                TileEntityCookingFurnace furnace = (TileEntityCookingFurnace) tile;
                if (furnace.isLocked() && !furnace.getLockCode().getLock().equals(
                        player.getHeldItemMainhand().getDisplayName())) {
                    player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation(
                            "container.isLocked", furnace.getDisplayName()), true);
                    world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_CHEST_LOCKED,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                } else {
                    player.openGui(FarmerFutureDelight.instance, FFDGuiHandler.COOKING_FURNACE,
                            world, pos.getX(), pos.getY(), pos.getZ());
                    player.addStat(smoking ? FFDStats.SMOKER_INTERACTION
                            : FFDStats.BLAST_FURNACE_INTERACTION);
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (stack.hasDisplayName() && tile instanceof TileEntityCookingFurnace) {
            ((TileEntityCookingFurnace) tile).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                           float hitX, float hitY, float hitZ, int metadata,
                                           EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCookingFurnace) {
            TileEntityCookingFurnace furnace = (TileEntityCookingFurnace) tile;
            InventoryHelper.dropInventoryItems(world, pos, furnace);
            furnace.releaseExperience(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? 13 : 0;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        if (random.nextDouble() < 0.1D) {
            world.playSound(x, y, z, smoking ? FFDSounds.SMOKER_SMOKE : FFDSounds.BLAST_FURNACE_FIRE_CRACKLE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
        if (smoking) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y + 1.1D, z, 0.0D, 0.0D, 0.0D);
        } else {
            EnumFacing direction = state.getValue(FACING);
            double spread = random.nextDouble() * 0.6D - 0.3D;
            double offsetX = direction.getAxis() == EnumFacing.Axis.X ? direction.getFrontOffsetX() * 0.52D : spread;
            double offsetZ = direction.getAxis() == EnumFacing.Axis.Z ? direction.getFrontOffsetZ() * 0.52D : spread;
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + offsetX,
                    y + random.nextDouble() * 9.0D / 16.0D, z + offsetZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(metadata & 3))
                .withProperty(LIT, (metadata & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(LIT) ? 4 : 0);
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
        return new BlockStateContainer(this, FACING, LIT);
    }
}
