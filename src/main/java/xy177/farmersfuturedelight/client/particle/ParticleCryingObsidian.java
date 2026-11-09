package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticleCryingObsidian extends Particle {
    private static final int HANGING = 0;
    private static final int FALLING = 1;
    private static final int LANDED = 2;
    private final int stage;

    private ParticleCryingObsidian(World world, double x, double y, double z, int stage) {
        super(world, x, y, z);
        this.stage = stage;
        setSize(0.01F, 0.01F);
        particleGravity = stage == HANGING ? 0.000012F : stage == FALLING ? 0.01F : 0.06F;
        particleMaxAge = stage == HANGING ? 100
                : (int) ((stage == FALLING ? 64.0D : 28.0D) / (rand.nextFloat() * 0.8D + 0.2D));
        particleScale = (rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        setRBGColorF(0.51171875F, 0.03125F, 0.890625F);
        ResourceLocation texture = new ResourceLocation(FarmerFutureDelight.MODID,
                "particle/" + (stage == HANGING ? "drip_hang" : stage == FALLING ? "drip_fall" : "drip_land"));
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(texture.toString()));
    }

    public static ParticleCryingObsidian create(World world, double x, double y, double z) {
        return new ParticleCryingObsidian(world, x, y, z, HANGING);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            if (stage == HANGING) {
                spawnNextStage(FALLING);
            }
            return;
        }
        motionY -= particleGravity;
        move(motionX, motionY, motionZ);
        if (stage == FALLING && onGround) {
            setExpired();
            spawnNextStage(LANDED);
            return;
        }
        if (stage == HANGING) {
            motionX *= 0.02D;
            motionY *= 0.02D;
            motionZ *= 0.02D;
        }
        motionX *= 0.98D;
        motionY *= 0.98D;
        motionZ *= 0.98D;
    }

    private void spawnNextStage(int nextStage) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new ParticleCryingObsidian(world, posX, posY, posZ, nextStage));
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return (super.getBrightnessForRender(partialTicks) & 0xFFFF0000) | 240;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
