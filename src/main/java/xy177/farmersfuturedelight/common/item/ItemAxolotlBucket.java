package xy177.farmersfuturedelight.common.item;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;

import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemAxolotlBucket extends ItemBucket {
    public ItemAxolotlBucket() {
        super(Blocks.FLOWING_WATER);
        setMaxStackSize(1);
        setContainerItem(Items.BUCKET);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                     EnumHand hand) {
        ItemStack original = player.getHeldItem(hand).copy();
        RayTraceResult hit = rayTrace(world, player, false);
        BlockPos spawnPos = placementPos(world, hit);
        ActionResult<ItemStack> result = super.onItemRightClick(world, player, hand);
        if (result.getType() == EnumActionResult.SUCCESS && spawnPos != null && !world.isRemote
                && (world.provider.doesWaterVaporize()
                || world.getBlockState(spawnPos).getMaterial() == Material.WATER)) {
            spawnAxolotl(world, spawnPos, original);
        }
        return result;
    }

    @Nullable
    private static BlockPos placementPos(World world, @Nullable RayTraceResult hit) {
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
        return replaceable && hit.sideHit == EnumFacing.UP ? pos : pos.offset(hit.sideHit);
    }

    public static boolean spawnAxolotl(World world, BlockPos pos, ItemStack bucket) {
        Entity axolotl;
        if (FFDEntities.isLocalAxolotlEnabled()) {
            axolotl = new EntityAxolotl(world);
        } else {
            EntityEntry external = FFDCompat.getExternalEntityEntry(
                    FFDCompat.Feature.AXOLOTL, "axolotl");
            axolotl = external == null ? null : external.newInstance(world);
        }
        if (axolotl == null) {
            return false;
        }
        axolotl.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.1D,
                pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        if (axolotl instanceof EntityAxolotl) {
            ((EntityAxolotl) axolotl).readFromBucket(bucket);
        }
        if (axolotl instanceof EntityLiving) {
            ((EntityLiving) axolotl).enablePersistence();
        }
        return world.spawnEntity(axolotl);
    }

    @Override
    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World world,
                                            BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        boolean nonSolid = !material.isSolid();
        boolean replaceable = state.getBlock().isReplaceable(world, pos);
        if (!world.isAirBlock(pos) && !nonSolid && !replaceable) {
            return false;
        }
        if (world.provider.doesWaterVaporize()) {
            world.playSound(player, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                    SoundCategory.BLOCKS, 0.5F,
                    2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            for (int i = 0; i < 8; i++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                        pos.getX() + Math.random(), pos.getY() + Math.random(),
                        pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
            return true;
        }
        if (!world.isRemote && (nonSolid || replaceable) && !material.isLiquid()) {
            world.destroyBlock(pos, true);
        }
        world.playSound(player, pos, FFDSounds.BUCKET_EMPTY_AXOLOTL,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 11);
        return true;
    }
}
