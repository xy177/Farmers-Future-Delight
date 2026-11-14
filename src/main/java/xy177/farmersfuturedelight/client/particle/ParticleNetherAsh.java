package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleNetherAsh extends Particle {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FarmerFutureDelight.MODID, "particle/generic_0");
    private final float initialScale;

    private ParticleNetherAsh(World world, double x, double y, double z, boolean white) {
        super(world, x, y, z, 0, 0, 0);
        motionX *= .1D;
        motionY *= -.1D;
        motionZ *= .1D;
        if (white) {
            motionX += world.rand.nextFloat() * -1.9D * world.rand.nextFloat() * .1D;
            motionY += world.rand.nextFloat() * -.5D * world.rand.nextFloat() * .5D;
            motionZ += world.rand.nextFloat() * -1.9D * world.rand.nextFloat() * .1D;
        }
        float shade = rand.nextFloat() * (white ? 0 : .5F);
        setRBGColorF(white ? 0xBA / 255.0F : shade,
                white ? 0xB1 / 255.0F : shade, white ? 0xC2 / 255.0F : shade);
        particleScale *= .75F;
        initialScale = particleScale;
        particleMaxAge = Math.max(1, (int) (20 / (rand.nextFloat() * .8D + .2D)));
        particleGravity = white ? .0125F : .1F;
        canCollide = false;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleNetherAsh create(World world, double x, double y, double z, boolean white) {
        return new ParticleNetherAsh(world, x, y, z, white);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) { setExpired(); return; }
        motionY -= .04D * particleGravity;
        move(motionX, motionY, motionZ);
        motionX *= .96D;
        motionY *= .96D;
        motionZ *= .96D;
        particleScale = initialScale * Math.min(1, particleAge * 32.0F / particleMaxAge);
    }

    @Override public int getFXLayer() { return 1; }
}
