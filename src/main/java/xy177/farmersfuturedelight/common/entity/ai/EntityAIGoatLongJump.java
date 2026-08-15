package xy177.farmersfuturedelight.common.entity.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public class EntityAIGoatLongJump extends EntityAIBase {
    private static final int MAX_CANDIDATE_TRIES = 20;
    private static final int MAX_AIRBORNE_TICKS = 100;
    private static final double BASE_JUMP_STRENGTH = 0.42D;
    private static final double GRAVITY = 0.08D;
    private static final List<Integer> ALLOWED_ANGLES = Arrays.asList(65, 70, 75, 80);
    private static volatile CandidateSet cachedCandidates;

    private final EntityGoat goat;
    private Vec3d launchVelocity;
    private BlockPos landingPos;
    private Vec3d initialPosition;
    private int prepareTicks;
    private int airborneTicks;
    private boolean launched;

    public EntityAIGoatLongJump(EntityGoat goat) {
        this.goat = goat;
        setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (goat.getLongJumpCooldown() > 0 || !goat.onGround || goat.isInWater()
                || goat.isInLava() || goat.isInLove() || goat.isBeingRidden()
                || goat.world.getBlockState(goat.getPosition()).getBlock() == FFDBlocks.HONEY_BLOCK) {
            return false;
        }

        JumpPlan plan = findJumpPlan();
        if (plan == null) {
            goat.setLongJumpCooldown(Math.max(1, goat.nextLongJumpCooldown() / 2));
            return false;
        }
        landingPos = plan.landingPos;
        launchVelocity = plan.velocity;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!goat.isEntityAlive() || goat.isInWater() || goat.isInLava()) {
            return false;
        }
        if (launched) {
            return airborneTicks == 0 || !goat.onGround && airborneTicks < MAX_AIRBORNE_TICKS;
        }
        return initialPosition != null && goat.getPositionVector().squareDistanceTo(initialPosition) < 0.01D
                && prepareTicks <= FFDConfig.goatLongJumpPrepareTicks;
    }

    @Override
    public void startExecuting() {
        launched = false;
        prepareTicks = 0;
        airborneTicks = 0;
        initialPosition = goat.getPositionVector();
        goat.getNavigator().clearPath();
        goat.getLookHelper().setLookPosition(landingPos.getX() + 0.5D,
                landingPos.getY() + 0.5D, landingPos.getZ() + 0.5D,
                goat.getHorizontalFaceSpeed(), goat.getVerticalFaceSpeed());
    }

    @Override
    public void updateTask() {
        if (launched) {
            airborneTicks++;
            return;
        }

        goat.getNavigator().clearPath();
        goat.getLookHelper().setLookPosition(landingPos.getX() + 0.5D,
                landingPos.getY() + 0.5D, landingPos.getZ() + 0.5D,
                goat.getHorizontalFaceSpeed(), goat.getVerticalFaceSpeed());
        prepareTicks++;
        if (prepareTicks < FFDConfig.goatLongJumpPrepareTicks) {
            return;
        }

        launched = true;
        goat.rotationYaw = goat.renderYawOffset;
        goat.motionX = launchVelocity.x;
        goat.motionY = launchVelocity.y;
        goat.motionZ = launchVelocity.z;
        goat.setLongJumping(true);
        goat.setJumping(true);
        goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                goat.getLongJumpSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    @Override
    public void resetTask() {
        if (launched && goat.onGround) {
            goat.motionX *= 0.1D;
            goat.motionZ *= 0.1D;
            goat.world.playSound(null, goat.posX, goat.posY, goat.posZ,
                    FFDSounds.GOAT_STEP, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        }
        goat.setLongJumping(false);
        goat.setJumping(false);
        goat.setLongJumpCooldown(launched ? goat.nextLongJumpCooldown()
                : Math.max(1, goat.nextLongJumpCooldown() / 2));
        launchVelocity = null;
        landingPos = null;
        initialPosition = null;
        launched = false;
        prepareTicks = 0;
        airborneTicks = 0;
    }

    @Nullable
    private JumpPlan findJumpPlan() {
        BlockPos origin = goat.getPosition();
        int horizontal = FFDConfig.goatLongJumpHorizontalRange;
        int vertical = FFDConfig.goatLongJumpVerticalRange;
        CandidateSet candidates = candidateSet(horizontal, vertical);
        int tries = Math.min(MAX_CANDIDATE_TRIES, candidates.size());
        int[] removed = new int[tries];
        int removedCount = 0;
        int remainingWeight = candidates.totalWeight;
        for (int attempt = 0; attempt < tries && remainingWeight > 0; attempt++) {
            int candidateIndex = chooseCandidate(candidates, removed, removedCount,
                    remainingWeight);
            if (candidateIndex < 0) {
                break;
            }
            removed[removedCount++] = candidateIndex;
            remainingWeight -= candidates.weights[candidateIndex];
            BlockPos candidate = origin.add(candidates.offsetX[candidateIndex],
                    candidates.offsetY[candidateIndex], candidates.offsetZ[candidateIndex]);
            if (!isAcceptableLanding(candidate)) {
                continue;
            }
            Vec3d target = new Vec3d(candidate).addVector(0.5D, 0.0D, 0.5D);
            Vec3d velocity = calculateJumpVelocity(target);
            if (velocity != null && !isReachableByWalking(candidate)) {
                return new JumpPlan(candidate, velocity);
            }
        }
        return null;
    }

    private int chooseCandidate(CandidateSet candidates, int[] removed, int removedCount,
                                int remainingWeight) {
        int choice = goat.getRNG().nextInt(remainingWeight);
        for (int index = 0; index < candidates.size(); index++) {
            if (contains(removed, removedCount, index)) {
                continue;
            }
            choice -= candidates.weights[index];
            if (choice < 0) return index;
        }
        return -1;
    }

    private static boolean contains(int[] values, int length, int value) {
        for (int index = 0; index < length; index++) {
            if (values[index] == value) return true;
        }
        return false;
    }

    private static CandidateSet candidateSet(int horizontal, int vertical) {
        CandidateSet candidates = cachedCandidates;
        if (candidates != null && candidates.horizontal == horizontal
                && candidates.vertical == vertical) {
            return candidates;
        }
        synchronized (EntityAIGoatLongJump.class) {
            candidates = cachedCandidates;
            if (candidates == null || candidates.horizontal != horizontal
                    || candidates.vertical != vertical) {
                candidates = new CandidateSet(horizontal, vertical);
                cachedCandidates = candidates;
            }
            return candidates;
        }
    }

    private boolean isAcceptableLanding(BlockPos pos) {
        if (!goat.world.isBlockLoaded(pos)
                || pos.getY() <= FFDHeightHooks.minY(goat.world)
                || pos.getY() >= FFDHeightHooks.maxYExclusive(goat.world) - 1) {
            return false;
        }
        IBlockState below = goat.world.getBlockState(pos.down());
        if (!below.isSideSolid(goat.world, pos.down(), EnumFacing.UP)) {
            return false;
        }

        AxisAlignedBB landingBox = boundingBoxAt(pos.getX() + 0.5D, pos.getY(),
                pos.getZ() + 0.5D);
        if (!goat.world.getCollisionBoxes(goat, landingBox).isEmpty()) {
            return false;
        }

        return true;
    }

    private boolean isReachableByWalking(BlockPos pos) {
        net.minecraft.pathfinding.Path path = goat.getNavigator().getPathToPos(pos);
        if (path == null || path.getFinalPathPoint() == null) {
            return false;
        }
        net.minecraft.pathfinding.PathPoint endpoint = path.getFinalPathPoint();
        double dx = endpoint.x - pos.getX();
        double dy = endpoint.y - pos.getY();
        double dz = endpoint.z - pos.getZ();
        return dx * dx + dy * dy + dz * dz <= 2.25D;
    }

    @Nullable
    private Vec3d calculateJumpVelocity(Vec3d requestedTarget) {
        Vec3d origin = goat.getPositionVector();
        Vec3d horizontalDirection = new Vec3d(requestedTarget.x - origin.x, 0.0D,
                requestedTarget.z - origin.z).normalize().scale(0.5D);
        Vec3d target = requestedTarget.subtract(horizontalDirection);
        Vec3d displacement = target.subtract(origin);
        double horizontalDistanceSquared = displacement.x * displacement.x
                + displacement.z * displacement.z;
        double horizontalDistance = Math.sqrt(horizontalDistanceSquared);
        if (horizontalDistance < 1.0E-5D) {
            return null;
        }

        List<Integer> angles = new ArrayList<>(ALLOWED_ANGLES);
        Collections.shuffle(angles, goat.getRNG());
        double maximumVelocity = BASE_JUMP_STRENGTH * FFDConfig.goatLongJumpVelocityMultiplier;
        for (int angle : angles) {
            double radians = angle * Math.PI / 180.0D;
            double denominator = horizontalDistance * Math.sin(2.0D * radians)
                    - 2.0D * displacement.y * Math.pow(Math.cos(radians), 2.0D);
            if (denominator <= 0.0D) {
                continue;
            }
            double velocitySquared = horizontalDistanceSquared * GRAVITY / denominator;
            if (velocitySquared < 0.0D) {
                continue;
            }
            double velocity = Math.sqrt(velocitySquared);
            if (velocity > maximumVelocity) {
                continue;
            }
            double horizontalVelocity = velocity * Math.cos(radians);
            double yaw = Math.atan2(displacement.z, displacement.x);
            Vec3d result = new Vec3d(horizontalVelocity * Math.cos(yaw),
                    velocity * Math.sin(radians), horizontalVelocity * Math.sin(yaw)).scale(0.95D);
            if (isTrajectoryClear(origin, result, horizontalDistance, horizontalVelocity,
                    radians, velocitySquared, yaw)) {
                return result;
            }
        }
        return null;
    }

    private boolean isTrajectoryClear(Vec3d origin, Vec3d velocity, double horizontalDistance,
                                      double horizontalVelocity, double angle,
                                      double velocitySquared, double yaw) {
        if (horizontalVelocity <= 1.0E-5D) {
            return false;
        }
        int samples = Math.max(2, MathHelper.ceil(horizontalDistance / horizontalVelocity) * 2);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        for (int index = 1; index < samples; index++) {
            double radius = horizontalDistance * index / samples;
            double y = sin / cos * radius
                    - radius * radius * GRAVITY / (2.0D * velocitySquared * cos * cos);
            double x = origin.x + radius * cosYaw;
            double z = origin.z + radius * sinYaw;
            if (!goat.world.getCollisionBoxes(goat, boundingBoxAt(x, origin.y + y, z)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private AxisAlignedBB boundingBoxAt(double x, double y, double z) {
        double width = (goat.isChild() ? 0.45D : 0.9D) * 0.7D;
        double height = (goat.isChild() ? 0.65D : 1.3D) * 0.7D;
        return new AxisAlignedBB(x - width / 2.0D, y, z - width / 2.0D,
                x + width / 2.0D, y + height, z + width / 2.0D);
    }

    private static final class CandidateSet {
        final int horizontal;
        final int vertical;
        final int[] offsetX;
        final int[] offsetY;
        final int[] offsetZ;
        final int[] weights;
        final int totalWeight;

        CandidateSet(int horizontal, int vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
            int count = (horizontal * 2 + 1) * (horizontal * 2 + 1)
                    * (vertical * 2 + 1) - (vertical * 2 + 1);
            offsetX = new int[count];
            offsetY = new int[count];
            offsetZ = new int[count];
            weights = new int[count];
            int index = 0;
            int total = 0;
            for (int x = -horizontal; x <= horizontal; x++) {
                for (int y = -vertical; y <= vertical; y++) {
                    for (int z = -horizontal; z <= horizontal; z++) {
                        if (x == 0 && z == 0) continue;
                        int weight = Math.max(1, MathHelper.ceil(x * x + y * y + z * z));
                        offsetX[index] = x;
                        offsetY[index] = y;
                        offsetZ[index] = z;
                        weights[index] = weight;
                        total += weight;
                        index++;
                    }
                }
            }
            totalWeight = total;
        }

        int size() {
            return weights.length;
        }
    }

    private static final class JumpPlan {
        private final BlockPos landingPos;
        private final Vec3d velocity;

        private JumpPlan(BlockPos landingPos, Vec3d velocity) {
            this.landingPos = landingPos;
            this.velocity = velocity;
        }
    }
}
