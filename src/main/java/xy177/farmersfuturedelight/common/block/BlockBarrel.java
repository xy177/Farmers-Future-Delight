package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.Block;
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
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.tile.TileEntityBarrel;

public class BlockBarrel extends BlockContainer {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool OPEN = PropertyBool.create("open");

    public BlockBarrel() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "barrel");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".barrel");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.5F);
        setResistance(2.5F / 3.0F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(OPEN, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityBarrel();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityBarrel) {
                player.displayGUIChest((TileEntityBarrel) tile);
                player.addStat(FFDStats.BARREL_OPENED);
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (stack.hasDisplayName() && tile instanceof TileEntityBarrel) {
            ((TileEntityBarrel) tile).setCustomName(stack.getDisplayName());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                           float hitX, float hitY, float hitZ, int metadata,
                                           EntityLivingBase placer) {
        net.minecraft.util.math.Vec3d look = placer.getLookVec();
        return getDefaultState().withProperty(FACING, EnumFacing.getFacingFromVector(
                (float) look.x, (float) look.y, (float) look.z).getOpposite());
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBarrel) {
            ((TileEntityBarrel) tile).recheckOpeners();
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBarrel) {
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityBarrel) tile);
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, state);
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
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(metadata & 7))
                .withProperty(OPEN, (metadata & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex() | (state.getValue(OPEN) ? 8 : 0);
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
        return new BlockStateContainer(this, FACING, OPEN);
    }
}
