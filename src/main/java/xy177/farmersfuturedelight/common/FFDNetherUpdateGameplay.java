package xy177.farmersfuturedelight.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDNetherUpdateGameplay {
    private static final java.lang.reflect.Field HOE_MATERIAL =
            net.minecraftforge.fml.relauncher.ReflectionHelper.findField(
                    ItemHoe.class, "toolMaterial", "field_77843_a");
    private FFDNetherUpdateGameplay() {
    }

    public static boolean hoeTarget(IBlockState state) {
        Block block = state.getBlock();
        return state.getMaterial() == Material.LEAVES || block == Blocks.HAY_BLOCK
                || block == Blocks.SPONGE || block == Blocks.NETHER_WART_BLOCK
                || effective(block, FFDBlocks.WARPED_WART_BLOCK)
                || effective(block, FFDBlocks.SHROOMLIGHT)
                || effective(block, FFDBlocks.DRIED_KELP_BLOCK)
                || effective(block, FFDBlocks.TARGET);
    }

    private static boolean effective(Block actual, Block local) {
        return actual == local || actual == FFDItems.effectiveBlock(local);
    }

    private static Item.ToolMaterial hoeMaterial(Item item) {
        try {
            return (Item.ToolMaterial) HOE_MATERIAL.get(item);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static float destroySpeed(float original, ItemStack stack, IBlockState state) {
        return stack.getItem() instanceof ItemHoe && hoeTarget(state)
                ? hoeMaterial(stack.getItem()).getEfficiency() : original;
    }

    public static int enchantability(int original, Item item) {
        return item instanceof ItemHoe ? hoeMaterial(item).getEnchantability() : original;
    }

    public static boolean canEnchant(boolean original, ItemStack stack, Enchantment enchantment) {
        if (stack.getItem() instanceof ItemHoe && (enchantment == Enchantments.EFFICIENCY
                || enchantment == Enchantments.FORTUNE || enchantment == Enchantments.SILK_TOUCH)) {
            return true;
        }
        return original || stack.getItem() == Items.COMPASS
                && enchantment == Enchantments.VANISHING_CURSE;
    }

    public static boolean destroyed(boolean original, ItemStack stack, IBlockState state,
                                    EntityLivingBase user) {
        if (stack.getItem() instanceof ItemHoe && hoeTarget(state)
                && !user.world.isRemote) stack.damageItem(1, user);
        return original;
    }

    public static void knockBack(EntityLivingBase entity, Entity attacker, float strength,
                                 double ratioX, double ratioZ) {
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(entity, attacker,
                strength, ratioX, ratioZ);
        if (event.isCanceled()) return;
        double resistance = MathHelper.clamp(entity.getEntityAttribute(
                SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0.0D, 1.0D);
        double impulse = event.getStrength() * (1.0D - resistance);
        double length = Math.sqrt(event.getRatioX() * event.getRatioX()
                + event.getRatioZ() * event.getRatioZ());
        if (impulse <= 0.0D || length < 1.0E-8D) return;
        entity.isAirBorne = true;
        entity.velocityChanged = true;
        entity.motionX = entity.motionX / 2.0D - event.getRatioX() / length * impulse;
        entity.motionZ = entity.motionZ / 2.0D - event.getRatioZ() / length * impulse;
        if (entity.onGround) entity.motionY = Math.min(0.4D, entity.motionY / 2.0D + impulse);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void bonemeal(BonemealEvent event) {
        if (event.getResult() != Event.Result.DEFAULT || event.getBlock().getBlock() != Blocks.NETHERRACK
                || event.getWorld().getBlockState(event.getPos().up()).isNormalCube()) return;
        Block crimson = FFDItems.effectiveBlock(FFDBlocks.CRIMSON_NYLIUM);
        Block warped = FFDItems.effectiveBlock(FFDBlocks.WARPED_NYLIUM);
        boolean nearCrimson = false;
        boolean nearWarped = false;
        for (BlockPos nearby : BlockPos.getAllInBox(event.getPos().add(-1, -1, -1),
                event.getPos().add(1, 1, 1))) {
            if (!event.getWorld().isBlockLoaded(nearby)) continue;
            Block block = event.getWorld().getBlockState(nearby).getBlock();
            nearCrimson |= crimson != null && block == crimson;
            nearWarped |= warped != null && block == warped;
        }
        if (!nearCrimson && !nearWarped) return;
        if (!event.getWorld().isRemote) {
            Block selected = nearCrimson && nearWarped
                    ? (event.getWorld().rand.nextBoolean() ? warped : crimson)
                    : nearWarped ? warped : crimson;
            event.getWorld().setBlockState(event.getPos(), selected.getDefaultState(), 3);
            event.getWorld().playSound(null, event.getPos(), FFDSounds.BONE_MEAL_USE,
                    SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        event.setResult(Event.Result.ALLOW);
    }

    public static void updateEndermanBlocks() {
        EntityEnderman.setCarriable(Blocks.NETHERRACK, false);
        for (Block local : new Block[] {FFDBlocks.CRIMSON_NYLIUM, FFDBlocks.WARPED_NYLIUM,
                FFDBlocks.CRIMSON_FUNGUS, FFDBlocks.WARPED_FUNGUS,
                FFDBlocks.CRIMSON_ROOTS, FFDBlocks.WARPED_ROOTS}) {
            Block block = FFDItems.effectiveBlock(local);
            if (block != null && block != Blocks.AIR) EntityEnderman.setCarriable(block, true);
        }
    }
}
