package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public final class ParticleSoul extends Particle {
    public ParticleSoul(EntityLivingBase entity) {
        super(entity.world,entity.posX+(entity.getRNG().nextDouble()-.5D)*entity.width,entity.posY+.1D,
                entity.posZ+(entity.getRNG().nextDouble()-.5D)*entity.width,-entity.motionX*.2D,.1D,-entity.motionZ*.2D);
        motionX=-entity.motionX*.2D;motionY=.1D;motionZ=-entity.motionZ*.2D;
        particleScale*=1.5F;particleMaxAge=20+rand.nextInt(13);setFrame();
    }
    private void setFrame() {
        int frame=Math.min(10,particleAge*11/particleMaxAge);
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("farmers_future_delight:particle/soul_"+frame));
    }
    @Override public int getFXLayer() {return 1;}
    @Override public int getBrightnessForRender(float partialTicks) {return 240;}
    @Override public void onUpdate() {
        prevPosX=posX;prevPosY=posY;prevPosZ=posZ;
        if(particleAge++>=particleMaxAge)setExpired();
        move(motionX,motionY,motionZ);motionX*=.96D;motionY*=.96D;motionZ*=.96D;setFrame();
    }
}
