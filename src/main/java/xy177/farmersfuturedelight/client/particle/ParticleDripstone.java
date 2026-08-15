package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class ParticleDripstone extends Particle {
    private static final ResourceLocation HANG_TEXTURE = id("particle/drip_hang");
    private static final ResourceLocation FALL_TEXTURE = id("particle/drip_fall");
    private static final ResourceLocation LAND_TEXTURE = id("particle/drip_land");

    private final boolean lava;
    private Phase phase = Phase.HANG;

    private ParticleDripstone(World world, double x, double y, double z, boolean lava) {
        super(world, x, y, z);
        this.lava = lava;
        setSize(0.01F, 0.01F);
        particleMaxAge = 40;
        particleGravity = 0.06F * 0.02F;
        setTexture(HANG_TEXTURE);
        updateColor();
    }

    public static ParticleDripstone create(World world, double x, double y, double z,
                                            boolean lava) {
        return new ParticleDripstone(world, x, y, z, lava);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        updateColor();

        if (particleAge++ >= particleMaxAge) {
            if (phase == Phase.HANG) {
                phase = Phase.FALL;
                resetPhase((int) (64.0D / (rand.nextFloat() * 0.8F + 0.2F)),
                        FALL_TEXTURE);
                particleGravity = 0.06F;
            } else {
                setExpired();
            }
            return;
        }

        motionY -= particleGravity;
        move(motionX, motionY, motionZ);
        if (phase == Phase.HANG) {
            motionX *= 0.0196D;
            motionY *= 0.0196D;
            motionZ *= 0.0196D;
        } else {
            motionX *= 0.98D;
            motionY *= 0.98D;
            motionZ *= 0.98D;
            if (phase == Phase.FALL && onGround) {
                land();
            }
        }
    }

    private void land() {
        world.playSound(posX, posY, posZ,
                lava ? FFDSounds.POINTED_DRIPSTONE_DRIP_LAVA
                        : FFDSounds.POINTED_DRIPSTONE_DRIP_WATER,
                SoundCategory.BLOCKS, 0.3F + rand.nextFloat() * 0.7F, 1.0F, false);
        if (!lava) {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                    posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            setExpired();
            return;
        }
        phase = Phase.LAND;
        resetPhase((int) (16.0D / (rand.nextFloat() * 0.8F + 0.2F)), LAND_TEXTURE);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        particleGravity = 0.06F;
    }

    private void resetPhase(int maxAge, ResourceLocation texture) {
        particleAge = 0;
        particleMaxAge = maxAge;
        particleScale = (rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        setTexture(texture);
    }

    private void updateColor() {
        if (!lava) {
            setRBGColorF(0.2F, 0.3F, 1.0F);
            return;
        }
        if (phase == Phase.HANG) {
            setRBGColorF(1.0F,
                    16.0F / (40.0F - particleMaxAge + particleAge + 16.0F),
                    4.0F / (40.0F - particleMaxAge + particleAge + 8.0F));
        } else {
            setRBGColorF(1.0F, 0.2857143F, 0.083333336F);
        }
    }

    private void setTexture(ResourceLocation texture) {
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(texture.toString()));
    }

    @Override
    public int getFXLayer() {
        return 1;
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
