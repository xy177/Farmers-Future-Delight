package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRaidEffects {
    public static final String ANCHOR_TAG = "FFDRaidOmenPosition";
    private static final String EXPIRED_TAG = "FFDRaidOmenExpired";

    private FFDRaidEffects() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRemoved(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() == FFDRaidContent.RAID_OMEN && !event.isCanceled()) {
            event.getEntityLiving().getEntityData().removeTag(ANCHOR_TAG);
            event.getEntityLiving().getEntityData().removeTag(EXPIRED_TAG);
        }
    }

    @SubscribeEvent
    public static void onExpired(PotionEvent.PotionExpiryEvent event) {
        if (!event.getEntityLiving().world.isRemote && event.getPotionEffect() != null
                && event.getPotionEffect().getPotion() == FFDRaidContent.RAID_OMEN) {
            event.getEntityLiving().getEntityData().setInteger(EXPIRED_TAG,
                    event.getPotionEffect().getAmplifier() + 1);
        }
    }

    @SubscribeEvent
    public static void onAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getEntityLiving().world.isRemote || event.getOldPotionEffect() != null) return;
        if (event.getPotionEffect().getPotion() == FFDRaidContent.BAD_OMEN) {
            event.getEntityLiving().world.playSound(null, event.getEntityLiving().getPosition(),
                    FFDRaidContent.BAD_OMEN_APPLY, SoundCategory.PLAYERS, 1.0F, 1.0F);
        } else if (event.getPotionEffect().getPotion() == FFDRaidContent.RAID_OMEN) {
            event.getEntityLiving().world.playSound(null, event.getEntityLiving().getPosition(),
                    FFDRaidContent.RAID_OMEN_APPLY, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        for (EntityPlayer player : new ArrayList<>(world.playerEntities)) {
            if (!(player instanceof EntityPlayerMP)) continue;
            EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
            NBTTagCompound data = player.getEntityData();
            if (data.hasKey(EXPIRED_TAG, 99)) {
                int level = data.getInteger(EXPIRED_TAG);
                data.removeTag(EXPIRED_TAG);
                if (!player.isPotionActive(FFDRaidContent.RAID_OMEN)) {
                    if (data.hasKey(ANCHOR_TAG, 10) && player.isEntityAlive() && !player.isSpectator()) {
                        NBTTagCompound anchor = data.getCompoundTag(ANCHOR_TAG);
                        FFDRaidManager.createOrExtend(serverPlayer,
                                new BlockPos(anchor.getInteger("X"), anchor.getInteger("Y"),
                                        anchor.getInteger("Z")), Math.max(1, Math.min(5, level)));
                    }
                    data.removeTag(ANCHOR_TAG);
                }
            }
            PotionEffect badOmen = player.getActivePotionEffect(FFDRaidContent.BAD_OMEN);
            if (badOmen == null || !player.isEntityAlive() || player.isSpectator()
                    || world.getDifficulty() == EnumDifficulty.PEACEFUL
                    || !RaidVillages.isVillage(world, player.getPosition())
                    || !FFDRaidManager.canAcceptOmen(world, player.getPosition())) continue;
            int amplifier = badOmen.getAmplifier();
            player.removePotionEffect(FFDRaidContent.BAD_OMEN);
            if (player.isPotionActive(FFDRaidContent.BAD_OMEN)) continue;
            player.addPotionEffect(new PotionEffect(FFDRaidContent.RAID_OMEN, 600, amplifier));
            if (player.isPotionActive(FFDRaidContent.RAID_OMEN)) {
                BlockPos position = player.getPosition();
                NBTTagCompound anchor = new NBTTagCompound();
                anchor.setInteger("X", position.getX());
                anchor.setInteger("Y", position.getY());
                anchor.setInteger("Z", position.getZ());
                data.setTag(ANCHOR_TAG, anchor);
            }
        }
    }
}
