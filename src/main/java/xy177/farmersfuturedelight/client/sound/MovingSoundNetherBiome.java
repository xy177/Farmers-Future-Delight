package xy177.farmersfuturedelight.client.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public final class MovingSoundNetherBiome extends MovingSound {
    private int fadeDirection = 1;
    private int fade;

    public MovingSoundNetherBiome(SoundEvent sound) {
        super(sound, SoundCategory.AMBIENT);
        repeat = true;
        repeatDelay = 0;
        attenuationType = ISound.AttenuationType.NONE;
        volume = 0.0F;
    }

    @Override
    public void update() {
        if (fade < 0) {
            donePlaying = true;
            return;
        }
        fade += fadeDirection;
        volume = Math.max(0.0F, Math.min(1.0F, fade / 40.0F));
    }

    public void fadeOut() {
        fade = Math.min(fade, 40);
        fadeDirection = -1;
    }

    public void fadeIn() {
        fade = Math.max(0, fade);
        fadeDirection = 1;
    }
}
