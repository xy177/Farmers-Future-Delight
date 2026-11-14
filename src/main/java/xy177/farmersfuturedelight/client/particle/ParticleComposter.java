package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleComposter extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "particle/generic_0");

    private ParticleComposter(World world, double x, double y, double z,
                              double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        setSize(0.02F, 0.02F);
        this.motionX *= 0.02D;
        this.motionY *= 0.02D;
        this.motionZ *= 0.02D;
        particleScale *= rand.nextFloat() * 0.6F + 0.5F;
        particleMaxAge = 3 + rand.nextInt(5);
        setRBGColorF(1.0F, 1.0F, 1.0F);
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleComposter create(World world, double x, double y, double z,
                                           double motionX, double motionY, double motionZ) {
        return new ParticleComposter(world, x, y, z, motionX, motionY, motionZ);
    }

    @Override
    public void move(double x, double y, double z) {
        setBoundingBox(getBoundingBox().offset(x, y, z));
        resetPositionToBB();
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
        motionX *= 0.99D;
        motionY *= 0.99D;
        motionZ *= 0.99D;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
