package xy177.farmersfuturedelight.common.item;

import com.google.common.collect.Multimap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.MoverType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.FFDRiptide;
import xy177.farmersfuturedelight.common.entity.EntityTrident;
import xy177.farmersfuturedelight.common.registry.FFDEnchantments;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemTrident extends Item {
    public ItemTrident() {
        setMaxDamage(250);
        setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation("throwing"),
                (stack, world, entity) -> entity != null && entity.isHandActive()
                        && entity.getActiveItemStack() == stack ? 1.0F : 0.0F);
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItemDamage() >= stack.getMaxDamage() - 1
                || FFDEnchantments.getRiptide(stack) > 0 && !player.isWet()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity,
                                     int timeLeft) {
        if (!(entity instanceof EntityPlayer)
                || getMaxItemUseDuration(stack) - timeLeft < 10) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        int riptide = FFDEnchantments.getRiptide(stack);
        if (riptide > 0 && (!player.isWet() || player.isRiding())) {
            return;
        }
        if (!world.isRemote) {
            stack.damageItem(1, player);
            if (riptide == 0) {
                EntityTrident trident = new EntityTrident(world, player, stack);
                trident.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F,
                        2.5F, 1.0F);
                trident.pickupStatus = player.capabilities.isCreativeMode
                        ? net.minecraft.entity.projectile.EntityArrow.PickupStatus.CREATIVE_ONLY
                        : net.minecraft.entity.projectile.EntityArrow.PickupStatus.ALLOWED;
                world.spawnEntity(trident);
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
            }
        }
        net.minecraft.util.SoundEvent sound = FFDSounds.TRIDENT_THROW;
        if (riptide > 0) {
            float yaw = player.rotationYaw * 0.017453292F;
            float pitch = player.rotationPitch * 0.017453292F;
            float x = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
            float y = -MathHelper.sin(pitch);
            float z = MathHelper.cos(yaw) * MathHelper.cos(pitch);
            float length = MathHelper.sqrt(x * x + y * y + z * z);
            float speed = 3.0F * ((1.0F + riptide) / 4.0F);
            player.addVelocity(x * speed / length, y * speed / length, z * speed / length);
            FFDRiptide.start(player, 20);
            if (player.onGround) {
                player.move(MoverType.SELF, 0.0D, 1.1999999D, 0.0D);
            }
            sound = riptide >= 3 ? FFDSounds.TRIDENT_RIPTIDE_3
                    : riptide == 2 ? FFDSounds.TRIDENT_RIPTIDE_2
                    : FFDSounds.TRIDENT_RIPTIDE_1;
        }
        player.addStat(StatList.getObjectUseStats(this));
        world.playSound(null, player.posX, player.posY, player.posZ, sound,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target,
                             EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world,
                                    net.minecraft.block.state.IBlockState state,
                                    net.minecraft.util.math.BlockPos pos,
                                    EntityLivingBase entity) {
        if (state.getBlockHardness(world, pos) != 0.0F) {
            stack.damageItem(2, entity);
        }
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> modifiers = super.getItemAttributeModifiers(slot);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 8.0D, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.9D, 0));
        }
        return modifiers;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }
}
