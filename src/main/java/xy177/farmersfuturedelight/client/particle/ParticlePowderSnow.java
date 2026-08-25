package xy177.farmersfuturedelight.client.particle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class ParticlePowderSnow extends ParticleDigging {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FarmerFutureDelight.MODID, "block/powder_snow");

    private ParticlePowderSnow(World world, double x, double y, double z,
                               double motionX, double motionY, double motionZ,
                               IBlockState state, BlockPos pos) {
        super(world, x, y, z, motionX, motionY, motionZ, state);
        setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(TEXTURE.toString()));
        setBlockPos(pos);
    }

    public static void addDestroyEffects(World world, BlockPos pos, IBlockState state,
                                         ParticleManager manager) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    double offsetX = (x + 0.5D) / 4.0D;
                    double offsetY = (y + 0.5D) / 4.0D;
                    double offsetZ = (z + 0.5D) / 4.0D;
                    manager.addEffect(new ParticlePowderSnow(world,
                            pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                            offsetX - 0.5D, offsetY - 0.5D, offsetZ - 0.5D, state, pos));
                }
            }
        }
    }
}
