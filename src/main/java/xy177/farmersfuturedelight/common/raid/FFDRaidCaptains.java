package xy177.farmersfuturedelight.common.raid;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.entity.EntityRavager;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDRaidCaptains {
    private static final String LEADER_TAG = "farmers_future_delight:patrol_leader";
    private static final String SPAWN_REASON_TAG = "farmers_future_delight:patrol_spawn_reason";
    private static final String COMPONENTS_TAG = "farmers_future_delight:structure_components";
    private static final String DIED_IN_RAID_TAG = "farmers_future_delight:died_in_raid";

    private FFDRaidCaptains() {
    }

    public static boolean isRaider(EntityLivingBase entity) {
        return entity instanceof AbstractIllager || entity instanceof EntityWitch
                || FFDRaidManager.isParticipant(entity);
    }

    public static boolean canLead(EntityLivingBase entity) {
        return entity instanceof AbstractIllager && !(entity instanceof EntityRavager);
    }

    public static void prepareSpawn(EntityLiving entity, String reason) {
        entity.getEntityData().setString(SPAWN_REASON_TAG, reason);
    }

    public static void onInitialSpawn(EntityLiving entity) {
        NBTTagCompound data = entity.getEntityData();
        String reason = data.getString(SPAWN_REASON_TAG);
        data.removeTag(SPAWN_REASON_TAG);
        if (!"patrol".equals(reason) && !"event".equals(reason) && !"structure".equals(reason)
                && canLead(entity) && entity.getRNG().nextFloat() < 0.06F) {
            data.setBoolean(LEADER_TAG, true);
        }
        if (isLeader(entity)) equipBanner(entity);
    }

    public static boolean isLeader(EntityLivingBase entity) {
        return entity.getEntityData().getBoolean(LEADER_TAG);
    }

    public static boolean isCaptain(EntityLivingBase entity) {
        return isLeader(entity) && ItemStack.areItemStacksEqual(
                entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD), createBanner());
    }

    public static boolean hasOminousBanner(EntityLivingBase entity) {
        ItemStack banner = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (banner.getItem() != Items.BANNER
                || banner.getMetadata() != EnumDyeColor.WHITE.getDyeDamage()) return false;
        NBTTagCompound block = banner.getSubCompound("BlockEntityTag");
        NBTTagCompound expected = createBanner().getSubCompound("BlockEntityTag");
        return block != null && expected.getTagList("Patterns", 10).equals(block.getTagList("Patterns", 10))
                && "block.minecraft.ominous_banner".equals(block.getCompoundTag("ForgeData")
                .getCompoundTag(COMPONENTS_TAG).getCompoundTag("minecraft:item_name").getString("translate"));
    }

    public static void setLeader(EntityLiving entity, boolean leader) {
        entity.getEntityData().setBoolean(LEADER_TAG, leader);
        if (leader) equipBanner(entity);
    }

    private static void equipBanner(EntityLiving entity) {
        entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, createBanner());
        entity.setDropChance(EntityEquipmentSlot.HEAD, 2.0F);
    }

    public static ItemStack createBanner() {
        ItemStack banner = new ItemStack(Items.BANNER, 1, EnumDyeColor.WHITE.getDyeDamage());
        NBTTagList patterns = new NBTTagList();
        addPattern(patterns, "mr", EnumDyeColor.CYAN);
        addPattern(patterns, "bs", EnumDyeColor.SILVER);
        addPattern(patterns, "cs", EnumDyeColor.GRAY);
        addPattern(patterns, "bo", EnumDyeColor.SILVER);
        addPattern(patterns, "ms", EnumDyeColor.BLACK);
        addPattern(patterns, "hh", EnumDyeColor.SILVER);
        addPattern(patterns, "mc", EnumDyeColor.SILVER);
        addPattern(patterns, "bo", EnumDyeColor.BLACK);
        NBTTagCompound block = banner.getOrCreateSubCompound("BlockEntityTag");
        block.setTag("Patterns", patterns);
        NBTTagCompound components = new NBTTagCompound();
        NBTTagCompound name = new NBTTagCompound();
        name.setString("translate", "block.minecraft.ominous_banner");
        components.setTag("minecraft:item_name", name);
        components.setString("minecraft:rarity", "uncommon");
        NBTTagCompound tooltip = new NBTTagCompound();
        NBTTagList hidden = new NBTTagList();
        hidden.appendTag(new net.minecraft.nbt.NBTTagString("minecraft:banner_patterns"));
        tooltip.setTag("hidden_components", hidden);
        components.setTag("minecraft:tooltip_display", tooltip);
        NBTTagCompound custom = new NBTTagCompound();
        custom.setTag(COMPONENTS_TAG, components);
        block.setTag("ForgeData", custom);
        return banner;
    }

    private static void addPattern(NBTTagList patterns, String pattern, EnumDyeColor color) {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Pattern", pattern);
        entry.setInteger("Color", color.getDyeDamage());
        patterns.appendTag(entry);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote || !isCaptain(entity) || FFDRaidManager.isParticipant(entity)
                || entity.getEntityData().getBoolean(DIED_IN_RAID_TAG)
                || entity.world instanceof net.minecraft.world.WorldServer
                && FFDRaidManager.hasActiveRaid((net.minecraft.world.WorldServer) entity.world,
                        entity.getPosition())
                || !entity.world.getGameRules().getBoolean("doMobLoot")) return;
        net.minecraft.world.WorldServer world = (net.minecraft.world.WorldServer) entity.world;
        net.minecraft.world.storage.loot.LootContext context =
                xy177.farmersfuturedelight.common.FFDEntityLootTables.deathContext(
                        world, entity, event.getSource(), event.isRecentlyHit());
        for (ItemStack stack : world.getLootTableManager().getLootTableFromLocation(
                xy177.farmersfuturedelight.common.FFDEntityLootTables.RAID_CAPTAIN)
                .generateLootForPools(entity.getRNG(), context)) {
            event.getDrops().add(new net.minecraft.entity.item.EntityItem(world,
                    entity.posX, entity.posY, entity.posZ, stack));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDying(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote) {
            entity.getEntityData().setBoolean(DIED_IN_RAID_TAG, FFDRaidManager.isParticipant(entity));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKilled(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote && isRaider(entity) && hasOminousBanner(entity)
                && event.getSource().getTrueSource() instanceof EntityPlayerMP) {
            FFDAdvancements.VOLUNTARY_EXILE.trigger((EntityPlayerMP) event.getSource().getTrueSource());
        }
    }
}
