package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockHoney extends Block {
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D,
            0.9375D, 0.9375D, 0.9375D);
    private static final SoundType HONEY_SOUND = new SoundType(1.0F, 1.0F,
            FFDSounds.HONEY_BLOCK_BREAK, FFDSounds.HONEY_BLOCK_STEP,
            FFDSounds.HONEY_BLOCK_PLACE, FFDSounds.HONEY_BLOCK_HIT,
            FFDSounds.HONEY_BLOCK_FALL);

    public BlockHoney() {
        super(Material.CLAY);
        setRegistryName(FarmerFutureDelight.MODID, "honey_block");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".honey_block");
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.0F);
        setSoundType(HONEY_SOUND);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn,
                                                  BlockPos pos) {
        return AABB;
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
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        if (world.getBlockState(pos.offset(side)).getBlock() == this) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public boolean isStickyBlock(IBlockState state) {
        return true;
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        entity.playSound(FFDSounds.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
        if (world.isRemote) {
            spawnParticles(entity, 10);
        }
        entity.fall(fallDistance, 0.2F);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (isSliding(pos, entity)) {
            updateVelocity(entity);
            slideParticles(world, entity);
        } else {
            entity.motionX *= 0.4D;
            entity.motionZ *= 0.4D;
        }
    }

    private static void updateVelocity(Entity entity) {
        double oldMotionY = getOldMotionY(entity.motionY);
        if (oldMotionY < -0.13D) {
            double factor = -0.05D / oldMotionY;
            entity.motionX *= factor;
            entity.motionY = getNewMotionY(-0.05D);
            entity.motionZ *= factor;
        } else {
            entity.motionY = getNewMotionY(-0.05D);
        }
        entity.fallDistance = 0.0F;
    }

    private static double getOldMotionY(double motionY) {
        return motionY / 0.98F + 0.08D;
    }

    private static double getNewMotionY(double motionY) {
        return (motionY - 0.08D) * 0.98F;
    }

    private static boolean isSliding(BlockPos pos, Entity entity) {
        if (entity.onGround || entity.posY > pos.getY() + 0.9375D - 1.0E-7D
                || getOldMotionY(entity.motionY) >= -0.08D) {
            return false;
        }
        double xDistance = Math.abs(pos.getX() + 0.5D - entity.posX);
        double zDistance = Math.abs(pos.getZ() + 0.5D - entity.posZ);
        double edge = 0.4375D + entity.width / 2.0F;
        return xDistance + 1.0E-7D > edge || zDistance + 1.0E-7D > edge;
    }

    private static void slideParticles(World world, Entity entity) {
        if (!hasSlideParticles(entity)) {
            return;
        }
        if (world.rand.nextInt(5) == 0) {
            entity.playSound(FFDSounds.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
        }
        if (world.isRemote && world.rand.nextInt(5) == 0) {
            spawnParticles(entity, 5);
        }
    }

    private static boolean hasSlideParticles(Entity entity) {
        return entity instanceof EntityLivingBase || entity instanceof EntityMinecart
                || entity instanceof EntityTNTPrimed || entity instanceof EntityBoat;
    }

    private static void spawnParticles(Entity entity, int count) {
        for (int i = 0; i < count; i++) {
            entity.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, entity.posX, entity.posY,
                    entity.posZ, 0.0D, 0.0D, 0.0D, Block.getStateId(FFDBlocks.HONEY_BLOCK.getDefaultState()));
        }
    }
}
