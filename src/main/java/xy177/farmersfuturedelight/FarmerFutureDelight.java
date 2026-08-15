package xy177.farmersfuturedelight;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
import xy177.farmersfuturedelight.common.registry.FFDTileEntities;
import xy177.farmersfuturedelight.common.network.FFDNetwork;
import xy177.farmersfuturedelight.common.world.FFDWorldTypes;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.command.CommandLocateBiome;
import xy177.farmersfuturedelight.core.FFDCoreCompat;
import xy177.farmersfuturedelight.core.FFDHeightHooks;
import xy177.farmersfuturedelight.proxy.CommonProxy;

@Mod(modid = FarmerFutureDelight.MODID, name = FarmerFutureDelight.NAME, version = FarmerFutureDelight.VERSION,
        acceptedMinecraftVersions = "[1.12.2]")
public class FarmerFutureDelight {
    public static final String MODID = "farmers_future_delight";
    public static final String NAME = "Farmer's Future Delight";
    public static final String VERSION = "1.2.0";

    @Mod.Instance(MODID)
    public static FarmerFutureDelight instance;

    @SidedProxy(clientSide = "xy177.farmersfuturedelight.proxy.ClientProxy",
            serverSide = "xy177.farmersfuturedelight.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FFDWorldTypes.register();
        FFDConfig.load(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(FFDAxolotlSpawner.class);
        FFDGameplayHooks.applyModernBlockProperties();
        FFDNetwork.init();
        FFDAdvancements.init();
        FFDTileEntities.register();
        MinecraftForge.EVENT_BUS.register(FFDVerticalBiomeManager.class);
        MinecraftForge.TERRAIN_GEN_BUS.register(xy177.farmersfuturedelight.common.worldgen.FFDWorldgenEvents.class);
        MinecraftForge.ORE_GEN_BUS.register(xy177.farmersfuturedelight.common.worldgen.FFDWorldgenEvents.class);
        proxy.preInit();
        GameRegistry.registerWorldGenerator(new FFDWorldGenerator(), 0);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
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
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandLocateBiome());
    }
}
