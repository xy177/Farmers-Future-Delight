package xy177.farmersfuturedelight.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityTrident extends EntityArrow implements IThrowableEntity {
    private static final DataParameter<Byte> LOYALTY = EntityDataManager.createKey(
            EntityTrident.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> RETURNING = EntityDataManager.createKey(
            EntityTrident.class, DataSerializers.BOOLEAN);
    private ItemStack tridentStack = new ItemStack(FFDItems.TRIDENT);
    private boolean dealtDamage;
    private int returnTicks;
    private int ownerEntityId = -1;
    private UUID ownerUuid;
    private boolean suppressGroundDespawn;

    public EntityTrident(World world) {
        super(world);
        setDamage(8.0D);
    }

    public EntityTrident(World world, EntityLivingBase shooter, ItemStack stack) {
        super(world, shooter);
        tridentStack = stack.copy();
        tridentStack.setCount(1);
        dataManager.set(LOYALTY, loyaltyByte(stack));
        setThrower(shooter);
        setDamage(8.0D);
    }

    public EntityTrident(World world, double x, double y, double z) {
        super(world, x, y, z);
        setDamage(8.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(LOYALTY, (byte) 0);
        dataManager.register(RETURNING, false);
    }

    public ItemStack getTridentStack() {
        return tridentStack.copy();
    }

    @Override
    public void onUpdate() {
        int loyalty = getLoyalty();
        if (timeInGround > 4) {
            dealtDamage = true;
        }

        Entity owner = getThrower();
        boolean readyToReturn = dealtDamage || isReturning();
        if (loyalty > 0 && readyToReturn && owner != null && !isValidOwner(owner)) {
            if (!world.isRemote && pickupStatus == PickupStatus.ALLOWED) {
                entityDropItem(getArrowStack(), 0.1F);
            }
            setDead();
            return;
        }

        boolean returning = loyalty > 0 && readyToReturn && owner != null;
        if (returning) {
            double pickupDistance = owner.width + 1.0D;
            if (!(owner instanceof EntityPlayer)
                    && distanceSqToOwnerEye(owner) < pickupDistance * pickupDistance) {
                setDead();
                return;
            }
            dataManager.set(RETURNING, true);
        }

        boolean returnInProgress = loyalty > 0 && (returning || isReturning());
        noClip = returnInProgress;
        setNoGravity(returnInProgress);
        if (returnInProgress) {
            inGround = false;
        }
        if (returning) {
            Vec3d direction = new Vec3d(owner.posX - posX,
                    owner.posY + owner.getEyeHeight() - posY, owner.posZ - posZ);
            posY += direction.y * 0.015D * loyalty;
            direction = direction.normalize();
            double acceleration = 0.05D * loyalty;
            motionX += direction.x * acceleration - motionX * 0.05D;
            motionY += direction.y * acceleration - motionY * 0.05D;
            motionZ += direction.z * acceleration - motionZ * 0.05D;
            if (returnTicks == 0) {
                playSound(FFDSounds.TRIDENT_RETURN, 10.0F, 1.0F);
            }
            ++returnTicks;
        }

        float previousYaw = rotationYaw;
        float previousPitch = rotationPitch;
        Vec3d returnMovement = returnInProgress ? new Vec3d(motionX, motionY, motionZ) : null;
        suppressGroundDespawn = loyalty > 0
                && pickupStatus == PickupStatus.ALLOWED && inGround;
        try {
            super.onUpdate();
        } finally {
            suppressGroundDespawn = false;
        }
        boolean advancedFromEmbeddedBlock = !isDead && returnInProgress && inGround;
        if (advancedFromEmbeddedBlock) {
            advanceReturnThroughEmbeddedBlock();
        }
        if (!advancedFromEmbeddedBlock && !isDead && isInWater() && !inGround) {
            double correction = 0.99D / 0.6D;
            motionX *= correction;
            motionY *= correction;
            motionZ *= correction;
        }
        if (!isDead && returnMovement != null) {
            updateReturnRotation(previousYaw, previousPitch, returnMovement);
        }
    }

    @Override
    public void setDead() {
        if (suppressGroundDespawn && inGround && getLoyalty() > 0
                && pickupStatus == PickupStatus.ALLOWED) {
            return;
        }
        super.setDead();
    }

    private void advanceReturnThroughEmbeddedBlock() {
        inGround = false;
        timeInGround = 0;
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        setPosition(posX, posY, posZ);

        motionX *= 0.99D;
        motionY *= 0.99D;
        motionZ *= 0.99D;
    }

    private void updateReturnRotation(float previousYaw, float previousPitch, Vec3d movement) {
        prevRotationYaw = previousYaw;
        prevRotationPitch = previousPitch;
        float horizontalSpeed = MathHelper.sqrt(movement.x * movement.x + movement.z * movement.z);
        rotationYaw = (float) (MathHelper.atan2(-movement.x, -movement.z) * (180.0D / Math.PI));
        rotationPitch = (float) (MathHelper.atan2(movement.y, horizontalSpeed)
                * (180.0D / Math.PI));
        while (rotationPitch - prevRotationPitch < -180.0F) {
            prevRotationPitch -= 360.0F;
        }
        while (rotationPitch - prevRotationPitch >= 180.0F) {
            prevRotationPitch += 360.0F;
        }
        while (rotationYaw - prevRotationYaw < -180.0F) {
            prevRotationYaw -= 360.0F;
        }
        while (rotationYaw - prevRotationYaw >= 180.0F) {
            prevRotationYaw += 360.0F;
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
    }

    @Override
    @Nullable
    protected Entity findEntityOnPath(Vec3d start, Vec3d end) {
        return dealtDamage ? null : super.findEntityOnPath(start, end);
    }

    @Override
    protected void onHit(RayTraceResult result) {
        Entity target = result.entityHit;
        if (target == null) {
            if (noClip) {
                return;
            }
            boolean silent = isSilent();
            setSilent(true);
            super.onHit(result);
            setSilent(silent);
            playSound(FFDSounds.TRIDENT_HIT_GROUND, 1.0F, 1.0F);
            return;
        }
        if (dealtDamage) {
            return;
        }
        Entity owner = getThrower();
        float damage = 8.0F;
        if (target instanceof EntityLivingBase) {
            damage += FFDEnchantments.getImpalingDamage(
                    tridentStack, (EntityLivingBase) target);
        }
        DamageSource source = new EntityDamageSourceIndirect(
                "trident", this, owner == null ? this : owner).setProjectile();
        dealtDamage = true;
        boolean damaged = target.attackEntityFrom(source, damage);
        if (damaged && owner instanceof EntityPlayerMP) {
            FFDAdvancements.THROW_TRIDENT.trigger((EntityPlayerMP) owner);
        }
        if (damaged && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) target;
            if (owner instanceof EntityLivingBase) {
                EnchantmentHelper.applyThornEnchantments(living, owner);
                EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) owner, living);
            }
            arrowHit(living);
        }
        motionX *= -0.01D;
        motionY *= -0.1D;
        motionZ *= -0.01D;
        float volume = 1.0F;
        if (!world.isRemote && world.isThundering()
                && FFDEnchantments.hasChanneling(tridentStack)) {
            BlockPos pos = target.getPosition();
            if (world.canSeeSky(pos)) {
                world.addWeatherEffect(new EntityLightningBolt(world,
                        pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, false));
                if (owner instanceof EntityPlayerMP && hasVillagerInLightningRange(pos)) {
                    FFDAdvancements.VERY_VERY_FRIGHTENING.trigger((EntityPlayerMP) owner);
                }
                volume = 5.0F;
            }
        }
        playSound(volume > 1.0F ? FFDSounds.TRIDENT_THUNDER : FFDSounds.TRIDENT_HIT,
                volume, 1.0F);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (world.isRemote || (!inGround && !noClip) || arrowShake > 0) {
            return;
        }
        Entity owner = getThrower();
        if (owner != null && !isOwner(player)) {
            return;
        }
        if (inGround) {
            super.onCollideWithPlayer(player);
            return;
        }
        if (noClip) {
            tryPickup(player);
        }
    }

    @Override
    protected ItemStack getArrowStack() {
        return tridentStack.copy();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("Trident", tridentStack.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("DealtDamage", dealtDamage);
        if (ownerUuid != null) {
            compound.setUniqueId("Owner", ownerUuid);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Trident", 10)) {
            tridentStack = new ItemStack(compound.getCompoundTag("Trident"));
        }
        if (tridentStack.isEmpty()) {
            tridentStack = new ItemStack(FFDItems.TRIDENT);
        }
        dealtDamage = compound.getBoolean("DealtDamage");
        returnTicks = 0;
        ownerUuid = compound.hasUniqueId("Owner") ? compound.getUniqueId("Owner") : null;
        ownerEntityId = -1;
        shootingEntity = null;
        dataManager.set(LOYALTY, loyaltyByte(tridentStack));
        dataManager.set(RETURNING, false);
    }

    private int getLoyalty() {
        return dataManager.get(LOYALTY) & 255;
    }

    private boolean isReturning() {
        return dataManager.get(RETURNING);
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    @Nullable
    public Entity getThrower() {
        if (shootingEntity != null && shootingEntity.world == world
                && world.getEntityByID(shootingEntity.getEntityId()) == shootingEntity) {
            return shootingEntity;
        }
        shootingEntity = null;
        Entity owner = ownerEntityId < 0 ? null : world.getEntityByID(ownerEntityId);
        if (owner != null && ownerUuid != null
                && !ownerUuid.equals(owner.getUniqueID())) {
            owner = null;
        }
        if (owner == null && ownerUuid != null && world instanceof WorldServer) {
            owner = ((WorldServer) world).getEntityFromUuid(ownerUuid);
        }
        if (owner != null) {
            shootingEntity = owner;
            ownerEntityId = owner.getEntityId();
        }
        return owner;
    }

    @Override
    public void setThrower(@Nullable Entity entity) {
        shootingEntity = entity;
        ownerEntityId = entity == null ? -1 : entity.getEntityId();
        ownerUuid = entity == null ? null : entity.getUniqueID();
    }

    private boolean isValidOwner(@Nullable Entity owner) {
        return owner != null && owner.isEntityAlive()
                && (!(owner instanceof EntityPlayerMP)
                || !((EntityPlayerMP) owner).isSpectator());
    }

    private boolean isOwner(Entity entity) {
        return ownerUuid != null ? ownerUuid.equals(entity.getUniqueID())
                : entity == shootingEntity;
    }

    private double distanceSqToOwnerEye(Entity owner) {
        double x = owner.posX - posX;
        double y = owner.posY + owner.getEyeHeight() - posY;
        double z = owner.posZ - posZ;
        return x * x + y * y + z * z;
    }

    private static byte loyaltyByte(ItemStack stack) {
        return (byte) net.minecraft.util.math.MathHelper.clamp(
                FFDEnchantments.getLoyalty(stack), 0, 127);
    }

    private void tryPickup(EntityPlayer player) {
        if (world.isRemote || isDead) {
            return;
        }
        boolean pickedUp = pickupStatus == PickupStatus.CREATIVE_ONLY
                && player.capabilities.isCreativeMode;
        if (pickupStatus == PickupStatus.ALLOWED) {
            pickedUp = player.inventory.addItemStackToInventory(getArrowStack());
        }
        if (pickedUp) {
            player.onItemPickup(this, 1);
            setDead();
        }
    }

    private boolean hasVillagerInLightningRange(BlockPos pos) {
        return !world.getEntitiesWithinAABB(EntityVillager.class,
                new net.minecraft.util.math.AxisAlignedBB(
                        pos.getX() - 3.0D, pos.getY() - 3.0D, pos.getZ() - 3.0D,
                        pos.getX() + 4.0D, pos.getY() + 10.0D, pos.getZ() + 4.0D)).isEmpty();
    }
}
