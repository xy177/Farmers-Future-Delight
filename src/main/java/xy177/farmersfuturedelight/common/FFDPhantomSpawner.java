package xy177.farmersfuturedelight.common;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityPhantom;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDPhantomSpawner {
    private static final String REST_TICKS = FarmerFutureDelight.MODID + ".phantomRestTicks";
    private static final Map<WorldServer, Integer> NEXT_TICKS = new WeakHashMap<>();

    private FFDPhantomSpawner() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != Side.SERVER || event.phase != TickEvent.Phase.END
                || !(event.player instanceof EntityPlayerMP)) {
            return;
        }
        NBTTagCompound data = event.player.getEntityData();
        if (event.player.isPlayerSleeping()) {
            data.setInteger(REST_TICKS, 0);
            return;
        }
        int ticks = data.getInteger(REST_TICKS);
        if (ticks < Integer.MAX_VALUE) {
            data.setInteger(REST_TICKS, ticks + 1);
        }
    }

    @SubscribeEvent
    public static void onPlayerWake(PlayerWakeUpEvent event) {
        event.getEntityPlayer().getEntityData().setInteger(REST_TICKS, 0);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            event.getEntityLiving().getEntityData().setInteger(REST_TICKS, 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        int restTicks = event.isWasDeath() ? 0
                : event.getOriginal().getEntityData().getInteger(REST_TICKS);
        event.getEntityPlayer().getEntityData().setInteger(REST_TICKS, restTicks);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != TickEvent.Phase.END
                || !(event.world instanceof WorldServer) || !FFDEntities.isPhantomEnabled()) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        if (world.provider.getDimension() != 0
                || world.getDifficulty() == EnumDifficulty.PEACEFUL
                || !world.getGameRules().getBoolean("doMobSpawning")) {
            return;
        }

        int nextTick = NEXT_TICKS.containsKey(world) ? NEXT_TICKS.get(world) - 1 : -1;
        if (nextTick > 0) {
            NEXT_TICKS.put(world, nextTick);
            return;
        }
        int minimum = Math.min(FFDConfig.phantomSpawnMinIntervalSeconds,
                FFDConfig.phantomSpawnMaxIntervalSeconds);
        int maximum = Math.max(FFDConfig.phantomSpawnMinIntervalSeconds,
                FFDConfig.phantomSpawnMaxIntervalSeconds);
        nextTick += (minimum + world.rand.nextInt(maximum - minimum + 1)) * 20;
        NEXT_TICKS.put(world, nextTick);

        if (world.provider.hasSkyLight() && world.getSkylightSubtracted() < 5) {
            return;
        }

        for (EntityPlayer player : world.playerEntities) {
            if (!(player instanceof EntityPlayerMP) || player.isSpectator()) {
                continue;
            }
            BlockPos playerPos = player.getPosition();
            if (world.provider.hasSkyLight()
                    && (playerPos.getY() < world.getSeaLevel() || !world.canSeeSky(playerPos))) {
                continue;
            }
            DifficultyInstance difficulty = world.getDifficultyForLocation(playerPos);
            if (!difficulty.isHarderThan(world.rand.nextFloat() * 3.0F)) {
                continue;
            }

            int restTicks = Math.max(1, player.getEntityData().getInteger(REST_TICKS));
            if (world.rand.nextInt(restTicks) < FFDConfig.phantomInsomniaThresholdTicks) {
                continue;
            }
            BlockPos spawnPos = playerPos.up(20 + world.rand.nextInt(15)).add(
                    -10 + world.rand.nextInt(21), 0, -10 + world.rand.nextInt(21));
            if (!isValidSpawnBlock(world, spawnPos)) {
                continue;
            }

            IEntityLivingData groupData = null;
            int groupSize = 1 + world.rand.nextInt(world.getDifficulty().getDifficultyId() + 1);
            for (int i = 0; i < groupSize; i++) {
                EntityPhantom phantom = new EntityPhantom(world);
                phantom.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(),
                        spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                groupData = phantom.onInitialSpawn(difficulty, groupData);
                world.spawnEntity(phantom);
            }
        }
    }

    private static boolean isValidSpawnBlock(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return !state.isFullCube() && !state.getMaterial().isLiquid()
                && !state.getBlock().canProvidePower(state);
    }
}
