package xy177.farmersfuturedelight.common.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDPowderSnowEvents;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityDrowned extends EntityZombie implements IRangedAttackMob {
    private static final ResourceLocation LOOT = new ResourceLocation(
            FarmerFutureDelight.MODID, "entities/drowned");
    private static final String SUBMERGED_TICKS_TAG =
            FarmerFutureDelight.MODID + ".drownedSubmergedTicks";
    private static final String CONVERSION_TICKS_TAG =
            FarmerFutureDelight.MODID + ".drownedConversionTicks";
    private final PathNavigateSwimmer waterNavigator;
    private final PathNavigateGround groundNavigator;
    private final EntityAIBreakDoor breakDoorTask;
    private boolean breakDoorsEnabled;
    private boolean swimmingUp;
    private float swimAmount;
    private float previousSwimAmount;
    private boolean wasInPowderSnow;

    public EntityDrowned(World world) {
        super(world);
        stepHeight = 1.0F;
        moveHelper = new DrownedMoveHelper(this);
        setPathPriority(PathNodeType.WATER, 0.0F);
        waterNavigator = new PathNavigateSwimmer(this, world);
        groundNavigator = new PathNavigateGround(this, world);
        navigator = groundNavigator;
        breakDoorTask = new DrownedBreakDoor(this);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new GoToWater(this, 1.0D));
        tasks.addTask(2, new TridentAttack(this, 1.0D, 40, 10.0F));
        tasks.addTask(2, new DrownedAttack(this, 1.0D, false));
        tasks.addTask(5, new GoToBeach(this, 1.0D));
        tasks.addTask(6, new SwimUp(this, 1.0D, world.getSeaLevel()));
        tasks.addTask(7, new EntityAIWander(this, 1.0D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityDrowned.class));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this,
                EntityPlayer.class, 10, true, false, new PlayerTarget(this)));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this,
                EntityVillager.class, false));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this,
                EntityIronGolem.class, true));
        targetTasks.addTask(5, new EntityAINearestAttackableTarget<>(this,
                EntityTurtle.class, 10, true, false, new Predicate<EntityTurtle>() {
                    @Override
                    public boolean apply(@Nullable EntityTurtle turtle) {
                        return turtle != null && turtle.isChild() && !turtle.isInWater();
                    }
                }));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(0.23000000417232513D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    @Override
    public void onLivingUpdate() {
        boolean inPowderSnow = FFDItems.isPowderSnowEnabled()
                && FFDPowderSnowEvents.isInsidePowderSnow(this);
        if (!world.isRemote) {
            boolean swimming = isInWater() && wantsToSwim();
            navigator = swimming ? waterNavigator : groundNavigator;
            setFlag(4, swimming);
            updateDaylightBurning(inPowderSnow || wasInPowderSnow);
        }
        super.onLivingUpdate();
        previousSwimAmount = swimAmount;
        if (getFlag(4)) {
            swimAmount = Math.min(1.0F, swimAmount + 0.09F);
        } else {
            swimAmount = Math.max(0.0F, swimAmount - 0.09F);
        }
        wasInPowderSnow = inPowderSnow;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!world.isRemote && isInWater() && wantsToSwim()) {
            moveRelative(strafe, vertical, forward, 0.01F);
            move(MoverType.SELF, motionX, motionY, motionZ);
            motionX *= 0.9D;
            motionY *= 0.9D;
            motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    public boolean isSwimmingInWater() {
        return isInWater() && wantsToSwim();
    }

    public float getSwimAmount(float partialTicks) {
        return previousSwimAmount + (swimAmount - previousSwimAmount) * partialTicks;
    }

    private boolean wantsToSwim() {
        if (swimmingUp) {
            return true;
        }
        EntityLivingBase target = getAttackTarget();
        return target != null && target.isInWater();
    }

    public boolean canAttackTarget(@Nullable EntityLivingBase target) {
        return target != null && (!world.isDaytime() || target.isInWater());
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected boolean shouldBurnInDay() {
        return false;
    }

    private void updateDaylightBurning(boolean powderSnowProtected) {
        if (!isEntityAlive() || !world.isDaytime() || isWet() || powderSnowProtected) {
            return;
        }
        float brightness = getBrightness();
        BlockPos eyePos = new BlockPos(posX, posY + getEyeHeight(), posZ);
        if (brightness <= 0.5F
                || rand.nextFloat() * 30.0F >= (brightness - 0.4F) * 2.0F
                || !world.canSeeSky(eyePos)) {
            return;
        }
        ItemStack head = getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!head.isEmpty()) {
            if (head.isItemStackDamageable()) {
                head.setItemDamage(head.getItemDamage() + rand.nextInt(2));
                if (head.getItemDamage() >= head.getMaxDamage()) {
                    renderBrokenItemStack(head);
                    setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
            return;
        }
        setFire(8);
    }

    @Override
    public boolean isBreakDoorsTaskSet() {
        return breakDoorsEnabled;
    }

    @Override
    public void setBreakDoorsAItask(boolean enabled) {
        if (groundNavigator == null || breakDoorsEnabled == enabled) {
            return;
        }
        breakDoorsEnabled = enabled;
        groundNavigator.setBreakDoors(enabled);
        if (enabled) {
            tasks.addTask(1, breakDoorTask);
        } else {
            tasks.removeTask(breakDoorTask);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(posX, getEntityBoundingBox().minY, posZ);
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL
                || world.getBlockState(pos).getMaterial() != Material.WATER
                || world.getBlockState(pos.down()).getMaterial() != Material.WATER
                || !isValidLightLevel() || !isNotColliding()) {
            return false;
        }
        Biome biome = world.getBiome(pos);
        boolean river = biome == net.minecraft.init.Biomes.RIVER
                || biome == net.minecraft.init.Biomes.FROZEN_RIVER
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
        if (river) {
            return rand.nextInt(15) == 0;
        }
        return getEntityBoundingBox().minY < world.getSeaLevel() - 5
                && rand.nextInt(40) == 0;
    }

    @Override
    public void setChildSize(boolean child) {
        width = child ? 0.49F : 0.6F;
        height = child ? 0.98F : 1.95F;
        setPosition(posX, posY, posZ);
    }

    @Override
    public float getEyeHeight() {
        return isChild() ? 0.775F : 1.74F;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager.isBiome(
                world, getPosition(),
                xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome.DRIPSTONE_CAVES)
                ? 4 : 1;
    }

    @Override
    public boolean isNotColliding() {
        return world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()
                && world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             @Nullable IEntityLivingData livingData) {
        livingData = super.onInitialSpawn(difficulty, livingData);
        if (getHeldItemOffhand().isEmpty() && rand.nextFloat() < 0.03F) {
            ItemStack shell = FFDItems.effectiveStack(FFDItems.NAUTILUS_SHELL);
            if (!shell.isEmpty()) {
                setItemStackToSlot(EntityEquipmentSlot.OFFHAND, shell);
                setDropChance(EntityEquipmentSlot.OFFHAND, 2.0F);
            }
        }
        return livingData;
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        if (rand.nextFloat() > 0.9F) {
            if (rand.nextInt(16) < 10) {
                ItemStack trident = FFDItems.effectiveStack(FFDItems.TRIDENT);
                if (!trident.isEmpty()) {
                    setItemStackToSlot(EntityEquipmentSlot.MAINHAND, trident);
                    return;
                }
            }
            setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
        }
    }

    @Override
    protected boolean canEquipItem(ItemStack stack) {
        return super.canEquipItem(stack);
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        ItemStack stack = itemEntity.getItem();
        EntityEquipmentSlot slot = getSlotForItemStack(stack);
        ItemStack held = getItemStackFromSlot(slot);
        if (!canReplaceCurrentItem(stack, held, slot) || !canEquipItem(stack)) {
            return;
        }
        double dropChance = slot.getSlotType() == EntityEquipmentSlot.Type.HAND
                ? inventoryHandsDropChances[slot.getIndex()]
                : inventoryArmorDropChances[slot.getIndex()];
        if (!held.isEmpty() && rand.nextFloat() - 0.1F < dropChance) {
            entityDropItem(held, 0.0F);
        }
        setItemStackToSlot(slot, stack);
        setDropChance(slot, 2.0F);
        enablePersistence();
        onItemPickup(itemEntity, stack.getCount());
        itemEntity.setDead();
    }

    private boolean canReplaceCurrentItem(ItemStack stack, ItemStack held,
                                          EntityEquipmentSlot slot) {
        if (held.isEmpty()) {
            return true;
        }
        if (held.getItem() == FFDItems.effectiveItem(FFDItems.NAUTILUS_SHELL)) {
            return false;
        }
        if (slot == EntityEquipmentSlot.MAINHAND) {
            boolean newTrident = FFDItems.isTridentStack(stack);
            boolean oldTrident = FFDItems.isTridentStack(held);
            if (newTrident != oldTrident) {
                return newTrident;
            }
            double newDamage = equipmentAttribute(stack,
                    SharedMonsterAttributes.ATTACK_DAMAGE, slot);
            double oldDamage = equipmentAttribute(held,
                    SharedMonsterAttributes.ATTACK_DAMAGE, slot);
            return newDamage != oldDamage ? newDamage > oldDamage
                    : canReplaceEqualItem(stack, held);
        }
        if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            if (EnchantmentHelper.hasBindingCurse(held)) {
                return false;
            }
            double newArmor = equipmentAttribute(stack,
                    SharedMonsterAttributes.ARMOR, slot);
            double oldArmor = equipmentAttribute(held,
                    SharedMonsterAttributes.ARMOR, slot);
            if (newArmor != oldArmor) {
                return newArmor > oldArmor;
            }
            double newToughness = equipmentAttribute(stack,
                    SharedMonsterAttributes.ARMOR_TOUGHNESS, slot);
            double oldToughness = equipmentAttribute(held,
                    SharedMonsterAttributes.ARMOR_TOUGHNESS, slot);
            return newToughness != oldToughness ? newToughness > oldToughness
                    : canReplaceEqualItem(stack, held);
        }
        return false;
    }

    private boolean canReplaceEqualItem(ItemStack stack, ItemStack held) {
        int newEnchantments = EnchantmentHelper.getEnchantments(stack).size();
        int oldEnchantments = EnchantmentHelper.getEnchantments(held).size();
        if (newEnchantments != oldEnchantments) {
            return newEnchantments > oldEnchantments;
        }
        if (stack.getItemDamage() != held.getItemDamage()) {
            return stack.getItemDamage() < held.getItemDamage();
        }
        return stack.hasDisplayName() && !held.hasDisplayName();
    }

    private double equipmentAttribute(ItemStack stack, IAttribute attribute,
                                      EntityEquipmentSlot slot) {
        double base = getEntityAttribute(attribute) == null
                ? 0.0D : getEntityAttribute(attribute).getBaseValue();
        Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
        double value = base;
        for (AttributeModifier modifier : modifiers.get(attribute.getName())) {
            if (modifier.getOperation() == 0) {
                value += modifier.getAmount();
            }
        }
        double valueWithAdditions = value;
        for (AttributeModifier modifier : modifiers.get(attribute.getName())) {
            if (modifier.getOperation() == 1) {
                valueWithAdditions += value * modifier.getAmount();
            }
        }
        for (AttributeModifier modifier : modifiers.get(attribute.getName())) {
            if (modifier.getOperation() == 2) {
                valueWithAdditions *= 1.0D + modifier.getAmount();
            }
        }
        return valueWithAdditions;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        ItemStack held = getHeldItemMainhand();
        EntityTrident trident = new EntityTrident(world, this, held);
        double x = target.posX - posX;
        double y = target.getEntityBoundingBox().minY + target.height / 3.0F - trident.posY;
        double z = target.posZ - posZ;
        double horizontal = MathHelper.sqrt(x * x + z * z);
        trident.shoot(x, y + horizontal * 0.2D, z, 1.6F,
                14 - world.getDifficulty().getDifficultyId() * 4);
        playSound(FFDSounds.DROWNED_SHOOT, 1.0F,
                1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(trident);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        setArmsRaised(swingingArms);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        if (wasRecentlyHit && rand.nextFloat() < 0.11F + 0.02F * lootingModifier) {
            ItemStack copper = FFDItems.effectiveStack(FFDItems.COPPER_INGOT);
            if (!copper.isEmpty()) {
                entityDropItem(copper, 0.0F);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWater() ? FFDSounds.DROWNED_AMBIENT_WATER : FFDSounds.DROWNED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isInWater() ? FFDSounds.DROWNED_HURT_WATER : FFDSounds.DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isInWater() ? FFDSounds.DROWNED_DEATH_WATER : FFDSounds.DROWNED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return FFDSounds.DROWNED_STEP;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return FFDSounds.DROWNED_SWIM;
    }

    public static boolean convertFrom(EntityZombie zombie) {
        if (zombie.world.isRemote || zombie.isDead) {
            return false;
        }
        EntityDrowned drowned = new EntityDrowned(zombie.world);
        drowned.copyLocationAndAnglesFrom(zombie);
        drowned.motionX = zombie.motionX;
        drowned.motionY = zombie.motionY;
        drowned.motionZ = zombie.motionZ;
        drowned.fallDistance = zombie.fallDistance;
        drowned.hurtTime = zombie.hurtTime;
        drowned.hurtResistantTime = zombie.hurtResistantTime;
        drowned.onGround = zombie.onGround;
        drowned.renderYawOffset = zombie.renderYawOffset;
        drowned.rotationYawHead = zombie.rotationYawHead;
        drowned.setAbsorptionAmount(zombie.getAbsorptionAmount());
        for (PotionEffect effect : zombie.getActivePotionEffects()) {
            drowned.addPotionEffect(new PotionEffect(effect));
        }
        drowned.setCanPickUpLoot(zombie.canPickUpLoot());
        drowned.setLeftHanded(zombie.isLeftHanded());
        drowned.setChild(zombie.isChild());
        drowned.setNoAI(zombie.isAIDisabled());
        drowned.setBreakDoorsAItask(zombie.isBreakDoorsTaskSet());
        if (zombie.isNoDespawnRequired()) {
            drowned.enablePersistence();
        }
        if (zombie.hasCustomName()) {
            drowned.setCustomNameTag(zombie.getCustomNameTag());
            drowned.setAlwaysRenderNameTag(zombie.getAlwaysRenderNameTag());
        }
        drowned.setSilent(zombie.isSilent());
        drowned.setNoGravity(zombie.hasNoGravity());
        drowned.setEntityInvulnerable(zombie.getIsInvulnerable());
        drowned.timeUntilPortal = zombie.timeUntilPortal;
        if (zombie.isBurning()) {
            drowned.setFire(1);
        }
        for (String tag : zombie.getTags()) {
            drowned.addTag(tag);
        }
        NBTTagCompound customData = (NBTTagCompound) zombie.getEntityData().copy();
        customData.removeTag(SUBMERGED_TICKS_TAG);
        customData.removeTag(CONVERSION_TICKS_TAG);
        drowned.getEntityData().merge(customData);
        NBTTagCompound saved = new NBTTagCompound();
        zombie.writeEntityToNBT(saved);
        NBTTagList handChances = saved.getTagList("HandDropChances", 5);
        NBTTagList armorChances = saved.getTagList("ArmorDropChances", 5);
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = zombie.getItemStackFromSlot(slot);
            if (!stack.isEmpty()) {
                drowned.setItemStackToSlot(slot, stack.copy());
            }
            NBTTagList chances = slot.getSlotType() == EntityEquipmentSlot.Type.HAND
                    ? handChances : armorChances;
            if (slot.getIndex() < chances.tagCount()) {
                drowned.setDropChance(slot, chances.getFloatAt(slot.getIndex()));
            }
        }
        Entity passenger = zombie.getPassengers().isEmpty()
                ? null : zombie.getPassengers().get(0);
        Entity vehicle = zombie.getRidingEntity();
        Entity leashHolder = zombie.getLeashed() ? zombie.getLeashHolder() : null;
        Team team = zombie.getTeam();
        if (!zombie.world.spawnEntity(drowned)) {
            return false;
        }
        if (passenger != null) {
            passenger.dismountRidingEntity();
            passenger.startRiding(drowned, true);
        }
        if (vehicle != null) {
            zombie.dismountRidingEntity();
            drowned.startRiding(vehicle, true);
        }
        if (leashHolder != null) {
            zombie.clearLeashed(false, false);
            drowned.setLeashHolder(leashHolder, true);
        }
        if (team instanceof ScorePlayerTeam) {
            ScorePlayerTeam scoreTeam = (ScorePlayerTeam) team;
            zombie.world.getScoreboard().addPlayerToTeam(
                    drowned.getCachedUniqueIdString(), scoreTeam.getName());
            zombie.world.getScoreboard().removePlayerFromTeam(
                    zombie.getCachedUniqueIdString(), scoreTeam);
        }
        zombie.setDead();
        return true;
    }

    private boolean nearNavigatorTarget() {
        Path path = getNavigator().getPath();
        if (path == null) {
            return false;
        }
        PathPoint point = path.getFinalPathPoint();
        return point != null && getDistanceSq(point.x, point.y, point.z) < 4.0D;
    }

    private static final class DrownedBreakDoor extends EntityAIBreakDoor {
        private DrownedBreakDoor(EntityDrowned drowned) {
            super(drowned);
        }

        @Override
        public boolean shouldExecute() {
            return entity.getNavigator() instanceof PathNavigateGround && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return entity.getNavigator() instanceof PathNavigateGround
                    && super.shouldContinueExecuting();
        }
    }

    private static final class DrownedAttack extends EntityAIZombieAttack {
        private final EntityDrowned drowned;

        private DrownedAttack(EntityDrowned drowned, double speed, boolean longMemory) {
            super(drowned, speed, longMemory);
            this.drowned = drowned;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && drowned.canAttackTarget(drowned.getAttackTarget());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting()
                    && drowned.canAttackTarget(drowned.getAttackTarget());
        }
    }

    private static final class TridentAttack extends EntityAIAttackRanged {
        private final EntityDrowned drowned;

        private TridentAttack(EntityDrowned drowned, double speed, int interval,
                              float radius) {
            super(drowned, speed, interval, radius);
            this.drowned = drowned;
        }

        @Override
        public boolean shouldExecute() {
            return FFDItems.isTridentStack(drowned.getHeldItemMainhand())
                    && super.shouldExecute();
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            drowned.setArmsRaised(true);
            drowned.setActiveHand(EnumHand.MAIN_HAND);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            drowned.resetActiveHand();
            drowned.setArmsRaised(false);
        }
    }

    private static final class PlayerTarget implements Predicate<EntityPlayer> {
        private final EntityDrowned drowned;

        private PlayerTarget(EntityDrowned drowned) {
            this.drowned = drowned;
        }

        @Override
        public boolean apply(@Nullable EntityPlayer player) {
            return drowned.canAttackTarget(player);
        }
    }

    private static final class GoToWater extends EntityAIBase {
        private final EntityCreature creature;
        private final double speed;
        private double x;
        private double y;
        private double z;

        private GoToWater(EntityCreature creature, double speed) {
            this.creature = creature;
            this.speed = speed;
            setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!creature.world.isDaytime() || creature.isInWater()) {
                return false;
            }
            Vec3d target = findWater();
            if (target == null) {
                return false;
            }
            x = target.x;
            y = target.y;
            z = target.z;
            return true;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !creature.getNavigator().noPath();
        }

        @Override
        public void startExecuting() {
            creature.getNavigator().tryMoveToXYZ(x, y, z, speed);
        }

        @Nullable
        private Vec3d findWater() {
            Random random = creature.getRNG();
            BlockPos origin = new BlockPos(creature.posX,
                    creature.getEntityBoundingBox().minY, creature.posZ);
            for (int i = 0; i < 10; i++) {
                BlockPos pos = origin.add(random.nextInt(20) - 10,
                        2 - random.nextInt(8), random.nextInt(20) - 10);
                if (creature.world.getBlockState(pos).getMaterial() == Material.WATER) {
                    return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return null;
        }
    }

    private static final class GoToBeach extends EntityAIMoveToBlock {
        private final EntityDrowned drowned;

        private GoToBeach(EntityDrowned drowned, double speed) {
            super(drowned, speed, 8);
            this.drowned = drowned;
        }

        @Override
        public boolean shouldExecute() {
            return !drowned.world.isDaytime() && drowned.isInWater()
                    && drowned.posY >= drowned.world.getSeaLevel() - 3
                    && super.shouldExecute();
        }

        @Override
        public void startExecuting() {
            drowned.swimmingUp = false;
            drowned.navigator = drowned.groundNavigator;
            super.startExecuting();
        }

        @Override
        protected boolean shouldMoveTo(World world, BlockPos pos) {
            BlockPos above = pos.up();
            return world.isAirBlock(above) && world.isAirBlock(above.up())
                    && world.getBlockState(pos).isSideSolid(world, pos,
                    net.minecraft.util.EnumFacing.UP);
        }
    }

    private static final class SwimUp extends EntityAIBase {
        private final EntityDrowned drowned;
        private final double speed;
        private final int seaLevel;
        private boolean blocked;

        private SwimUp(EntityDrowned drowned, double speed, int seaLevel) {
            this.drowned = drowned;
            this.speed = speed;
            this.seaLevel = seaLevel;
        }

        @Override
        public boolean shouldExecute() {
            return !drowned.world.isDaytime() && drowned.isInWater()
                    && drowned.posY < seaLevel - 2;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute() && !blocked;
        }

        @Override
        public void startExecuting() {
            drowned.swimmingUp = true;
            blocked = false;
        }

        @Override
        public void resetTask() {
            drowned.swimmingUp = false;
        }

        @Override
        public void updateTask() {
            if (drowned.posY < seaLevel - 1
                    && (drowned.getNavigator().noPath() || drowned.nearNavigatorTarget())) {
                Vec3d target = RandomPositionGenerator.findRandomTargetBlockTowards(
                        drowned, 4, 8,
                        new Vec3d(drowned.posX, seaLevel - 1, drowned.posZ));
                if (target == null) {
                    blocked = true;
                } else {
                    drowned.getNavigator().tryMoveToXYZ(target.x, target.y, target.z, speed);
                }
            }
        }
    }

    private static final class DrownedMoveHelper extends EntityMoveHelper {
        private final EntityDrowned drowned;

        private DrownedMoveHelper(EntityDrowned drowned) {
            super(drowned);
            this.drowned = drowned;
        }

        @Override
        public void onUpdateMoveHelper() {
            EntityLivingBase target = drowned.getAttackTarget();
            if (drowned.wantsToSwim() && drowned.isInWater()) {
                if (target != null && target.posY > drowned.posY || drowned.swimmingUp) {
                    drowned.motionY += 0.002D;
                }
                if (action != Action.MOVE_TO || drowned.getNavigator().noPath()) {
                    drowned.setAIMoveSpeed(0.0F);
                    return;
                }
                double x = posX - drowned.posX;
                double y = posY - drowned.posY;
                double z = posZ - drowned.posZ;
                double distance = Math.sqrt(x * x + y * y + z * z);
                y /= distance;
                float yaw = (float) (MathHelper.atan2(z, x) * 57.295776D) - 90.0F;
                drowned.rotationYaw = limitAngle(drowned.rotationYaw, yaw, 90.0F);
                drowned.renderYawOffset = drowned.rotationYaw;
                float desired = (float) (speed * drowned.getEntityAttribute(
                        SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                drowned.setAIMoveSpeed(drowned.getAIMoveSpeed()
                        + (desired - drowned.getAIMoveSpeed()) * 0.125F);
                drowned.motionY += drowned.getAIMoveSpeed() * y * 0.1D;
                drowned.motionX += drowned.getAIMoveSpeed() * x * 0.005D;
                drowned.motionZ += drowned.getAIMoveSpeed() * z * 0.005D;
            } else {
                if (!drowned.onGround) {
                    drowned.motionY -= 0.008D;
                }
                super.onUpdateMoveHelper();
            }
        }
    }
}
