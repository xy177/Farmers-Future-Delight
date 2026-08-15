package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.potion.PotionSlowFalling;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPotions {
    public static final Potion SLOW_FALLING = new PotionSlowFalling()
            .setRegistryName(FarmerFutureDelight.MODID, "slow_falling");
    public static final PotionType SLOW_FALLING_TYPE = slowFalling("slow_falling", 90 * 20);
    public static final PotionType LONG_SLOW_FALLING = slowFalling("long_slow_falling", 240 * 20);
    public static final PotionType TURTLE_MASTER = turtleMaster("turtle_master", 20 * 20, 3, 2);
    public static final PotionType LONG_TURTLE_MASTER = turtleMaster("long_turtle_master", 40 * 20, 3, 2);
    public static final PotionType STRONG_TURTLE_MASTER = turtleMaster("strong_turtle_master", 20 * 20, 5, 3);

    private FFDPotions() {
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        if (FFDItems.isPhantomEnabled()) {
            event.getRegistry().register(SLOW_FALLING);
        }
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        if (FFDItems.isPhantomEnabled()) {
            event.getRegistry().registerAll(SLOW_FALLING_TYPE, LONG_SLOW_FALLING);
            PotionHelper.addMix(PotionTypes.AWKWARD, FFDItems.PHANTOM_MEMBRANE,
                    SLOW_FALLING_TYPE);
            PotionHelper.addMix(SLOW_FALLING_TYPE, Items.REDSTONE, LONG_SLOW_FALLING);
        }

        if (FFDEntities.isTurtleEnabled()) {
            event.getRegistry().registerAll(TURTLE_MASTER, LONG_TURTLE_MASTER,
                    STRONG_TURTLE_MASTER);
            PotionHelper.addMix(PotionTypes.AWKWARD, FFDItems.TURTLE_HELMET, TURTLE_MASTER);
            PotionHelper.addMix(TURTLE_MASTER, Items.REDSTONE, LONG_TURTLE_MASTER);
            PotionHelper.addMix(TURTLE_MASTER, Items.GLOWSTONE_DUST, STRONG_TURTLE_MASTER);
        }
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
