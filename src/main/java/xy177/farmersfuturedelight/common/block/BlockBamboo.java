package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockBamboo extends Block implements IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 1);
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 1);
    public static final PropertyEnum<Leaves> LEAVES = PropertyEnum.create("leaves", Leaves.class);
    private static final AxisAlignedBB SMALL = new AxisAlignedBB(5.0D / 16, 0, 5.0D / 16,
            11.0D / 16, 1, 11.0D / 16);
    private static final AxisAlignedBB LARGE = new AxisAlignedBB(3.0D / 16, 0, 3.0D / 16,
            13.0D / 16, 1, 13.0D / 16);
    private static final AxisAlignedBB COLLISION = new AxisAlignedBB(6.5D / 16, 0, 6.5D / 16,
            9.5D / 16, 1, 9.5D / 16);

    public BlockBamboo() {
        super(Material.WOOD, MapColor.FOLIAGE);
        setRegistryName(FarmerFutureDelight.MODID, "bamboo");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".bamboo");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(1.0F);
        setLightOpacity(0);
        setSoundType(FFDSounds.BAMBOO);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0)
                .withProperty(LEAVES, Leaves.NONE).withProperty(STAGE, 0));
    }

    public static boolean supportsBamboo(IBlockState state) {
        Block block = state.getBlock();
        FFDLushCaveBlockProvider provider = FFDLushCaveBlockProvider.get();
        return block == FFDBlocks.BAMBOO || block == FFDBlocks.BAMBOO_SAPLING
                || block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.MYCELIUM
                || block == Blocks.SAND || block == Blocks.GRAVEL
                || provider.isMossBlock(state) || provider.isRootedDirt(state);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && WaterloggedBlockApi.getContainedFluid(world, pos) == null
                && supportsBamboo(world.getBlockState(pos.down()));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState below = world.getBlockState(pos.down());
        if (below.getBlock() == FFDBlocks.BAMBOO_SAPLING) {
            return getDefaultState();
        }
        if (below.getBlock() == this) {
            return getDefaultState().withProperty(AGE, below.getValue(AGE));
        }
        IBlockState above = world.getBlockState(pos.up());
        return above.getBlock() == this ? getDefaultState().withProperty(AGE, above.getValue(AGE))
                : FFDBlocks.BAMBOO_SAPLING.getDefaultState();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (!supportsBamboo(world.getBlockState(pos.down()))) {
            world.scheduleUpdate(pos, this, 1);
        }
        IBlockState above = world.getBlockState(pos.up());
        if (above.getBlock() == this && above.getValue(AGE) > state.getValue(AGE)) {
            world.setBlockState(pos, state.withProperty(AGE, 1), 3);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!supportsBamboo(world.getBlockState(pos.down()))) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (state.getValue(STAGE) == 0 && random.nextInt(3) == 0 && world.isAirBlock(pos.up())
                && !FFDHeightHooks.isOutsideBuildHeight(world, pos.up())
                && world.getLightFromNeighbors(pos.up()) >= 9) {
            int height = count(world, pos, EnumFacing.DOWN) + 1;
            if (height < 16 && ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
                growBamboo(world, pos, state, random, height);
                ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
            }
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        int above = count(world, pos, EnumFacing.UP);
        BlockPos top = pos.up(above);
        return above + count(world, pos, EnumFacing.DOWN) + 1 < 16
                && world.getBlockState(top).getValue(STAGE) == 0
                && !FFDHeightHooks.isOutsideBuildHeight(world, top.up()) && world.isAirBlock(top.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        int above = count(world, pos, EnumFacing.UP);
        int height = above + count(world, pos, EnumFacing.DOWN) + 1;
        int amount = 1 + random.nextInt(2);
        for (int index = 0; index < amount; index++) {
            BlockPos top = pos.up(above);
            IBlockState topState = world.getBlockState(top);
            if (height >= 16 || topState.getValue(STAGE) == 1 || !world.isAirBlock(top.up())
                    || FFDHeightHooks.isOutsideBuildHeight(world, top.up())) {
                return;
            }
            growBamboo(world, top, topState, random, height);
            above++;
            height++;
        }
    }

    private int count(IBlockAccess world, BlockPos pos, EnumFacing direction) {
        int height = 0;
        while (height < 16 && world.getBlockState(pos.offset(direction, height + 1)).getBlock() == this) {
            height++;
        }
        return height;
    }

    private void growBamboo(World world, BlockPos pos, IBlockState state, Random random, int height) {
        IBlockState below = world.getBlockState(pos.down());
        IBlockState twoBelow = world.getBlockState(pos.down(2));
        Leaves leaves = Leaves.NONE;
        if (height >= 1) {
            if (below.getBlock() != this || below.getValue(LEAVES) == Leaves.NONE) {
                leaves = Leaves.SMALL;
            } else {
                leaves = Leaves.LARGE;
                if (twoBelow.getBlock() == this) {
                    world.setBlockState(pos.down(), below.withProperty(LEAVES, Leaves.SMALL), 3);
                    world.setBlockState(pos.down(2), twoBelow.withProperty(LEAVES, Leaves.NONE), 3);
                }
            }
        }
        int age = state.getValue(AGE) == 1 || twoBelow.getBlock() == this ? 1 : 0;
        int stage = height >= 11 && random.nextFloat() < 0.25F || height == 15 ? 1 : 0;
        world.setBlockState(pos.up(), getDefaultState().withProperty(AGE, age)
                .withProperty(LEAVES, leaves).withProperty(STAGE, stage), 3);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return (state.getValue(LEAVES) == Leaves.LARGE ? LARGE : SMALL).offset(state.getOffset(world, pos));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return COLLISION.offset(state.getOffset(world, pos));
    }

    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        return player.getHeldItemMainhand().getItem() instanceof ItemSword
                ? 1.0F : super.getPlayerRelativeBlockHardness(state, player, world, pos);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return FFDItems.effectiveItem(FFDItems.BAMBOO);
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
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, LEAVES, STAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, meta & 1)
                .withProperty(LEAVES, Leaves.values()[Math.min((meta >> 1) & 3, 2)])
                .withProperty(STAGE, (meta >> 3) & 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE) | state.getValue(LEAVES).ordinal() << 1 | state.getValue(STAGE) << 3;
    }

    public enum Leaves implements IStringSerializable {
        NONE, SMALL, LARGE;

        @Override
        public String getName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
