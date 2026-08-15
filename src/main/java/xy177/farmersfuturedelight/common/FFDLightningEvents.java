package xy177.farmersfuturedelight.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.block.BlockLightningRod;
import xy177.farmersfuturedelight.common.block.CopperWeathering;
import xy177.farmersfuturedelight.common.block.IWeatheringCopper;
import xy177.farmersfuturedelight.common.registry.FFDItems;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDLightningEvents {
    private static final Map<UUID, TrackedStrike> TRACKED_STRIKES = new HashMap<>();

    private FFDLightningEvents() {
    }

    @SubscribeEvent
    public static void onLightningAdded(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !FFDItems.isCopperEnabled()
                || !(event.getWorld() instanceof WorldServer)
                || !(event.getEntity() instanceof EntityLightningBolt)) {
            return;
        }

        WorldServer world = (WorldServer) event.getWorld();
        EntityLightningBolt lightning = (EntityLightningBolt) event.getEntity();
        BlockPos strikePos = new BlockPos(lightning.posX, lightning.posY - 1.0E-6D,
                lightning.posZ);
        BlockPos firePos = new BlockPos(lightning);
        TrackedStrike strike = new TrackedStrike(world, lightning, firePos,
                world.getTotalWorldTime() + 40L);
        strike.blocksSetOnFire = hasFreshInitialFire(world, firePos);
        TRACKED_STRIKES.put(lightning.getUniqueID(), strike);

        IBlockState strikeState = world.getBlockState(strikePos);
        if (strikeState.getBlock() instanceof BlockLightningRod) {
            ((BlockLightningRod) strikeState.getBlock()).onLightningStrike(world, strikePos,
                    strikeState);
        }
        cleanCopperOnLightningStrike(world, strikePos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        TrackedStrike strike = TRACKED_STRIKES.get(event.getLightning().getUniqueID());
        if (strike != null && !event.isCanceled()) {
            strike.hitEntities.add(event.getEntity().getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || !(event.world instanceof WorldServer)) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        Iterator<TrackedStrike> iterator = TRACKED_STRIKES.values().iterator();
        while (iterator.hasNext()) {
            TrackedStrike strike = iterator.next();
            if (strike.world != world) {
                continue;
            }
            if (isFreshFire(world, strike.firePos)) {
                strike.blocksSetOnFire = true;
            }
            if (!strike.lightning.isDead
                    && world.getTotalWorldTime() < strike.expireTime) {
                continue;
            }
            grantSurgeProtector(strike);
            iterator.remove();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof WorldServer)) {
            return;
        }
        WorldServer world = (WorldServer) event.getWorld();
        TRACKED_STRIKES.values().removeIf(strike -> strike.world == world);
    }

    private static boolean hasFreshInitialFire(WorldServer world, BlockPos center) {
        if (!world.getGameRules().getBoolean("doFireTick")
                || world.getDifficulty() != EnumDifficulty.NORMAL
                && world.getDifficulty() != EnumDifficulty.HARD
                || !world.isAreaLoaded(center, 10)) {
            return false;
        }
        for (BlockPos pos : BlockPos.getAllInBox(center.add(-1, -1, -1),
                center.add(1, 1, 1))) {
            if (isFreshFire(world, pos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFreshFire(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.FIRE && state.getValue(BlockFire.AGE) == 0;
    }

    private static void grantSurgeProtector(TrackedStrike strike) {
        if (strike.blocksSetOnFire) {
            return;
        }
        double x = strike.lightning.posX;
        double y = strike.lightning.posY;
        double z = strike.lightning.posZ;
        AxisAlignedBB bystanderBox = new AxisAlignedBB(
                x - 15.0D, y - 15.0D, z - 15.0D,
                x + 15.0D, y + 21.0D, z + 15.0D);
        boolean protectedVillager = false;
        for (EntityVillager villager
                : strike.world.getEntitiesWithinAABB(EntityVillager.class, bystanderBox)) {
            if (villager.isEntityAlive() && !strike.hitEntities.contains(villager.getUniqueID())) {
                protectedVillager = true;
                break;
            }
        }
        if (!protectedVillager) {
            return;
        }
        for (net.minecraft.entity.player.EntityPlayer player : strike.world.playerEntities) {
            if (player instanceof EntityPlayerMP && player.getDistanceSq(x, y, z) <= 900.0D) {
                FFDAdvancements.LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE
                        .trigger((EntityPlayerMP) player);
            }
        }
    }

    private static void cleanCopperOnLightningStrike(WorldServer world, BlockPos strikePos) {
        IBlockState struckState = world.getBlockState(strikePos);
        boolean waxed = CopperWeathering.isWaxed(struckState.getBlock());
        boolean weathering = struckState.getBlock() instanceof IWeatheringCopper
                && !((IWeatheringCopper) struckState.getBlock()).isWaxed();
        if (!weathering && !waxed) {
            return;
        }
        if (weathering) {
            IBlockState first = CopperWeathering.getFirst(struckState);
            if (first != null) {
                world.setBlockState(strikePos, first, 3);
            }
        }

        Random random = world.rand;
        int pathCount = random.nextInt(3) + 3;
        for (int path = 0; path < pathCount; path++) {
            BlockPos current = strikePos;
            int stepCount = random.nextInt(8) + 1;
            for (int step = 0; step < stepCount; step++) {
                BlockPos next = randomCleaningStep(world, current, random);
                if (next == null) {
                    break;
                }
                current = next;
            }
        }
    }

    private static BlockPos randomCleaningStep(WorldServer world, BlockPos origin, Random random) {
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos candidate = origin.add(random.nextInt(3) - 1, random.nextInt(3) - 1,
                    random.nextInt(3) - 1);
            IBlockState state = world.getBlockState(candidate);
            if (!(state.getBlock() instanceof IWeatheringCopper)
                    || ((IWeatheringCopper) state.getBlock()).isWaxed()) {
                continue;
            }
            IBlockState previous = CopperWeathering.getPrevious(state);
            if (previous != null) {
                world.setBlockState(candidate, previous, 3);
            }
            CopperWeathering.spawnLightningCleanParticles(world, candidate);
            return candidate;
        }
        return null;
    }

    private static final class TrackedStrike {
        private final WorldServer world;
        private final EntityLightningBolt lightning;
        private final BlockPos firePos;
        private final long expireTime;
        private final Set<UUID> hitEntities = new HashSet<>();
        private boolean blocksSetOnFire;

        private TrackedStrike(WorldServer world, EntityLightningBolt lightning, BlockPos firePos,
                              long expireTime) {
            this.world = world;
            this.lightning = lightning;
            this.firePos = firePos;
            this.expireTime = expireTime;
        }
    }
}
