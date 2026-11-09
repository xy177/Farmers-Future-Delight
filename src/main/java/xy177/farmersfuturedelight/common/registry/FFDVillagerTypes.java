package xy177.farmersfuturedelight.common.registry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class FFDVillagerTypes {
    public static final ResourceLocation MASON = new ResourceLocation(FarmerFutureDelight.MODID, "mason");
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            "desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga"));
    private static final Map<EntityVillager, NBTTagCompound> LAST_STATE = new WeakHashMap<>();
    public static final String CLIENT_STATE = "farmers_future_delight:villager_client_state";

    private FFDVillagerTypes() {
    }

    @SubscribeEvent
    public static void registerProfessions(RegistryEvent.Register<VillagerProfession> event) {
        VillagerProfession mason = new VillagerProfession(MASON.toString(),
                "minecraft:textures/entity/villager/smith.png",
                "minecraft:textures/entity/zombie_villager/zombie_smith.png");
        new VillagerCareer(mason, "mason");
        event.getRegistry().register(mason);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerJoin(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity().getClass() == EntityVillager.class) {
            EntityVillager villager = (EntityVillager) event.getEntity();
            if (!FFDConfig.modernVillagerSystem) {
                FFDProfessionWorkstations.restoreLegacyState(villager);
                FFDWorkstationLifecycle.clearWorkstation(villager);
            }
            ensureType(villager);
        } else if (!event.getWorld().isRemote && event.getEntity().getClass() == EntityZombieVillager.class) {
            FFDZombieVillagerState.ensureState((EntityZombieVillager) event.getEntity());
        }
    }

    public static boolean isType(String type) {
        return TYPES.contains(type);
    }

    public static String ensureType(EntityVillager villager) {
        NBTTagCompound data = villager.getEntityData();
        String type = data.getString(FFDModernVillagerTrades.VARIANT_TAG);
        if (!TYPES.contains(type)) {
            type = byBiome(villager.world, villager.getPosition());
            data.setString(FFDModernVillagerTrades.VARIANT_TAG, type);
        }
        return type;
    }

    public static void inheritType(EntityVillager child, EntityVillager first, EntityVillager second) {
        double roll = child.getRNG().nextDouble();
        String type = roll < 0.5D ? byBiome(first.world, first.getPosition())
                : ensureType(roll < 0.75D ? first : second);
        child.getEntityData().setString(FFDModernVillagerTrades.VARIANT_TAG, type);
    }

    public static String byBiome(World world, BlockPos position) {
        return byBiome(world.getBiome(position));
    }

    public static String byBiome(Biome biome) {
        if (biome == FFDBiomes.GROVE || biome == FFDBiomes.SNOWY_SLOPES
                || biome == FFDBiomes.JAGGED_PEAKS || biome == FFDBiomes.FROZEN_PEAKS
                || biome == FFDBiomes.FROZEN_OCEAN || biome == FFDBiomes.DEEP_FROZEN_OCEAN) return "snow";
        ResourceLocation id = biome.getRegistryName();
        if (id == null) return "plains";
        String name = id.getResourcePath();
        if (FarmerFutureDelight.MODID.equals(id.getResourceDomain()) && "bamboo_jungle".equals(name)) return "jungle";
        if (!"minecraft".equals(id.getResourceDomain())) return "plains";
        switch (name) {
            case "desert":
            case "desert_hills":
            case "mutated_desert":
            case "mesa":
            case "mesa_rock":
            case "mesa_clear_rock":
            case "mutated_mesa":
            case "mutated_mesa_rock":
            case "mutated_mesa_clear_rock": return "desert";
            case "jungle":
            case "jungle_hills":
            case "jungle_edge":
            case "mutated_jungle":
            case "mutated_jungle_edge": return "jungle";
            case "savanna":
            case "savanna_rock":
            case "mutated_savanna":
            case "mutated_savanna_rock": return "savanna";
            case "frozen_ocean":
            case "frozen_river":
            case "ice_flats":
            case "ice_mountains":
            case "mutated_ice_flats":
            case "cold_beach":
            case "taiga_cold":
            case "taiga_cold_hills":
            case "mutated_taiga_cold": return "snow";
            case "swampland":
            case "mutated_swampland": return "swamp";
            case "taiga":
            case "taiga_hills":
            case "mutated_taiga":
            case "redwood_taiga":
            case "redwood_taiga_hills":
            case "mutated_redwood_taiga":
            case "mutated_redwood_taiga_hills":
            case "extreme_hills":
            case "extreme_hills_with_trees":
            case "smaller_extreme_hills":
            case "mutated_extreme_hills":
            case "mutated_extreme_hills_with_trees": return "taiga";
            default: return "plains";
        }
    }

    @SubscribeEvent
    public static void onTracking(PlayerEvent.StartTracking event) {
        if (!FFDConfig.modernVillagerSystem) return;
        if (event.getTarget().getClass() == EntityVillager.class
                && event.getEntityPlayer() instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.network.FFDNetwork.sendVillagerState(
                    (net.minecraft.entity.player.EntityPlayerMP) event.getEntityPlayer(),
                    (EntityVillager) event.getTarget());
        } else if (event.getTarget().getClass() == EntityZombieVillager.class
                && event.getEntityPlayer() instanceof net.minecraft.entity.player.EntityPlayerMP) {
            xy177.farmersfuturedelight.common.network.FFDNetwork.sendVillagerState(
                    (net.minecraft.entity.player.EntityPlayerMP) event.getEntityPlayer(),
                    (EntityZombieVillager) event.getTarget());
        }
    }

    public static void sync(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem) return;
        NBTTagCompound current = clientState(villager);
        if (current.equals(LAST_STATE.get(villager))) return;
        LAST_STATE.put(villager, current);
        xy177.farmersfuturedelight.common.network.FFDNetwork.sendVillagerState(villager);
    }

    public static void refuseTrade(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || villager.world.isRemote) return;
        villager.getEntityData().setLong("farmers_future_delight:unhappy_until",
                villager.world.getTotalWorldTime() + 40L);
        villager.playSound(net.minecraft.init.SoundEvents.ENTITY_VILLAGER_NO, 1.0F,
                (villager.isChild() ? 1.5F : 1.0F)
                        + (villager.getRNG().nextFloat() - villager.getRNG().nextFloat()) * 0.2F);
        sync(villager);
    }

    public static NBTTagCompound clientState(EntityVillager villager) {
        NBTTagCompound state = new NBTTagCompound();
        state.setString("type", ensureType(villager));
        String profession = FFDProfessionWorkstations.isProfessionUnassigned(villager) ? "none"
                : "minecraft:nitwit".equals(String.valueOf(villager.getProfessionForge().getRegistryName()))
                ? "nitwit" : FFDModernVillagerTrades.canManage(villager)
                ? FFDModernVillagerTrades.getModernProfession(villager) : null;
        state.setBoolean("modern", profession != null);
        state.setString("profession", profession == null ? "none" : profession);
        state.setInteger("level", FFDVillagerRestocking.getLevel(villager));
        state.setBoolean("unassigned", FFDProfessionWorkstations.isProfessionUnassigned(villager));
        state.setString("profession_name", professionNameKey(villager));
        state.setString("forge_profession", String.valueOf(villager.getProfessionForge().getRegistryName()));
        state.setInteger("career", net.minecraftforge.fml.common.ObfuscationReflectionHelper
                .<Integer, EntityVillager>getPrivateValue(EntityVillager.class, villager, "field_175563_bv"));
        state.setInteger("career_level", net.minecraftforge.fml.common.ObfuscationReflectionHelper
                .<Integer, EntityVillager>getPrivateValue(EntityVillager.class, villager, "field_175562_bw"));
        state.setBoolean("sleeping", FFDVillagerHomes.isSleeping(villager));
        net.minecraft.item.ItemStack display = xy177.farmersfuturedelight.common.entity.ai.EntityAIVillagerShowTrades
                .getDisplayedItem(villager);
        if (!display.isEmpty()) state.setTag("trade_display", display.writeToNBT(new NBTTagCompound()));
        state.setLong("unhappy_until", villager.getEntityData().getLong("farmers_future_delight:unhappy_until"));
        BlockPos home = FFDVillagerHomes.getHome(villager);
        if (home != null) state.setLong("home", home.toLong());
        return state;
    }

    public static String professionNameKey(EntityVillager villager) {
        if (villager.world.isRemote) {
            String key = villager.getEntityData().getCompoundTag(CLIENT_STATE).getString("profession_name");
            return key.isEmpty() ? "entity.Villager.name" : key;
        }
        if (FFDProfessionWorkstations.isProfessionUnassigned(villager)) return "ffd.villager.unemployed";
        String career = FFDProfessionWorkstations.getCareerName(villager);
        return career == null ? "entity.Villager.name" : "entity.Villager." + career;
    }

    public static net.minecraft.util.text.ITextComponent displayName(EntityVillager villager) {
        if (!FFDConfig.modernVillagerSystem || villager.getClass() != EntityVillager.class
                || villager.hasCustomName()) return null;
        net.minecraft.util.text.ITextComponent result = new net.minecraft.util.text.TextComponentTranslation(
                professionNameKey(villager));
        NBTTagCompound hover = new NBTTagCompound();
        hover.setString("id", villager.getCachedUniqueIdString());
        hover.setString("name", villager.getName());
        ResourceLocation entityType = net.minecraft.entity.EntityList.getKey(villager);
        if (entityType != null) hover.setString("type", entityType.toString());
        result.getStyle().setHoverEvent(new net.minecraft.util.text.event.HoverEvent(
                net.minecraft.util.text.event.HoverEvent.Action.SHOW_ENTITY,
                new net.minecraft.util.text.TextComponentString(hover.toString())));
        result.getStyle().setInsertion(villager.getCachedUniqueIdString());
        if (villager.getTeam() != null) result.getStyle().setColor(villager.getTeam().getColor());
        return result;
    }

    public static float eyeHeight(float original, EntityVillager villager) {
        return FFDConfig.modernVillagerSystem && FFDVillagerHomes.isSleeping(villager) ? 0.2F : original;
    }

    public static void receiveClientState(EntityVillager villager, NBTTagCompound state) {
        if (!FFDConfig.modernVillagerSystem) {
            villager.getEntityData().removeTag(CLIENT_STATE);
            return;
        }
        VillagerProfession profession = net.minecraftforge.fml.common.registry.ForgeRegistries.VILLAGER_PROFESSIONS
                .getValue(new ResourceLocation(state.getString("forge_profession")));
        if (profession != null && profession != villager.getProfessionForge()) villager.setProfession(profession);
        net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class,
                villager, state.getInteger("career"), "field_175563_bv");
        net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class,
                villager, state.getInteger("career_level"), "field_175562_bw");
        villager.getEntityData().setBoolean("farmers_future_delight:workstation_unassigned", state.getBoolean("unassigned"));
        villager.getEntityData().setTag(CLIENT_STATE, state.copy());
        villager.getEntityData().setLong("farmers_future_delight:unhappy_until", state.getLong("unhappy_until"));
        villager.getEntityData().setBoolean(FFDVillagerHomes.SLEEPING_TAG, state.getBoolean("sleeping"));
        if (state.hasKey("home", 99)) {
            NBTTagCompound home = new NBTTagCompound();
            home.setInteger("dimension", villager.dimension);
            home.setLong("position", state.getLong("home"));
            villager.getEntityData().setTag(FFDVillagerHomes.HOME_TAG, home);
        } else {
            villager.getEntityData().removeTag(FFDVillagerHomes.HOME_TAG);
        }
        FFDVillagerHomes.clientPose(villager);
    }
}
