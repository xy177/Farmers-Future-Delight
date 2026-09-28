package xy177.farmersfuturedelight.common.fluid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDFluidMigration {
    public static final String CAPABILITY = "fluidlogged_api:fluid_states";
    public static final String PENDING_TAG = "FFDFluidMigrationPending";
    private static final Logger LOG = LogManager.getLogger("FFD Fluid Migration");
    private static final Map<Chunk, NBTTagCompound> PENDING = new WeakHashMap<>();
    private static final Map<World, Map<Long, Chunk>> QUEUED = new WeakHashMap<>();
    private static final Map<String, Offer> OFFERS = new LinkedHashMap<>();
    private static final Map<UUID, String> NOTIFIED = new HashMap<>();
    private static final int MAX_PER_TICK = 16;

    private FFDFluidMigration() {}

    public static synchronized void load(ChunkDataEvent.Load event) {
        if (event.getWorld().isRemote || Loader.isModLoaded("fluidlogged_api")) return;
        NBTTagCompound root = event.getData();
        NBTTagCompound pending = root.hasKey(PENDING_TAG, Constants.NBT.TAG_COMPOUND)
                ? root.getCompoundTag(PENDING_TAG).copy() : null;
        if (pending == null) {
            NBTTagCompound level = root.hasKey("Level", Constants.NBT.TAG_COMPOUND)
                    ? root.getCompoundTag("Level") : root;
            NBTBase old = level.getCompoundTag("ForgeCaps").getTag(CAPABILITY);
            if (old == null) return;
            pending = decode(event.getChunk(), old);
        }
        if (!pending.hasNoTags()) PENDING.put(event.getChunk(), pending);
    }

    public static NBTTagCompound decode(Chunk chunk, NBTBase old) {
        NBTTagCompound pending = new NBTTagCompound();
        NBTTagList entries = new NBTTagList();
        NBTTagList list = null;
        if (old instanceof NBTTagList) {
            list = (NBTTagList) old;
        } else if (old instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) old;
            if ((compound.getInteger("version") == 1 || compound.getInteger("version") == 2)
                    && compound.hasKey("data", Constants.NBT.TAG_LIST)) {
                list = (NBTTagList) compound.getTag("data");
            }
        }
        if (list == null || list.tagCount() > 0 && list.getTagType() != Constants.NBT.TAG_COMPOUND) {
            pending.setTag("Unparsed", old.copy());
            return pending;
        }
        NBTTagList malformed = new NBTTagList();
        for (NBTBase raw : list) {
            if (!(raw instanceof NBTTagCompound)) {
                malformed.appendTag(raw.copy());
                continue;
            }
            NBTTagCompound tag = (NBTTagCompound) raw;
            if (!tag.hasKey("id", Constants.NBT.TAG_STRING)
                    || !(tag.hasKey("pos", Constants.NBT.TAG_INT)
                            || tag.hasKey("pos", Constants.NBT.TAG_LONG))
                    || tag.hasKey("meta") && !tag.hasKey("meta", Constants.NBT.TAG_ANY_NUMERIC)) {
                malformed.appendTag(tag.copy());
                continue;
            }
            NBTPrimitive number = (NBTPrimitive) tag.getTag("pos");
            int packed = number.getInt();
            BlockPos pos = number instanceof NBTTagLong ? BlockPos.fromLong(number.getLong())
                    : new BlockPos((chunk.x << 4) | (packed & 15),
                            (packed & 65535) >>> 8, (chunk.z << 4) | ((packed >> 4) & 15));
            if ((pos.getX() >> 4) != chunk.x || (pos.getZ() >> 4) != chunk.z
                    || FFDHeightHooks.isOutsideBuildHeight(chunk.getWorld(), pos)
                    || (!(number instanceof NBTTagLong) && (packed < 0 || packed > 65535))) {
                malformed.appendTag(tag.copy());
                continue;
            }
            NBTTagCompound entry = tag.copy();
            entry.setLong("Pos", pos.toLong());
            entry.setString("Host", String.valueOf(chunk.getBlockState(pos).getBlock().getRegistryName()));
            entries.appendTag(entry);
        }
        if (entries.tagCount() > 0) pending.setTag("Entries", entries);
        if (malformed.tagCount() > 0) pending.setTag("Unparsed", malformed);
        return pending;
    }

    public static synchronized void save(ChunkDataEvent.Save event) {
        if (Loader.isModLoaded("fluidlogged_api")) return;
        NBTTagCompound pending = PENDING.get(event.getChunk());
        if (pending != null && !pending.hasNoTags()) {
            event.getData().setTag(PENDING_TAG, pending.copy());
        } else {
            event.getData().removeTag(PENDING_TAG);
        }
        NBTTagCompound level = event.getData().hasKey("Level", Constants.NBT.TAG_COMPOUND)
                ? event.getData().getCompoundTag("Level") : event.getData();
        level.getCompoundTag("ForgeCaps").removeTag(CAPABILITY);
    }

    public static synchronized void queue(Chunk chunk) {
        if (chunk.getWorld().isRemote || !PENDING.containsKey(chunk)) return;
        QUEUED.computeIfAbsent(chunk.getWorld(), ignored -> new LinkedHashMap<>())
                .put(chunk.x & 0xffffffffL | (long) chunk.z << 32, chunk);
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || Loader.isModLoaded("fluidlogged_api")) return;
        ArrayList<Chunk> work = new ArrayList<>();
        synchronized (FFDFluidMigration.class) {
            Map<Long, Chunk> queued = QUEUED.get(event.world);
            if (queued != null) {
                java.util.Iterator<Chunk> iterator = queued.values().iterator();
                while (iterator.hasNext() && work.size() < MAX_PER_TICK) {
                    work.add(iterator.next());
                    iterator.remove();
                }
            }
        }
        for (Chunk chunk : work) if (chunk.isLoaded()) process(chunk);
        if (event.world.getTotalWorldTime() % 40 == 0) notifyPlayers((WorldServer) event.world);
    }

    public static synchronized void process(Chunk chunk) {
        if (Loader.isModLoaded("fluidlogged_api") || chunk.getWorld().isRemote) return;
        NBTTagCompound pending = PENDING.get(chunk);
        if (pending == null) return;
        NBTTagList list = pending.getTagList("Entries", Constants.NBT.TAG_COMPOUND);
        NBTTagList retained = new NBTTagList();
        Decisions decisions = decisions(chunk.getWorld());
        for (NBTBase raw : list) {
            NBTTagCompound entry = (NBTTagCompound) raw;
            BlockPos pos = BlockPos.fromLong(entry.getLong("Pos"));
            IBlockState host = chunk.getBlockState(pos);
            String pair = entry.getString("Host") + "|" + entry.getString("id");
            if (decisions.discarded.hasKey(pair)) continue;
            IBlockState fluidState = resolveFluid(entry);
            Fluid fluid = fluidState == null ? null
                    : WaterloggedBlockApi.getFluidForBlock(fluidState.getBlock());
            boolean hostMatches = hostMatches(chunk, entry);
            boolean hostAllowed = WaterloggedBlockApi.canBeWaterlogged(host)
                    || FFDConfig.isAdditionalWaterloggingBlock(host.getBlock());
            if (fluid != null && hostMatches && hostAllowed && FFDConfig.isFluidAllowed(fluid)) {
                migrate(chunk, pos, host, fluidState);
            } else {
                retained.appendTag(entry);
                boolean exists = false;
                for (Offer offer : OFFERS.values()) {
                    if (offer.world == chunk.getWorld() && offer.pair.equals(pair)) exists = true;
                }
                if (!exists) {
                    Offer offer = new Offer(chunk.getWorld(), pair, entry, fluid != null && hostMatches);
                    OFFERS.put(offer.token, offer);
                    LOG.warn("Fluidlogged API migration needs a decision: {}. {}. "
                            + "/ffd_fluidmigration convert {} OR /ffd_fluidmigration discard {}",
                            pair, offer.convertible ? "Conversion changes only required configuration"
                                    : "Missing fluid/host: restore the providing mod or explicitly discard",
                            offer.token, offer.token);
                }
            }
        }
        pending.removeTag("Entries");
        if (retained.tagCount() > 0) pending.setTag("Entries", retained);
        if (pending.hasKey("Unparsed")) {
            LOG.warn("Unrecognized Fluidlogged API data at {},{} preserved in {}. No data discarded.",
                    chunk.x, chunk.z, PENDING_TAG);
        }
        if (pending.hasNoTags()) PENDING.remove(chunk);
        pruneOffers();
        chunk.markDirty();
    }

    private static void pruneOffers() {
        OFFERS.values().removeIf(offer -> {
            for (Map.Entry<Chunk, NBTTagCompound> item : PENDING.entrySet()) {
                if (item.getKey().getWorld() != offer.world) continue;
                for (NBTBase raw : item.getValue().getTagList("Entries", 10)) {
                    NBTTagCompound entry = (NBTTagCompound) raw;
                    if (offer.pair.equals(entry.getString("Host") + "|" + entry.getString("id")))
                        return false;
                }
            }
            return true;
        });
        for (Offer offer : OFFERS.values()) refreshOffer(offer);
        NOTIFIED.values().removeIf(token -> !OFFERS.containsKey(token));
    }

    private static boolean hostMatches(Chunk chunk, NBTTagCompound entry) {
        IBlockState host = chunk.getBlockState(BlockPos.fromLong(entry.getLong("Pos")));
        return host.getBlock() != net.minecraft.init.Blocks.AIR
                && WaterloggedBlockApi.getFluidForBlock(host.getBlock()) == null
                && !entry.getBoolean("HostChanged") && entry.getString("Host").equals(
                        String.valueOf(host.getBlock().getRegistryName()));
    }

    private static void refreshOffer(Offer offer) {
        offer.convertible = false;
        for (Map.Entry<Chunk, NBTTagCompound> item : PENDING.entrySet()) {
            Chunk chunk = item.getKey();
            if (chunk.getWorld() != offer.world || !chunk.isLoaded()) continue;
            for (NBTBase raw : item.getValue().getTagList("Entries", 10)) {
                NBTTagCompound entry = (NBTTagCompound) raw;
                if (offer.pair.equals(entry.getString("Host") + "|" + entry.getString("id"))
                        && hostMatches(chunk, entry) && resolveFluid(entry) != null) {
                    offer.entry = entry.copy();
                    offer.convertible = true;
                    return;
                }
            }
        }
    }

    public static synchronized void hostChanged(Chunk chunk, BlockPos pos) {
        NBTTagCompound pending = PENDING.get(chunk);
        if (pending == null) return;
        for (NBTBase raw : pending.getTagList("Entries", 10)) {
            NBTTagCompound entry = (NBTTagCompound) raw;
            if (entry.getLong("Pos") == pos.toLong()) {
                entry.setBoolean("HostChanged", true);
                chunk.markDirty();
            }
        }
    }

    private static void migrate(Chunk chunk, BlockPos pos, IBlockState host, IBlockState fluid) {
        IBlockState wet = WaterloggedBlockApi.withWaterlogged(host, true);
        if (wet != null && wet != host) {
            int index = FFDHeightHooks.storageIndexForSectionY(pos.getY() >> 4, chunk.getWorld());
            ExtendedBlockStorage storage = chunk.getBlockStorageArray()[index];
            if (storage != null) storage.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, wet);
        }
        FFDStoredFluidStates.put(chunk, pos, fluid);
        FFDFluidloggedData.set(chunk.getWorld(), pos, null);
        if (chunk.isLoaded()) {
            World world = chunk.getWorld();
            world.notifyBlockUpdate(pos, host, chunk.getBlockState(pos), 2);
            FFDStoredFluidStates.sync(world, pos);
            world.scheduleUpdate(pos, host.getBlock(),
                    WaterloggedBlockApi.fluidTickRate(world, WaterloggedBlockApi.getFluidForBlock(fluid.getBlock())));
        }
    }

    public static IBlockState resolveFluid(NBTTagCompound entry) {
        try {
            ResourceLocation id = new ResourceLocation(entry.getString("id"));
            if (!ForgeRegistries.BLOCKS.containsKey(id)) return null;
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            int meta = entry.getInteger("meta");
            if (meta < 0 || meta > 15 || WaterloggedBlockApi.getFluidForBlock(block) == null) return null;
            return block.getStateFromMeta(meta);
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    public static boolean canDecide(MinecraftServer server, ICommandSender sender) {
        if (!(sender instanceof EntityPlayerMP)) return sender.canUseCommand(2, "ffd_fluidmigration");
        EntityPlayerMP player = (EntityPlayerMP) sender;
        return server.isSinglePlayer() && player.getName().equals(server.getServerOwner())
                || player.canUseCommand(2, "ffd_fluidmigration");
    }

    public static synchronized String decide(MinecraftServer server, ICommandSender sender,
                                               String token, boolean convert) {
        if (!canDecide(server, sender)) return "ffd.migration.denied";
        pruneOffers();
        Offer offer = OFFERS.get(token);
        if (offer == null || offer.world.getMinecraftServer() != server) return "ffd.migration.expired";
        IBlockState fluid = resolveFluid(offer.entry);
        if (convert) {
            if (!offer.convertible || fluid == null) return "ffd.migration.missing";
            Block host = Block.getBlockFromName(offer.entry.getString("Host"));
            try {
                FFDConfig.allowMigratedFluid(host, WaterloggedBlockApi.getFluidForBlock(fluid.getBlock()));
            } catch (IOException | RuntimeException failure) {
                LOG.error("Migration configuration was not saved; retained all pending data", failure);
                return "ffd.migration.save_failed";
            }
            FFDNetwork.syncFluidPolicy(server);
        } else {
            Decisions decisions = decisions(offer.world);
            decisions.discarded.setBoolean(offer.pair, true);
            decisions.markDirty();
        }
        OFFERS.remove(token);
        NOTIFIED.values().removeIf(value -> value.equals(token));
        for (Chunk chunk : new ArrayList<>(PENDING.keySet())) {
            if (chunk.getWorld().getMinecraftServer() == server && chunk.isLoaded()) queue(chunk);
        }
        return convert ? "ffd.migration.converted" : "ffd.migration.discarded";
    }

    private static synchronized void notifyPlayers(WorldServer world) {
        pruneOffers();
        for (Offer offer : OFFERS.values()) {
            if (offer.world != world) continue;
            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
                if (canDecide(world.getMinecraftServer(), player)
                        && !NOTIFIED.containsKey(player.getUniqueID())) {
                    NOTIFIED.put(player.getUniqueID(), offer.token);
                    FFDNetwork.sendMigrationPrompt(player, offer.token, offer.entry.getString("Host"),
                            offer.entry.getString("id"), offer.convertible);
                }
            }
            break;
        }
    }

    public static synchronized void status(MinecraftServer server, ICommandSender sender) {
        if (!canDecide(server, sender)) return;
        pruneOffers();
        Offer first = null;
        for (Offer offer : OFFERS.values()) {
            if (offer.world.getMinecraftServer() != server) continue;
            if (first == null) first = offer;
            sender.sendMessage(new net.minecraft.util.text.TextComponentString(
                    offer.pair + " : /ffd_fluidmigration convert " + offer.token
                            + " | /ffd_fluidmigration discard " + offer.token));
        }
        if (first == null) sender.sendMessage(new TextComponentTranslation("ffd.migration.none"));
        else if (sender instanceof EntityPlayerMP) {
            NOTIFIED.remove(((EntityPlayerMP) sender).getUniqueID());
            Offer offer = first;
            NOTIFIED.put(((EntityPlayerMP) sender).getUniqueID(), offer.token);
            FFDNetwork.sendMigrationPrompt((EntityPlayerMP) sender, offer.token,
                    offer.entry.getString("Host"), offer.entry.getString("id"), offer.convertible);
        }
        int unparsed = 0;
        for (NBTTagCompound pending : PENDING.values()) if (pending.hasKey("Unparsed")) unparsed++;
        if (unparsed > 0) sender.sendMessage(new TextComponentTranslation("ffd.migration.unparsed", unparsed));
    }

    @SubscribeEvent
    public static synchronized void logout(
            net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        NOTIFIED.remove(event.player.getUniqueID());
    }

    public static synchronized void unload(World world) {
        QUEUED.remove(world);
        PENDING.keySet().removeIf(chunk -> chunk.getWorld() == world);
        OFFERS.values().removeIf(offer -> offer.world == world);
        NOTIFIED.clear();
    }

    private static Decisions decisions(World world) {
        Decisions result = (Decisions) world.getMapStorage().getOrLoadData(Decisions.class, "ffd_fluid_migration");
        if (result == null) {
            result = new Decisions("ffd_fluid_migration");
            world.getMapStorage().setData("ffd_fluid_migration", result);
        }
        return result;
    }

    public static final class Decisions extends WorldSavedData {
        private NBTTagCompound discarded = new NBTTagCompound();
        public Decisions(String name) { super(name); }
        @Override public void readFromNBT(NBTTagCompound nbt) {
            discarded = nbt.getCompoundTag("Discarded").copy();
        }
        @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            nbt.setTag("Discarded", discarded.copy());
            return nbt;
        }
    }

    private static final class Offer {
        final String token = UUID.randomUUID().toString();
        final World world;
        final String pair;
        NBTTagCompound entry;
        boolean convertible;
        Offer(World world, String pair, NBTTagCompound entry, boolean convertible) {
            this.world = world;
            this.pair = pair;
            this.entry = entry.copy();
            this.convertible = convertible;
        }
    }
}
