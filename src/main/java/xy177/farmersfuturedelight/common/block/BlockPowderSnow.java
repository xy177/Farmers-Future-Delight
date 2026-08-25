package xy177.farmersfuturedelight.common.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.client.particle.ParticlePowderSnow;

public class BlockPowderSnow extends Block {
    private static final AxisAlignedBB FALLING_COLLISION =
            new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9D, 1.0D);

    public BlockPowderSnow() {
        super(Material.SNOW, MapColor.SNOW);
        setRegistryName(FarmerFutureDelight.MODID, "powder_snow");
        setUnlocalizedName(FarmerFutureDelight.MODID + ".powder_snow");
        setHardness(0.25F);
        setSoundType(FFDSounds.POWDER_SNOW);
        setLightOpacity(0);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                      @Nullable Entity entity, boolean actualState) {
        if (entity == null) {
            return;
        }
        if (entity instanceof EntityFallingBlock) {
            addCollisionBoxToList(pos, entityBox, boxes, FULL_BLOCK_AABB);
            return;
        }
        if (entity.fallDistance > 2.5F) {
            addCollisionBoxToList(pos, entityBox, boxes, FALLING_COLLISION);
            return;
        }
        if (canEntityWalkOnPowderSnow(entity) && !entity.isSneaking()
                && entity.getEntityBoundingBox().minY >= pos.getY() + 0.999D) {
            addCollisionBoxToList(pos, entityBox, boxes, FULL_BLOCK_AABB);
        }
    }

    public static boolean canEntityWalkOnPowderSnow(Entity entity) {
        if (entity instanceof EntityRabbit || entity instanceof EntityEndermite
                || entity instanceof EntitySilverfish) {
            return true;
        }
        return entity instanceof EntityLivingBase
                && ((EntityLivingBase) entity).getItemStackFromSlot(
                        net.minecraft.inventory.EntityEquipmentSlot.FEET).getItem()
                == Items.LEATHER_BOOTS
                || entity instanceof EntityLivingBase
                && xy177.farmersfuturedelight.common.FFDPowderSnowEvents.isWalkableBoots(
                        ((EntityLivingBase) entity).getItemStackFromSlot(
                                net.minecraft.inventory.EntityEquipmentSlot.FEET));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state,
                                          Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            entity.fallDistance = 0.0F;
            entity.motionX *= 0.9D;
            entity.motionY *= 1.5D;
            entity.motionZ *= 0.9D;
            if (world.isRemote
                    && (entity.prevPosX != entity.posX || entity.prevPosZ != entity.posZ)
                    && world.rand.nextBoolean()) {
                world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL,
                        entity.posX, pos.getY() + 1.0D, entity.posZ,
                        (world.rand.nextFloat() * 2.0F - 1.0F) / 12.0F,
                        0.05D, (world.rand.nextFloat() * 2.0F - 1.0F) / 12.0F);
            }
        }
        if (!world.isRemote && entity.isBurning()) {
            if (entity instanceof net.minecraft.entity.player.EntityPlayer
                    || world.getGameRules().getBoolean("mobGriefing")) {
                destroyPowderSnow(world, pos);
            }
            entity.extinguish();
        }
    }

    public static boolean destroyPowderSnow(World world, BlockPos pos) {
        return destroyPowderSnow(world, pos, FFDBlocks.POWDER_SNOW);
    }

    public static boolean destroyPowderSnow(World world, BlockPos pos, Block expectedBlock) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != expectedBlock || !world.setBlockToAir(pos)) {
            return false;
        }
        if (!world.isRemote) {
            world.playEvent(2001, pos, Block.getStateId(state));
        }
        return true;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof net.minecraft.entity.player.EntityPlayerMP
                && !entity.isSneaking() && canEntityWalkOnPowderSnow(entity)
                && entity.getEntityBoundingBox().minY >= pos.getY() + 0.999D) {
            FFDAdvancements.WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) entity);
        }
        super.onEntityWalk(world, pos, entity);
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (fallDistance >= 4.0F && entity instanceof EntityLivingBase) {
            entity.playSound(fallDistance < 7.0F
                            ? net.minecraft.init.SoundEvents.ENTITY_GENERIC_SMALL_FALL
                            : net.minecraft.init.SoundEvents.ENTITY_GENERIC_BIG_FALL,
                    1.0F, 1.0F);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.AIR);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public net.minecraft.item.ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.POWDER_SNOW_BUCKET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        ParticlePowderSnow.addDestroyEffects(world, pos, getDefaultState(), manager);
        return true;
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

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
                                        EnumFacing side) {
        return world.getBlockState(pos.offset(side)).getBlock() != this
                && super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
                                    EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state,
                                            BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
