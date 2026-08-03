package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class ParticleHoneyDrip extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "particle/drip_fall");

    private ParticleHoneyDrip(World world, double x, double y, double z) {
        super(world, x, y, z);
        setSize(0.01F, 0.01F);
        setRBGColorF(1.0F, 0.64F, 0.08F);
        particleScale = 0.7F;
        particleMaxAge = 40;
        particleGravity = 0.06F;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleHoneyDrip create(World world, double x, double y, double z) {
        return new ParticleHoneyDrip(world, x, y, z);
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
        motionY -= 0.01D * particleGravity;
        move(motionX, motionY, motionZ);
        if (onGround) {
            world.playSound(posX, posY, posZ, FFDSounds.BEEHIVE_DRIP,
                    SoundCategory.BLOCKS, 1.0F, 1.0F, false);
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
