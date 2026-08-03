package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleSporeBlossom extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "particle/drip_fall");
    private final boolean ambient;

    private ParticleSporeBlossom(World world, double x, double y, double z, boolean ambient) {
        super(world, x, y, z);
        this.ambient = ambient;
        setSize(0.01F, 0.01F);
        setRBGColorF(0.32F, 0.5F, 0.22F);
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
        if (ambient) {
            canCollide = false;
            motionY = -0.003D;
            particleScale *= rand.nextFloat() * 0.6F + 0.6F;
            particleMaxAge = 500 + rand.nextInt(501);
        } else {
            canCollide = true;
            particleMaxAge = (int) (64.0F / (0.1F + rand.nextFloat() * 0.8F));
        }
    }

    public static ParticleSporeBlossom create(World world, double x, double y, double z,
                                               boolean ambient) {
        return new ParticleSporeBlossom(world, x, y, z, ambient);
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
        motionY -= ambient ? 0.0004D : 0.005D;
        move(motionX, motionY, motionZ);
        if (!ambient && onGround) {
            setExpired();
        }
        motionX *= 0.98D;
        motionY *= 0.98D;
        motionZ *= 0.98D;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
