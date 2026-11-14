package xy177.farmersfuturedelight;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.FFDAxolotlSpawner;
import xy177.farmersfuturedelight.common.FFDDispenserBehaviors;
import xy177.farmersfuturedelight.common.FFDWorldGenerator;
import xy177.farmersfuturedelight.core.FFDGameplayHooks;
import xy177.farmersfuturedelight.common.advancement.FFDAdvancements;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDRecipes;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDCustomDeepslateOres;
import xy177.farmersfuturedelight.common.registry.FFDCustomStrippedWoods;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;
import xy177.farmersfuturedelight.common.registry.FFDRawOreCompat;
import xy177.farmersfuturedelight.common.registry.FFDRawOreOreDictionaryCompat;
import xy177.farmersfuturedelight.common.registry.FFDTileEntities;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.common.world.FFDWorldTypes;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.fluid.FFDFluidloggedData;
import xy177.farmersfuturedelight.common.worldgen.FFDOceanStructures;
import xy177.farmersfuturedelight.common.command.CommandLocateBiome;
import xy177.farmersfuturedelight.common.command.CommandTeamMsg;
import xy177.farmersfuturedelight.common.command.CommandSpectate;
import xy177.farmersfuturedelight.common.command.CommandCompatibility;
import xy177.farmersfuturedelight.common.command.CommandRawOre;
import xy177.farmersfuturedelight.common.command.CommandStrippedWood;
import xy177.farmersfuturedelight.core.FFDCoreCompat;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.proxy.CommonProxy;

@Mod(modid = FarmerFutureDelight.MODID, name = FarmerFutureDelight.NAME, version = FarmerFutureDelight.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "before:jei")
public class FarmerFutureDelight {
    public static final String MODID = "farmers_future_delight";
    public static final String NAME = "Farmer's Future Delight";
    public static final String VERSION = "1.6.0";

    @Mod.Instance(MODID)
    public static FarmerFutureDelight instance;

