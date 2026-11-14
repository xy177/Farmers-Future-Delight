package xy177.farmersfuturedelight.common.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.core.FFDCrossbowArrowHooks;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemCrossbow extends Item {
    private static final String CHARGED_PROJECTILES = "ChargedProjectiles";
    private static final String INTANGIBLE_PROJECTILE = "IntangibleProjectile";

    public ItemCrossbow() {
        setMaxStackSize(1);
        setMaxDamage(465);
        addPropertyOverride(new ResourceLocation("charged"),
                (stack, world, entity) -> isCharged(stack) ? 1.0F : 0.0F);
        addPropertyOverride(new ResourceLocation("firework"),
                (stack, world, entity) -> hasChargedFirework(stack) ? 1.0F : 0.0F);
        addPropertyOverride(new ResourceLocation("pulling"),
                (stack, world, entity) -> entity != null && entity.isHandActive()
                        && entity.getActiveItemStack() == stack && !isCharged(stack)
                        ? 1.0F : 0.0F);
        addPropertyOverride(new ResourceLocation("pull"), (stack, world, entity) -> {
            if (entity == null || !entity.isHandActive()
                    || entity.getActiveItemStack() != stack || isCharged(stack)) {
                return 0.0F;
            }
            int elapsed = getMaxItemUseDuration(stack) - entity.getItemInUseCount();
            return Math.min(1.0F, (float) elapsed / getChargeDuration(stack));
        });
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        NBTTagList projectiles = getChargedProjectiles(stack);
        if (projectiles.tagCount() == 0) {
            return;
        }
        ItemStack previous = null;
        int count = 0;
        for (int index = 0; index < projectiles.tagCount(); index++) {
            ItemStack projectile = new ItemStack(projectiles.getCompoundTagAt(index));
            if (projectile.isEmpty()) {
                continue;
            }
            if (previous != null && ItemStack.areItemStacksEqual(previous, projectile)) {
                count++;
            } else {
                if (previous != null) {
                    addProjectileTooltip(tooltip, previous, count);
                }
                previous = projectile;
                count = 1;
            }
        }
        if (previous != null) {
            addProjectileTooltip(tooltip, previous, count);
        }
    }

    private static void addProjectileTooltip(List<String> tooltip, ItemStack projectile, int count) {
        if (count == 1) {
            tooltip.add(TextFormatting.GRAY + net.minecraft.util.text.translation.I18n.translateToLocalFormatted(
                    "item.farmers_future_delight.crossbow.projectile.single",
                    "[" + projectile.getDisplayName() + "]"));
        } else {
            tooltip.add(TextFormatting.GRAY + net.minecraft.util.text.translation.I18n.translateToLocalFormatted(
                    "item.farmers_future_delight.crossbow.projectile.multiple",
                    count, projectile.getDisplayName()));
        }
        if (isIntangibleProjectile(projectile)) {
            tooltip.add(TextFormatting.GRAY + net.minecraft.util.text.translation.I18n.translateToLocal(
                    "item.farmers_future_delight.crossbow.projectile.intangible"));
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == Items.IRON_INGOT || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem()
                && oldStack.getMetadata() == newStack.getMetadata();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                               boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (isCharged(stack)) {
            if (!world.isRemote) {
                shoot(world, player, stack);
            } else if (stack.hasTagCompound()) {
                stack.getTagCompound().removeTag(CHARGED_PROJECTILES);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (findAmmo(player).isEmpty()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase user, int count) {
        if (user.world.isRemote || isCharged(stack)) {
            return;
        }
        int elapsed = getMaxItemUseDuration(stack) - count;
        int duration = getChargeDuration(stack);
        int quickCharge = FFDEnchantments.getQuickCharge(stack);
        if (elapsed == (int) Math.ceil(duration * 0.2D)) {
            net.minecraft.util.SoundEvent sound = quickCharge == 1
                    ? FFDSounds.CROSSBOW_QUICK_CHARGE_1
                    : quickCharge == 2 ? FFDSounds.CROSSBOW_QUICK_CHARGE_2
                    : quickCharge >= 3 ? FFDSounds.CROSSBOW_QUICK_CHARGE_3
                    : FFDSounds.CROSSBOW_LOADING_START;
            user.world.playSound(null, user.posX, user.posY, user.posZ, sound,
                    SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
        if (elapsed == (int) Math.ceil(duration * 0.5D) && quickCharge == 0) {
            user.world.playSound(null, user.posX, user.posY, user.posZ,
                    FFDSounds.CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
        if (elapsed >= duration && loadProjectiles(stack, user)) {
            playSound(user, FFDSounds.CROSSBOW_LOADING_END, 1.0F,
                    1.0F / (user.world.rand.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    private boolean loadProjectiles(ItemStack crossbow, EntityLivingBase user) {
        ItemStack ammo = findAmmo(user);
        if (ammo.isEmpty()) {
            return false;
        }
        ItemStack projectile = ammo.copy();
        projectile.setCount(1);
        if (user instanceof EntityPlayer && ((EntityPlayer) user).capabilities.isCreativeMode) {
            getOrCreateTag(projectile).setBoolean(INTANGIBLE_PROJECTILE, true);
        }
        NBTTagList charged = new NBTTagList();
        charged.appendTag(projectile.writeToNBT(new NBTTagCompound()));
        if (FFDEnchantments.getMultishot(crossbow) > 0) {
            charged.appendTag(projectile.writeToNBT(new NBTTagCompound()));
            charged.appendTag(projectile.writeToNBT(new NBTTagCompound()));
        }
        getOrCreateTag(crossbow).setTag(CHARGED_PROJECTILES, charged);
        if (!(user instanceof EntityPlayer)
                || !((EntityPlayer) user).capabilities.isCreativeMode) {
            ammo.shrink(1);
            if (ammo.isEmpty() && user instanceof EntityPlayer) {
                ((EntityPlayer) user).inventory.deleteStack(ammo);
            }
        }
        return true;
    }

    public static void loadProjectileForMob(ItemStack crossbow, ItemStack projectile) {
        if (crossbow.isEmpty() || projectile.isEmpty()) {
            return;
        }
        ItemStack loaded = projectile.copy();
        loaded.setCount(1);
        NBTTagList charged = new NBTTagList();
        charged.appendTag(loaded.writeToNBT(new NBTTagCompound()));
        if (FFDEnchantments.getMultishot(crossbow) > 0) {
            charged.appendTag(loaded.writeToNBT(new NBTTagCompound()));
            charged.appendTag(loaded.writeToNBT(new NBTTagCompound()));
        }
        getOrCreateTag(crossbow).setTag(CHARGED_PROJECTILES, charged);
    }

    public void fireLoadedProjectiles(World world, EntityLivingBase shooter,
                                      ItemStack crossbow) {
        shoot(world, shooter, crossbow, 3.15F);
    }

    public void fireLoadedProjectiles(World world, EntityLivingBase shooter,
                                      ItemStack crossbow, float arrowPower) {
        shoot(world, shooter, crossbow, arrowPower);
    }

    private void shoot(World world, EntityLivingBase shooter, ItemStack crossbow) {
        shoot(world, shooter, crossbow, 3.15F);
    }

    private void shoot(World world, EntityLivingBase shooter, ItemStack crossbow,
                       float arrowPower) {
        NBTTagList charged = getChargedProjectiles(crossbow);
        if (charged.tagCount() == 0) {
            return;
        }
        NBTTagList projectiles = new NBTTagList();
        for (int index = 0; index < charged.tagCount(); index++) {
            projectiles.appendTag(charged.getCompoundTagAt(index).copy());
        }
        getOrCreateTag(crossbow).removeTag(CHARGED_PROJECTILES);
        float power = hasFirework(projectiles) ? 1.6F : arrowPower;
        for (int index = 0; index < projectiles.tagCount(); index++) {
            ItemStack projectile = new ItemStack(projectiles.getCompoundTagAt(index));
            boolean intangible = isIntangibleProjectile(projectile);
            if (intangible) {
                projectile = projectile.copy();
                NBTTagCompound projectileTag = projectile.getTagCompound();
                projectileTag.removeTag(INTANGIBLE_PROJECTILE);
                if (projectileTag.hasNoTags()) {
                    projectile.setTagCompound(null);
                }
            }
            float spread = projectiles.tagCount() == 3
                    ? index == 0 ? 0.0F : index == 1 ? -10.0F : 10.0F : 0.0F;
            Vec3d direction = shooter instanceof xy177.farmersfuturedelight.common.entity.EntityPillager
                    || shooter instanceof xy177.farmersfuturedelight.common.entity.EntityPiglin
                    ? getPillagerShotDirection((net.minecraft.entity.EntityLiving) shooter,
                            spread, projectile.getItem() == Items.FIREWORKS)
                    : getShotDirection(shooter, spread);
            if (projectile.getItem() == Items.FIREWORKS) {
                shootFirework(world, shooter, projectile, power, direction);
            } else {
                shootArrow(world, shooter, projectile, crossbow, power, direction,
                        projectiles.tagCount() == 3 && index != 0, intangible);
            }
            crossbow.damageItem(projectile.getItem() == Items.FIREWORKS ? 3 : 1, shooter);
            playSound(shooter, FFDSounds.CROSSBOW_SHOOT, 1.0F,
                    getShotPitch(world, index));
            if (crossbow.isEmpty()) {
                break;
            }
        }
        if (shooter instanceof EntityPlayer) {
            ((EntityPlayer) shooter).addStat(StatList.getObjectUseStats(this));
        }
        if (shooter instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.advancement.FFDAdvancements.SHOT_CROSSBOW.trigger(
                    (net.minecraft.entity.player.EntityPlayerMP) shooter);
        }
    }

    private void shootArrow(World world, EntityLivingBase shooter, ItemStack projectile,
                            ItemStack crossbow, float power, Vec3d direction,
                            boolean multishotSideArrow, boolean intangible) {
        EntityArrow arrow = ((ItemArrow) projectile.getItem()).createArrow(world, projectile, shooter);
        if (arrow == null) {
            return;
        }
        FFDCrossbowArrowHooks.configure(arrow, FFDEnchantments.getPiercing(crossbow));
        arrow.setDamage(2.0D);
        arrow.setIsCritical(shooter instanceof EntityPlayer);
        float uncertainty = shooter instanceof xy177.farmersfuturedelight.common.entity.EntityPillager
                ? 14 - world.getDifficulty().getDifficultyId() * 4 : 1.0F;
        arrow.shoot(direction.x, direction.y, direction.z, power, uncertainty);
        if (multishotSideArrow || intangible) {
            arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
        } else if (!(shooter instanceof EntityPlayer)
                || !((EntityPlayer) shooter).capabilities.isCreativeMode) {
            arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
        } else {
            arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
        }
        world.spawnEntity(arrow);
    }

    private void shootFirework(World world, EntityLivingBase shooter, ItemStack projectile,
                               float power, Vec3d direction) {
        EntityFireworkRocket rocket = new EntityFireworkRocket(world, shooter.posX,
                shooter.posY + shooter.getEyeHeight() - 0.15D, shooter.posZ, projectile);
        xy177.farmersfuturedelight.core.FFDFireworkHooks.setShooter(rocket, shooter);
        xy177.farmersfuturedelight.core.FFDFireworkHooks.setShotAtAngle(rocket, true);
        rocket.motionX = direction.x * power;
        rocket.motionY = direction.y * power;
        rocket.motionZ = direction.z * power;
        world.spawnEntity(rocket);
    }

    private static Vec3d getShotDirection(EntityLivingBase shooter, float angleDegrees) {
        double yaw = Math.toRadians(shooter.rotationYaw);
        double pitch = Math.toRadians(shooter.rotationPitch);
        double cosPitch = Math.cos(pitch);
        double directionX = -Math.sin(yaw) * cosPitch;
        double directionY = -Math.sin(pitch);
        double directionZ = Math.cos(yaw) * cosPitch;
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);
        double axisX = -rightZ * directionY;
        double axisY = rightZ * directionX - rightX * directionZ;
        double axisZ = rightX * directionY;
        double axisLength = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        if (axisLength > 0.0D) {
            axisX /= axisLength;
            axisY /= axisLength;
            axisZ /= axisLength;
        }
        double angle = Math.toRadians(angleDegrees);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double crossX = axisY * directionZ - axisZ * directionY;
        double crossY = axisZ * directionX - axisX * directionZ;
        double crossZ = axisX * directionY - axisY * directionX;
        return new Vec3d(directionX * cosAngle + crossX * sinAngle,
                directionY * cosAngle + crossY * sinAngle,
                directionZ * cosAngle + crossZ * sinAngle);
    }

    private static Vec3d getPillagerShotDirection(
            net.minecraft.entity.EntityLiving shooter,
            float angleDegrees, boolean firework) {
        EntityLivingBase target = shooter.getAttackTarget();
        if (target == null) {
            return getShotDirection(shooter, angleDegrees);
        }
        double differenceX = target.posX - shooter.posX;
        double differenceZ = target.posZ - shooter.posZ;
        double distance = Math.sqrt(differenceX * differenceX + differenceZ * differenceZ);
        double projectileY = shooter.posY + shooter.getEyeHeight() - (firework ? 0.15D : 0.1D);
        Vec3d direction = new Vec3d(differenceX,
                target.posY + target.height / 3.0D - projectileY + distance * 0.2D,
                differenceZ).normalize();
        Vec3d right = direction.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
        if (right.lengthSquared() <= 1.0E-7D) {
            Vec3d view = shooter.getLookVec();
            double yaw = Math.toRadians(shooter.rotationYaw);
            Vec3d viewRight = new Vec3d(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
            right = direction.crossProduct(viewRight.crossProduct(view).normalize());
        }
        Vec3d axis = direction.crossProduct(right.normalize()).normalize();
        double angle = Math.toRadians(angleDegrees);
        double cosine = Math.cos(angle);
        return direction.scale(cosine).add(axis.crossProduct(direction).scale(Math.sin(angle)))
                .add(axis.scale(axis.dotProduct(direction) * (1.0D - cosine)));
    }

    private static float getShotPitch(World world, int index) {
        if (index == 0) {
            return 1.0F;
        }
        float pitchOffset = (index & 1) == 1 ? 0.63F : 0.43F;
        return 1.0F / (world.rand.nextFloat() * 0.5F + 1.8F) + pitchOffset;
    }

    private ItemStack findAmmo(EntityLivingBase user) {
        ItemStack offhand = user.getHeldItem(EnumHand.OFF_HAND);
        if (isProjectile(offhand)) {
            return offhand;
        }
        ItemStack mainhand = user.getHeldItem(EnumHand.MAIN_HAND);
        if (isProjectile(mainhand)) {
            return mainhand;
        }
        if (user instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) user;
            for (int index = 0; index < player.inventory.getSizeInventory(); index++) {
                ItemStack stack = player.inventory.getStackInSlot(index);
                if (isProjectile(stack)) {
                    return stack;
                }
            }
            if (player.capabilities.isCreativeMode) {
                return new ItemStack(Items.ARROW);
            }
        } else if (user instanceof xy177.farmersfuturedelight.common.entity.EntityPillager) {
            return new ItemStack(Items.ARROW);
        }
        return ItemStack.EMPTY;
    }

    private boolean isProjectile(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemArrow
                || stack.getItem() == Items.FIREWORKS);
    }

    private static NBTTagList getChargedProjectiles(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(CHARGED_PROJECTILES, 9)
                ? tag.getTagList(CHARGED_PROJECTILES, 10) : new NBTTagList();
    }

    private static boolean isIntangibleProjectile(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(INTANGIBLE_PROJECTILE);
    }

    public static boolean isCharged(ItemStack stack) {
        return getChargedProjectiles(stack).tagCount() > 0;
    }

    private static boolean hasFirework(NBTTagList projectiles) {
        for (int index = 0; index < projectiles.tagCount(); index++) {
            if (new ItemStack(projectiles.getCompoundTagAt(index)).getItem() == Items.FIREWORKS) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasChargedFirework(ItemStack stack) {
        return hasFirework(getChargedProjectiles(stack));
    }

    public static int getChargeDuration(ItemStack stack) {
        return Math.max(1, 25 - 5 * FFDEnchantments.getQuickCharge(stack));
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    private static void playSound(EntityLivingBase entity,
                                  net.minecraft.util.SoundEvent sound,
                                  float volume, float pitch) {
        entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, sound,
                entity.getSoundCategory(), volume, pitch);
    }
}
