package xy177.farmersfuturedelight.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.init.Biomes;
import net.minecraft.world.WorldServer;
import net.minecraft.village.Village;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.EntityCat;
import xy177.farmersfuturedelight.common.registry.FFDEntities;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDCatSpawner {
    private static final int SPAWN_INTERVAL_TICKS = 1200;
    private static final int VILLAGE_RADIUS = 48;
    private static final int MAX_VILLAGE_CATS = 5;

    private FFDCatSpawner() {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != net.minecraftforge.fml.relauncher.Side.SERVER
                || event.phase != TickEvent.Phase.END
                || !(event.world instanceof WorldServer)
                || !FFDEntities.isLocalCatEnabled()) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        if (world.provider.getDimension() != 0
                || !world.getGameRules().getBoolean("doMobSpawning")
                || world.getTotalWorldTime() % SPAWN_INTERVAL_TICKS != 0L) {
            return;
        }
        List<EntityPlayer> players = new ArrayList<>();
        for (EntityPlayer player : world.playerEntities) {
            if (!player.isSpectator()) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            return;
        }
        EntityPlayer player = players.get(world.rand.nextInt(players.size()));
        int offsetX = (8 + world.rand.nextInt(24))
                * (world.rand.nextBoolean() ? -1 : 1);
        int offsetZ = (8 + world.rand.nextInt(24))
                * (world.rand.nextBoolean() ? -1 : 1);
        BlockPos spawnPos = player.getPosition().add(offsetX, 0, offsetZ);
        if (!world.isAreaLoaded(spawnPos, 10, false)) {
            return;
        }
        Village village = world.getVillageCollection().getNearestVillage(spawnPos, 2);
        boolean closeToVillage = village != null;
        boolean villageSpawn = closeToVillage && village.getNumVillageDoors() > 4;
        boolean hutSpawn = !closeToVillage && world.getBiome(spawnPos) == Biomes.SWAMPLAND
                && world.getChunkProvider().isInsideStructure(world, "Temple", spawnPos);
        if (!villageSpawn && !hutSpawn) {
            return;
        }
        AxisAlignedBB villageArea = new AxisAlignedBB(spawnPos).grow(
                villageSpawn ? VILLAGE_RADIUS : 16.0D, 8.0D,
                villageSpawn ? VILLAGE_RADIUS : 16.0D);
        int nearbyCats = world.getEntitiesWithinAABB(EntityCat.class, villageArea).size();
        if (villageSpawn && nearbyCats >= MAX_VILLAGE_CATS || hutSpawn && nearbyCats > 0) {
            return;
        }
        EntityCat cat = new EntityCat(world);
        cat.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(),
                spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        Event.Result spawnResult = ForgeEventFactory.canEntitySpawn(cat, world,
                (float) cat.posX, (float) cat.posY, (float) cat.posZ, false);
        if (spawnResult == Event.Result.DENY || spawnResult == Event.Result.DEFAULT
                && !cat.isNotColliding()) {
            return;
        }
        DifficultyInstance difficulty = world.getDifficultyForLocation(spawnPos);
        cat.onInitialSpawn(difficulty, null);
        if (hutSpawn) {
            cat.enablePersistence();
        }
        world.spawnEntity(cat);
    }
}
