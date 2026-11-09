package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityTraderLlama;
import xy177.farmersfuturedelight.common.entity.EntityWanderingTrader;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.tile.TileEntityBell;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDWanderingTraderSpawner {
    private static final String RULE = xy177.farmersfuturedelight.common.raid.RaidGameRules.TRADERS;
    private static final String DATA_NAME = "farmers_future_delight_wandering_trader";

    private FFDWanderingTraderSpawner() {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != net.minecraftforge.fml.relauncher.Side.SERVER
                || event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)
                || event.world.provider.getDimension() != 0 || !FFDEntities.isLocalWanderingTraderEnabled()) return;
        WorldServer world = (WorldServer) event.world;
        GameRules rules = world.getGameRules();
        xy177.farmersfuturedelight.common.raid.RaidGameRules.ensure(rules);
        if (!rules.getBoolean(RULE)) return;
        TraderData data = getData(world);
        if (--data.tickDelay > 0) return;
        data.tickDelay = FFDConfig.wanderingTraderCheckInterval;
        data.spawnDelay -= FFDConfig.wanderingTraderCheckInterval;
        data.markDirty();
        if (data.spawnDelay > 0) return;
        data.spawnDelay = FFDConfig.wanderingTraderSpawnInterval;
        int chance = MathHelper.clamp(data.spawnChance, FFDConfig.wanderingTraderInitialChance,
                FFDConfig.wanderingTraderMaximumChance);
        data.spawnChance = MathHelper.clamp(chance + FFDConfig.wanderingTraderChanceStep,
                FFDConfig.wanderingTraderInitialChance, FFDConfig.wanderingTraderMaximumChance);
        if (world.rand.nextInt(100) >= chance) return;
        if (spawn(world)) data.spawnChance = FFDConfig.wanderingTraderInitialChance;
    }

    public static TraderData getData(WorldServer world) {
        MapStorage storage = world.getPerWorldStorage();
        TraderData data = (TraderData) storage.getOrLoadData(TraderData.class, DATA_NAME);
        if (data == null) {
            data = new TraderData(DATA_NAME);
            storage.setData(DATA_NAME, data);
            data.markDirty();
        }
        return data;
    }

    public static boolean spawn(WorldServer world) {
        List<EntityPlayer> players = new ArrayList<>();
        for (EntityPlayer player : world.playerEntities) {
            if (!player.isSpectator()) players.add(player);
        }
        if (players.isEmpty()) return true;
        EntityPlayer player = players.get(world.rand.nextInt(players.size()));
        if (world.rand.nextInt(100) >= FFDConfig.wanderingTraderPositionChance) return false;
        BlockPos reference = findMeetingPoint(world, player.getPosition(), FFDConfig.wanderingTraderMeetingPointRange);
        BlockPos position = findSpawnPosition(world, reference, FFDConfig.wanderingTraderSpawnRadius,
                FFDConfig.wanderingTraderSpawnAttempts);
        if (position == null || world.getBiome(position) == Biomes.VOID || !hasEnoughSpace(world, position)) return false;
        EntityWanderingTrader trader = new EntityWanderingTrader(world);
        trader.setLocationAndAngles(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F, 0.0F);
        if (ForgeEventFactory.canEntitySpawn(trader, world, (float) trader.posX,
                (float) trader.posY, (float) trader.posZ, false) == Event.Result.DENY) return false;
        trader.onInitialSpawn(world.getDifficultyForLocation(position), null);
        trader.setGrowingAge(0);
        trader.setDespawnDelay(FFDConfig.wanderingTraderDespawnDelay);
        trader.setWanderTarget(reference);
        trader.setHomePosAndDistance(reference, 16);
        if (!world.spawnEntity(trader)) return false;
        if (FFDEntities.isLocalTraderLlamaEnabled()) {
            for (int index = 0; index < FFDConfig.wanderingTraderLlamaCount; index++) spawnLlama(world, trader);
        }
        return true;
    }

    private static BlockPos findMeetingPoint(WorldServer world, BlockPos player, int range) {
        BlockPos nearest = player;
        double nearestDistance = Double.MAX_VALUE;
        for (int chunkX = (player.getX() - range) >> 4; chunkX <= (player.getX() + range) >> 4; chunkX++) {
            for (int chunkZ = (player.getZ() - range) >> 4; chunkZ <= (player.getZ() + range) >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (TileEntity tile : chunk.getTileEntityMap().values()) {
                    if (!(tile instanceof TileEntityBell) || tile.isInvalid()) continue;
                    double distance = tile.getPos().distanceSq(player);
                    if (distance <= range * (double) range && distance < nearestDistance) {
                        nearest = tile.getPos();
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private static void spawnLlama(WorldServer world, EntityWanderingTrader trader) {
        BlockPos position = findSpawnPosition(world, trader.getPosition(), FFDConfig.wanderingTraderLlamaRadius,
                FFDConfig.wanderingTraderSpawnAttempts);
        if (position == null) return;
        EntityTraderLlama llama = new EntityTraderLlama(world);
        llama.setLocationAndAngles(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D,
                world.rand.nextFloat() * 360.0F, 0.0F);
        if (ForgeEventFactory.canEntitySpawn(llama, world, (float) llama.posX,
                (float) llama.posY, (float) llama.posZ, false) == Event.Result.DENY) return;
        llama.onInitialSpawn(world.getDifficultyForLocation(position), null);
        llama.setGrowingAge(0);
        if (!world.getCollisionBoxes(llama, llama.getEntityBoundingBox()).isEmpty()) return;
        if (world.spawnEntity(llama)) llama.setLeashHolder(trader, true);
    }

    @Nullable
    private static BlockPos findSpawnPosition(WorldServer world, BlockPos reference, int radius, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos column = reference.add(world.rand.nextInt(radius * 2) - radius, 0,
                    world.rand.nextInt(radius * 2) - radius);
            if (!world.isBlockLoaded(column)) continue;
            BlockPos position = world.getTopSolidOrLiquidBlock(column);
            if (world.isAreaLoaded(position.add(-1, -1, -1), position.add(2, 3, 2))
                    && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, position)) {
                return position;
            }
        }
        return null;
    }

    private static boolean hasEnoughSpace(WorldServer world, BlockPos position) {
        for (BlockPos sample : BlockPos.getAllInBox(position, position.add(1, 2, 1))) {
            IBlockState state = world.getBlockState(sample);
            if (state.getCollisionBoundingBox(world, sample) != null) return false;
        }
        return true;
    }

    public static final class TraderData extends WorldSavedData {
        private int tickDelay = FFDConfig.wanderingTraderCheckInterval;
        private int spawnDelay = FFDConfig.wanderingTraderSpawnInterval;
        private int spawnChance = FFDConfig.wanderingTraderInitialChance;

        public TraderData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            spawnDelay = compound.hasKey("SpawnDelay", 99) ? compound.getInteger("SpawnDelay")
                    : FFDConfig.wanderingTraderSpawnInterval;
            spawnChance = compound.hasKey("SpawnChance", 99)
                    ? MathHelper.clamp(compound.getInteger("SpawnChance"), 0, 100)
                    : FFDConfig.wanderingTraderInitialChance;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            compound.setInteger("SpawnDelay", spawnDelay);
            compound.setInteger("SpawnChance", spawnChance);
            return compound;
        }
    }
}
