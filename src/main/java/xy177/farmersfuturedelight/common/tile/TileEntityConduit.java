package xy177.farmersfuturedelight.common.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDPotions;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityConduit extends TileEntity implements ITickable {
    private final List<BlockPos> framePositions = new ArrayList<>();
    private int tickCount;
    private float activeRotation;
    private boolean active;
    private boolean eyeOpen;
    @Nullable
    private EntityLivingBase target;
    @Nullable
    private UUID targetUuid;
    private long nextShortSound;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos,
                                 IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update() {
        if (world == null) {
            return;
        }
        tickCount++;
        long time = world.getTotalWorldTime();
        if (time % 40L == 0L) {
            setActive(scanFrame());
            if (!world.isRemote && active) {
                applyConduitPower();
                updateTarget();
            }
        }
        if (!world.isRemote && active && time % 80L == 0L) {
            playSound(FFDSounds.CONDUIT_AMBIENT);
        }
        if (!world.isRemote && active && time > nextShortSound) {
            nextShortSound = time + 60L + world.rand.nextInt(40);
            playSound(FFDSounds.CONDUIT_AMBIENT_SHORT);
        }
        if (world.isRemote) {
            resolveClientTarget();
            spawnParticles();
            if (active) {
                activeRotation++;
            }
        }
    }

    private boolean scanFrame() {
        framePositions.clear();
        if (!world.isAreaLoaded(pos, 2, false)) {
            eyeOpen = false;
            return false;
        }
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!WaterloggedBlockApi.containsWater(world, pos.add(x, y, z))) {
                        eyeOpen = false;
                        return false;
                    }
                }
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    int absX = Math.abs(x);
                    int absY = Math.abs(y);
                    int absZ = Math.abs(z);
                    if ((absX > 1 || absY > 1 || absZ > 1)
                            && (x == 0 && (absY == 2 || absZ == 2)
                            || y == 0 && (absX == 2 || absZ == 2)
                            || z == 0 && (absX == 2 || absY == 2))) {
                        BlockPos framePos = pos.add(x, y, z);
                        if (isFrameBlock(world.getBlockState(framePos))) {
                            framePositions.add(framePos);
                        }
                    }
                }
            }
        }
        eyeOpen = framePositions.size() >= 42;
        return framePositions.size() >= 16;
    }

    private static boolean isFrameBlock(IBlockState state) {
        return FFDConfig.isConduitFrameBlock(state);
    }

    private void applyConduitPower() {
        int radius = getEffectRadius();
        double radiusSq = radius * radius;
        AxisAlignedBB area = new AxisAlignedBB(pos).grow(radius);
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, area)) {
            if (pos.distanceSq(player.getPosition()) < radiusSq && player.isWet()) {
                player.addPotionEffect(new PotionEffect(
                        FFDPotions.CONDUIT_POWER, 260, 0, true, true));
            }
        }
    }

    private void updateTarget() {
        EntityLivingBase previous = target;
        if (!eyeOpen) {
            target = null;
        } else if (target == null && targetUuid != null) {
            target = findTarget(targetUuid);
            targetUuid = null;
        } else if (target == null) {
            List<EntityLivingBase> candidates = world.getEntitiesWithinAABB(
                    EntityLivingBase.class, getAttackBox(), entity -> entity instanceof IMob
                            && entity.isWet() && entity.isEntityAlive());
            if (!candidates.isEmpty()) {
                target = candidates.get(world.rand.nextInt(candidates.size()));
            }
        } else if (!target.isEntityAlive()
                || pos.distanceSq(target.getPosition()) >= 64.0D) {
            target = null;
        }
        if (target != null) {
            world.playSound(null, target.posX, target.posY, target.posZ,
                    FFDSounds.CONDUIT_ATTACK_TARGET, SoundCategory.BLOCKS, 1.0F, 1.0F);
            target.attackEntityFrom(DamageSource.MAGIC, 4.0F);
        }
        if (previous != target) {
            targetUuid = target == null ? null : target.getUniqueID();
            markDirty();
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 2);
        }
    }

    private void resolveClientTarget() {
        if (targetUuid == null) {
            target = null;
        } else if (target == null || !target.getUniqueID().equals(targetUuid)) {
            target = findTarget(targetUuid);
            if (target == null) {
                targetUuid = null;
            }
        }
    }

    @Nullable
    private EntityLivingBase findTarget(UUID uuid) {
        List<EntityLivingBase> matches = world.getEntitiesWithinAABB(
                EntityLivingBase.class, getAttackBox(), entity -> entity.getUniqueID().equals(uuid));
        return matches.size() == 1 ? matches.get(0) : null;
    }

    private AxisAlignedBB getAttackBox() {
        return new AxisAlignedBB(pos).grow(8.0D);
    }

    private void spawnParticles() {
        Random random = world.rand;
        float bob = MathHelper.sin((tickCount + 35) * 0.1F) / 2.0F + 0.5F;
        bob = (bob * bob + bob) * 0.3F;
        double sourceX = pos.getX() + 0.5D;
        double sourceY = pos.getY() + 1.5D + bob;
        double sourceZ = pos.getZ() + 0.5D;
        for (BlockPos framePos : framePositions) {
            if (random.nextInt(50) == 0) {
                double motionX = framePos.getX() - pos.getX() - 0.5D + random.nextFloat();
                double motionY = framePos.getY() - pos.getY() - 2.0D + random.nextFloat();
                double motionZ = framePos.getZ() - pos.getZ() - 0.5D + random.nextFloat();
                FarmerFutureDelight.proxy.spawnConduitParticle(world, sourceX, sourceY, sourceZ,
                        motionX, motionY, motionZ);
            }
        }
        if (target != null) {
            double x = target.posX + (random.nextFloat() - 0.5F) * (3.0F + target.width);
            double y = target.posY + target.getEyeHeight()
                    + (random.nextFloat() - 1.0F) * target.height;
            double z = target.posZ + (random.nextFloat() - 0.5F) * (3.0F + target.width);
            FarmerFutureDelight.proxy.spawnConduitParticle(world, x, y, z,
                    random.nextFloat() - 0.5F, random.nextFloat() - 0.5F,
                    random.nextFloat() - 0.5F);
        }
    }

    private void setActive(boolean active) {
        if (this.active != active && !world.isRemote) {
            playSound(active ? FFDSounds.CONDUIT_ACTIVATE : FFDSounds.CONDUIT_DEACTIVATE);
        }
        this.active = active;
    }

    private void playSound(net.minecraft.util.SoundEvent sound) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEyeOpen() {
        return eyeOpen;
    }

    public int getFrameCount() {
        return framePositions.size();
    }

    public int getEffectRadius() {
        return framePositions.size() / 7 * 16;
    }

    public List<BlockPos> getFramePositions() {
        return Collections.unmodifiableList(framePositions);
    }

    @Nullable
    public EntityLivingBase getTarget() {
        return target;
    }

    public int getTickCount() {
        return tickCount;
    }

    public float getActiveRotation(float partialTicks) {
        return (activeRotation + partialTicks) * -0.0375F;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        targetUuid = compound.hasUniqueId("TargetUuid")
                ? compound.getUniqueId("TargetUuid") : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        UUID uuid = target == null ? targetUuid : target.getUniqueID();
        if (uuid != null) {
            compound.setUniqueId("TargetUuid", uuid);
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 5, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
}
