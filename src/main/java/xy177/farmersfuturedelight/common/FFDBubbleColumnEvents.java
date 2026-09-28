package xy177.farmersfuturedelight.common;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockBubbleColumn;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDBubbleColumnEvents {
    private static final Map<World, Set<BlockPos>> PENDING = new WeakHashMap<>();
    private static final Map<EntityBoat, BoatState> BOATS = new WeakHashMap<>();

    private FFDBubbleColumnEvents() {
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getWorld().isRemote || !isRelevantChange(event.getWorld(), event.getPos(),
                event.getState())) {
            return;
        }
        queue(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.PlaceEvent event) {
        if (!event.getWorld().isRemote) {
            queue(event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote) {
            queue(event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (!event.getWorld().isRemote) {
            queue(event.getWorld(), event.getPos());
            queue(event.getWorld(), event.getLiquidPos());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!event.world.isRemote) {
            Set<BlockPos> pending;
            synchronized (PENDING) {
                pending = PENDING.remove(event.world);
            }
            if (pending != null) {
                for (BlockPos pos : pending) {
                    BlockBubbleColumn.refreshFromChange(event.world, pos);
                }
            }
        }
        tickBoats(event.world);
    }

    public static void touchBoat(EntityBoat boat, boolean drag) {
        if (boat == null || !FFDItems.isBlockRegistered(FFDBlocks.BUBBLE_COLUMN)) {
            return;
        }
        synchronized (BOATS) {
            BoatState state = BOATS.get(boat);
            if (state == null) {
                state = new BoatState();
                BOATS.put(boat, state);
            }
            state.touched = true;
            state.drag = drag;
            if (!boat.world.isRemote && state.ticks == 0) {
                state.ticks = 60;
            }
        }
    }

    public static float getBoatBubbleAngle(EntityBoat boat, float partialTicks) {
        synchronized (BOATS) {
            BoatState state = BOATS.get(boat);
            return state == null ? 0.0F
                    : state.anglePrevious + (state.angle - state.anglePrevious) * partialTicks;
        }
    }

    private static void queue(World world, BlockPos pos) {
        if (!FFDItems.isBlockRegistered(FFDBlocks.BUBBLE_COLUMN)) {
            return;
        }
        synchronized (PENDING) {
            PENDING.computeIfAbsent(world, key -> new LinkedHashSet<>()).add(pos.toImmutable());
        }
    }

    private static boolean isRelevant(IBlockState state) {
        return state.getBlock() == FFDBlocks.BUBBLE_COLUMN
                || state.getBlock() == net.minecraft.init.Blocks.SOUL_SAND
                || state.getBlock() == net.minecraft.init.Blocks.MAGMA
                || state.getMaterial() == Material.WATER;
    }

    private static boolean isRelevantChange(World world, BlockPos pos, IBlockState state) {
        return isRelevant(state) || isRelevant(world.getBlockState(pos.up()))
                || isRelevant(world.getBlockState(pos.down()));
    }

    private static void tickBoats(World world) {
        synchronized (BOATS) {
            Iterator<Map.Entry<EntityBoat, BoatState>> iterator = BOATS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntityBoat, BoatState> entry = iterator.next();
                EntityBoat boat = entry.getKey();
                BoatState state = entry.getValue();
                if (boat == null || boat.isDead) {
                    iterator.remove();
                    continue;
                }
                if (boat.world != world) {
                    continue;
                }
                if (world.isRemote) {
                    state.anglePrevious = state.angle;
                    state.multiplier = MathHelper.clamp(state.multiplier
                            + (state.touched ? 0.05F : -0.1F), 0.0F, 1.0F);
                    state.angle = 10.0F * MathHelper.sin(0.5F * boat.ticksExisted)
                            * state.multiplier;
                    state.touched = false;
                    if (state.multiplier == 0.0F) {
                        iterator.remove();
                    }
                    continue;
                }
                if (!state.touched) {
                    iterator.remove();
                    continue;
                }
                state.touched = false;
                if (state.ticks > 0) {
                    state.ticks--;
                }
                if (state.ticks == 0) {
                    applyBoatMotion(boat, state.drag);
                    iterator.remove();
                }
            }
        }
    }

    private static void applyBoatMotion(EntityBoat boat, boolean drag) {
        if (drag) {
            boat.motionY -= 0.7D;
            boat.removePassengers();
            return;
        }
        boat.motionY = 0.6D;
    }

    private static final class BoatState {
        private int ticks;
        private boolean drag;
        private boolean touched;
        private float multiplier;
        private float angle;
        private float anglePrevious;
    }
}
