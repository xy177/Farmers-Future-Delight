package xy177.farmersfuturedelight.client.particle;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public final class ParticleBubbleColumnUp extends Particle {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "particle/bubble");

    private ParticleBubbleColumnUp(World world, double x, double y, double z,
                                   double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        particleGravity = -0.125F;
        setSize(0.02F, 0.02F);
        particleScale *= rand.nextFloat() * 0.6F + 0.2F;
        this.motionX = motionX * 0.2D + (rand.nextFloat() * 2.0F - 1.0F) * 0.02F;
        this.motionY = motionY * 0.2D + (rand.nextFloat() * 2.0F - 1.0F) * 0.02F;
        this.motionZ = motionZ * 0.2D + (rand.nextFloat() * 2.0F - 1.0F) * 0.02F;
        particleMaxAge = (int) (40.0D / (rand.nextFloat() * 0.8D + 0.2D));
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleBubbleColumnUp create(World world, double x, double y, double z,
                                                 double motionX, double motionY,
                                                 double motionZ) {
        return new ParticleBubbleColumnUp(world, x, y, z, motionX, motionY, motionZ);
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
        motionY -= 0.04D * particleGravity;
        move(motionX, motionY, motionZ);
        motionX *= 0.85D;
        motionY *= 0.85D;
        motionZ *= 0.85D;
        if (onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
        if (!isInWater()) {
            setExpired();
        }
    }

    private boolean isInWater() {
        IBlockState state = world.getBlockState(new BlockPos(posX, posY, posZ));
        return state.getMaterial() == Material.WATER || WaterloggedBlockApi.containsWater(state);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
