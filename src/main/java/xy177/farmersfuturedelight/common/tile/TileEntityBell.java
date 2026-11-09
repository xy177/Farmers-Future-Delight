package xy177.farmersfuturedelight.common.tile;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.raid.FFDRaidCaptains;
import xy177.farmersfuturedelight.common.raid.FFDRaidVillagers;
import xy177.farmersfuturedelight.common.registry.FFDRaidContent;
import xy177.farmersfuturedelight.common.FFDConfig;

public class TileEntityBell extends TileEntity implements ITickable {
    private int ticks;
    private boolean shaking;
    private boolean powered;
    private EnumFacing direction = EnumFacing.NORTH;
    private long lastRingTimestamp;
    private List<EntityLivingBase> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void ring(EnumFacing hitDirection) {
        direction = hitDirection;
        ticks = 0;
        shaking = true;
        world.addBlockEvent(pos, world.getBlockState(pos).getBlock(), 1,
                hitDirection.getIndex());
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean value) {
        powered = value;
        markDirty();
    }

    @Override
    public void onLoad() {
        if (!world.isRemote && getBlockType()
                instanceof xy177.farmersfuturedelight.common.block.BlockBell) {
            ((xy177.farmersfuturedelight.common.block.BlockBell) getBlockType()).updatePower(
                    world, pos, world.getBlockState(pos));
        }
    }

    @Override
    public net.minecraft.nbt.NBTTagCompound writeToNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readFromNBT(tag);
        powered = tag.getBoolean("Powered");
    }

    @Override
    public boolean receiveClientEvent(int eventId, int parameter) {
        if (eventId != 1) {
            return super.receiveClientEvent(eventId, parameter);
        }
        direction = EnumFacing.getFront(parameter);
        updateEntities();
        resonationTicks = 0;
        ticks = 0;
        shaking = true;
        return true;
    }

    @Override
    public void update() {
        if (shaking && ++ticks >= 50) {
            ticks = 0;
            shaking = false;
        }
        if (ticks >= 5 && resonationTicks == 0 && hasNearbyRaider()) {
            resonating = true;
            world.playSound(null, pos, FFDRaidContent.BELL_RESONATE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        if (resonating) {
            if (resonationTicks < 40) resonationTicks++;
            else {
                finishResonating();
                resonating = false;
            }
        }
    }

    private void updateEntities() {
        long now = world.getTotalWorldTime();
        if (nearbyEntities == null || now > lastRingTimestamp + 60L) {
            lastRingTimestamp = now;
            nearbyEntities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(pos).grow(Math.max(48.0D, FFDConfig.bellRaidHighlightRange)));
        }
        if (!world.isRemote) {
            for (EntityLivingBase entity : nearbyEntities) {
                if (entity instanceof EntityVillager && withinRange(entity, 32.0D)) {
                    FFDRaidVillagers.heardBell((EntityVillager) entity, now);
                }
            }
        }
    }

    private boolean withinRange(EntityLivingBase entity, double range) {
        return entity.isEntityAlive() && !entity.isDead && entity.getDistanceSqToCenter(pos) < range * range;
    }

    private boolean hasNearbyRaider() {
        if (nearbyEntities == null) return false;
        for (EntityLivingBase entity : nearbyEntities) {
            if (withinRange(entity, 32.0D) && FFDRaidCaptains.isRaider(entity)) return true;
        }
        return false;
    }

    private void finishResonating() {
        if (nearbyEntities == null) return;
        int nearbyCount = 0;
        for (EntityLivingBase entity : nearbyEntities) {
            if (entity.getDistanceSqToCenter(pos)
                    < FFDConfig.bellRaidHighlightRange * FFDConfig.bellRaidHighlightRange) nearbyCount++;
        }
        int color = 16700985;
        int particles = Math.max(3, Math.min(15, (nearbyCount - 21) / -2));
        for (EntityLivingBase entity : nearbyEntities) {
            if (!withinRange(entity, FFDConfig.bellRaidHighlightRange)
                    || !FFDRaidCaptains.isRaider(entity)) continue;
            if (!world.isRemote) {
                entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 60));
                continue;
            }
            double deltaX = entity.posX - pos.getX();
            double deltaZ = entity.posZ - pos.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (distance < 1.0E-7D) continue;
            for (int index = 0; index < particles; index++) {
                color += 5;
                world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                        pos.getX() + 0.5D + deltaX / distance, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D + deltaZ / distance,
                        ((color >> 16) & 255) / 255.0D,
                        ((color >> 8) & 255) / 255.0D, (color & 255) / 255.0D);
            }
        }
    }

    public float getTicks(float partialTicks) {
        return ticks + partialTicks;
    }

    public EnumFacing getDirection() {
        return shaking ? direction : null;
    }
}
