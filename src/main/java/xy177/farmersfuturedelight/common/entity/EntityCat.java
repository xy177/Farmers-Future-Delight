package xy177.farmersfuturedelight.common.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.entity.ai.EntityAICatRelaxOnOwner;

public class EntityCat extends EntityOcelot {
    private static final DataParameter<Integer> COLLAR_COLOR = EntityDataManager.createKey(
            EntityCat.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> LYING_WITH_OWNER = EntityDataManager.createKey(
            EntityCat.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RELAX_STATE_ONE = EntityDataManager.createKey(
            EntityCat.class, DataSerializers.BOOLEAN);
    private static final String[] COMMON_VARIANTS = {
            "tabby", "black", "red", "siamese", "british_shorthair", "calico",
            "persian", "ragdoll", "white", "jellie"
    };
    private float lieDownAmount;
    private float previousLieDownAmount;
    private float lieDownTailAmount;
    private float previousLieDownTailAmount;
    private float relaxStateOneAmount;
    private float previousRelaxStateOneAmount;

    public EntityCat(World world) {
        super(world);
        tasks.addTask(3, new EntityAICatRelaxOnOwner(this));
    }

    @Override
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return xy177.farmersfuturedelight.common.FFDEntityLootTables.CAT;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(COLLAR_COLOR, EnumDyeColor.RED.getDyeDamage());
        dataManager.register(LYING_WITH_OWNER, false);
        dataManager.register(RELAX_STATE_ONE, false);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
                                             IEntityLivingData livingData) {
        IEntityLivingData result = super.onInitialSpawn(difficulty, livingData);
        if (isWitchHutPosition()) {
            setTameSkin(COMMON_VARIANTS.length);
        } else {
            int variantCount = COMMON_VARIANTS.length
                    + (world.getCurrentMoonPhaseFactor() >= 0.9F ? 1 : 0);
            setTameSkin(rand.nextInt(variantCount));
        }
        return result;
    }

    @Override
    public void setTameSkin(int variant) {
        super.setTameSkin(Math.max(0, Math.min(COMMON_VARIANTS.length, variant)));
    }

    public String getVariantName() {
        int variant = getTameSkin();
        return variant == COMMON_VARIANTS.length ? "all_black"
                : COMMON_VARIANTS[Math.max(0, Math.min(COMMON_VARIANTS.length - 1, variant))];
    }

    public EnumDyeColor getCollarColor() {
        return EnumDyeColor.byDyeDamage(dataManager.get(COLLAR_COLOR));
    }

    public void setCollarColor(EnumDyeColor color) {
        dataManager.set(COLLAR_COLOR, color.getDyeDamage());
    }

    public boolean isLyingWithOwner() {
        return dataManager.get(LYING_WITH_OWNER);
    }

    public void setLyingWithOwner(boolean lying) {
        dataManager.set(LYING_WITH_OWNER, lying);
    }

    public boolean isRelaxStateOne() {
        return dataManager.get(RELAX_STATE_ONE);
    }

    public void setRelaxStateOne(boolean relaxing) {
        dataManager.set(RELAX_STATE_ONE, relaxing);
    }

    public float getLieDownAmount(float partialTicks) {
        return previousLieDownAmount
                + (lieDownAmount - previousLieDownAmount) * partialTicks;
    }

    public float getLieDownTailAmount(float partialTicks) {
        return previousLieDownTailAmount
                + (lieDownTailAmount - previousLieDownTailAmount) * partialTicks;
    }

    public float getRelaxStateOneAmount(float partialTicks) {
        return previousRelaxStateOneAmount
                + (relaxStateOneAmount - previousRelaxStateOneAmount) * partialTicks;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        previousLieDownAmount = lieDownAmount;
        previousLieDownTailAmount = lieDownTailAmount;
        previousRelaxStateOneAmount = relaxStateOneAmount;
        lieDownAmount = approach(lieDownAmount, isLyingWithOwner() ? 1.0F : 0.0F,
                isLyingWithOwner() ? 0.15F : 0.22F);
        lieDownTailAmount = approach(lieDownTailAmount,
                isLyingWithOwner() ? 1.0F : 0.0F, isLyingWithOwner() ? 0.08F : 0.13F);
        relaxStateOneAmount = approach(relaxStateOneAmount,
                isRelaxStateOne() ? 1.0F : 0.0F, isRelaxStateOne() ? 0.1F : 0.13F);
    }

    @Override
    public boolean isSitting() {
        return super.isSitting();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!isTamed() && isBreedingItem(held)) {
            if (!world.isRemote) {
                if (!player.capabilities.isCreativeMode) held.shrink(1);
                boolean tamed = rand.nextInt(3) == 0
                        && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player);
                if (tamed) {
                    setTamedBy(player);
                    aiSit.setSitting(true);
                    getNavigator().clearPath();
                    if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                        xy177.farmersfuturedelight.common.advancement.FFDAdvancements.tamedCat(
                                (net.minecraft.entity.player.EntityPlayerMP) player, getVariantName());
                    }
                }
                world.setEntityState(this, (byte) (tamed ? 7 : 6));
                enablePersistence();
            }
            return true;
        }
        if (isTamed() && isOwner(player) && isBreedingItem(held)
                && getHealth() < getMaxHealth()) {
            if (!world.isRemote) {
                heal(2.0F);
                if (!player.capabilities.isCreativeMode) held.shrink(1);
            }
            return true;
        }
        if (isTamed() && isOwner(player) && held.getItem() == Items.DYE) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(15 - held.getMetadata());
            if (color != getCollarColor()) {
                setCollarColor(color);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                return true;
            }
        }
        int variant = getTameSkin();
        boolean wasTamed = isTamed();
        boolean interacted = super.processInteract(player, hand);
        setTameSkin(variant);
        if (!world.isRemote && !wasTamed && isTamed() && isOwner(player)
                && player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.advancement.FFDAdvancements.tamedCat(
                    (net.minecraft.entity.player.EntityPlayerMP) player, getVariantName());
        }
        return interacted;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH
                && (stack.getMetadata() == 0 || stack.getMetadata() == 1);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("CollarColor", (byte) getCollarColor().getDyeDamage());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        int color = compound.hasKey("CollarColor", 99)
                ? compound.getByte("CollarColor") & 255
                : EnumDyeColor.RED.getDyeDamage();
        setCollarColor(EnumDyeColor.byDyeDamage(color));
    }

    @Override
    public EntityCat createChild(EntityAgeable mate) {
        if (!(mate instanceof EntityCat)) {
            return null;
        }
        EntityCat partner = (EntityCat) mate;
        EntityCat child = new EntityCat(world);
        EntityOcelot selected = rand.nextBoolean() ? this : partner;
        child.setTameSkin(selected.getTameSkin());
        if (isTamed()) {
            child.setOwnerId(getOwnerId());
            child.setTamed(true);
            child.setCollarColor(mixCollarColors(getCollarColor(), partner.getCollarColor()));
        }
        return child;
    }

    @Override
    public boolean canMateWith(net.minecraft.entity.passive.EntityAnimal otherAnimal) {
        return isTamed() && otherAnimal instanceof EntityCat
                && ((EntityCat) otherAnimal).isTamed() && super.canMateWith(otherAnimal);
    }

    private static float approach(float value, float target, float step) {
        if (value < target) {
            return Math.min(target, value + step);
        }
        return Math.max(target, value - step);
    }

    private EnumDyeColor mixCollarColors(EnumDyeColor first, EnumDyeColor second) {
        InventoryCrafting inventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return false;
            }
        }, 2, 1);
        inventory.setInventorySlotContents(0, dyeStack(first));
        inventory.setInventorySlotContents(1, dyeStack(second));
        ItemStack result = CraftingManager.findMatchingResult(inventory, world);
        EnumDyeColor mixed = getDyeColor(result);
        return mixed == null ? (rand.nextBoolean() ? first : second) : mixed;
    }

    private ItemStack dyeStack(EnumDyeColor color) {
        switch (color) {
            case WHITE:
                return new ItemStack(FFDItems.WHITE_DYE);
            case BLACK:
                return new ItemStack(FFDItems.BLACK_DYE);
            case BLUE:
                return new ItemStack(FFDItems.BLUE_DYE);
            case BROWN:
                return new ItemStack(FFDItems.BROWN_DYE);
            default:
                return new ItemStack(Items.DYE, 1, 15 - color.getDyeDamage());
        }
    }

    private EnumDyeColor getDyeColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() == FFDItems.WHITE_DYE) return EnumDyeColor.WHITE;
        if (stack.getItem() == FFDItems.BLACK_DYE) return EnumDyeColor.BLACK;
        if (stack.getItem() == FFDItems.BLUE_DYE) return EnumDyeColor.BLUE;
        if (stack.getItem() == FFDItems.BROWN_DYE) return EnumDyeColor.BROWN;
        if (stack.getItem() == Items.DYE) {
            return EnumDyeColor.byDyeDamage(15 - stack.getMetadata());
        }
        return null;
    }

    private boolean isWitchHutPosition() {
        if (!(world instanceof net.minecraft.world.WorldServer)
                || world.getBiome(getPosition()) != Biomes.SWAMPLAND) {
            return false;
        }
        return ((net.minecraft.world.WorldServer) world).getChunkProvider()
                .isInsideStructure(world, "Temple", getPosition());
    }
}
