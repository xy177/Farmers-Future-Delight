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
        CHANNEL.registerMessage(PacketDrownedConverting.Handler.class,
                PacketDrownedConverting.class, 8, Side.CLIENT);
        CHANNEL.registerMessage(PacketMooshroomState.Handler.class,
                PacketMooshroomState.class, 9, Side.CLIENT);
        CHANNEL.registerMessage(PacketSignText.Handler.class,
                PacketSignText.class, 10, Side.SERVER);
        CHANNEL.registerMessage(PacketMerchantState.Handler.class,
                PacketMerchantState.class, 11, Side.CLIENT);
        CHANNEL.registerMessage(PacketMerchantSelect.Handler.class,
                PacketMerchantSelect.class, 12, Side.SERVER);
        CHANNEL.registerMessage(PacketVillagerState.Handler.class,
                PacketVillagerState.class, 13, Side.CLIENT);
        CHANNEL.registerMessage(PacketJigsawEdit.Handler.class,
                PacketJigsawEdit.class, 14, Side.SERVER);
        CHANNEL.registerMessage(PacketMapDecorations.Handler.class,
                PacketMapDecorations.class, 15, Side.CLIENT);
        CHANNEL.registerMessage(PacketImmediateRespawnRule.Handler.class,
                PacketImmediateRespawnRule.class, 16, Side.CLIENT);
        CHANNEL.registerMessage(PacketCookingBook.ClientHandler.class,
                PacketCookingBook.class, 17, Side.CLIENT);
        CHANNEL.registerMessage(PacketCookingBook.ServerHandler.class,
                PacketCookingBook.class, 18, Side.SERVER);
        CHANNEL.registerMessage(PacketAgeLockState.Handler.class,
                PacketAgeLockState.class, 19, Side.CLIENT);
    }

    public static void sendAgeLockState(net.minecraft.entity.EntityLivingBase entity) {
        CHANNEL.sendToAllTracking(new PacketAgeLockState(entity), entity);
    }

    public static void sendAgeLockState(EntityPlayerMP player, net.minecraft.entity.EntityLivingBase entity) {
        CHANNEL.sendTo(new PacketAgeLockState(entity), player);
    }

    public static void sendCookingBook(EntityPlayerMP player, net.minecraft.nbt.NBTTagCompound data) {
        CHANNEL.sendTo(new PacketCookingBook(data), player);
    }

    public static void updateCookingBook(net.minecraft.nbt.NBTTagCompound data) {
        CHANNEL.sendToServer(new PacketCookingBook(data));
    }

    public static void sendMapDecorations(EntityPlayerMP player, String mapName,
                                          net.minecraft.nbt.NBTTagCompound decorations) {
        CHANNEL.sendTo(new PacketMapDecorations(mapName, decorations), player);
    }

    public static void sendImmediateRespawnRule(EntityPlayerMP player, boolean enabled) {
        CHANNEL.sendTo(new PacketImmediateRespawnRule(enabled), player);
    }

    public static void editJigsaw(PacketJigsawEdit packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendVillagerState(net.minecraft.entity.passive.EntityVillager villager) {
        CHANNEL.sendToAllTracking(new PacketVillagerState(villager), villager);
    }

    public static void sendVillagerState(EntityPlayerMP player,
                                         net.minecraft.entity.passive.EntityVillager villager) {
        CHANNEL.sendTo(new PacketVillagerState(villager), player);
    }

    public static void sendMerchantState(EntityPlayerMP player, PacketMerchantState packet) {
        CHANNEL.sendTo(packet, player);
    }

    public static void sendVillagerState(net.minecraft.entity.monster.EntityZombieVillager zombie) {
        CHANNEL.sendToAllTracking(new PacketVillagerState(zombie), zombie);
    }

    public static void sendVillagerState(EntityPlayerMP player,
                                         net.minecraft.entity.monster.EntityZombieVillager zombie) {
        CHANNEL.sendTo(new PacketVillagerState(zombie), player);
    }

    public static void selectMerchantTrade(int windowId, int selection) {
        CHANNEL.sendToServer(new PacketMerchantSelect(windowId, selection));
    }

    public static void sendSignText(PacketSignText packet) {
        CHANNEL.sendToServer(packet);
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

    public static void sendDrownedConverting(net.minecraft.entity.EntityLivingBase entity,
                                             boolean converting) {
        PacketDrownedConverting packet = new PacketDrownedConverting(entity, converting);
        CHANNEL.sendToAllTracking(packet, entity);
        if (entity instanceof EntityPlayerMP) {
            CHANNEL.sendTo(packet, (EntityPlayerMP) entity);
        }
    }

    public static void sendDrownedConverting(EntityPlayerMP player,
                                             net.minecraft.entity.EntityLivingBase entity,
                                             boolean converting) {
        if (converting) {
            CHANNEL.sendTo(new PacketDrownedConverting(entity, true), player);
        }
    }

    public static void sendMooshroomState(
            net.minecraft.entity.passive.EntityMooshroom entity, boolean brown) {
        CHANNEL.sendToAllTracking(new PacketMooshroomState(entity, brown), entity);
    }

    public static void sendMooshroomState(EntityPlayerMP player,
            net.minecraft.entity.passive.EntityMooshroom entity, boolean brown) {
        CHANNEL.sendTo(new PacketMooshroomState(entity, brown), player);
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
