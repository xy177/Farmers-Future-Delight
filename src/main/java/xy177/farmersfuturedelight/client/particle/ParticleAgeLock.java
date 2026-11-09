package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public final class ParticleAgeLock extends Particle {
    public ParticleAgeLock(EntityLivingBase entity, boolean locked) {
        super(entity.world, entity.posX + (entity.getRNG().nextDouble() * 2.0D - 1.0D) * entity.width,
                entity.posY + entity.getRNG().nextDouble() * entity.height * 0.2D
                        + entity.height + (locked ? 0.2D : 0.0D),
                entity.posZ + (entity.getRNG().nextDouble() * 2.0D - 1.0D) * entity.width);
        motionX = 0.0D;
        motionY = locked ? -0.03D : 0.03D;
        motionZ = 0.0D;
        particleGravity = 0.0F;
        particleScale *= rand.nextFloat() * 0.6F + 0.5F;
        particleMaxAge = 8;
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
                new ResourceLocation("farmers_future_delight", "particle/age_lock_glint").toString()));
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}
