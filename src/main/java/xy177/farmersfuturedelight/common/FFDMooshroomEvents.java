package xy177.farmersfuturedelight.common;

import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.FFDMooshroomData;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDMooshroomEvents {
    private FFDMooshroomEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightningStrike(EntityStruckByLightningEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof EntityMooshroom)) {
            return;
        }
        EntityMooshroom mooshroom = (EntityMooshroom) event.getEntity();
        if (FFDMooshroomData.wasStruckBy(mooshroom, event.getLightning().getUniqueID())) {
            return;
        }
        FFDMooshroomData.setBrown(mooshroom, !FFDMooshroomData.isBrown(mooshroom));
        FFDNetwork.sendMooshroomState(mooshroom, FFDMooshroomData.isBrown(mooshroom));
        mooshroom.playSound(FFDSounds.MOOSHROOM_CONVERT, 2.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onBabyMooshroomSpawn(BabyEntitySpawnEvent event) {
        if (!(event.getChild() instanceof EntityMooshroom)
                || !(event.getParentA() instanceof EntityMooshroom)
                || !(event.getParentB() instanceof EntityMooshroom)) {
            return;
        }
        EntityMooshroom child = (EntityMooshroom) event.getChild();
        EntityMooshroom first = (EntityMooshroom) event.getParentA();
        EntityMooshroom second = (EntityMooshroom) event.getParentB();
        boolean firstBrown = FFDMooshroomData.isBrown(first);
        boolean secondBrown = FFDMooshroomData.isBrown(second);
        boolean childBrown = firstBrown == secondBrown
                ? first.getRNG().nextInt(1024) == 0 ? !firstBrown : firstBrown
                : first.getRNG().nextBoolean() ? firstBrown : secondBrown;
        FFDMooshroomData.setBrown(child, childBrown);
    }

    @SubscribeEvent
    public static void onStartTrackingMooshroom(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP
                && event.getTarget() instanceof EntityMooshroom) {
            EntityMooshroom mooshroom = (EntityMooshroom) event.getTarget();
            FFDNetwork.sendMooshroomState((EntityPlayerMP) event.getEntityPlayer(), mooshroom,
                    FFDMooshroomData.isBrown(mooshroom));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMooshroomInteraction(EntityInteract event) {
        if (!(event.getTarget() instanceof EntityMooshroom)) {
            return;
        }
        EntityMooshroom mooshroom = (EntityMooshroom) event.getTarget();
        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = event.getItemStack();
        if (held.getItem() == Items.BOWL && mooshroom.getGrowingAge() >= 0) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            if (!event.getWorld().isRemote) {
                PotionEffect stored = FFDMooshroomData.getStewEffect(mooshroom);
                ItemStack result = stored == null ? ItemStack.EMPTY
                        : FFDMooshroomData.createStew(stored);
                boolean suspicious = !result.isEmpty();
                if (result.isEmpty()) {
                    result = new ItemStack(Items.MUSHROOM_STEW);
                }
                FFDMooshroomData.clearStewEffect(mooshroom);
                giveStew(player, event.getHand(), held, result);
                if (suspicious) {
                    mooshroom.playSound(FFDSounds.MOOSHROOM_SUSPICIOUS_MILK, 1.0F, 1.0F);
                } else {
                    mooshroom.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
                }
            }
            return;
        }
        if (!FFDMooshroomData.isBrown(mooshroom) || mooshroom.getGrowingAge() < 0) {
            return;
        }
        PotionEffect effect = FFDMooshroomData.getFlowerEffect(held);
        if (effect == null || FFDItems.effectiveItem(FFDItems.SUSPICIOUS_STEW) == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        if (event.getWorld().isRemote) {
            return;
        }
        if (FFDMooshroomData.getStewEffect(mooshroom) != null) {
            WorldServer world = (WorldServer) event.getWorld();
            spawnMooshroomParticles(world, mooshroom, EnumParticleTypes.SMOKE_NORMAL, 2, 0);
            return;
        }
        if (!player.capabilities.isCreativeMode) {
            held.shrink(1);
        }
        FFDMooshroomData.setStewEffect(mooshroom, effect);
        spawnMooshroomParticles((WorldServer) event.getWorld(), mooshroom,
                EnumParticleTypes.SPELL_MOB, 4, -1);
        mooshroom.playSound(FFDSounds.MOOSHROOM_EAT, 2.0F, 1.0F);
    }

    private static void spawnMooshroomParticles(WorldServer world, EntityMooshroom mooshroom,
                                                EnumParticleTypes type, int count, int color) {
        for (int i = 0; i < count; i++) {
            double x = mooshroom.posX + mooshroom.getRNG().nextDouble() / 2.0D;
            double y = mooshroom.posY + mooshroom.height * 0.5D;
            double z = mooshroom.posZ + mooshroom.getRNG().nextDouble() / 2.0D;
            double verticalSpeed = mooshroom.getRNG().nextDouble() / 5.0D;
            if (type == EnumParticleTypes.SPELL_MOB) {
                world.spawnParticle(type, x, y, z, 0, 0.0D, verticalSpeed, 0.0D,
                        1.0D, color);
            } else {
                world.spawnParticle(type, x, y, z, 0, 0.0D, verticalSpeed, 0.0D, 1.0D);
            }
        }
    }

    private static void giveStew(EntityPlayer player, net.minecraft.util.EnumHand hand,
                                 ItemStack bowl, ItemStack stew) {
        if (player.capabilities.isCreativeMode) {
            if (!player.inventory.addItemStackToInventory(stew)) {
                player.dropItem(stew, false);
            }
            return;
        }
        if (bowl.getCount() == 1) {
            player.setHeldItem(hand, stew);
            return;
        }
        bowl.shrink(1);
        if (!player.inventory.addItemStackToInventory(stew)) {
            player.dropItem(stew, false);
        }
    }
}
