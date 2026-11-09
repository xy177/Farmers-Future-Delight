package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.state.BlockFaceShape;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.network.FFDGuiHandler;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.tile.TileEntityLectern;

public class BlockLectern extends BlockContainer {
    private static final net.minecraft.util.math.AxisAlignedBB BASE =
            new net.minecraft.util.math.AxisAlignedBB(0, 0, 0, 1, 0.125D, 1);
    private static final net.minecraft.util.math.AxisAlignedBB POST =
            new net.minecraft.util.math.AxisAlignedBB(0.25D, 0.125D, 0.25D, 0.75D, 0.875D, 0.75D);
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool HAS_BOOK = PropertyBool.create("has_book");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockLectern() {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, "lectern");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".lectern");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.5F);
        setResistance(2.5F);
        setSoundType(SoundType.WOOD);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(HAS_BOOK, false).withProperty(POWERED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HAS_BOOK, POWERED);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityLectern();
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
    public net.minecraft.util.math.AxisAlignedBB getBoundingBox(
            IBlockState state, IBlockAccess world, BlockPos pos) {
        return new net.minecraft.util.math.AxisAlignedBB(0, 0, 0, 1, 1.125D, 1);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            net.minecraft.util.math.AxisAlignedBB entityBox,
            java.util.List<net.minecraft.util.math.AxisAlignedBB> boxes,
            net.minecraft.entity.Entity entity, boolean actualState) {
        addCollisionBoxToList(pos, entityBox, boxes, BASE);
        addCollisionBoxToList(pos, entityBox, boxes, POST);
    }

    @Override
    public net.minecraft.util.math.RayTraceResult collisionRayTrace(IBlockState state,
            World world, BlockPos pos, net.minecraft.util.math.Vec3d start,
            net.minecraft.util.math.Vec3d end) {
        net.minecraft.util.math.RayTraceResult closest = rayTrace(pos, start, end, BASE);
        net.minecraft.util.math.AxisAlignedBB[] shapes = {
            POST,
            new net.minecraft.util.math.AxisAlignedBB(0, 10.0D / 16, 1.0D / 16,
                    1, 14.0D / 16, 5.333333D / 16),
            new net.minecraft.util.math.AxisAlignedBB(0, 12.0D / 16, 5.333333D / 16,
                    1, 1, 9.666667D / 16),
            new net.minecraft.util.math.AxisAlignedBB(0, 14.0D / 16, 9.666667D / 16,
                    1, 18.0D / 16, 14.0D / 16)
        };
        EnumFacing facing = state.getValue(FACING);
        for (net.minecraft.util.math.AxisAlignedBB shape : shapes) {
            net.minecraft.util.math.AxisAlignedBB rotated = facing == EnumFacing.EAST
                    ? new net.minecraft.util.math.AxisAlignedBB(1 - shape.maxZ, shape.minY,
                            shape.minX, 1 - shape.minZ, shape.maxY, shape.maxX)
                    : facing == EnumFacing.SOUTH
                    ? new net.minecraft.util.math.AxisAlignedBB(1 - shape.maxX, shape.minY,
                            1 - shape.maxZ, 1 - shape.minX, shape.maxY, 1 - shape.minZ)
                    : facing == EnumFacing.WEST
                    ? new net.minecraft.util.math.AxisAlignedBB(shape.minZ, shape.minY,
                            1 - shape.maxX, shape.maxZ, shape.maxY, 1 - shape.minX) : shape;
            net.minecraft.util.math.RayTraceResult hit = rayTrace(pos, start, end, rotated);
            if (hit != null && (closest == null || start.squareDistanceTo(hit.hitVec)
                    < start.squareDistanceTo(closest.hitVec))) {
                closest = hit;
            }
        }
        return closest;
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(metadata & 3))
                .withProperty(HAS_BOOK, (metadata & 4) != 0)
                .withProperty(POWERED, (metadata & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(HAS_BOOK) ? 4 : 0)
                | (state.getValue(POWERED) ? 8 : 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            net.minecraft.entity.EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING,
                placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing face,
                                    float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return state.getValue(HAS_BOOK) || isBook(player.getHeldItem(hand));
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityLectern)) {
            return false;
        }
        TileEntityLectern lectern = (TileEntityLectern) tile;
        ItemStack held = player.getHeldItem(hand);
        if (!state.getValue(HAS_BOOK) && isBook(held)) {
            ItemStack book = held.copy();
            book.setCount(1);
            lectern.setBook(book, player);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            world.playSound(null, pos, FFDSounds.BOOK_PUT,
                    net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.addStat(FFDStats.LECTERN_INTERACTION);
            return true;
        }
        if (state.getValue(HAS_BOOK)) {
            player.openGui(FarmerFutureDelight.instance, FFDGuiHandler.LECTERN,
                    world, pos.getX(), pos.getY(), pos.getZ());
            player.addStat(FFDStats.LECTERN_INTERACTION);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityLectern
                ? ((TileEntityLectern) tile).getRedstoneSignal() : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos,
                            EnumFacing side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos,
                              EnumFacing side) {
        return side == EnumFacing.UP && state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state,
                           java.util.Random random) {
        if (!world.isRemote && state.getValue(POWERED)) {
            world.setBlockState(pos, state.withProperty(POWERED, false), 3);
            world.notifyNeighborsOfStateChange(pos.down(), this, false);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityLectern) {
            ItemStack book = ((TileEntityLectern) tile).removeBook(false);
            if (!book.isEmpty()) {
                spawnAsEntity(world, pos, book);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public IBlockState withRotation(IBlockState state, net.minecraft.util.Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, net.minecraft.util.Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    private static boolean isBook(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.WRITABLE_BOOK
                || stack.getItem() == Items.WRITTEN_BOOK);
    }
}
