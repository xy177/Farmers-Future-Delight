package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.proxy.ClientProxy;

public final class ParticleHoneyDrip extends Particle {
    private static final ResourceLocation HANG_TEXTURE = id("particle/drip_hang");
    private static final ResourceLocation FALL_TEXTURE = id("particle/drip_fall");
    private static final ResourceLocation LAND_TEXTURE = id("particle/drip_land");
    private Phase phase = Phase.HANG;

    private ParticleHoneyDrip(World world, double x, double y, double z) {
        super(world, x, y, z);
        setSize(0.01F, 0.01F);
        particleMaxAge = 100;
        particleGravity = 0.06F * 0.02F * 0.01F;
        particleScale = 1.0F + rand.nextFloat();
        setTexture(HANG_TEXTURE);
        setRBGColorF(0.622F, 0.508F, 0.082F);
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
            if (phase == Phase.HANG && ClientProxy.shouldSpawnBeeParticle(world, posX, posY, posZ)) {
                phase = Phase.FALL;
                particleAge = 0;
                particleMaxAge = (int) (64.0D / (rand.nextFloat() * 0.8F + 0.2F));
                particleGravity = 0.01F;
                particleScale = 1.0F + rand.nextFloat();
                motionX = 0.0D;
                motionY = 0.0D;
                motionZ = 0.0D;
                setTexture(FALL_TEXTURE);
                setRBGColorF(0.582F, 0.448F, 0.082F);
            } else {
                setExpired();
            }
            return;
        }
        motionY -= particleGravity;
        move(motionX, motionY, motionZ);
        if (phase == Phase.HANG) {
            motionX *= 0.02D * 0.98F;
            motionY *= 0.02D * 0.98F;
            motionZ *= 0.02D * 0.98F;
        } else {
            motionX *= 0.98F;
            motionY *= 0.98F;
            motionZ *= 0.98F;
            if (phase == Phase.FALL && onGround) {
                world.playSound(posX, posY, posZ, FFDSounds.BEEHIVE_DRIP,
                        SoundCategory.BLOCKS, 0.3F + rand.nextFloat() * 0.7F, 1.0F, false);
                if (!ClientProxy.shouldSpawnBeeParticle(world, posX, posY, posZ)) {
                    setExpired();
                    return;
                }
                phase = Phase.LAND;
                particleAge = 0;
                particleMaxAge = (int) (128.0D / (rand.nextFloat() * 0.8F + 0.2F));
                particleGravity = 0.0F;
                particleScale = 1.0F + rand.nextFloat();
                motionX = 0.0D;
                motionY = 0.0D;
                motionZ = 0.0D;
                setTexture(LAND_TEXTURE);
                setRBGColorF(0.522F, 0.408F, 0.082F);
            }
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    private void setTexture(ResourceLocation texture) {
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(texture.toString()));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }

    private enum Phase {
        HANG,
        FALL,
        LAND
    }
}
