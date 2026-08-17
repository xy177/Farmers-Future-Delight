package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class ParticleSmallFlame extends Particle {
    private final float flameScale;

    private ParticleSmallFlame(World world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        motionX *= 0.01D;
        motionY *= 0.01D;
        motionZ *= 0.01D;
        posX += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        posY += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        posZ += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
        flameScale = particleScale * 0.5F;
        particleScale = flameScale;
        particleRed = 1.0F;
        particleGreen = 1.0F;
        particleBlue = 1.0F;
        particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
        setParticleTextureIndex(48);
    }

    public static ParticleSmallFlame create(World world, double x, double y, double z) {
        return new ParticleSmallFlame(world, x, y, z);
    }

    @Override
    public void move(double x, double y, double z) {
        setBoundingBox(getBoundingBox().offset(x, y, z));
        resetPositionToBB();
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ,
                               float rotationXY, float rotationXZ) {
        float progress = (particleAge + partialTicks) / particleMaxAge;
        particleScale = flameScale * (1.0F - progress * progress * 0.5F);
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ,
                rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        float progress = MathHelper.clamp(
                (particleAge + partialTicks) / particleMaxAge, 0.0F, 1.0F);
        int brightness = super.getBrightnessForRender(partialTicks);
        int block = brightness & 255;
        int sky = brightness >> 16 & 255;
        block = Math.min(240, block + (int) (progress * 15.0F * 16.0F));
        return block | sky << 16;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
        }
        move(motionX, motionY, motionZ);
        motionX *= 0.96D;
        motionY *= 0.96D;
        motionZ *= 0.96D;
        if (onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
    }
}
