package xy177.farmersfuturedelight.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.network.FFDNetwork;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRedstoneWire {
    private static final String KEY = "FFDRedstoneDots";
    private static final Map<World, Map<Long, Set<Long>>> DOTS = new WeakHashMap<>();

    private FFDRedstoneWire() {
    }

    private static IProperty property(EnumFacing facing) {
        switch (facing) {
            case NORTH: return BlockRedstoneWire.NORTH;
            case SOUTH: return BlockRedstoneWire.SOUTH;
            case WEST: return BlockRedstoneWire.WEST;
            default: return BlockRedstoneWire.EAST;
        }
    }

    private static Set<Long> dots(World world, int chunkX, int chunkZ) {
        Map<Long, Set<Long>> chunks = DOTS.get(world);
        return chunks == null ? null : chunks.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static boolean isDot(World world, BlockPos pos) {
        Set<Long> positions = dots(world, pos.getX() >> 4, pos.getZ() >> 4);
        return positions != null && positions.contains(pos.toLong());
    }

    private static boolean connected(IBlockState state, EnumFacing facing) {
        IProperty property = property(facing);
        Object value = state.getValue(property);
        return !"none".equals(property.getName((Comparable) value));
    }

    private static boolean isolated(IBlockState state) {
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (connected(state, facing)) return false;
        }
        return true;
    }

    private static Comparable side(IProperty property) {
        for (Object value : property.getAllowedValues()) {
            Comparable comparable = (Comparable) value;
            if ("side".equals(property.getName(comparable))) return comparable;
        }
        return null;
    }

    private static IBlockState raw(IBlockState state, IBlockAccess world, BlockPos pos) {
        return ((xy177.farmersfuturedelight.core.FFDRedstoneAccess) Blocks.REDSTONE_WIRE)
                .ffd$getWireState(state, world, pos);
    }

    public static IBlockState actual(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean empty = isolated(state);
        boolean dot = world instanceof World ? isDot((World) world, pos)
                : FarmerFutureDelight.proxy.isRedstoneDot(pos);
        if (empty && dot) return state;
        boolean northSouth = connected(state, EnumFacing.NORTH)
                || connected(state, EnumFacing.SOUTH);
        boolean eastWest = connected(state, EnumFacing.EAST)
                || connected(state, EnumFacing.WEST);
        if (!eastWest) {
            if (!connected(state, EnumFacing.NORTH))
                state = state.withProperty((IProperty) BlockRedstoneWire.NORTH, side(BlockRedstoneWire.NORTH));
            if (!connected(state, EnumFacing.SOUTH))
                state = state.withProperty((IProperty) BlockRedstoneWire.SOUTH, side(BlockRedstoneWire.SOUTH));
        }
        if (!northSouth) {
            if (!connected(state, EnumFacing.EAST))
                state = state.withProperty((IProperty) BlockRedstoneWire.EAST, side(BlockRedstoneWire.EAST));
            if (!connected(state, EnumFacing.WEST))
                state = state.withProperty((IProperty) BlockRedstoneWire.WEST, side(BlockRedstoneWire.WEST));
        }
        return state;
    }

    public static int power(BlockRedstoneWire wire, IBlockState state, IBlockAccess world,
                            BlockPos pos, EnumFacing side) {
        if (!wire.canProvidePower(state) || side == EnumFacing.DOWN) return 0;
        int power = state.getValue(BlockRedstoneWire.POWER);
        if (power == 0 || side == EnumFacing.UP) return power;
        IBlockState connectionState = actual(raw(state, world, pos), world, pos);
        return connected(connectionState, side.getOpposite()) ? power : 0;
    }

    private static void setDot(World world, BlockPos pos, boolean dot) {
        Map<Long, Set<Long>> chunks = DOTS.computeIfAbsent(world, ignored -> new HashMap<>());
        long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Set<Long> positions = chunks.computeIfAbsent(chunk, ignored -> new HashSet<>());
        boolean changed = dot ? positions.add(pos.toLong()) : positions.remove(pos.toLong());
        if (positions.isEmpty()) chunks.remove(chunk);
        if (!changed) return;
        world.markChunkDirty(pos, null);
        if (world instanceof WorldServer) {
            for (EntityPlayerMP player : ((WorldServer) world).getMinecraftServer().getPlayerList().getPlayers()) {
                if (player.world == world && ((WorldServer) world).getPlayerChunkMap()
                        .isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
                    FFDNetwork.sendRedstoneDots(player, pos.getX() >> 4, pos.getZ() >> 4,
                            snapshot(world, pos.getX() >> 4, pos.getZ() >> 4));
                }
            }
        }
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public static void changed(World world, BlockPos pos) {
        if (!world.isRemote && isDot(world, pos)) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != Blocks.REDSTONE_WIRE || !isolated(raw(state, world, pos))) {
                setDot(world, pos, false);
            }
        }
    }

    public static void removed(World world, BlockPos pos) {
        if (!world.isRemote) setDot(world, pos, false);
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (event.getEntityPlayer().isSneaking() || !event.getEntityPlayer().capabilities.allowEdit
                || state.getBlock() != Blocks.REDSTONE_WIRE
                || !isolated(raw(state, event.getWorld(), event.getPos()))) return;
        if (!event.getWorld().isRemote) {
            setDot(event.getWorld(), event.getPos(), !isDot(event.getWorld(), event.getPos()));
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos neighbor = event.getPos().offset(facing);
                event.getWorld().notifyNeighborsOfStateChange(neighbor, Blocks.REDSTONE_WIRE, false);
            }
            event.getWorld().notifyNeighborsOfStateChange(event.getPos(), Blocks.REDSTONE_WIRE, false);
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    public static long[] snapshot(World world, int chunkX, int chunkZ) {
        Set<Long> positions = dots(world, chunkX, chunkZ);
        return positions == null ? new long[0] : positions.stream().mapToLong(Long::longValue).toArray();
    }

    public static void apply(World world, int chunkX, int chunkZ, long[] positions) {
        Map<Long, Set<Long>> chunks = DOTS.computeIfAbsent(world, ignored -> new HashMap<>());
        long chunk = ChunkPos.asLong(chunkX, chunkZ);
        Set<Long> values = new HashSet<>();
        for (long value : positions) {
            BlockPos pos = BlockPos.fromLong(value);
            if (pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ) values.add(value);
        }
        if (values.isEmpty()) chunks.remove(chunk); else chunks.put(chunk, values);
        if (world.isRemote) world.markBlockRangeForRenderUpdate(
                new BlockPos(chunkX * 16, -64, chunkZ * 16),
                new BlockPos(chunkX * 16 + 15, 319, chunkZ * 16 + 15));
    }

    @SubscribeEvent
    public static void load(ChunkDataEvent.Load event) {
        NBTTagList list = event.getData().getTagList(KEY, 4);
        long[] positions = new long[list.tagCount()];
        for (int index = 0; index < positions.length; index++) positions[index] = ((NBTTagLong) list.get(index)).getLong();
        apply(event.getWorld(), event.getChunk().x, event.getChunk().z, positions);
    }

    @SubscribeEvent
    public static void save(ChunkDataEvent.Save event) {
        NBTTagList list = new NBTTagList();
        for (long pos : snapshot(event.getWorld(), event.getChunk().x, event.getChunk().z)) list.appendTag(new NBTTagLong(pos));
        event.getData().setTag(KEY, list);
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Watch event) {
        FFDNetwork.sendRedstoneDots(event.getPlayer(), event.getChunk().x, event.getChunk().z,
                snapshot(event.getPlayer().world, event.getChunk().x, event.getChunk().z));
    }

    @SubscribeEvent
    public static void unload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) return;
        Map<Long, Set<Long>> chunks = DOTS.get(event.getWorld());
        if (chunks != null) chunks.remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z));
    }

    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        DOTS.remove(event.getWorld());
    }
}
