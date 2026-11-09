package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import xy177.farmersfuturedelight.common.registry.FFDStats;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.network.FFDGuiHandler;
import xy177.farmersfuturedelight.common.tile.TileEntityGrindstone;

public class BlockGrindstone extends BlockContainer {
    private static final AxisAlignedBB[][] SHAPES = createShapes();
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum<Face> FACE = PropertyEnum.create("face", Face.class);

    public BlockGrindstone() {
        super(Material.ROCK);
        setRegistryName(FarmerFutureDelight.MODID, "grindstone");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".grindstone");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(2.0F);
        setResistance(2.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
                .withProperty(FACE, Face.FLOOR));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, FACE);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityGrindstone();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB[] parts = shapes(state);
        AxisAlignedBB bounds = parts[0];
        for (int index = 1; index < parts.length; index++) bounds = bounds.union(parts[index]);
        return bounds;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            AxisAlignedBB entityBox, java.util.List<AxisAlignedBB> boxes,
            net.minecraft.entity.Entity entity, boolean actualState) {
        for (AxisAlignedBB part : shapes(state)) addCollisionBoxToList(pos, entityBox, boxes, part);
    }

    @Override
    public net.minecraft.util.math.RayTraceResult collisionRayTrace(IBlockState state, World world,
            BlockPos pos, net.minecraft.util.math.Vec3d start, net.minecraft.util.math.Vec3d end) {
        net.minecraft.util.math.RayTraceResult closest = null;
        for (AxisAlignedBB part : shapes(state)) {
            net.minecraft.util.math.RayTraceResult hit = rayTrace(pos, start, end, part);
            if (hit != null && (closest == null || start.squareDistanceTo(hit.hitVec)
                    < start.squareDistanceTo(closest.hitVec))) closest = hit;
        }
        return closest;
    }

    @Override
    public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world,
            IBlockState state, BlockPos pos, EnumFacing side) {
        return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
    }

    private static AxisAlignedBB[] shapes(IBlockState state) {
        return SHAPES[state.getValue(FACE).ordinal() * 4
                + state.getValue(FACING).getHorizontalIndex()];
    }

    private static AxisAlignedBB[][] createShapes() {
        AxisAlignedBB[] north = {
            new AxisAlignedBB(4, 2, 0, 12, 14, 12),
            new AxisAlignedBB(2, 6, 7, 4, 10, 16),
            new AxisAlignedBB(2, 5, 3, 4, 11, 9),
            new AxisAlignedBB(12, 6, 7, 14, 10, 16),
            new AxisAlignedBB(12, 5, 3, 14, 11, 9)
        };
        AxisAlignedBB[][] result = new AxisAlignedBB[12][];
        for (Face face : Face.values()) {
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                AxisAlignedBB[] parts = new AxisAlignedBB[north.length];
                for (int index = 0; index < north.length; index++) {
                    AxisAlignedBB original = north[index];
                    AxisAlignedBB part = face == Face.FLOOR
                            ? new AxisAlignedBB(original.minX, 16 - original.maxZ, original.minY,
                                    original.maxX, 16 - original.minZ, original.maxY)
                            : face == Face.CEILING
                            ? new AxisAlignedBB(original.minX, original.minZ, 16 - original.maxY,
                                    original.maxX, original.maxZ, 16 - original.minY) : original;
                    if (facing == EnumFacing.EAST) {
                        part = new AxisAlignedBB(16 - part.maxZ, part.minY, part.minX,
                                16 - part.minZ, part.maxY, part.maxX);
                    } else if (facing == EnumFacing.SOUTH) {
                        part = new AxisAlignedBB(16 - part.maxX, part.minY, 16 - part.maxZ,
                                16 - part.minX, part.maxY, 16 - part.minZ);
                    } else if (facing == EnumFacing.WEST) {
                        part = new AxisAlignedBB(part.minZ, part.minY, 16 - part.maxX,
                                part.maxZ, part.maxY, 16 - part.minX);
                    }
                    parts[index] = new AxisAlignedBB(part.minX / 16, part.minY / 16, part.minZ / 16,
                            part.maxX / 16, part.maxY / 16, part.maxZ / 16);
                }
                result[face.ordinal() * 4 + facing.getHorizontalIndex()] = parts;
            }
        }
        return result;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(FarmerFutureDelight.instance, FFDGuiHandler.GRINDSTONE,
                    world, pos.getX(), pos.getY(), pos.getZ());
            player.addStat(FFDStats.GRINDSTONE_INTERACTION);
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            EntityLivingBase placer) {
        Face face = facing == EnumFacing.UP ? Face.FLOOR
                : facing == EnumFacing.DOWN ? Face.CEILING : Face.WALL;
        return getDefaultState().withProperty(FACE, face)
                .withProperty(FACING, face == Face.WALL ? facing : placer.getHorizontalFacing());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        Face[] faces = Face.values();
        return getDefaultState().withProperty(FACING,
                EnumFacing.getHorizontal(metadata & 3))
                .withProperty(FACE, faces[(metadata >> 2) % faces.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex()
                | state.getValue(FACE).ordinal() << 2;
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
        if (!world.isRemote && tile instanceof TileEntityGrindstone) {
            ((TileEntityGrindstone) tile).dropContents();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return canPlaceBlockAt(world, pos);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                net.minecraft.block.Block block, BlockPos neighborPos) {
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    public enum Face implements IStringSerializable {
        FLOOR,
        WALL,
        CEILING;

        @Override
        public String getName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
