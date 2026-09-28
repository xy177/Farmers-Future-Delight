package xy177.farmersfuturedelight.common.item;

import java.util.List;

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
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import xy177.farmersfuturedelight.common.FFDCompat;
import xy177.farmersfuturedelight.common.entity.IFishBucketEntity;
import xy177.farmersfuturedelight.common.entity.EntityTropicalFish;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class ItemFishBucket extends ItemBucket {
    private final ResourceLocation entityId;
    private final String entityPath;

    public ItemFishBucket(String entityPath) {
        super(Blocks.FLOWING_WATER);
        this.entityId = new ResourceLocation("farmers_future_delight", entityPath);
        this.entityPath = entityPath;
        setMaxStackSize(1);
        setContainerItem(Items.BUCKET);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                     EnumHand hand) {
        ItemStack original = player.getHeldItem(hand).copy();
        RayTraceResult hit = rayTrace(world, player, false);
        FFDAquaticBucketPlacement.PlacementResult placement =
                FFDAquaticBucketPlacement.empty(this, world, player, hand, hit,
                        FFDSounds.BUCKET_EMPTY_FISH, SoundCategory.NEUTRAL);
        if (placement.action.getType() == EnumActionResult.SUCCESS
                && placement.spawnPos != null && !world.isRemote) {
            spawnFish(world, placement.spawnPos, original);
        }
        return placement.action;
    }

    public boolean spawnFish(World world, BlockPos pos, ItemStack bucket) {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(entityId);
        if (entry == null) {
            entry = FFDCompat.getExternalEntityEntry(FFDCompat.Feature.FISH, entityPath);
        }
        Entity fish = entry == null ? null : entry.newInstance(world);
        if (fish == null) {
            return false;
        }
        fish.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.1D,
                pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        if (fish instanceof EntityLiving) {
            ((EntityLiving) fish).onInitialSpawn(world.getDifficultyForLocation(pos), null);
        }
        if (fish instanceof IFishBucketEntity) {
            ((IFishBucketEntity) fish).readFromBucket(bucket);
        } else {
            if (bucket.hasDisplayName()) {
                fish.setCustomNameTag(bucket.getDisplayName());
            }
            if (fish instanceof EntityLiving) {
                ((EntityLiving) fish).enablePersistence();
            }
        }
        return world.spawnEntity(fish);
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
        world.playSound(player, pos, FFDSounds.BUCKET_EMPTY_FISH,
                SoundCategory.NEUTRAL, 1.0F, 1.0F);
        world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 11);
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (!"tropical_fish".equals(entityPath) || !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey("BucketVariantTag", 99)) {
            return;
        }
        int variant = stack.getTagCompound().getInteger("BucketVariantTag");
        int predefined = EntityTropicalFish.predefinedIndex(variant);
        if (predefined >= 0) {
            tooltip.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC
                    + I18n.format("entity.farmers_future_delight.tropical_fish.predefined."
                    + predefined));
            return;
        }
        tooltip.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC + I18n.format(
                "entity.farmers_future_delight.tropical_fish.type."
                        + EntityTropicalFish.typeName(variant)));
        String base = dyeName(EntityTropicalFish.baseColor(variant));
        String pattern = dyeName(EntityTropicalFish.patternColor(variant));
        tooltip.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC
                + (base.equals(pattern) ? base : base + ", " + pattern));
    }

    private static String dyeName(int metadata) {
        net.minecraft.item.EnumDyeColor color = net.minecraft.item.EnumDyeColor.byMetadata(metadata);
        return I18n.format("item.fireworksCharge." + color.getUnlocalizedName());
    }
}
