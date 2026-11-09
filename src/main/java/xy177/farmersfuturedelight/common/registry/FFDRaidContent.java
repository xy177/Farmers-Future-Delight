package xy177.farmersfuturedelight.common.registry;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.item.ItemOminousBottle;
import xy177.farmersfuturedelight.common.potion.PotionRaidEffect;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRaidContent {
    public static final Potion BAD_OMEN = new PotionRaidEffect("bad_omen", 745784, false);
    public static final Potion RAID_OMEN = new PotionRaidEffect("raid_omen", 14565464, false);
    public static final Potion HERO_OF_THE_VILLAGE =
            new PotionRaidEffect("hero_of_the_village", 0x44FF44, true);
    public static final Item OMINOUS_BOTTLE = new ItemOminousBottle()
            .setRegistryName(FarmerFutureDelight.MODID, "ominous_bottle")
            .setUnlocalizedName(FarmerFutureDelight.MODID + ".ominous_bottle");
    public static final SoundEvent OMINOUS_BOTTLE_DISPOSE = sound("item.ominous_bottle.dispose");
    public static final SoundEvent BAD_OMEN_APPLY = sound("event.mob_effect.bad_omen");
    public static final SoundEvent RAID_OMEN_APPLY = sound("event.mob_effect.raid_omen");
    public static final SoundEvent RAID_HORN = sound("event.raid.horn");
    public static final SoundEvent BELL_RESONATE = sound("block.bell.resonate");
    public static final SoundEvent VILLAGER_CELEBRATE = sound("entity.villager.celebrate");

    private FFDRaidContent() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(OMINOUS_BOTTLE);
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        event.getRegistry().registerAll(BAD_OMEN, RAID_OMEN, HERO_OF_THE_VILLAGE);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(OMINOUS_BOTTLE_DISPOSE, BAD_OMEN_APPLY,
                RAID_OMEN_APPLY, RAID_HORN, BELL_RESONATE, VILLAGER_CELEBRATE);
    }

    private static SoundEvent sound(String name) {
        ResourceLocation id = new ResourceLocation(FarmerFutureDelight.MODID, name);
        return new SoundEvent(id).setRegistryName(id);
    }
}
