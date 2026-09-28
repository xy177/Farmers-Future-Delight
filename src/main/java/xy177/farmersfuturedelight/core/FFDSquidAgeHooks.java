package xy177.farmersfuturedelight.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;

public final class FFDSquidAgeHooks {
    private static final DataParameter<Boolean> CHILD =
            EntityDataManager.createKey(EntitySquid.class, DataSerializers.BOOLEAN);
    private static final String AGE = "ffdSquidAge";

    private FFDSquidAgeHooks() {
    }

    public static void initialize() {
    }

    public static void register(EntitySquid squid) {
        squid.getDataManager().register(CHILD, false);
    }

    public static boolean isChild(EntitySquid squid) {
        return squid.getClass() == EntitySquid.class && squid.getDataManager().get(CHILD);
    }

    public static boolean ageChanged(EntitySquid squid, DataParameter<?> key) {
        return squid.getClass() == EntitySquid.class && CHILD.equals(key);
    }

    public static float size(EntitySquid squid) {
        return isChild(squid) ? 0.5F : 0.8F;
    }

    public static float eyeHeight(float original, EntitySquid squid) {
        return (squid.getClass() == EntitySquid.class || squid.getClass() == EntityGlowSquid.class)
                && squid.isChild() ? 0.37F : original;
    }

    public static int getAge(EntitySquid squid) {
        return squid.world.isRemote ? (isChild(squid) ? -1 : 0)
                : squid.getEntityData().getInteger(AGE);
    }

    public static void setAge(EntitySquid squid, int age) {
        squid.getEntityData().setInteger(AGE, age);
        squid.getDataManager().set(CHILD, age < 0);
    }

    public static void tick(EntitySquid squid) {
        if (squid.getClass() != EntitySquid.class || squid.world.isRemote
                || !squid.isEntityAlive()) {
            return;
        }
        int age = getAge(squid);
        if (age != 0) {
            setAge(squid, age + (age < 0 ? 1 : -1));
        }
    }

    public static void write(EntitySquid squid, NBTTagCompound tag) {
        if (squid.getClass() == EntitySquid.class) {
            tag.setInteger("Age", getAge(squid));
        }
    }

    public static void read(EntitySquid squid, NBTTagCompound tag) {
        if (squid.getClass() == EntitySquid.class) {
            setAge(squid, tag.hasKey("Age", 99) ? tag.getInteger("Age")
                    : tag.getBoolean("IsBaby") ? -24000 : 0);
        }
    }

    public static IEntityLivingData spawn(IEntityLivingData data, EntitySquid squid) {
        if (squid.getClass() != EntitySquid.class) {
            return data;
        }
        GroupData group = data instanceof GroupData ? (GroupData) data : new GroupData();
        if (group.count > 0 && squid.getRNG().nextFloat() <= 0.05F) {
            setAge(squid, -24000);
        }
        group.count++;
        return group;
    }

    public static boolean interact(EntitySquid parent, EntityPlayer player, EnumHand hand) {
        if (parent.getClass() != EntitySquid.class
                && parent.getClass() != EntityGlowSquid.class) {
            return false;
        }
        ItemStack egg = player.getHeldItem(hand);
        ResourceLocation type = EntityList.getKey(parent);
        if (egg.getItem() != Items.SPAWN_EGG || type == null
                || !type.equals(ItemMonsterPlacer.getNamedIdFrom(egg))) {
            return false;
        }
        if (!parent.world.isRemote) {
            Entity child = EntityList.createEntityByIDFromName(type, parent.world);
            if (child instanceof EntityGlowSquid) {
                ((EntityGlowSquid) child).setGrowingAge(-24000);
            } else if (child != null && child.getClass() == EntitySquid.class) {
                setAge((EntitySquid) child, -24000);
            } else {
                return false;
            }
            child.setLocationAndAngles(parent.posX, parent.posY, parent.posZ, 0.0F, 0.0F);
            if (egg.hasDisplayName()) {
                child.setCustomNameTag(egg.getDisplayName());
            }
            ItemMonsterPlacer.applyItemEntityDataToEntity(parent.world, player, egg, child);
            if (parent.world.spawnEntity(child) && !player.capabilities.isCreativeMode) {
                egg.shrink(1);
            }
        }
        return true;
    }

    private static final class GroupData implements IEntityLivingData {
        private int count;
    }
}
