package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerHeroGift;
import xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerRaidResponse;
import xy177.farmersfuturedelight.common.registry.FFDModernVillagerTrades;
import xy177.farmersfuturedelight.common.registry.FFDProfessionWorkstations;
import xy177.farmersfuturedelight.common.registry.FFDVillagerBedIndex;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRestocking;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRaidVillagers {
    private static final String HEARD_BELL_TAG = "farmers_future_delight:heard_bell_time";

    private FFDRaidVillagers() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || event.getEntity().getClass() != EntityVillager.class) return;
        EntityVillager villager = (EntityVillager) event.getEntity();
        for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry task : villager.tasks.taskEntries) {
            if (task.action instanceof EntityAIVillagerRaidResponse) return;
        }
        villager.tasks.addTask(2, new EntityAIVillagerRaidResponse(villager));
        villager.tasks.addTask(3, new EntityAIVillagerHeroGift(villager));
    }

    public static void heardBell(EntityVillager villager, long now) {
        if (villager.getClass() == EntityVillager.class) {
            villager.getEntityData().setLong(HEARD_BELL_TAG, now);
        }
    }

    public static boolean hasHeardBell(EntityVillager villager) {
        if (!villager.getEntityData().hasKey(HEARD_BELL_TAG, 99)) return false;
        if (villager.world.getTotalWorldTime() >= villager.getEntityData().getLong(HEARD_BELL_TAG) + 300L) {
            villager.getEntityData().removeTag(HEARD_BELL_TAG);
            return false;
        }
        return true;
    }

    public static BlockPos findHidingPlace(EntityVillager villager, int radius, int closeEnough) {
        WorldServer world = (WorldServer) villager.world;
        BlockPos origin = villager.getPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (int chunkX = (origin.getX() - radius) >> 4; chunkX <= (origin.getX() + radius) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - radius) >> 4; chunkZ <= (origin.getZ() + radius) >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos position : FFDVillagerBedIndex.getBeds(world, chunk)) {
                    if (origin.distanceSq(position) > (double) radius * radius) continue;
                    IBlockState state = chunk.getBlockState(position);
                    if (!FFDVillagerHomes.isBedHead(state)) continue;
                    if (villager.getDistanceSqToCenter(position) < (double) closeEnough * closeEnough) return position;
                    candidates.add(position);
                }
            }
        }
        if (!candidates.isEmpty()) return candidates.get(villager.getRNG().nextInt(candidates.size()));
        return FFDVillagerHomes.getHome(villager);
    }

    public static List<ItemStack> gifts(EntityVillager villager, EntityPlayer player) {
        if (!(villager.world instanceof WorldServer)) return Collections.emptyList();
        String profession = null;
        if (FFDVillagerRestocking.isManaged(villager)
                && !FFDProfessionWorkstations.isProfessionUnassigned(villager)) {
            profession = FFDModernVillagerTrades.getModernProfession(villager);
        }
        String name = villager.isChild() ? "baby" : profession == null ? "unemployed" : profession;
        ResourceLocation table = new ResourceLocation(FarmerFutureDelight.MODID,
                "gameplay/hero_of_the_village/" + name + "_gift");
        WorldServer world = (WorldServer) villager.world;
        LootContext context = new LootContext.Builder(world).withLootedEntity(villager)
                .withPlayer(player).withLuck(player.getLuck()).build();
        return world.getLootTableManager().getLootTableFromLocation(table)
                .generateLootForPools(villager.getRNG(), context);
    }
}
