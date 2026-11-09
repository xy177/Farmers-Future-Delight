package xy177.farmersfuturedelight.common.raid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class RaidParticipantAI extends EntityAIBase {
    private final EntityLiving entity;
    private final RaidInstance instance;
    private final List<BlockPos> visited = new ArrayList<>();
    private BlockPos target;
    private int repath;

    private RaidParticipantAI(EntityLiving entity, RaidInstance instance) {
        this.entity = entity;
        this.instance = instance;
        setMutexBits(1);
    }

    static void install(EntityLiving entity, RaidInstance instance) {
        for (EntityAITasks.EntityAITaskEntry entry : entity.tasks.taskEntries) {
            if (entry.action instanceof RaidParticipantAI && ((RaidParticipantAI) entry.action).instance == instance) return;
        }
        remove(entity);
        entity.tasks.addTask(1, new Banner(entity, instance));
        entity.tasks.addTask(3, new RaidParticipantAI(entity, instance));
        if (instance.definition.getScriptRules() != null && instance.definition.getScriptRules().hasTargeting()) {
            entity.targetTasks.addTask(0, new ScriptTarget(entity, instance));
        }
    }

    static void remove(EntityLiving entity) {
        for (EntityAITasks.EntityAITaskEntry entry : new ArrayList<>(entity.tasks.taskEntries)) {
            if (entry.action instanceof RaidParticipantAI || entry.action instanceof Banner) entity.tasks.removeTask(entry.action);
        }
        for (EntityAITasks.EntityAITaskEntry entry : new ArrayList<>(entity.targetTasks.taskEntries)) {
            if (entry.action instanceof ScriptTarget) {
                ScriptTarget target = (ScriptTarget) entry.action;
                if (entity.getAttackTarget() == target.target) entity.setAttackTarget(null);
                entity.targetTasks.removeTask(entry.action);
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        if (!instance.isOngoing() || entity.isRiding() || entity.getAttackTarget() != null) return false;
        if (entity.getPosition().distanceSq(instance.center) > 1024.0D) {
            target = instance.center;
            return true;
        }
        List<BlockPos> candidates = RaidVillages.occupied(instance.world(), entity.getPosition(), 3);
        candidates.removeIf(position -> visited.contains(position) || !instance.world().isBlockLoaded(position)
                || position.distanceSq(entity.getPosition()) < 4.0D);
        if (candidates.isEmpty()) return false;
        target = candidates.get(entity.getRNG().nextInt(candidates.size()));
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return instance.isOngoing() && entity.getAttackTarget() == null && target != null
                && !entity.isRiding() && entity.getDistanceSqToCenter(target) > 4.0D;
    }

    @Override
    public void startExecuting() { repath = 0; }

    @Override
    public void updateTask() {
        if (--repath > 0) return;
        repath = 20;
        if (entity.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.05D)) return;
        if (!(entity instanceof net.minecraft.entity.EntityCreature)) return;
        Vec3d next = RandomPositionGenerator.findRandomTargetBlockTowards((net.minecraft.entity.EntityCreature) entity,
                16, 7, new Vec3d(target).addVector(0.5D, 0.0D, 0.5D));
        if (next != null) entity.getNavigator().tryMoveToXYZ(next.x, next.y, next.z, 1.0D);
    }

    @Override
    public void resetTask() {
        if (target != null && entity.getDistanceSqToCenter(target) <= 4.0D) {
            visited.add(target);
            if (visited.size() > 2) visited.remove(0);
        }
        target = null;
        entity.getNavigator().clearPath();
    }

    private static final class Banner extends EntityAIBase {
        private final EntityLiving entity;
        private final RaidInstance instance;
        private final Map<Integer, Long> unreachable = new HashMap<>();
        private EntityItem target;
        private Path path;

        Banner(EntityLiving entity, RaidInstance instance) {
            this.entity = entity;
            this.instance = instance;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!instance.canSucceed(entity) || entity.isRiding()) return false;
            long now = instance.world().getTotalWorldTime();
            Iterator<Long> expiry = unreachable.values().iterator();
            while (expiry.hasNext()) if (expiry.next() <= now) expiry.remove();
            double follow = entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            List<EntityItem> items = instance.world().getEntitiesWithinAABB(EntityItem.class,
                    entity.getEntityBoundingBox().grow(follow, 8.0D, follow),
                    item -> item.isEntityAlive() && !item.cannotPickup() && ItemStack.areItemStacksEqual(item.getItem(), FFDRaidCaptains.createBanner()));
            items.sort(Comparator.comparingDouble(entity::getDistanceSq));
            for (EntityItem item : items) {
                if (unreachable.containsKey(item.getEntityId())) continue;
                Path candidate = entity.getNavigator().getPathToEntityLiving(item);
                if (candidate != null && candidate.getFinalPathPoint() != null
                        && candidate.getFinalPathPoint().distanceTo(new net.minecraft.pathfinding.PathPoint(
                                net.minecraft.util.math.MathHelper.floor(item.posX), net.minecraft.util.math.MathHelper.floor(item.posY),
                                net.minecraft.util.math.MathHelper.floor(item.posZ))) < 2.0F) {
                    target = item;
                    path = candidate;
                    return true;
                }
                unreachable.put(item.getEntityId(), now + 600L);
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && target.isEntityAlive() && !target.cannotPickup() && instance.canSucceed(entity)
                    && (entity.getDistanceSq(target) < 2.0D || !entity.getNavigator().noPath());
        }

        @Override
        public void startExecuting() { entity.getNavigator().setPath(path, 1.15D); }

        @Override
        public void updateTask() {
            if (!instance.canSucceed(entity) || target == null || entity.getDistanceSq(target) >= 2.0D) return;
            ItemStack previous = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            NBTTagCompound saved = entity.writeToNBT(new NBTTagCompound());
            net.minecraft.nbt.NBTTagList chances = saved.getTagList("ArmorDropChances", 5);
            float chance = chances.tagCount() > 3 ? chances.getFloatAt(3) : 0.085F;
            if (!previous.isEmpty() && Math.max(entity.getRNG().nextFloat() - 0.1F, 0.0F) < chance) entity.entityDropItem(previous.copy(), 0.0F);
            entity.onItemPickup(target, target.getItem().getCount());
            target.setDead();
            instance.succeed(entity);
        }

        @Override
        public void resetTask() {
            target = null;
            path = null;
            entity.getNavigator().clearPath();
        }
    }

    private static final class ScriptTarget extends EntityAIBase {
        private final EntityLiving entity;
        private final RaidInstance instance;
        private net.minecraft.entity.EntityLivingBase target;

        private ScriptTarget(EntityLiving entity, RaidInstance instance) {
            this.entity = entity;
            this.instance = instance;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!instance.isOngoing() || entity.ticksExisted % 10 != 0) return false;
            xy177.farmersfuturedelight.api.raid.RaidScriptRules rules = instance.definition.getScriptRules();
            double distance = entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
            List<net.minecraft.entity.EntityLivingBase> candidates = entity.world.getEntitiesWithinAABB(net.minecraft.entity.EntityLivingBase.class,
                    entity.getEntityBoundingBox().grow(distance), candidate -> candidate != entity && candidate.isEntityAlive()
                    && !instance.manager.owns(candidate.getUniqueID()) && instance.contains(candidate.getPosition())
                    && (!(candidate instanceof net.minecraft.entity.player.EntityPlayer)
                    || !((net.minecraft.entity.player.EntityPlayer) candidate).isSpectator() && !((net.minecraft.entity.player.EntityPlayer) candidate).isCreative())
                    && rules.isHostile(candidate) && entity.canEntityBeSeen(candidate));
            candidates.sort(java.util.Comparator.<net.minecraft.entity.EntityLivingBase>comparingInt(candidate -> rules.isTarget(candidate) ? 0 : 1)
                    .thenComparingDouble(candidate -> entity.getDistanceSq(candidate)));
            target = candidates.isEmpty() ? null : candidates.get(0);
            return target != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return instance.isOngoing() && target != null && target.isEntityAlive() && instance.contains(target.getPosition())
                    && entity.getAttackTarget() == target && entity.ticksExisted % 20 != 0;
        }

        @Override
        public void startExecuting() { entity.setAttackTarget(target); }

        @Override
        public void resetTask() {
            if (entity.getAttackTarget() == target) entity.setAttackTarget(null);
            target = null;
        }
    }
}
