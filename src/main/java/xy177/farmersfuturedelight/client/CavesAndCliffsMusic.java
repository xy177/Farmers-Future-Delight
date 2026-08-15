package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class CavesAndCliffsMusic {
    private CavesAndCliffsMusic() {
    }

    @SubscribeEvent
    public static void onSoundLoad(SoundLoadEvent event) {
        if (!FFDConfig.cavesAndCliffsBackgroundMusic) {
            return;
        }

        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        SoundEventAccessor music = handler.getAccessor(
                FFDSounds.CAVES_AND_CLIFFS_MUSIC.getSoundName());
        if (music == null || music.getWeight() == 0) {
            return;
        }

        addToPool(handler, SoundEvents.MUSIC_GAME, music);
        addToPool(handler, SoundEvents.MUSIC_MENU, music);
    }

    private static void addToPool(SoundHandler handler, net.minecraft.util.SoundEvent event,
                                  SoundEventAccessor music) {
        SoundEventAccessor pool = handler.getAccessor(event.getSoundName());
        if (pool != null) {
            pool.addSound(music);
        }
    }
}
