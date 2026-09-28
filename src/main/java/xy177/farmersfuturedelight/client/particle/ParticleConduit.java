package xy177.farmersfuturedelight.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEnchantmentTable;
import net.minecraft.world.World;

public final class ParticleConduit extends ParticleEnchantmentTable {
    private ParticleConduit(World world, double x, double y, double z,
                            double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        setParticleTextureIndex(208);
    }

    public static ParticleConduit create(World world, double x, double y, double z,
                                         double motionX, double motionY, double motionZ) {
        return new ParticleConduit(world, x, y, z, motionX, motionY, motionZ);
    }
}
