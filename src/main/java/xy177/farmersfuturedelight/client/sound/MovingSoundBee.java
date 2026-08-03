package xy177.farmersfuturedelight.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@SideOnly(Side.CLIENT)
public class MovingSoundBee extends MovingSound {
    private final EntityBee bee;
    private final boolean aggressive;

    public MovingSoundBee(EntityBee bee) {
        this(bee, bee.getAnger() > 0);
    }

    private MovingSoundBee(EntityBee bee, boolean aggressive) {
        super(aggressive ? FFDSounds.BEE_LOOP_AGGRESSIVE : FFDSounds.BEE_LOOP,
                SoundCategory.NEUTRAL);
        this.bee = bee;
        this.aggressive = aggressive;
        repeat = true;
        repeatDelay = 0;
        volume = 0.01F;
        xPosF = (float) bee.posX;
        yPosF = (float) bee.posY;
        zPosF = (float) bee.posZ;
    }

    @Override
    public void update() {
        if (bee.isDead) {
            donePlaying = true;
            return;
        }
        boolean isAggressive = bee.getAnger() > 0;
        if (isAggressive != aggressive) {
            donePlaying = true;
            Minecraft.getMinecraft().getSoundHandler()
                    .playDelayedSound(new MovingSoundBee(bee, isAggressive), 0);
            return;
        }
        xPosF = (float) bee.posX;
        yPosF = (float) bee.posY;
        zPosF = (float) bee.posZ;
        if (bee.isSilent()) {
            volume = 0.0F;
            return;
        }
        float speed = MathHelper.sqrt(bee.motionX * bee.motionX + bee.motionZ * bee.motionZ);
        if (speed < 0.01F) {
            pitch = 0.0F;
            volume = 0.0F;
            return;
        }
        float minPitch = bee.isChild() ? 1.1F : 0.7F;
        float maxPitch = bee.isChild() ? 1.5F : 1.1F;
        pitch = minPitch + MathHelper.clamp(speed, minPitch, maxPitch)
                * (maxPitch - minPitch);
        volume = MathHelper.clamp(speed, 0.0F, 0.5F) * 1.2F;
    }
}
