package xy177.farmersfuturedelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.client.particle.ParticleComposter;
import xy177.farmersfuturedelight.common.block.BlockComposter;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public final class FFDComposterClientHooks {
    private FFDComposterClientHooks() {
    }

    public static boolean handleWorldEvent(EntityPlayer player, int eventType,
                                           BlockPos pos, int data) {
        if (eventType != 1500) {
            return false;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        if (world == null || pos == null
                || !(world.getBlockState(pos).getBlock() instanceof BlockComposter)) {
            return false;
        }
        boolean success = data != 0;
        world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                success ? FFDSounds.COMPOSTER_FILL_SUCCESS : FFDSounds.COMPOSTER_FILL,
                SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        int level = world.getBlockState(pos).getValue(BlockComposter.LEVEL);
        double center = Math.max(2, Math.min(15, 1 + level * 2)) / 16.0D + 0.03125D;
        for (int index = 0; index < 10; index++) {
            minecraft.effectRenderer.addEffect(ParticleComposter.create(world,
                    pos.getX() + 0.13125D + 0.7375D * world.rand.nextFloat(),
                    pos.getY() + center + world.rand.nextFloat() * (1.0D - center),
                    pos.getZ() + 0.13125D + 0.7375D * world.rand.nextFloat(),
                    world.rand.nextGaussian() * 0.02D,
                    world.rand.nextGaussian() * 0.02D,
                    world.rand.nextGaussian() * 0.02D));
        }
        return true;
    }
}
