package xy177.farmersfuturedelight.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData;

import java.util.Map;

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
        CHANNEL.registerMessage(PacketRiptideState.Handler.class,
                PacketRiptideState.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(PacketFluidloggedChunk.Handler.class,
                PacketFluidloggedChunk.class, 3, Side.CLIENT);
        CHANNEL.registerMessage(PacketFluidloggedUpdate.Handler.class,
                PacketFluidloggedUpdate.class, 4, Side.CLIENT);
        CHANNEL.registerMessage(PacketStoredFluidChunk.Handler.class,
                PacketStoredFluidChunk.class, 5, Side.CLIENT);
        CHANNEL.registerMessage(PacketFluidMigration.ClientHandler.class,
                PacketFluidMigration.class, 6, Side.CLIENT);
        CHANNEL.registerMessage(PacketFluidMigration.ServerHandler.class,
                PacketFluidMigration.class, 7, Side.SERVER);
    }

    public static void sendVerticalBiomes(EntityPlayerMP player, int dimension,
                                          int chunkX, int chunkZ,
                                          FFDVerticalBiomeData data) {
        CHANNEL.sendTo(new PacketVerticalBiomes(dimension, chunkX, chunkZ, data), player);
    }

    public static void sendStoredFluidChunk(EntityPlayerMP player, int x, int z,
                                             net.minecraft.nbt.NBTTagCompound data) {
        net.minecraft.nbt.NBTTagList entries = data.getTagList("Entries", 10);
        for (int start = 0; start == 0 || start < entries.tagCount(); start += 128) {
            net.minecraft.nbt.NBTTagCompound batch = new net.minecraft.nbt.NBTTagCompound();
            net.minecraft.nbt.NBTTagList part = new net.minecraft.nbt.NBTTagList();
            for (int i = start; i < Math.min(start + 128, entries.tagCount()); i++)
                part.appendTag(entries.getCompoundTagAt(i));
            batch.setTag("Entries", part);
            if (start == 0) batch.setTag("Policy",
                    xy177.farmersfuturedelight.common.FFDConfig.fluidPolicy());
            CHANNEL.sendTo(new PacketStoredFluidChunk(player.dimension, x, z, batch, start == 0), player);
        }
    }

    public static void sendStoredFluidChunk(World world, int x, int z,
                                             net.minecraft.nbt.NBTTagCompound data) {
        if (!(world instanceof WorldServer)) return;
        WorldServer server = (WorldServer) world;
        for (EntityPlayerMP player : server.getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.world == world && server.getPlayerChunkMap().isPlayerWatchingChunk(player, x, z))
                sendStoredFluidChunk(player, x, z, data);
        }
    }

    public static void syncFluidPolicy(net.minecraft.server.MinecraftServer server) {
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            net.minecraft.nbt.NBTTagCompound data = new net.minecraft.nbt.NBTTagCompound();
            data.setTag("Policy", xy177.farmersfuturedelight.common.FFDConfig.fluidPolicy());
            CHANNEL.sendTo(new PacketStoredFluidChunk(player.dimension, 0, 0, data, false), player);
        }
    }

    public static void sendMigrationPrompt(EntityPlayerMP player, String token, String host,
                                           String fluid, boolean convertible) {
        CHANNEL.sendTo(new PacketFluidMigration(token, host, fluid, convertible), player);
    }

    public static void answerMigration(String token, boolean convert) {
        CHANNEL.sendToServer(new PacketFluidMigration(token, "", "", convert));
    }

    public static void sendDripstoneParticle(WorldServer world, double x, double y,
                                              double z, boolean lava) {
        CHANNEL.sendToAllAround(new PacketDripstoneParticle(x, y, z, lava),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0D));
    }

    public static void sendRiptideState(EntityPlayerMP player, int ticks) {
        PacketRiptideState packet = new PacketRiptideState(player.getEntityId(), ticks);
        CHANNEL.sendToAllTracking(packet, player);
        CHANNEL.sendTo(packet, player);
    }

    public static void sendFluidloggedChunk(EntityPlayerMP player, World world,
                                            int chunkX, int chunkZ,
                                            Map<Long, String> fluids) {
        CHANNEL.sendTo(new PacketFluidloggedChunk(world.provider.getDimension(), chunkX, chunkZ,
                fluids), player);
    }

    public static void sendFluidloggedUpdate(World world, BlockPos pos, String fluidName) {
        CHANNEL.sendToAllAround(new PacketFluidloggedUpdate(world.provider.getDimension(), pos,
                        fluidName),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5D,
                        pos.getY() + 0.5D, pos.getZ() + 0.5D, 256.0D));
    }
}
