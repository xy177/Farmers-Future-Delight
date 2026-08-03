package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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

    private ParticleGlowSquid(World world, double x, double y, double z,
                              double motionX, double motionY, double motionZ, boolean ink) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.ink = ink;
        canCollide = false;
        particleGravity = 0.0F;
        if (ink) {
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            particleScale = 0.8F + rand.nextFloat() * 0.4F;
            particleMaxAge = 20 + rand.nextInt(12);
            setRBGColorF(0.6F, 1.0F, 0.8F);
            setTexture(INK_TEXTURES[0]);
        } else {
            this.motionX *= 0.1D;
            this.motionY *= 0.2D;
            this.motionZ *= 0.1D;
            particleScale *= 0.25F;
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
        return new ParticleGlowSquid(world, x, y, z, 0.0D, 0.0D, 0.0D, false);
    }

    public static ParticleGlowSquid ink(World world, double x, double y, double z,
                                        double motionX, double motionY, double motionZ) {
        return new ParticleGlowSquid(world, x, y, z, motionX, motionY, motionZ, true);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ink) {
            int frame = Math.min(INK_TEXTURES.length - 1,
                    particleAge * INK_TEXTURES.length / Math.max(1, particleMaxAge));
            setTexture(INK_TEXTURES[frame]);
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 0xF000F0;
    }

    private void setTexture(ResourceLocation texture) {
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString()));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, path);
    }
}
