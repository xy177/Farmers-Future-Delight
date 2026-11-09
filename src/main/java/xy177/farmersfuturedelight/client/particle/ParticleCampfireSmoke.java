package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.Minecraft;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import net.minecraft.world.World;

public final class ParticleCampfireSmoke extends Particle {
    private ParticleCampfireSmoke(World world, double x, double y, double z, boolean signalFire) {
        super(world, x, y, z);
        setSize(0.25F, 0.25F);
        particleScale *= 3.0F;
        particleMaxAge = (signalFire ? 280 : 80) + rand.nextInt(50);
        particleAlpha = signalFire ? 0.95F : 0.9F;
        motionX = 0.0D;
        motionY = 0.07D + rand.nextFloat() / 500.0F;
        motionZ = 0.0D;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                FarmerFutureDelight.MODID + ":particle/big_smoke_" + rand.nextInt(12)));
    }

    public static ParticleCampfireSmoke create(World world, double x, double y, double z,
                                                boolean signalFire) {
        return new ParticleCampfireSmoke(world, x, y, z, signalFire);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge || particleAlpha <= 0.0F) {
            setExpired();
            return;
        }
        motionX += rand.nextFloat() / 5000.0F * (rand.nextBoolean() ? 1 : -1);
        motionZ += rand.nextFloat() / 5000.0F * (rand.nextBoolean() ? 1 : -1);
        motionY -= 0.000003F;
        move(motionX, motionY, motionZ);
        if (particleAge >= particleMaxAge - 60 && particleAlpha > 0.01F) {
            particleAlpha -= 0.015F;
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
