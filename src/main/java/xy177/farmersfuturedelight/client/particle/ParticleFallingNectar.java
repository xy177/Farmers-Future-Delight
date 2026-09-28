package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleFallingNectar extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "particle/drip_fall");

    private ParticleFallingNectar(World world, double x, double y, double z) {
        super(world, x, y, z);
        setSize(0.01F, 0.01F);
        setRBGColorF(0.92F, 0.782F, 0.72F);
        particleScale = 1.0F + rand.nextFloat();
        particleMaxAge = (int) (16.0D / (rand.nextFloat() * 0.8F + 0.2F));
        particleGravity = 0.007F;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleFallingNectar create(World world, double x, double y, double z) {
        return new ParticleFallingNectar(world, x, y, z);
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
        motionY -= particleGravity;
        move(motionX, motionY, motionZ);
        if (onGround) {
            setExpired();
            return;
        }
        motionX *= 0.98F;
        motionY *= 0.98F;
        motionZ *= 0.98F;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
