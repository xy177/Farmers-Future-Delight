package xy177.farmersfuturedelight.common.entity;

import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityPufferfish extends EntityAbstractFish {
    private static final DataParameter<Integer> PUFF_STATE = EntityDataManager.createKey(
            EntityPufferfish.class, DataSerializers.VARINT);
    private int inflateTicks;
    private int deflateTicks;
    private float baseWidth = -1.0F;
    private float baseHeight;

    public EntityPufferfish(World world) {
        super(world);
        setSize(0.7F, 0.7F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PUFF_STATE, 0);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(1, new PuffGoal(this));
    }

    public int getPuffState() {
        return dataManager.get(PUFF_STATE);
    }

    public void setPuffState(int state) {
        int clamped = Math.max(0, Math.min(2, state));
        dataManager.set(PUFF_STATE, clamped);
        updatePuffSize(clamped);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (PUFF_STATE.equals(key)) {
            updatePuffSize(getPuffState());
        }
        super.notifyDataManagerChange(key);
    }

    @Override
    protected final void setSize(float width, float height) {
        boolean initialized = baseWidth > 0.0F;
        baseWidth = width;
        baseHeight = height;
        if (!initialized) {
            updatePuffSize(0);
        }
    }

    private void updatePuffSize(int state) {
        if (baseWidth <= 0.0F) {
            return;
        }
        float scale = state == 0 ? 0.5F : state == 1 ? 0.7F : 1.0F;
        super.setSize(baseWidth * scale, baseHeight * scale);
    }

    @Override
    public void onUpdate() {
        if (isEntityAlive() && !world.isRemote) {
            if (inflateTicks > 0) {
                if (getPuffState() == 0) {
                    playSound(FFDSounds.PUFFERFISH_BLOW_UP, getSoundVolume(), getSoundPitch());
                    setPuffState(1);
                } else if (inflateTicks > 40 && getPuffState() == 1) {
                    playSound(FFDSounds.PUFFERFISH_BLOW_UP, getSoundVolume(), getSoundPitch());
                    setPuffState(2);
                }
                inflateTicks++;
            } else if (getPuffState() != 0) {
                if (deflateTicks > 60 && getPuffState() == 2) {
                    playSound(FFDSounds.PUFFERFISH_BLOW_OUT, getSoundVolume(), getSoundPitch());
                    setPuffState(1);
                } else if (deflateTicks > 100 && getPuffState() == 1) {
                    playSound(FFDSounds.PUFFERFISH_BLOW_OUT, getSoundVolume(), getSoundPitch());
                    setPuffState(0);
                }
                deflateTicks++;
            }
        }
        super.onUpdate();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && isEntityAlive() && getPuffState() > 0) {
            List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class,
                    getEntityBoundingBox().grow(0.3D), EntityPufferfish::isThreat);
            for (EntityLiving entity : entities) {
                if (entity.isEntityAlive()) {
                    sting(entity);
                }
            }
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (!world.isRemote && getPuffState() > 0 && isThreat(player)) {
            sting(player);
        }
    }

    private void sting(EntityLivingBase entity) {
        if (!teamsAllowDamage(entity)) {
            return;
        }
        int state = getPuffState();
        if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0F + state)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 60 * state, 0));
            playSound(FFDSounds.PUFFERFISH_STING, 1.0F, 1.0F);
        }
    }

    private static boolean isThreat(EntityLivingBase entity) {
        if (entity == null || isNotScary(entity)) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    private static boolean isNotScary(EntityLivingBase entity) {
        if (entity instanceof EntityTurtle || entity instanceof EntityGuardian
                || entity instanceof EntityCod || entity instanceof EntityPufferfish
                || entity instanceof EntitySalmon || entity instanceof EntityTropicalFish
                || entity instanceof EntityDolphin || entity instanceof EntitySquid
                || entity instanceof EntityGlowSquid) {
            return true;
        }
        ResourceLocation id = EntityList.getKey(entity);
        if (id == null || !("futuremc".equals(id.getResourceDomain())
                || "oe".equals(id.getResourceDomain()))) {
            return false;
        }
        String path = id.getResourcePath();
        return "cod".equals(path) || "pufferfish".equals(path) || "salmon".equals(path)
                || "tropical_fish".equals(path) || "dolphin".equals(path)
                || "turtle".equals(path) || "glow_squid".equals(path);
    }

    private boolean teamsAllowDamage(EntityLivingBase entity) {
        Team team = getTeam();
        return team == null || !isOnSameTeam(entity) || team.getAllowFriendlyFire();
    }

    @Override
    protected ItemStack getBucketStack() {
        return FFDItems.effectiveStack(FFDItems.PUFFERFISH_BUCKET);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("PuffState", getPuffState());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setPuffState(compound.getInteger("PuffState"));
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        entityDropItem(new ItemStack(Items.FISH, 1, 3), 0.0F);
        if (rand.nextFloat() < 0.05F) {
            entityDropItem(new ItemStack(Items.DYE, 1, 15), 0.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.PUFFERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.PUFFERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.PUFFERFISH_DEATH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return FFDSounds.PUFFERFISH_FLOP;
    }

    private static final class PuffGoal extends EntityAIBase {
        private final EntityPufferfish fish;

        private PuffGoal(EntityPufferfish fish) {
            this.fish = fish;
        }

        @Override
        public boolean shouldExecute() {
            return !fish.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    fish.getEntityBoundingBox().grow(2.0D), EntityPufferfish::isThreat).isEmpty();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void startExecuting() {
            fish.inflateTicks = 1;
            fish.deflateTicks = 0;
        }

        @Override
        public void resetTask() {
            fish.inflateTicks = 0;
        }
    }
}
