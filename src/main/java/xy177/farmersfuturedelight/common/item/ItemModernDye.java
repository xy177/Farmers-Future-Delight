package xy177.farmersfuturedelight.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import xy177.farmersfuturedelight.common.entity.EntityCat;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class ItemModernDye extends Item {
    private final EnumDyeColor color;

    public ItemModernDye(EnumDyeColor color) {
        this.color = color;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player,
                                            EntityLivingBase target, EnumHand hand) {
        if (target instanceof EntitySheep) {
            EntitySheep sheep = (EntitySheep) target;
            if (!sheep.isEntityAlive() || sheep.getFleeceColor() == color || sheep.getSheared()) return false;
            if (!target.world.isRemote) {
                sheep.setFleeceColor(color);
                target.world.playSound(null, target.posX, target.posY, target.posZ,
                        FFDSounds.DYE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                consume(stack, player);
            }
            return true;
        }
        if (target instanceof EntityCat) {
            EntityCat cat = (EntityCat) target;
            if (!cat.isTamed() || !cat.isOwner(player) || cat.getCollarColor() == color) {
                return false;
            }
            if (!target.world.isRemote) {
                cat.setCollarColor(color);
                consume(stack, player);
            }
            return true;
        }
        if (!(target instanceof EntityWolf)) return false;
        EntityWolf wolf = (EntityWolf) target;
        if (!wolf.isTamed() || !wolf.isOwner(player) || wolf.getCollarColor() == color) return false;
        if (!target.world.isRemote) {
            wolf.setCollarColor(color);
            consume(stack, player);
        }
        return true;
    }

    private static void consume(ItemStack stack, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) stack.shrink(1);
    }
}
