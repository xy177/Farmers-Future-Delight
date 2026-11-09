package xy177.farmersfuturedelight.common.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import io.netty.buffer.ByteBuf;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import xy177.farmersfuturedelight.common.registry.FFDSounds;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;

public class EntityCrossbowArrow extends EntityTippedArrow implements IEntityAdditionalSpawnData {
    private final Set<Integer> piercedEntityIds = new HashSet<>();
    private final Set<UUID> killedEntityIds = new HashSet<>();
    private final Set<ResourceLocation> killedEntityTypes = new HashSet<>();
    private int killedPhantoms;
    private ItemStack projectileStack = new ItemStack(Items.ARROW);
    private int piercingLevel;
    private boolean spectral;

    public EntityCrossbowArrow(World world) {
        super(world);
    }

    public EntityCrossbowArrow(World world, EntityLivingBase shooter, ItemStack projectile,
                               int piercingLevel) {
        super(world, shooter);
        this.projectileStack = projectile.copy();
        this.projectileStack.setCount(1);
        this.piercingLevel = Math.max(0, Math.min(4, piercingLevel));
        this.spectral = projectile.getItem() == Items.SPECTRAL_ARROW;
        if (projectile.getItem() == Items.TIPPED_ARROW) {
            setPotionEffect(projectile);
        }
    }

    @Override
    protected ItemStack getArrowStack() {
        return projectileStack.copy();
    }

    public boolean isSpectralArrow() {
        return spectral;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, projectileStack);
        buffer.writeInt(piercingLevel);
        buffer.writeBoolean(spectral);
        buffer.writeInt(shootingEntity == null ? -1 : shootingEntity.getEntityId());
        buffer.writeDouble(motionX);
        buffer.writeDouble(motionY);
        buffer.writeDouble(motionZ);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        projectileStack = ByteBufUtils.readItemStack(buffer);
        piercingLevel = Math.max(0, Math.min(4, buffer.readInt()));
        spectral = buffer.readBoolean();
        int shooterId = buffer.readInt();
        shootingEntity = shooterId < 0 ? null : world.getEntityByID(shooterId);
        if (projectileStack.getItem() == Items.TIPPED_ARROW) {
            setPotionEffect(projectileStack);
        }
        setVelocity(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        super.playSound(sound == SoundEvents.ENTITY_ARROW_HIT
                ? FFDSounds.CROSSBOW_HIT : sound, volume, pitch);
    }

    @Override
    protected void arrowHit(EntityLivingBase target) {
        super.arrowHit(target);
        if (spectral) {
            target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0));
        }
    }

    @Override
    protected Entity findEntityOnPath(Vec3d start, Vec3d end) {
        AxisAlignedBB bounds = getEntityBoundingBox().expand(motionX, motionY, motionZ)
                .grow(1.0D);
        Predicate<Entity> targets = entity -> EntitySelectors.NOT_SPECTATING.apply(entity)
                && entity.isEntityAlive() && entity.canBeCollidedWith();
        List<Entity> entities = world.getEntitiesInAABBexcluding(this, bounds, targets);
        Entity nearest = null;
        double nearestDistance = 0.0D;
        for (Entity entity : entities) {
            if ((entity == shootingEntity && ticksExisted < 5)
                    || piercedEntityIds.contains(entity.getEntityId())) {
                continue;
            }
            RayTraceResult intercept = entity.getEntityBoundingBox()
                    .grow(0.30000001192092896D).calculateIntercept(start, end);
            if (intercept == null) {
                continue;
            }
            double distance = start.squareDistanceTo(intercept.hitVec);
            if (nearest == null || distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (result.entityHit == null) {
            piercedEntityIds.clear();
            super.onHit(result);
            return;
        }
        if (piercingLevel <= 0) {
            boolean wasAlive = result.entityHit.isEntityAlive();
            super.onHit(result);
            recordKill(result.entityHit, wasAlive);
            return;
        }
        if (piercedEntityIds.size() >= piercingLevel + 1) {
            setDead();
            return;
        }
        Entity target = result.entityHit;
        boolean wasAlive = target.isEntityAlive();
        piercedEntityIds.add(target.getEntityId());
        super.onHit(result);
        recordKill(target, wasAlive);
        if (isDead) {
            isDead = false;
        }
    }

    private void recordKill(Entity target, boolean wasAlive) {
        if (world.isRemote || !(shootingEntity instanceof EntityPlayerMP) || !wasAlive
                || target.isEntityAlive() || !killedEntityIds.add(target.getUniqueID())) {
            return;
        }
        ResourceLocation type = EntityList.getKey(target);
        if (type != null) {
            killedEntityTypes.add(type);
        }
        if (target instanceof EntityPhantom) {
            killedPhantoms++;
        }
        EntityPlayerMP player = (EntityPlayerMP) shootingEntity;
        if (killedPhantoms >= 2) {
            FFDAdvancements.TWO_BIRDS_ONE_ARROW.trigger(player);
        }
        if (killedEntityTypes.size() >= 5) {
            FFDAdvancements.ARBALISTIC.trigger(player);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("CrossbowProjectile", projectileStack.writeToNBT(new NBTTagCompound()));
        compound.setInteger("CrossbowPiercing", piercingLevel);
        compound.setBoolean("CrossbowSpectral", spectral);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("CrossbowProjectile", 10)) {
            projectileStack = new ItemStack(compound.getCompoundTag("CrossbowProjectile"));
        }
        piercingLevel = Math.max(0, Math.min(4, compound.getInteger("CrossbowPiercing")));
        spectral = compound.getBoolean("CrossbowSpectral");
        piercedEntityIds.clear();
        killedEntityIds.clear();
        killedEntityTypes.clear();
        killedPhantoms = 0;
    }
}
