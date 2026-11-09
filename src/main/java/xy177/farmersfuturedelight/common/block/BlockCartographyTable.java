package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import xy177.farmersfuturedelight.common.network.FFDGuiHandler;
import xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable;

public class BlockCartographyTable extends BlockContainer {
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockCartographyTable() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "cartography_table");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".cartography_table");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.5F);
        setResistance(2.5F);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCartographyTable();
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
            player.openGui(FarmerFutureDelight.instance, FFDGuiHandler.CARTOGRAPHY_TABLE,
                    world, pos.getX(), pos.getY(), pos.getZ());
            player.addStat(FFDStats.CARTOGRAPHY_TABLE_INTERACTION);
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING,
                placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING,
                EnumFacing.getHorizontal(metadata & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
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
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (!world.isRemote && tile instanceof TileEntityCartographyTable) {
            ((TileEntityCartographyTable) tile).dropContents();
        }
        super.breakBlock(world, pos, state);
    }
}
