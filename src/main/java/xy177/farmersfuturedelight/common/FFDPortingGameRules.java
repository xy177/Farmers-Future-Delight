package xy177.farmersfuturedelight.common;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPortingGameRules {
    public static final String SPAWN_PHANTOMS = "minecraft:spawn_phantoms";
    public static final String IMMEDIATE_RESPAWN = "minecraft:immediate_respawn";
    public static final String DROWNING_DAMAGE = "minecraft:drowning_damage";
    public static final String FALL_DAMAGE = "minecraft:fall_damage";
    public static final String FIRE_DAMAGE = "minecraft:fire_damage";
    public static final String UNIVERSAL_ANGER = "minecraft:universal_anger";
    public static final String FORGIVE_DEAD_PLAYERS = "minecraft:forgive_dead_players";
    private static final Map<EntityPlayerMP, RuleSnapshot> LAST_SENT = new WeakHashMap<>();

    private FFDPortingGameRules() {
    }

    public static void ensure(GameRules rules) {
        ensureBoolean(rules, SPAWN_PHANTOMS, true, "doInsomnia");
        ensureBoolean(rules, IMMEDIATE_RESPAWN, false, "doImmediateRespawn");
        ensureBoolean(rules, DROWNING_DAMAGE, true, "drowningDamage");
        ensureBoolean(rules, FALL_DAMAGE, true, "fallDamage");
        ensureBoolean(rules, FIRE_DAMAGE, true, "fireDamage");
        ensureBoolean(rules, UNIVERSAL_ANGER, false, "universalAnger");
        ensureBoolean(rules, FORGIVE_DEAD_PLAYERS, true, "forgiveDeadPlayers");
    }

    private static void ensureBoolean(GameRules rules, String name, boolean defaultValue,
                                     String legacyName) {
        if (rules.hasRule(name)) {
            return;
        }
        boolean value = rules.hasRule(legacyName)
                ? rules.getBoolean(legacyName) : defaultValue;
        rules.addGameRule(name, Boolean.toString(value), GameRules.ValueType.BOOLEAN_VALUE);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDamage(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        GameRules rules = entity.world.getGameRules();
        ensure(rules);
        DamageSource source = event.getSource();
        String type = source.getDamageType();
        if ("drown".equals(type) && !rules.getBoolean(DROWNING_DAMAGE)
                || "fall".equals(type) && !rules.getBoolean(FALL_DAMAGE)
                || source.isFireDamage() && !rules.getBoolean(FIRE_DAMAGE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncImmediateRespawn(event.player, true);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            LAST_SENT.remove(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END
                && event.player instanceof EntityPlayerMP) {
            syncImmediateRespawn(event.player, false);
        }
    }

    private static void syncImmediateRespawn(EntityPlayer player, boolean force) {
        if (!(player instanceof EntityPlayerMP) || player.world.isRemote) {
            return;
        }
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        GameRules rules = serverPlayer.world.getGameRules();
        ensure(rules);
        boolean enabled = rules.getBoolean(IMMEDIATE_RESPAWN);
        RuleSnapshot previous = LAST_SENT.get(serverPlayer);
        if (!force && previous != null && previous.dimension == serverPlayer.dimension
                && previous.enabled == enabled) {
            return;
        }
        LAST_SENT.put(serverPlayer, new RuleSnapshot(serverPlayer.dimension, enabled));
        FFDNetwork.sendImmediateRespawnRule(serverPlayer, enabled);
    }

    private static final class RuleSnapshot {
        private final int dimension;
        private final boolean enabled;

        private RuleSnapshot(int dimension, boolean enabled) {
            this.dimension = dimension;
            this.enabled = enabled;
        }
    }
}
