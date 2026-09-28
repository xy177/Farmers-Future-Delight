package xy177.farmersfuturedelight.common;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityTrident;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRiptide {
    private static final String TICKS_TAG = FarmerFutureDelight.MODID + ".riptideTicks";
    private static final Map<EntityPlayer, AxisAlignedBB> PREVIOUS_BOUNDS = new WeakHashMap<>();

    private FFDRiptide() {
    }

    public static void start(EntityPlayer player, int ticks) {
        setTicks(player, ticks);
        if (!player.world.isRemote && player instanceof EntityPlayerMP
                && player.world instanceof WorldServer) {
            FFDNetwork.sendRiptideState((EntityPlayerMP) player, ticks);
        }
    }

    public static void setClientTicks(EntityPlayer player, int ticks) {
        setTicks(player, ticks);
    }

    public static int getTicks(EntityPlayer player) {
        return player == null ? 0 : player.getEntityData().getInteger(TICKS_TAG);
    }

    public static boolean isActive(EntityPlayer player) {
        return getTicks(player) > 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        int ticks = getTicks(event.player);
        if (ticks <= 0) {
            PREVIOUS_BOUNDS.remove(event.player);
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            if (event.side == Side.SERVER) {
                PREVIOUS_BOUNDS.put(event.player, event.player.getEntityBoundingBox());
            }
            return;
        }
        if (event.side == Side.CLIENT) {
            setTicks(event.player, ticks - 1);
            return;
        }

        AxisAlignedBB previous = PREVIOUS_BOUNDS.remove(event.player);
        AxisAlignedBB area = previous == null ? event.player.getEntityBoundingBox()
                : previous.union(event.player.getEntityBoundingBox());
        boolean hit = attackFirstTarget(event.player, area);
        int remaining = hit || event.player.collidedHorizontally ? 0 : ticks - 1;
        if (hit) {
            event.player.motionX *= -0.2D;
            event.player.motionY *= -0.2D;
            event.player.motionZ *= -0.2D;
        }
        setTicks(event.player, remaining);
        if (remaining <= 0 && event.player instanceof EntityPlayerMP
                && event.player.world instanceof WorldServer) {
            FFDNetwork.sendRiptideState((EntityPlayerMP) event.player, 0);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityLivingBase)
                || event.getSource().getImmediateSource() instanceof EntityTrident) {
            return;
        }
        ItemStack held = ((EntityLivingBase) source).getHeldItemMainhand();
        if (!FFDItems.isTridentStack(held) || !FFDEnchantments.isAquatic(event.getEntityLiving())) {
            return;
        }
        int level = FFDEnchantments.getImpaling(held);
        if (level > 0) {
            event.setAmount(event.getAmount() + level * 2.5F);
        }
    }

    private static boolean attackFirstTarget(EntityPlayer player, AxisAlignedBB area) {
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, area);
        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase) || !entity.canBeCollidedWith()
                    || entity.isDead || entity.isRidingSameEntity(player)) {
                continue;
            }
            if (entity instanceof EntityPlayer
                    && !player.canAttackPlayer((EntityPlayer) entity)) {
                continue;
            }
            IAttributeInstance damage = player.getEntityAttribute(
                    SharedMonsterAttributes.ATTACK_DAMAGE);
            double baseDamage = damage.getBaseValue();
            damage.setBaseValue(baseDamage - 1.0D);
            try {
                player.attackTargetEntityWithCurrentItem(entity);
            } finally {
                damage.setBaseValue(baseDamage);
            }
            return true;
        }
        return false;
    }

    private static void setTicks(EntityPlayer player, int ticks) {
        NBTTagCompound data = player.getEntityData();
        if (ticks > 0) {
            data.setInteger(TICKS_TAG, ticks);
        } else {
            data.removeTag(TICKS_TAG);
        }
    }
}