    @SidedProxy(clientSide = "xy177.farmersfuturedelight.proxy.ClientProxy",
            serverSide = "xy177.farmersfuturedelight.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (net.minecraftforge.fml.common.Loader.isModLoaded("crafttweaker")) {
            xy177.farmersfuturedelight.common.registry.FFDFutureScriptCompat.register();
        }
        FFDWorldTypes.register();
        xy177.farmersfuturedelight.common.FFDBlockLootTables.registerConditions();
        xy177.farmersfuturedelight.common.FFDFishing.register();
        FFDConfig.load(event.getSuggestedConfigurationFile());
        xy177.farmersfuturedelight.common.registry.FFDWorkstationDefaults.configure(
                event.getSuggestedConfigurationFile().getParentFile());
        xy177.farmersfuturedelight.common.raid.RaidEventConfiguration.configure(
                event.getSuggestedConfigurationFile().getParentFile());
        xy177.farmersfuturedelight.common.raid.RaiderSpawnConfiguration.configure(
                event.getSuggestedConfigurationFile().getParentFile());
        FFDCustomRawOres.load(event.getSuggestedConfigurationFile().getParentFile());
        FFDCustomDeepslateOres.load(event.getSuggestedConfigurationFile().getParentFile());
        FFDCustomStrippedWoods.load(event.getSuggestedConfigurationFile().getParentFile());
        FMLCommonHandler.instance().bus().register(FFDAxolotlSpawner.class);
        FFDGameplayHooks.applyModernBlockProperties();
        FFDNetwork.init();
        xy177.farmersfuturedelight.common.registry.FFDStats.init();
        xy177.farmersfuturedelight.common.command.FFDForcedChunks.register();
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(this,
                new xy177.farmersfuturedelight.common.network.FFDGuiHandler());
        FFDAdvancements.init();
        xy177.farmersfuturedelight.common.advancement.FFDCookingRecipeAdvancements.init();
        FFDTileEntities.register();
        FFDOceanStructures.init();
        xy177.farmersfuturedelight.common.FFDEntityLootTables.register();
        MinecraftForge.EVENT_BUS.register(FFDVerticalBiomeManager.class);
        MinecraftForge.EVENT_BUS.register(FFDFluidloggedData.class);
        MinecraftForge.EVENT_BUS.register(xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.class);
        MinecraftForge.EVENT_BUS.register(xy177.farmersfuturedelight.common.fluid.FFDFluidMigration.class);
        MinecraftForge.EVENT_BUS.register(
                xy177.farmersfuturedelight.common.scheduler.FFDFunctionScheduler.class);
        MinecraftForge.TERRAIN_GEN_BUS.register(xy177.farmersfuturedelight.common.worldgen.FFDWorldgenEvents.class);
        MinecraftForge.ORE_GEN_BUS.register(xy177.farmersfuturedelight.common.worldgen.FFDWorldgenEvents.class);
        proxy.preInit(event);
        GameRegistry.registerWorldGenerator(new FFDWorldGenerator(), 0);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        WaterloggedBlockApi.initializeUniversalStateMappings();
        FFDCoreCompat.validateCaveBiomesHeightRange(
                FFDHeightHooks.MIN_Y, FFDHeightHooks.MAX_Y_EXCLUSIVE);
        FFDCoreCompat.validateDepthsUpdateHeightRange(
                FFDHeightHooks.MIN_Y, FFDHeightHooks.MAX_Y_EXCLUSIVE);
        proxy.init();
        FFDDispenserBehaviors.register();
        FFDRecipes.init();
        FFDEntities.registerSpawns();
    }

    @Mod.EventHandler
    public void idMapping(FMLModIdMappingEvent event) {
        WaterloggedBlockApi.initializeUniversalStateMappings();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        xy177.farmersfuturedelight.common.FFDNetherUpdateGameplay.updateEndermanBlocks();
        xy177.farmersfuturedelight.common.raid.RaiderSpawnConfiguration.load();
        MinecraftForge.EVENT_BUS.post(
                new xy177.farmersfuturedelight.api.biome.VerticalBiomeRegistrationEvent());
        xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiome.freeze();
        xy177.farmersfuturedelight.common.registry.FFDWorkstationDefaults.finishLoading();
        xy177.farmersfuturedelight.common.raid.RaidEventConfiguration.finishLoading(
                xy177.farmersfuturedelight.common.raid.FFDRaidManager.defaultDefinitions());
        FFDRawOreOreDictionaryCompat.register();
        FFDDeepslateOreCompat.registerOreDictionary();
        FFDDeepslateOreCompat.registerRecipes();
        FFDRawOreCompat.register();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        net.minecraft.command.ICommand recipeCommand = event.getServer().getCommandManager()
                .getCommands().get("recipe");
        if (recipeCommand != null) {
            event.registerServerCommand(
                    new xy177.farmersfuturedelight.common.command.CommandCookingRecipe(recipeCommand));
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("teammsg")
                && !event.getServer().getCommandManager().getCommands().containsKey("tm")) {
            event.registerServerCommand(new CommandTeamMsg());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("spectate")) {
            event.registerServerCommand(new CommandSpectate());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("schedule")) {
            event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandSchedule());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("data")) {
            event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandData());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("loot")) {
            event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandLoot());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("tag")) {
            event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandTag());
        }
        if (!event.getServer().getCommandManager().getCommands().containsKey("forceload")) {
            event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandForceLoad());
        }
        event.registerServerCommand(new CommandLocateBiome());
        event.registerServerCommand(new CommandCompatibility());
        event.registerServerCommand(new CommandRawOre());
        event.registerServerCommand(new CommandStrippedWood());
        event.registerServerCommand(new xy177.farmersfuturedelight.common.command.CommandFluidMigration());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        net.minecraft.server.MinecraftServer server = FMLCommonHandler.instance()
                .getMinecraftServerInstance();
        if (server != null) {
            server.getFunctionManager().reload();
        }
    }
}
