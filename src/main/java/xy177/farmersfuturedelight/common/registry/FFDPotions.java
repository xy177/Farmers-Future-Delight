package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.potion.PotionSlowFalling;
import xy177.farmersfuturedelight.common.potion.PotionConduitPower;
import xy177.farmersfuturedelight.common.potion.PotionDolphinsGrace;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPotions {
    private static final ResourceLocation OE_TURTLE_MASTER =
            new ResourceLocation("oe", "turtle_master");
    private static final ResourceLocation OE_LONG_TURTLE_MASTER =
            new ResourceLocation("oe", "turtle_master_long");
    private static final ResourceLocation OE_STRONG_TURTLE_MASTER =
            new ResourceLocation("oe", "turtle_master_strong");
    private static final ResourceLocation PHANTOMS_SLOW_FALLING =
            new ResourceLocation("phantoms", "slow_falling");
    private static final ResourceLocation OE_DOLPHINS_GRACE =
            new ResourceLocation("oe", "dolphins_grace");
    private static final ResourceLocation OE_CONDUIT_POWER =
            new ResourceLocation("oe", "conduit_power");
    public static final Potion SLOW_FALLING = new PotionSlowFalling()
            .setRegistryName(FarmerFutureDelight.MODID, "slow_falling");
    public static final Potion CONDUIT_POWER = new PotionConduitPower()
            .setRegistryName(FarmerFutureDelight.MODID, "conduit_power");
    public static final Potion DOLPHINS_GRACE = new PotionDolphinsGrace()
            .setRegistryName(FarmerFutureDelight.MODID, "dolphins_grace");
    public static final PotionType SLOW_FALLING_TYPE = slowFalling("slow_falling", 90 * 20);
    public static final PotionType LONG_SLOW_FALLING = slowFalling("long_slow_falling", 240 * 20);
    public static final PotionType TURTLE_MASTER = turtleMaster("turtle_master", 20 * 20, 3, 2);
    public static final PotionType LONG_TURTLE_MASTER = turtleMaster("long_turtle_master", 40 * 20, 3, 2);
    public static final PotionType STRONG_TURTLE_MASTER = turtleMaster("strong_turtle_master", 20 * 20, 5, 3);

    private FFDPotions() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        if (shouldRegisterLocalSlowFalling(
                event.getRegistry().containsKey(PHANTOMS_SLOW_FALLING))) {
            event.getRegistry().register(SLOW_FALLING);
        }
        if (shouldRegisterLocalConduitPower(
                event.getRegistry().containsKey(OE_CONDUIT_POWER))) {
            event.getRegistry().register(CONDUIT_POWER);
        }
        if (shouldRegisterLocalDolphinsGrace(
                event.getRegistry().containsKey(OE_DOLPHINS_GRACE))) {
            event.getRegistry().register(DOLPHINS_GRACE);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        if (shouldRegisterLocalSlowFalling(
                ForgeRegistries.POTIONS.containsKey(PHANTOMS_SLOW_FALLING))) {
            event.getRegistry().registerAll(SLOW_FALLING_TYPE, LONG_SLOW_FALLING);
            ItemStack membrane = FFDItems.effectiveStack(FFDItems.PHANTOM_MEMBRANE);
            if (!membrane.isEmpty()) {
                PotionHelper.addMix(PotionTypes.AWKWARD, membrane.getItem(),
                        SLOW_FALLING_TYPE);
            }
            PotionHelper.addMix(SLOW_FALLING_TYPE, Items.REDSTONE, LONG_SLOW_FALLING);
        }

        if (FFDEntities.isTurtleEnabled()) {
            boolean localTurtleMaster = !event.getRegistry().containsKey(OE_TURTLE_MASTER);
            boolean localLongTurtleMaster = !event.getRegistry().containsKey(OE_LONG_TURTLE_MASTER);
            boolean localStrongTurtleMaster = !event.getRegistry().containsKey(OE_STRONG_TURTLE_MASTER);
            PotionType turtleMaster = localTurtleMaster ? TURTLE_MASTER
                    : event.getRegistry().getValue(OE_TURTLE_MASTER);
            PotionType longTurtleMaster = localLongTurtleMaster ? LONG_TURTLE_MASTER
                    : event.getRegistry().getValue(OE_LONG_TURTLE_MASTER);
            PotionType strongTurtleMaster = localStrongTurtleMaster ? STRONG_TURTLE_MASTER
                    : event.getRegistry().getValue(OE_STRONG_TURTLE_MASTER);
            if (localTurtleMaster) {
                event.getRegistry().register(TURTLE_MASTER);
            }
            if (localLongTurtleMaster) {
                event.getRegistry().register(LONG_TURTLE_MASTER);
            }
            if (localStrongTurtleMaster) {
                event.getRegistry().register(STRONG_TURTLE_MASTER);
            }
            ItemStack helmet = FFDItems.effectiveStack(FFDItems.TURTLE_HELMET);
            if (localTurtleMaster && !helmet.isEmpty()) {
                PotionHelper.addMix(PotionTypes.AWKWARD, helmet.getItem(), turtleMaster);
            }
            if (localLongTurtleMaster) {
                PotionHelper.addMix(turtleMaster, Items.REDSTONE, longTurtleMaster);
            }
            if (localStrongTurtleMaster) {
                PotionHelper.addMix(turtleMaster, Items.GLOWSTONE_DUST, strongTurtleMaster);
            }
        }
    }

    private static boolean shouldRegisterLocalSlowFalling(boolean externalRegistered) {
        if (FFDConfig.phantomMode == FFDConfig.FeatureMode.ENABLED) {
            return true;
        }
        if (FFDConfig.phantomMode == FFDConfig.FeatureMode.DISABLED) {
            return false;
        }
        return !externalRegistered || !FFDConfig.isAutoCompatibilityEnabled(
                "slow_falling", "phantoms");
    }

    private static boolean shouldRegisterLocalDolphinsGrace(boolean externalRegistered) {
        if (FFDConfig.dolphinMode == FFDConfig.FeatureMode.ENABLED) {
            return true;
        }
        if (FFDConfig.dolphinMode == FFDConfig.FeatureMode.DISABLED) {
            return false;
        }
        return !externalRegistered || !FFDConfig.isAutoCompatibilityEnabled(
                "dolphins_grace", "oe");
    }

    private static boolean shouldRegisterLocalConduitPower(boolean externalRegistered) {
        if (FFDConfig.conduitMode == FFDConfig.FeatureMode.ENABLED) {
            return true;
        }
        if (FFDConfig.conduitMode == FFDConfig.FeatureMode.DISABLED) {
            return false;
        }
        return FFDItems.isBlockRegistered(FFDBlocks.CONDUIT) || !externalRegistered
                || !FFDConfig.isAutoCompatibilityEnabled("conduit_power", "oe");
    }

    public static Potion effectiveDolphinsGrace() {
        Potion external = ForgeRegistries.POTIONS.getValue(OE_DOLPHINS_GRACE);
        if (FFDConfig.dolphinMode == FFDConfig.FeatureMode.AUTO && external != null
                && FFDConfig.isAutoCompatibilityEnabled("dolphins_grace", "oe")) {
            return external;
        }
        Potion local = ForgeRegistries.POTIONS.getValue(DOLPHINS_GRACE.getRegistryName());
        return local == null ? external : local;
    }

    public static boolean hasDolphinsGrace(net.minecraft.entity.EntityLivingBase entity) {
        if (entity == null) {
            return false;
        }
        Potion local = ForgeRegistries.POTIONS.getValue(DOLPHINS_GRACE.getRegistryName());
        Potion external = ForgeRegistries.POTIONS.getValue(OE_DOLPHINS_GRACE);
        return local != null && entity.isPotionActive(local)
                || external != null && entity.isPotionActive(external);
    }

    public static boolean hasConduitPower(net.minecraft.entity.EntityLivingBase entity) {
        return conduitPowerAmplifier(entity) >= 0;
    }

    public static int conduitPowerAmplifier(net.minecraft.entity.EntityLivingBase entity) {
        if (entity == null) {
            return -1;
        }
        Potion local = ForgeRegistries.POTIONS.getValue(CONDUIT_POWER.getRegistryName());
        Potion external = ForgeRegistries.POTIONS.getValue(OE_CONDUIT_POWER);
        int amplifier = -1;
        if (local != null && entity.isPotionActive(local)) {
            amplifier = entity.getActivePotionEffect(local).getAmplifier();
        }
        if (external != null && entity.isPotionActive(external)) {
            amplifier = Math.max(amplifier,
                    entity.getActivePotionEffect(external).getAmplifier());
        }
        return amplifier;
    }

    private static PotionType slowFalling(String registryName, int duration) {
        return new PotionType(FarmerFutureDelight.MODID + ".slow_falling",
                new PotionEffect(SLOW_FALLING, duration))
                .setRegistryName(FarmerFutureDelight.MODID, registryName);
    }

    private static PotionType turtleMaster(String name, int duration, int slownessAmplifier,
                                           int resistanceAmplifier) {
        return new PotionType(FarmerFutureDelight.MODID + "." + name,
                new PotionEffect(MobEffects.SLOWNESS, duration, slownessAmplifier),
                new PotionEffect(MobEffects.RESISTANCE, duration, resistanceAmplifier))
                .setRegistryName(FarmerFutureDelight.MODID, name);
    }
}
