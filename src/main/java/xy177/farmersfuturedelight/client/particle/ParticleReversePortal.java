package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public final class ParticleReversePortal extends Particle {
    private final float initialScale;

    public ParticleReversePortal(World world, double x, double y, double z, double speed) {
        super(world, x, y, z);
        motionY = speed;
        initialScale = particleScale = (rand.nextFloat() * .5F + .2F) * 1.5F;
        float tint = rand.nextFloat() * .6F + .4F;
        setRBGColorF(tint * .9F, tint * .3F, tint);
        particleMaxAge = 60 + rand.nextInt(2);
        setParticleTextureIndex(rand.nextInt(8));
        canCollide = false;
    }

    @Override public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        float elapsed = (float) particleAge / particleMaxAge;
        move(0, motionY * elapsed, 0);
        particleScale = initialScale * (1.0F - elapsed / 1.5F);
    }

    @Override public int getBrightnessForRender(float partialTicks) {
        int original = super.getBrightnessForRender(partialTicks);
        float elapsed = ((float) particleAge + partialTicks) / particleMaxAge;
        elapsed *= elapsed;
        elapsed *= elapsed;
        return original & 255 | Math.min(240, (original >> 16 & 255) + (int) (elapsed * 240.0F)) << 16;
    }
}
