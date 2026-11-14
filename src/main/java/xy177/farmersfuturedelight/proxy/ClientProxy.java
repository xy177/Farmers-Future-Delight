package xy177.farmersfuturedelight.proxy;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;
import xy177.farmersfuturedelight.client.particle.ParticleGlowSquid;
import xy177.farmersfuturedelight.client.model.CustomRawOreModelLoader;
import xy177.farmersfuturedelight.client.model.CustomDeepslateOreModelLoader;
import xy177.farmersfuturedelight.client.particle.ParticleDripstone;
import xy177.farmersfuturedelight.client.particle.ParticleCryingObsidian;
import xy177.farmersfuturedelight.client.particle.ParticleFallingNectar;
import xy177.farmersfuturedelight.client.particle.ParticleHoneyDrip;
import xy177.farmersfuturedelight.client.particle.ParticleSmallFlame;
import xy177.farmersfuturedelight.client.particle.ParticleCampfireSmoke;
import xy177.farmersfuturedelight.client.particle.ParticleConduit;
import xy177.farmersfuturedelight.client.particle.ParticleBubbleColumnUp;
import xy177.farmersfuturedelight.client.particle.ParticleCurrentDown;
import xy177.farmersfuturedelight.client.particle.ParticleSporeBlossom;
import xy177.farmersfuturedelight.client.model.CustomStrippedWoodModelLoader;
import xy177.farmersfuturedelight.client.render.RenderBee;
import xy177.farmersfuturedelight.client.render.RenderAxolotl;
import xy177.farmersfuturedelight.client.render.RenderGlowSquid;
import xy177.farmersfuturedelight.client.render.RenderAgeableSquid;
import xy177.farmersfuturedelight.client.render.RenderGlowItemFrame;
import xy177.farmersfuturedelight.client.render.RenderGoat;
import xy177.farmersfuturedelight.client.render.RenderCat;
import xy177.farmersfuturedelight.client.render.RenderFox;
import xy177.farmersfuturedelight.client.render.RenderPanda;
import xy177.farmersfuturedelight.client.render.RenderPillager;
import xy177.farmersfuturedelight.client.render.RenderRavager;
import xy177.farmersfuturedelight.client.render.RenderPhantom;
import xy177.farmersfuturedelight.client.render.RenderTurtle;
import xy177.farmersfuturedelight.client.render.RenderSign;
import xy177.farmersfuturedelight.client.render.RenderConduit;
import xy177.farmersfuturedelight.client.render.RenderConduitItem;
import xy177.farmersfuturedelight.client.render.RenderCod;
import xy177.farmersfuturedelight.client.render.RenderSalmon;
import xy177.farmersfuturedelight.client.render.RenderPufferfish;
import xy177.farmersfuturedelight.client.render.RenderTropicalFish;
import xy177.farmersfuturedelight.client.render.RenderDolphin;
import xy177.farmersfuturedelight.client.render.RenderDrowned;
import xy177.farmersfuturedelight.client.render.RenderTrident;
import xy177.farmersfuturedelight.client.render.RenderTridentItem;
import xy177.farmersfuturedelight.client.render.LayerRiptide;
import xy177.farmersfuturedelight.common.entity.EntityBee;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;
import xy177.farmersfuturedelight.common.entity.EntityGlowSquid;
import xy177.farmersfuturedelight.common.entity.EntityGlowItemFrame;
import xy177.farmersfuturedelight.common.entity.EntityGoat;
import xy177.farmersfuturedelight.common.entity.EntityCat;
import xy177.farmersfuturedelight.common.entity.EntityFox;
import xy177.farmersfuturedelight.common.entity.EntityPanda;
import xy177.farmersfuturedelight.common.entity.EntityPillager;
import xy177.farmersfuturedelight.common.entity.EntityRavager;
import xy177.farmersfuturedelight.common.entity.EntityPhantom;
import xy177.farmersfuturedelight.common.entity.EntityTurtle;
import xy177.farmersfuturedelight.common.entity.EntityCod;
import xy177.farmersfuturedelight.common.entity.EntitySalmon;
import xy177.farmersfuturedelight.common.entity.EntityPufferfish;
import xy177.farmersfuturedelight.common.entity.EntityTropicalFish;
import xy177.farmersfuturedelight.common.entity.EntityDolphin;
import xy177.farmersfuturedelight.common.entity.EntityDrowned;
import xy177.farmersfuturedelight.common.entity.EntityTrident;
import xy177.farmersfuturedelight.common.entity.EntityCrossbowArrow;
import net.minecraft.tileentity.TileEntitySign;
import xy177.farmersfuturedelight.common.tile.TileEntityConduit;
import xy177.farmersfuturedelight.common.block.BlockKelp;
import xy177.farmersfuturedelight.common.block.BlockKelpYoung;
import xy177.farmersfuturedelight.common.block.BlockCoralPlant;
import xy177.farmersfuturedelight.common.block.BlockCoralWallFan;
import xy177.farmersfuturedelight.common.block.BlockBigDripleafStem;
import xy177.farmersfuturedelight.common.block.BlockAmethystCluster;
import xy177.farmersfuturedelight.common.block.BlockAzaleaLeaves;
import xy177.farmersfuturedelight.common.block.BlockSmallDripleaf;
import xy177.farmersfuturedelight.common.block.BlockHangingRoots;
import xy177.farmersfuturedelight.common.block.BlockFutureStairs;
import xy177.farmersfuturedelight.common.block.BlockFutureSlab;
import xy177.farmersfuturedelight.common.block.BlockFutureWall;
import xy177.farmersfuturedelight.common.block.BlockModernWall;
import xy177.farmersfuturedelight.common.block.BlockIronChain;
import xy177.farmersfuturedelight.common.block.BlockCandle;
import xy177.farmersfuturedelight.common.block.BlockNetherFence;
import xy177.farmersfuturedelight.common.block.BlockNetherSlab;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDCustomRawOres;
import xy177.farmersfuturedelight.common.registry.FFDEntities;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDDeepslateOreCompat;
import xy177.farmersfuturedelight.client.FFDDeepslateOreCompatClient;
import xy177.farmersfuturedelight.client.AquaAcrobaticsWaterCompat;
import xy177.farmersfuturedelight.client.ModernWaterRendering;
import xy177.farmersfuturedelight.common.registry.FFDRawOres;
import xy177.farmersfuturedelight.common.registry.FFDCustomStrippedWoods;
import xy177.farmersfuturedelight.common.item.ItemGoatHorn;
import xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeManager;
import xy177.farmersfuturedelight.common.fluid.FFDFluidloggedData;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void handleCookingBook(net.minecraft.nbt.NBTTagCompound data) {
        Minecraft.getMinecraft().addScheduledTask(() ->
                xy177.farmersfuturedelight.client.GuiCookingFurnace.receiveBook(data));
    }

    @Override
    public void openJigsaw(xy177.farmersfuturedelight.common.tile.TileEntityJigsaw tile) {
        Minecraft.getMinecraft().displayGuiScreen(new xy177.farmersfuturedelight.client.GuiJigsawEditor(tile));
    }

    @Override
    public void handleMerchantState(xy177.farmersfuturedelight.common.network.PacketMerchantState state) {
        Minecraft.getMinecraft().addScheduledTask(() ->
                xy177.farmersfuturedelight.client.GuiModernMerchant.receive(state));
    }

    @Override
    public Object getWorkstationGui(int id, net.minecraft.entity.player.EntityPlayer player,
                                    net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.SMITHING_TABLE) {
            return new xy177.farmersfuturedelight.client.GuiSmithingTable(
                    player.inventory, world, pos);
        }
        net.minecraft.tileentity.TileEntity tile = world.getTileEntity(pos);
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.COOKING_FURNACE
                && tile instanceof xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace) {
            return new xy177.farmersfuturedelight.client.GuiCookingFurnace(player.inventory,
                    (xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace) tile);
        }
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.STONECUTTER
                && tile instanceof xy177.farmersfuturedelight.common.tile.TileEntityStonecutter) {
            return new xy177.farmersfuturedelight.client.GuiStonecutter(player.inventory,
                    (xy177.farmersfuturedelight.common.tile.TileEntityStonecutter) tile);
        }
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.GRINDSTONE
                && tile instanceof xy177.farmersfuturedelight.common.tile.TileEntityGrindstone) {
            return new xy177.farmersfuturedelight.client.GuiGrindstone(player.inventory,
                    (xy177.farmersfuturedelight.common.tile.TileEntityGrindstone) tile);
        }
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.CARTOGRAPHY_TABLE
                && tile instanceof xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable) {
            return new xy177.farmersfuturedelight.client.GuiCartographyTable(player.inventory,
                    (xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable) tile);
        }
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.LOOM) {
            return new xy177.farmersfuturedelight.client.GuiLoom(
                    new xy177.farmersfuturedelight.common.inventory.ContainerLoom(
                            player.inventory, world, pos));
        }
        if (id == xy177.farmersfuturedelight.common.network.FFDGuiHandler.LECTERN
                && tile instanceof xy177.farmersfuturedelight.common.tile.TileEntityLectern) {
            xy177.farmersfuturedelight.common.tile.TileEntityLectern lectern =
                    (xy177.farmersfuturedelight.common.tile.TileEntityLectern) tile;
            return new xy177.farmersfuturedelight.client.GuiLecternBook(player,
                    new xy177.farmersfuturedelight.common.inventory.ContainerLectern(lectern));
        }
        return null;
    }

    @Override
    public boolean isHoldingScaffolding() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null && (player.getHeldItemMainhand().getItem() == FFDItems.SCAFFOLDING
                || player.getHeldItemOffhand().getItem() == FFDItems.SCAFFOLDING);
    }

    @Override
    public AxisAlignedBB getLightSelectionBox(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player != null && FFDItems.isLightEnabled()
                && (player.getHeldItemMainhand().getItem() == FFDItems.LIGHT
                || player.getHeldItemOffhand().getItem() == FFDItems.LIGHT)) {
            return net.minecraft.block.Block.FULL_BLOCK_AABB;
        }
        return net.minecraft.block.Block.NULL_AABB;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (net.minecraft.block.Block block : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS) {
            if (block instanceof xy177.farmersfuturedelight.common.block.BlockWaterloggedStateCarrier) {
                ModelLoader.setCustomStateMapper(block, ignored -> java.util.Collections.emptyMap());
                continue;
            }
            if (WaterloggedBlockApi.hasUniversalWaterloggedProperty(block.getDefaultState())) {
                WaterloggedBlockApi.registerRenderedBlock(block);
                WaterloggedBlockApi.registerWaterColorBlock(block);
            }
        }
        registerModel(FFDItems.SWEET_BERRIES, "sweet_berries");
        registerModel(FFDItems.SUSPICIOUS_STEW, "suspicious_stew");
        registerModel(FFDItems.LEATHER_HORSE_ARMOR, "leather_horse_armor");
        registerModel(FFDItems.WARPED_FUNGUS_ON_A_STICK, "warped_fungus_on_a_stick");
        registerModel(FFDItems.LANTERN, "lantern");
        registerModel(FFDItems.BARREL, "barrel");
        registerModel(FFDItems.CAMPFIRE, "campfire");
        registerModel(FFDItems.SOUL_SOIL, "soul_soil");
        registerModel(FFDItems.SOUL_TORCH, "soul_torch");
        registerModel(FFDItems.SOUL_LANTERN, "soul_lantern");
        registerModel(FFDItems.SOUL_CAMPFIRE, "soul_campfire");
        registerStateMapper(FFDBlocks.SOUL_CAMPFIRE, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockCampfire.SIGNAL_FIRE,
                        xy177.farmersfuturedelight.common.block.BlockCampfire.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.SOUL_LANTERN, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockLantern.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.SOUL_FIRE, new StateMap.Builder()
                .ignore(net.minecraft.block.BlockFire.AGE, net.minecraft.block.BlockFire.NORTH,
                        net.minecraft.block.BlockFire.SOUTH, net.minecraft.block.BlockFire.EAST,
                        net.minecraft.block.BlockFire.WEST, net.minecraft.block.BlockFire.UPPER).build());
        registerStateMapper(FFDBlocks.CAMPFIRE, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockCampfire.SIGNAL_FIRE,
                        xy177.farmersfuturedelight.common.block.BlockCampfire.WATERLOGGED).build());
        registerModel(FFDItems.SMOKER, "smoker");
        registerModel(FFDItems.BLAST_FURNACE, "blast_furnace");
        registerModel(FFDItems.COMPOSTER, "composter");
        registerModel(FFDItems.STONECUTTER, "stonecutter");
        registerModel(FFDItems.GRINDSTONE, "grindstone");
        registerModel(FFDItems.CARTOGRAPHY_TABLE, "cartography_table");
        registerModel(FFDItems.LOOM, "loom");
        registerModel(FFDItems.LECTERN, "lectern");
        registerStateMapper(FFDBlocks.LECTERN, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockLectern.POWERED).build());
        registerModel(FFDItems.FLETCHING_TABLE, "fletching_table");
        registerModel(FFDItems.SMITHING_TABLE, "smithing_table");
        if (FFDItems.isNetheriteEnabled()) {
            registerModels(FFDItems.NETHERITE_ITEMS);
        }
        registerModel(FFDItems.BELL, "bell");
        registerModel(FFDItems.JIGSAW, "jigsaw");
        registerStateMapper(FFDBlocks.BELL, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockBell.POWERED).build());
        registerModel(FFDItems.FLOWER_BANNER_PATTERN, "flower_banner_pattern");
        registerModel(FFDItems.CREEPER_BANNER_PATTERN, "creeper_banner_pattern");
        registerModel(FFDItems.SKULL_BANNER_PATTERN, "skull_banner_pattern");
        registerModel(FFDItems.MOJANG_BANNER_PATTERN, "mojang_banner_pattern");
        registerModel(FFDItems.GLOBE_BANNER_PATTERN, "globe_banner_pattern");
        registerModel(FFDItems.PIGLIN_BANNER_PATTERN, "piglin_banner_pattern");
        registerModel(FFDItems.FIELD_MASONED_BANNER_PATTERN, "field_masoned_banner_pattern");
        registerModel(FFDItems.BORDURE_INDENTED_BANNER_PATTERN, "bordure_indented_banner_pattern");
        registerModel(FFDItems.CORNFLOWER, "cornflower");
        registerModel(FFDItems.LILY_OF_THE_VALLEY, "lily_of_the_valley");
        registerModel(FFDItems.WITHER_ROSE, "wither_rose");
        registerModel(FFDItems.GOLDEN_DANDELION, "golden_dandelion");
        registerModel(FFDItems.WHITE_DYE, "white_dye");
        registerModel(FFDItems.BLACK_DYE, "black_dye");
        registerModel(FFDItems.BLUE_DYE, "blue_dye");
        registerModel(FFDItems.BROWN_DYE, "brown_dye");
        registerModel(FFDItems.BAMBOO, "bamboo");
        registerModel(FFDItems.SCAFFOLDING, "scaffolding");
        registerStateMapper(FFDBlocks.SCAFFOLDING, new StateMap.Builder()
                .ignore(xy177.farmersfuturedelight.common.block.BlockScaffolding.DISTANCE,
                        xy177.farmersfuturedelight.common.block.BlockScaffolding.WATERLOGGED).build());
        registerModel(FFDItems.MOSS_BLOCK, "moss_block");
        registerModel(FFDItems.MOSS_CARPET, "moss_carpet");
        registerModel(FFDItems.GLOW_BERRIES, "glow_berries");
        registerModel(FFDItems.AZALEA, "azalea");
        registerModel(FFDItems.FLOWERING_AZALEA, "flowering_azalea");
        registerModel(FFDItems.AZALEA_LEAVES, "azalea_leaves");
        registerModel(FFDItems.FLOWERING_AZALEA_LEAVES, "flowering_azalea_leaves");
        registerModel(FFDItems.SMALL_DRIPLEAF, "small_dripleaf");
        registerModel(FFDItems.BIG_DRIPLEAF, "big_dripleaf");
        registerStateMapper(FFDBlocks.HANGING_ROOTS,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockHangingRoots.WATERLOGGED).build());
        registerModel(FFDItems.ROOTED_DIRT, "rooted_dirt");
        registerModel(FFDItems.HANGING_ROOTS, "hanging_roots_item");
        registerModel(FFDItems.SPORE_BLOSSOM, "spore_blossom");
        if (FFDItems.isGlowLichenEnabled()) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.GLOW_LICHEN, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":glow_lichen_item", "inventory"));
            for (net.minecraft.block.Block block : FFDBlocks.GLOW_LICHEN_VARIANTS) {
                registerStateMapper(block,
                        new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            }
        }
        if (FFDEntities.isGlowSquidEnabled()) {
            registerModel(FFDItems.GLOW_INK_SAC, "glow_ink_sac");
        }
        if (FFDItems.isGlowItemFrameEnabled()) {
            registerModel(FFDItems.GLOW_ITEM_FRAME, "glow_item_frame");
        }
        if (FFDItems.isKelpEnabled()) {
            registerModel(FFDItems.KELP, "kelp");
            registerModel(FFDItems.DRIED_KELP, "dried_kelp");
            registerModel(FFDItems.DRIED_KELP_BLOCK, "dried_kelp_block");
        }
        if (FFDItems.isSeagrassEnabled()) {
            registerModel(FFDItems.SEAGRASS, "seagrass");
        }
        if (FFDItems.isSeaPickleEnabled()) {
            registerModel(FFDItems.SEA_PICKLE, "sea_pickle");
        }
        if (FFDItems.isCoralEnabled()) {
            registerModels(FFDItems.CORAL_BLOCK_ITEMS);
            registerModels(FFDItems.DEAD_CORAL_BLOCK_ITEMS);
            registerModels(FFDItems.CORAL_ITEMS);
            registerModels(FFDItems.DEAD_CORAL_ITEMS);
            registerModels(FFDItems.CORAL_FAN_ITEMS);
            registerModels(FFDItems.DEAD_CORAL_FAN_ITEMS);
            for (net.minecraft.block.Block block : FFDBlocks.CORALS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED).build());
            }
            for (net.minecraft.block.Block block : FFDBlocks.DEAD_CORALS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED).build());
            }
            for (net.minecraft.block.Block block : FFDBlocks.CORAL_FANS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED).build());
            }
            for (net.minecraft.block.Block block : FFDBlocks.DEAD_CORAL_FANS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralPlant.WATERLOGGED).build());
            }
            for (net.minecraft.block.Block block : FFDBlocks.CORAL_WALL_FANS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralWallFan.WATERLOGGED).build());
            }
            for (net.minecraft.block.Block block : FFDBlocks.DEAD_CORAL_WALL_FANS) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockLiquid.LEVEL, BlockCoralWallFan.WATERLOGGED).build());
            }
        }
        if (FFDItems.isBlueIceEnabled()) {
            registerModel(FFDItems.BLUE_ICE, "blue_ice");
        }
        if (FFDItems.isStrippedWoodEnabled()) {
            registerModels(FFDItems.WOOD_ITEMS);
            registerModels(FFDItems.STRIPPED_LOG_ITEMS);
            registerModels(FFDItems.STRIPPED_WOOD_ITEMS);
            registerModels(FFDItems.OVERWORLD_TRAPDOOR_ITEMS);
            registerModels(FFDItems.OVERWORLD_BUTTON_ITEMS);
            registerModels(FFDItems.OVERWORLD_PRESSURE_PLATE_ITEMS);
            registerMirroredStateMappers(FFDBlocks.OVERWORLD_TRAPDOORS_WATERLOGGED,
                    FFDBlocks.OVERWORLD_TRAPDOORS);
            registerMirroredStateMapper(FFDBlocks.OVERWORLD_TRAPDOORS_WATERLOGGED[0],
                    net.minecraft.init.Blocks.TRAPDOOR);
        }
        if (FFDItems.isPumpkinEnabled()) {
            registerModel(FFDItems.PUMPKIN, "pumpkin");
        }
        if (FFDItems.isPrismarineDecorEnabled()) {
            registerModels(FFDItems.PRISMARINE_STAIR_ITEMS);
            registerModels(FFDItems.PRISMARINE_SLAB_ITEMS);
        }
        if (FFDItems.isConduitEnabled()) {
            registerModel(FFDItems.NAUTILUS_SHELL, "nautilus_shell");
            registerModel(FFDItems.HEART_OF_THE_SEA, "heart_of_the_sea");
            registerModel(FFDItems.CONDUIT, "conduit");
            registerStateMapper(FFDBlocks.CONDUIT,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            xy177.farmersfuturedelight.common.block.BlockConduit.WATERLOGGED).build());
        }
        if (FFDItems.isTridentEnabled()) {
            registerModel(FFDItems.TRIDENT, "trident");
            ModelBakery.registerItemVariants(FFDItems.TRIDENT,
                    new ResourceLocation(FarmerFutureDelight.MODID, "trident_in_hand"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "trident_throwing"));
        }
        if (FFDItems.isLocalCrossbowEnabled()) {
            registerModel(FFDItems.CROSSBOW, "crossbow");
            ModelBakery.registerItemVariants(FFDItems.CROSSBOW,
                    new ResourceLocation(FarmerFutureDelight.MODID, "crossbow_pulling_0"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "crossbow_pulling_1"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "crossbow_pulling_2"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "crossbow_arrow"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "crossbow_firework"));
        }
        if (FFDItems.isBlockRegistered(FFDBlocks.BUBBLE_COLUMN)) {
            registerStateMapper(FFDBlocks.BUBBLE_COLUMN,
                    new StateMap.Builder().ignore(
                            xy177.farmersfuturedelight.common.block.BlockBubbleColumn.DRAG).build());
        }
        if (FFDItems.isFishEnabled()) {
            registerModel(FFDItems.COD_BUCKET, "cod_bucket");
            registerModel(FFDItems.SALMON_BUCKET, "salmon_bucket");
            registerModel(FFDItems.PUFFERFISH_BUCKET, "pufferfish_bucket");
            registerModel(FFDItems.TROPICAL_FISH_BUCKET, "tropical_fish_bucket");
        }
        if (FFDItems.isTurtleEnabled()) {
            registerModel(FFDItems.TURTLE_EGG, "turtle_egg");
            registerModel(FFDItems.TURTLE_SCUTE, "turtle_scute");
            registerModel(FFDItems.TURTLE_HELMET, "turtle_helmet");
        }
        if (FFDItems.isAxolotlEnabled()) {
            registerModel(FFDItems.AXOLOTL_BUCKET, "axolotl_bucket");
        }
        if (FFDItems.isItemRegistered(FFDItems.GOAT_HORN)) {
            for (int instrument = 0; instrument < ItemGoatHorn.INSTRUMENT_COUNT; instrument++) {
                ModelLoader.setCustomModelResourceLocation(FFDItems.GOAT_HORN, instrument,
                        new ModelResourceLocation(FarmerFutureDelight.MODID + ":goat_horn", "inventory"));
            }
            ModelBakery.registerItemVariants(FFDItems.GOAT_HORN,
                    new ResourceLocation(FarmerFutureDelight.MODID, "goat_horn"),
                    new ResourceLocation(FarmerFutureDelight.MODID, "tooting_goat_horn"));
        }
        if (FFDItems.isItemRegistered(FFDItems.PHANTOM_MEMBRANE)) {
            ModelLoader.setCustomModelResourceLocation(FFDItems.PHANTOM_MEMBRANE, 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID + ":phantom_membrane", "inventory"));
        }
        if (FFDItems.isOthersideEnabled()) {
            registerModel(FFDItems.MUSIC_DISC_OTHERSIDE, "music_disc_otherside");
        }
        if (FFDItems.isAmethystEnabled()) {
            registerModel(FFDItems.AMETHYST_BLOCK, "amethyst_block");
            registerModel(FFDItems.BUDDING_AMETHYST, "budding_amethyst");
            registerModel(FFDItems.SMALL_AMETHYST_BUD, "small_amethyst_bud");
            registerModel(FFDItems.MEDIUM_AMETHYST_BUD, "medium_amethyst_bud");
            registerModel(FFDItems.LARGE_AMETHYST_BUD, "large_amethyst_bud");
            registerModel(FFDItems.AMETHYST_CLUSTER, "amethyst_cluster");
            registerModel(FFDItems.CALCITE, "calcite");
            registerModel(FFDItems.SMOOTH_BASALT, "smooth_basalt");
            registerModel(FFDItems.TINTED_GLASS, "tinted_glass");
            registerModel(FFDItems.AMETHYST_SHARD, "amethyst_shard");
            registerStateMapper(FFDBlocks.SMALL_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.MEDIUM_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.LARGE_AMETHYST_BUD,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.AMETHYST_CLUSTER,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL,
                            BlockAmethystCluster.WATERLOGGED).build());
        }
        if (FFDItems.isDripstoneEnabled()) {
            registerModel(FFDItems.DRIPSTONE_BLOCK, "dripstone_block");
            registerModel(FFDItems.POINTED_DRIPSTONE, "pointed_dripstone");
        }
        if (FFDItems.isIronChainEnabled()) {
            registerModel(FFDItems.IRON_CHAIN, "iron_chain");
        }
        if (FFDItems.isLightEnabled()) {
            for (int level = 0; level <= 15; level++) {
                String suffix = level < 10 ? "0" + level : Integer.toString(level);
                ModelLoader.setCustomModelResourceLocation(FFDItems.LIGHT, level,
                        new ModelResourceLocation(FarmerFutureDelight.MODID + ":light_" + suffix,
                                "inventory"));
            }
        }
        if (FFDItems.isCandleEnabled()) {
            registerModels(FFDItems.CANDLE_ITEMS);
        }
        if (FFDItems.isPowderSnowEnabled()) {
            registerModel(FFDItems.POWDER_SNOW, "powder_snow");
            registerModel(FFDItems.POWDER_SNOW_BUCKET, "powder_snow_bucket");
        }
        if (FFDItems.isBlackstoneEnabled()) {
            registerModel(FFDItems.BLACKSTONE, "blackstone");
            registerModel(FFDItems.POLISHED_BLACKSTONE, "polished_blackstone");
            registerModel(FFDItems.POLISHED_BLACKSTONE_BRICKS, "polished_blackstone_bricks");
            registerModel(FFDItems.CRACKED_POLISHED_BLACKSTONE_BRICKS, "cracked_polished_blackstone_bricks");
            registerModel(FFDItems.CHISELED_POLISHED_BLACKSTONE, "chiseled_polished_blackstone");
            registerModel(FFDItems.GILDED_BLACKSTONE, "gilded_blackstone");
            registerModel(FFDItems.BLACKSTONE_STAIRS, "blackstone_stairs");
            registerModel(FFDItems.POLISHED_BLACKSTONE_STAIRS, "polished_blackstone_stairs");
            registerModel(FFDItems.POLISHED_BLACKSTONE_BRICK_STAIRS, "polished_blackstone_brick_stairs");
            registerModel(FFDItems.BLACKSTONE_WALL, "blackstone_wall");
            registerModel(FFDItems.POLISHED_BLACKSTONE_WALL, "polished_blackstone_wall");
            registerModel(FFDItems.POLISHED_BLACKSTONE_BRICK_WALL, "polished_blackstone_brick_wall");
            registerModel(FFDItems.POLISHED_BLACKSTONE_BUTTON, "polished_blackstone_button");
            registerModel(FFDItems.POLISHED_BLACKSTONE_PRESSURE_PLATE, "polished_blackstone_pressure_plate");
            registerModel(FFDItems.BLACKSTONE_SLAB, "blackstone_slab");
            registerModel(FFDItems.POLISHED_BLACKSTONE_SLAB, "polished_blackstone_slab");
            registerModel(FFDItems.POLISHED_BLACKSTONE_BRICK_SLAB, "polished_blackstone_brick_slab");
        }
        registerModel(FFDItems.BASALT,"basalt");
        registerModel(FFDItems.POLISHED_BASALT,"polished_basalt");
        registerModel(FFDItems.NETHER_GOLD_ORE,"nether_gold_ore");
        if (FFDItems.isQuartzBricksEnabled()) {
            registerModel(FFDItems.QUARTZ_BRICKS, "quartz_bricks");
        }
        if (FFDItems.isCryingObsidianEnabled()) {
            registerModel(FFDItems.CRYING_OBSIDIAN, "crying_obsidian");
        }
        registerWaterloggedShapeMappers(FFDBlocks.BLACKSTONE_STAIRS, FFDBlocks.POLISHED_BLACKSTONE_STAIRS, FFDBlocks.POLISHED_BLACKSTONE_BRICK_STAIRS, FFDBlocks.BLACKSTONE_WALL, FFDBlocks.POLISHED_BLACKSTONE_WALL, FFDBlocks.POLISHED_BLACKSTONE_BRICK_WALL, FFDBlocks.BLACKSTONE_SLAB, FFDBlocks.POLISHED_BLACKSTONE_SLAB, FFDBlocks.POLISHED_BLACKSTONE_BRICK_SLAB, FFDBlocks.BLACKSTONE_DOUBLE_SLAB, FFDBlocks.POLISHED_BLACKSTONE_DOUBLE_SLAB, FFDBlocks.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB);
        if (FFDItems.isDeepslateEnabled()) {
            registerModel(FFDItems.DEEPSLATE, "deepslate");
            registerModel(FFDItems.COBBLED_DEEPSLATE, "cobbled_deepslate");
            registerModel(FFDItems.POLISHED_DEEPSLATE, "polished_deepslate");
            registerModel(FFDItems.DEEPSLATE_BRICKS, "deepslate_bricks");
            registerModel(FFDItems.CRACKED_DEEPSLATE_BRICKS, "cracked_deepslate_bricks");
            registerModel(FFDItems.DEEPSLATE_TILES, "deepslate_tiles");
            registerModel(FFDItems.CRACKED_DEEPSLATE_TILES, "cracked_deepslate_tiles");
            registerModel(FFDItems.CHISELED_DEEPSLATE, "chiseled_deepslate");
            registerModel(FFDItems.TUFF, "tuff");
            registerModel(FFDItems.INFESTED_DEEPSLATE, "infested_deepslate");
            registerModel(FFDItems.COBBLED_DEEPSLATE_STAIRS, "cobbled_deepslate_stairs");
            registerModel(FFDItems.POLISHED_DEEPSLATE_STAIRS, "polished_deepslate_stairs");
            registerModel(FFDItems.DEEPSLATE_BRICK_STAIRS, "deepslate_brick_stairs");
            registerModel(FFDItems.DEEPSLATE_TILE_STAIRS, "deepslate_tile_stairs");
            registerModel(FFDItems.COBBLED_DEEPSLATE_SLAB, "cobbled_deepslate_slab");
            registerModel(FFDItems.POLISHED_DEEPSLATE_SLAB, "polished_deepslate_slab");
            registerModel(FFDItems.DEEPSLATE_BRICK_SLAB, "deepslate_brick_slab");
            registerModel(FFDItems.DEEPSLATE_TILE_SLAB, "deepslate_tile_slab");
            registerModel(FFDItems.COBBLED_DEEPSLATE_WALL, "cobbled_deepslate_wall");
            registerModel(FFDItems.POLISHED_DEEPSLATE_WALL, "polished_deepslate_wall");
            registerModel(FFDItems.DEEPSLATE_BRICK_WALL, "deepslate_brick_wall");
            registerModel(FFDItems.DEEPSLATE_TILE_WALL, "deepslate_tile_wall");
            registerModel(FFDItems.DEEPSLATE_COAL_ORE, "deepslate_coal_ore");
            registerModel(FFDItems.DEEPSLATE_IRON_ORE, "deepslate_iron_ore");
            registerModel(FFDItems.DEEPSLATE_GOLD_ORE, "deepslate_gold_ore");
            registerModel(FFDItems.DEEPSLATE_REDSTONE_ORE, "deepslate_redstone_ore");
            registerModel(FFDItems.DEEPSLATE_LAPIS_ORE, "deepslate_lapis_ore");
            registerModel(FFDItems.DEEPSLATE_DIAMOND_ORE, "deepslate_diamond_ore");
            registerModel(FFDItems.DEEPSLATE_EMERALD_ORE, "deepslate_emerald_ore");
        }
        if (FFDItems.isRawOreEnabled()) {
            registerRawOreModels(false);
        }
        registerModel(FFDItems.COPPER_ORE, "copper_ore");
        if (FFDItems.isCopperEnabled()) {
            registerRawOreModels(true);
            registerModel(FFDItems.COPPER_INGOT, "copper_ingot");
        }
        registerModels(FFDItems.COPPER_BLOCK_ITEMS);
        registerModels(FFDItems.WAXED_COPPER_BLOCK_ITEMS);
        registerModels(FFDItems.CUT_COPPER_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_ITEMS);
        registerModels(FFDItems.CUT_COPPER_STAIR_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_STAIR_ITEMS);
        registerModels(FFDItems.CUT_COPPER_SLAB_ITEMS);
        registerModels(FFDItems.WAXED_CUT_COPPER_SLAB_ITEMS);
        registerModels(FFDItems.LIGHTNING_ROD_ITEMS);
        registerModels(FFDItems.WAXED_LIGHTNING_ROD_ITEMS);
        registerMirroredStateMappers(FFDBlocks.LIGHTNING_RODS_WATERLOGGED,
                FFDBlocks.LIGHTNING_RODS);
        registerMirroredStateMappers(FFDBlocks.WAXED_LIGHTNING_RODS_WATERLOGGED,
                FFDBlocks.WAXED_LIGHTNING_RODS);
        registerModel(FFDItems.SPYGLASS, "spyglass");
        if (FFDItems.isItemRegistered(FFDItems.SPYGLASS)) {
            ModelBakery.registerItemVariants(FFDItems.SPYGLASS,
                    new ResourceLocation(FarmerFutureDelight.MODID, "spyglass_in_hand"));
        }
        if (FFDItems.isDeepslateEnabled()) {
            registerModel(FFDItems.DEEPSLATE_COPPER_ORE, "deepslate_copper_ore");
        }
        if (FFDItems.isHoneyEnabled()) {
            registerModel(FFDItems.HONEY_BOTTLE, "honey_bottle");
            registerModel(FFDItems.HONEYCOMB, "honeycomb");
            registerModel(FFDItems.HONEY_BLOCK, "honey_block");
            registerModel(FFDItems.HONEYCOMB_BLOCK, "honeycomb_block");
            registerModel(FFDItems.BEE_NEST, "bee_nest");
            registerModel(FFDItems.BEEHIVE, "beehive");
        }
        if (FFDItems.isCrimsonEnabled()) {
            registerModel(FFDItems.CRIMSON_NYLIUM, "crimson_nylium");
            registerModel(FFDItems.CRIMSON_FUNGUS, "crimson_fungus");
            registerModel(FFDItems.CRIMSON_ROOTS, "crimson_roots");
            registerModel(FFDItems.WEEPING_VINES, "weeping_vines");
        }
        if (FFDItems.isWarpedEnabled()) {
            registerModel(FFDItems.WARPED_NYLIUM, "warped_nylium");
            registerModel(FFDItems.WARPED_FUNGUS, "warped_fungus");
            registerModel(FFDItems.WARPED_ROOTS, "warped_roots");
            registerModel(FFDItems.NETHER_SPROUTS, "nether_sprouts");
            registerModel(FFDItems.TWISTING_VINES, "twisting_vines");
        }
        if (FFDItems.isCrimsonWoodEnabled()) {
            registerModel(FFDItems.CRIMSON_STEM, "crimson_stem");
            registerModel(FFDItems.STRIPPED_CRIMSON_STEM, "stripped_crimson_stem");
            registerModel(FFDItems.CRIMSON_HYPHAE, "crimson_hyphae");
            registerModel(FFDItems.STRIPPED_CRIMSON_HYPHAE, "stripped_crimson_hyphae");
            registerModel(FFDItems.CRIMSON_PLANKS, "crimson_planks");
            registerModel(FFDItems.CRIMSON_STAIRS, "crimson_stairs");
            registerModel(FFDItems.CRIMSON_SLAB, "crimson_slab");
            registerModel(FFDItems.CRIMSON_FENCE, "crimson_fence");
            registerModel(FFDItems.CRIMSON_FENCE_GATE, "crimson_fence_gate");
            registerModel(FFDItems.CRIMSON_DOOR, "crimson_door");
            registerModel(FFDItems.CRIMSON_TRAPDOOR, "crimson_trapdoor");
            registerMirroredStateMapper(FFDBlocks.CRIMSON_TRAPDOOR_WATERLOGGED,
                    FFDBlocks.CRIMSON_TRAPDOOR);
            registerModel(FFDItems.CRIMSON_BUTTON, "crimson_button");
            registerModel(FFDItems.CRIMSON_PRESSURE_PLATE, "crimson_pressure_plate");
            registerStateMapper(FFDBlocks.CRIMSON_FENCE,
                    new StateMap.Builder().ignore(BlockNetherFence.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.CRIMSON_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            registerStateMapper(FFDBlocks.CRIMSON_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.WARPED_STEM, "warped_stem");
            registerModel(FFDItems.STRIPPED_WARPED_STEM, "stripped_warped_stem");
            registerModel(FFDItems.WARPED_HYPHAE, "warped_hyphae");
            registerModel(FFDItems.STRIPPED_WARPED_HYPHAE, "stripped_warped_hyphae");
            registerModel(FFDItems.WARPED_WART_BLOCK, "warped_wart_block");
            registerModel(FFDItems.WARPED_PLANKS, "warped_planks");
            registerModel(FFDItems.WARPED_STAIRS, "warped_stairs");
            registerModel(FFDItems.WARPED_SLAB, "warped_slab");
            registerModel(FFDItems.WARPED_FENCE, "warped_fence");
            registerModel(FFDItems.WARPED_FENCE_GATE, "warped_fence_gate");
            registerModel(FFDItems.WARPED_DOOR, "warped_door");
            registerModel(FFDItems.WARPED_TRAPDOOR, "warped_trapdoor");
            registerMirroredStateMapper(FFDBlocks.WARPED_TRAPDOOR_WATERLOGGED,
                    FFDBlocks.WARPED_TRAPDOOR);
            registerModel(FFDItems.WARPED_BUTTON, "warped_button");
            registerModel(FFDItems.WARPED_PRESSURE_PLATE, "warped_pressure_plate");
            registerStateMapper(FFDBlocks.WARPED_FENCE,
                    new StateMap.Builder().ignore(BlockNetherFence.WATERLOGGED).build());
            registerStateMapper(FFDBlocks.WARPED_DOOR,
                    new StateMap.Builder().ignore(BlockDoor.POWERED).build());
            registerStateMapper(FFDBlocks.WARPED_FENCE_GATE,
                    new StateMap.Builder().ignore(BlockFenceGate.POWERED).build());
        }
        if (FFDItems.isCrimsonWoodEnabled() || FFDItems.isWarpedWoodEnabled()) {
            registerModel(FFDItems.SHROOMLIGHT, "shroomlight");
        }
        registerStateMapper(FFDBlocks.AZALEA_LEAVES,
                new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY,
                        BlockAzaleaLeaves.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.FLOWERING_AZALEA_LEAVES,
                new StateMap.Builder().ignore(BlockLeaves.DECAYABLE, BlockLeaves.CHECK_DECAY,
                        BlockAzaleaLeaves.WATERLOGGED).build());
        if (FFDItems.isKelpEnabled()) {
            registerStateMapper(FFDBlocks.KELP,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelp.AGE).build());
            registerStateMapper(FFDBlocks.KELP_YOUNG,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockKelpYoung.AGE).build());
            registerStateMapper(FFDBlocks.KELP_PLANT,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeagrassEnabled()) {
            registerStateMapper(FFDBlocks.SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
            registerStateMapper(FFDBlocks.TALL_SEAGRASS,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        if (FFDItems.isSeaPickleEnabled()) {
            registerStateMapper(FFDBlocks.SEA_PICKLE,
                    new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        }
        registerStateMapper(FFDBlocks.SMALL_DRIPLEAF,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockSmallDripleaf.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF_STEM,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBigDripleafStem.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        registerStateMapper(FFDBlocks.BIG_DRIPLEAF_WATERLOGGED,
                new StateMap.Builder().ignore(BlockLiquid.LEVEL).build());
        registerStateMapper(FFDBlocks.IRON_CHAIN,
                new StateMap.Builder().ignore(BlockIronChain.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.LANTERN,
                new StateMap.Builder().ignore(
                        xy177.farmersfuturedelight.common.block.BlockLantern.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.COBBLED_DEEPSLATE_WALL,
                new StateMap.Builder().ignore(BlockFutureWall.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.POLISHED_DEEPSLATE_WALL,
                new StateMap.Builder().ignore(BlockFutureWall.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.DEEPSLATE_BRICK_WALL,
                new StateMap.Builder().ignore(BlockFutureWall.WATERLOGGED).build());
        registerStateMapper(FFDBlocks.DEEPSLATE_TILE_WALL,
                new StateMap.Builder().ignore(BlockFutureWall.WATERLOGGED).build());
        for (BlockCandle candle : FFDBlocks.CANDLES) {
            registerStateMapper(candle,
                    new StateMap.Builder().ignore(BlockCandle.WATERLOGGED).build());
        }
        registerWaterloggedShapeMappers(FFDBlocks.PRISMARINE_STAIRS);
        registerWaterloggedShapeMappers(FFDBlocks.PRISMARINE_SLABS);
        registerWaterloggedShapeMappers(FFDBlocks.PRISMARINE_DOUBLE_SLABS);
        registerWaterloggedShapeMappers(FFDBlocks.COBBLED_DEEPSLATE_STAIRS,
                FFDBlocks.POLISHED_DEEPSLATE_STAIRS,
                FFDBlocks.DEEPSLATE_BRICK_STAIRS,
                FFDBlocks.DEEPSLATE_TILE_STAIRS);
        registerWaterloggedShapeMappers(FFDBlocks.CRIMSON_STAIRS, FFDBlocks.WARPED_STAIRS);
        registerWaterloggedShapeMappers(FFDBlocks.COBBLED_DEEPSLATE_SLAB,
                FFDBlocks.POLISHED_DEEPSLATE_SLAB,
                FFDBlocks.DEEPSLATE_BRICK_SLAB,
                FFDBlocks.DEEPSLATE_TILE_SLAB,
                FFDBlocks.COBBLED_DEEPSLATE_DOUBLE_SLAB,
                FFDBlocks.POLISHED_DEEPSLATE_DOUBLE_SLAB,
                FFDBlocks.DEEPSLATE_BRICK_DOUBLE_SLAB,
                FFDBlocks.DEEPSLATE_TILE_DOUBLE_SLAB);
        registerWaterloggedShapeMappersArrays(FFDBlocks.CUT_COPPER_STAIRS,
                FFDBlocks.WAXED_CUT_COPPER_STAIRS);
        registerWaterloggedShapeMappersArrays(FFDBlocks.CUT_COPPER_SLABS,
                FFDBlocks.WAXED_CUT_COPPER_SLABS,
                FFDBlocks.CUT_COPPER_DOUBLE_SLABS,
                FFDBlocks.WAXED_CUT_COPPER_DOUBLE_SLABS);
        registerWaterloggedShapeMappers(FFDBlocks.CRIMSON_SLAB,
                FFDBlocks.CRIMSON_DOUBLE_SLAB, FFDBlocks.WARPED_SLAB,
                FFDBlocks.WARPED_DOUBLE_SLAB);
        registerCustomRawOreModels();
        registerModels(xy177.farmersfuturedelight.common.registry.FFDItems.STONE_DECOR_ITEMS);
        registerModel(FFDItems.CHISELED_NETHER_BRICKS, "chiseled_nether_bricks");
        registerModel(FFDItems.CRACKED_NETHER_BRICKS, "cracked_nether_bricks");
        registerModel(FFDItems.MUSIC_DISC_PIGSTEP, "music_disc_pigstep");
        registerModel(FFDItems.TARGET, "target");
        registerModel(FFDItems.LODESTONE, "lodestone");
        registerModel(FFDItems.RESPAWN_ANCHOR, "respawn_anchor");
        registerModels(FFDItems.WOOD_SIGN_ITEMS);
        registerModel(FFDItems.CRIMSON_SIGN, "crimson_sign");
        registerModel(FFDItems.WARPED_SIGN, "warped_sign");
        registerWaterloggedShapeMappers(xy177.farmersfuturedelight.common.registry.FFDStoneDecor
                .blocks().toArray(new net.minecraft.block.Block[0]));
        registerCustomStrippedWoodModels();
        FFDDeepslateOreCompatClient.registerModels();
    }

    private static void registerModel(net.minecraft.item.Item item, String name) {
        if (!FFDItems.isItemRegistered(item)) {
            return;
        }
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(FarmerFutureDelight.MODID + ":" + name, "inventory"));
    }

    private static void registerModels(net.minecraft.item.Item[] items) {
        for (net.minecraft.item.Item item : items) {
            registerModel(item, item.getRegistryName().getResourcePath());
        }
    }

    private static void registerRawOreModels(boolean copperOnly) {
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            if (FFDRawOres.isCopper(FFDRawOres.NAMES[i]) != copperOnly) {
                continue;
            }
            if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_ITEMS[i])) {
                registerModel(FFDItems.RAW_ORE_ITEMS[i],
                        FFDItems.RAW_ORE_ITEMS[i].getRegistryName().getResourcePath());
            }
            if (FFDItems.isItemRegistered(FFDItems.RAW_ORE_BLOCK_ITEMS[i])) {
                registerModel(FFDItems.RAW_ORE_BLOCK_ITEMS[i],
                        FFDItems.RAW_ORE_BLOCK_ITEMS[i].getRegistryName().getResourcePath());
            }
        }
    }

    private static void registerCustomRawOreModels() {
        for (FFDCustomRawOres.Entry entry : FFDCustomRawOres.entries()) {
            if (!entry.isRegistered() || !entry.isBlockRegistered()) {
                continue;
            }
            ModelLoader.setCustomModelResourceLocation(entry.item(), 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID
                            + ":custom_raw_ore_item/" + entry.material(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(entry.blockItem(), 0,
                    new ModelResourceLocation(FarmerFutureDelight.MODID
                            + ":custom_raw_ore_block/" + entry.material(), "inventory"));
            ModelLoader.setCustomStateMapper(entry.block(), block -> {
                Map<IBlockState, ModelResourceLocation> models = new HashMap<>();
                ModelResourceLocation location = new ModelResourceLocation(
                        FarmerFutureDelight.MODID + ":custom_raw_ore_block/"
                                + entry.material(), "normal");
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    models.put(state, location);
                }
                return models;
            });
        }
    }

    private static void registerCustomStrippedWoodModels() {
        if (!FFDItems.isStrippedWoodEnabled()) {
            return;
        }
        for (FFDCustomStrippedWoods.Entry entry : FFDCustomStrippedWoods.entries()) {
            if (!entry.isEnabled()) {
                continue;
            }
            if (entry.logItem() != null && entry.isLogRegistered()) {
                ModelLoader.setCustomModelResourceLocation(entry.logItem(), 0,
                        new ModelResourceLocation(FarmerFutureDelight.MODID
                                + ":custom_stripped_wood_log/" + entry.material(), "inventory"));
                registerCustomStrippedWoodStateMapper(entry.logBlock(),
                        "custom_stripped_wood_log/" + entry.material());
            }
            if (entry.woodItem() != null && entry.isWoodRegistered()) {
                ModelLoader.setCustomModelResourceLocation(entry.woodItem(), 0,
                        new ModelResourceLocation(FarmerFutureDelight.MODID
                                + ":custom_stripped_wood_wood/" + entry.material(), "inventory"));
                registerCustomStrippedWoodStateMapper(entry.woodBlock(),
                        "custom_stripped_wood_wood/" + entry.material());
            }
        }
    }

    private static void registerCustomStrippedWoodStateMapper(
            net.minecraft.block.Block block, String modelPath) {
        if (!FFDItems.isBlockRegistered(block)) {
            return;
        }
        ModelLoader.setCustomStateMapper(block, source -> {
            Map<IBlockState, ModelResourceLocation> models = new HashMap<>();
            for (IBlockState state : source.getBlockState().getValidStates()) {
                String variant = "axis=" + state.getValue(
                        net.minecraft.block.BlockRotatedPillar.AXIS).getName();
                models.put(state, new ModelResourceLocation(
                        FarmerFutureDelight.MODID + ":" + modelPath, variant));
            }
            return models;
        });
    }

    private static void registerStateMapper(net.minecraft.block.Block block,
                                            net.minecraft.client.renderer.block.statemap.IStateMapper mapper) {
        if (FFDItems.isBlockRegistered(block)) {
            ModelLoader.setCustomStateMapper(block, mapper);
        }
    }

    private static void registerMirroredStateMapper(net.minecraft.block.Block source,
                                                    net.minecraft.block.Block target) {
        if (!FFDItems.isBlockRegistered(source) || !FFDItems.isBlockRegistered(target)) {
            return;
        }
        net.minecraft.client.renderer.block.statemap.IStateMapper mapper =
                new StateMap.Builder().build();
        Map<IBlockState, ModelResourceLocation> targetModels =
                mapper.putStateModelLocations(target);
        ModelLoader.setCustomStateMapper(source, block -> {
            Map<IBlockState, ModelResourceLocation> models = new HashMap<>();
            for (IBlockState state : block.getBlockState().getValidStates()) {
                IBlockState targetState = target.getStateFromMeta(block.getMetaFromState(state));
                ModelResourceLocation location = targetModels.get(targetState);
                if (location != null) {
                    models.put(state, location);
                }
            }
            return models;
        });
    }

    private static void registerMirroredStateMappers(net.minecraft.block.Block[] sources,
                                                     net.minecraft.block.Block[] targets) {
        int count = Math.min(sources.length, targets.length);
        for (int index = 0; index < count; index++) {
            registerMirroredStateMapper(sources[index], targets[index]);
        }
    }

    private static void registerWaterloggedShapeMappers(net.minecraft.block.Block... blocks) {
        for (net.minecraft.block.Block block : blocks) {
            if (block instanceof BlockFutureStairs) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockFutureStairs.WATERLOGGED).build());
            } else if (block instanceof BlockFutureSlab) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockFutureSlab.WATERLOGGED).build());
            } else if (block instanceof BlockNetherSlab) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockNetherSlab.WATERLOGGED).build());
            } else if (block instanceof BlockModernWall) {
                registerStateMapper(block, new StateMap.Builder()
                        .ignore(BlockModernWall.WATERLOGGED).build());
            }
        }
    }

    private static void registerWaterloggedShapeMappersArrays(
            net.minecraft.block.Block[]... groups) {
        for (net.minecraft.block.Block[] group : groups) {
            registerWaterloggedShapeMappers(group);
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModernWaterRendering.installResourcePack(event.getSourceFile());
        ModelLoaderRegistry.registerLoader(CustomRawOreModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(CustomDeepslateOreModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(CustomStrippedWoodModelLoader.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(
                net.minecraft.entity.passive.EntitySquid.class, RenderAgeableSquid::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, RenderGlowSquid::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTurtle.class, RenderTurtle::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, RenderAxolotl::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, RenderGoat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCat.class, RenderCat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, RenderFox::new);
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityPiglin.class, xy177.farmersfuturedelight.client.render.RenderPiglin::new);
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityPiglinBrute.class, xy177.farmersfuturedelight.client.render.RenderPiglin::new);
        if (xy177.farmersfuturedelight.common.FFDPiglinEvents.modernZombified()) {
            RenderingRegistry.registerEntityRenderingHandler(net.minecraft.entity.monster.EntityPigZombie.class,
                    xy177.farmersfuturedelight.client.render.RenderPiglin::new);
        }
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityStrider.class,
                xy177.farmersfuturedelight.client.render.RenderStrider::new);
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityHoglin.class,
                manager -> new xy177.farmersfuturedelight.client.render.RenderHoglin<>(manager));
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityZoglin.class,
                manager -> new xy177.farmersfuturedelight.client.render.RenderHoglin<>(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityPanda.class, RenderPanda::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPillager.class, RenderPillager::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRavager.class, RenderRavager::new);
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityWanderingTrader.class,
                xy177.farmersfuturedelight.client.render.RenderWanderingTrader::new);
        RenderingRegistry.registerEntityRenderingHandler(
                xy177.farmersfuturedelight.common.entity.EntityTraderLlama.class,
                xy177.farmersfuturedelight.client.render.RenderTraderLlama::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBee.class, RenderBee::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPhantom.class, RenderPhantom::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowItemFrame.class,
                manager -> new RenderGlowItemFrame(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityCod.class, RenderCod::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySalmon.class, RenderSalmon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPufferfish.class,
                RenderPufferfish::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTropicalFish.class,
                RenderTropicalFish::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDolphin.class,
                RenderDolphin::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrowned.class,
                RenderDrowned::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrident.class,
                RenderTrident::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCrossbowArrow.class,
                xy177.farmersfuturedelight.client.render.RenderCrossbowArrow::new);
        FFDItems.TRIDENT.setTileEntityItemStackRenderer(new RenderTridentItem());
        if (FFDItems.isSignTextEnabled()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySign.class, new RenderSign());
        }
    }

    @Override
    public void init() {
        if (FFDItems.isBlockRegistered(FFDBlocks.LODESTONE)) {
            xy177.farmersfuturedelight.client.FFDLodestoneNeedle.install();
        }
        xy177.farmersfuturedelight.client.FFDCreativeInventory.refreshTabSnapshot();
        xy177.farmersfuturedelight.client.FFDModernMenus.install();
        xy177.farmersfuturedelight.client.friends.FFDFriendsMenu.install();
        AquaAcrobaticsWaterCompat.registerBiomeColors();
        if (FFDItems.isItemRegistered(FFDItems.CONDUIT)) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConduit.class,
                    new RenderConduit());
            FFDItems.CONDUIT.setTileEntityItemStackRenderer(new RenderConduitItem());
        }
        ClientRegistry.bindTileEntitySpecialRenderer(xy177.farmersfuturedelight.common.tile.TileEntityCampfire.class,
                new xy177.farmersfuturedelight.client.render.RenderCampfireContents());
        ClientRegistry.bindTileEntitySpecialRenderer(
                xy177.farmersfuturedelight.common.tile.TileEntityBell.class,
                new xy177.farmersfuturedelight.client.render.RenderBell());
        ClientRegistry.bindTileEntitySpecialRenderer(
                xy177.farmersfuturedelight.common.tile.TileEntityLectern.class,
                new xy177.farmersfuturedelight.client.render.RenderLecternBook());
        if (FFDItems.isItemRegistered(FFDItems.TRIDENT)) {
            for (RenderPlayer renderer : Minecraft.getMinecraft()
                    .getRenderManager().getSkinMap().values()) {
                renderer.addLayer(new LayerRiptide(renderer));
            }
        }
    }

    @Override
    public void handleVillagerState(int dimension, int entityId, net.minecraft.nbt.NBTTagCompound state) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != dimension) return;
            net.minecraft.entity.Entity entity = world.getEntityByID(entityId);
            if (entity != null && entity.getClass() == net.minecraft.entity.passive.EntityVillager.class) {
                xy177.farmersfuturedelight.common.registry.FFDVillagerTypes.receiveClientState(
                        (net.minecraft.entity.passive.EntityVillager) entity, state);
            } else if (entity != null
                    && entity.getClass() == net.minecraft.entity.monster.EntityZombieVillager.class) {
                xy177.farmersfuturedelight.common.registry.FFDZombieVillagerState.receiveClientState(
                        (net.minecraft.entity.monster.EntityZombieVillager) entity, state);
            }
        });
    }

    @Override
    public void handleMapDecorations(String mapName, net.minecraft.nbt.NBTTagCompound decorations) {
        Minecraft.getMinecraft().addScheduledTask(() ->
                xy177.farmersfuturedelight.client.FFDMapDecorationRenderer.receive(mapName, decorations));
    }

    @Override
    public void spawnGlowParticle(EntityGlowSquid squid) {
        double x = squid.posX + (squid.getRNG().nextDouble() * 2.0D - 1.0D) * 0.6D * squid.width;
        double y = squid.posY + squid.getRNG().nextDouble() * squid.height;
        double z = squid.posZ + (squid.getRNG().nextDouble() * 2.0D - 1.0D) * 0.6D * squid.width;
        Minecraft.getMinecraft().effectRenderer.addEffect(ParticleGlowSquid.ambient(squid.world, x, y, z));
    }

    @Override public void spawnSoulParticle(net.minecraft.entity.EntityLivingBase entity) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new xy177.farmersfuturedelight.client.particle.ParticleSoul(entity));
    }

    @Override
    public void spawnGlowInkParticles(EntityGlowSquid squid) {
        spawnInkParticles(squid, true);
    }

    @Override
    public void spawnSquidInkParticles(net.minecraft.entity.passive.EntitySquid squid) {
        spawnInkParticles(squid, false);
    }

    private void spawnInkParticles(net.minecraft.entity.passive.EntitySquid squid, boolean glow) {
        float pitch = squid.prevSquidPitch * ((float) Math.PI / 180.0F);
        float yaw = -squid.prevRenderYawOffset * ((float) Math.PI / 180.0F);
        Vec3d originOffset = new Vec3d(0.0D, -1.0D, 0.0D).rotatePitch(pitch).rotateYaw(yaw);
        Vec3d origin = originOffset.addVector(squid.posX, squid.posY + 0.5D, squid.posZ);

        for (int i = 0; i < 30; i++) {
            Vec3d direction = new Vec3d(
                    squid.getRNG().nextFloat() * 0.6F - 0.3F,
                    -1.0D,
                    squid.getRNG().nextFloat() * 0.6F - 0.3F)
                    .rotatePitch(pitch).rotateYaw(yaw);
            double scale = (squid.isChild() ? 0.1D : 0.3D) + squid.getRNG().nextFloat() * 2.0D;
            direction = direction.scale(scale * 0.1D);
            Minecraft.getMinecraft().effectRenderer.addEffect(
                    glow ? ParticleGlowSquid.ink(squid.world, origin.x, origin.y, origin.z,
                            direction.x, direction.y, direction.z)
                            : ParticleGlowSquid.normalInk(squid.world, origin.x, origin.y, origin.z,
                                    direction.x, direction.y, direction.z));
        }
    }

    @Override
    public void spawnSporeBlossomParticle(net.minecraft.world.World world, double x, double y,
                                          double z, boolean ambient) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        if (particleSetting == 2
                || particleSetting == 1 && world.rand.nextInt(3) == 0) {
            return;
        }
        ParticleSporeBlossom particle = ParticleSporeBlossom.create(world, x, y, z, ambient);
        if (particle != null) {
            minecraft.effectRenderer.addEffect(particle);
        }
    }

    @Override
    public void spawnHoneyDripParticle(net.minecraft.world.World world, double x, double y,
                                       double z) {
        if (!shouldSpawnBeeParticle(world, x, y, z)) {
            return;
        }
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleHoneyDrip.create(world, x, y, z));
    }

    @Override
    public void spawnFallingNectarParticle(net.minecraft.world.World world, double x, double y,
                                           double z) {
        if (!shouldSpawnBeeParticle(world, x, y, z)) {
            return;
        }
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleFallingNectar.create(world, x, y, z));
    }

    public static boolean shouldSpawnBeeParticle(net.minecraft.world.World world, double x, double y,
                                                 double z) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        return particleSetting != 2
                && (particleSetting != 1 || world.rand.nextInt(3) != 0)
                && minecraft.getRenderViewEntity() != null
                && minecraft.getRenderViewEntity().getDistanceSq(x, y, z) <= 1024.0D;
    }

    @Override
    public void spawnDripstoneParticle(net.minecraft.world.World world, double x, double y,
                                       double z, boolean lava) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleDripstone.create(world, x, y, z, lava));
    }

    @Override
    public void spawnReversePortalParticle(net.minecraft.world.World world, double x, double y, double z, double speed) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == world && minecraft.effectRenderer != null && minecraft.gameSettings.particleSetting < 2) {
            minecraft.effectRenderer.addEffect(new xy177.farmersfuturedelight.client.particle.ParticleReversePortal(world, x, y, z, speed));
        }
    }

    @Override
    public void spawnCryingObsidianParticle(net.minecraft.world.World world, double x, double y,
                                            double z) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                ParticleCryingObsidian.create(world, x, y, z));
    }

    @Override
    public void spawnSmallFlameParticle(net.minecraft.world.World world, double x, double y,
                                        double z) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        if (particleSetting == 2
                || particleSetting == 1 && world.rand.nextInt(3) == 0
                || minecraft.getRenderViewEntity() == null
                || minecraft.getRenderViewEntity().getDistanceSq(x, y, z) > 1024.0D) {
            return;
        }
        minecraft.effectRenderer.addEffect(ParticleSmallFlame.create(world, x, y, z));
    }

    @Override
    public void spawnSoulFlameParticle(net.minecraft.world.World world, double x, double y, double z) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int setting = minecraft.gameSettings.particleSetting;
        if (setting == 2 || setting == 1 && world.rand.nextInt(3) == 0
                || minecraft.getRenderViewEntity() == null
                || minecraft.getRenderViewEntity().getDistanceSq(x, y, z) > 1024.0D) return;
        minecraft.effectRenderer.addEffect(
                xy177.farmersfuturedelight.client.particle.ParticleSoulFlame.create(world, x, y, z));
    }

    @Override
    public void spawnCampfireSmoke(net.minecraft.world.World world, double x, double y,
                                   double z, boolean signalFire) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int particleSetting = minecraft.gameSettings.particleSetting;
        if (particleSetting == 2
                || particleSetting == 1 && world.rand.nextInt(3) != 0
                || minecraft.getRenderViewEntity() == null
                || minecraft.getRenderViewEntity().getDistanceSq(x, y, z) > 4096.0D) {
            return;
        }
        minecraft.effectRenderer.addEffect(
                ParticleCampfireSmoke.create(world, x, y, z, signalFire));
    }

    @Override
    public void spawnConduitParticle(net.minecraft.world.World world, double x, double y,
                                     double z, double motionX, double motionY, double motionZ) {
        Minecraft.getMinecraft().effectRenderer.addEffect(ParticleConduit.create(
                world, x, y, z, motionX, motionY, motionZ));
    }

    @Override
    public void spawnBubbleColumnParticle(net.minecraft.world.World world, double x, double y,
                                          double z, double motionX, double motionY,
                                          double motionZ, boolean dragDown) {
        Minecraft.getMinecraft().effectRenderer.addEffect(dragDown
                ? ParticleCurrentDown.create(world, x, y, z)
                : ParticleBubbleColumnUp.create(world, x, y, z,
                        motionX, motionY, motionZ));
    }

    @Override
    public void handleDripstoneParticle(double x, double y, double z, boolean lava) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null) {
                spawnDripstoneParticle(world, x, y, z, lava);
            }
        });
    }

    @Override
    public void handleVerticalBiomes(int dimension, int chunkX, int chunkZ,
                                    xy177.farmersfuturedelight.common.world.biome.FFDVerticalBiomeData biomes) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension) {
                FFDVerticalBiomeManager.receiveClientData(world, chunkX, chunkZ, biomes);
            }
        });
    }

    @Override
    public void handleRiptideState(int entityId, int ticks) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world == null) {
                return;
            }
            net.minecraft.entity.Entity entity = world.getEntityByID(entityId);
            if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
                xy177.farmersfuturedelight.common.FFDRiptide.setClientTicks(
                        (net.minecraft.entity.player.EntityPlayer) entity, ticks);
            }
        });
    }

    @Override
    public void handleImmediateRespawnRule(boolean enabled) {
        Minecraft.getMinecraft().addScheduledTask(
                () -> xy177.farmersfuturedelight.client.FFDClientGameRules
                        .setImmediateRespawn(enabled));
    }

    @Override
    public void handleRavagerRoar(EntityRavager ravager) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft minecraft = Minecraft.getMinecraft();
            EntityPlayer player = minecraft.player;
            if (player != null && !player.isDead && player.world == ravager.world
                    && ravager.getEntityBoundingBox().grow(4.0D)
                    .intersects(player.getEntityBoundingBox())) {
                ravager.applyClientRoarKnockback(player);
            }
        });
    }

    @Override
    public void handleDrownedConverting(int dimension, int entityId, boolean converting) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != dimension) {
                return;
            }
            net.minecraft.entity.Entity entity = world.getEntityByID(entityId);
            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                entity.getEntityData().setBoolean(
                        xy177.farmersfuturedelight.common.FFDGameplayEvents.DROWNED_CONVERTING_TAG,
                        converting);
            }
        });
    }

    @Override
    public void handleMooshroomState(int dimension, int entityId, boolean brown) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != dimension) {
                return;
            }
            net.minecraft.entity.Entity entity = world.getEntityByID(entityId);
            if (entity instanceof net.minecraft.entity.passive.EntityMooshroom) {
                xy177.farmersfuturedelight.common.entity.FFDMooshroomData.setBrown(
                        (net.minecraft.entity.passive.EntityMooshroom) entity, brown);
            }
        });
    }

    @Override
    public boolean isRedstoneDot(BlockPos pos) {
        net.minecraft.world.World world = Minecraft.getMinecraft().world;
        return world != null && xy177.farmersfuturedelight.common.FFDRedstoneWire.isDot(world, pos);
    }

    @Override
    public void handleRedstoneDots(int dimension, int chunkX, int chunkZ, long[] positions) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension)
                xy177.farmersfuturedelight.common.FFDRedstoneWire.apply(world, chunkX, chunkZ, positions);
        });
    }

    @Override
    public void handleAgeLockState(int dimension, int entityId, boolean locked, int timer) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world == null || world.provider.getDimension() != dimension) return;
            net.minecraft.entity.Entity entity = world.getEntityByID(entityId);
            if (entity instanceof net.minecraft.entity.EntityLivingBase) {
                xy177.farmersfuturedelight.common.entity.FFDAgeLock.applyState(
                        (net.minecraft.entity.EntityLivingBase) entity, locked, timer);
            }
        });
    }

    @Override
    public void spawnAgeLockParticle(net.minecraft.entity.EntityLivingBase entity, boolean locked) {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new xy177.farmersfuturedelight.client.particle.ParticleAgeLock(entity, locked));
    }

    @Override
    public void handleFluidloggedChunk(int dimension, int chunkX, int chunkZ,
                                       java.util.Map<Long, String> fluids) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension) {
                FFDFluidloggedData.receiveChunk(world, chunkX, chunkZ, fluids);
            }
        });
    }

    @Override
    public void handleFluidloggedUpdate(int dimension, BlockPos pos, String fluidName) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension) {
                FFDFluidloggedData.receiveUpdate(world, pos, fluidName);
            }
        });
    }

    @Override
    public net.minecraft.world.World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void handleStoredFluidChunk(int dimension, int x, int z,
                                       net.minecraft.nbt.NBTTagCompound data, boolean replace) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            net.minecraft.world.World world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimension)
                xy177.farmersfuturedelight.common.fluid.FFDStoredFluidStates.receive(world, x, z, data, replace);
        });
    }

    @Override
    public void handleMigrationPrompt(String token, String host, String fluid, boolean convertible) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().world != null)
                Minecraft.getMinecraft().displayGuiScreen(
                        new xy177.farmersfuturedelight.client.FluidMigrationScreen(
                                token, host, fluid, convertible));
        });
    }

    @Override
    public String getLanguageCode() {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage()
                .getLanguageCode();
    }
}
