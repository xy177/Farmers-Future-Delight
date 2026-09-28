package xy177.farmersfuturedelight.common.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public abstract class EntitySchoolingFish extends EntityAbstractFish {
    @Nullable
    private EntitySchoolingFish leader;
    private int schoolSize = 1;

    protected EntitySchoolingFish(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(5, new FollowSchoolLeaderGoal(this));
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return getMaxSchoolSize();
    }

    protected int getMaxSchoolSize() {
        return super.getMaxSpawnedInChunk();
    }

    @Override
    protected boolean canRandomSwim() {
        return !hasLeader();
    }

    public boolean hasLeader() {
        return leader != null && leader.isEntityAlive();
    }

    public EntitySchoolingFish follow(EntitySchoolingFish newLeader) {
        leader = newLeader;
        newLeader.schoolSize++;
        return newLeader;
    }

    public void stopFollowing() {
        if (leader != null) {
            leader.schoolSize = Math.max(1, leader.schoolSize - 1);
            leader = null;
        }
    }

    public boolean canAcceptFollowers() {
        return hasFollowers() && schoolSize < getMaxSchoolSize();
    }

    public boolean hasFollowers() {
        return schoolSize > 1;
    }

    public boolean isCloseToLeader() {
        return hasLeader() && getDistanceSq(leader) <= 121.0D;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (leader != null && !leader.isEntityAlive()) {
            stopFollowing();
        }
        if (!world.isRemote && hasFollowers() && rand.nextInt(200) == 1) {
            List<? extends EntitySchoolingFish> nearby = world.getEntitiesWithinAABB(
                    getClass(), getEntityBoundingBox().grow(8.0D));
            if (nearby.size() <= 1) {
                schoolSize = 1;
            }
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        if (livingData instanceof SchoolData) {
            follow(((SchoolData) livingData).leader);
            return livingData;
        }
        return new SchoolData(this);
    }

    public static class SchoolData implements IEntityLivingData {
        private final EntitySchoolingFish leader;

        public SchoolData(EntitySchoolingFish leader) {
            this.leader = leader;
        }
    }

    private static final class FollowSchoolLeaderGoal extends EntityAIBase {
        private final EntitySchoolingFish fish;
        private int searchDelay;
        private int pathDelay;

        private FollowSchoolLeaderGoal(EntitySchoolingFish fish) {
            this.fish = fish;
            searchDelay = nextStartDelay();
            setMutexBits(1);
        }

        private int nextStartDelay() {
            return 200 + fish.rand.nextInt(200) % 20;
        }

        @Override
        public boolean shouldExecute() {
            if (fish.hasFollowers()) {
                return false;
            }
            if (fish.hasLeader()) {
                return true;
            }
            if (searchDelay > 0) {
                searchDelay--;
                return false;
            }
            searchDelay = nextStartDelay();
            List<? extends EntitySchoolingFish> nearby = fish.world.getEntitiesWithinAABB(
                    fish.getClass(), fish.getEntityBoundingBox().grow(8.0D));
            EntitySchoolingFish newLeader = fish;
            for (EntitySchoolingFish candidate : nearby) {
                if (candidate.canAcceptFollowers()) {
                    newLeader = candidate;
                    break;
                }
            }
            int remaining = newLeader.getMaxSchoolSize() - newLeader.schoolSize;
            for (EntitySchoolingFish candidate : nearby) {
                if (remaining <= 0) {
                    break;
                }
                if (candidate != newLeader && !candidate.hasLeader()) {
                    candidate.follow(newLeader);
                    remaining--;
                }
            }
            return fish.hasLeader();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return fish.hasLeader() && fish.isCloseToLeader();
        }

        @Override
        public void startExecuting() {
            pathDelay = 0;
        }

        @Override
        public void updateTask() {
            if (--pathDelay <= 0) {
                pathDelay = 10;
                fish.getNavigator().tryMoveToEntityLiving(fish.leader, 1.0D);
            }
        }

        @Override
        public void resetTask() {
            fish.stopFollowing();
        }
    }
}
