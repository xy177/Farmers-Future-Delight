package xy177.farmersfuturedelight.common.registry;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPotions {
    public static final PotionType TURTLE_MASTER = turtleMaster("turtle_master", 20 * 20, 3, 2);
    public static final PotionType LONG_TURTLE_MASTER = turtleMaster("long_turtle_master", 40 * 20, 3, 2);
    public static final PotionType STRONG_TURTLE_MASTER = turtleMaster("strong_turtle_master", 20 * 20, 5, 3);

    private FFDPotions() {
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        if (!FFDEntities.isTurtleEnabled()) {
            return;
        }

        event.getRegistry().registerAll(TURTLE_MASTER, LONG_TURTLE_MASTER, STRONG_TURTLE_MASTER);
        PotionHelper.addMix(PotionTypes.AWKWARD, FFDItems.TURTLE_HELMET, TURTLE_MASTER);
        PotionHelper.addMix(TURTLE_MASTER, Items.REDSTONE, LONG_TURTLE_MASTER);
        PotionHelper.addMix(TURTLE_MASTER, Items.GLOWSTONE_DUST, STRONG_TURTLE_MASTER);
    }

    private static PotionType turtleMaster(String name, int duration, int slownessAmplifier,
                                           int resistanceAmplifier) {
        return new PotionType(FarmerFutureDelight.MODID + "." + name,
                new PotionEffect(MobEffects.SLOWNESS, duration, slownessAmplifier),
                new PotionEffect(MobEffects.RESISTANCE, duration, resistanceAmplifier))
                .setRegistryName(FarmerFutureDelight.MODID, name);
    }
}
