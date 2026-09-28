package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.block.properties.PropertyInteger;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.worldgen.FFDLushCaveBlockProvider;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class BlockSweetBerryBush extends BlockBush implements net.minecraft.block.IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    private static final AxisAlignedBB AGE_0_BOX = new AxisAlignedBB(0.1875D, 0.0D, 0.1875D,
            0.8125D, 0.5D, 0.8125D);
    private static final AxisAlignedBB GROWING_BOX = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D,
            0.9375D, 1.0D, 0.9375D);
    private static final DamageSource SWEET_BERRY_DAMAGE = new DamageSource("sweetBerryBush");

    public BlockSweetBerryBush() {
        setRegistryName(FarmerFutureDelight.MODID, "sweet_berry_bush");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".sweet_berry_bush");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(FFDSounds.SWEET_BERRY_BUSH);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, Math.min(meta, 3));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int age = state.getValue(AGE);
        if (!world.isRemote && FFDItems.isSweetBerryEnabled() && age < 3
                && rand.nextInt(FFDConfig.sweetBerryGrowthRoll) == 0
                && world.getLightFromNeighbors(pos.up()) >= 9) {
            world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int age = state.getValue(AGE);
        if (age < 2) {
            return false;
        }
        ItemStack held = player.getHeldItem(hand);
        if (age < 3 && held.getItem() == Items.DYE && held.getMetadata() == 15) {
            return false;
        }
        if (!world.isRemote) {
            int count = 1 + world.rand.nextInt(2) + (age == 3 ? 1 : 0);
            spawnAsEntity(world, pos, FFDItems.effectiveStack(FFDItems.SWEET_BERRIES, count));
            world.setBlockState(pos, state.withProperty(AGE, 1), 2);
            world.playSound(null, pos, FFDSounds.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundCategory.BLOCKS, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
        }
        return true;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase) || isImmuneToBush(entity)) {
            return;
        }
        FFDGameplayHooks.markSweetBerrySlowdown(entity);
        if (!world.isRemote && state.getValue(AGE) != 0
                && (Math.abs(entity.posX - entity.prevPosX) >= (double) 0.003F
                        || Math.abs(entity.posZ - entity.prevPosZ) >= (double) 0.003F)) {
            entity.attackEntityFrom(SWEET_BERRY_DAMAGE, 1.0F);
        }
    }

    private static boolean isImmuneToBush(Entity entity) {
        if (entity instanceof EntityBee) {
            return true;
        }
        ResourceLocation id = EntityList.getKey(entity);
        return id != null && ("bee".equals(id.getResourcePath())
                || "fox".equals(id.getResourcePath()));
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return super.canSustainBush(state) || state.getBlock() == net.minecraft.init.Blocks.MYCELIUM
                || FFDLushCaveBlockProvider.get().isMossBlock(state)
                || FFDLushCaveBlockProvider.get().isRootedDirt(state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(AGE)) {
            case 1:
            case 2:
                return GROWING_BOX;
            case 3:
                return FULL_BLOCK_AABB;
            default:
                return AGE_0_BOX;
        }
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world,
                                  BlockPos pos, EntityPlayer player) {
        return FFDItems.effectiveStack(FFDItems.SWEET_BERRIES);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        int age = state.getValue(AGE);
        if (age >= 2) {
            Random random = world instanceof World ? ((World) world).rand : RANDOM;
            int count = (age == 3 ? 2 : 1) + random.nextInt(2);
            if (fortune > 0) {
                count += random.nextInt(fortune + 1);
            }
            drops.add(FFDItems.effectiveStack(FFDItems.SWEET_BERRIES, count));
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int age = Math.min(3, state.getValue(AGE) + 1);
        world.setBlockState(pos, state.withProperty(AGE, age), 2);
    }
}
