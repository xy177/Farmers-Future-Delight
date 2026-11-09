package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.block.BlockBed;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import xy177.farmersfuturedelight.common.registry.FFDVillagerBedIndex;
import xy177.farmersfuturedelight.common.registry.FFDVillagerHomes;
import xy177.farmersfuturedelight.common.registry.FFDVillagerRoutines;

public final class EntityAIVillagerJumpOnBed extends EntityAIBase {
    private final EntityVillager villager;
    private BlockPos target;
    private int remainingTravel;
    private int remainingJumps;
    private int cooldown;

    public EntityAIVillagerJumpOnBed(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
    }

    private boolean available() {
        FFDVillagerRoutines.Activity activity = FFDVillagerRoutines.getActivity(villager);
        return villager.isChild() && !villager.isTrading() && !villager.isRiding() && !villager.isInWater()
                && !FFDVillagerHomes.isSleeping(villager)
                && (activity == FFDVillagerRoutines.Activity.PLAY || activity == FFDVillagerRoutines.Activity.IDLE);
    }

    @Override
    public boolean shouldExecute() {
        if (!available() || !villager.getNavigator().noPath() || villager.getRNG().nextInt(10) != 0) return false;
        if (!villager.world.getEntitiesWithinAABB(EntityVillager.class, villager.getEntityBoundingBox().grow(16.0D),
                other -> other != villager && other.isChild() && other.isEntityAlive()
                        && villager.canEntityBeSeen(other)).isEmpty()) return false;
        WorldServer world = (WorldServer) villager.world;
        BlockPos origin = villager.getPosition();
        target = null;
        double distance = 48.0D * 48.0D;
        for (int chunkX = (origin.getX() - 48) >> 4; chunkX <= (origin.getX() + 48) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - 48) >> 4; chunkZ <= (origin.getZ() + 48) >> 4; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockPos bed : FFDVillagerBedIndex.getBeds(world, chunk)) {
                    double candidateDistance = villager.getDistanceSqToCenter(bed);
                    if (candidateDistance < distance && jumpable(bed)) {
                        target = bed;
                        distance = candidateDistance;
                    }
                }
            }
        }
        return target != null && FFDVillagerHomes.canNavigate(villager, target);
    }

    private boolean jumpable(BlockPos position) {
        return villager.world.isBlockLoaded(position)
                && villager.world.getBlockState(position).getBlock() instanceof BlockBed;
    }

    private boolean overBed() {
        return jumpable(villager.getPosition()) || jumpable(villager.getPosition().down());
    }

    @Override
    public void startExecuting() {
        remainingTravel = 100;
        remainingJumps = 3 + villager.getRNG().nextInt(4);
        cooldown = 0;
        villager.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.5D);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return available() && target != null && jumpable(target)
                && (overBed() ? remainingJumps > 0 : remainingTravel > 0);
    }

    @Override
    public void updateTask() {
        if (!overBed()) {
            remainingTravel--;
        } else if (cooldown > 0) {
            cooldown--;
        } else if (jumpable(villager.getPosition())) {
            villager.getJumpHelper().setJumping();
            remainingJumps--;
            cooldown = 5;
        }
    }

    @Override
    public void resetTask() {
        villager.getNavigator().clearPath();
        target = null;
    }
}
