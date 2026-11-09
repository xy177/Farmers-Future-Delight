package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTraderLlama extends EntityLlama {
    private int despawnDelay = 47999;

    public EntityTraderLlama(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        targetTasks.addTask(1, new DefendTraderGoal(this));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityZombie.class,
                10, true, false, target -> !(target instanceof EntityPigZombie)));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, AbstractIllager.class, true));
    }

    public int getDespawnDelay() {
        return despawnDelay;
    }

    public void setDespawnDelay(int ticks) {
        despawnDelay = ticks;
    }

    @Override
    protected void mountTo(EntityPlayer player) {
        if (!(getLeashHolder() instanceof EntityWanderingTrader)) super.mountTo(player);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote || isTame() || isNoDespawnRequired()
                || getLeashed() && !(getLeashHolder() instanceof EntityWanderingTrader)
                || getPassengers().size() == 1 && getPassengers().get(0) instanceof EntityPlayer) return;
        Entity holder = getLeashHolder();
        despawnDelay = holder instanceof EntityWanderingTrader
                ? ((EntityWanderingTrader) holder).getDespawnDelay() - 1 : despawnDelay - 1;
        if (despawnDelay <= 0) {
            clearLeashed(false, false);
            setDead();
        }
    }

    @Override
    public EntityLlama createChild(EntityAgeable mate) {
        EntityTraderLlama child = new EntityTraderLlama(world);
        setOffspringAttributes(mate, child);
        EntityLlama partner = (EntityLlama) mate;
        int strength = rand.nextInt(Math.max(getStrength(), partner.getStrength())) + 1;
        if (rand.nextFloat() < 0.03F) strength++;
        NBTTagCompound attributes = new NBTTagCompound();
        child.writeEntityToNBT(attributes);
        attributes.setInteger("Strength", Math.min(5, strength));
        attributes.setInteger("Variant", rand.nextBoolean() ? getVariant() : partner.getVariant());
        child.readEntityFromNBT(attributes);
        child.enablePersistence();
        return child;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("DespawnDelay", despawnDelay);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        despawnDelay = compound.hasKey("DespawnDelay", 99) ? compound.getInteger("DespawnDelay") : 47999;
    }

    private static final class DefendTraderGoal extends EntityAITarget {
        private final EntityTraderLlama llama;
        private EntityLivingBase attacker;
        private int lastRevenge;

        private DefendTraderGoal(EntityTraderLlama llama) {
            super(llama, false);
            this.llama = llama;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            Entity holder = llama.getLeashHolder();
            if (!(holder instanceof EntityWanderingTrader)) return false;
            EntityWanderingTrader trader = (EntityWanderingTrader) holder;
            attacker = trader.getRevengeTarget();
            return trader.getRevengeTimer() != lastRevenge && isSuitableTarget(attacker, false);
        }

        @Override
        public void startExecuting() {
            llama.setAttackTarget(attacker);
            Entity holder = llama.getLeashHolder();
            if (holder instanceof EntityWanderingTrader) lastRevenge = ((EntityWanderingTrader) holder).getRevengeTimer();
            super.startExecuting();
        }
    }
}
