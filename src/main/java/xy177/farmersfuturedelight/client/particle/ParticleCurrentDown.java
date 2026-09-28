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

public final class ParticleCurrentDown extends Particle {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            FarmerFutureDelight.MODID, "particle/bubble");
    private float angle;

    private ParticleCurrentDown(World world, double x, double y, double z) {
        super(world, x, y, z);
        particleMaxAge = (int) (rand.nextFloat() * 60.0F) + 30;
        canCollide = false;
        motionX = 0.0D;
        motionY = -0.05D;
        motionZ = 0.0D;
        setSize(0.02F, 0.02F);
        particleScale *= rand.nextFloat() * 0.6F + 0.2F;
        particleGravity = 0.002F;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
    }

    public static ParticleCurrentDown create(World world, double x, double y, double z) {
        return new ParticleCurrentDown(world, x, y, z);
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
        motionX += 0.6D * Math.cos(angle);
        motionZ += 0.6D * Math.sin(angle);
        motionX *= 0.07D;
        motionZ *= 0.07D;
        move(motionX, motionY, motionZ);
        if (!isInWater() || onGround) {
            setExpired();
        }
        angle += 0.08F;
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
