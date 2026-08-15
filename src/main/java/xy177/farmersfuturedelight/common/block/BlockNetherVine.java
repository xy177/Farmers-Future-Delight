package xy177.farmersfuturedelight.common.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.FFDNetherBlockProvider;
import xy177.farmersfuturedelight.common.tile.TileEntityNetherVines;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockNetherVine extends Block implements IGrowable, IShearable {
    private static final int MAX_AGE = 25;
    private static final double GROWTH_CHANCE = 0.1D;
    private static final double BONEMEAL_DECAY = 0.826D;
    private static final float[] DROP_CHANCES = {0.33F, 0.55F, 0.77F, 1.0F};
    private static final AxisAlignedBB TWISTING_HEAD_AABB =
            new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.9375D, 0.75D);
    private static final AxisAlignedBB WEEPING_HEAD_AABB =
            new AxisAlignedBB(0.25D, 0.5625D, 0.25D, 0.75D, 1.0D, 0.75D);

    private final boolean growsUpward;
    private final boolean warped;

    public BlockNetherVine(String name, boolean growsUpward, boolean warped) {
        super(Material.VINE);
        this.growsUpward = growsUpward;
        this.warped = warped;
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(growsUpward ? FFDSounds.TWISTING_VINES : FFDSounds.WEEPING_VINES);
        setLightOpacity(0);
        setTickRandomly(true);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return isEnabled() && world.isAirBlock(pos) && canStay(world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        IBlockState body = bodyState();
        return body != null && isVineSegment(
                world.getBlockState(pos.offset(growthDirection())), growsUpward)
                ? body : getDefaultState();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && world.getBlockState(pos).getBlock() == this) {
            setAge(world, pos, world.rand.nextInt(MAX_AGE));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                Block blockIn, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        if (!canStay(world, pos)) {
            world.destroyBlock(pos, true);
        } else if (isVineSegment(world.getBlockState(pos.offset(growthDirection())),
                growsUpward)) {
            IBlockState body = bodyState();
            if (body != null) {
                world.setBlockState(pos, body, 2);
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !isEnabled()) {
            return;
        }
        int age = getOrInitializeAge(world, pos, random);
        BlockPos next = pos.offset(growthDirection());
        IBlockState body = bodyState();
        if (body != null && age < MAX_AGE && random.nextDouble() < GROWTH_CHANCE
                && world.isAirBlock(next)) {
            world.setBlockState(pos, body, 2);
            world.setBlockState(next, getDefaultState(), 2);
            setAge(world, next, age + 1);
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        BlockPos growthPos = pos.offset(growthDirection());
        return isEnabled() && !FFDHeightHooks.isOutsideBuildHeight(world, growthPos)
                && world.isAirBlock(growthPos);
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        BlockPos growthPos = pos.offset(growthDirection());
        return isEnabled() && !FFDHeightHooks.isOutsideBuildHeight(world, growthPos)
                && world.isAirBlock(growthPos);
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!isEnabled()) {
            return;
        }
        int age = getOrInitializeAge(world, pos, random);
        int blocksToGrow = getBlocksToGrowWhenBonemealed(random);
        BlockPos current = pos;
        IBlockState body = bodyState();
        if (body == null) {
            return;
        }
        for (int i = 0; i < blocksToGrow; i++) {
            BlockPos next = current.offset(growthDirection());
            if (FFDHeightHooks.isOutsideBuildHeight(world, next)
                    || !world.isAirBlock(next)) {
                break;
            }
            world.setBlockState(current, body, 2);
            world.setBlockState(next, getDefaultState(), 2);
            age = Math.min(age + 1, MAX_AGE);
            setAge(world, next, age);
            current = next;
        }
    }

    public boolean growsUpward() {
        return growsUpward;
    }

    public boolean isWarped() {
        return warped;
    }

    public static boolean isVineSegment(IBlockState state, boolean growsUpward) {
        return FFDNetherBlockProvider.get().isVine(state, growsUpward);
    }

    public static void setAge(World world, BlockPos pos, int age) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityNetherVines)) {
            tile = new TileEntityNetherVines();
            world.setTileEntity(pos, tile);
        }
        ((TileEntityNetherVines) tile).setAge(age);
    }

    public static boolean stopGrowth(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityNetherVines
                && ((TileEntityNetherVines) tile).getAge() >= MAX_AGE) {
            return false;
        }
        setAge(world, pos, MAX_AGE);
        return true;
    }

    public static boolean shouldDrop(Random random, int fortune) {
        return random.nextFloat() < DROP_CHANCES[Math.min(Math.max(fortune, 0), 3)];
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityNetherVines();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random random) {
        return shouldDrop(random, 0) ? 1 : 0;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return shouldDrop(random, fortune) ? 1 : 0;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos,
                                    int fortune) {
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this)));
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos,
                            EntityLivingBase entity) {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return growsUpward ? TWISTING_HEAD_AABB : WEEPING_HEAD_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    private int getOrInitializeAge(World world, BlockPos pos, Random random) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityNetherVines
                && ((TileEntityNetherVines) tile).getAge() >= 0) {
            return ((TileEntityNetherVines) tile).getAge();
        }
        int age = random.nextInt(MAX_AGE);
        setAge(world, pos, age);
        return age;
    }

    private int getBlocksToGrowWhenBonemealed(Random random) {
        double probability = 1.0D;
        int count = 0;
        while (random.nextDouble() < probability) {
            probability *= BONEMEAL_DECAY;
            count++;
        }
        return count;
    }

    private boolean canStay(World world, BlockPos pos) {
        BlockPos anchorPos = pos.offset(growthDirection().getOpposite());
        IBlockState anchor = world.getBlockState(anchorPos);
        return isVineSegment(anchor, growsUpward)
                || anchor.isSideSolid(world, anchorPos, growthDirection());
    }

    private EnumFacing growthDirection() {
        return growsUpward ? EnumFacing.UP : EnumFacing.DOWN;
    }

    private IBlockState bodyState() {
        return FFDNetherBlockProvider.get().vine(growsUpward, false);
    }

    private boolean isEnabled() {
        return warped ? FFDItems.isWarpedEnabled() : FFDItems.isCrimsonEnabled();
    }
}
