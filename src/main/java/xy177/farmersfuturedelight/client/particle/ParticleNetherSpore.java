package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleNetherSpore extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "particle/generic_0");

    private ParticleNetherSpore(World world, double x, double y, double z,
                                double motionX, double motionY, double motionZ,
                                boolean warped) {
        super(world, x, y - 0.125D, z);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        canCollide = false;
        particleGravity = 0.0F;
        particleScale *= rand.nextFloat() * 0.6F + 0.6F;
        particleMaxAge = (int) (16.0D / (rand.nextFloat() * 0.8D + 0.2D));
        setSize(warped ? 0.001F : 0.01F, warped ? 0.001F : 0.01F);
        setRBGColorF(warped ? 0.1F : 0.9F, warped ? 0.1F : 0.4F,
                warped ? 0.3F : 0.5F);
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleNetherSpore create(World world, double x, double y, double z,
                                              boolean warped) {
        if (warped) {
            double motionY = world.rand.nextFloat() * -1.9D
                    * world.rand.nextFloat() * 0.1D;
            return new ParticleNetherSpore(world, x, y, z, 0.0D, motionY, 0.0D, true);
        }
        return new ParticleNetherSpore(world, x, y, z,
                world.rand.nextGaussian() * 1.0E-6D,
                world.rand.nextGaussian() * 1.0E-4D,
                world.rand.nextGaussian() * 1.0E-6D, false);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        move(motionX, motionY, motionZ);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
