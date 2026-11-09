package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.tile.TileEntityJigsaw;

public final class BlockJigsaw extends Block {
    public static final PropertyEnum<Orientation> ORIENTATION = PropertyEnum.create("orientation", Orientation.class);

    public BlockJigsaw() {
        super(Material.IRON);
        setRegistryName(FarmerFutureDelight.MODID, "jigsaw");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".jigsaw");
        setBlockUnbreakable();
        setResistance(3600000.0F);
        setSoundType(SoundType.STONE);
        setDefaultState(blockState.getBaseState().withProperty(ORIENTATION, Orientation.NORTH_UP));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ORIENTATION);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityJigsaw();
    }

    @Override
    public int quantityDropped(java.util.Random random) {
        return 0;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORIENTATION).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        Orientation[] orientations = Orientation.values();
        return getDefaultState().withProperty(ORIENTATION,
                orientations[Math.floorMod(meta, orientations.length)]);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                           float hitX, float hitY, float hitZ, int meta,
                                           EntityLivingBase placer, EnumHand hand) {
        EnumFacing top = facing.getAxis() == EnumFacing.Axis.Y
                ? placer.getHorizontalFacing().getOpposite() : EnumFacing.UP;
        return getDefaultState().withProperty(ORIENTATION, Orientation.of(facing, top));
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        Orientation orientation = state.getValue(ORIENTATION);
        return state.withProperty(ORIENTATION,
                Orientation.of(rotation.rotate(orientation.front), rotation.rotate(orientation.top)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        Orientation orientation = state.getValue(ORIENTATION);
        return state.withProperty(ORIENTATION,
                Orientation.of(mirror.mirror(orientation.front), mirror.mirror(orientation.top)));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.canUseCommandBlock()) return false;
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityJigsaw)) return false;
        if (world.isRemote) FarmerFutureDelight.proxy.openJigsaw((TileEntityJigsaw) tile);
        return true;
    }

    public enum Orientation implements IStringSerializable {
        NORTH_UP(EnumFacing.NORTH, EnumFacing.UP),
        EAST_UP(EnumFacing.EAST, EnumFacing.UP),
        SOUTH_UP(EnumFacing.SOUTH, EnumFacing.UP),
        WEST_UP(EnumFacing.WEST, EnumFacing.UP),
        UP_NORTH(EnumFacing.UP, EnumFacing.NORTH),
        UP_EAST(EnumFacing.UP, EnumFacing.EAST),
        UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
        UP_WEST(EnumFacing.UP, EnumFacing.WEST),
        DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
        DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST),
        DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH),
        DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST);

        public final EnumFacing front;
        public final EnumFacing top;

        Orientation(EnumFacing front, EnumFacing top) {
            this.front = front;
            this.top = top;
        }

        @Override
        public String getName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Orientation of(EnumFacing front, EnumFacing top) {
            for (Orientation orientation : values()) {
                if (orientation.front == front && orientation.top == top) return orientation;
            }
            throw new IllegalArgumentException("Invalid jigsaw orientation: " + front + "/" + top);
        }
    }
}
