package xy177.farmersfuturedelight.common;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockFutureCauldron;
import xy177.farmersfuturedelight.common.block.BlockPowderSnow;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDCauldronEvents {
    private FFDCauldronEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCauldronBucketUse(PlayerInteractEvent.RightClickBlock event) {
        if (!FFDConfig.modernCauldronFeatures) {
            return;
        }
        IBlockState oldState = event.getWorld().getBlockState(event.getPos());
        if (oldState.getBlock() != Blocks.CAULDRON
                && !(oldState.getBlock() instanceof BlockFutureCauldron)) {
            return;
        }
        Item item = event.getItemStack().getItem();
        ItemStack powderSnowBucket = FFDItems.effectiveStack(FFDItems.POWDER_SNOW_BUCKET);
        IBlockState newState;
        net.minecraft.util.SoundEvent sound;
        if (item == Items.WATER_BUCKET) {
            newState = Blocks.CAULDRON.getDefaultState().withProperty(BlockCauldron.LEVEL, 3);
            sound = net.minecraft.init.SoundEvents.ITEM_BUCKET_EMPTY;
        } else if (item == Items.LAVA_BUCKET) {
            newState = FFDBlocks.LAVA_CAULDRON.getDefaultState();
            sound = net.minecraft.init.SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
        } else if (!powderSnowBucket.isEmpty() && item == powderSnowBucket.getItem()) {
            newState = FFDBlocks.POWDER_SNOW_CAULDRON.getDefaultState()
                    .withProperty(BlockCauldron.LEVEL, 3);
            sound = FFDSounds.BUCKET_EMPTY_POWDER_SNOW;
        } else {
            return;
        }

        EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
        if (!event.getEntityPlayer().canPlayerEdit(event.getPos(), face, event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), newState, 3);
            consumeFilledBucket(event.getEntityPlayer(), event.getHand());
            event.getEntityPlayer().addStat(StatList.CAULDRON_FILLED);
            event.getWorld().updateComparatorOutputLevel(event.getPos(), newState.getBlock());
            event.getWorld().playSound(null, event.getPos(), sound,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onPowderSnowPickup(FillBucketEvent event) {
        ItemStack powderSnowBucket = FFDItems.effectiveStack(FFDItems.POWDER_SNOW_BUCKET);
        net.minecraft.block.Block powderSnow = FFDItems.effectiveBlock(FFDBlocks.POWDER_SNOW);
        if (powderSnowBucket.isEmpty() || powderSnow == null
                || event.getEmptyBucket().getItem() != Items.BUCKET
                || event.getTarget() == null
                || event.getTarget().typeOfHit != net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = event.getTarget().getBlockPos();
        if (event.getWorld().getBlockState(pos).getBlock() != powderSnow) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        EnumFacing side = event.getTarget().sideHit == null ? EnumFacing.UP
                : event.getTarget().sideHit;
        if (!event.getWorld().isBlockModifiable(player, pos)
                || !player.canPlayerEdit(pos, side, event.getEmptyBucket())) {
            event.setCanceled(true);
            return;
        }
        if (!BlockPowderSnow.destroyPowderSnow(event.getWorld(), pos, powderSnow)) {
            return;
        }
        event.getWorld().playSound(player, pos, FFDSounds.BUCKET_FILL_POWDER_SNOW,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        event.setFilledBucket(powderSnowBucket);
        event.setResult(Event.Result.ALLOW);
    }

    private static void consumeFilledBucket(EntityPlayer player, net.minecraft.util.EnumHand hand) {
        if (player.capabilities.isCreativeMode) {
            return;
        }
        ItemStack held = player.getHeldItem(hand);
        held.shrink(1);
        ItemStack empty = new ItemStack(Items.BUCKET);
        if (held.isEmpty()) {
            player.setHeldItem(hand, empty);
        } else if (!player.inventory.addItemStackToInventory(empty)) {
            player.dropItem(empty, false);
        }
    }
}
