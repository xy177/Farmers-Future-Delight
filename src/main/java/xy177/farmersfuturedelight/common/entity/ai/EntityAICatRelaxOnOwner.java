package xy177.farmersfuturedelight.common.entity.ai;

import java.util.List;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import xy177.farmersfuturedelight.common.FFDEntityLootTables;
import xy177.farmersfuturedelight.common.entity.EntityCat;

public class EntityAICatRelaxOnOwner extends EntityAIBase {
    private final EntityCat cat;
    private EntityPlayer owner;
    private BlockPos goalPos;
    private int restingTicks;

    public EntityAICatRelaxOnOwner(EntityCat cat) {
        this.cat = cat;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!cat.isTamed() || cat.isSitting()) {
            return false;
        }
        EntityLivingBase ownerEntity = cat.getOwner();
        if (!(ownerEntity instanceof EntityPlayer)) {
            return false;
        }
        owner = (EntityPlayer) ownerEntity;
        if (!owner.isPlayerSleeping() || cat.getDistanceSq(owner) > 100.0D) {
            return false;
        }
        BlockPos bedPos = owner.getPosition();
        IBlockState bedState = cat.world.getBlockState(bedPos);
        if (!(bedState.getBlock() instanceof BlockBed)) {
            return false;
        }
        goalPos = bedPos.offset(bedState.getValue(BlockBed.FACING).getOpposite());
        return !isOccupied();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return cat.isTamed() && owner != null && owner.isPlayerSleeping()
                && !cat.isSitting() && goalPos != null && !isOccupied();
    }

    @Override
    public void startExecuting() {
        cat.setSitting(false);
        cat.getNavigator().tryMoveToXYZ(
                goalPos.getX() + 0.5D, goalPos.getY(), goalPos.getZ() + 0.5D, 1.1D);
    }

    @Override
    public void updateTask() {
        if (goalPos == null || owner == null) {
            return;
        }
        cat.getNavigator().tryMoveToXYZ(
                goalPos.getX() + 0.5D, goalPos.getY(), goalPos.getZ() + 0.5D, 1.1D);
        if (cat.getDistanceSq(owner) < 2.5D) {
            restingTicks++;
            if (restingTicks > 16) {
                cat.setLyingWithOwner(true);
                cat.setRelaxStateOne(false);
            } else {
                cat.setLyingWithOwner(false);
                cat.setRelaxStateOne(true);
            }
        } else {
            cat.setLyingWithOwner(false);
            cat.setRelaxStateOne(false);
        }
    }

    @Override
    public void resetTask() {
        boolean giveGift = owner != null && owner.getSleepTimer() >= 100
                && cat.world.rand.nextFloat() < 0.7F;
        cat.setLyingWithOwner(false);
        cat.setRelaxStateOne(false);
        cat.getNavigator().clearPath();
        if (giveGift && cat.world instanceof WorldServer) {
            dropMorningGift((WorldServer) cat.world, owner);
        }
        restingTicks = 0;
        owner = null;
        goalPos = null;
    }

    private boolean isOccupied() {
        if (goalPos == null) {
            return false;
        }
        List<EntityCat> nearbyCats = cat.world.getEntitiesWithinAABB(EntityCat.class,
                new AxisAlignedBB(goalPos).grow(2.0D));
        for (EntityCat other : nearbyCats) {
            if (other != cat && (other.isLyingWithOwner() || other.isRelaxStateOne())) {
                return true;
            }
        }
        return false;
    }

    private void dropMorningGift(WorldServer world, EntityPlayer owner) {
        BlockPos origin = cat.getLeashed() && cat.getLeashHolder() != null
                ? cat.getLeashHolder().getPosition() : cat.getPosition();
        BlockPos giftPosition = findGiftPosition(origin);
        if (giftPosition != null) {
            cat.setPositionAndUpdate(giftPosition.getX() + 0.5D,
                    giftPosition.getY(), giftPosition.getZ() + 0.5D);
        }
        LootContext context = new LootContext.Builder(world).withLootedEntity(cat)
                .withPlayer(owner).withLuck(owner.getLuck()).build();
        double radians = Math.toRadians(cat.renderYawOffset);
        for (ItemStack gift : world.getLootTableManager()
                .getLootTableFromLocation(FFDEntityLootTables.CAT_MORNING_GIFT)
                .generateLootForPools(cat.getRNG(), context)) {
            if (gift.isEmpty()) continue;
            EntityItem item = new EntityItem(world,
                    cat.posX - MathHelper.sin((float) radians), cat.posY,
                    cat.posZ + MathHelper.cos((float) radians), gift);
            world.spawnEntity(item);
        }
    }

    private BlockPos findGiftPosition(BlockPos origin) {
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos candidate = origin.add(cat.world.rand.nextInt(11) - 5,
                    cat.world.rand.nextInt(5) - 2, cat.world.rand.nextInt(11) - 5);
            if (cat.world.isAirBlock(candidate)
                    && cat.world.getBlockState(candidate.down()).isSideSolid(
                    cat.world, candidate.down(), net.minecraft.util.EnumFacing.UP)
                    && cat.world.getCollisionBoxes(cat,
                    cat.getEntityBoundingBox().offset(candidate.getX() + 0.5D - cat.posX,
                            candidate.getY() - cat.posY, candidate.getZ() + 0.5D - cat.posZ))
                    .isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

}
