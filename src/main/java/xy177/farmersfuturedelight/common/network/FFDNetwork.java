package xy177.farmersfuturedelight.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;

public final class FFDNetwork {
    private static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel("ffd_biomes");

    private FFDNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(PacketVerticalBiomes.Handler.class,
                PacketVerticalBiomes.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(PacketDripstoneParticle.Handler.class,
                PacketDripstoneParticle.class, 1, Side.CLIENT);
    }

    public static void sendVerticalBiomes(EntityPlayerMP player, int dimension,
                                          int chunkX, int chunkZ,
                                          FFDVerticalBiomeData data) {
        CHANNEL.sendTo(new PacketVerticalBiomes(dimension, chunkX, chunkZ, data), player);
    }

    public static void sendDripstoneParticle(WorldServer world, double x, double y,
                                              double z, boolean lava) {
        CHANNEL.sendToAllAround(new PacketDripstoneParticle(x, y, z, lava),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0D));
    }
}
