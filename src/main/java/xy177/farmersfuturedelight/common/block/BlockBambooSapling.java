package xy177.farmersfuturedelight.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLogic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class BlockBambooSapling extends BlockBush implements IGrowable {
    private static final Material SHOOT_MATERIAL = new MaterialLogic(MapColor.WOOD) {
        @Override
        public boolean getCanBurn() {
            return true;
        }
    };
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.25D, 0, 0.25D, 0.75D, 0.75D, 0.75D);

    public BlockBambooSapling() {
        super(SHOOT_MATERIAL);
        setRegistryName(FarmerFutureDelight.MODID, "bamboo_sapling");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".bamboo_sapling");
        setHardness(1.0F);
        setLightOpacity(0);
        setSoundType(FFDSounds.BAMBOO_SAPLING);
        setTickRandomly(true);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && WaterloggedBlockApi.getContainedFluid(world, pos) == null
                && canBlockStay(world, pos, getDefaultState());
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return BlockBamboo.supportsBamboo(world.getBlockState(pos.down()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (world.getBlockState(pos).getBlock() == this && world.getBlockState(pos.up()).getBlock() == FFDBlocks.BAMBOO) {
            world.setBlockState(pos, FFDBlocks.BAMBOO.getDefaultState(), 3);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (random.nextInt(3) == 0 && canGrow(world, pos, state, false)
                && world.getLightFromNeighbors(pos.up()) >= 9
                && ForgeHooks.onCropsGrowPre(world, pos, state, true)) {
            grow(world, random, pos, state);
            ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return world.isAirBlock(pos.up()) && !FFDHeightHooks.isOutsideBuildHeight(world, pos.up());
    }

    @Override
    public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        world.setBlockState(pos.up(), FFDBlocks.BAMBOO.getDefaultState()
                .withProperty(BlockBamboo.LEAVES, BlockBamboo.Leaves.SMALL), 3);
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
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.BAMBOO);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return SHAPE.offset(state.getOffset(world, pos));
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public net.minecraft.block.material.EnumPushReaction getMobilityFlag(IBlockState state) {
        return net.minecraft.block.material.EnumPushReaction.DESTROY;
    }
}
