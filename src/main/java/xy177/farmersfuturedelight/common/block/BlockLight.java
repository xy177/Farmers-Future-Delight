package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

/**
 * Invisible administrator light source from Minecraft 1.17.
 *
 * <p>The 1.12 metadata stores the complete 0-15 light level. Waterlogging is
 * intentionally deferred to the project's later system-wide waterlogging pass.</p>
 */
public class BlockLight extends Block {
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

    public BlockLight() {
        super(Material.AIR);
        setRegistryName(FarmerFutureDelight.MODID, "light");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".light");
        setHardness(0.0F);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(LEVEL, 15));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LEVEL, Math.max(0, Math.min(15, meta)));
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return FFDItems.isLightEnabled() && (world.isAirBlock(pos)
                || world.getBlockState(pos).getBlock() == this);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(meta);
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FarmerFutureDelight.proxy.getLightSelectionBox(source, pos);
    }

    @Override
    @Nullable
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
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, net.minecraft.util.EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    net.minecraft.util.EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!player.getHeldItem(hand).isEmpty()) {
            return false;
        }
        if (!world.isRemote && canUseGameMasterBlocks(player)) {
            int nextLevel = (state.getValue(LEVEL) + 1) & 15;
            world.setBlockState(pos, state.withProperty(LEVEL, nextLevel), 2);
        }
        return true;
    }

    private static boolean canUseGameMasterBlocks(EntityPlayer player) {
        return player.capabilities.isCreativeMode && player.canUseCommand(2, "");
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(FFDItems.LIGHT, 1, state.getValue(LEVEL));
    }
}
