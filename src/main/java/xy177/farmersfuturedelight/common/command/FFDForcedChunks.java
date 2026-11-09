package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import xy177.farmersfuturedelight.FarmerFutureDelight;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDForcedChunks implements ForgeChunkManager.LoadingCallback {
    private static final String CHUNKS_KEY = "ForcedChunks";
    private static final int MAX_CHUNKS = 256;
    private static final Map<World, List<ForgeChunkManager.Ticket>> TICKETS = new WeakHashMap<>();

    private FFDForcedChunks() {
    }

    public static void register() {
        ForgeChunkManager.setForcedChunkLoadingCallback(FarmerFutureDelight.instance,
                new FFDForcedChunks());
    }

    public static int add(WorldServer world, Set<Long> requested) throws CapacityException {
        List<ForgeChunkManager.Ticket> tickets = tickets(world);
        Set<Long> existing = getChunks(tickets);
        List<Long> additions = new ArrayList<>();
        for (Long chunk : requested) {
            if (!existing.contains(chunk)) {
                additions.add(chunk);
            }
        }
        if (additions.isEmpty()) {
            return 0;
        }
        if (existing.size() + additions.size() > MAX_CHUNKS) {
            throw new CapacityException("max_chunks");
        }

        List<ForgeChunkManager.Ticket> created = new ArrayList<>();
        int available = 0;
        for (ForgeChunkManager.Ticket ticket : tickets) {
            available += Math.max(0, ticket.getChunkListDepth() - ticket.getModData()
                    .getTagList(CHUNKS_KEY, 10).tagCount());
        }
        int remaining = Math.max(0, additions.size() - available);
        while (remaining > 0) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(
                    FarmerFutureDelight.instance, world, ForgeChunkManager.Type.NORMAL);
            if (ticket == null) {
                for (ForgeChunkManager.Ticket allocated : created) {
                    ForgeChunkManager.releaseTicket(allocated);
                    tickets.remove(allocated);
                }
                throw new CapacityException("tickets");
            }
            if (ticket.getChunkListDepth() <= 0) {
                ForgeChunkManager.releaseTicket(ticket);
                for (ForgeChunkManager.Ticket allocated : created) {
                    ForgeChunkManager.releaseTicket(allocated);
                    tickets.remove(allocated);
                }
                throw new CapacityException("tickets");
            }
            created.add(ticket);
            tickets.add(ticket);
            remaining -= ticket.getChunkListDepth();
        }

        int ticketIndex = 0;
        for (Long packedChunk : additions) {
            while (ticketIndex < tickets.size()
                    && tickets.get(ticketIndex).getModData().getTagList(CHUNKS_KEY, 10).tagCount()
                    >= tickets.get(ticketIndex).getChunkListDepth()) {
                ticketIndex++;
            }
            if (ticketIndex >= tickets.size()) {
                break;
            }
            ForgeChunkManager.Ticket ticket = tickets.get(ticketIndex);
            ChunkPos chunk = unpack(packedChunk);
            ForgeChunkManager.forceChunk(ticket, chunk);
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("X", chunk.x);
            entry.setInteger("Z", chunk.z);
            NBTTagList chunks = ticket.getModData().getTagList(CHUNKS_KEY, 10);
            chunks.appendTag(entry);
            ticket.getModData().setTag(CHUNKS_KEY, chunks);
        }
        return additions.size();
    }

    public static int remove(WorldServer world, Set<Long> requested) {
        List<ForgeChunkManager.Ticket> tickets = tickets(world);
        int removed = 0;
        for (ForgeChunkManager.Ticket ticket : new ArrayList<>(tickets)) {
            NBTTagList oldChunks = ticket.getModData().getTagList(CHUNKS_KEY, 10);
            NBTTagList retained = new NBTTagList();
            for (int index = 0; index < oldChunks.tagCount(); index++) {
                NBTTagCompound entry = oldChunks.getCompoundTagAt(index);
                long packed = pack(entry.getInteger("X"), entry.getInteger("Z"));
                if (requested.contains(packed)) {
                    ForgeChunkManager.unforceChunk(ticket,
                            new ChunkPos(entry.getInteger("X"), entry.getInteger("Z")));
                    removed++;
                } else {
                    retained.appendTag(entry.copy());
                }
            }
            if (retained.tagCount() == 0) {
                ForgeChunkManager.releaseTicket(ticket);
                tickets.remove(ticket);
            } else {
                ticket.getModData().setTag(CHUNKS_KEY, retained);
            }
        }
        return removed;
    }

    public static Set<Long> getChunks(WorldServer world) {
        return getChunks(tickets(world));
    }

    public static boolean isForced(WorldServer world, BlockPos pos) {
        long chunk = pack(pos.getX() >> 4, pos.getZ() >> 4);
        return getChunks(world).contains(chunk);
    }

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> loaded, World world) {
        List<ForgeChunkManager.Ticket> accepted = new ArrayList<>();
        int remaining = MAX_CHUNKS;
        for (ForgeChunkManager.Ticket ticket : loaded) {
            NBTTagList chunks = ticket.getModData().getTagList(CHUNKS_KEY, 10);
            if (chunks.tagCount() == 0 || chunks.tagCount() > remaining) {
                ForgeChunkManager.releaseTicket(ticket);
                continue;
            }
            for (int index = 0; index < chunks.tagCount(); index++) {
                NBTTagCompound entry = chunks.getCompoundTagAt(index);
                ForgeChunkManager.forceChunk(ticket,
                        new ChunkPos(entry.getInteger("X"), entry.getInteger("Z")));
            }
            remaining -= chunks.tagCount();
            accepted.add(ticket);
        }
        TICKETS.put(world, accepted);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        TICKETS.remove(event.getWorld());
    }

    private static List<ForgeChunkManager.Ticket> tickets(World world) {
        List<ForgeChunkManager.Ticket> tickets = TICKETS.get(world);
        if (tickets == null) {
            tickets = new ArrayList<>();
            TICKETS.put(world, tickets);
        }
        return tickets;
    }

    private static Set<Long> getChunks(List<ForgeChunkManager.Ticket> tickets) {
        Set<Long> chunks = new java.util.LinkedHashSet<>();
        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagList entries = ticket.getModData().getTagList(CHUNKS_KEY, 10);
            for (int index = 0; index < entries.tagCount(); index++) {
                NBTTagCompound entry = entries.getCompoundTagAt(index);
                chunks.add(pack(entry.getInteger("X"), entry.getInteger("Z")));
            }
        }
        return chunks;
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    private static ChunkPos unpack(long packed) {
        return new ChunkPos((int) (packed >> 32), (int) packed);
    }

    public static final class CapacityException extends Exception {
        private final String reason;

        private CapacityException(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}
