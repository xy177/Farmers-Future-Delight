package xy177.farmersfuturedelight.common.entity;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.oredict.OreDictionary;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockCampfire;
import xy177.farmersfuturedelight.common.item.ItemCrossbow;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityPiglin extends EntityMob {
    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = getPosition();
        return world.getDifficulty() != net.minecraft.world.EnumDifficulty.PEACEFUL
                && world.getBlockState(pos.down()).getBlock() != net.minecraft.init.Blocks.NETHER_WART_BLOCK
                && net.minecraft.world.WorldEntitySpawner.canCreatureTypeSpawnAtLocation(
                        net.minecraft.entity.EntityLiving.SpawnPlacementType.ON_GROUND, world, pos);
    }

    private static final DataParameter<Boolean> BABY = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IMMUNE = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ADMIRING = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DANCING = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> AGGRESSIVE = EntityDataManager.createKey(EntityPiglin.class, DataSerializers.BOOLEAN);
    private static final AttributeModifier BABY_SPEED = new AttributeModifier(UUID.fromString("3a8b5141-74b3-4f65-97c1-b34e851639b5"), "Piglin baby movement", 0.2D, 1).setSaved(false);
    private final InventoryBasic inventory = new InventoryBasic("Piglin", false, 8);
    private UUID angerTarget;
    private int angerTicks;
    private int admireTicks;
    private int admireDisabledTicks;
    private int retreatTicks;
    private int eatCooldown;
    private int huntCooldown;
    private int danceTicks;
    private int timeInOverworld;
    private int rideCooldown;
    private int rideTicks;
    private boolean cannotHunt;
    private EntityLivingBase retreatTarget;
    private BlockPos repellent;
    private BlockPos home;

    public EntityPiglin(World world) {
        super(world);
        setSize(0.6F, 1.95F);
        setCanPickUpLoot(!isBrute());
        if (getNavigator() instanceof PathNavigateGround) ((PathNavigateGround) getNavigator()).setEnterDoors(true);
        setPathPriority(net.minecraft.pathfinding.PathNodeType.DAMAGE_FIRE, -1.0F);
        setPathPriority(net.minecraft.pathfinding.PathNodeType.DANGER_FIRE, 16.0F);
        experienceValue = 5;
        huntCooldown = 600 + rand.nextInt(1801);
        rideCooldown = 200 + rand.nextInt(601);
    }

    public boolean isBrute() { return false; }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(BABY, false); dataManager.register(IMMUNE, false);
        dataManager.register(CHARGING, false); dataManager.register(ADMIRING, false); dataManager.register(DANCING, false);
        dataManager.register(AGGRESSIVE, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(isBrute() ? 50.0D : 16.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(isBrute() ? 7.0D : 5.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(isBrute() ? 12.0D : 16.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        if (!isBrute()) {
            tasks.addTask(1, new FleeGoal());
            tasks.addTask(2, new AdmireGoal());
            tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityPigZombie.class, 6.0F, 1.0D, 1.0D));
            tasks.addTask(1, new EntityAIAvoidEntity<>(this, EntityZoglin.class, 6.0F, 1.0D, 1.0D));
            tasks.addTask(1, new EntityAIAvoidEntity<EntityWitherSkeleton>(this, EntityWitherSkeleton.class, 8.0F, 1.0D, 1.0D) {
                @Override public boolean shouldExecute() { return isChild() && super.shouldExecute(); }
            });
            tasks.addTask(5, new BabyRideGoal());
        }
        tasks.addTask(3, new CrossbowGoal());
        tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true) {
            @Override public boolean shouldExecute() { return !isChild() && !isAdmiring() && !hasCrossbow() && super.shouldExecute(); }
            @Override public boolean shouldContinueExecuting() { return !isChild() && !isAdmiring() && !hasCrossbow() && super.shouldContinueExecuting(); }
        });
        tasks.addTask(4, new EntityAIOpenDoor(this, true));
        tasks.addTask(6, new EntityAIWander(this, 0.6D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
    }

    @Override public boolean isChild() { return dataManager.get(BABY); }

    public void setBaby(boolean baby) {
        if (isBrute()) baby = false;
        dataManager.set(BABY, baby);
        refreshDimensions();
        net.minecraft.entity.ai.attributes.IAttributeInstance speed = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (baby && !speed.hasModifier(BABY_SPEED)) speed.applyModifier(BABY_SPEED);
        else if (!baby) speed.removeModifier(BABY_SPEED);
    }

    private void refreshDimensions() { setSize(isChild() ? 0.49F : 0.6F, isChild() ? 0.98F : 1.95F); }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {
        super.notifyDataManagerChange(parameter);
        if (BABY.equals(parameter)) refreshDimensions();
    }

    @Override public float getEyeHeight() { return isChild() ? 0.78F : 1.79F; }
    public void setImmuneToZombification(boolean immune) { dataManager.set(IMMUNE, immune); }
    public boolean isConverting() { return !dataManager.get(IMMUNE) && !isAIDisabled() && world.provider.getDimension() != -1; }
    public boolean isChargingCrossbow() { return dataManager.get(CHARGING); }
    public boolean isAdmiring() { return dataManager.get(ADMIRING); }
    public boolean isDancing() { return dataManager.get(DANCING); }
    public boolean isAggressive() { return dataManager.get(AGGRESSIVE); }
    public boolean hasCrossbow() { return FFDItems.isCrossbowItem(getHeldItemMainhand().getItem()); }

    @Override
    protected void updateAITasks() {
        if (isConverting()) { if (++timeInOverworld > 300) finishConversion(); } else timeInOverworld = 0;
        if (admireDisabledTicks > 0) --admireDisabledTicks;
        if (eatCooldown > 0) --eatCooldown;
        if (huntCooldown > 0) --huntCooldown;
        if (retreatTicks > 0) --retreatTicks;
        if (rideCooldown > 0) --rideCooldown;
        if (isChild() && isRiding()) {
            Entity vehicle=getRidingEntity();
            if (--rideTicks <= 0 || retreatTicks > 0 || !(vehicle instanceof net.minecraft.entity.EntityLivingBase)
                    || !((net.minecraft.entity.EntityLivingBase)vehicle).isChild() || !vehicle.isEntityAlive()) {
                dismountRidingEntity();rideCooldown=200+rand.nextInt(601);
            }
        }
        if (danceTicks > 0 && --danceTicks == 0) dataManager.set(DANCING, false);
        if (angerTicks > 0 && --angerTicks == 0) { angerTarget = null; setAttackTarget(null); }
        if (admireTicks > 0 && --admireTicks == 0) finishAdmiring(true);
        if (ticksExisted % 20 == 0) updateAwareness();
        if (isChild() || isAdmiring() || retreatTicks > 0 || repellent != null && !isBrute()) setAttackTarget(null);
        dataManager.set(AGGRESSIVE,getAttackTarget()!=null);
        super.updateAITasks();
    }

    public void forgivePlayer(EntityPlayer player) {
        if (player.getUniqueID().equals(angerTarget)) {
            angerTarget = null;
            angerTicks = 0;
            setRevengeTarget(null);
            setAttackTarget(null);
        }
    }

    private void updateAwareness() {
        if (!isBrute()) findRepellent();
        if (getAttackTarget() != null && (!getAttackTarget().isEntityAlive() || getDistanceSq(getAttackTarget()) > 1024.0D)) setAttackTarget(null);
        if (isChild() || isAdmiring() || retreatTicks > 0 || repellent != null && !isBrute()) return;
        if (getAttackTarget() == null && angerTarget != null && world instanceof WorldServer) {
            Entity target = ((WorldServer) world).getEntityFromUuid(angerTarget);
            if (target instanceof EntityLivingBase && validTarget((EntityLivingBase) target)) setAttackTarget((EntityLivingBase) target);
        }
        if (getAttackTarget() == null) {
            double range = isBrute() ? 12.0D : 16.0D;
            EntityLivingBase nearest = null;
            for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(range), this::validTarget)) {
                if (!(target instanceof EntityWitherSkeleton) && !(target instanceof net.minecraft.entity.boss.EntityWither)
                        && !(target instanceof EntityPlayer && (isBrute() || !wearsGold(target)))) continue;
                if (getDistanceSq(target) > range * range || !getEntitySenses().canSee(target)) continue;
                if (nearest == null || getDistanceSq(target) < getDistanceSq(nearest)) nearest = target;
            }
            setAttackTarget(nearest);
        }
        if (!isBrute() && getAttackTarget() == null && !cannotHunt && huntCooldown == 0) {
            for (EntityHoglin hoglin : world.getEntitiesWithinAABB(EntityHoglin.class, getEntityBoundingBox().grow(16),
                    animal -> animal.canBeHunted() && getEntitySenses().canSee(animal))) {
                angerAt(hoglin, false);
                huntCooldown = 600 + rand.nextInt(1801);
                for (EntityPiglin ally : world.getEntitiesWithinAABB(EntityPiglin.class, getEntityBoundingBox().grow(16))) {
                    if (!ally.isChild() && !ally.isBrute() && ally.huntCooldown == 0) ally.angerAt(hoglin, false);
                }
                break;
            }
        }
        if (isBrute() && getAttackTarget() == null && home != null && getDistanceSq(home) > 100.0D) getNavigator().tryMoveToXYZ(home.getX()+0.5D,home.getY(),home.getZ()+0.5D,0.6D);
        if (!isBrute() && getAttackTarget() == null && !isAdmiring() && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this)) {
            EntityItem nearestItem = null;
            for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(8), entity -> !entity.isDead && wantsItem(entity.getItem()))) {
                if (getEntitySenses().canSee(item) && (nearestItem == null || getDistanceSq(item) < getDistanceSq(nearestItem))) nearestItem = item;
            }
            if (nearestItem != null) getNavigator().tryMoveToXYZ(nearestItem.posX,nearestItem.posY,nearestItem.posZ,1.0D);
        }
    }

    private boolean validTarget(EntityLivingBase target) {
        return target != this && target.isEntityAlive() && !(target instanceof EntityPiglin) && !isOnSameTeam(target)
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isCreative() && !((EntityPlayer) target).isSpectator());
    }

    public static boolean wearsGold(EntityLivingBase target) {
        for (ItemStack stack : target.getArmorInventoryList()) if (stack.getItem() instanceof ItemArmor
                && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD) return true;
        return false;
    }

    public void angerAt(EntityLivingBase target, boolean broadcast) {
        if (!validTarget(target)) return;
        angerTarget = target.getUniqueID(); angerTicks = 600;
        if (!isChild()) setAttackTarget(target);
        if (broadcast) for (EntityPiglin ally : world.getEntitiesWithinAABB(EntityPiglin.class, getEntityBoundingBox().grow(16))) {
            if (!ally.isChild() && ally != this && ally.getEntitySenses().canSee(target)) ally.angerAt(target, false);
        }
    }

    private void findRepellent() {
        repellent = null;
        BlockPos center = getPosition();
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(center.getX()-8,center.getY()-4,center.getZ()-8),new BlockPos(center.getX()+8,center.getY()+4,center.getZ()+8))) {
            if (!world.isBlockLoaded(pos)) continue;
            ResourceLocation id = world.getBlockState(pos).getBlock().getRegistryName();
            boolean litCampfire = world.getBlockState(pos).getBlock() == xy177.farmersfuturedelight.common.registry.FFDBlocks.SOUL_CAMPFIRE
                    && world.getBlockState(pos).getValue(BlockCampfire.LIT);
            if (id != null && ("soul_fire".equals(id.getResourcePath()) || "soul_torch".equals(id.getResourcePath())
                    || "soul_lantern".equals(id.getResourcePath()) || litCampfire)) {
                if (repellent == null || pos.distanceSq(center) < repellent.distanceSq(center)) repellent = pos.toImmutable();
            }
        }
    }

    public static boolean loves(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.GOLD) return true;
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null) return false;
        String path = name.getResourcePath();
        if (path.startsWith("golden_") || path.startsWith("gold_") || path.equals("raw_gold") || path.equals("raw_gold_block")
                || path.equals("nether_gold_ore") || path.equals("gilded_blackstone") || path.equals("clock")
                || path.equals("bell") || path.equals("light_weighted_pressure_plate") || path.equals("speckled_melon")) return true;
        for (int ore : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(ore);
            if (oreName.equals("ingotGold") || oreName.equals("oreGold") || oreName.equals("blockGold")) return true;
        }
        return false;
    }

    private boolean wantsItem(ItemStack stack) {
        if (isBrute() || stack.isEmpty() || admireDisabledTicks > 0 && getAttackTarget() != null) return false;
        if (isChild() && stack.getItem() == Items.LEATHER) return false;
        return loves(stack) && !isAdmiring() || stack.getItem() == Items.GOLD_NUGGET
                || eatCooldown == 0 && (stack.getItem() == Items.PORKCHOP || stack.getItem() == Items.COOKED_PORKCHOP)
                || canEquip(stack);
    }

    private boolean canEquip(ItemStack stack) {
        EntityEquipmentSlot slot = net.minecraft.entity.EntityLiving.getSlotForItemStack(stack);
        ItemStack current = getItemStackFromSlot(slot);
        if (stack.getItem() instanceof ItemArmor) return current.isEmpty() || loves(stack) && !loves(current)
                || loves(stack) == loves(current) && ((ItemArmor) stack.getItem()).damageReduceAmount > (current.getItem() instanceof ItemArmor ? ((ItemArmor) current.getItem()).damageReduceAmount : 0);
        return slot == EntityEquipmentSlot.MAINHAND && (stack.getItem() instanceof net.minecraft.item.ItemSword || FFDItems.isCrossbowItem(stack.getItem()))
                && (current.isEmpty() || loves(stack) && !loves(current));
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem entity) {
        ItemStack stack = entity.getItem();
        if (!wantsItem(stack)) return;
        int count = stack.getItem() == Items.GOLD_NUGGET ? stack.getCount() : 1;
        ItemStack taken = stack.splitStack(count);
        if (stack.isEmpty()) entity.setDead();
        onItemPickup(entity, count); enablePersistence();
        if (taken.getItem() == Items.PORKCHOP || taken.getItem() == Items.COOKED_PORKCHOP) { eatCooldown = 200; return; }
        if (loves(taken) && taken.getItem() != Items.GOLD_NUGGET) {
            if (entity.getThrower() != null) distract(world.getPlayerEntityByName(entity.getThrower()));
            startAdmiring(taken);
        }
        else if (!equip(taken)) store(taken);
    }

    private void distract(EntityPlayer player) {
        if (!isBrute() && !isChild() && player instanceof net.minecraft.entity.player.EntityPlayerMP
                && !wearsGold(player)) {
            xy177.farmersfuturedelight.common.advancement.FFDAdvancements.DISTRACT_PIGLIN.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) player);
        }
    }

    private boolean equip(ItemStack stack) {
        if (!canEquip(stack)) return false;
        EntityEquipmentSlot slot = net.minecraft.entity.EntityLiving.getSlotForItemStack(stack);
        ItemStack previous = getItemStackFromSlot(slot);
        if (!previous.isEmpty()) entityDropItem(previous, 0.0F);
        setItemStackToSlot(slot, stack); setDropChance(slot, 2.0F);
        return true;
    }

    private void store(ItemStack stack) {
        for (int index = 0; index < inventory.getSizeInventory() && !stack.isEmpty(); index++) {
            ItemStack existing = inventory.getStackInSlot(index);
            if (existing.isEmpty()) { inventory.setInventorySlotContents(index, stack); return; }
            if (ItemStack.areItemsEqual(existing,stack) && ItemStack.areItemStackTagsEqual(existing,stack)) {
                int count = Math.min(stack.getCount(),existing.getMaxStackSize()-existing.getCount()); existing.grow(count); stack.shrink(count);
            }
        }
        if (!stack.isEmpty()) entityDropItem(stack,0.0F);
    }

    public void startAdmiring(ItemStack stack) {
        if (!getHeldItemOffhand().isEmpty()) store(getHeldItemOffhand());
        setHeldItem(EnumHand.OFF_HAND, stack); admireTicks = 119; dataManager.set(ADMIRING,true);
        getNavigator().clearPath();
    }

    private void finishAdmiring(boolean barter) {
        ItemStack admired = getHeldItemOffhand(); setHeldItem(EnumHand.OFF_HAND,ItemStack.EMPTY);
        dataManager.set(ADMIRING,false); admireTicks = 0;
        if (admired.isEmpty()) return;
        if (!isChild() && admired.getItem() == Items.GOLD_INGOT) {
            if (barter && world instanceof WorldServer) {
                List<ItemStack> gifts = ((WorldServer) world).getLootTableManager().getLootTableFromLocation(
                        new ResourceLocation(FarmerFutureDelight.MODID,"gameplay/piglin_bartering"))
                        .generateLootForPools(rand,new LootContext.Builder((WorldServer)world).withLootedEntity(this).build());
                EntityPlayer player = world.getClosestPlayerToEntity(this,16);
                Vec3d target = player == null ? getPositionVector().addVector(rand.nextInt(9)-4,1,rand.nextInt(9)-4) : player.getPositionVector();
                for (ItemStack gift : gifts) {
                    EntityItem item = new EntityItem(world,posX,posY+getEyeHeight()-.3D,posZ,gift);
                    Vec3d direction = target.subtract(getPositionVector()).normalize().scale(.3D);
                    item.motionX=direction.x; item.motionY=.2D; item.motionZ=direction.z; item.setDefaultPickupDelay(); world.spawnEntity(item);
                }
            }
        } else if (!equip(admired)) {
            if (isChild() && !loves(getHeldItemMainhand())) {
                if (!getHeldItemMainhand().isEmpty()) entityDropItem(getHeldItemMainhand(),0);
                setHeldItem(EnumHand.MAIN_HAND,admired);
            } else store(admired);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player,EnumHand hand) {
        ItemStack offered=player.getHeldItem(hand);
        if (!isBrute() && !isChild() && offered.getItem()==Items.GOLD_INGOT && admireDisabledTicks==0 && !isAdmiring()) {
            if (!world.isRemote) { distract(player); startAdmiring(new ItemStack(Items.GOLD_INGOT)); if(!player.capabilities.isCreativeMode)offered.shrink(1); enablePersistence(); }
            return true;
        }
        return super.processInteract(player,hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source,float amount) {
        boolean hurt=super.attackEntityFrom(source,amount);
        if (hurt && !world.isRemote && source.getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker=(EntityLivingBase)source.getTrueSource();
            if (!(attacker instanceof EntityPiglin)) {
                finishAdmiring(false); danceTicks=0; dataManager.set(DANCING,false);
                if(attacker instanceof EntityPlayer)admireDisabledTicks=400;
                if(isChild()) {retreatTarget=attacker;retreatTicks=100;}
                angerAt(attacker,true);
                if(!isBrute() && attacker instanceof EntityHoglin && !((EntityHoglin)attacker).isZoglin()) {
                    int hoglins=world.getEntitiesWithinAABB(EntityHoglin.class,getEntityBoundingBox().grow(16),animal->!animal.isChild()&&!animal.isZoglin()).size();
                    int piglins=world.getEntitiesWithinAABB(EntityPiglin.class,getEntityBoundingBox().grow(16),animal->!animal.isChild()).size();
                    if(hoglins>piglins) {retreatTarget=attacker;retreatTicks=100+rand.nextInt(301);huntCooldown=2400;setAttackTarget(null);}
                }
            }
        }
        return hurt;
    }

    @Override
    public void onKillEntity(EntityLivingBase victim) {
        super.onKillEntity(victim);
        if(victim instanceof EntityHoglin && !isBrute()) {
            huntCooldown=600+rand.nextInt(1801);setAttackTarget(null);angerTarget=null;angerTicks=0;
            if(rand.nextFloat()<.1F) {danceTicks=300;dataManager.set(DANCING,true);playSound(FFDSounds.PIGLIN_CELEBRATE,1,1);}
        }
    }

    private void finishConversion() {
        EntityPigZombie converted=new EntityPigZombie(world);converted.copyLocationAndAnglesFrom(this);
        converted.setChild(isChild());converted.setSilent(isSilent());converted.setEntityInvulnerable(getIsInvulnerable());
        if(hasCustomName()) {converted.setCustomNameTag(getCustomNameTag());converted.setAlwaysRenderNameTag(getAlwaysRenderNameTag());}
        if(isNoDespawnRequired())converted.enablePersistence();
        for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {converted.setItemStackToSlot(slot,getItemStackFromSlot(slot).copy());converted.setDropChance(slot,2.0F);}
        converted.addPotionEffect(new PotionEffect(MobEffects.NAUSEA,200));
        if(world.spawnEntity(converted)) {
            for(int index=0;index<inventory.getSizeInventory();index++)if(!inventory.getStackInSlot(index).isEmpty())entityDropItem(inventory.getStackInSlot(index),0);
            for(Entity passenger:new java.util.ArrayList<>(getPassengers()))passenger.startRiding(converted,true);
            if(getLeashed())converted.setLeashHolder(getLeashHolder(),true);
            playSound(isBrute()?FFDSounds.PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED:FFDSounds.PIGLIN_CONVERTED_TO_ZOMBIFIED,1,1);setDead();
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,@Nullable IEntityLivingData data) {
        IEntityLivingData result=super.onInitialSpawn(difficulty,data);
        if(!isBrute())setBaby(rand.nextFloat()<.2F);
        if(!isChild()) {
            ItemStack weapon=isBrute()?new ItemStack(Items.GOLDEN_AXE):rand.nextBoolean()?FFDItems.effectiveStack(FFDItems.CROSSBOW):new ItemStack(Items.GOLDEN_SWORD);
            if(weapon.isEmpty())weapon=new ItemStack(Items.GOLDEN_SWORD);
            setHeldItem(EnumHand.MAIN_HAND,weapon);
            if(!isBrute())for(EntityEquipmentSlot slot:new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD,EntityEquipmentSlot.CHEST,EntityEquipmentSlot.LEGS,EntityEquipmentSlot.FEET}) {
                if(rand.nextFloat()<.1F)setItemStackToSlot(slot,new ItemStack(slot==EntityEquipmentSlot.HEAD?Items.GOLDEN_HELMET:slot==EntityEquipmentSlot.CHEST?Items.GOLDEN_CHESTPLATE:slot==EntityEquipmentSlot.LEGS?Items.GOLDEN_LEGGINGS:Items.GOLDEN_BOOTS));
            }
        }
        if(isBrute()) {enablePersistence();home=getPosition();}
        return result;
    }

    @Override
    protected void dropEquipment(boolean recentlyHit,int looting) {
        super.dropEquipment(recentlyHit,looting);
        for(int index=0;index<inventory.getSizeInventory();index++)if(!inventory.getStackInSlot(index).isEmpty())entityDropItem(inventory.getStackInSlot(index),0);
    }

    @Override protected boolean isValidLightLevel() {return world.getLightFor(net.minecraft.world.EnumSkyBlock.BLOCK,getPosition())<=11;}
    @Override protected ResourceLocation getLootTable() {return new ResourceLocation(FarmerFutureDelight.MODID,"entities/"+(isBrute()?"piglin_brute":"piglin"));}
    @Override protected SoundEvent getAmbientSound() {return isBrute()?(getAttackTarget()==null?FFDSounds.PIGLIN_BRUTE_AMBIENT:FFDSounds.PIGLIN_BRUTE_ANGRY):isAdmiring()?FFDSounds.PIGLIN_ADMIRING_ITEM:retreatTicks>0?FFDSounds.PIGLIN_RETREAT:getAttackTarget()==null?FFDSounds.PIGLIN_AMBIENT:FFDSounds.PIGLIN_ANGRY;}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return isBrute()?FFDSounds.PIGLIN_BRUTE_HURT:FFDSounds.PIGLIN_HURT;}
    @Override protected SoundEvent getDeathSound() {return isBrute()?FFDSounds.PIGLIN_BRUTE_DEATH:FFDSounds.PIGLIN_DEATH;}
    @Override protected void playStepSound(BlockPos pos,Block block) {playSound(isBrute()?FFDSounds.PIGLIN_BRUTE_STEP:FFDSounds.PIGLIN_STEP,.15F,1);}

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);compound.setBoolean("IsBaby",isChild());compound.setBoolean("IsImmuneToZombification",dataManager.get(IMMUNE));
        compound.setInteger("TimeInOverworld",timeInOverworld);compound.setBoolean("CannotHunt",cannotHunt);compound.setInteger("AngerTime",angerTicks);
        if(angerTarget!=null)compound.setUniqueId("AngryAt",angerTarget);
        compound.setInteger("AdmireTicks",admireTicks);compound.setInteger("AdmireDisabledTicks",admireDisabledTicks);
        compound.setInteger("HuntCooldown",huntCooldown);compound.setInteger("EatCooldown",eatCooldown);compound.setInteger("DanceTicks",danceTicks);
        compound.setInteger("RideCooldown",rideCooldown);compound.setInteger("RideTicks",rideTicks);
        if(home!=null)compound.setLong("Home",home.toLong());
        NBTTagList contents=new NBTTagList();for(int index=0;index<inventory.getSizeInventory();index++)if(!inventory.getStackInSlot(index).isEmpty()) {
            NBTTagCompound stack=inventory.getStackInSlot(index).writeToNBT(new NBTTagCompound());stack.setByte("Slot",(byte)index);contents.appendTag(stack);
        }compound.setTag("Inventory",contents);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);setBaby(compound.getBoolean("IsBaby"));setImmuneToZombification(compound.getBoolean("IsImmuneToZombification"));
        timeInOverworld=compound.getInteger("TimeInOverworld");cannotHunt=compound.getBoolean("CannotHunt");angerTicks=compound.getInteger("AngerTime");
        angerTarget=compound.hasUniqueId("AngryAt")?compound.getUniqueId("AngryAt"):null;
        admireTicks=compound.getInteger("AdmireTicks");admireDisabledTicks=compound.getInteger("AdmireDisabledTicks");dataManager.set(ADMIRING,admireTicks>0);
        if(compound.hasKey("HuntCooldown"))huntCooldown=compound.getInteger("HuntCooldown");eatCooldown=compound.getInteger("EatCooldown");danceTicks=compound.getInteger("DanceTicks");dataManager.set(DANCING,danceTicks>0);
        if(compound.hasKey("RideCooldown"))rideCooldown=compound.getInteger("RideCooldown");rideTicks=compound.getInteger("RideTicks");
        if(compound.hasKey("Home"))home=BlockPos.fromLong(compound.getLong("Home"));
        inventory.clear();NBTTagList contents=compound.getTagList("Inventory",10);for(int index=0;index<contents.tagCount();index++) {
            NBTTagCompound stack=contents.getCompoundTagAt(index);int slot=stack.getByte("Slot")&255;if(slot<inventory.getSizeInventory())inventory.setInventorySlotContents(slot,new ItemStack(stack));
        }
    }

    private final class BabyRideGoal extends EntityAIBase {
        private Entity carrier;
        private BabyRideGoal() { setMutexBits(3); }
        @Override public boolean shouldExecute() {
            if(!isChild()||isRiding()||isAdmiring()||retreatTicks>0||rideCooldown>0)return false;
            carrier=null;
            for(EntityHoglin hoglin:world.getEntitiesWithinAABB(EntityHoglin.class,getEntityBoundingBox().grow(8),animal->animal.isChild()&&!animal.isZoglin()&&animal.isEntityAlive())) {
                Entity candidate=hoglin;int depth=0;
                while(!candidate.getPassengers().isEmpty()&&depth<3) {candidate=candidate.getPassengers().get(0);depth++;}
                if(depth>=3||candidate==EntityPiglin.this||!(candidate instanceof EntityHoglin||candidate instanceof EntityPiglin))continue;
                if(carrier==null||getDistanceSq(candidate)<getDistanceSq(carrier))carrier=candidate;
            }
            return carrier!=null;
        }
        @Override public boolean shouldContinueExecuting() { return carrier!=null&&carrier.isEntityAlive()&&!isRiding()&&retreatTicks==0&&getDistanceSq(carrier)<144; }
        @Override public void updateTask() {
            if(getDistanceSq(carrier)<=2.25D) {
                if(startRiding(carrier)) {rideTicks=200+rand.nextInt(401);rideCooldown=200+rand.nextInt(601);getNavigator().clearPath();}
            } else getNavigator().tryMoveToEntityLiving(carrier,.8D);
        }
        @Override public void resetTask() { carrier=null; }
    }

    private final class AdmireGoal extends EntityAIBase {
        private AdmireGoal() {setMutexBits(3);}
        @Override public boolean shouldExecute() {return isAdmiring();}
        @Override public void startExecuting() {getNavigator().clearPath();}
        @Override public void updateTask() {rotationPitch=25.0F;}
        @Override public void resetTask() {rotationPitch=0;}
    }

    private final class FleeGoal extends EntityAIBase {
        private Vec3d destination;
        private FleeGoal() {setMutexBits(3);}
        @Override public boolean shouldExecute() {
            Vec3d danger=retreatTicks>0&&retreatTarget!=null?retreatTarget.getPositionVector():repellent==null?null:new Vec3d(repellent);
            if(danger==null)return false;destination=RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityPiglin.this,16,7,danger);return destination!=null;
        }
        @Override public void startExecuting() {setAttackTarget(null);getNavigator().tryMoveToXYZ(destination.x,destination.y,destination.z,1.0D);}
        @Override public boolean shouldContinueExecuting() {return !getNavigator().noPath()&&(retreatTicks>0||repellent!=null);}
    }

    private final class CrossbowGoal extends EntityAIBase {
        private int chargingTicks;
        private int cooldown;
        private CrossbowGoal() {setMutexBits(3);}
        @Override public boolean shouldExecute() {return !isChild()&&!isAdmiring()&&hasCrossbow()&&getAttackTarget()!=null;}
        @Override public boolean shouldContinueExecuting() {return shouldExecute();}
        @Override public void resetTask() {dataManager.set(CHARGING,false);chargingTicks=0;resetActiveHand();}
        @Override public void updateTask() {
            EntityLivingBase target=getAttackTarget();if(target==null)return;
            boolean visible=getEntitySenses().canSee(target);double distance=getDistanceSq(target);
            getLookHelper().setLookPositionWithEntity(target,30,30);
            if(distance>64||!visible)getNavigator().tryMoveToEntityLiving(target,1.0D);else getNavigator().clearPath();
            if(cooldown>0) {--cooldown;return;}
            if(!visible||distance>256)return;
            ItemStack weapon=getHeldItemMainhand();
            if(!isChargingCrossbow()) {setActiveHand(EnumHand.MAIN_HAND);dataManager.set(CHARGING,true);chargingTicks=0;}
            int duration=Math.max(5,ItemCrossbow.getChargeDuration(weapon));
            if(++chargingTicks<duration)return;
            dataManager.set(CHARGING,false);resetActiveHand();cooldown=20+rand.nextInt(20);
            if(weapon.getItem() instanceof ItemCrossbow) {
                ItemCrossbow.loadProjectileForMob(weapon,new ItemStack(Items.ARROW));
                ((ItemCrossbow)weapon.getItem()).fireLoadedProjectiles(world,EntityPiglin.this,weapon,1.6F);
            } else {
                net.minecraft.entity.projectile.EntityTippedArrow arrow=new net.minecraft.entity.projectile.EntityTippedArrow(world,EntityPiglin.this);
                double directionX=target.posX-posX;double directionZ=target.posZ-posZ;
                arrow.shoot(directionX,target.posY+target.height/3.0D-arrow.posY+Math.sqrt(directionX*directionX+directionZ*directionZ)*.2D,directionZ,1.6F,14-world.getDifficulty().getDifficultyId()*4);
                world.spawnEntity(arrow);weapon.damageItem(1,EntityPiglin.this);playSound(FFDSounds.CROSSBOW_SHOOT,1,1);
            }
        }
    }
}
