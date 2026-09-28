package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public class ParticleGlowSquid extends Particle {
    private static final ResourceLocation GLOW_TEXTURE = id("particle/glow");
    private static final ResourceLocation[] INK_TEXTURES = {
            id("particle/generic_7"), id("particle/generic_6"),
            id("particle/generic_5"), id("particle/generic_4"),
            id("particle/generic_3"), id("particle/generic_2"),
            id("particle/generic_1"), id("particle/generic_0")
    };
    private final boolean ink;
    private final boolean glowInk;

    private ParticleGlowSquid(World world, double x, double y, double z,
                              double motionX, double motionY, double motionZ,
                              boolean ink, boolean glowInk) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.ink = ink;
        this.glowInk = glowInk;
        canCollide = false;
        particleGravity = 0.0F;
        if (ink) {
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            particleScale = 5.0F;
            particleMaxAge = (int) (6.0F / (rand.nextFloat() * 0.8F + 0.2F));
            setRBGColorF(glowInk ? 0.2F : 0.0F,
                    glowInk ? 0.8F : 0.0F, glowInk ? 0.6F : 0.0F);
            setTexture(INK_TEXTURES[0]);
        } else {
            this.motionX *= 0.1D;
            this.motionY *= 0.2D;
            this.motionZ *= 0.1D;
            particleScale *= 0.75F;
            particleMaxAge = (int) (8.0D / (rand.nextDouble() * 0.8D + 0.2D));
            if (rand.nextBoolean()) {
                setRBGColorF(0.6F, 1.0F, 0.8F);
            } else {
                setRBGColorF(0.08F, 0.4F, 0.4F);
            }
            setTexture(GLOW_TEXTURE);
        }
    }

    public static ParticleGlowSquid ambient(World world, double x, double y, double z) {
        return new ParticleGlowSquid(world, x, y, z,
                0.5D - world.rand.nextDouble(), 0.0D, 0.5D - world.rand.nextDouble(), false, true);
    }

    public static ParticleGlowSquid ink(World world, double x, double y, double z,
                                        double motionX, double motionY, double motionZ) {
        return new ParticleGlowSquid(world, x, y, z, motionX, motionY, motionZ, true, true);
    }

    public static ParticleGlowSquid normalInk(World world, double x, double y, double z,
                                               double motionX, double motionY, double motionZ) {
        return new ParticleGlowSquid(world, x, y, z, motionX, motionY, motionZ, true, false);
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
        if (!ink && posY == prevPosY) {
            motionX *= 1.1D;
            motionZ *= 1.1D;
        }
        double friction = ink ? (double) 0.92F : 0.96D;
        motionX *= friction;
        motionY *= friction;
        motionZ *= friction;
        if (!ink && onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
        if (ink) {
            int frame = Math.min(INK_TEXTURES.length - 1,
                    particleAge * (INK_TEXTURES.length - 1) / Math.max(1, particleMaxAge));
            setTexture(INK_TEXTURES[frame]);
            if (particleAge > particleMaxAge / 2) {
                particleAlpha = 1.0F - (particleAge - particleMaxAge / 2) / (float) particleMaxAge;
            }
            if (world.isAirBlock(new net.minecraft.util.math.BlockPos(posX, posY, posZ))) {
                motionY -= (double) 0.0074F;
            }
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        if (ink && glowInk) {
            return 0xF000F0;
        }
        float progress = MathHelper.clamp(
                (particleAge + partialTick) / (float) Math.max(1, particleMaxAge), 0.0F, 1.0F);
        int brightness = super.getBrightnessForRender(partialTick);
        int block = brightness & 255;
        int sky = brightness >> 16 & 255;
        block = Math.min(240, block + (int) (progress * 240.0F));
        return block | sky << 16;
    }

    private void setTexture(ResourceLocation texture) {
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }
}
