package xy177.farmersfuturedelight.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.VarInsnNode;
import xy177.farmersfuturedelight.common.worldgen.FFDMineshaftHooks;
import xy177.farmersfuturedelight.common.FFDConfig;

public final class FFDHeightTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FFD Height Core");
    private static final String HOOKS = Type.getInternalName(FFDHeightHooks.class);
    private static final String GAMEPLAY_HOOKS = Type.getInternalName(FFDGameplayHooks.class);
    private static final String CLIENT_HOOKS = Type.getInternalName(FFDClientHeightHooks.class);
    private static final String PULSAR_HOOKS = Type.getInternalName(FFDPulsarHooks.class);
    private static final String MINESHAFT_HOOKS = Type.getInternalName(FFDMineshaftHooks.class);
    private static final String VIEW_FRUSTUM_ACCESS = Type.getInternalName(FFDViewFrustumAccess.class);
    private static final String RENDER_GLOBAL_ACCESS = Type.getInternalName(FFDRenderGlobalAccess.class);
    private static final String CONFIG = Type.getInternalName(FFDConfig.class);
    private static final String STORAGE = "net/minecraft/world/chunk/storage/ExtendedBlockStorage";
    private static final String CAVE_BIOMES_EXTENDED_WORLD_TYPE =
            "net/celestiald/cavebiomes/api/IExtendedHeightWorldType";
    private static final boolean CAVE_BIOMES_HEIGHT_CORE = FFDCoreCompat.isCaveBiomesApiPresent();
    private static final boolean DEPTHS_UPDATE_HEIGHT_CORE =
            FFDCoreCompat.isDepthsUpdateHeightCorePresent();
    private static final boolean CAVES_NOT_CLIFFS = FFDCoreCompat.isCavesNotCliffsPresent();

    private static final Set<String> WORLD_CLASS = names("net.minecraft.world.World");
    private static final Set<String> COMMAND_BASE_CLASS = names("net.minecraft.command.CommandBase");
    private static final Set<String> HEIGHT_BOUNDED_COMMAND_CLASSES = names(
            "net.minecraft.command.CommandFill",
            "net.minecraft.command.CommandClone",
            "net.minecraft.command.CommandCompare");
    private static final Set<String> WORLD_PROVIDER_CLASS = names("net.minecraft.world.WorldProvider");
    private static final Set<String> WORLD_SERVER_CLASS = names("net.minecraft.world.WorldServer");
    private static final Set<String> WORLD_ENTITY_SPAWNER_CLASS = names("net.minecraft.world.WorldEntitySpawner");
    private static final Set<String> BLOCK_CLASS = names("net.minecraft.block.Block");
    private static final Set<String> BLOCK_PISTON_BASE_CLASS = names("net.minecraft.block.BlockPistonBase");
    private static final Set<String> BLOCK_FALLING_CLASS = names("net.minecraft.block.BlockFalling");
    private static final Set<String> BLOCK_DRAGON_EGG_CLASS = names("net.minecraft.block.BlockDragonEgg");
    private static final Set<String> BLOCK_PORTAL_CLASS = names("net.minecraft.block.BlockPortal");
    private static final Set<String> BLOCK_PORTAL_SIZE_CLASS = names("net.minecraft.block.BlockPortal$Size");
    private static final Set<String> BLOCK_MUSHROOM_CLASS = names("net.minecraft.block.BlockMushroom");
    private static final Set<String> BLOCK_LILY_PAD_CLASS = names("net.minecraft.block.BlockLilyPad");
    private static final Set<String> BLOCK_GRASS_CLASS = names("net.minecraft.block.BlockGrass");
    private static final Set<String> BLOCK_DYNAMIC_LIQUID_CLASS = names(
            "net.minecraft.block.BlockDynamicLiquid");
    private static final Set<String> BLOCK_STATIC_LIQUID_CLASS = names("net.minecraft.block.BlockStaticLiquid");
    private static final Set<String> BLOCK_CHORUS_FLOWER_CLASS = names("net.minecraft.block.BlockChorusFlower");
    private static final Set<String> PATH_NAVIGATE_GROUND_CLASS = names("net.minecraft.pathfinding.PathNavigateGround");
    private static final Set<String> WALK_NODE_PROCESSOR_CLASS = names("net.minecraft.pathfinding.WalkNodeProcessor");
    private static final Set<String> ENTITY_LIVING_BASE_CLASS = names("net.minecraft.entity.EntityLivingBase");
    private static final Set<String> ENTITY_SHULKER_CLASS = names("net.minecraft.entity.monster.EntityShulker");
    private static final Set<String> ENTITY_XP_ORB_CLASS = names("net.minecraft.entity.item.EntityXPOrb");
    private static final Set<String> CHUNK_CLASS = names("net.minecraft.world.chunk.Chunk");
    private static final Set<String> EMPTY_CHUNK_CLASS = names("net.minecraft.world.chunk.EmptyChunk");
    private static final Set<String> ENTITY_CLASS = names("net.minecraft.entity.Entity");
    private static final Set<String> BOAT_CLASS = names("net.minecraft.entity.item.EntityBoat");
    private static final Set<String> MINECART_CLASS = names("net.minecraft.entity.item.EntityMinecart");
    private static final Set<String> FALLING_BLOCK_CLASS = names("net.minecraft.entity.item.EntityFallingBlock");
    private static final Set<String> CAULDRON_CLASS = names("net.minecraft.block.BlockCauldron");
    private static final Set<String> JUKEBOX_CLASS = names("net.minecraft.block.BlockJukebox");
    private static final Set<String> PLAYER_MP_CLASS = names("net.minecraft.entity.player.EntityPlayerMP");
    private static final Set<String> PLAYER_LIST_CLASS = names("net.minecraft.server.management.PlayerList");
    private static final Set<String> TELEPORTER_CLASS = names("net.minecraft.world.Teleporter");
    private static final Set<String> CHUNK_CACHE_CLASS = names("net.minecraft.world.ChunkCache");
    private static final Set<String> ANVIL_CLASS = names("net.minecraft.world.chunk.storage.AnvilChunkLoader");
    private static final Set<String> CHUNK_PACKET_CLASS = names("net.minecraft.network.play.server.SPacketChunkData");
    private static final Set<String> VIEW_FRUSTUM_CLASS = names("net.minecraft.client.renderer.ViewFrustum");
    private static final Set<String> RENDER_GLOBAL_CLASS = names("net.minecraft.client.renderer.RenderGlobal");
    private static final Set<String> RENDER_CHUNK_CLASS = names(
            "net.minecraft.client.renderer.chunk.RenderChunk");
    private static final Set<String> MINECRAFT_CLIENT_CLASS = names("net.minecraft.client.Minecraft");
    private static final Set<String> OPTIFINE_RENDER_CHUNK_UTILS_CLASS = names(
            "net.optifine.util.RenderChunkUtils");
    private static final Set<String> OPTIFINE_SHADER_VERTEX_BUILDER_CLASS = names(
            "net.optifine.shaders.SVertexBuilder");
    private static final Set<String> HWYLA_BLOCK_HUD_CLASSES = names(
            "mcp.mobius.waila.addons.core.HUDHandlerBlocks");
    private static final Set<String> PLAYER_CHUNK_ENTRY_CLASS = names("net.minecraft.server.management.PlayerChunkMapEntry");
    private static final Set<String> MULTI_BLOCK_PACKET_CLASS = names("net.minecraft.network.play.server.SPacketMultiBlockChange");
    private static final Set<String> MULTI_BLOCK_DATA_CLASS = names("net.minecraft.network.play.server.SPacketMultiBlockChange$BlockUpdateData");
    private static final Set<String> STRUCTURE_START_CLASS = names(
            "net.minecraft.world.gen.structure.StructureStart");
    private static final Set<String> ANCIENT_WARFARE_BUILDER_CLASSES = names(
            "net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder");
    private static final Set<String> ANCIENT_WARFARE_WORLD_STRUCTURE_GENERATOR_CLASSES = names(
            "net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator");
    private static final Set<String> ANCIENT_WARFARE_SMOOTHING_MATRIX_CLASSES = names(
            "net.shadowmage.ancientwarfare.structure.template.build.validation.border.SmoothingMatrix");
    private static final Set<String> ICE_AND_FIRE_WORLDGEN_EVENTS_CLASSES = names(
            "com.github.alexthe666.iceandfire.event.WorldGenEvents");
    private static final Set<String> FORGE_HOOKS_CLIENT_CLASS = names(
            "net.minecraftforge.client.ForgeHooksClient");
    private static final Set<String> OPTIMIZED_WORLD_RENDERER_CLASSES = names(
            "org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer",
            "com.dhj.actinium.render.terrain.ActiniumWorldRenderer");
    private static final Set<String> OPTIMIZED_SECTION_MANAGER_CLASSES = names(
            "org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager",
            "com.dhj.actinium.render.terrain.VintageRenderSectionManager");
    private static final Set<String> OPTIMIZED_WORLD_SLICE_CLASSES = names(
            "org.taumc.celeritas.impl.world.WorldSlice",
            "com.dhj.actinium.world.WorldSlice");
    private static final Set<String> OPTIMIZED_CLONED_SECTION_CLASSES = names(
            "org.taumc.celeritas.impl.world.cloned.ClonedChunkSection",
            "com.dhj.actinium.world.cloned.ClonedChunkSection");
    private static final Set<String> PULSAR_WORLD_UTIL_CLASSES = names(
            "com.sumirelabs.pulsar.util.WorldUtil");
    private static final Set<String> ALFHEIM_WORLD_MIXIN_CLASSES = names(
            "dev.redstudio.alfheim.mixin.WorldMixin");
    private static final Set<String> ALFHEIM_CHUNK_MIXIN_CLASSES = names(
            "dev.redstudio.alfheim.mixin.ChunkMixin");
    private static final Set<String> ALFHEIM_CHUNK_CACHE_MIXIN_CLASSES = names(
            "dev.redstudio.alfheim.mixin.ChunkCacheMixin");
    private static final Set<String> NOTHIRIUM_WORLD_UTIL_CLASSES = names(
            "meldexun.nothirium.mc.util.WorldUtil");
    private static final Set<String> NOTHIRIUM_RENDER_CHUNK_CLASSES = names(
            "meldexun.nothirium.mc.renderer.chunk.RenderChunk");
    private static final Set<String> NOTHIRIUM_RENDER_MANAGER_CLASSES = names(
            "meldexun.nothirium.mc.renderer.ChunkRenderManager");
    private static final Set<String> NOTHIRIUM_COMPILE_TASK_CLASSES = names(
            "meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile");
    private static final Set<String> NOTHIRIUM_VERTICAL_DIRECTION_CLASSES = names(
            "meldexun.nothirium.util.Direction$1",
            "meldexun.nothirium.util.Direction$2");
    private static final Set<String> DISTANT_HORIZONS_CHUNK_WRAPPER_CLASSES = names(
            "com.seibel.distanthorizons.common.wrappers.chunk.ChunkWrapper");
    private static final Set<String> DISTANT_HORIZONS_LEVEL_WRAPPER_CLASSES = names(
            "com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper",
            "com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper");
    private static final Set<String> NETHER_API_CHUNK_GENERATOR_CLASSES = names(
            "git.jbredwards.nether_api.mod.common.world.gen.ChunkGeneratorNether");
    private static final Map<String, String> PULSAR_DEV_MIXIN_TARGETS = pulsarDevMixinTargets();
    private static final Map<String, String> PULSAR_DEV_METHOD_NAMES = pulsarDevMethodNames();
    private static final Map<String, String> PULSAR_DEV_FIELD_NAMES = pulsarDevFieldNames();
    private static final Map<String, String> PULSAR_DEV_CLASS_METHOD_NAMES =
            pulsarDevClassMethodNames();
    private static final Map<String, String> PULSAR_DEV_CLASS_FIELD_NAMES =
            pulsarDevClassFieldNames();
    private static final Set<String> XAERO_WRITER_CLASSES = names(
            "xaero.common.minimap.write.MinimapWriter",
            "xaero.map.MapWriter");
    private static final Set<String> XAERO_CAVE_START_CLASSES = names(
            "xaero.map.misc.CaveStartCalculator");
    private static final Set<String> XAERO_MAP_PROCESSOR_CLASSES = names(
            "xaero.map.MapProcessor");
    private static final Set<String> XAERO_WORLD_DATA_READER_CLASSES = names(
            "xaero.map.file.worldsave.WorldDataReader");
    private static final Set<String> XAERO_MAP_SAVE_LOAD_CLASSES = names(
            "xaero.map.file.MapSaveLoad");
    private static final Set<String> XAERO_MINIMAP_TILE_CLASSES = names(
            "xaero.common.minimap.region.MinimapTile");
    private static final Set<String> XAERO_MAP_TILE_CHUNK_CLASSES = names(
            "xaero.map.region.MapTileChunk");
    private static final Set<String> XAERO_MAP_BLOCK_CLASSES = names(
            "xaero.map.region.MapBlock");
    private static final Set<String> XAERO_CAVE_OPTIONS_CLASSES = names(
            "xaero.map.gui.GuiCaveModeOptions");
    private static final Set<String> VOXELMAP_MAP_CLASSES = names(
            "com.mamiyaotaru.voxelmap.Map");
    private static final Set<String> VOXELMAP_DATA_CLASSES = names(
            "com.mamiyaotaru.voxelmap.persistent.CompressibleMapData");
    private static final Set<String> VOXELMAP_WAYPOINT_GUI_CLASSES = names(
            "com.mamiyaotaru.voxelmap.gui.GuiWaypoints");
    private static final Set<String> VOXELMAP_PERSISTENT_GUI_CLASSES = names(
            "com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap");
    private static final Set<String> VOXELMAP_COMMAND_CLASSES = names(
            "com.mamiyaotaru.voxelmap.util.CommandUtils");
    private static final Set<String> FFD_WORLD_TYPE_CLASS = names(
            "xy177.farmersfuturedelight.common.world.WorldTypeExtended");
    private static final Set<String> CAVE_BIOMES_OWNED_HEIGHT_CLASSES = names(
            "net.minecraft.world.World",
            "net.minecraft.command.CommandBase",
            "net.minecraft.command.CommandFill",
            "net.minecraft.command.CommandClone",
            "net.minecraft.command.CommandCompare",
            "net.minecraft.world.WorldEntitySpawner",
            "net.minecraft.world.chunk.Chunk",
            "net.minecraft.entity.Entity",
            "net.minecraft.entity.item.EntityMinecart",
            "net.minecraft.entity.item.EntityFallingBlock",
            "net.minecraft.entity.player.EntityPlayerMP",
            "net.minecraft.server.management.PlayerList",
            "net.minecraft.world.Teleporter",
            "net.minecraft.world.ChunkCache",
            "net.minecraft.world.chunk.storage.AnvilChunkLoader",
            "net.minecraft.network.play.server.SPacketChunkData",
            "net.minecraft.client.renderer.ViewFrustum",
            "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.server.management.PlayerChunkMapEntry",
            "net.minecraft.network.play.server.SPacketMultiBlockChange",
            "net.minecraft.network.play.server.SPacketMultiBlockChange$BlockUpdateData");
    private static final Set<String> DEPTHS_UPDATE_OWNED_HEIGHT_CLASSES = names(
            "net.minecraft.world.World",
            "net.minecraft.world.WorldEntitySpawner",
            "net.minecraft.entity.Entity",
            "net.minecraft.entity.item.EntityFallingBlock",
            "net.minecraft.world.ChunkCache",
            "net.minecraft.world.chunk.storage.AnvilChunkLoader",
            "net.minecraft.network.play.server.SPacketChunkData",
            "net.minecraft.client.renderer.ViewFrustum",
            "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.server.management.PlayerChunkMapEntry",
            "net.minecraft.network.play.server.SPacketMultiBlockChange",
            "net.minecraft.network.play.server.SPacketMultiBlockChange$BlockUpdateData");

    private static Set<String> names(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    private static Map<String, String> pulsarDevMixinTargets() {
        Map<String, String> result = new HashMap<>();
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockFaceLight",
                "net/minecraft/block/Block");
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockModelRendererLight",
                "net/minecraft/client/renderer/BlockModelRenderer");
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockRenderLight",
                "net/minecraft/block/Block");
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockSlabFaceLight",
                "net/minecraft/block/BlockSlab");
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockStairsFaceLight",
                "net/minecraft/block/BlockStairs");
        result.put("com.sumirelabs.pulsar.mixin.MixinBlockStateImplementation",
                "net/minecraft/block/state/BlockStateContainer$StateImplementation");
        result.put("com.sumirelabs.pulsar.mixin.MixinCeleritasWorldSlice",
                "org/taumc/celeritas/impl/world/WorldSlice");
        result.put("com.sumirelabs.pulsar.mixin.MixinChunk",
                "net/minecraft/world/chunk/Chunk");
        result.put("com.sumirelabs.pulsar.mixin.MixinChunkCacheRenderLight",
                "net/minecraft/world/ChunkCache");
        result.put("com.sumirelabs.pulsar.mixin.MixinChunkSectionChanges",
                "net/minecraft/world/chunk/Chunk");
        result.put("com.sumirelabs.pulsar.mixin.MixinChunkVanillaLighting",
                "net/minecraft/world/chunk/Chunk");
        result.put("com.sumirelabs.pulsar.mixin.MixinPlayerChunkMapEntry",
                "net/minecraft/server/management/PlayerChunkMapEntry");
        result.put("com.sumirelabs.pulsar.mixin.MixinRenderJSONPaintingLight",
                "git/jbredwards/jsonpaintings/mod/client/RenderJSONPainting");
        result.put("com.sumirelabs.pulsar.mixin.MixinRenderPaintingLight",
                "net/minecraft/client/renderer/entity/RenderPainting");
        result.put("com.sumirelabs.pulsar.mixin.MixinSPacketChunkData",
                "net/minecraft/network/play/server/SPacketChunkData");
        result.put("com.sumirelabs.pulsar.mixin.MixinWorld",
                "net/minecraft/world/World");
        result.put("com.sumirelabs.pulsar.mixin.MixinWorldEntitySpawner",
                "net/minecraft/world/WorldEntitySpawner");
        result.put("com.sumirelabs.pulsar.mixin.MixinWorldRenderLight",
                "net/minecraft/world/World");
        result.put("com.sumirelabs.pulsar.mixin.MixinWorldServer",
                "net/minecraft/world/WorldServer");
        return result;
    }

    private static Map<String, String> pulsarDevMethodNames() {
        Map<String, String> result = new HashMap<>();
        putPulsarMethod(result, "net/minecraft/block/Block", "getPackedLightmapCoords",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;"
                        + "Lnet/minecraft/util/math/BlockPos;)I", "func_185484_c");
        putPulsarMethod(result, "net/minecraft/block/Block", "getAmbientOcclusionLightValue",
                "(Lnet/minecraft/block/state/IBlockState;)F", "func_185485_f");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getBlockStorageArray",
                "()[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", "func_76587_i");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getLightFor",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                "func_177413_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "onLoad", "()V",
                "func_76631_c");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "onUnload", "()V",
                "func_76623_d");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "read",
                "(Lnet/minecraft/network/PacketBuffer;IZ)V", "func_186033_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "setBlockState",
                "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)"
                        + "Lnet/minecraft/block/state/IBlockState;", "func_177436_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getTopFilledSegment", "()I",
                "func_76625_h");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getBlockState",
                "(III)Lnet/minecraft/block/state/IBlockState;", "func_186032_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "markDirty", "()V",
                "func_76630_e");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "generateSkylightMap", "()V",
                "func_76603_b");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "relightBlock", "(III)V",
                "func_76615_h");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "recheckGaps", "(Z)V",
                "func_150803_c");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "enqueueRelightChecks", "()V",
                "func_76594_o");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "checkLight", "()V",
                "func_150809_p");
        putPulsarMethod(result, "net/minecraft/world/ChunkCache", "getLightFor",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                "func_175628_b");
        putPulsarMethod(result, "net/minecraft/world/ChunkCache", "getBlockState",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                "func_180495_p");
        putPulsarMethod(result, "net/minecraft/world/ChunkCache", "getLightForExt",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                "func_175629_a");
        putPulsarMethod(result, "net/minecraft/server/management/PlayerChunkMapEntry",
                "sendToPlayers", "()Z", "func_187272_b");
        putPulsarMethod(result, "net/minecraft/client/renderer/entity/RenderPainting",
                "renderPainting", "(Lnet/minecraft/entity/item/EntityPainting;IIII)V",
                "func_77010_a");
        putPulsarMethod(result, "net/minecraft/client/renderer/BufferBuilder", "begin",
                "(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V", "func_181668_a");
        putPulsarMethod(result, "net/minecraft/client/renderer/BufferBuilder", "pos",
                "(DDD)Lnet/minecraft/client/renderer/BufferBuilder;", "func_181662_b");
        putPulsarMethod(result, "net/minecraft/client/renderer/BufferBuilder", "normal",
                "(FFF)Lnet/minecraft/client/renderer/BufferBuilder;", "func_181663_c");
        putPulsarMethod(result, "net/minecraft/client/renderer/BufferBuilder", "lightmap",
                "(II)Lnet/minecraft/client/renderer/BufferBuilder;", "func_187314_a");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getBlock",
                "()Lnet/minecraft/block/Block;", "func_177230_c");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getValue",
                "(Lnet/minecraft/block/properties/IProperty;)Ljava/lang/Comparable;",
                "func_177229_b");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getLightValue", "()I",
                "func_185906_d");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "useNeighborBrightness",
                "()Z", "func_185916_f");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "isBlockNormalCube",
                "()Z", "func_185898_k");
        putPulsarMethod(result, "net/minecraft/block/BlockSlab", "isFullCube",
                "(Lnet/minecraft/block/state/IBlockState;)Z", "func_149686_d");
        putPulsarMethod(result, "net/minecraft/util/EnumFacing", "getAxis",
                "()Lnet/minecraft/util/EnumFacing$Axis;", "func_176740_k");
        putPulsarMethod(result, "net/minecraft/util/EnumFacing", "getXOffset", "()I",
                "func_82601_c");
        putPulsarMethod(result, "net/minecraft/util/EnumFacing", "getYOffset", "()I",
                "func_96559_d");
        putPulsarMethod(result, "net/minecraft/util/EnumFacing", "getZOffset", "()I",
                "func_82599_e");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getX", "()I",
                "func_177958_n");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getY", "()I",
                "func_177956_o");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getZ", "()I",
                "func_177952_p");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "offset",
                "(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;",
                "func_177972_a");
        putPulsarMethod(result, "net/minecraft/world/IBlockAccess", "getCombinedLight",
                "(Lnet/minecraft/util/math/BlockPos;I)I", "func_175626_b");
        putPulsarMethod(result, "net/minecraft/world/World", "markBlocksDirtyVertical",
                "(IIII)V", "func_72975_g");
        putPulsarMethod(result, "net/minecraft/world/WorldProvider", "hasSkyLight", "()Z",
                "func_191066_m");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getWorld",
                "()Lnet/minecraft/world/World;", "func_177412_p");
        putPulsarMethod(result, "net/minecraft/world/chunk/NibbleArray", "getData", "()[B",
                "func_177481_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/NibbleArray", "set", "(IIII)V",
                "func_76581_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
                "getBlockLight", "()Lnet/minecraft/world/chunk/NibbleArray;", "func_76661_k");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
                "getSkyLight", "()Lnet/minecraft/world/chunk/NibbleArray;", "func_76671_l");
        putPulsarMethod(result, "net/minecraft/world/World", "checkLightFor",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z",
                "func_180500_c");
        putPulsarMethod(result, "net/minecraft/world/World", "getLightFor",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                "func_175642_b");
        putPulsarMethod(result, "net/minecraft/world/World", "getBlockState",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                "func_180495_p");
        putPulsarMethod(result, "net/minecraft/world/World", "getLightFromNeighborsFor",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                "func_175705_a");
        putPulsarMethod(result, "net/minecraft/world/WorldEntitySpawner", "findChunksForSpawning",
                "(Lnet/minecraft/world/WorldServer;ZZZ)I", "func_77192_a");
        putPulsarMethod(result, "net/minecraft/world/WorldServer", "tick", "()V",
                "func_72835_b");
        return result;
    }

    private static Map<String, String> pulsarDevFieldNames() {
        Map<String, String> result = new HashMap<>();
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "x", "I", "field_76635_g");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "z", "I", "field_76647_h");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "isLightPopulated", "Z",
                "field_150814_l");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "isTerrainPopulated", "Z",
                "field_76646_k");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "heightMap", "[I",
                "field_76634_f");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "precipitationHeightMap", "[I",
                "field_76638_b");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "heightMapMinimum", "I",
                "field_82912_p");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "world",
                "Lnet/minecraft/world/World;", "field_76637_e");
        putPulsarField(result, "net/minecraft/server/management/PlayerChunkMapEntry", "chunk",
                "Lnet/minecraft/world/chunk/Chunk;", "field_187286_f");
        putPulsarField(result, "net/minecraft/world/WorldEntitySpawner",
                "eligibleChunksForSpawning", "Ljava/util/Set;", "field_77193_b");
        putPulsarField(result, "net/minecraft/block/BlockSlab", "HALF",
                "Lnet/minecraft/block/properties/PropertyEnum;", "field_176554_a");
        putPulsarField(result, "net/minecraft/block/BlockStairs", "HALF",
                "Lnet/minecraft/block/properties/PropertyEnum;", "field_176308_b");
        putPulsarField(result, "net/minecraft/util/EnumFacing", "VALUES",
                "[Lnet/minecraft/util/EnumFacing;", "field_82609_l");
        putPulsarField(result, "net/minecraft/util/math/ChunkPos", "x", "I",
                "field_77276_a");
        putPulsarField(result, "net/minecraft/util/math/ChunkPos", "z", "I",
                "field_77275_b");
        putPulsarField(result, "net/minecraft/world/EnumSkyBlock", "defaultLightValue", "I",
                "field_77198_c");
        putPulsarField(result, "net/minecraft/world/World", "isRemote", "Z",
                "field_72995_K");
        putPulsarField(result, "net/minecraft/world/World", "provider",
                "Lnet/minecraft/world/WorldProvider;", "field_73011_w");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "NULL_BLOCK_STORAGE",
                "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", "field_186036_a");
        return result;
    }

    private static Map<String, String> pulsarDevClassMethodNames() {
        Map<String, String> result = new HashMap<>();
        putPulsarMethod(result, "net/minecraft/block/Block", "getDefaultState",
                "()Lnet/minecraft/block/state/IBlockState;", "func_176223_P");
        putPulsarMethod(result, "net/minecraft/block/Block", "getMetaFromState",
                "(Lnet/minecraft/block/state/IBlockState;)I", "func_176201_c");
        putPulsarMethod(result, "net/minecraft/block/Block", "getStateFromMeta",
                "(I)Lnet/minecraft/block/state/IBlockState;", "func_176203_a");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getBlock",
                "()Lnet/minecraft/block/Block;", "func_177230_c");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getLightOpacity",
                "()I", "func_185891_c");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "getLightValue",
                "()I", "func_185906_d");
        putPulsarMethod(result, "net/minecraft/block/state/IBlockState", "isFullCube",
                "()Z", "func_185917_h");
        putPulsarMethod(result, "net/minecraft/client/Minecraft", "getIntegratedServer",
                "()Lnet/minecraft/server/integrated/IntegratedServer;", "func_71401_C");
        putPulsarMethod(result, "net/minecraft/client/Minecraft", "getMinecraft",
                "()Lnet/minecraft/client/Minecraft;", "func_71410_x");
        putPulsarMethod(result, "net/minecraft/client/renderer/vertex/VertexFormat", "addElement",
                "(Lnet/minecraft/client/renderer/vertex/VertexFormatElement;)"
                        + "Lnet/minecraft/client/renderer/vertex/VertexFormat;",
                "func_181721_a");
        putPulsarMethod(result, "net/minecraft/command/ICommandSender", "getEntityWorld",
                "()Lnet/minecraft/world/World;", "func_130014_f_");
        putPulsarMethod(result, "net/minecraft/command/ICommandSender", "sendMessage",
                "(Lnet/minecraft/util/text/ITextComponent;)V", "func_145747_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "getByte",
                "(Ljava/lang/String;)B", "func_74771_c");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "getByteArray",
                "(Ljava/lang/String;)[B", "func_74770_j");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "getCompoundTag",
                "(Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;", "func_74775_l");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "getInteger",
                "(Ljava/lang/String;)I", "func_74762_e");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "getTagList",
                "(Ljava/lang/String;I)Lnet/minecraft/nbt/NBTTagList;", "func_150295_c");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "hasKey",
                "(Ljava/lang/String;I)Z", "func_150297_b");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "setByte",
                "(Ljava/lang/String;B)V", "func_74774_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "setByteArray",
                "(Ljava/lang/String;[B)V", "func_74773_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "setInteger",
                "(Ljava/lang/String;I)V", "func_74768_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagCompound", "setTag",
                "(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V", "func_74782_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagList", "appendTag",
                "(Lnet/minecraft/nbt/NBTBase;)V", "func_74742_a");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagList", "getCompoundTagAt",
                "(I)Lnet/minecraft/nbt/NBTTagCompound;", "func_150305_b");
        putPulsarMethod(result, "net/minecraft/nbt/NBTTagList", "tagCount",
                "()I", "func_74745_c");
        putPulsarMethod(result, "net/minecraft/server/MinecraftServer", "addScheduledTask",
                "(Ljava/lang/Runnable;)Lcom/google/common/util/concurrent/ListenableFuture;",
                "func_152344_a");
        putPulsarMethod(result, "net/minecraft/server/MinecraftServer", "getWorld",
                "(I)Lnet/minecraft/world/WorldServer;", "func_71218_a");
        putPulsarMethod(result, "net/minecraft/server/management/PlayerChunkMap", "getEntry",
                "(II)Lnet/minecraft/server/management/PlayerChunkMapEntry;", "func_187301_b");
        putPulsarMethod(result, "net/minecraft/server/management/PlayerChunkMapEntry",
                "sendPacket", "(Lnet/minecraft/network/Packet;)V", "func_187267_a");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos$MutableBlockPos", "setPos",
                "(III)Lnet/minecraft/util/math/BlockPos$MutableBlockPos;", "func_181079_c");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "down",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177977_b");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "east",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177974_f");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getX", "()I",
                "func_177958_n");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getY", "()I",
                "func_177956_o");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "getZ", "()I",
                "func_177952_p");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "north",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177978_c");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "south",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177968_d");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "up",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177984_a");
        putPulsarMethod(result, "net/minecraft/util/math/BlockPos", "west",
                "()Lnet/minecraft/util/math/BlockPos;", "func_177976_e");
        putPulsarMethod(result, "net/minecraft/util/math/MathHelper", "floor", "(D)I",
                "func_76128_c");
        putPulsarMethod(result, "net/minecraft/world/World", "getCombinedLight",
                "(Lnet/minecraft/util/math/BlockPos;I)I", "func_175626_b");
        putPulsarMethod(result, "net/minecraft/world/World", "getMinecraftServer",
                "()Lnet/minecraft/server/MinecraftServer;", "func_73046_m");
        putPulsarMethod(result, "net/minecraft/world/World", "markBlockRangeForRenderUpdate",
                "(IIIIII)V", "func_147458_c");
        putPulsarMethod(result, "net/minecraft/world/WorldProvider", "hasSkyLight", "()Z",
                "func_191066_m");
        putPulsarMethod(result, "net/minecraft/world/WorldServer", "getPlayerChunkMap",
                "()Lnet/minecraft/server/management/PlayerChunkMap;", "func_184164_w");
        putPulsarMethod(result, "net/minecraft/world/biome/Biome", "getBiomeForId",
                "(I)Lnet/minecraft/world/biome/Biome;", "func_185357_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getBlockStorageArray",
                "()[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", "func_76587_i");
        putPulsarMethod(result, "net/minecraft/world/chunk/Chunk", "getWorld",
                "()Lnet/minecraft/world/World;", "func_177412_p");
        putPulsarMethod(result, "net/minecraft/world/chunk/NibbleArray", "get", "(III)I",
                "func_76582_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/NibbleArray", "getData", "()[B",
                "func_177481_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/NibbleArray", "set", "(IIII)V",
                "func_76581_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage", "get",
                "(III)Lnet/minecraft/block/state/IBlockState;", "func_177485_a");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
                "getBlockLight", "()Lnet/minecraft/world/chunk/NibbleArray;", "func_76661_k");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
                "getSkyLight", "()Lnet/minecraft/world/chunk/NibbleArray;", "func_76671_l");
        putPulsarMethod(result, "net/minecraft/world/chunk/storage/ExtendedBlockStorage", "isEmpty",
                "()Z", "func_76663_a");
        putPulsarMethod(result, "net/minecraftforge/client/ClientCommandHandler",
                "registerCommand",
                "(Lnet/minecraft/command/ICommand;)Lnet/minecraft/command/ICommand;",
                "func_71560_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsar", "execute",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;"
                        + "[Ljava/lang/String;)V", "func_184881_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsar", "getName",
                "()Ljava/lang/String;", "func_71517_b");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsar",
                "getRequiredPermissionLevel", "()I", "func_82362_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsar",
                "getTabCompletions",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;"
                        + "[Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;",
                "func_184883_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsar", "getUsage",
                "(Lnet/minecraft/command/ICommandSender;)Ljava/lang/String;", "func_71518_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsarClient",
                "checkPermission",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;)Z",
                "func_184882_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsarClient", "execute",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;"
                        + "[Ljava/lang/String;)V", "func_184881_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsarClient", "getName",
                "()Ljava/lang/String;", "func_71517_b");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsarClient",
                "getRequiredPermissionLevel", "()I", "func_82362_a");
        putPulsarMethod(result, "com/sumirelabs/pulsar/command/CommandPulsarClient", "getUsage",
                "(Lnet/minecraft/command/ICommandSender;)Ljava/lang/String;", "func_71518_a");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess", "getBiome",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;",
                "func_180494_b");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess",
                "getBlockState",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                "func_180495_p");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess",
                "getCombinedLight", "(Lnet/minecraft/util/math/BlockPos;I)I", "func_175626_b");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess",
                "getStrongPower",
                "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I",
                "func_175627_a");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess",
                "getTileEntity",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",
                "func_175625_s");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess",
                "getWorldType", "()Lnet/minecraft/world/WorldType;", "func_175624_G");
        putPulsarMethod(result,
                "com/sumirelabs/pulsar/light/engine/FaceOcclusion$FakeBlockAccess", "isAirBlock",
                "(Lnet/minecraft/util/math/BlockPos;)Z", "func_175623_d");
        return result;
    }

    private static Map<String, String> pulsarDevClassFieldNames() {
        Map<String, String> result = new HashMap<>();
        putPulsarField(result, "net/minecraft/client/Minecraft", "gameSettings",
                "Lnet/minecraft/client/settings/GameSettings;", "field_71474_y");
        putPulsarField(result, "net/minecraft/client/Minecraft", "player",
                "Lnet/minecraft/client/entity/EntityPlayerSP;", "field_71439_g");
        putPulsarField(result, "net/minecraft/client/Minecraft", "world",
                "Lnet/minecraft/client/multiplayer/WorldClient;", "field_71441_e");
        putPulsarField(result, "net/minecraft/client/renderer/vertex/DefaultVertexFormats",
                "NORMAL_3B", "Lnet/minecraft/client/renderer/vertex/VertexFormatElement;",
                "field_181717_q");
        putPulsarField(result, "net/minecraft/client/renderer/vertex/DefaultVertexFormats",
                "PADDING_1B", "Lnet/minecraft/client/renderer/vertex/VertexFormatElement;",
                "field_181718_r");
        putPulsarField(result, "net/minecraft/client/renderer/vertex/DefaultVertexFormats",
                "POSITION_3F", "Lnet/minecraft/client/renderer/vertex/VertexFormatElement;",
                "field_181713_m");
        putPulsarField(result, "net/minecraft/client/renderer/vertex/DefaultVertexFormats",
                "TEX_2F", "Lnet/minecraft/client/renderer/vertex/VertexFormatElement;",
                "field_181715_o");
        putPulsarField(result, "net/minecraft/client/renderer/vertex/DefaultVertexFormats",
                "TEX_2S", "Lnet/minecraft/client/renderer/vertex/VertexFormatElement;",
                "field_181716_p");
        putPulsarField(result, "net/minecraft/client/settings/GameSettings", "ambientOcclusion",
                "I", "field_74348_k");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting$EnumArt", "sizeX",
                "I", "field_75703_B");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting$EnumArt", "sizeY",
                "I", "field_75704_C");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "art",
                "Lnet/minecraft/entity/item/EntityPainting$EnumArt;", "field_70522_e");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "facingDirection",
                "Lnet/minecraft/util/EnumFacing;", "field_174860_b");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "posX", "D",
                "field_70165_t");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "posY", "D",
                "field_70163_u");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "posZ", "D",
                "field_70161_v");
        putPulsarField(result, "net/minecraft/entity/item/EntityPainting", "world",
                "Lnet/minecraft/world/World;", "field_70170_p");
        putPulsarField(result, "net/minecraft/entity/player/EntityPlayer", "dimension", "I",
                "field_71093_bK");
        putPulsarField(result, "net/minecraft/entity/player/EntityPlayerMP", "posX", "D",
                "field_70165_t");
        putPulsarField(result, "net/minecraft/entity/player/EntityPlayerMP", "posZ", "D",
                "field_70161_v");
        putPulsarField(result, "net/minecraft/init/Blocks", "AIR",
                "Lnet/minecraft/block/Block;", "field_150350_a");
        putPulsarField(result, "net/minecraft/util/math/BlockPos", "ORIGIN",
                "Lnet/minecraft/util/math/BlockPos;", "field_177992_a");
        putPulsarField(result, "net/minecraft/world/World", "isRemote", "Z", "field_72995_K");
        putPulsarField(result, "net/minecraft/world/World", "provider",
                "Lnet/minecraft/world/WorldProvider;", "field_73011_w");
        putPulsarField(result, "net/minecraft/world/WorldType", "DEFAULT",
                "Lnet/minecraft/world/WorldType;", "field_77137_b");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "x", "I", "field_76635_g");
        putPulsarField(result, "net/minecraft/world/chunk/Chunk", "z", "I", "field_76647_h");
        return result;
    }

    private static void putPulsarMethod(Map<String, String> result, String owner, String name,
            String desc, String mappedName) {
        result.put(pulsarMemberKey(owner, name, desc), mappedName);
        String nameOnlyKey = pulsarMemberKey(owner, name, "");
        String previous = result.get(nameOnlyKey);
        if (previous == null || previous.equals(mappedName)) {
            result.put(nameOnlyKey, mappedName);
        }
    }

    private static void putPulsarField(Map<String, String> result, String owner, String name,
            String desc, String mappedName) {
        result.put(pulsarMemberKey(owner, name, desc), mappedName);
    }

    private static String pulsarMemberKey(String owner, String name, String desc) {
        return owner + '#' + name + desc;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (ALFHEIM_WORLD_MIXIN_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAlfheimWorldMixin(bytes);
                        }
                    });
        }
        if (ALFHEIM_CHUNK_MIXIN_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAlfheimChunkMixin(bytes);
                        }
                    });
        }
        if (ALFHEIM_CHUNK_CACHE_MIXIN_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAlfheimChunkCacheMixin(bytes);
                        }
                    });
        }
        if (NOTHIRIUM_WORLD_UTIL_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNothiriumWorldUtil(bytes);
                        }
                    });
        }
        if (NOTHIRIUM_RENDER_CHUNK_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNothiriumRenderChunk(bytes);
                        }
                    });
        }
        if (NOTHIRIUM_RENDER_MANAGER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNothiriumRenderManager(bytes);
                        }
                    });
        }
        if (NOTHIRIUM_COMPILE_TASK_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNothiriumCompileTask(bytes);
                        }
                    });
        }
        if (NOTHIRIUM_VERTICAL_DIRECTION_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNothiriumVerticalDirection(transformedName, bytes);
                        }
                    });
        }
        if (DISTANT_HORIZONS_CHUNK_WRAPPER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformDistantHorizonsChunkWrapper(bytes);
                        }
                    });
        }
        if (DISTANT_HORIZONS_LEVEL_WRAPPER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformDistantHorizonsLevelWrapper(transformedName, bytes);
                        }
                    });
        }
        if (NETHER_API_CHUNK_GENERATOR_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformNetherApiChunkGenerator(bytes);
                        }
                    });
        }
        if (OPTIFINE_SHADER_VERTEX_BUILDER_CLASS.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformOptiFineShaderVertexBuilder(bytes);
                        }
                    });
        }
        if (HWYLA_BLOCK_HUD_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformHwylaBlockHud(bytes);
                        }
                    });
        }
        if (PULSAR_DEV_MIXIN_TARGETS.containsKey(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformPulsarDevMixin(transformedName, bytes);
                        }
                    });
        }
        if (transformedName != null
                && transformedName.startsWith("com.sumirelabs.pulsar.")) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            byte[] transformed = transformPulsarDevClass(transformedName, bytes);
                            return PULSAR_WORLD_UTIL_CLASSES.contains(transformedName)
                                    ? transformPulsarWorldUtil(transformed) : transformed;
                        }
                    });
        }
        if (FFD_WORLD_TYPE_CLASS.contains(transformedName) && CAVE_BIOMES_HEIGHT_CORE) {
            return transformFfdWorldTypeForCaveBiomes(basicClass);
        }
        if (ENTITY_CLASS.contains(transformedName)
                && (CAVE_BIOMES_HEIGHT_CORE || DEPTHS_UPDATE_HEIGHT_CORE)) {
            return transformEntityGameplayOnly(basicClass);
        }
        if (CAVE_BIOMES_HEIGHT_CORE) {
            if (WORLD_PROVIDER_CLASS.contains(transformedName)) {
                return transformWorldProviderCloudOnly(basicClass);
            }
            if (WORLD_SERVER_CLASS.contains(transformedName)) {
                return transformWorldServerGameplayOnly(basicClass);
            }
            if (CAVE_BIOMES_OWNED_HEIGHT_CLASSES.contains(transformedName)) {
                return basicClass;
            }
        }
        if (DEPTHS_UPDATE_HEIGHT_CORE) {
            if (WORLD_PROVIDER_CLASS.contains(transformedName)) {
                return transformWorldProviderCloudOnly(basicClass);
            }
            if (CHUNK_CLASS.contains(transformedName)) {
                return transformChunkLegacyWorldgenOnly(basicClass);
            }
            if (DEPTHS_UPDATE_OWNED_HEIGHT_CLASSES.contains(transformedName)) {
                return basicClass;
            }
        }
        if (WORLD_CLASS.contains(transformedName)) {
            return transformWorld(basicClass);
        }
        if (MINECRAFT_CLIENT_CLASS.contains(transformedName)) {
            return transformMinecraftClient(basicClass);
        }
        if (COMMAND_BASE_CLASS.contains(transformedName)) {
            return transformCommandBase(basicClass);
        }
        if (HEIGHT_BOUNDED_COMMAND_CLASSES.contains(transformedName)) {
            return transformHeightBoundedCommand(basicClass,
                    "net.minecraft.command.CommandFill".equals(transformedName) ? 1 : 2);
        }
        if (WORLD_PROVIDER_CLASS.contains(transformedName)) {
            return transformWorldProvider(basicClass);
        }
        if (WORLD_SERVER_CLASS.contains(transformedName)) {
            return transformWorldServer(basicClass);
        }
        if (WORLD_ENTITY_SPAWNER_CLASS.contains(transformedName)) {
            return transformWorldEntitySpawner(basicClass);
        }
        if (BLOCK_CLASS.contains(transformedName)) {
            return transformBlock(basicClass);
        }
        if (BLOCK_PISTON_BASE_CLASS.contains(transformedName)) {
            return transformPistonBase(basicClass);
        }
        if (BLOCK_FALLING_CLASS.contains(transformedName)) {
            return transformFallingBlockLogic(basicClass);
        }
        if (BLOCK_DRAGON_EGG_CLASS.contains(transformedName)) {
            return transformDragonEgg(basicClass);
        }
        if (BLOCK_PORTAL_CLASS.contains(transformedName)) {
            return transformBlockPortal(basicClass);
        }
        if (BLOCK_PORTAL_SIZE_CLASS.contains(transformedName)) {
            return transformPortalSize(basicClass);
        }
        if (BLOCK_MUSHROOM_CLASS.contains(transformedName)) {
            if (CAVES_NOT_CLIFFS) {
                LOGGER.info("Delegated mushroom support to Caves Not Cliffs");
                return basicClass;
            }
            return transformMushroom(basicClass);
        }
        if (BLOCK_LILY_PAD_CLASS.contains(transformedName)) {
            if (CAVES_NOT_CLIFFS) {
                LOGGER.info("Delegated lily-pad support to Caves Not Cliffs");
                return basicClass;
            }
            return transformLilyPad(basicClass);
        }
        if (BLOCK_GRASS_CLASS.contains(transformedName)) {
            return transformGrass(basicClass);
        }
        if (BLOCK_DYNAMIC_LIQUID_CLASS.contains(transformedName)) {
            return transformDynamicLiquid(basicClass);
        }
        if (BLOCK_STATIC_LIQUID_CLASS.contains(transformedName)) {
            return transformStaticLiquid(basicClass);
        }
        if (BLOCK_CHORUS_FLOWER_CLASS.contains(transformedName)) {
            return transformChorusFlower(basicClass);
        }
        if (ENTITY_SHULKER_CLASS.contains(transformedName)) {
            return transformShulker(basicClass);
        }
        if (PATH_NAVIGATE_GROUND_CLASS.contains(transformedName)) {
            return transformPathNavigateGround(basicClass);
        }
        if (WALK_NODE_PROCESSOR_CLASS.contains(transformedName)) {
            return transformWalkNodeProcessor(basicClass);
        }
        if (ENTITY_LIVING_BASE_CLASS.contains(transformedName)) {
            return transformEntityLivingBase(basicClass);
        }
        if (ENTITY_XP_ORB_CLASS.contains(transformedName)) {
            return transformExperienceOrb(basicClass);
        }
        if (CHUNK_CLASS.contains(transformedName)) {
            return transformChunk(basicClass);
        }
        if (EMPTY_CHUNK_CLASS.contains(transformedName)) {
            return transformEmptyChunk(basicClass);
        }
        if (ENTITY_CLASS.contains(transformedName)) {
            return transformEntity(basicClass);
        }
        if (BOAT_CLASS.contains(transformedName)) {
            return transformBoat(basicClass);
        }
        if (MINECART_CLASS.contains(transformedName)) {
            return transformMinecart(basicClass);
        }
        if (FALLING_BLOCK_CLASS.contains(transformedName)) {
            return transformFallingBlock(basicClass);
        }
        if (CAULDRON_CLASS.contains(transformedName)) {
            return transformCauldron(basicClass);
        }
        if (JUKEBOX_CLASS.contains(transformedName)) {
            return transformJukebox(basicClass);
        }
        if (PLAYER_MP_CLASS.contains(transformedName)) {
            return transformPlayerMP(basicClass);
        }
        if (PLAYER_LIST_CLASS.contains(transformedName)) {
            return transformPlayerList(basicClass);
        }
        if (TELEPORTER_CLASS.contains(transformedName)) {
            return transformTeleporter(basicClass);
        }
        if (CHUNK_CACHE_CLASS.contains(transformedName)) {
            return transformChunkCache(basicClass);
        }
        if (ANVIL_CLASS.contains(transformedName)) {
            return transformAnvilChunkLoader(basicClass);
        }
        if (CHUNK_PACKET_CLASS.contains(transformedName)) {
            return transformChunkPacket(basicClass);
        }
        if (VIEW_FRUSTUM_CLASS.contains(transformedName)) {
            return transformViewFrustum(basicClass);
        }
        if (RENDER_GLOBAL_CLASS.contains(transformedName)) {
            return transformRenderGlobal(basicClass);
        }
        if (RENDER_CHUNK_CLASS.contains(transformedName)) {
            return transformRenderChunk(basicClass);
        }
        if (OPTIFINE_RENDER_CHUNK_UTILS_CLASS.contains(transformedName)) {
            return transformOptiFineRenderChunkUtils(basicClass);
        }
        if (PLAYER_CHUNK_ENTRY_CLASS.contains(transformedName)) {
            return transformPlayerChunkMapEntry(basicClass);
        }
        if (MULTI_BLOCK_PACKET_CLASS.contains(transformedName)) {
            return transformMultiBlockPacket(basicClass);
        }
        if (MULTI_BLOCK_DATA_CLASS.contains(transformedName)) {
            return transformMultiBlockData(basicClass);
        }
        if (STRUCTURE_START_CLASS.contains(transformedName)) {
            return transformStructureStart(basicClass);
        }
        if (ANCIENT_WARFARE_BUILDER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAncientWarfareStructureBuilder(bytes);
                        }
                    });
        }
        if (ANCIENT_WARFARE_WORLD_STRUCTURE_GENERATOR_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAncientWarfareWorldStructureGenerator(bytes);
                        }
                    });
        }
        if (ANCIENT_WARFARE_SMOOTHING_MATRIX_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformAncientWarfareSmoothingMatrix(bytes);
                        }
                    });
        }
        if (ICE_AND_FIRE_WORLDGEN_EVENTS_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformIceAndFireWorldgenEvents(bytes);
                        }
                    });
        }
        if (FORGE_HOOKS_CLIENT_CLASS.contains(transformedName)) {
            return transformForgeHooksClient(basicClass);
        }
        if (OPTIMIZED_WORLD_RENDERER_CLASSES.contains(transformedName)) {
            return transformOptimizedWorldRenderer(basicClass);
        }
        if (OPTIMIZED_SECTION_MANAGER_CLASSES.contains(transformedName)) {
            return transformOptimizedSectionManager(basicClass);
        }
        if (OPTIMIZED_WORLD_SLICE_CLASSES.contains(transformedName)) {
            return transformOptimizedWorldSlice(basicClass);
        }
        if (OPTIMIZED_CLONED_SECTION_CLASSES.contains(transformedName)) {
            return transformOptimizedClonedSection(basicClass);
        }
        if (XAERO_WRITER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroWriter(transformedName, bytes);
                        }
                    });
        }
        if (XAERO_CAVE_START_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroCaveStartCalculator(bytes);
                        }
                    });
        }
        if (XAERO_MAP_PROCESSOR_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroMapProcessor(bytes);
                        }
                    });
        }
        if (XAERO_WORLD_DATA_READER_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroWorldDataReader(bytes);
                        }
                    });
        }
        if (XAERO_MAP_SAVE_LOAD_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroMapSaveLoad(bytes);
                        }
                    });
        }
        if (XAERO_MINIMAP_TILE_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroMinimapTile(bytes);
                        }
                    });
        }
        if (XAERO_MAP_TILE_CHUNK_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroMapTileChunk(bytes);
                        }
                    });
        }
        if (XAERO_MAP_BLOCK_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroMapBlock(bytes);
                        }
                    });
        }
        if (XAERO_CAVE_OPTIONS_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformXaeroCaveOptions(bytes);
                        }
                    });
        }
        if (VOXELMAP_MAP_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformVoxelMap(bytes);
                        }
                    });
        }
        if (VOXELMAP_DATA_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformVoxelMapData(bytes);
                        }
                    });
        }
        if (VOXELMAP_WAYPOINT_GUI_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformVoxelMapWaypointTeleport(bytes,
                                    "func_146284_a",
                                    "(Lnet/minecraft/client/gui/GuiButton;)V");
                        }
                    });
        }
        if (VOXELMAP_PERSISTENT_GUI_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformVoxelMapWaypointTeleport(bytes,
                                    "popupAction",
                                    "(Lcom/mamiyaotaru/voxelmap/gui/overridden/Popup;I)V");
                        }
                    });
        }
        if (VOXELMAP_COMMAND_CLASSES.contains(transformedName)) {
            return transformOptionalCompat(transformedName, basicClass,
                    new OptionalTransformer() {
                        @Override
                        public byte[] transform(byte[] bytes) {
                            return transformVoxelMapWaypointTeleport(bytes,
                                    "teleport", "(Ljava/lang/String;)V");
                        }
                    });
        }
        return basicClass;
    }

    private interface OptionalTransformer {
        byte[] transform(byte[] bytes);
    }

    private static byte[] transformOptionalCompat(String className, byte[] basicClass,
            OptionalTransformer transformer) {
        try {
            return transformer.transform(basicClass);
        } catch (RuntimeException exception) {
            LOGGER.warn("Skipping optional height compatibility for {}", className, exception);
            return basicClass;
        }
    }

    private static byte[] transformFfdWorldTypeForCaveBiomes(byte[] basicClass) {
        ClassNode node = read(basicClass);
        if (!node.interfaces.contains(CAVE_BIOMES_EXTENDED_WORLD_TYPE)) {
            node.interfaces.add(CAVE_BIOMES_EXTENDED_WORLD_TYPE);
        }
        LOGGER.info("Delegating ffd_cac height storage, networking, saving, and rendering to CaveBiomesAPI");
        return write(node);
    }

    private static byte[] transformAlfheimWorldMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/WorldMixin".equals(node.name),
                "Unexpected Alfheim WorldMixin class");

        prependExtendedWorldBranch(findMethod(node, "func_180500_c", "func_180500_c",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z"),
                loadMixinSelfAsWorld(), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/World"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/World",
                                "ffd$checkLightFor",
                                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_175721_c", "func_175721_c",
                "(Lnet/minecraft/util/math/BlockPos;Z)I"), loadMixinSelfAsWorld(), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/World"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLight",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        MethodNode neighborLight = findMethodOptional(node, "func_175705_a", "func_175705_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (neighborLight != null) {
            prependExtendedWorldBranch(neighborLight, loadMixinSelfAsWorld(), list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/World"),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "getWorldLightFromNeighborsFor",
                            "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                            false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        LOGGER.info("Patched Alfheim 1.6 WorldMixin for signed extended-height lighting");
        return write(node);
    }

    private static byte[] transformAlfheimChunkMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/ChunkMixin".equals(node.name),
                "Unexpected Alfheim ChunkMixin class");
        InsnList world = loadAlfheimChunkWorld(node.name);

        prependExtendedWorldBranch(findMethodByName(node, "onGetLightSubtracted"),
                copy(world), list(new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "onLoad"),
                copy(world), list(new InsnNode(Opcodes.RETURN)));
        MethodNode setLightRedirect = findMethodByName(node,
                "setLightForRedirectGenerateSkylightMap");
        setMixinAnnotationInt(setLightRedirect,
                "Lorg/spongepowered/asm/mixin/injection/Redirect;", "require", 0);
        prependExtendedWorldBranch(setLightRedirect, copy(world),
                list(new InsnNode(Opcodes.RETURN)));

        prependExtendedWorldBranch(findMethod(node, "func_76615_h", "func_76615_h", "(III)V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "relightBlock",
                                "(Lnet/minecraft/world/chunk/Chunk;III)V", false),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_177413_a", "func_177413_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_150809_p", "func_150809_p", "()V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/chunk/Chunk",
                                "ffd$checkLight", "()V", false),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethod(node, "func_150803_c", "func_150803_c", "(Z)V"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/chunk/Chunk",
                                "ffd$recheckGaps", "(Z)V", false),
                        new InsnNode(Opcodes.RETURN)));

        prependExtendedWorldBranch(findMethodByName(node, "setBlockStateCreateSectionVanilla"),
                copy(world), list(
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.NEW, STORAGE),
                        new InsnNode(Opcodes.DUP),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESPECIAL, STORAGE, "<init>", "(IZ)V", false),
                        new InsnNode(Opcodes.ARETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "preventGenerateSkylightMap"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "doPropagateSkylight"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                                "field_76639_c", "[Z"),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new IntInsnNode(Opcodes.BIPUSH, 16),
                        new InsnNode(Opcodes.IMUL),
                        new InsnNode(Opcodes.IADD),
                        new InsnNode(Opcodes.ICONST_1),
                        new InsnNode(Opcodes.BASTORE),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new InsnNode(Opcodes.ICONST_1),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, node.name,
                                "field_76650_s", "Z"),
                        new InsnNode(Opcodes.RETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "fakeGetLightFor"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.IRETURN)));
        prependExtendedWorldBranch(findMethodByName(node, "alfheim$getCachedLightFor"),
                copy(world), list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                                "net/minecraft/world/chunk/Chunk"),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                                false),
                        new InsnNode(Opcodes.I2B),
                        new InsnNode(Opcodes.IRETURN)));

        LOGGER.info("Patched Alfheim 1.6 ChunkMixin for FFD's 24-section light storage");
        return write(node);
    }

    private static byte[] transformAlfheimChunkCacheMixin(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("dev/redstudio/alfheim/mixin/ChunkCacheMixin".equals(node.name),
                "Unexpected Alfheim ChunkCacheMixin class");
        MethodNode lightForExt = findMethodOptional(node, "func_175629_a", "func_175629_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (lightForExt != null) {
            InsnList condition = list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/ChunkCache"),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/ChunkCache",
                            "ffd$isExtended", "()Z", false));
            prependConditionBranch(lightForExt, condition, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                            "net/minecraft/world/ChunkCache"),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/ChunkCache",
                            "ffd$getLightForExt",
                            "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I",
                            false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        LOGGER.info("Patched Alfheim 1.6 ChunkCacheMixin for extended-height light access");
        return write(node);
    }

    private static byte[] transformNothiriumWorldUtil(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("meldexun/nothirium/mc/util/WorldUtil".equals(node.name),
                "Unexpected Nothirium WorldUtil class");
        replace(findMethod(node, "getSection", "getSection",
                "(Lnet/minecraft/world/World;III)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;"),
                list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                "getNothiriumSection",
                                "(Lnet/minecraft/world/World;III)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;",
                                false),
                        new InsnNode(Opcodes.ARETURN)));
        LOGGER.info("Patched Nothirium WorldUtil section lookup for signed height");
        return write(node);
    }

    private static byte[] transformNothiriumRenderChunk(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("meldexun/nothirium/mc/renderer/chunk/RenderChunk".equals(node.name),
                "Unexpected Nothirium RenderChunk class");
        LabelNode outsideWorld = new LabelNode();
        replace(findMethod(node, "markDirty", "markDirty", "()V"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name,
                                "getSectionY", "()I", false),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                "isNothiriumSectionInWorld", "(I)Z", false),
                        new JumpInsnNode(Opcodes.IFEQ, outsideWorld),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESPECIAL,
                                "meldexun/nothirium/renderer/chunk/AbstractRenderChunk",
                                "markDirty", "()V", false),
                        new InsnNode(Opcodes.RETURN),
                        outsideWorld,
                        new FrameNode(Opcodes.F_SAME, 0, null, 0, null),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name,
                                "getVisibility", "()Lmeldexun/nothirium/util/VisibilitySet;", false),
                        new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "meldexun/nothirium/util/VisibilitySet",
                                "setAllVisible", "()V", false),
                        new InsnNode(Opcodes.RETURN)));
        LOGGER.info("Patched Nothirium RenderChunk dirty marking for signed height");
        return write(node);
    }

    private static byte[] transformNothiriumRenderManager(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("meldexun/nothirium/mc/renderer/ChunkRenderManager".equals(node.name),
                "Unexpected Nothirium ChunkRenderManager class");
        MethodNode allChanged = findMethod(node, "allChanged", "allChanged", "()V");
        int patchedInit = 0;
        for (AbstractInsnNode instruction = allChanged.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                    || instruction.getOpcode() != Opcodes.INVOKEINTERFACE
                    || !"init".equals(((MethodInsnNode) instruction).name)
                    || !"(III)V".equals(((MethodInsnNode) instruction).desc)) {
                continue;
            }
            AbstractInsnNode zLoad = previousReal(instruction);
            AbstractInsnNode yLoad = previousReal(zLoad);
            AbstractInsnNode xLoad = previousReal(yLoad);
            require(xLoad instanceof VarInsnNode && yLoad instanceof VarInsnNode
                            && zLoad instanceof VarInsnNode
                            && xLoad.getOpcode() == Opcodes.ILOAD
                            && yLoad.getOpcode() == Opcodes.ILOAD
                            && zLoad.getOpcode() == Opcodes.ILOAD,
                    "Unexpected Nothirium render-grid init shape");
            int renderDistanceLocal = ((VarInsnNode) xLoad).var;
            InsnList replacement = list(
                    new VarInsnNode(Opcodes.ILOAD, renderDistanceLocal),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                            "nothiriumVerticalRenderDistance", "(I)I", false));
            allChanged.instructions.insertBefore(yLoad, replacement);
            allChanged.instructions.remove(yLoad);
            patchedInit++;
            break;
        }
        require(patchedInit == 1, "Expected one Nothirium render-grid init, patched " + patchedInit);

        replace(findMethod(node, "totalSections", "totalSections", "()I"),
                list(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "net/minecraft/client/Minecraft", "func_71410_x",
                                "()Lnet/minecraft/client/Minecraft;", false),
                        new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft",
                                "field_71474_y",
                                "Lnet/minecraft/client/settings/GameSettings;"),
                        new FieldInsnNode(Opcodes.GETFIELD,
                                "net/minecraft/client/settings/GameSettings",
                                "field_151451_c", "I"),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                "nothiriumTotalSections", "(I)I", false),
                        new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched Nothirium render-grid height and section count");
        return write(node);
    }

    private static byte[] transformNothiriumCompileTask(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("meldexun/nothirium/mc/renderer/chunk/RenderChunkTaskCompile".equals(node.name),
                "Unexpected Nothirium RenderChunkTaskCompile class");
        MethodNode compile = findMethod(node, "compileSection", "compileSection",
                "()Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;");
        org.objectweb.asm.tree.FieldInsnNode queueField = null;
        for (AbstractInsnNode instruction = compile.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                    && instruction.getOpcode() == Opcodes.GETSTATIC
                    && "Ljava/util/concurrent/BlockingQueue;".equals(
                            ((org.objectweb.asm.tree.FieldInsnNode) instruction).desc)) {
                queueField = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                break;
            }
        }
        require(queueField != null, "Missing Nothirium compile buffer queue");
        LabelNode permitAcquired = new LabelNode();
        compile.instructions.insertBefore(queueField, list(
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                        "acquireNothiriumBufferPermit", "()Z", false),
                new JumpInsnNode(Opcodes.IFNE, permitAcquired),
                new FieldInsnNode(Opcodes.GETSTATIC,
                        "meldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult",
                        "CANCELLED",
                        "Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;"),
                new InsnNode(Opcodes.ARETURN),
                permitAcquired,
                new FrameNode(Opcodes.F_SAME, 0, null, 0, null)));

        MethodNode freeBuffer = findMethod(node, "freeBuffer", "freeBuffer",
                "(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)V");
        int patchedRelease = 0;
        for (AbstractInsnNode instruction = freeBuffer.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                    || !"java/util/concurrent/BlockingQueue".equals(
                            ((MethodInsnNode) instruction).owner)
                    || !"add".equals(((MethodInsnNode) instruction).name)
                    || !"(Ljava/lang/Object;)Z".equals(((MethodInsnNode) instruction).desc)) {
                continue;
            }
            MethodInsnNode replacement = (MethodInsnNode) instruction;
            replacement.setOpcode(Opcodes.INVOKESTATIC);
            replacement.owner = CLIENT_HOOKS;
            replacement.name = "releaseNothiriumBuffer";
            replacement.desc = "(Ljava/util/concurrent/BlockingQueue;"
                    + "Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)V";
            replacement.itf = false;
            AbstractInsnNode pop = nextReal(replacement);
            require(pop != null && pop.getOpcode() == Opcodes.POP,
                    "Missing Nothirium buffer queue result discard");
            freeBuffer.instructions.remove(pop);
            patchedRelease++;
        }
        require(patchedRelease == 1,
                "Expected one Nothirium compile buffer release, patched " + patchedRelease);
        LOGGER.info("Patched Nothirium compile buffers with bounded direct-memory permits");
        return write(node);
    }

    private static byte[] transformNothiriumVerticalDirection(String className,
            byte[] basicClass) {
        ClassNode node = read(basicClass);
        require(className.replace('.', '/').equals(node.name),
                "Unexpected Nothirium Direction class");
        MethodNode method = findMethod(node, "isFaceCulled", "isFaceCulled",
                "(Lmeldexun/nothirium/api/renderer/chunk/IRenderChunk;DDD)Z");
        if (className.endsWith("$1")) {
            int patched = 0;
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof IntInsnNode
                        && instruction.getOpcode() == Opcodes.BIPUSH
                        && ((IntInsnNode) instruction).operand == 16) {
                    method.instructions.insertBefore(instruction,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                    "currentWorldMaxSectionYExclusive", "()I", false));
                    method.instructions.remove(instruction);
                    patched++;
                    break;
                }
            }
            require(patched == 1,
                    "Expected one Nothirium upper section boundary, patched " + patched);
        } else {
            int patched = 0;
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof JumpInsnNode
                        && instruction.getOpcode() == Opcodes.IFGE) {
                    method.instructions.insertBefore(instruction,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                    "currentWorldMinSectionY", "()I", false));
                    ((JumpInsnNode) instruction).setOpcode(Opcodes.IF_ICMPGE);
                    patched++;
                    break;
                }
            }
            require(patched == 1,
                    "Expected one Nothirium lower section boundary, patched " + patched);
        }
        LOGGER.info("Patched Nothirium vertical face culling for {}", node.name);
        return write(node);
    }

    private static byte[] transformDistantHorizonsChunkWrapper(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("com/seibel/distanthorizons/common/wrappers/chunk/ChunkWrapper".equals(node.name),
                "Unexpected Distant Horizons ChunkWrapper class");
        String chunkDesc = "Lnet/minecraft/world/chunk/Chunk;";
        replace(findMethod(node, "getHeight", "getHeight", "(" + chunkDesc + ")I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYInclusive",
                                "(" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getInclusiveMinBuildHeight", "getInclusiveMinBuildHeight",
                        "(" + chunkDesc + ")I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                "(" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getExclusiveMaxBuildHeight", "getExclusiveMaxBuildHeight",
                        "(" + chunkDesc + ")I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYExclusive",
                                "(" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getMinNonEmptyHeight", "getMinNonEmptyHeight", "()I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new FieldInsnNode(Opcodes.GETFIELD, node.name, "chunk", chunkDesc),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "distantHorizonsMinNonEmptyHeight",
                                "(" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getMaxNonEmptyHeight", "getMaxNonEmptyHeight", "()I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new FieldInsnNode(Opcodes.GETFIELD, node.name, "chunk", chunkDesc),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "distantHorizonsMaxNonEmptyHeight",
                                "(" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getChunkSectionMinHeight", "getChunkSectionMinHeight", "(I)I"),
                list(new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new FieldInsnNode(Opcodes.GETFIELD, node.name, "chunk", chunkDesc),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "sectionBaseYForChunkIndex", "(I" + chunkDesc + ")I", false),
                        new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched Distant Horizons chunk height and storage ordering");
        return write(node);
    }

    private static byte[] transformDistantHorizonsLevelWrapper(String className,
            byte[] basicClass) {
        ClassNode node = read(basicClass);
        require(className.replace('.', '/').equals(node.name),
                "Unexpected Distant Horizons level wrapper class");
        FieldNode levelField = null;
        for (FieldNode field : node.fields) {
            if ("level".equals(field.name) && field.desc.startsWith("Lnet/minecraft/")) {
                levelField = field;
                break;
            }
        }
        require(levelField != null, "Missing Distant Horizons wrapped level field");
        replace(findMethod(node, "getMinHeight", "getMinHeight", "()I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new FieldInsnNode(Opcodes.GETFIELD, node.name,
                                levelField.name, levelField.desc),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                "(Lnet/minecraft/world/World;)I", false),
                        new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getMaxHeight", "getMaxHeight", "()I"),
                list(new VarInsnNode(Opcodes.ALOAD, 0),
                        new FieldInsnNode(Opcodes.GETFIELD, node.name,
                                levelField.name, levelField.desc),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYExclusive",
                                "(Lnet/minecraft/world/World;)I", false),
                        new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched Distant Horizons level height for {}", node.name);
        return write(node);
    }

    private static byte[] transformNetherApiChunkGenerator(byte[] basicClass) {
        ClassNode node = read(basicClass);
        int patched = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                    instruction != null; instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!("getActualHeight".equals(call.name)
                        || "func_72940_L".equals(call.name))
                        || !"()I".equals(call.desc)) {
                    continue;
                }
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        HOOKS, "netherApiGenerationHeight",
                        "(Lnet/minecraft/world/World;)I", false));
                patched++;
            }
        }
        require(patched == 3,
                "Expected three Nether API generation-height calls, patched " + patched);
        LOGGER.info("Patched Nether API to retain vanilla Nether generation height in ffd_cac");
        return write(node);
    }

    private static byte[] transformForgeHooksClient(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getSkyBlendColour", "getSkyBlendColour",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I");
        org.objectweb.asm.tree.LabelNode smoothBlend = new org.objectweb.asm.tree.LabelNode();
        method.instructions.insertBefore(method.instructions.getFirst(), list(
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETSTATIC, CONFIG,
                        "smoothBiomeSkyColors", "Z"),
                new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IFNE, smoothBlend),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                        "getDirectBiomeSkyColour",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", false),
                new InsnNode(Opcodes.IRETURN),
                smoothBlend,
                new FrameNode(Opcodes.F_SAME, 0, null, 0, null)));
        LOGGER.info("Patched optional biome sky-color blending");
        return write(node);
    }

    private static byte[] transformBoat(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "checkInWater", "func_184446_u", "()Z");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode
                    && Double.valueOf(Double.MIN_VALUE).equals(((LdcInsnNode) instruction).cst)) {
                ((LdcInsnNode) instruction).cst = Double.NEGATIVE_INFINITY;
                patched++;
            }
        }
        require(patched == 1, "Expected one boat water-level sentinel, patched " + patched);
        LOGGER.info("Patched boat water-level detection for negative heights");
        return write(node);
    }

    private static byte[] transformOptimizedWorldRenderer(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode minimum = findMethod(node, "getMinimumBuildHeight",
                "getMinimumBuildHeight", "()I");
        MethodNode maximum = findMethod(node, "getMaximumBuildHeight",
                "getMaximumBuildHeight", "()I");
        org.objectweb.asm.tree.FieldInsnNode worldField = null;
        for (AbstractInsnNode instruction = maximum.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                    && instruction.getOpcode() == Opcodes.GETFIELD) {
                worldField = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                break;
            }
        }
        require(worldField != null, "Missing optimized renderer world field");
        replace(minimum, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, worldField.owner,
                        worldField.name, worldField.desc),
                new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                        "net/minecraft/world/World"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                        "(Lnet/minecraft/world/World;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched optimized renderer minimum build height for {}", node.name);
        return write(node);
    }

    private static byte[] transformOptimizedSectionManager(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode create = null;
        for (MethodNode method : node.methods) {
            if ("create".equals(method.name) && (method.access & Opcodes.ACC_STATIC) != 0
                    && method.desc.endsWith(")L" + node.name + ";")) {
                create = method;
                break;
            }
        }
        require(create != null, "Missing optimized section-manager factory");
        int rangesPatched = 0;
        for (AbstractInsnNode instruction = create.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                    || instruction.getOpcode() != Opcodes.INVOKESPECIAL) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!node.name.equals(call.owner) || !"<init>".equals(call.name)
                    || !call.desc.endsWith(";II)V")) {
                continue;
            }
            AbstractInsnNode maximum = previousReal(instruction);
            AbstractInsnNode minimum = previousReal(maximum);
            require(minimum != null && minimum.getOpcode() == Opcodes.ICONST_0,
                    "Missing optimized renderer minimum-section constant");
            require(maximum instanceof IntInsnNode && maximum.getOpcode() == Opcodes.BIPUSH
                            && ((IntInsnNode) maximum).operand == 16,
                    "Missing optimized renderer maximum-section constant");
            create.instructions.insertBefore(minimum, list(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minSectionY",
                            "(Lnet/minecraft/world/World;)I", false)));
            create.instructions.remove(minimum);
            create.instructions.insertBefore(maximum, list(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxSectionYExclusive",
                            "(Lnet/minecraft/world/World;)I", false)));
            create.instructions.remove(maximum);
            rangesPatched++;
        }
        require(rangesPatched == 1,
                "Expected one optimized renderer section range, patched " + rangesPatched);

        MethodNode empty = findMethod(node, "isSectionVisuallyEmpty",
                "isSectionVisuallyEmpty", "(III)Z");
        org.objectweb.asm.tree.FieldInsnNode worldField = null;
        for (AbstractInsnNode instruction = empty.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                    && instruction.getOpcode() == Opcodes.GETFIELD
                    && "Lnet/minecraft/client/multiplayer/WorldClient;".equals(
                            ((org.objectweb.asm.tree.FieldInsnNode) instruction).desc)) {
                worldField = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                break;
            }
        }
        require(worldField != null, "Missing optimized section-manager world field");
        replace(empty, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, worldField.owner,
                        worldField.name, worldField.desc),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isSectionVisuallyEmpty",
                        "(Lnet/minecraft/world/World;III)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched optimized render section range and storage lookup for {}", node.name);
        return write(node);
    }

    private static byte[] transformOptimizedWorldSlice(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode prepare = null;
        for (MethodNode method : node.methods) {
            if ("prepare".equals(method.name) && (method.access & Opcodes.ACC_STATIC) != 0) {
                prepare = method;
                break;
            }
        }
        require(prepare != null, "Missing optimized world-slice prepare method");
        int patched = 0;
        for (AbstractInsnNode instruction = prepare.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.AALOAD) {
                continue;
            }
            AbstractInsnNode index = previousReal(instruction);
            if (!(index instanceof MethodInsnNode) || !"y".equals(((MethodInsnNode) index).name)
                    || !"()I".equals(((MethodInsnNode) index).desc)) {
                continue;
            }
            prepare.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "storageIndexForSectionY",
                            "(ILnet/minecraft/world/World;)I", false)));
            patched++;
        }
        require(patched == 1, "Expected one optimized world-slice storage lookup, patched "
                + patched);
        LOGGER.info("Patched optimized world-slice section mapping for {}", node.name);
        return write(node);
    }

    private static byte[] transformOptimizedClonedSection(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getChunkSection", "getChunkSection",
                "(Lnet/minecraft/world/chunk/Chunk;I)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getStorageForSectionY",
                        "(Lnet/minecraft/world/chunk/Chunk;I)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;",
                        false),
                new InsnNode(Opcodes.ARETURN)));
        LOGGER.info("Patched optimized cloned-section storage mapping for {}", node.name);
        return write(node);
    }

    private static byte[] transformPulsarWorldUtil(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getHeightContext", "getHeightContext",
                "(Lnet/minecraft/world/World;)Lcom/sumirelabs/pulsar/util/WorldHeightContext;");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, PULSAR_HOOKS, "getHeightContext",
                        "(Lnet/minecraft/world/World;)Ljava/lang/Object;", false),
                new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                        "com/sumirelabs/pulsar/util/WorldHeightContext"),
                new InsnNode(Opcodes.ARETURN)));
        LOGGER.info("Patched Pulsar height-context integration");
        return write(node);
    }

    private static byte[] transformPulsarDevClass(String className, byte[] basicClass) {
        if (classMajorVersion(basicClass) != 69) {
            return basicClass;
        }
        ClassNode node = read(basicClass);
        require(node.name.equals(className.replace('.', '/')),
                "Unexpected Pulsar class structure for " + className);

        int definitions = 0;
        int methodCalls = 0;
        int fieldAccesses = 0;
        for (MethodNode method : node.methods) {
            String mappedDefinition = mapPulsarDevClassMethod(node.name, method.name, method.desc);
            if (!mappedDefinition.equals(method.name)) {
                method.name = mappedDefinition;
                definitions++;
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    String mapped = mapPulsarDevClassMethod(call.owner, call.name, call.desc);
                    if (!mapped.equals(call.name)) {
                        call.name = mapped;
                        methodCalls++;
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode access = (FieldInsnNode) instruction;
                    String mapped = mapPulsarDevClassField(access.owner, access.name, access.desc);
                    if (!mapped.equals(access.name)) {
                        access.name = mapped;
                        fieldAccesses++;
                    }
                }
            }
        }

        int patched = definitions + methodCalls + fieldAccesses;
        if (patched == 0) {
            return basicClass;
        }
        LOGGER.info("Remapped Pulsar 0.2.4-dev class {}: {} definitions, {} method calls, "
                        + "{} field accesses",
                className, definitions, methodCalls, fieldAccesses);
        return write(node);
    }

    private static byte[] transformPulsarDevMixin(String className, byte[] basicClass) {
        if (classMajorVersion(basicClass) != 69) {
            return basicClass;
        }
        ClassNode node = read(basicClass);
        String targetOwner = PULSAR_DEV_MIXIN_TARGETS.get(className);
        require(targetOwner != null && node.name.equals(className.replace('.', '/')),
                "Unexpected Pulsar mixin class structure for " + className);

        int definitions = 0;
        int annotations = 0;
        int instructions = 0;
        for (FieldNode field : node.fields) {
            if (!hasPulsarMixinAnnotation(field.visibleAnnotations,
                    "Lorg/spongepowered/asm/mixin/Shadow;")) {
                continue;
            }
            String mapped = mapPulsarField(targetOwner, field.name, field.desc);
            if (!mapped.equals(field.name)) {
                field.name = mapped;
                definitions++;
            }
        }
        for (MethodNode method : node.methods) {
            if (hasPulsarMixinAnnotation(method.visibleAnnotations,
                    "Lorg/spongepowered/asm/mixin/Shadow;")
                    || hasPulsarMixinAnnotation(method.visibleAnnotations,
                            "Lorg/spongepowered/asm/mixin/Overwrite;")) {
                String mapped = mapPulsarMethod(targetOwner, method.name, method.desc);
                if (!mapped.equals(method.name)) {
                    method.name = mapped;
                    definitions++;
                }
            }
            annotations += remapPulsarMixinAnnotations(method.visibleAnnotations, targetOwner);
            annotations += remapPulsarMixinAnnotations(method.invisibleAnnotations, targetOwner);
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    String owner = node.name.equals(call.owner) ? targetOwner : call.owner;
                    String mapped = mapPulsarMethod(owner, call.name, call.desc);
                    if (!mapped.equals(call.name)) {
                        call.name = mapped;
                        instructions++;
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode access = (FieldInsnNode) instruction;
                    String owner = node.name.equals(access.owner) ? targetOwner : access.owner;
                    String mapped = mapPulsarField(owner, access.name, access.desc);
                    if (!mapped.equals(access.name)) {
                        access.name = mapped;
                        instructions++;
                    }
                }
            }
        }

        int patched = definitions + annotations + instructions;
        if (patched == 0) {
            return basicClass;
        }
        LOGGER.info("Remapped Pulsar 0.2.4-dev mixin {}: {} definitions, {} selectors, "
                        + "{} member accesses",
                className, definitions, annotations, instructions);
        return write(node);
    }

    private static int remapPulsarMixinAnnotations(List<AnnotationNode> annotations,
            String targetOwner) {
        if (annotations == null) {
            return 0;
        }
        int patched = 0;
        for (AnnotationNode annotation : annotations) {
            patched += remapPulsarMixinAnnotation(annotation, targetOwner);
        }
        return patched;
    }

    private static int remapPulsarMixinAnnotation(AnnotationNode annotation, String targetOwner) {
        if (annotation.values == null || !pulsarAnnotationRemaps(annotation)) {
            return 0;
        }
        int patched = 0;
        for (int index = 0; index < annotation.values.size(); index += 2) {
            String key = (String) annotation.values.get(index);
            Object value = annotation.values.get(index + 1);
            if ("method".equals(key)) {
                patched += remapPulsarMixinSelectorValue(annotation.values, index + 1, value,
                        targetOwner);
            } else if ("target".equals(key)) {
                patched += remapPulsarMixinSelectorValue(annotation.values, index + 1, value,
                        targetOwner);
            } else if (value instanceof AnnotationNode) {
                patched += remapPulsarMixinAnnotation((AnnotationNode) value, targetOwner);
            } else if (value instanceof List<?>) {
                for (Object entry : (List<?>) value) {
                    if (entry instanceof AnnotationNode) {
                        patched += remapPulsarMixinAnnotation((AnnotationNode) entry, targetOwner);
                    }
                }
            }
        }
        return patched;
    }

    @SuppressWarnings("unchecked")
    private static int remapPulsarMixinSelectorValue(List<Object> annotationValues,
            int valueIndex, Object value, String targetOwner) {
        if (value instanceof String) {
            String selector = (String) value;
            String mapped = remapPulsarMixinSelector(selector, targetOwner);
            if (!mapped.equals(selector)) {
                annotationValues.set(valueIndex, mapped);
                return 1;
            }
            return 0;
        }
        if (!(value instanceof List<?>)) {
            return 0;
        }
        int patched = 0;
        List<Object> entries = (List<Object>) value;
        for (int index = 0; index < entries.size(); index++) {
            Object entry = entries.get(index);
            if (!(entry instanceof String)) {
                continue;
            }
            String selector = (String) entry;
            String mapped = remapPulsarMixinSelector(selector, targetOwner);
            if (!mapped.equals(selector)) {
                entries.set(index, mapped);
                patched++;
            }
        }
        return patched;
    }

    private static String remapPulsarMixinSelector(String selector, String targetOwner) {
        if (selector == null || selector.startsWith("<")) {
            return selector;
        }
        if (selector.startsWith("L")) {
            int ownerEnd = selector.indexOf(';');
            int methodStart = selector.indexOf('(', ownerEnd + 1);
            if (ownerEnd < 2 || methodStart < 0) {
                return selector;
            }
            String owner = selector.substring(1, ownerEnd);
            String name = selector.substring(ownerEnd + 1, methodStart);
            String mapped = mapPulsarMethod(owner, name, selector.substring(methodStart));
            return mapped.equals(name) ? selector
                    : selector.substring(0, ownerEnd + 1) + mapped + selector.substring(methodStart);
        }
        int methodStart = selector.indexOf('(');
        String name = methodStart < 0 ? selector : selector.substring(0, methodStart);
        String desc = methodStart < 0 ? "" : selector.substring(methodStart);
        String mapped = mapPulsarMethod(targetOwner, name, desc);
        return mapped.equals(name) ? selector : mapped + desc;
    }

    private static boolean pulsarAnnotationRemaps(AnnotationNode annotation) {
        if (annotation.values == null) {
            return true;
        }
        for (int index = 0; index < annotation.values.size(); index += 2) {
            if ("remap".equals(annotation.values.get(index))) {
                return !Boolean.FALSE.equals(annotation.values.get(index + 1));
            }
        }
        return true;
    }

    private static boolean hasPulsarMixinAnnotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return false;
        }
        for (AnnotationNode annotation : annotations) {
            if (desc.equals(annotation.desc)) {
                return true;
            }
        }
        return false;
    }

    private static String mapPulsarMethod(String owner, String name, String desc) {
        String mapped = PULSAR_DEV_METHOD_NAMES.get(pulsarMemberKey(owner, name, desc));
        if (mapped == null && desc.length() == 0) {
            mapped = PULSAR_DEV_METHOD_NAMES.get(pulsarMemberKey(owner, name, ""));
        }
        return mapped == null ? name : mapped;
    }

    private static String mapPulsarField(String owner, String name, String desc) {
        String mapped = PULSAR_DEV_FIELD_NAMES.get(pulsarMemberKey(owner, name, desc));
        return mapped == null ? name : mapped;
    }

    private static String mapPulsarDevClassMethod(String owner, String name, String desc) {
        String mapped = PULSAR_DEV_CLASS_METHOD_NAMES.get(pulsarMemberKey(owner, name, desc));
        return mapped == null ? name : mapped;
    }

    private static String mapPulsarDevClassField(String owner, String name, String desc) {
        String mapped = PULSAR_DEV_CLASS_FIELD_NAMES.get(pulsarMemberKey(owner, name, desc));
        return mapped == null ? name : mapped;
    }

    private static int classMajorVersion(byte[] bytes) {
        return (bytes[6] & 0xFF) << 8 | bytes[7] & 0xFF;
    }

    private static byte[] transformXaeroWriter(String className, byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getSectionBasedHeight", "getSectionBasedHeight",
                "(Lnet/minecraft/world/chunk/Chunk;I)I");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getSectionBasedHeight",
                        "(Lnet/minecraft/world/chunk/Chunk;I)I", false),
                new InsnNode(Opcodes.IRETURN)));
        if ("xaero.common.minimap.write.MinimapWriter".equals(className)) {
            MethodNode getCaving = findMethod(node, "getCaving", "getCaving",
                    "(DDDLnet/minecraft/world/World;)I");
            patchXaeroCaveDetector(getCaving, 7);

            MethodNode loadBlockColor = findMethodByName(node, "loadBlockColor");
            patchXaeroLowYFloor(loadBlockColor, 2, false);

            MethodNode findBlock = findMethodByName(node, "findBlock");
            patchXaeroMaxYConstants(findBlock, 1, 1);
        } else {
            MethodNode writeChunk = findMethodByName(node, "writeChunk");
            patchXaeroLowYFloor(writeChunk, 1, true);
            int decodedHeights = patchXaeroWorldMapEffectiveHeights(writeChunk, 13);
            require(decodedHeights > 0, "Missing Xaero world-map effective-height reads");

            MethodNode loadPixel = findMethodByName(node, "loadPixel");
            patchXaeroMaxYConstants(loadPixel, 1, 1);
        }
        LOGGER.info("Patched Xaero extended cave-height handling for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroCaveStartCalculator(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getCaving", "getCaving",
                "(DDDLnet/minecraft/world/World;)I");
        patchXaeroCaveDetector(method, 7);
        LOGGER.info("Patched Xaero world-map cave detector for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroMapProcessor(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "updateCaveStart", "updateCaveStart", "()V");
        String worldField = findReferencedField(method, node.name,
                "Lnet/minecraft/client/multiplayer/WorldClient;");
        int patched = 0;
        int newCaveStartLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode clamp = (MethodInsnNode) instruction;
            if (clamp.getOpcode() != Opcodes.INVOKESTATIC || !"(III)I".equals(clamp.desc)
                    || !("func_76125_a".equals(clamp.name) || "clamp".equals(clamp.name))) {
                continue;
            }
            AbstractInsnNode subtract = previousReal(clamp);
            AbstractInsnNode one = previousReal(subtract);
            AbstractInsnNode getHeight = previousReal(one);
            AbstractInsnNode worldGet = previousReal(getHeight);
            AbstractInsnNode loadThis = previousReal(worldGet);
            AbstractInsnNode lowerZero = previousReal(loadThis);
            require(subtract != null && subtract.getOpcode() == Opcodes.ISUB,
                    "Unexpected Xaero cave clamp upper bound");
            require(isIntConstant(one, 1) && getHeight instanceof MethodInsnNode,
                    "Unexpected Xaero cave clamp height lookup");
            require(worldGet instanceof org.objectweb.asm.tree.FieldInsnNode
                    && ((org.objectweb.asm.tree.FieldInsnNode) worldGet).name.equals(worldField)
                    && loadThis != null && loadThis.getOpcode() == Opcodes.ALOAD
                    && isIntConstant(lowerZero, 0),
                    "Unexpected Xaero cave clamp lower bound");
            AbstractInsnNode store = nextReal(clamp);
            require(store instanceof VarInsnNode && store.getOpcode() == Opcodes.ISTORE,
                    "Unexpected Xaero cave-start result local");

            replaceIntConstantWithWorldHook(method, lowerZero, 0, worldField,
                    "Lnet/minecraft/client/multiplayer/WorldClient;", "minY");
            method.instructions.insertBefore(getHeight, new MethodInsnNode(Opcodes.INVOKESTATIC,
                    HOOKS, "maxYInclusive", "(Lnet/minecraft/world/World;)I", false));
            method.instructions.remove(getHeight);
            method.instructions.remove(one);
            method.instructions.remove(subtract);
            newCaveStartLocal = ((VarInsnNode) store).var;
            patched++;
        }
        require(patched == 1 && newCaveStartLocal >= 0,
                "Expected one Xaero cave-start clamp, patched " + patched);

        int configuredCaveStartLocal = -1;
        int isMapScreenLocal = -1;
        int detectedCaveStartLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                org.objectweb.asm.tree.FieldInsnNode field =
                        (org.objectweb.asm.tree.FieldInsnNode) instruction;
                if (instruction.getOpcode() == Opcodes.GETSTATIC
                        && "CAVE_MODE_START".equals(field.name)) {
                    configuredCaveStartLocal = findNextIntStore(instruction, 8);
                }
            }
            if (instruction instanceof org.objectweb.asm.tree.TypeInsnNode
                    && instruction.getOpcode() == Opcodes.INSTANCEOF
                    && "xaero/map/gui/GuiMap".equals(
                            ((org.objectweb.asm.tree.TypeInsnNode) instruction).desc)) {
                isMapScreenLocal = findNextIntStore(instruction, 16);
            }
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("xaero/map/misc/CaveStartCalculator".equals(call.owner)
                        && "getCaving".equals(call.name)
                        && "(DDDLnet/minecraft/world/World;)I".equals(call.desc)) {
                    detectedCaveStartLocal = findNextIntStore(instruction, 4);
                }
            }
        }
        int automaticPreparationPatched = 0;
        int automaticResultPatched = 0;
        if (configuredCaveStartLocal >= 0 && isMapScreenLocal >= 0
                && detectedCaveStartLocal >= 0) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof org.objectweb.asm.tree.JumpInsnNode
                        && instruction.getOpcode() == Opcodes.IF_ICMPNE) {
                    AbstractInsnNode minimum = previousReal(instruction);
                    AbstractInsnNode caveStartLoad = previousReal(minimum);
                    AbstractInsnNode fallthrough = nextReal(instruction);
                    if (isIntConstant(minimum, Integer.MIN_VALUE)
                            && caveStartLoad instanceof VarInsnNode
                            && caveStartLoad.getOpcode() == Opcodes.ILOAD
                            && ((VarInsnNode) caveStartLoad).var == newCaveStartLocal
                            && fallthrough instanceof MethodInsnNode
                            && "java/lang/System".equals(((MethodInsnNode) fallthrough).owner)
                            && "currentTimeMillis".equals(
                                    ((MethodInsnNode) fallthrough).name)) {
                        method.instructions.insertBefore(caveStartLoad, list(
                                new VarInsnNode(Opcodes.ILOAD, newCaveStartLocal),
                                new VarInsnNode(Opcodes.ILOAD, isMapScreenLocal),
                                new VarInsnNode(Opcodes.ILOAD, configuredCaveStartLocal),
                                new VarInsnNode(Opcodes.ALOAD, 0),
                                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                                        node.name, worldField,
                                        "Lnet/minecraft/client/multiplayer/WorldClient;"),
                                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                        "prepareXaeroFullMapAutoCaveStart",
                                        "(IZILnet/minecraft/world/World;)I", false),
                                new VarInsnNode(Opcodes.ISTORE, newCaveStartLocal)));
                        automaticPreparationPatched++;
                    }
                }

                if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                        && instruction.getOpcode() == Opcodes.GETFIELD) {
                    org.objectweb.asm.tree.FieldInsnNode field =
                            (org.objectweb.asm.tree.FieldInsnNode) instruction;
                    AbstractInsnNode store = nextReal(instruction);
                    if ("localCaveMode".equals(field.name) && "I".equals(field.desc)
                            && store instanceof VarInsnNode
                            && store.getOpcode() == Opcodes.ISTORE
                            && ((VarInsnNode) store).var == newCaveStartLocal) {
                        method.instructions.insertBefore(store, list(
                                new VarInsnNode(Opcodes.ILOAD, isMapScreenLocal),
                                new VarInsnNode(Opcodes.ILOAD, configuredCaveStartLocal),
                                new VarInsnNode(Opcodes.ILOAD, detectedCaveStartLocal),
                                new VarInsnNode(Opcodes.ALOAD, 0),
                                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                                        node.name, worldField,
                                        "Lnet/minecraft/client/multiplayer/WorldClient;"),
                                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                        "resolveXaeroFullMapCaveStart",
                                        "(IZIILnet/minecraft/world/World;)I", false)));
                        automaticResultPatched++;
                    }
                }
            }
        }
        if (automaticPreparationPatched != 1 || automaticResultPatched != 1) {
            LOGGER.warn("Xaero full-map automatic cave-state patch incomplete for {}: "
                            + "new={}, configured={}, screen={}, detected={}, preparation={}, result={}",
                    node.name, newCaveStartLocal, configuredCaveStartLocal, isMapScreenLocal,
                    detectedCaveStartLocal, automaticPreparationPatched,
                    automaticResultPatched);
        } else {
            LOGGER.info("Patched Xaero world-map automatic cave-state ownership for {}",
                    node.name);
        }
        LOGGER.info("Patched Xaero world-map cave-start range for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroWorldDataReader(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "buildTile", "buildTile",
                "(Lnet/minecraft/nbt/NBTTagCompound;Lxaero/map/region/MapTile;"
                        + "Lxaero/map/region/MapTileChunk;IIIIIIZZLnet/minecraft/world/World;"
                        + "Lxaero/map/world/MapDimensionTypeInfo;Z)Z");
        patchXaeroLowYFloor(method, 12, true);
        patchXaeroMaxYConstants(method, 12, 2);

        int sectionOrderingPatched = 0;
        int dimensionHeightPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                AbstractInsnNode tagType = previousReal(instruction);
                AbstractInsnNode tagName = previousReal(tagType);
                AbstractInsnNode store = nextReal(instruction);
                if ("net/minecraft/nbt/NBTTagCompound".equals(call.owner)
                        && "(Ljava/lang/String;I)Lnet/minecraft/nbt/NBTTagList;".equals(call.desc)
                        && isIntConstant(tagType, 10)
                        && tagName instanceof LdcInsnNode
                        && "Sections".equals(((LdcInsnNode) tagName).cst)
                        && store instanceof VarInsnNode && store.getOpcode() == Opcodes.ASTORE) {
                    int sectionsLocal = ((VarInsnNode) store).var;
                    method.instructions.insert(store, list(
                            new VarInsnNode(Opcodes.ALOAD, sectionsLocal),
                            new VarInsnNode(Opcodes.ALOAD, 12),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                    "sortXaeroWorldMapSections",
                                    "(Lnet/minecraft/nbt/NBTTagList;Lnet/minecraft/world/World;)"
                                            + "Lnet/minecraft/nbt/NBTTagList;",
                                    false),
                            new VarInsnNode(Opcodes.ASTORE, sectionsLocal)));
                    sectionOrderingPatched++;
                }
                if ("xaero/map/world/MapDimensionTypeInfo".equals(call.owner)
                        && "getHeight".equals(call.name) && "()I".equals(call.desc)) {
                    AbstractInsnNode one = nextReal(instruction);
                    AbstractInsnNode subtract = nextReal(one);
                    require(isIntConstant(one, 1) && subtract != null
                            && subtract.getOpcode() == Opcodes.ISUB,
                            "Unexpected Xaero saved-world height expression");
                    method.instructions.insertBefore(instruction, list(
                            new InsnNode(Opcodes.POP),
                            new VarInsnNode(Opcodes.ALOAD, 12),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYInclusive",
                                    "(Lnet/minecraft/world/World;)I", false)));
                    method.instructions.remove(instruction);
                    method.instructions.remove(one);
                    method.instructions.remove(subtract);
                    dimensionHeightPatched++;
                }
            }
            instruction = next;
        }
        require(dimensionHeightPatched == 1,
                "Expected one Xaero saved-world dimension height, patched "
                        + dimensionHeightPatched);
        require(sectionOrderingPatched == 1,
                "Expected one Xaero saved-world section ordering, patched "
                        + sectionOrderingPatched);
        LOGGER.info("Patched Xaero saved-world cave scanning for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroMapSaveLoad(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getCacheFolder", "getCacheFolder",
                "(Ljava/nio/file/Path;)Ljava/nio/file/Path;");
        String mapProcessorField = findField(node, "Lxaero/map/MapProcessor;");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            AbstractInsnNode resolve = nextReal(instruction);
            if (!"java/lang/StringBuilder".equals(call.owner)
                    || !"toString".equals(call.name)
                    || !"()Ljava/lang/String;".equals(call.desc)
                    || !(resolve instanceof MethodInsnNode)
                    || !"java/nio/file/Path".equals(((MethodInsnNode) resolve).owner)
                    || !"resolve".equals(((MethodInsnNode) resolve).name)
                    || !"(Ljava/lang/String;)Ljava/nio/file/Path;"
                            .equals(((MethodInsnNode) resolve).desc)) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                            mapProcessorField, "Lxaero/map/MapProcessor;"),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "xaero/map/MapProcessor",
                            "getWorld", "()Lnet/minecraft/client/multiplayer/WorldClient;", false),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                            "resolveXaeroCacheFolderName",
                            "(Ljava/lang/String;Lnet/minecraft/world/World;)Ljava/lang/String;",
                            false)));
            patched++;
        }
        require(patched == 1, "Expected one Xaero cache-folder namespace, patched " + patched);
        LOGGER.info("Patched Xaero extended-height cache namespace for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroMinimapTile(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getHeight", "getHeight", "(II)I");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.IRETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                            "caveLevel", "I"),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                            "X", "I"),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                            "Z", "I"),
                    new VarInsnNode(Opcodes.ILOAD, 1),
                    new VarInsnNode(Opcodes.ILOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                            "decodeXaeroMinimapHeight", "(IIIIII)I", false)));
            patched++;
        }
        require(patched == 1, "Expected one Xaero minimap height return, patched " + patched);
        LOGGER.info("Patched Xaero minimap cached-height decoding for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroMapTileChunk(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethodByName(node, "updateBuffers");
        int worldLocal = -1;
        int tileLocal = -1;
        int xLocal = -1;
        int zLocal = -1;
        int caveStartLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if ("xaero/map/MapProcessor".equals(call.owner)
                    && "getWorld".equals(call.name)
                    && "()Lnet/minecraft/client/multiplayer/WorldClient;".equals(call.desc)) {
                AbstractInsnNode store = nextReal(instruction);
                require(store instanceof VarInsnNode && store.getOpcode() == Opcodes.ASTORE,
                        "Unexpected Xaero world-map world local");
                worldLocal = ((VarInsnNode) store).var;
            }
            if ("xaero/map/region/MapTile".equals(call.owner)
                    && "getWrittenCaveStart".equals(call.name) && "()I".equals(call.desc)) {
                AbstractInsnNode tileLoad = previousReal(instruction);
                AbstractInsnNode store = nextReal(instruction);
                require(tileLoad instanceof VarInsnNode
                                && tileLoad.getOpcode() == Opcodes.ALOAD
                                && store instanceof VarInsnNode
                                && store.getOpcode() == Opcodes.ISTORE,
                        "Unexpected Xaero cave-start local");
                tileLocal = ((VarInsnNode) tileLoad).var;
                caveStartLocal = ((VarInsnNode) store).var;
            }
            if ("xaero/map/region/MapTile".equals(call.owner)
                    && "getBlock".equals(call.name) && "(II)Lxaero/map/region/MapBlock;".equals(call.desc)) {
                AbstractInsnNode zLoad = previousReal(instruction);
                AbstractInsnNode xLoad = previousReal(zLoad);
                require(xLoad instanceof VarInsnNode && xLoad.getOpcode() == Opcodes.ILOAD
                                && zLoad instanceof VarInsnNode
                                && zLoad.getOpcode() == Opcodes.ILOAD,
                        "Unexpected Xaero world-map pixel locals");
                xLocal = ((VarInsnNode) xLoad).var;
                zLocal = ((VarInsnNode) zLoad).var;
            }
        }
        require(worldLocal >= 0 && tileLocal >= 0 && caveStartLocal >= 0
                        && xLocal >= 0 && zLocal >= 0,
                "Missing Xaero world-map height decoding locals");

        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"xaero/map/region/texture/RegionTexture".equals(call.owner)
                    && !"xaero/map/region/texture/LeafRegionTexture".equals(call.owner)) {
                continue;
            }
            if (!("getHeight".equals(call.name) || "getTopHeight".equals(call.name))
                    || !"(II)I".equals(call.desc)) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ILOAD, caveStartLocal),
                    new VarInsnNode(Opcodes.ALOAD, worldLocal),
                    new VarInsnNode(Opcodes.ALOAD, tileLocal),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "xaero/map/region/MapTile",
                            "getChunkX", "()I", false),
                    new VarInsnNode(Opcodes.ALOAD, tileLocal),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "xaero/map/region/MapTile",
                            "getChunkZ", "()I", false),
                    new VarInsnNode(Opcodes.ILOAD, xLocal),
                    new VarInsnNode(Opcodes.ILOAD, zLocal),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "decodeXaeroWorldMapHeightAt",
                            "(IILnet/minecraft/world/World;IIII)I", false)));
            patched++;
        }
        require(patched == 2, "Expected two Xaero world-map height reads, patched " + patched);
        LOGGER.info("Patched Xaero world-map cached-height decoding for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroMapBlock(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethodByName(node, "fixHeightType");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            boolean mapBlockHeight = "xaero/map/region/MapBlock".equals(call.owner)
                    && "getEffectiveHeight".equals(call.name) && call.desc.endsWith(")I");
            boolean textureHeight = "xaero/map/region/texture/LeafRegionTexture".equals(call.owner)
                    && "getHeight".equals(call.name) && "(II)I".equals(call.desc);
            if (!mapBlockHeight && !textureHeight) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ILOAD, 8),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "decodeXaeroHeightNearReference", "(II)I", false)));
            patched++;
        }
        require(patched == 5, "Expected five Xaero world-map slope heights, patched " + patched);
        LOGGER.info("Patched Xaero world-map extended surface slopes for {}", node.name);
        return write(node);
    }

    private static byte[] transformXaeroCaveOptions(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethodByName(node, "createSlider");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (!isIntConstant(instruction, 255)) {
                instruction = next;
                continue;
            }
            AbstractInsnNode maxStore = nextReal(instruction);
            AbstractInsnNode minStore = previousReal(instruction);
            AbstractInsnNode minZero = previousReal(minStore);
            if (!(maxStore instanceof VarInsnNode) || maxStore.getOpcode() != Opcodes.ISTORE
                    || !(minStore instanceof VarInsnNode)
                    || minStore.getOpcode() != Opcodes.ISTORE
                    || !isIntConstant(minZero, 0)) {
                instruction = next;
                continue;
            }
            method.instructions.insertBefore(minZero, new MethodInsnNode(Opcodes.INVOKESTATIC,
                    CLIENT_HOOKS, "currentWorldMinY", "()I", false));
            method.instructions.remove(minZero);
            method.instructions.insertBefore(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                    CLIENT_HOOKS, "currentWorldMaxYInclusive", "()I", false));
            method.instructions.remove(instruction);
            patched++;
            instruction = next;
        }
        require(patched == 1, "Expected one Xaero cave slider range, patched " + patched);
        LOGGER.info("Patched Xaero cave-mode slider world-height range for {}", node.name);
        return write(node);
    }

    private static void patchXaeroCaveDetector(MethodNode method, int worldLocal) {
        int yLocal = -1;
        int topLocal = -1;
        int lowerBoundPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != Opcodes.INVOKESTATIC || !"java/lang/Math".equals(call.owner)
                    || !"max".equals(call.name) || !"(II)I".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode lowerZero = previousReal(instruction);
            AbstractInsnNode store = nextReal(instruction);
            if (!isIntConstant(lowerZero, 0) || !(store instanceof VarInsnNode)
                    || store.getOpcode() != Opcodes.ISTORE) {
                continue;
            }
            replaceIntConstantWithWorldHook(method, lowerZero, worldLocal, "minY");
            yLocal = ((VarInsnNode) store).var;
            lowerBoundPatched++;
            break;
        }
        require(lowerBoundPatched == 1 && yLocal >= 0,
                "Missing Xaero cave detector lower bound");

        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof VarInsnNode) || instruction.getOpcode() != Opcodes.ILOAD
                    || ((VarInsnNode) instruction).var != yLocal) {
                continue;
            }
            AbstractInsnNode bottomStore = nextReal(instruction);
            AbstractInsnNode minusOne = nextReal(bottomStore);
            AbstractInsnNode topStore = nextReal(minusOne);
            if (bottomStore instanceof VarInsnNode && bottomStore.getOpcode() == Opcodes.ISTORE
                    && isIntConstant(minusOne, -1) && topStore instanceof VarInsnNode
                    && topStore.getOpcode() == Opcodes.ISTORE) {
                topLocal = ((VarInsnNode) topStore).var;
                break;
            }
        }
        require(topLocal >= 0, "Missing Xaero cave detector top local");

        int zeroComparisonsPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || instruction.getOpcode() != Opcodes.IFGE) {
                continue;
            }
            AbstractInsnNode load = previousReal(instruction);
            if (!(load instanceof VarInsnNode) || load.getOpcode() != Opcodes.ILOAD) {
                continue;
            }
            int variable = ((VarInsnNode) load).var;
            if (variable != yLocal && variable != topLocal) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, worldLocal),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            ((org.objectweb.asm.tree.JumpInsnNode) instruction).setOpcode(Opcodes.IF_ICMPGE);
            zeroComparisonsPatched++;
        }
        require(zeroComparisonsPatched == 2,
                "Expected two Xaero cave detector lower comparisons, patched "
                        + zeroComparisonsPatched);

        patchXaeroMaxYConstants(method, worldLocal, 3);

        int playerSectionLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.ISHR) {
                continue;
            }
            AbstractInsnNode four = previousReal(instruction);
            AbstractInsnNode loadY = previousReal(four);
            AbstractInsnNode store = nextReal(instruction);
            if (!isIntConstant(four, 4) || !(loadY instanceof VarInsnNode)
                    || loadY.getOpcode() != Opcodes.ILOAD
                    || ((VarInsnNode) loadY).var != yLocal
                    || !(store instanceof VarInsnNode) || store.getOpcode() != Opcodes.ISTORE) {
                continue;
            }
            method.instructions.insertBefore(four, list(
                    new VarInsnNode(Opcodes.ALOAD, worldLocal),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndex",
                            "(ILnet/minecraft/world/World;)I", false)));
            method.instructions.remove(four);
            method.instructions.remove(instruction);
            playerSectionLocal = ((VarInsnNode) store).var;
            break;
        }
        require(playerSectionLocal >= 0, "Missing Xaero cave detector section index");

        int loopSectionLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof VarInsnNode) || instruction.getOpcode() != Opcodes.ILOAD
                    || ((VarInsnNode) instruction).var != playerSectionLocal) {
                continue;
            }
            AbstractInsnNode store = nextReal(instruction);
            if (store instanceof VarInsnNode && store.getOpcode() == Opcodes.ISTORE) {
                loopSectionLocal = ((VarInsnNode) store).var;
                break;
            }
        }
        require(loopSectionLocal >= 0, "Missing Xaero cave detector section loop");

        int sectionYPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == Opcodes.ISHL) {
                AbstractInsnNode four = previousReal(instruction);
                AbstractInsnNode loadSection = previousReal(four);
                if (isIntConstant(four, 4) && loadSection instanceof VarInsnNode
                        && loadSection.getOpcode() == Opcodes.ILOAD
                        && ((VarInsnNode) loadSection).var == loopSectionLocal) {
                    method.instructions.insertBefore(four, list(
                            new VarInsnNode(Opcodes.ALOAD, worldLocal),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                    "sectionMinYForStorageIndex",
                                    "(ILnet/minecraft/world/World;)I", false)));
                    method.instructions.remove(four);
                    method.instructions.remove(instruction);
                    sectionYPatched++;
                }
            }
            instruction = next;
        }
        require(sectionYPatched == 2,
                "Expected two Xaero cave detector section heights, patched " + sectionYPatched);
    }

    private static void patchXaeroLowYFloor(MethodNode method, int worldLocal,
            boolean patchInitialValue) {
        int lowYLocal = -1;
        AbstractInsnNode clampAssignment = null;
        org.objectweb.asm.tree.JumpInsnNode comparison = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || instruction.getOpcode() != Opcodes.IFGE) {
                continue;
            }
            AbstractInsnNode load = previousReal(instruction);
            AbstractInsnNode zero = nextReal(instruction);
            AbstractInsnNode store = nextReal(zero);
            if (!(load instanceof VarInsnNode) || load.getOpcode() != Opcodes.ILOAD
                    || !isIntConstant(zero, 0) || !(store instanceof VarInsnNode)
                    || store.getOpcode() != Opcodes.ISTORE
                    || ((VarInsnNode) load).var != ((VarInsnNode) store).var) {
                continue;
            }
            lowYLocal = ((VarInsnNode) load).var;
            comparison = (org.objectweb.asm.tree.JumpInsnNode) instruction;
            clampAssignment = zero;
            break;
        }
        require(lowYLocal >= 0 && comparison != null,
                "Missing Xaero cave scan lower-bound clamp in " + method.name);

        method.instructions.insertBefore(comparison, list(
                new VarInsnNode(Opcodes.ALOAD, worldLocal),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                        "(Lnet/minecraft/world/World;)I", false)));
        comparison.setOpcode(Opcodes.IF_ICMPGE);
        replaceIntConstantWithWorldHook(method, clampAssignment, worldLocal, "minY");

        if (!patchInitialValue) {
            return;
        }
        AbstractInsnNode initialValue = null;
        for (AbstractInsnNode instruction = comparison.getPrevious(); instruction != null;
                instruction = instruction.getPrevious()) {
            if (!(instruction instanceof VarInsnNode) || instruction.getOpcode() != Opcodes.ISTORE
                    || ((VarInsnNode) instruction).var != lowYLocal) {
                continue;
            }
            AbstractInsnNode value = previousReal(instruction);
            if (isIntConstant(value, 0)) {
                initialValue = value;
                break;
            }
        }
        require(initialValue != null, "Missing Xaero initial cave scan lower bound");
        replaceIntConstantWithWorldHook(method, initialValue, worldLocal, "minY");
    }

    private static void patchXaeroMaxYConstants(MethodNode method, int worldLocal,
            int expected) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (isIntConstant(instruction, 255)
                    && (nextReal(instruction) == null
                            || nextReal(instruction).getOpcode() != Opcodes.IAND)) {
                replaceIntConstantWithWorldHook(method, instruction, worldLocal, "maxYInclusive");
                patched++;
            }
            instruction = next;
        }
        require(patched == expected,
                "Expected " + expected + " Xaero max-Y constants in " + method.name
                        + ", patched " + patched);
    }

    private static int patchXaeroWorldMapEffectiveHeights(MethodNode method,
            int caveStartLocal) {
        int surfaceReferenceLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/world/chunk/Chunk".equals(call.owner)
                    || !("getHeightValue".equals(call.name)
                            || "func_76611_b".equals(call.name))
                    || !"(II)I".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode store = nextReal(instruction);
            require(store instanceof VarInsnNode && store.getOpcode() == Opcodes.ISTORE,
                    "Unexpected Xaero world-map surface height local");
            call.setOpcode(Opcodes.INVOKESTATIC);
            call.owner = HOOKS;
            call.name = "getXaeroSurfaceHeight";
            call.desc = "(Lnet/minecraft/world/chunk/Chunk;II)I";
            call.itf = false;
            surfaceReferenceLocal = ((VarInsnNode) store).var;
            break;
        }
        require(surfaceReferenceLocal >= 0, "Missing Xaero world-map surface height local");

        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"xaero/map/region/MapBlock".equals(call.owner)
                    || !"getEffectiveHeight".equals(call.name) || !call.desc.endsWith(")I")) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ILOAD, caveStartLocal),
                    new VarInsnNode(Opcodes.ILOAD, surfaceReferenceLocal),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                            "decodeXaeroWorldMapHeight", "(III)I", false)));
            patched++;
        }
        return patched;
    }

    private static void replaceIntConstantWithWorldHook(MethodNode method,
            AbstractInsnNode constant, int worldLocal, String hook) {
        method.instructions.insertBefore(constant, list(
                new VarInsnNode(Opcodes.ALOAD, worldLocal),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hook,
                        "(Lnet/minecraft/world/World;)I", false)));
        method.instructions.remove(constant);
    }

    private static void replaceIntConstantWithWorldHook(MethodNode method,
            AbstractInsnNode constant, int thisLocal, String worldField, String worldDesc,
            String hook) {
        method.instructions.insertBefore(constant, list(
                new VarInsnNode(Opcodes.ALOAD, thisLocal),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                        "xaero/map/MapProcessor", worldField, worldDesc),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hook,
                        "(Lnet/minecraft/world/World;)I", false)));
        method.instructions.remove(constant);
    }

    private static byte[] transformVoxelMap(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode newWorld = findMethod(node, "newWorld", "newWorld",
                "(Lnet/minecraft/world/World;)V");
        newWorld.instructions.insertBefore(newWorld.instructions.getFirst(), list(
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "setVoxelMapWorld",
                        "(Lnet/minecraft/world/World;)V", false),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYExclusive",
                        "(Lnet/minecraft/world/World;)I", false),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, node.name,
                        "worldHeight", "I")));

        MethodNode blockHeight = findMethod(node, "getBlockHeight", "getBlockHeight",
                "(ZZLnet/minecraft/world/World;II)I");
        replace(blockHeight, list(
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ALOAD, 3),
                new VarInsnNode(Opcodes.ILOAD, 4),
                new VarInsnNode(Opcodes.ILOAD, 5),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                        "lastY", "I"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getVoxelMapBlockHeight",
                        "(ZZLnet/minecraft/world/World;III)I", false),
                new InsnNode(Opcodes.IRETURN)));

        MethodNode seafloor = findMethod(node, "getSeafloorHeight", "getSeafloorHeight",
                "(Lnet/minecraft/world/World;III)[I");
        replace(seafloor, list(
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new VarInsnNode(Opcodes.ILOAD, 4),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "getVoxelMapSeafloorHeight",
                        "(Lnet/minecraft/world/World;III)[I", false),
                new InsnNode(Opcodes.ARETURN)));
        int clamps = patchVoxelMapHeightClamps(node);
        require(clamps == 3, "Expected three VoxelMap world-height clamps, patched " + clamps);
        LOGGER.info("Patched VoxelMap live-map height scans and bounds");
        return write(node);
    }

    private static byte[] transformVoxelMapData(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchVoxelMapDataGetter(node, "getHeight", 0);
        patchVoxelMapDataGetter(node, "getOceanFloorHeight", 4);
        patchVoxelMapDataGetter(node, "getTransparentHeight", 8);
        patchVoxelMapDataGetter(node, "getFoliageHeight", 12);
        patchVoxelMapDataSetter(node, "setHeight", 0);
        patchVoxelMapDataSetter(node, "setOceanFloorHeight", 4);
        patchVoxelMapDataSetter(node, "setTransparentHeight", 8);
        patchVoxelMapDataSetter(node, "setFoliageHeight", 12);
        LOGGER.info("Patched VoxelMap persistent height encoding for extended worlds");
        return write(node);
    }

    private static byte[] transformVoxelMapWaypointTeleport(byte[] basicClass,
            String methodName, String methodDesc) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, methodName, methodName, methodDesc);
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode getY = (MethodInsnNode) instruction;
                AbstractInsnNode fallbackJump = nextReal(getY);
                if (getY.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && "com/mamiyaotaru/voxelmap/util/Waypoint".equals(getY.owner)
                        && "getY".equals(getY.name) && "()I".equals(getY.desc)
                        && fallbackJump instanceof JumpInsnNode
                        && fallbackJump.getOpcode() == Opcodes.IFLE) {
                    JumpInsnNode oldJump = (JumpInsnNode) fallbackJump;
                    method.instructions.insertBefore(oldJump, list(
                            new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                    "shouldUseVoxelMapWaypointFallback", "(I)Z", false),
                            new JumpInsnNode(Opcodes.IFNE, oldJump.label)));
                    method.instructions.remove(oldJump);
                    patched++;
                }
            }
            instruction = next;
        }
        require(patched == 1, "Expected one VoxelMap waypoint teleport height fallback in "
                + node.name + "." + methodName + ", patched " + patched);
        LOGGER.info("Patched VoxelMap waypoint teleport height in {}", node.name);
        return write(node);
    }

    private static void patchVoxelMapDataGetter(ClassNode node, String name, int bit) {
        MethodNode method = findMethod(node, name, name, "(II)I");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new IntInsnNode(Opcodes.BIPUSH, bit),
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "getData",
                        "(III)B", false),
                new IntInsnNode(Opcodes.SIPUSH, 255),
                new InsnNode(Opcodes.IAND),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "decodeVoxelMapHeight",
                        "(I)I", false),
                new InsnNode(Opcodes.IRETURN)));
    }

    private static void patchVoxelMapDataSetter(ClassNode node, String name, int bit) {
        MethodNode method = findMethod(node, name, name, "(III)V");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new IntInsnNode(Opcodes.BIPUSH, bit),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "encodeVoxelMapHeight",
                        "(I)I", false),
                new InsnNode(Opcodes.I2B),
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "setData",
                        "(IIIB)V", false),
                new InsnNode(Opcodes.RETURN)));
    }

    private static int patchVoxelMapHeightClamps(ClassNode node) {
        int patched = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (!(instruction instanceof MethodInsnNode)
                        || instruction.getOpcode() != Opcodes.INVOKESTATIC) {
                    instruction = next;
                    continue;
                }
                MethodInsnNode max = (MethodInsnNode) instruction;
                if (!"java/lang/Math".equals(max.owner) || !"max".equals(max.name)
                        || !"(II)I".equals(max.desc)) {
                    instruction = next;
                    continue;
                }
                AbstractInsnNode zero = previousReal(instruction);
                AbstractInsnNode minimum = previousReal(zero);
                if (zero == null || zero.getOpcode() != Opcodes.ICONST_0
                        || !(minimum instanceof MethodInsnNode)
                        || !"java/lang/Math".equals(((MethodInsnNode) minimum).owner)
                        || !"min".equals(((MethodInsnNode) minimum).name)
                        || !"(II)I".equals(((MethodInsnNode) minimum).desc)) {
                    instruction = next;
                    continue;
                }
                InsnList replacement = new InsnList();
                int worldLocal = findWorldArgumentLocal(method);
                if (worldLocal >= 0) {
                    replacement.add(new VarInsnNode(Opcodes.ALOAD, worldLocal));
                } else {
                    replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    replacement.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                            node.name, "world", "Lnet/minecraft/world/World;"));
                }
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "clampYToWorld", "(ILnet/minecraft/world/World;)I", false));
                method.instructions.insertBefore(zero, replacement);
                method.instructions.remove(zero);
                method.instructions.remove(instruction);
                patched++;
                instruction = next;
            }
        }
        return patched;
    }

    private static int findWorldArgumentLocal(MethodNode method) {
        Type[] arguments = Type.getArgumentTypes(method.desc);
        int local = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type argument : arguments) {
            if ("Lnet/minecraft/world/World;".equals(argument.getDescriptor())) {
                return local;
            }
            local += argument.getSize();
        }
        return -1;
    }

    private static byte[] transformStructureStart(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "generateStructure", "func_75068_a",
                "(Lnet/minecraft/world/World;Ljava/util/Random;"
                        + "Lnet/minecraft/world/gen/structure/StructureBoundingBox;)V");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.RETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, MINESHAFT_HOOKS,
                            "addModernMineshaftSupports",
                            "(Lnet/minecraft/world/gen/structure/StructureStart;"
                                    + "Lnet/minecraft/world/World;"
                                    + "Lnet/minecraft/world/gen/structure/StructureBoundingBox;)V",
                            false)));
            patched++;
        }
        require(patched == 1, "Expected one structure generation return, patched " + patched);
        LOGGER.info("Patched completed mineshafts with support columns and hanging chains");
        return write(node);
    }

    private static byte[] transformAncientWarfareStructureBuilder(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode placeBlock = findMethod(node, "placeBlock", "placeBlock",
                "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z");
        int blockLowerBounds = 0;
        for (AbstractInsnNode instruction = placeBlock.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                    || instruction.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/util/math/BlockPos".equals(call.owner)
                    || !"()I".equals(call.desc)
                    || !("getY".equals(call.name) || "func_177956_o".equals(call.name))) {
                continue;
            }
            AbstractInsnNode jumpNode = nextReal(instruction);
            if (!(jumpNode instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || jumpNode.getOpcode() != Opcodes.IFLE) {
                continue;
            }
            placeBlock.instructions.insertBefore(jumpNode, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, "world",
                            "Lnet/minecraft/world/World;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            org.objectweb.asm.tree.JumpInsnNode jump =
                    (org.objectweb.asm.tree.JumpInsnNode) jumpNode;
            placeBlock.instructions.set(jump,
                    new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IF_ICMPLT, jump.label));
            blockLowerBounds++;
            break;
        }
        require(blockLowerBounds == 1,
                "Expected one Ancient Warfare StructureBuilder lower bound, patched " + blockLowerBounds);

        MethodNode placeRule = findMethod(node, "placeRule", "placeRule",
                "(Lnet/shadowmage/ancientwarfare/structure/api/TemplateRule;)V");
        int ruleLowerBounds = 0;
        for (AbstractInsnNode instruction = placeRule.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                    || instruction.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/util/math/BlockPos".equals(call.owner)
                    || !"()I".equals(call.desc)
                    || !("getY".equals(call.name) || "func_177956_o".equals(call.name))) {
                continue;
            }
            AbstractInsnNode jumpNode = nextReal(instruction);
            if (!(jumpNode instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || jumpNode.getOpcode() != Opcodes.IFGT) {
                continue;
            }
            placeRule.instructions.insertBefore(jumpNode, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, "world",
                            "Lnet/minecraft/world/World;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            org.objectweb.asm.tree.JumpInsnNode jump =
                    (org.objectweb.asm.tree.JumpInsnNode) jumpNode;
            placeRule.instructions.set(jump,
                    new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IF_ICMPGE, jump.label));
            ruleLowerBounds++;
            break;
        }
        require(ruleLowerBounds == 1,
                "Expected one Ancient Warfare StructureBuilder rule lower bound, patched " + ruleLowerBounds);

        MethodNode topSolid = findMethod(node, "isTopBlockSolid", "isTopBlockSolid",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z");
        int topSolidLowerBounds = 0;
        for (AbstractInsnNode instruction = topSolid.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || instruction.getOpcode() != Opcodes.IFLT) {
                continue;
            }
            topSolid.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            org.objectweb.asm.tree.JumpInsnNode jump =
                    (org.objectweb.asm.tree.JumpInsnNode) instruction;
            topSolid.instructions.set(jump,
                    new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IF_ICMPLT, jump.label));
            topSolidLowerBounds++;
            break;
        }
        require(topSolidLowerBounds == 1,
                "Expected one Ancient Warfare top-solid lower bound, patched " + topSolidLowerBounds);
        LOGGER.info("Patched Ancient Warfare structure placement for extended lower height");
        return write(node);
    }

    private static byte[] transformAncientWarfareWorldStructureGenerator(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getTargetY", "getTargetY",
                "(Lnet/minecraft/world/World;IIZI)I");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || instruction.getOpcode() != Opcodes.IFLE) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            org.objectweb.asm.tree.JumpInsnNode jump =
                    (org.objectweb.asm.tree.JumpInsnNode) instruction;
            method.instructions.set(jump,
                    new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IF_ICMPLT, jump.label));
            patched++;
            break;
        }
        require(patched == 1,
                "Expected one Ancient Warfare target-Y lower bound, patched " + patched);
        LOGGER.info("Patched Ancient Warfare target-Y scan for extended lower height");
        return write(node);
    }

    private static byte[] transformAncientWarfareSmoothingMatrix(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "decorate", "decorate",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/world/biome/Biome".equals(call.owner)
                    || !"(Lnet/minecraft/world/World;Ljava/util/Random;"
                            .concat("Lnet/minecraft/util/math/BlockPos;)V").equals(call.desc)
                    || !("decorate".equals(call.name) || "func_180624_a".equals(call.name))) {
                continue;
            }
            call.setOpcode(Opcodes.INVOKESTATIC);
            call.owner = "xy177/farmersfuturedelight/core/FFDHeightHooks";
            call.name = "decorateBiome";
            call.desc = "(Lnet/minecraft/world/biome/Biome;Lnet/minecraft/world/World;Ljava/util/Random;"
                    + "Lnet/minecraft/util/math/BlockPos;)V";
            patched++;
        }
        require(patched == 1,
                "Expected one Ancient Warfare biome decoration call, patched " + patched);
        LOGGER.info("Patched Ancient Warfare biome decoration for legacy height assumptions");
        return write(node);
    }

    private static byte[] transformIceAndFireWorldgenEvents(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "degradeSurface", "degradeSurface",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.ICONST_1) {
                continue;
            }
            AbstractInsnNode jumpNode = nextReal(instruction);
            if (!(jumpNode instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || jumpNode.getOpcode() != Opcodes.IF_ICMPLE) {
                continue;
            }
            method.instructions.remove(instruction);
            method.instructions.insertBefore(jumpNode, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            patched++;
            break;
        }
        require(patched == 1,
                "Expected one Ice and Fire degradeSurface lower bound, patched " + patched);
        LOGGER.info("Patched Ice and Fire surface degradation for extended lower height");
        return write(node);
    }

    private static byte[] transformCommandBase(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "parseBlockPos", "func_175757_a",
                "(Lnet/minecraft/command/ICommandSender;[Ljava/lang/String;IZ)Lnet/minecraft/util/math/BlockPos;");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "parseBlockPos",
                        "(Lnet/minecraft/command/ICommandSender;[Ljava/lang/String;IZ)Lnet/minecraft/util/math/BlockPos;", false),
                new InsnNode(Opcodes.ARETURN)));
        LOGGER.info("Patched command coordinate parsing for extended build height");
        return write(node);
    }

    private static byte[] transformCauldron(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "fillWithRain", "func_176224_k",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V");
        org.objectweb.asm.tree.LabelNode vanilla = new org.objectweb.asm.tree.LabelNode();
        method.instructions.insertBefore(method.instructions.getFirst(), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ALOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "handleCauldronPrecipitation",
                        "(Lnet/minecraft/block/BlockCauldron;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
                        false),
                new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IFEQ, vanilla),
                new InsnNode(Opcodes.RETURN),
                vanilla,
                new FrameNode(Opcodes.F_SAME, 0, null, 0, null)));
        LOGGER.info("Patched cauldron precipitation for rain and powder snow");
        return write(node);
    }

    private static byte[] transformJukebox(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getComparatorInputOverride", "func_180641_l",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;"
                        + "Lnet/minecraft/util/math/BlockPos;)I");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.IRETURN
                    || previousReal(instruction).getOpcode() == Opcodes.ICONST_0) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "adjustJukeboxComparatorOutput",
                            "(ILnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I",
                            false)));
            patched++;
        }
        require(patched >= 1, "Expected at least one occupied-jukebox comparator return, patched " + patched);
        LOGGER.info("Patched jukebox comparator output for modern music discs");
        return write(node);
    }

    private static byte[] transformBlock(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getPlayerRelativeBlockHardness", "func_180647_a",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;"
                        + "Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.FRETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "adjustInfestedBlockBreakProgress",
                            "(FLnet/minecraft/block/state/IBlockState;"
                                    + "Lnet/minecraft/entity/player/EntityPlayer;"
                                    + "Lnet/minecraft/world/World;"
                                    + "Lnet/minecraft/util/math/BlockPos;)F",
                            false)));
            patched++;
        }
        require(patched == 1, "Expected one Block break-progress return, patched " + patched);
        LOGGER.info("Patched infested-block break progress");
        return write(node);
    }

    private static byte[] transformDynamicLiquid(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "isBlocked", "func_176372_g",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;)Z");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.IRETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "blocksFlowingWater",
                            "(Lnet/minecraft/block/Block;"
                                    + "Lnet/minecraft/block/state/IBlockState;)Z",
                            false),
                    new InsnNode(Opcodes.IOR)));
            patched++;
        }
        require(patched > 0, "Missing BlockDynamicLiquid isBlocked returns");
        LOGGER.info("Patched flowing-water interaction with waterloggable blocks at {} returns",
                patched);
        return write(node);
    }

    private static byte[] transformPistonBase(byte[] basicClass) {
        ClassNode node = read(basicClass);
        if (!CAVE_BIOMES_HEIGHT_CORE) {
            MethodNode canPush = findMethod(node, "canPush", "func_185646_a",
                    "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;"
                            + "Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;"
                            + "ZLnet/minecraft/util/EnumFacing;)Z");
            int lowerBounds = 0;
            for (AbstractInsnNode instruction = canPush.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!"net/minecraft/util/math/BlockPos".equals(call.owner)
                        || !"()I".equals(call.desc)
                        || !("getY".equals(call.name) || "func_177956_o".equals(call.name))) {
                    continue;
                }
                AbstractInsnNode next = nextReal(instruction);
                if (!(next instanceof JumpInsnNode)
                        || (next.getOpcode() != Opcodes.IFLT
                                && next.getOpcode() != Opcodes.IFEQ)) {
                    continue;
                }
                canPush.instructions.insertBefore(next, list(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                "(Lnet/minecraft/world/World;)I", false)));
                ((JumpInsnNode) next).setOpcode(next.getOpcode() == Opcodes.IFLT
                        ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPEQ);
                lowerBounds++;
            }
            require(lowerBounds == 2,
                    "Expected two piston lower build-height comparisons, patched " + lowerBounds);
        }
        MethodNode method = findMethod(node, "doMove", "func_176319_a",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/util/EnumFacing;Z)Z");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    .concat("Lnet/minecraft/block/state/IBlockState;FI)V").equals(call.desc)
                    || !("dropBlockAsItemWithChance".equals(call.name)
                            || "func_180653_a".equals(call.name))) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 13),
                    new VarInsnNode(Opcodes.ALOAD, 14),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "spawnPistonDestroyParticles",
                            "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                                    + "Lnet/minecraft/block/state/IBlockState;)V",
                            false)));
            patched++;
        }
        require(patched == 1, "Expected one piston destroy loop, patched " + patched);
        LOGGER.info("Patched piston-destroyed block particles");
        if (CAVE_BIOMES_HEIGHT_CORE) {
            LOGGER.info("Delegated piston build-height bounds to CaveBiomesAPI");
        } else {
            LOGGER.info("Patched piston build-height bounds");
        }
        return write(node);
    }

    private static byte[] transformFallingBlockLogic(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "checkFallable", "func_176503_e",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V");
        int patched = patchDynamicLowerBounds(method, 1, 2, 3);
        require(patched == 3, "Expected three BlockFalling lower bounds, patched " + patched);
        LOGGER.info("Patched BlockFalling lower build-height bounds");
        return write(node);
    }

    private static byte[] transformDragonEgg(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "checkFall", "func_180683_d",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V");
        int patched = patchDynamicLowerBounds(method, 1, 2, 3);
        require(patched == 3, "Expected three BlockDragonEgg lower bounds, patched " + patched);
        LOGGER.info("Patched BlockDragonEgg lower build-height bounds");
        return write(node);
    }

    private static int patchDynamicLowerBounds(MethodNode method, int worldLocal,
            int posLocal, int expected) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)
                    && ("getY".equals(((MethodInsnNode) instruction).name)
                            || "func_177956_o".equals(((MethodInsnNode) instruction).name))) {
                AbstractInsnNode jump = nextReal(instruction);
                if (jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && (jump.getOpcode() == Opcodes.IFLT || jump.getOpcode() == Opcodes.IFLE)) {
                    int opcode = jump.getOpcode() == Opcodes.IFLT
                            ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPLE;
                    method.instructions.insertBefore(jump, list(
                            new VarInsnNode(Opcodes.ALOAD, worldLocal),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                    "(Lnet/minecraft/world/World;)I", false)));
                    ((org.objectweb.asm.tree.JumpInsnNode) jump).setOpcode(opcode);
                    patched++;
                }
            }
            instruction = next;
        }
        return patched;
    }

    private static byte[] transformBlockPortal(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode update = findMethod(node, "updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
        int patched = patchDynamicLowerBounds(update, 1, 2, 1);
        require(patched == 1, "Expected one portal lower bound, patched " + patched);
        LOGGER.info("Patched portal lower build-height bounds");
        return write(node);
    }

    private static byte[] transformPortalSize(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode constructor = findMethod(node, "<init>", "<init>",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/util/EnumFacing$Axis;)V");
        int patched = patchDynamicLowerBounds(constructor, 1, 2, 1);
        require(patched == 1, "Expected one portal-size lower bound, patched " + patched);
        LOGGER.info("Patched portal-size lower build-height bound");
        return write(node);
    }

    private static byte[] transformMushroom(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "canBlockStay", "func_180671_f",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;)Z");
        int[] patched = patchDynamicWorldBounds(method, 1);
        require(patched[0] == 1 && patched[1] == 1,
                "Expected mushroom min/max bounds, patched " + patched[0] + "/" + patched[1]);
        LOGGER.info("Patched mushroom build-height bounds");
        return write(node);
    }

    private static byte[] transformLilyPad(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "canBlockStay", "func_180671_f",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;)Z");
        int[] patched = patchDynamicWorldBounds(method, 1);
        require(patched[0] == 1 && patched[1] == 1,
                "Expected lily-pad min/max bounds, patched " + patched[0] + "/" + patched[1]);
        LOGGER.info("Patched lily-pad build-height bounds");
        return write(node);
    }

    private static int[] patchDynamicWorldBounds(MethodNode method, int worldLocal) {
        int minPatched = 0;
        int maxPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)
                    && ("getY".equals(((MethodInsnNode) instruction).name)
                            || "func_177956_o".equals(((MethodInsnNode) instruction).name))) {
                AbstractInsnNode operand = nextReal(instruction);
                if (operand instanceof org.objectweb.asm.tree.JumpInsnNode
                        && operand.getOpcode() == Opcodes.IFLT) {
                    method.instructions.insertBefore(operand,
                            new VarInsnNode(Opcodes.ALOAD, worldLocal));
                    method.instructions.insertBefore(operand,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                    "(Lnet/minecraft/world/World;)I", false));
                    ((org.objectweb.asm.tree.JumpInsnNode) operand).setOpcode(Opcodes.IF_ICMPLT);
                    minPatched++;
                    instruction = next;
                    continue;
                }
                AbstractInsnNode jump = nextReal(operand);
                if (isIntConstant(operand, 0) && jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && jump.getOpcode() == Opcodes.IF_ICMPLT) {
                    method.instructions.insertBefore(operand,
                            new VarInsnNode(Opcodes.ALOAD, worldLocal));
                    method.instructions.insertBefore(operand, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            HOOKS, "minY", "(Lnet/minecraft/world/World;)I", false));
                    method.instructions.remove(operand);
                    minPatched++;
                } else if (isIntConstant(operand, 256)
                        && jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && (jump.getOpcode() == Opcodes.IF_ICMPGE
                                || jump.getOpcode() == Opcodes.IF_ICMPLT)) {
                    method.instructions.insertBefore(operand,
                            new VarInsnNode(Opcodes.ALOAD, worldLocal));
                    method.instructions.insertBefore(operand, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            HOOKS, "maxYExclusive", "(Lnet/minecraft/world/World;)I", false));
                    method.instructions.remove(operand);
                    maxPatched++;
                }
            }
            instruction = next;
        }
        return new int[] {minPatched, maxPatched};
    }

    private static byte[] transformGrass(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
        int[] patched = patchDynamicWorldBounds(method, 1);
        require(patched[0] == 1 && patched[1] == 1,
                "Expected grass min/max bounds, patched " + patched[0] + "/" + patched[1]);
        LOGGER.info("Patched grass spread build-height bounds");
        return write(node);
    }

    private static byte[] transformStaticLiquid(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
        int[] updatePatched = patchDynamicWorldBounds(method, 1);
        MethodNode burn = findMethod(node, "getCanBlockBurn", "func_176368_m",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z");
        int[] burnPatched = patchDynamicWorldBounds(burn, 1);
        require(updatePatched[0] == 2 && updatePatched[1] == 1,
                "Expected static-liquid update min/max bounds, patched "
                        + updatePatched[0] + "/" + updatePatched[1]);
        require(burnPatched[0] == 1 && burnPatched[1] == 1,
                "Expected static-liquid burn min/max bounds, patched "
                        + burnPatched[0] + "/" + burnPatched[1]);
        LOGGER.info("Patched static-liquid fire checks build-height bounds");
        return write(node);
    }

    private static byte[] transformChorusFlower(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "updateTick", "func_180650_b",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
        int[] patched = patchDynamicWorldBounds(method, 1);
        require(patched[0] == 0 && patched[1] == 1,
                "Expected one chorus-flower upper bound, patched "
                        + patched[0] + "/" + patched[1]);
        LOGGER.info("Patched chorus-flower build-height bounds");
        return write(node);
    }

    private static byte[] transformPathNavigateGround(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getPathToPos", "func_179680_a",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/pathfinding/Path;");
        int patched = patchPathNavigateLowerBounds(method, node.name, 2);
        require(patched == 2, "Expected two ground-navigation lower bounds, patched " + patched);
        LOGGER.info("Patched ground-navigation lower build-height bounds");
        return write(node);
    }

    private static int patchPathNavigateLowerBounds(MethodNode method, String owner, int expected) {
        org.objectweb.asm.tree.FieldInsnNode worldField = findReferencedFieldInstruction(
                method, "Lnet/minecraft/world/World;");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)
                    && ("getY".equals(((MethodInsnNode) instruction).name)
                            || "func_177956_o".equals(((MethodInsnNode) instruction).name))) {
                AbstractInsnNode jump = nextReal(instruction);
                if (jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && (jump.getOpcode() == Opcodes.IFLE || jump.getOpcode() == Opcodes.IFLT)) {
                    int opcode = jump.getOpcode() == Opcodes.IFLE
                            ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPLT;
                    method.instructions.insertBefore(jump, list(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                                    worldField.owner, worldField.name, worldField.desc),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                    "(Lnet/minecraft/world/World;)I", false)));
                    ((org.objectweb.asm.tree.JumpInsnNode) jump).setOpcode(opcode);
                    patched++;
                    if (patched == expected) {
                        break;
                    }
                }
            }
            instruction = next;
        }
        return patched;
    }

    private static byte[] transformWalkNodeProcessor(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "getStart", "func_186318_b",
                "()Lnet/minecraft/pathfinding/PathPoint;");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)
                    && ("getY".equals(((MethodInsnNode) instruction).name)
                            || "func_177956_o".equals(((MethodInsnNode) instruction).name))) {
                AbstractInsnNode jump = nextReal(instruction);
                if (jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && jump.getOpcode() == Opcodes.IFLE) {
                    org.objectweb.asm.tree.FieldInsnNode entityField =
                            findReferencedFieldInstruction(method,
                                    "Lnet/minecraft/entity/EntityLiving;");
                    method.instructions.insertBefore(jump, list(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                                    entityField.owner, entityField.name, entityField.desc),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                    "(Lnet/minecraft/entity/Entity;)I", false)));
                    ((org.objectweb.asm.tree.JumpInsnNode) jump).setOpcode(Opcodes.IF_ICMPLE);
                    patched++;
                }
            }
            instruction = next;
        }
        require(patched == 1, "Expected one walk-node lower build-height bound, patched " + patched);
        LOGGER.info("Patched walk-node lower build-height bound");
        return write(node);
    }

    private static byte[] transformEntityLivingBase(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "attemptTeleport", "func_184595_k",
                "(DDD)Z");
        int patched = patchDynamicLowerBounds(method, 15, 8, 1);
        require(patched == 1, "Expected one entity-teleport lower build-height bound, patched " + patched);
        patchPowderSnowJump(node);
        LOGGER.info("Patched entity teleport lower build-height bound and powder-snow jump");
        return write(node);
    }

    private static void patchPowderSnowJump(ClassNode node) {
        MethodNode method = findMethod(node, "onLivingUpdate", "func_70636_d", "()V");
        AbstractInsnNode jumpSection = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode
                    && "jump".equals(((LdcInsnNode) instruction).cst)) {
                jumpSection = instruction;
                break;
            }
        }
        require(jumpSection != null, "Could not find EntityLivingBase jump profiler section");

        org.objectweb.asm.tree.FieldInsnNode jumpingField = null;
        for (AbstractInsnNode instruction = jumpSection.getNext(); instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode
                    && "travel".equals(((LdcInsnNode) instruction).cst)) {
                break;
            }
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                    && instruction.getOpcode() == Opcodes.GETFIELD
                    && "Z".equals(((org.objectweb.asm.tree.FieldInsnNode) instruction).desc)) {
                jumpingField = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                break;
            }
        }
        require(jumpingField != null, "Could not find EntityLivingBase jumping field");
        AbstractInsnNode entityLoad = previousReal(jumpingField);
        require(entityLoad instanceof VarInsnNode && entityLoad.getOpcode() == Opcodes.ALOAD
                        && ((VarInsnNode) entityLoad).var == 0,
                "Unexpected EntityLivingBase jumping-field load pattern");
        AbstractInsnNode falseJump = nextReal(jumpingField);
        require(falseJump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && falseJump.getOpcode() == Opcodes.IFEQ,
                "Unexpected EntityLivingBase jumping branch pattern");
        method.instructions.insert(falseJump, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "handlePowderSnowJump",
                        "(Lnet/minecraft/entity/EntityLivingBase;)V", false)));
    }

    private static byte[] transformShulker(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "attackEntityFrom", "func_70097_a",
                "(Lnet/minecraft/util/DamageSource;F)Z");
        org.objectweb.asm.tree.LabelNode modernBulletPath = new org.objectweb.asm.tree.LabelNode();
        int redirected = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || (instruction.getOpcode() != Opcodes.IFGE
                            && instruction.getOpcode() != Opcodes.IFNE)) {
                continue;
            }
            ((org.objectweb.asm.tree.JumpInsnNode) instruction).label = modernBulletPath;
            redirected++;
        }
        require(redirected == 2, "Expected two shulker teleport-skip branches, patched " + redirected);
        MethodNode teleport = findMethod(node, "tryTeleportToNewPosition", "func_184689_o", "()Z");
        int teleportPatched = patchEntityLowerBounds(teleport);
        require(teleportPatched == 1,
                "Expected one shulker teleport lower bound, patched " + teleportPatched);
        method.instructions.add(list(
                modernBulletPath,
                new FrameNode(Opcodes.F_SAME, 0, null, 0, null),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "handleShulkerBulletHit",
                        "(Lnet/minecraft/entity/monster/EntityShulker;"
                                + "Lnet/minecraft/util/DamageSource;)V",
                        false),
                new InsnNode(Opcodes.ICONST_1),
                new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched shulker-bullet duplication");
        return write(node);
    }

    private static int patchEntityLowerBounds(MethodNode method) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)
                    && ("getY".equals(((MethodInsnNode) instruction).name)
                            || "func_177956_o".equals(((MethodInsnNode) instruction).name))) {
                AbstractInsnNode jump = nextReal(instruction);
                if (jump instanceof org.objectweb.asm.tree.JumpInsnNode
                        && (jump.getOpcode() == Opcodes.IFLT || jump.getOpcode() == Opcodes.IFLE)) {
                    int opcode = jump.getOpcode() == Opcodes.IFLE
                            ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPLT;
                    method.instructions.insertBefore(jump, list(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                                    "(Lnet/minecraft/entity/Entity;)I", false)));
                    ((org.objectweb.asm.tree.JumpInsnNode) jump).setOpcode(opcode);
                    patched++;
                }
            }
            instruction = next;
        }
        return patched;
    }

    private static byte[] transformExperienceOrb(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode update = findMethod(node, "onUpdate", "func_70071_h_", "()V");
        int updatePatched = 0;
        for (AbstractInsnNode instruction = update.instructions.getFirst(); instruction != null;
             instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != Opcodes.INVOKESPECIAL
                    || !"net/minecraft/entity/Entity".equals(call.owner)
                    || !"()V".equals(call.desc)
                    || !("onUpdate".equals(call.name) || "func_70071_h_".equals(call.name))) {
                continue;
            }
            update.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "mergeNearbyExperienceOrbs",
                            "(Lnet/minecraft/entity/item/EntityXPOrb;)V", false)));
            updatePatched++;
        }

        MethodNode pickup = findMethod(node, "onCollideWithPlayer", "func_70100_b_",
                "(Lnet/minecraft/entity/player/EntityPlayer;)V");
        int pickupPatched = 0;
        for (AbstractInsnNode instruction = pickup.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("()V".equals(call.desc)
                        && ("setDead".equals(call.name) || "func_70106_y".equals(call.name))) {
                    pickup.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            GAMEPLAY_HOOKS, "finishExperienceOrbPickup",
                            "(Lnet/minecraft/entity/item/EntityXPOrb;)V", false));
                    pickupPatched++;
                }
            }
            instruction = next;
        }
        require(updatePatched == 1,
                "Expected one experience-orb update hook, patched " + updatePatched);
        require(pickupPatched == 1,
                "Expected one experience-orb pickup hook, patched " + pickupPatched);
        LOGGER.info("Patched experience-orb merging and counted pickup");
        return write(node);
    }

    private static byte[] transformHeightBoundedCommand(byte[] basicClass, int expectedBounds) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "execute", "func_184881_a",
                "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;[Ljava/lang/String;)V");
        int minPatched = 0;
        int maxPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof org.objectweb.asm.tree.JumpInsnNode
                    && instruction.getOpcode() == Opcodes.IFLT
                    && isCommandHeightValue(previousReal(instruction), true)) {
                org.objectweb.asm.tree.JumpInsnNode jump =
                        (org.objectweb.asm.tree.JumpInsnNode) instruction;
                method.instructions.insertBefore(instruction, list(
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minCommandY",
                                "(Lnet/minecraft/command/ICommandSender;)I", false),
                        new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IF_ICMPLT, jump.label)));
                method.instructions.remove(instruction);
                minPatched++;
            } else if (instruction instanceof IntInsnNode
                    && instruction.getOpcode() == Opcodes.SIPUSH
                    && ((IntInsnNode) instruction).operand == 256
                    && nextReal(instruction) instanceof org.objectweb.asm.tree.JumpInsnNode
                    && (nextReal(instruction).getOpcode() == Opcodes.IF_ICMPGE
                            || nextReal(instruction).getOpcode() == Opcodes.IF_ICMPLT)
                    && isCommandHeightValue(previousReal(instruction), false)) {
                method.instructions.insertBefore(instruction, list(
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxCommandY",
                                "(Lnet/minecraft/command/ICommandSender;)I", false)));
                method.instructions.remove(instruction);
                maxPatched++;
            }
            instruction = next;
        }
        require(minPatched == expectedBounds && maxPatched == expectedBounds,
                "Expected " + expectedBounds + " command min/max height pairs, patched "
                        + minPatched + "/" + maxPatched + " in " + node.name);
        LOGGER.info("Patched {} command height bounds", node.name);
        return write(node);
    }

    private static boolean isCommandHeightValue(AbstractInsnNode instruction, boolean minimum) {
        if (instruction instanceof MethodInsnNode) {
            MethodInsnNode method = (MethodInsnNode) instruction;
            return "net/minecraft/util/math/BlockPos".equals(method.owner)
                    && "()I".equals(method.desc)
                    && ("getY".equals(method.name) || "func_177956_o".equals(method.name));
        }
        if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
            org.objectweb.asm.tree.FieldInsnNode field =
                    (org.objectweb.asm.tree.FieldInsnNode) instruction;
            return "net/minecraft/world/gen/structure/StructureBoundingBox".equals(field.owner)
                    && "I".equals(field.desc)
                    && (minimum
                            ? "minY".equals(field.name) || "field_78895_b".equals(field.name)
                            : "maxY".equals(field.name) || "field_78894_e".equals(field.name));
        }
        return false;
    }

    private static byte[] transformWorld(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode checkLightFor = findMethod(node, "checkLightFor", "func_180500_c",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z");
        MethodNode method = findMethod(node, "isOutsideBuildHeight", "func_189509_E",
                "(Lnet/minecraft/util/math/BlockPos;)Z");
        replace(method, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isOutsideBuildHeight",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "isAreaLoaded", "func_175663_a", "(IIIIIIZ)Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new VarInsnNode(Opcodes.ILOAD, 4),
                new VarInsnNode(Opcodes.ILOAD, 5),
                new VarInsnNode(Opcodes.ILOAD, 6),
                new VarInsnNode(Opcodes.ILOAD, 7),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isAreaLoaded",
                        "(Lnet/minecraft/world/World;IIIIIIZ)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getLight", "func_175699_k",
                "(Lnet/minecraft/util/math/BlockPos;)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLight",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getLight", "func_175721_c",
                "(Lnet/minecraft/util/math/BlockPos;Z)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLight",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)I", false),
                new InsnNode(Opcodes.IRETURN)));
        MethodNode neighborLight = findMethodOptional(node, "getLightFromNeighborsFor", "func_175705_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (neighborLight != null) {
            replace(neighborLight, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLightFromNeighborsFor",
                            "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I", false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        replace(findMethod(node, "getLightFor", "func_175642_b",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ALOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldLightFor",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getHeight", "func_189649_b", "(II)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getWorldHeight",
                        "(Lnet/minecraft/world/World;II)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getChunksLowestHorizon", "func_82734_g", "(II)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getChunksLowestHorizon",
                        "(Lnet/minecraft/world/World;II)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getTopSolidOrLiquidBlock", "func_175672_r",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getTopSolidOrLiquidBlock",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", false),
                new InsnNode(Opcodes.ARETURN)));
        replace(findMethod(node, "canBlockFreezeBody", "canBlockFreezeBody",
                "(Lnet/minecraft/util/math/BlockPos;Z)Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "canBlockFreezeBody",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "canSnowAtBody", "canSnowAtBody",
                "(Lnet/minecraft/util/math/BlockPos;Z)Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "canSnowAtBody",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getHeight", "func_72800_K", "()I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYExclusive",
                        "(Lnet/minecraft/world/World;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getActualHeight", "func_72940_L", "()I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "maxYExclusive",
                        "(Lnet/minecraft/world/World;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        addPublicBridgeCopy(node, checkLightFor, "ffd$checkLightFor");
        LOGGER.info("Patched World build-height and light bounds");
        return write(node);
    }

    private static byte[] transformChunkCache(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String worldField = findField(node, "Lnet/minecraft/world/World;");
        String chunkArrayField = findField(node, "[[Lnet/minecraft/world/chunk/Chunk;");
        java.util.List<String> intFields = findFields(node, "I");
        require(intFields.size() == 2, "Expected two ChunkCache integer fields, found " + intFields.size());
        String chunkXField = intFields.get(0);
        String chunkZField = intFields.get(1);

        replace(findMethod(node, "getBlockState", "func_180495_p",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, worldField,
                        "Lnet/minecraft/world/World;"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, chunkXField, "I"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, chunkZField, "I"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, chunkArrayField,
                        "[[Lnet/minecraft/world/chunk/Chunk;"),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getChunkCacheBlockState",
                        "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", false),
                new InsnNode(Opcodes.ARETURN)));
        MethodNode lightForExt = findMethodOptional(node, "getLightForExt", "func_175629_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (lightForExt != null) {
            replace(lightForExt, list(
                    loadChunkCacheFields(node.name, worldField, chunkXField, chunkZField, chunkArrayField),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getChunkCacheLightForExt",
                            "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I", false),
                    new InsnNode(Opcodes.IRETURN)));
            addPublicBridgeCopy(node, lightForExt, "ffd$getLightForExt");
        }
        MethodNode lightFor = findMethodOptional(node, "getLightFor", "func_175628_b",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I");
        if (lightFor != null) {
            replace(lightFor, list(
                    loadChunkCacheFields(node.name, worldField, chunkXField, chunkZField, chunkArrayField),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getChunkCacheLightFor",
                            "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I", false),
                    new InsnNode(Opcodes.IRETURN)));
        }
        replace(findMethod(node, "isSideSolid", "isSideSolid",
                "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z"), list(
                loadChunkCacheFields(node.name, worldField, chunkXField, chunkZField, chunkArrayField),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ALOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isChunkCacheSideSolid",
                        "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        MethodNode isExtended = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "ffd$isExtended", "()Z", null, null);
        isExtended.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        isExtended.instructions.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                node.name, worldField, "Lnet/minecraft/world/World;"));
        isExtended.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                "isExtended", "(Lnet/minecraft/world/World;)Z", false));
        isExtended.instructions.add(new InsnNode(Opcodes.IRETURN));
        node.methods.add(isExtended);
        LOGGER.info("Patched ChunkCache for extended block and light access");
        return write(node);
    }

    private static byte[] transformPlayerChunkMapEntry(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String changedBlocks = findField(node, "[S");
        String chunkField = findField(node, "Lnet/minecraft/world/chunk/Chunk;");

        for (FieldNode field : node.fields) {
            if (changedBlocks.equals(field.name)) {
                field.desc = "[I";
            }
        }
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                    org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                    if (node.name.equals(field.owner) && changedBlocks.equals(field.name)) {
                        field.desc = "[I";
                    }
                } else if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.NEWARRAY
                        && ((IntInsnNode) instruction).operand == Opcodes.T_SHORT) {
                    ((IntInsnNode) instruction).operand = Opcodes.T_INT;
                } else if (instruction.getOpcode() == Opcodes.SALOAD) {
                    method.instructions.set(instruction, new InsnNode(Opcodes.IALOAD));
                } else if (instruction.getOpcode() == Opcodes.SASTORE) {
                    method.instructions.set(instruction, new InsnNode(Opcodes.IASTORE));
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ("java/util/Arrays".equals(call.owner) && "copyOf".equals(call.name)
                            && "([SI)[S".equals(call.desc)) {
                        call.desc = "([II)[I";
                    } else if ("net/minecraft/network/play/server/SPacketMultiBlockChange".equals(call.owner)
                            && "<init>".equals(call.name)
                            && "(I[SLnet/minecraft/world/chunk/Chunk;)V".equals(call.desc)) {
                        call.desc = "(I[ILnet/minecraft/world/chunk/Chunk;)V";
                    }
                }
                instruction = next;
            }
        }

        MethodNode blockChanged = findMethod(node, "blockChanged", "func_187265_a", "(III)V");
        patchChangedSectionFilter(blockChanged, node.name, chunkField);
        patchPackedBlockChange(blockChanged);

        MethodNode update = findMethod(node, "update", "func_187280_d", "()V");
        patchPackedYReads(update);
        LOGGER.info("Patched PlayerChunkMapEntry for signed-height block changes");
        return write(node);
    }

    private static byte[] transformWorldProvider(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String worldField = findField(node, "Lnet/minecraft/world/World;");
        patchProviderHeightReturn(findMethod(node, "getHeight", "getHeight", "()I"), node.name,
                worldField);
        patchProviderHeightReturn(findMethod(node, "getActualHeight", "getActualHeight", "()I"),
                node.name, worldField);
        MethodNode cloudHeight = findMethodOptional(node, "getCloudHeight", "func_76571_f", "()F");
        if (cloudHeight != null) {
            patchProviderCloudHeightReturn(cloudHeight, node.name, worldField);
        }
        LOGGER.info("Patched WorldProvider height and optional cloud-height reporting");
        return write(node);
    }

    private static byte[] transformWorldProviderCloudOnly(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String worldField = findField(node, "Lnet/minecraft/world/World;");
        MethodNode cloudHeight = findMethodOptional(node, "getCloudHeight", "func_76571_f", "()F");
        if (cloudHeight != null) {
            patchProviderCloudHeightReturn(cloudHeight, node.name, worldField);
        }
        LOGGER.info("Delegated WorldProvider height to CaveBiomesAPI and retained cloud-height configuration");
        return write(node);
    }

    private static void patchProviderHeightReturn(MethodNode method, String owner, String worldField) {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.IRETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                            "Lnet/minecraft/world/World;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "adjustProviderHeight",
                            "(ILnet/minecraft/world/World;)I", false)));
        }
    }

    private static void patchProviderCloudHeightReturn(MethodNode method, String owner,
            String worldField) {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.FRETURN) {
                continue;
            }
            method.instructions.insertBefore(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                            "Lnet/minecraft/world/World;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                            "adjustCloudHeight", "(FLnet/minecraft/world/World;)F", false)));
        }
    }

    private static byte[] transformWorldServer(byte[] basicClass) {
        ClassNode node = read(basicClass);
        replace(findMethod(node, "getPendingBlockUpdates", "func_72920_a",
                "(Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/List;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getPendingBlockUpdates",
                        "(Lnet/minecraft/world/WorldServer;Lnet/minecraft/world/chunk/Chunk;Z)Ljava/util/List;", false),
                new InsnNode(Opcodes.ARETURN)));
        patchWorldServerGameplay(node);
        LOGGER.info("Patched WorldServer pending ticks, sleeping percentage, and lightning behavior");
        return write(node);
    }

    private static byte[] transformWorldServerGameplayOnly(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchWorldServerGameplay(node);
        LOGGER.info("Delegated WorldServer height handling to CaveBiomesAPI and retained gameplay rules");
        return write(node);
    }

    private static void patchWorldServerGameplay(ClassNode node) {
        replace(findMethod(node, "areAllPlayersAsleep", "func_73056_e", "()Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "hasEnoughFullySleepingPlayers", "(Lnet/minecraft/world/WorldServer;)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "adjustPosToNearbyEntity", "func_175736_a",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new InsnNode(Opcodes.ICONST_M1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "findLightningTarget",
                         "(Lnet/minecraft/world/WorldServer;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/util/math/BlockPos;",
                         false),
                new InsnNode(Opcodes.ARETURN)));
        patchLightningRodSkeletonTrap(findMethod(node, "updateBlocks", "func_147456_g", "()V"));
    }

    private static void patchLightningRodSkeletonTrap(MethodNode method) {
        int strikePosLocal = -1;
        AbstractInsnNode skeletonHorseNew = null;
        org.objectweb.asm.tree.LabelNode normalLightningLabel = null;
        int lightningBoltNews = 0;

        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (("adjustPosToNearbyEntity".equals(call.name) || "func_175736_a".equals(call.name))
                        && "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"
                                .equals(call.desc)) {
                    AbstractInsnNode store = nextReal(instruction);
                    if (store instanceof VarInsnNode && store.getOpcode() == Opcodes.ASTORE) {
                        strikePosLocal = ((VarInsnNode) store).var;
                    }
                }
            }
            if (!(instruction instanceof org.objectweb.asm.tree.TypeInsnNode)
                    || instruction.getOpcode() != Opcodes.NEW) {
                continue;
            }
            String desc = ((org.objectweb.asm.tree.TypeInsnNode) instruction).desc;
            if ("net/minecraft/entity/passive/EntitySkeletonHorse".equals(desc)) {
                skeletonHorseNew = instruction;
            } else if (skeletonHorseNew != null
                    && "net/minecraft/entity/effect/EntityLightningBolt".equals(desc)
                    && ++lightningBoltNews == 2) {
                AbstractInsnNode cursor = instruction.getPrevious();
                while (cursor != null && !(cursor instanceof org.objectweb.asm.tree.LabelNode)) {
                    cursor = cursor.getPrevious();
                }
                normalLightningLabel = (org.objectweb.asm.tree.LabelNode) cursor;
                break;
            }
        }

        if (strikePosLocal < 0 || skeletonHorseNew == null || normalLightningLabel == null) {
            throw new IllegalStateException("Missing WorldServer lightning skeleton-trap pattern");
        }
        method.instructions.insertBefore(skeletonHorseNew, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, strikePosLocal),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "isLightningRodStrikeTarget",
                        "(Lnet/minecraft/world/WorldServer;Lnet/minecraft/util/math/BlockPos;)Z", false),
                new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IFNE, normalLightningLabel)));
    }

    private static byte[] transformWorldEntitySpawner(byte[] basicClass) {
        ClassNode node = read(basicClass);
        replace(findMethod(node, "getRandomChunkPosition", "func_180621_a",
                "(Lnet/minecraft/world/World;II)Lnet/minecraft/util/math/BlockPos;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getRandomChunkPosition",
                        "(Lnet/minecraft/world/World;II)Lnet/minecraft/util/math/BlockPos;", false),
                new InsnNode(Opcodes.ARETURN)));
        LOGGER.info("Patched natural-spawn vertical range");
        return write(node);
    }

    private static byte[] transformEmptyChunk(byte[] basicClass) {
        ClassNode node = read(basicClass);
        replace(findMethod(node, "getHeightValue", "func_76611_b", "(II)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                        "(Lnet/minecraft/world/chunk/Chunk;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        LOGGER.info("Patched EmptyChunk height floor");
        return write(node);
    }

    private static byte[] transformEntity(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchPowderSnowMovement(node);
        patchEntityDoubleConstant(findMethod(node, "onEntityUpdate", "func_70030_z", "()V"),
                -64.0D, "outOfWorldThreshold");
        MethodNode prepareSpawn = findMethodOptional(node, "preparePlayerToSpawn", "func_70065_x", "()V");
        if (prepareSpawn != null) {
            replace(prepareSpawn, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "prepareEntityToSpawn",
                            "(Lnet/minecraft/entity/Entity;)V", false),
                    new InsnNode(Opcodes.RETURN)));
        }
        LOGGER.info("Patched Entity void threshold and powder-snow movement");
        return write(node);
    }

    private static byte[] transformEntityGameplayOnly(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchPowderSnowMovement(node);
        LOGGER.info("Patched Entity powder-snow movement while external height core owns bounds");
        return write(node);
    }

    private static void patchPowderSnowMovement(ClassNode node) {
        MethodNode move = findMethod(node, "move", "func_70091_d",
                "(Lnet/minecraft/entity/MoverType;DDD)V");
        int multiplierLocal = Math.max(move.maxLocals, 8);
        move.maxLocals = multiplierLocal + 2;
        move.instructions.insertBefore(move.instructions.getFirst(), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "powderSnowHorizontalMovementMultiplier",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/MoverType;)D", false),
                new VarInsnNode(Opcodes.DSTORE, multiplierLocal),
                new VarInsnNode(Opcodes.DLOAD, 2),
                new VarInsnNode(Opcodes.DLOAD, multiplierLocal),
                new InsnNode(Opcodes.DMUL),
                new VarInsnNode(Opcodes.DSTORE, 2),
                new VarInsnNode(Opcodes.DLOAD, 4),
                new LdcInsnNode(6.0D),
                new LdcInsnNode(5.0D),
                new VarInsnNode(Opcodes.DLOAD, multiplierLocal),
                new InsnNode(Opcodes.DMUL),
                new InsnNode(Opcodes.DSUB),
                new InsnNode(Opcodes.DMUL),
                new VarInsnNode(Opcodes.DSTORE, 4),
                new VarInsnNode(Opcodes.DLOAD, 6),
                new VarInsnNode(Opcodes.DLOAD, multiplierLocal),
                new InsnNode(Opcodes.DMUL),
                new VarInsnNode(Opcodes.DSTORE, 6),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.DLOAD, multiplierLocal),
                new MethodInsnNode(Opcodes.INVOKESTATIC, GAMEPLAY_HOOKS,
                        "finishPowderSnowMovement",
                        "(Lnet/minecraft/entity/Entity;D)V", false)));
    }

    private static byte[] transformMinecart(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchEntityDoubleConstant(findMethod(node, "onUpdate", "func_70071_h_", "()V"),
                -64.0D, "outOfWorldThreshold");
        LOGGER.info("Patched minecart void threshold");
        return write(node);
    }

    private static byte[] transformFallingBlock(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "onUpdate", "func_70071_h_", "()V");
        int patchedMin = 0;
        int patchedMax = 0;
        java.util.List<MethodInsnNode> yCalls = new java.util.ArrayList<>();
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode
                    && "net/minecraft/util/math/BlockPos".equals(((MethodInsnNode) instruction).owner)
                    && "()I".equals(((MethodInsnNode) instruction).desc)) {
                yCalls.add((MethodInsnNode) instruction);
            }
        }
        for (MethodInsnNode yCall : yCalls) {
            AbstractInsnNode constant = nextReal(yCall);
            if (constant != null && constant.getOpcode() == Opcodes.ICONST_1) {
                method.instructions.insertBefore(constant, list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "fallingBlockMinY",
                                "(Lnet/minecraft/entity/Entity;)I", false)));
                method.instructions.remove(constant);
                patchedMin++;
            } else if (constant instanceof IntInsnNode && constant.getOpcode() == Opcodes.SIPUSH
                    && ((IntInsnNode) constant).operand == 256) {
                method.instructions.insertBefore(constant, list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "fallingBlockMaxY",
                                "(Lnet/minecraft/entity/Entity;)I", false)));
                method.instructions.remove(constant);
                patchedMax++;
            }
        }
        require(patchedMin == 1 && patchedMax == 1,
                "Expected falling-block min/max bounds, patched " + patchedMin + "/" + patchedMax);
        LOGGER.info("Patched falling-block vertical lifetime bounds");
        return write(node);
    }

    private static byte[] transformPlayerMP(byte[] basicClass) {
        ClassNode node = read(basicClass);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if ("<init>".equals(method.name)) {
                patched += patchLoadedEntityDoubleConstant(method, 255.0D, "playerSpawnCeiling");
            }
        }
        require(patched == 1, "Expected one player spawn ceiling, patched " + patched);
        LOGGER.info("Patched player spawn collision ceiling");
        return write(node);
    }

    private static byte[] transformPlayerList(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode recreate = findMethod(node, "recreatePlayerEntity", "func_72368_a",
                "(Lnet/minecraft/entity/player/EntityPlayerMP;IZ)Lnet/minecraft/entity/player/EntityPlayerMP;");
        int patched = patchLoadedEntityDoubleConstant(recreate, 256.0D, "playerTransferCeiling");
        require(patched == 1, "Expected one player transfer ceiling, patched " + patched);
        LOGGER.info("Patched player transfer collision ceiling");
        return write(node);
    }

    private static byte[] transformTeleporter(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String worldField = findField(node, "Lnet/minecraft/world/WorldServer;");
        MethodNode placeExisting = findMethod(node, "placeInExistingPortal", "func_180620_b",
                "(Lnet/minecraft/entity/Entity;F)Z");
        patchBlockPosLowerBound(placeExisting, node.name, worldField);

        MethodNode makePortal = findMethod(node, "makePortal", "func_85188_a",
                "(Lnet/minecraft/entity/Entity;)Z");
        patchPortalVerticalSearch(makePortal, node.name, worldField);
        int existingHeightCalls = patchPortalSearchHeight(placeExisting);
        int newPortalHeightCalls = patchPortalSearchHeight(makePortal);
        require(existingHeightCalls == 1 && newPortalHeightCalls == 3,
                "Unexpected portal height calls: existing=" + existingHeightCalls
                        + ", new=" + newPortalHeightCalls);
        LOGGER.info("Patched portal search for extended lower and dimension-specific upper height");
        return write(node);
    }

    private static int patchPortalSearchHeight(MethodNode method) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (("getActualHeight".equals(call.name) || "func_72940_L".equals(call.name))
                        && "()I".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            HOOKS, "portalSearchHeight", "(Lnet/minecraft/world/World;)I", false));
                    patched++;
                }
            }
            instruction = next;
        }
        return patched;
    }

    private static void patchEntityDoubleConstant(MethodNode method, double value, String hookName) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof LdcInsnNode
                    && Double.valueOf(value).equals(((LdcInsnNode) instruction).cst)) {
                method.instructions.insert(instruction, list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hookName,
                                "(DLnet/minecraft/entity/Entity;)D", false)));
                patched++;
            }
            instruction = next;
        }
        require(patched == 1, "Expected one " + hookName + " constant, patched " + patched);
    }

    private static int patchLoadedEntityDoubleConstant(MethodNode method, double value, String hookName) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof LdcInsnNode
                    && Double.valueOf(value).equals(((LdcInsnNode) instruction).cst)) {
                AbstractInsnNode field = previousReal(instruction);
                AbstractInsnNode load = previousReal(field);
                require(field instanceof org.objectweb.asm.tree.FieldInsnNode
                                && field.getOpcode() == Opcodes.GETFIELD
                                && load instanceof VarInsnNode && load.getOpcode() == Opcodes.ALOAD,
                        "Missing entity load before " + hookName);
                method.instructions.insertBefore(instruction, list(
                        new VarInsnNode(Opcodes.ALOAD, ((VarInsnNode) load).var),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, hookName,
                                "(Lnet/minecraft/entity/Entity;)D", false)));
                method.instructions.remove(instruction);
                patched++;
            }
            instruction = next;
        }
        return patched;
    }

    private static byte[] transformMultiBlockPacket(byte[] basicClass) {
        ClassNode node = read(basicClass);
        replaceFrameType(node, "[S", "[I");
        MethodNode constructor = findMethod(node, "<init>", "<init>",
                "(I[SLnet/minecraft/world/chunk/Chunk;)V");
        constructor.desc = "(I[ILnet/minecraft/world/chunk/Chunk;)V";

        MethodInsnNode readVarInt = null;
        MethodInsnNode writeVarInt = null;
        MethodNode read = findMethod(node, "readPacketData", "func_148837_a",
                "(Lnet/minecraft/network/PacketBuffer;)V");
        MethodNode write = findMethod(node, "writePacketData", "func_148840_b",
                "(Lnet/minecraft/network/PacketBuffer;)V");
        for (AbstractInsnNode instruction = read.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("net/minecraft/network/PacketBuffer".equals(call.owner)
                        && ("readVarInt".equals(call.name) || "func_150792_a".equals(call.name))) {
                    readVarInt = call;
                    break;
                }
            }
        }
        for (AbstractInsnNode instruction = write.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("net/minecraft/network/PacketBuffer".equals(call.owner)
                        && ("writeVarInt".equals(call.name) || "func_150787_b".equals(call.name))) {
                    writeVarInt = call;
                    break;
                }
            }
        }
        require(readVarInt != null, "Missing PacketBuffer.readVarInt call");
        require(writeVarInt != null, "Missing PacketBuffer.writeVarInt call");

        int arrayLoads = 0;
        int shortReads = 0;
        int shortWrites = 0;
        int constructorCalls = 0;
        int offsetCalls = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction.getOpcode() == Opcodes.SALOAD) {
                    method.instructions.set(instruction, new InsnNode(Opcodes.IALOAD));
                    arrayLoads++;
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ("net/minecraft/network/PacketBuffer".equals(call.owner) && "()S".equals(call.desc)) {
                        call.name = readVarInt.name;
                        call.desc = readVarInt.desc;
                        shortReads++;
                    } else if ("net/minecraft/network/PacketBuffer".equals(call.owner)
                            && "(I)Lio/netty/buffer/ByteBuf;".equals(call.desc)
                            && ("writeShort".equals(call.name) || "writeShort".equals(call.name))) {
                        call.name = writeVarInt.name;
                        call.desc = writeVarInt.desc;
                        shortWrites++;
                    }
                    if ("net/minecraft/network/play/server/SPacketMultiBlockChange$BlockUpdateData".equals(call.owner)
                            && "<init>".equals(call.name) && call.desc.contains(";S")) {
                        call.desc = call.desc.replace(";S", ";I");
                        constructorCalls++;
                    } else if ("net/minecraft/network/play/server/SPacketMultiBlockChange$BlockUpdateData".equals(call.owner)
                            && "()S".equals(call.desc)) {
                        call.desc = "()I";
                        offsetCalls++;
                    }
                }
                instruction = next;
            }
        }
        require(arrayLoads == 1, "Expected one multi-block short array load, patched " + arrayLoads);
        require(shortReads == 1, "Expected one multi-block short read, patched " + shortReads);
        require(shortWrites == 1, "Expected one multi-block short write, patched " + shortWrites);
        require(constructorCalls == 2, "Expected two multi-block data constructors, patched " + constructorCalls);
        require(offsetCalls == 1, "Expected one multi-block offset getter, patched " + offsetCalls);
        LOGGER.info("Patched multi-block packet to VarInt offsets");
        return write(node);
    }

    private static byte[] transformMultiBlockData(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String offsetField = findField(node, "S");
        for (FieldNode field : node.fields) {
            if (offsetField.equals(field.name)) {
                field.desc = "I";
            }
        }
        int constructors = 0;
        for (MethodNode method : node.methods) {
            if ("<init>".equals(method.name) && method.desc.contains(";S")) {
                method.desc = method.desc.replace(";S", ";I");
                constructors++;
            } else if (("getOffset".equals(method.name) || "func_180089_b".equals(method.name))
                    && "()S".equals(method.desc)) {
                method.desc = "()I";
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                    org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                    if (node.name.equals(field.owner) && offsetField.equals(field.name)) {
                        field.desc = "I";
                    }
                }
            }
        }
        require(constructors == 2, "Expected two BlockUpdateData constructors, patched " + constructors);

        MethodNode getPos = findMethod(node, "getPos", "func_180090_a",
                "()Lnet/minecraft/util/math/BlockPos;");
        int offsetReads = 0;
        int yReads = 0;
        for (AbstractInsnNode instruction = getPos.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = nextReal(instruction);
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                if (node.name.equals(field.owner) && offsetField.equals(field.name)) {
                    offsetReads++;
                    if (offsetReads == 2 && next instanceof IntInsnNode
                            && next.getOpcode() == Opcodes.SIPUSH
                            && ((IntInsnNode) next).operand == 255
                            && nextReal(next) != null && nextReal(next).getOpcode() == Opcodes.IAND) {
                        AbstractInsnNode and = nextReal(next);
                        AbstractInsnNode after = and.getNext();
                        getPos.instructions.insert(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                                "unpackLocalBlockY", "(I)I", false));
                        getPos.instructions.remove(and);
                        getPos.instructions.remove(next);
                        yReads++;
                        instruction = after;
                        continue;
                    }
                }
            }
            instruction = instruction.getNext();
        }
        require(yReads == 1, "Expected one BlockUpdateData Y decode, patched " + yReads);
        LOGGER.info("Patched BlockUpdateData for signed 12-bit Y");
        return write(node);
    }

    private static void patchChangedSectionFilter(MethodNode method, String owner, String chunkField) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode shift = instruction.getNext();
            AbstractInsnNode after = shift == null ? null : shift.getNext();
            if (instruction.getOpcode() == Opcodes.ICONST_4 && shift != null
                    && shift.getOpcode() == Opcodes.ISHR && after != null
                    && after.getOpcode() == Opcodes.ISHL) {
                InsnList replacement = list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, chunkField,
                                "Lnet/minecraft/world/chunk/Chunk;"),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForChunk",
                                "(ILnet/minecraft/world/chunk/Chunk;)I", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(shift);
                method.instructions.remove(instruction);
                patched++;
                instruction = after;
                continue;
            }
            instruction = instruction.getNext();
        }
        require(patched == 1, "Expected one changed-section mapping, patched " + patched);
    }

    private static void patchPackedBlockChange(MethodNode method) {
        AbstractInsnNode end = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.I2S) {
                end = instruction;
                break;
            }
        }
        require(end != null, "Missing packed block-change I2S");
        AbstractInsnNode start = end;
        for (int i = 0; i < 20 && start != null; i++) {
            if (start instanceof VarInsnNode && start.getOpcode() == Opcodes.ILOAD
                    && ((VarInsnNode) start).var == 1) {
                break;
            }
            start = start.getPrevious();
        }
        require(start != null && start.getOpcode() == Opcodes.ILOAD,
                "Missing packed block-change expression start");
        method.instructions.insertBefore(start, list(
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "packLocalBlockChange", "(III)I", false)));
        AbstractInsnNode cursor = start;
        while (cursor != null) {
            AbstractInsnNode next = cursor.getNext();
            method.instructions.remove(cursor);
            if (cursor == end) {
                break;
            }
            cursor = next;
        }
    }

    private static void patchPackedYReads(MethodNode method) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode mask = nextReal(instruction);
            AbstractInsnNode and = nextReal(mask);
            if (instruction.getOpcode() == Opcodes.IALOAD && mask instanceof IntInsnNode
                    && mask.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) mask).operand == 255
                    && and != null && and.getOpcode() == Opcodes.IAND) {
                method.instructions.insert(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS,
                        "unpackLocalBlockY", "(I)I", false));
                AbstractInsnNode after = and.getNext();
                method.instructions.remove(and);
                method.instructions.remove(mask);
                patched++;
                instruction = after;
                continue;
            }
            instruction = instruction.getNext();
        }
        require(patched == 2, "Expected two PlayerChunkMapEntry Y decodes, patched " + patched);
    }

    private static byte[] transformChunk(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchChunkConstructor(node);
        MethodNode precipitationHeight = findMethod(node, "getPrecipitationHeight", "func_177440_h",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;");
        String precipitationField = findReferencedField(precipitationHeight, node.name, "[I");
        MethodNode resetRelight = findMethod(node, "resetRelightChecks", "func_76613_n", "()V");
        String queuedLightChecksField = findReferencedField(resetRelight, node.name, "I");
        patchInitialRelightCheckCount(node, queuedLightChecksField);
        replace(findMethod(node, "getTopFilledSegment", "func_76625_h", "()I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getTopFilledSegment",
                        "(Lnet/minecraft/world/chunk/Chunk;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        MethodNode clientHeightMap = findMethodOptional(node, "generateHeightMap", "func_76590_a", "()V");
        if (clientHeightMap != null) {
            replace(clientHeightMap, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, precipitationField, "[I"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "generateHeightMap",
                            "(Lnet/minecraft/world/chunk/Chunk;[I)V", false),
                    new InsnNode(Opcodes.RETURN)));
        }
        replace(findMethod(node, "generateSkylightMap", "func_76603_b", "()V"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, precipitationField, "[I"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "generateSkylightMap",
                        "(Lnet/minecraft/world/chunk/Chunk;[I)V", false),
                new InsnNode(Opcodes.RETURN)));
        replace(findMethod(node, "relightBlock", "func_76615_h", "(III)V"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "relightBlock",
                        "(Lnet/minecraft/world/chunk/Chunk;III)V", false),
                new InsnNode(Opcodes.RETURN)));
        replace(findMethod(node, "getBlockState", "func_186032_a",
                "(III)Lnet/minecraft/block/state/IBlockState;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getBlockState",
                        "(Lnet/minecraft/world/chunk/Chunk;III)Lnet/minecraft/block/state/IBlockState;", false),
                new InsnNode(Opcodes.ARETURN)));
        replace(findMethod(node, "getLightFor", "func_177413_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ALOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightFor",
                        "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "setLightFor", "func_177431_a",
                "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;I)V"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ALOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "setLightFor",
                        "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;I)V", false),
                new InsnNode(Opcodes.RETURN)));
        replace(findMethod(node, "getLightSubtracted", "func_177443_a",
                "(Lnet/minecraft/util/math/BlockPos;I)I"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getLightSubtracted",
                        "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;I)I", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "getLastExtendedBlockStorage", "func_186031_y",
                "()Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getTopStorage",
                        "(Lnet/minecraft/world/chunk/Chunk;)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", false),
                new InsnNode(Opcodes.ARETURN)));
        replace(precipitationHeight, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, precipitationField, "[I"),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getPrecipitationHeight",
                        "(Lnet/minecraft/world/chunk/Chunk;[ILnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", false),
                new InsnNode(Opcodes.ARETURN)));
        replace(findMethod(node, "isEmptyBetween", "func_76606_c", "(II)Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isEmptyBetween",
                        "(Lnet/minecraft/world/chunk/Chunk;II)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        replace(findMethod(node, "enqueueRelightChecks", "func_76594_o", "()V"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, queuedLightChecksField, "I"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "enqueueRelightChecks",
                        "(Lnet/minecraft/world/chunk/Chunk;I)I", false),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.PUTFIELD, node.name, queuedLightChecksField, "I"),
                new InsnNode(Opcodes.RETURN)));
        replace(findMethod(node, "checkLight", "func_150811_f", "(II)Z"), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "checkChunkColumnLight",
                        "(Lnet/minecraft/world/chunk/Chunk;II)Z", false),
                new InsnNode(Opcodes.IRETURN)));
        patchChunkEntitySections(node);
        patchChunkPacketRead(node);
        patchStorageCoordinateIndices(node);
        patchLegacyWorldGeneratorHeight(node);
        addPublicBridgeCopy(node, findMethod(node, "checkLight", "func_150809_p", "()V"),
                "ffd$checkLight");
        addPublicBridgeCopy(node, findMethod(node, "recheckGaps", "func_150803_c", "(Z)V"),
                "ffd$recheckGaps");
        LOGGER.info("Patched Chunk section storage, height maps, precipitation, relighting and legacy worldgen height");
        return write(node);
    }

    private static byte[] transformChunkLegacyWorldgenOnly(byte[] basicClass) {
        ClassNode node = read(basicClass);
        patchLegacyWorldGeneratorHeight(node);
        LOGGER.info("Delegating chunk height storage to Depths Update while retaining the 0..255 Forge worldgen bridge");
        return write(node);
    }

    private static void patchLegacyWorldGeneratorHeight(ClassNode node) {
        String descriptor = "(IILnet/minecraft/world/World;Lnet/minecraft/world/gen/IChunkGenerator;"
                + "Lnet/minecraft/world/chunk/IChunkProvider;)V";
        int patched = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() != Opcodes.INVOKESTATIC
                        || !"net/minecraftforge/fml/common/registry/GameRegistry".equals(call.owner)
                        || !"generateWorld".equals(call.name) || !descriptor.equals(call.desc)) {
                    continue;
                }
                call.owner = HOOKS;
                call.name = "generateLegacyWorld";
                call.itf = false;
                patched++;
            }
        }
        require(patched == 1, "Expected one Forge world-generator call in Chunk, patched " + patched);
    }

    private static byte[] transformViewFrustum(byte[] basicClass) {
        ClassNode node = read(basicClass);
        String worldField = findField(node, "Lnet/minecraft/world/World;");
        patchViewFrustumCount(node, worldField);
        patchViewFrustumYPositions(node, worldField);
        patchViewFrustumSectionLookup(node, worldField);
        addViewFrustumAccessBridge(node);
        LOGGER.info("Patched ViewFrustum for 24 render sections");
        return write(node);
    }

    private static void addViewFrustumAccessBridge(ClassNode node) {
        if (!node.interfaces.contains(VIEW_FRUSTUM_ACCESS)) {
            node.interfaces.add(VIEW_FRUSTUM_ACCESS);
        }
        MethodNode target = findMethod(node, "getRenderChunk", "func_178161_a",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/client/renderer/chunk/RenderChunk;");
        MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "ffd$getRenderChunk",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/client/renderer/chunk/RenderChunk;",
                null, null);
        bridge.instructions.add(list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 1),
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, target.name, target.desc, false),
                new InsnNode(Opcodes.ARETURN)));
        node.methods.add(bridge);
    }

    private static byte[] transformRenderGlobal(byte[] basicClass) {
        ClassNode node = read(basicClass);
        addRenderGlobalAccessBridge(node);
        MethodNode renderEntities = findMethod(node, "renderEntities", "func_180446_a",
                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V");
        patchRenderGlobalEntitySection(renderEntities);

        MethodNode getOffset = findMethodOptional(node, "getRenderChunkOffset", "func_181562_a",
                "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/client/renderer/chunk/RenderChunk;");
        String worldField = findField(node, "Lnet/minecraft/client/multiplayer/WorldClient;");
        String viewFrustumField = findField(node, "Lnet/minecraft/client/renderer/ViewFrustum;");
        if (getOffset != null) {
            String renderDistanceField = findReferencedField(getOffset, node.name, "I");
            replace(getOffset, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, worldField,
                            "Lnet/minecraft/client/multiplayer/WorldClient;"),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, viewFrustumField,
                            "Lnet/minecraft/client/renderer/ViewFrustum;"),
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name,
                            renderDistanceField, "I"),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, "getRenderChunkOffset",
                            "(Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/ViewFrustum;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/client/renderer/chunk/RenderChunk;", false),
                    new InsnNode(Opcodes.ARETURN)));
        } else {
            MethodNode optifineOffset = findMethodOptional(node, "getRenderChunkOffset", null,
                    "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/util/EnumFacing;ZI)Lnet/minecraft/client/renderer/chunk/RenderChunk;");
            require(optifineOffset != null,
                    "Missing vanilla and OptiFine RenderGlobal getRenderChunkOffset methods");
            LOGGER.info("Preserving OptiFine RenderGlobal neighbor lookup; its height ceiling is patched in setupTerrain");
        }
        MethodNode markRange = findMethod(node, "markBlockRangeForRenderUpdate", "func_147585_a",
                "(IIIIII)V");
        replace(markRange, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, worldField,
                        "Lnet/minecraft/client/multiplayer/WorldClient;"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, viewFrustumField,
                        "Lnet/minecraft/client/renderer/ViewFrustum;"),
                new VarInsnNode(Opcodes.ILOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new VarInsnNode(Opcodes.ILOAD, 3),
                new VarInsnNode(Opcodes.ILOAD, 4),
                new VarInsnNode(Opcodes.ILOAD, 5),
                new VarInsnNode(Opcodes.ILOAD, 6),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, "markBlockRangeForRenderUpdate",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/RenderGlobal;Lnet/minecraft/client/renderer/ViewFrustum;IIIIII)V", false),
                new InsnNode(Opcodes.RETURN)));
        patchRenderGlobalEntityHeightBounds(renderEntities, node.name, worldField);
        patchRenderGlobalFallbackLayers(node, worldField);
        patchWorldBorderHeight(node, worldField);
        LOGGER.info("Patched RenderGlobal entity sections, vertical traversal, chunk invalidation and world border height");
        return write(node);
    }

    private static void addRenderGlobalAccessBridge(ClassNode node) {
        if (!node.interfaces.contains(RENDER_GLOBAL_ACCESS)) {
            node.interfaces.add(RENDER_GLOBAL_ACCESS);
        }
        MethodNode target = findMethod(node, "markBlocksForUpdate", "func_184385_a",
                "(IIIIIIZ)V");
        require(findMethodOptional(node, "ffd$markBlocksForUpdate", "ffd$markBlocksForUpdate",
                        target.desc) == null,
                "Duplicate RenderGlobal block-update bridge");
        MethodNode bridge = new MethodNode(Opcodes.ASM5,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "ffd$markBlocksForUpdate", target.desc, null, null);
        bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        for (int local = 1; local <= 7; local++) {
            bridge.instructions.add(new VarInsnNode(Opcodes.ILOAD, local));
        }
        bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                node.name, target.name, target.desc, false));
        bridge.instructions.add(new InsnNode(Opcodes.RETURN));
        node.methods.add(bridge);
    }

    private static byte[] transformMinecraftClient(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode launchIntegratedServer = findMethod(node, "launchIntegratedServer", "func_71371_a",
                "(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V");
        launchIntegratedServer.instructions.insertBefore(launchIntegratedServer.instructions.getFirst(), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, "beginIntegratedWorldLoad",
                        "(Lnet/minecraft/client/Minecraft;)V", false)));
        int returns = 0;
        for (AbstractInsnNode instruction = launchIntegratedServer.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == Opcodes.RETURN) {
                launchIntegratedServer.instructions.insertBefore(instruction, list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, "endIntegratedWorldLoad",
                                "(Lnet/minecraft/client/Minecraft;)V", false)));
                returns++;
            }
            instruction = next;
        }
        require(returns > 0, "Missing Minecraft integrated-world load return");
        LOGGER.info("Scoped loading renderer to integrated-world loading");
        return write(node);
    }

    private static byte[] transformOptiFineRenderChunkUtils(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode countBlocks = null;
        for (MethodNode method : node.methods) {
            if ("getCountBlocks".equals(method.name) && method.desc.endsWith(")I")) {
                require(countBlocks == null, "Multiple OptiFine RenderChunkUtils getCountBlocks methods");
                countBlocks = method;
            }
        }
        require(countBlocks != null, "Missing OptiFine RenderChunkUtils getCountBlocks method");
        int found = 0;
        int patched = 0;
        for (AbstractInsnNode instruction = countBlocks.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.ISHR) {
                continue;
            }
            AbstractInsnNode four = previousReal(instruction);
            AbstractInsnNode getter = previousReal(four);
            if (!isPushFour(four) || !isBlockPosCoordinateGetter(getter)) {
                continue;
            }
            found++;
            if (found == 1) {
                countBlocks.instructions.insert(instruction, list(
                        new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                                "storageIndexForClientSectionY", "(I)I", false)));
                patched++;
            }
        }
        require(found == 1 && patched == 1,
                "Expected one OptiFine RenderChunkUtils section lookup, found " + found
                        + ", patched " + patched);
        LOGGER.info("Patched OptiFine RenderChunkUtils section lookup");
        return write(node);
    }

    private static byte[] transformOptiFineShaderVertexBuilder(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode pushEntity = null;
        for (MethodNode method : node.methods) {
            if (!"pushEntity".equals(method.name) || (method.access & Opcodes.ACC_STATIC) == 0
                    || !isOptiFineShaderEntityPushDescriptor(method.desc)) {
                continue;
            }
            require(pushEntity == null, "Multiple OptiFine shader entity-state methods");
            pushEntity = method;
        }
        require(pushEntity != null, "Missing OptiFine shader entity-state method");
        MethodInsnNode blockAliasCall = null;
        for (AbstractInsnNode instruction = pushEntity.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKESTATIC
                    && "net/optifine/shaders/BlockAliases".equals(call.owner)
                    && "(II)I".equals(call.desc)
                    && ("getBlockAliasId".equals(call.name)
                            || "getMappedBlockId".equals(call.name))) {
                require(blockAliasCall == null, "Multiple OptiFine shader block-alias lookups");
                blockAliasCall = call;
            }
        }
        require(blockAliasCall != null, "Missing OptiFine shader block-alias lookup");
        pushEntity.instructions.insertBefore(pushEntity.instructions.getFirst(), list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                        "shaderWaterloggedState",
                        "(Lnet/minecraft/block/state/IBlockState;)"
                                + "Lnet/minecraft/block/state/IBlockState;", false),
                new VarInsnNode(Opcodes.ASTORE, 0)));
        pushEntity.instructions.insert(blockAliasCall, list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new InsnNode(Opcodes.SWAP),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                        "shaderBlockAlias",
                        "(Lnet/minecraft/block/state/IBlockState;I)I", false)));
        LOGGER.info("Patched OptiFine shader material identity and modded block aliases");
        return write(node);
    }

    private static boolean isOptiFineShaderEntityPushDescriptor(String desc) {
        return "(Lawt;Let;Lamy;Lbuk;)V".equals(desc)
                || ("(Lnet/minecraft/block/state/IBlockState;"
                        + "Lnet/minecraft/util/math/BlockPos;"
                        + "Lnet/minecraft/world/IBlockAccess;"
                        + "Lnet/minecraft/client/renderer/BufferBuilder;)V").equals(desc);
    }

    private static byte[] transformHwylaBlockHud(byte[] basicClass) {
        ClassNode node = read(basicClass);
        require("mcp/mobius/waila/addons/core/HUDHandlerBlocks".equals(node.name),
                "Unexpected HWYLA block HUD class");
        String descriptor = "(Lnet/minecraft/item/ItemStack;Ljava/util/List;"
                + "Lmcp/mobius/waila/api/IWailaDataAccessor;"
                + "Lmcp/mobius/waila/api/IWailaConfigHandler;)Ljava/util/List;";
        patchHwylaLiquidCheck(findMethod(node, "getWailaHead", "getWailaHead", descriptor));
        patchHwylaLiquidCheck(findMethod(node, "getWailaTail", "getWailaTail", descriptor));
        LOGGER.info("Patched HWYLA labels for FFD waterlogged blocks");
        return write(node);
    }

    private static void patchHwylaLiquidCheck(MethodNode method) {
        MethodInsnNode isLiquid = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && "net/minecraft/block/material/Material".equals(call.owner)
                    && "()Z".equals(call.desc)
                    && ("isLiquid".equals(call.name) || "func_76224_d".equals(call.name))) {
                require(isLiquid == null, "Multiple HWYLA liquid checks in " + method.name);
                isLiquid = call;
            }
        }
        require(isLiquid != null, "Missing HWYLA liquid check in " + method.name);
        method.instructions.insert(isLiquid, list(
                new VarInsnNode(Opcodes.ALOAD, 3),
                new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                        "mcp/mobius/waila/api/IWailaDataAccessor", "getBlockState",
                        "()Lnet/minecraft/block/state/IBlockState;", true),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS,
                        "hwylaTreatAsLiquid",
                        "(ZLnet/minecraft/block/state/IBlockState;)Z", false)));
    }

    private static byte[] transformRenderChunk(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode preRender = findMethodOptional(node, "preRenderBlocks", "func_178573_a",
                "(Lnet/minecraft/client/renderer/BufferBuilder;"
                        + "Lnet/minecraft/util/math/BlockPos;)V");
        if (preRender == null) {
            preRender = findMethodOptional(node, "a", "a", "(Lbuk;Let;)V");
        }
        if (preRender == null || !callsOptiFineRenderRegions(preRender)) {
            return basicClass;
        }

        int alignedCoordinates = 0;
        int patchedY = 0;
        for (AbstractInsnNode instruction = preRender.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.ISHL) {
                continue;
            }
            AbstractInsnNode shiftBits = previousReal(instruction);
            AbstractInsnNode shiftRight = previousReal(shiftBits);
            if (!(shiftBits instanceof VarInsnNode) || shiftBits.getOpcode() != Opcodes.ILOAD
                    || shiftRight == null || shiftRight.getOpcode() != Opcodes.ISHR) {
                continue;
            }
            alignedCoordinates++;
            if (alignedCoordinates == 2) {
                preRender.instructions.insert(instruction, list(
                        new InsnNode(Opcodes.POP),
                        new InsnNode(Opcodes.ICONST_0)));
                patchedY++;
            }
        }
        require(alignedCoordinates == 3 && patchedY == 1,
                "Expected three OptiFine render-region coordinate alignments, found "
                        + alignedCoordinates + ", patched Y " + patchedY);
        LOGGER.info("Patched OptiFine render-region Y origin for extended-height sections");
        return write(node);
    }

    private static boolean callsOptiFineRenderRegions(MethodNode method) {
        int calls = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKESTATIC && "Config".equals(call.owner)
                    && "isRenderRegions".equals(call.name) && "()Z".equals(call.desc)) {
                calls++;
            }
        }
        require(calls <= 1, "Multiple OptiFine render-region checks in " + method.name);
        return calls == 1;
    }

    private static void patchRenderGlobalEntityHeightBounds(MethodNode method, String owner,
            String worldField) {
        int minPatched = 0;
        int maxPatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            AbstractInsnNode previous = previousReal(instruction);
            if (instruction.getOpcode() == Opcodes.DCONST_0 && isEntityPosYField(previous)) {
                method.instructions.insertBefore(instruction, loadClientWorldAndCall(owner, worldField,
                        HOOKS, "minY", "(Lnet/minecraft/world/World;)I", Opcodes.I2D));
                method.instructions.remove(instruction);
                minPatched++;
            } else if (instruction instanceof LdcInsnNode
                    && Double.valueOf(256.0D).equals(((LdcInsnNode) instruction).cst)
                    && isEntityPosYField(previous)) {
                method.instructions.insertBefore(instruction, loadClientWorldAndCall(owner, worldField,
                        HOOKS, "maxYExclusive", "(Lnet/minecraft/world/World;)I", Opcodes.I2D));
                method.instructions.remove(instruction);
                maxPatched++;
            }
            instruction = next;
        }
        require(minPatched == 1 && maxPatched == 1,
                "Expected one RenderGlobal entity min/max height pair, patched "
                        + minPatched + "/" + maxPatched);
    }

    private static boolean isEntityPosYField(AbstractInsnNode instruction) {
        if (!(instruction instanceof org.objectweb.asm.tree.FieldInsnNode)) {
            return false;
        }
        org.objectweb.asm.tree.FieldInsnNode field =
                (org.objectweb.asm.tree.FieldInsnNode) instruction;
        return field.getOpcode() == Opcodes.GETFIELD && "D".equals(field.desc)
                && ("posY".equals(field.name) || "field_70163_u".equals(field.name));
    }

    private static InsnList loadClientWorldAndCall(String owner, String worldField, String hookOwner,
            String hookName, String hookDesc, int conversionOpcode) {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                        "Lnet/minecraft/client/multiplayer/WorldClient;"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, hookOwner, hookName, hookDesc, false),
                new InsnNode(conversionOpcode));
    }

    private static void patchRenderGlobalFallbackLayers(ClassNode node, String worldField) {
        MethodNode setupTerrain = findMethod(node, "setupTerrain", "func_174970_a",
                "(Lnet/minecraft/entity/Entity;DLnet/minecraft/client/renderer/culling/ICamera;IZ)V");
        AbstractInsnNode top = null;
        for (AbstractInsnNode instruction = setupTerrain.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof IntInsnNode && ((IntInsnNode) instruction).operand == 248) {
                require(top == null, "Multiple RenderGlobal fallback top constants");
                top = instruction;
            }
        }
        require(top != null, "Missing RenderGlobal fallback top constant");
        AbstractInsnNode bottom = nextReal(top);
        while (bottom != null && (!(bottom instanceof IntInsnNode)
                || ((IntInsnNode) bottom).operand != 8)) {
            bottom = nextReal(bottom);
        }
        require(bottom != null, "Missing RenderGlobal fallback bottom constant");
        setupTerrain.instructions.insertBefore(top, clientWorldHook(node.name, worldField,
                "topRenderSectionCenter", "(Lnet/minecraft/world/World;)I"));
        setupTerrain.instructions.remove(top);
        setupTerrain.instructions.insertBefore(bottom, clientWorldHook(node.name, worldField,
                "bottomRenderSectionCenter", "(Lnet/minecraft/world/World;)I"));
        setupTerrain.instructions.remove(bottom);
    }

    private static void patchWorldBorderHeight(ClassNode node, String worldField) {
        MethodNode method = findMethod(node, "renderWorldBorder", "func_180449_a",
                "(Lnet/minecraft/entity/Entity;F)V");
        int topPatched = 0;
        int bottomPatched = 0;
        int texturePatched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof LdcInsnNode
                    && Double.valueOf(256.0D).equals(((LdcInsnNode) instruction).cst)) {
                method.instructions.insertBefore(instruction, clientWorldHook(node.name, worldField,
                        "worldBorderTop", "(Lnet/minecraft/world/World;)D"));
                method.instructions.remove(instruction);
                topPatched++;
            } else if (instruction.getOpcode() == Opcodes.DCONST_0 && bottomPatched < 8) {
                method.instructions.insertBefore(instruction, clientWorldHook(node.name, worldField,
                        "worldBorderBottom", "(Lnet/minecraft/world/World;)D"));
                method.instructions.remove(instruction);
                bottomPatched++;
            } else if (instruction instanceof LdcInsnNode
                    && Float.valueOf(128.0F).equals(((LdcInsnNode) instruction).cst)) {
                method.instructions.insertBefore(instruction, clientWorldHook(node.name, worldField,
                        "worldBorderTextureSpan", "(Lnet/minecraft/world/World;)F"));
                method.instructions.remove(instruction);
                texturePatched++;
            }
            instruction = next;
        }
        require(topPatched == 8 && bottomPatched == 8 && texturePatched == 9,
                "Unexpected world-border height constants: top=" + topPatched + ", bottom="
                        + bottomPatched + ", texture=" + texturePatched);
    }

    private static InsnList clientWorldHook(String owner, String worldField, String hookName,
            String hookDesc) {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                        "Lnet/minecraft/client/multiplayer/WorldClient;"),
                new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, hookName, hookDesc, false));
    }

    private static void patchBlockPosLowerBound(MethodNode method, String owner, String worldField) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/util/math/BlockPos".equals(call.owner)
                    || !"()I".equals(call.desc)
                    || !("getY".equals(call.name) || "func_177956_o".equals(call.name))) {
                continue;
            }
            AbstractInsnNode jumpNode = nextReal(instruction);
            if (!(jumpNode instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || jumpNode.getOpcode() != Opcodes.IFLT) {
                continue;
            }
            method.instructions.insertBefore(jumpNode, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                            "Lnet/minecraft/world/WorldServer;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            ((org.objectweb.asm.tree.JumpInsnNode) jumpNode).setOpcode(Opcodes.IF_ICMPLT);
            patched++;
        }
        require(patched == 1, "Expected one portal BlockPos lower bound, patched " + patched);
    }

    private static void patchPortalVerticalSearch(MethodNode method, String owner, String worldField) {
        java.util.Set<Integer> heightLocals = new HashSet<>();
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!("getActualHeight".equals(call.name) || "func_72940_L".equals(call.name))
                    || !"()I".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode one = nextReal(instruction);
            AbstractInsnNode subtract = nextReal(one);
            AbstractInsnNode store = nextReal(subtract);
            if (one != null && one.getOpcode() == Opcodes.ICONST_1
                    && subtract != null && subtract.getOpcode() == Opcodes.ISUB
                    && store instanceof VarInsnNode && store.getOpcode() == Opcodes.ISTORE) {
                heightLocals.add(((VarInsnNode) store).var);
            }
        }
        require(!heightLocals.isEmpty() && heightLocals.size() <= 2,
                "Expected one or two reused portal vertical search locals, found "
                        + heightLocals.size());

        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof VarInsnNode) || instruction.getOpcode() != Opcodes.ILOAD
                    || !heightLocals.contains(((VarInsnNode) instruction).var)) {
                continue;
            }
            AbstractInsnNode jumpNode = nextReal(instruction);
            if (!(jumpNode instanceof org.objectweb.asm.tree.JumpInsnNode)
                    || (jumpNode.getOpcode() != Opcodes.IFLT && jumpNode.getOpcode() != Opcodes.IFLE)) {
                continue;
            }
            int replacementOpcode = jumpNode.getOpcode() == Opcodes.IFLT
                    ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPLE;
            method.instructions.insertBefore(jumpNode, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                            "Lnet/minecraft/world/WorldServer;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "minY",
                            "(Lnet/minecraft/world/World;)I", false)));
            ((org.objectweb.asm.tree.JumpInsnNode) jumpNode).setOpcode(replacementOpcode);
            patched++;
        }
        require(patched == 4, "Expected four portal lower-bound checks, patched " + patched);
    }

    private static void patchRenderGlobalEntitySection(MethodNode method) {
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/world/chunk/Chunk".equals(call.owner)
                    || !"()[Lnet/minecraft/util/ClassInheritanceMultiMap;".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode chunkLoad = previousReal(instruction);
            require(chunkLoad instanceof VarInsnNode && chunkLoad.getOpcode() == Opcodes.ALOAD,
                    "Missing RenderGlobal chunk local before entity-list access");
            int chunkVar = ((VarInsnNode) chunkLoad).var;
            for (AbstractInsnNode cursor = instruction.getNext(), end = advance(instruction, 14);
                    cursor != null && cursor != end; cursor = cursor.getNext()) {
                if (cursor.getOpcode() != Opcodes.IDIV) {
                    continue;
                }
                AbstractInsnNode divisor = previousReal(cursor);
                AbstractInsnNode after = nextReal(cursor);
                if (divisor instanceof IntInsnNode && divisor.getOpcode() == Opcodes.BIPUSH
                        && ((IntInsnNode) divisor).operand == 16
                        && after != null && after.getOpcode() == Opcodes.AALOAD) {
                    method.instructions.insert(cursor, list(
                            new VarInsnNode(Opcodes.ALOAD, chunkVar),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "entitySectionIndexForChunk",
                                    "(ILnet/minecraft/world/chunk/Chunk;)I", false)));
                    patched++;
                    break;
                }
            }
        }
        require(patched == 1, "Expected one RenderGlobal entity-section lookup, patched " + patched);
    }

    private static byte[] transformAnvilChunkLoader(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode method = findMethod(node, "readChunkFromNBT", "func_75823_a",
                "(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;");
        int allocations = 0;
        int assignments = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH
                    && ((IntInsnNode) instruction).operand == 16 && next != null
                    && next.getOpcode() == Opcodes.ANEWARRAY
                    && STORAGE.equals(((org.objectweb.asm.tree.TypeInsnNode) next).desc)) {
                InsnList replacement = new InsnList();
                replacement.add(new VarInsnNode(Opcodes.ALOAD, 1));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sectionCount",
                        "(Lnet/minecraft/world/World;)I", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                allocations++;
                instruction = next;
                continue;
            }
            if (instruction.getOpcode() == Opcodes.ILOAD && instruction instanceof VarInsnNode
                    && ((VarInsnNode) instruction).var == 12 && next != null
                    && next.getOpcode() == Opcodes.ALOAD && next instanceof VarInsnNode
                    && ((VarInsnNode) next).var == 13 && next.getNext() != null
                    && next.getNext().getOpcode() == Opcodes.AASTORE) {
                InsnList mapping = new InsnList();
                mapping.add(new VarInsnNode(Opcodes.ALOAD, 1));
                mapping.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForSectionY",
                        "(ILnet/minecraft/world/World;)I", false));
                method.instructions.insert(instruction, mapping);
                assignments++;
            }
            instruction = next;
        }
        require(allocations == 1, "Expected one Anvil section-array allocation, patched " + allocations);
        require(assignments == 1, "Expected one Anvil section assignment, patched " + assignments);
        LOGGER.info("Patched Anvil chunk section loading");
        return write(node);
    }

    private static byte[] transformChunkPacket(byte[] basicClass) {
        ClassNode node = read(basicClass);
        MethodNode constructor = findMethod(node, "<init>", "<init>",
                "(Lnet/minecraft/world/chunk/Chunk;I)V");
        AbstractInsnNode superCall = null;
        for (AbstractInsnNode instruction = constructor.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.INVOKESPECIAL
                    && instruction instanceof MethodInsnNode
                    && "java/lang/Object".equals(((MethodInsnNode) instruction).owner)
                    && "<init>".equals(((MethodInsnNode) instruction).name)) {
                superCall = instruction;
                break;
            }
        }
        require(superCall != null, "Missing SPacketChunkData super constructor call");
        InsnList normalize = list(
                new VarInsnNode(Opcodes.ALOAD, 1),
                new VarInsnNode(Opcodes.ILOAD, 2),
                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "normalizeSectionMask",
                        "(Lnet/minecraft/world/chunk/Chunk;I)I", false),
                new VarInsnNode(Opcodes.ISTORE, 2));
        constructor.instructions.insert(superCall, normalize);

        int fullMasks = 0;
        int tileSections = 0;
        int preservedChunkFrames = 0;
        for (AbstractInsnNode instruction = constructor.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof FrameNode) {
                FrameNode frame = (FrameNode) instruction;
                if (frame.type == Opcodes.F_FULL && frame.local != null && frame.local.size() >= 5
                        && node.name.equals(frame.local.get(0)) && Opcodes.TOP.equals(frame.local.get(1))
                        && "java/util/Iterator".equals(frame.local.get(4))) {
                    frame.local.set(1, "net/minecraft/world/chunk/Chunk");
                    preservedChunkFrames++;
                }
            }
            if (instruction instanceof LdcInsnNode
                    && Integer.valueOf(65535).equals(((LdcInsnNode) instruction).cst)) {
                InsnList replacement = list(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "fullSectionMask",
                                "(Lnet/minecraft/world/chunk/Chunk;)I", false));
                constructor.instructions.insertBefore(instruction, replacement);
                constructor.instructions.remove(instruction);
                fullMasks++;
                instruction = next;
                continue;
            }
            if (instruction.getOpcode() == Opcodes.ICONST_4 && next != null
                    && next.getOpcode() == Opcodes.ISHR) {
                AbstractInsnNode after = next.getNext();
                InsnList replacement = list(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForChunk",
                                "(ILnet/minecraft/world/chunk/Chunk;)I", false));
                constructor.instructions.insertBefore(instruction, replacement);
                constructor.instructions.remove(next);
                constructor.instructions.remove(instruction);
                tileSections++;
                instruction = after;
                continue;
            }
            instruction = next;
        }
        require(fullMasks == 1, "Expected one SPacketChunkData full mask, patched " + fullMasks);
        require(tileSections == 1, "Expected one SPacketChunkData tile section, patched " + tileSections);
        require(preservedChunkFrames == 1,
                "Expected one SPacketChunkData chunk-local frame, patched " + preservedChunkFrames);
        LOGGER.info("Patched full chunk packet for 24 sections");
        return write(node);
    }

    private static void patchChunkPacketRead(ClassNode node) {
        MethodNode method = findMethodOptional(node, "read", "func_186033_a",
                "(Lnet/minecraft/network/PacketBuffer;IZ)V");
        if (method == null) {
            return;
        }
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode shift = instruction.getNext();
            AbstractInsnNode after = shift == null ? null : shift.getNext();
            if (instruction.getOpcode() == Opcodes.ICONST_4 && shift != null
                    && shift.getOpcode() == Opcodes.ISHL && after != null
                    && after.getOpcode() == Opcodes.ILOAD) {
                InsnList replacement = list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sectionBaseYForChunkIndex",
                                "(ILnet/minecraft/world/chunk/Chunk;)I", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(shift);
                method.instructions.remove(instruction);
                patched++;
                instruction = after;
                continue;
            }
            instruction = instruction.getNext();
        }
        require(patched == 1, "Expected one Chunk packet section base, patched " + patched);
    }

    private static void patchChunkEntitySections(ClassNode node) {
        MethodNode addEntity = findMethod(node, "addEntity", "func_76612_a",
                "(Lnet/minecraft/entity/Entity;)V");
        int floorCalls = 0;
        int mapped = 0;
        for (AbstractInsnNode instruction = addEntity.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.INVOKESTATIC && instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("net/minecraft/util/math/MathHelper".equals(call.owner)
                        && "(D)I".equals(call.desc)) {
                    floorCalls++;
                    if (floorCalls == 3) {
                        addEntity.instructions.insert(instruction, list(
                                new VarInsnNode(Opcodes.ALOAD, 0),
                                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "entitySectionIndexForChunk",
                                        "(ILnet/minecraft/world/chunk/Chunk;)I", false)));
                        mapped++;
                    }
                }
            }
        }
        require(mapped == 1, "Expected one Chunk entity section mapping, patched " + mapped);

        replace(findMethod(node, "getEntitiesWithinAABBForEntity", "func_177414_a",
                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"),
                list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3),
                        new VarInsnNode(Opcodes.ALOAD, 4),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getEntitiesWithinAABBForEntity",
                                "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V",
                                false),
                        new InsnNode(Opcodes.RETURN)));
        replace(findMethod(node, "getEntitiesOfTypeWithinAABB", "func_177430_a",
                "(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"),
                list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 3),
                        new VarInsnNode(Opcodes.ALOAD, 4),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "getEntitiesOfTypeWithinAABB",
                                "(Lnet/minecraft/world/chunk/Chunk;Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V",
                                false),
                        new InsnNode(Opcodes.RETURN)));
    }

    private static void patchViewFrustumCount(ClassNode node, String worldField) {
        MethodNode method = findMethod(node, "setCountChunksXYZ", "func_178159_a", "(I)V");
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH
                    && ((IntInsnNode) instruction).operand == 16 && next instanceof org.objectweb.asm.tree.FieldInsnNode
                    && next.getOpcode() == Opcodes.PUTFIELD) {
                InsnList replacement = list(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, worldField,
                                "Lnet/minecraft/world/World;"),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sectionCount",
                                "(Lnet/minecraft/world/World;)I", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                patched++;
                instruction = next;
                continue;
            }
            instruction = next;
        }
        require(patched == 1, "Expected one ViewFrustum Y count, patched " + patched);
    }

    private static void patchViewFrustumYPositions(ClassNode node, String worldField) {
        MethodNode create = findMethod(node, "createRenderChunks", "func_178158_a",
                "(Lnet/minecraft/client/renderer/chunk/IRenderChunkFactory;)V");
        patchNthMultiplyBy16(create, node.name, worldField, 2);

        MethodNode update = findMethod(node, "updateChunkPositions", "func_178163_a", "(DD)V");
        patchNthMultiplyBy16(update, node.name, worldField, 2);
    }

    private static void patchNthMultiplyBy16(MethodNode method, String owner, String worldField, int ordinal) {
        int found = 0;
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.BIPUSH
                    && ((IntInsnNode) instruction).operand == 16 && next != null
                    && next.getOpcode() == Opcodes.IMUL) {
                found++;
                if (found == ordinal) {
                    AbstractInsnNode after = next.getNext();
                    InsnList replacement = list(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                                    "Lnet/minecraft/world/World;"),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sectionBaseYForStorageIndex",
                                    "(ILnet/minecraft/world/World;)I", false));
                    method.instructions.insertBefore(instruction, replacement);
                    method.instructions.remove(next);
                    method.instructions.remove(instruction);
                    patched++;
                    instruction = after;
                    continue;
                }
            }
            instruction = next;
        }
        require(patched == 1, "Expected one ViewFrustum Y position in " + method.name + ", patched " + patched);
    }

    private static void patchViewFrustumSectionLookup(ClassNode node, String worldField) {
        MethodNode getRenderChunk = findMethod(node, "getRenderChunk", "func_178161_a",
                "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/client/renderer/chunk/RenderChunk;");
        patchNthFloorDiv(getRenderChunk, node.name, worldField, 2);

        MethodNode mark = findMethod(node, "markBlocksForUpdate", "func_187474_a", "(IIIIIIZ)V");
        int remainders = 0;
        int patched = 0;
        for (AbstractInsnNode instruction = mark.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == Opcodes.IREM) {
                remainders++;
                if (remainders == 2) {
                    AbstractInsnNode countField = previousReal(instruction);
                    AbstractInsnNode loadThis = previousReal(countField);
                    require(countField != null && countField.getOpcode() == Opcodes.GETFIELD
                                    && loadThis != null && loadThis.getOpcode() == Opcodes.ALOAD,
                            "Unexpected ViewFrustum Y remainder shape");
                    InsnList replacement = list(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, node.name, worldField,
                                    "Lnet/minecraft/world/World;"),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, CLIENT_HOOKS, "renderSectionIndex",
                                    "(IILnet/minecraft/world/World;)I", false));
                    mark.instructions.insertBefore(instruction, replacement);
                    mark.instructions.remove(instruction);
                    patched++;
                    instruction = next;
                    continue;
                }
            }
            instruction = next;
        }
        require(patched == 1, "Expected one ViewFrustum update Y lookup, patched " + patched);
    }

    private static void patchNthFloorDiv(MethodNode method, String owner, String worldField, int ordinal) {
        int found = 0;
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.INVOKESTATIC && instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (isFloorDivCall(call)) {
                    found++;
                    if (found == ordinal) {
                        method.instructions.insert(instruction, list(
                                new VarInsnNode(Opcodes.ALOAD, 0),
                                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                                        "Lnet/minecraft/world/World;"),
                                new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForSectionY",
                                        "(ILnet/minecraft/world/World;)I", false)));
                        patched++;
                    }
                }
            }
        }
        if (patched == 0 && found == 0) {
            patched = patchNthInlinedFloorDiv(method, owner, worldField, ordinal);
        }
        require(patched == 1, "Expected one ViewFrustum floor section lookup, patched " + patched);
    }

    /**
     * OptiFine may inline MathHelper.intFloorDiv(pos.getY(), 16) into a coordinate
     * getter followed by ICONST_4 and ISHR. Keep this fallback isolated from the
     * ordinary Forge call path so only one representation is rewritten.
     */
    private static int patchNthInlinedFloorDiv(MethodNode method, String owner, String worldField, int ordinal) {
        int found = 0;
        int patched = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.ISHR) {
                continue;
            }
            AbstractInsnNode four = previousReal(instruction);
            AbstractInsnNode getter = previousReal(four);
            if (!isPushFour(four) || !isBlockPosCoordinateGetter(getter)) {
                continue;
            }
            found++;
            if (found != ordinal) {
                continue;
            }
            method.instructions.insert(instruction, list(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                            "Lnet/minecraft/world/World;"),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForSectionY",
                            "(ILnet/minecraft/world/World;)I", false)));
            patched++;
        }
        return patched;
    }

    private static boolean isPushFour(AbstractInsnNode instruction) {
        return instruction != null && instruction.getOpcode() == Opcodes.ICONST_4;
    }

    private static boolean isIntConstant(AbstractInsnNode instruction, int value) {
        if (instruction == null) {
            return false;
        }
        int opcode = instruction.getOpcode();
        if (value >= -1 && value <= 5) {
            return opcode == Opcodes.ICONST_0 + value;
        }
        if (instruction instanceof IntInsnNode) {
            return ((IntInsnNode) instruction).operand == value;
        }
        return instruction instanceof LdcInsnNode
                && Integer.valueOf(value).equals(((LdcInsnNode) instruction).cst);
    }

    private static boolean isBlockPosCoordinateGetter(AbstractInsnNode instruction) {
        if (!(instruction instanceof MethodInsnNode)) {
            return false;
        }
        MethodInsnNode call = (MethodInsnNode) instruction;
        if (!"()I".equals(call.desc) || call.getOpcode() != Opcodes.INVOKEVIRTUAL) {
            return false;
        }
        boolean blockPosOwner = "net/minecraft/util/math/BlockPos".equals(call.owner)
                || call.owner.endsWith("/BlockPos") || "et".equals(call.owner) || "fq".equals(call.owner);
        return blockPosOwner && ("func_177958_n".equals(call.name)
                || "func_177956_o".equals(call.name)
                || "func_177952_p".equals(call.name)
                || "getX".equals(call.name)
                || "getY".equals(call.name)
                || "getZ".equals(call.name)
                || "p".equals(call.name)
                || "q".equals(call.name)
                || "r".equals(call.name));
    }

    private static boolean isFloorDivCall(MethodInsnNode call) {
        if (call.getOpcode() != Opcodes.INVOKESTATIC || !"(II)I".equals(call.desc)) {
            return false;
        }
        return "net/minecraft/util/math/MathHelper".equals(call.owner)
                || "rk".equals(call.owner)
                || call.owner.endsWith("/MathHelper");
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode node) {
        AbstractInsnNode cursor = node == null ? null : node.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getPrevious();
        }
        return cursor;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode node) {
        AbstractInsnNode cursor = node == null ? null : node.getNext();
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private static int findNextIntStore(AbstractInsnNode node, int maximumInstructions) {
        AbstractInsnNode cursor = node;
        for (int i = 0; i < maximumInstructions; i++) {
            cursor = nextReal(cursor);
            if (cursor == null) {
                break;
            }
            if (cursor instanceof VarInsnNode && cursor.getOpcode() == Opcodes.ISTORE) {
                return ((VarInsnNode) cursor).var;
            }
        }
        return -1;
    }

    private static String findField(ClassNode node, String desc) {
        String result = null;
        for (FieldNode field : node.fields) {
            if (desc.equals(field.desc)) {
                require(result == null, "Multiple fields with descriptor " + desc + " in " + node.name);
                result = field.name;
            }
        }
        require(result != null, "Missing field with descriptor " + desc + " in " + node.name);
        return result;
    }

    private static java.util.List<String> findFields(ClassNode node, String desc) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (FieldNode field : node.fields) {
            if (desc.equals(field.desc)) {
                result.add(field.name);
            }
        }
        return result;
    }

    private static InsnList loadChunkCacheFields(String owner, String worldField,
            String chunkXField, String chunkZField, String chunkArrayField) {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, worldField,
                        "Lnet/minecraft/world/World;"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, chunkXField, "I"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, chunkZField, "I"),
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner, chunkArrayField,
                        "[[Lnet/minecraft/world/chunk/Chunk;"));
    }

    private static String findReferencedField(MethodNode method, String owner, String desc) {
        String result = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.FieldInsnNode)) {
                continue;
            }
            org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
            if (!owner.equals(field.owner) || !desc.equals(field.desc)) {
                continue;
            }
            if (result == null) {
                result = field.name;
            } else {
                require(result.equals(field.name), "Multiple referenced fields with descriptor " + desc
                        + " in " + method.name);
            }
        }
        require(result != null, "Missing referenced field with descriptor " + desc + " in " + method.name);
        return result;
    }

    private static org.objectweb.asm.tree.FieldInsnNode findReferencedFieldInstruction(
            MethodNode method, String desc) {
        org.objectweb.asm.tree.FieldInsnNode result = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.FieldInsnNode)) {
                continue;
            }
            org.objectweb.asm.tree.FieldInsnNode field =
                    (org.objectweb.asm.tree.FieldInsnNode) instruction;
            if (field.getOpcode() != Opcodes.GETFIELD || !desc.equals(field.desc)) {
                continue;
            }
            if (result == null) {
                result = field;
            } else {
                require(result.owner.equals(field.owner) && result.name.equals(field.name),
                        "Multiple referenced fields with descriptor " + desc
                                + " in " + method.name);
            }
        }
        require(result != null,
                "Missing referenced field with descriptor " + desc + " in " + method.name);
        return result;
    }

    private static void replaceFrameType(ClassNode node, String from, String to) {
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;
                    instruction = instruction.getNext()) {
                if (!(instruction instanceof org.objectweb.asm.tree.FrameNode)) {
                    continue;
                }
                org.objectweb.asm.tree.FrameNode frame = (org.objectweb.asm.tree.FrameNode) instruction;
                replaceFrameEntries(frame.local, from, to);
                replaceFrameEntries(frame.stack, from, to);
            }
        }
    }

    private static void replaceFrameEntries(java.util.List<Object> entries, String from, String to) {
        if (entries == null) {
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (from.equals(entries.get(i))) {
                entries.set(i, to);
            }
        }
    }

    private static void patchChunkConstructor(ClassNode node) {
        MethodNode constructor = findMethod(node, "<init>", "<init>",
                "(Lnet/minecraft/world/World;II)V");
        int patched = 0;
        for (AbstractInsnNode instruction = constructor.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode following = instruction.getNext();
            if (!(instruction instanceof IntInsnNode) || instruction.getOpcode() != Opcodes.BIPUSH
                    || ((IntInsnNode) instruction).operand != 16) {
                instruction = following;
                continue;
            }
            AbstractInsnNode next = instruction.getNext();
            if (next == null || next.getOpcode() != Opcodes.ANEWARRAY) {
                instruction = following;
                continue;
            }
            String arrayType = ((org.objectweb.asm.tree.TypeInsnNode) next).desc;
            if (!STORAGE.equals(arrayType)
                    && !"net/minecraft/util/ClassInheritanceMultiMap".equals(arrayType)) {
                instruction = following;
                continue;
            }
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(Opcodes.ALOAD, 1));
            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "sectionCount",
                    "(Lnet/minecraft/world/World;)I", false));
            constructor.instructions.insertBefore(instruction, replacement);
            constructor.instructions.remove(instruction);
            patched++;
            instruction = next;
        }
        require(patched == 2, "Expected two Chunk section-array allocations, patched " + patched);
    }

    private static void patchInitialRelightCheckCount(ClassNode node, String queuedLightChecksField) {
        MethodNode constructor = findMethod(node, "<init>", "<init>",
                "(Lnet/minecraft/world/World;II)V");
        int patched = 0;
        for (AbstractInsnNode instruction = constructor.instructions.getFirst(); instruction != null;) {
            AbstractInsnNode next = instruction.getNext();
            AbstractInsnNode following = nextReal(instruction);
            if (instruction instanceof IntInsnNode && instruction.getOpcode() == Opcodes.SIPUSH
                    && ((IntInsnNode) instruction).operand == 4096
                    && following instanceof org.objectweb.asm.tree.FieldInsnNode) {
                org.objectweb.asm.tree.FieldInsnNode field =
                        (org.objectweb.asm.tree.FieldInsnNode) following;
                if (field.getOpcode() == Opcodes.PUTFIELD && node.name.equals(field.owner)
                        && queuedLightChecksField.equals(field.name)) {
                    constructor.instructions.insertBefore(instruction, list(
                            new VarInsnNode(Opcodes.ALOAD, 1),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "totalRelightChecks",
                                    "(Lnet/minecraft/world/World;)I", false)));
                    constructor.instructions.remove(instruction);
                    patched++;
                }
            }
            instruction = next;
        }
        require(patched == 1, "Expected one initial relight-check count, patched " + patched);
    }

    private static void patchStorageCoordinateIndices(ClassNode node) {
        int patched = 0;
        for (MethodNode method : node.methods) {
            if ("read".equals(method.name) || "func_186033_a".equals(method.name)) {
                continue;
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction.getOpcode() == Opcodes.ICONST_4 && next != null
                        && next.getOpcode() == Opcodes.ISHR && isStorageArrayIndex(next)) {
                    AbstractInsnNode after = next.getNext();
                    InsnList replacement = new InsnList();
                    replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "storageIndexForChunk",
                            "(ILnet/minecraft/world/chunk/Chunk;)I", false));
                    method.instructions.insertBefore(instruction, replacement);
                    method.instructions.remove(next);
                    method.instructions.remove(instruction);
                    patched++;
                    instruction = after;
                    continue;
                }
                instruction = next;
            }
        }
        require(patched == 2, "Expected two Chunk coordinate-to-section accesses, patched " + patched);
    }

    private static boolean isStorageArrayIndex(AbstractInsnNode shift) {
        for (AbstractInsnNode cursor = shift.getNext(), end = advance(shift, 6);
                cursor != null && cursor != end; cursor = cursor.getNext()) {
            int opcode = cursor.getOpcode();
            if (opcode == Opcodes.AALOAD || opcode == Opcodes.AASTORE) {
                return true;
            }
            if (opcode >= 0 && opcode != Opcodes.CHECKCAST && opcode != Opcodes.ALOAD) {
                return false;
            }
        }
        return false;
    }

    private static AbstractInsnNode advance(AbstractInsnNode node, int count) {
        AbstractInsnNode cursor = node;
        while (cursor != null && count-- > 0) {
            cursor = cursor.getNext();
        }
        return cursor;
    }

    private static InsnList loadMixinSelfAsWorld() {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST,
                        "net/minecraft/world/World"));
    }

    private static InsnList loadAlfheimChunkWorld(String owner) {
        return list(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner,
                        "field_76637_e", "Lnet/minecraft/world/World;"));
    }

    private static void prependExtendedWorldBranch(MethodNode method, InsnList worldLoad,
            InsnList extendedBody) {
        worldLoad.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "isExtended",
                "(Lnet/minecraft/world/World;)Z", false));
        prependConditionBranch(method, worldLoad, extendedBody);
    }

    private static void prependConditionBranch(MethodNode method, InsnList condition,
            InsnList trueBody) {
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(condition);
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(trueBody);
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        AbstractInsnNode first = method.instructions.getFirst();
        if (first == null) {
            method.instructions.add(prefix);
        } else {
            method.instructions.insertBefore(first, prefix);
        }
    }

    private static InsnList copy(InsnList source) {
        Map<LabelNode, LabelNode> labels = new java.util.IdentityHashMap<>();
        for (AbstractInsnNode instruction = source.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, new LabelNode());
            }
        }
        InsnList result = new InsnList();
        for (AbstractInsnNode instruction = source.getFirst(); instruction != null;
                instruction = instruction.getNext()) {
            result.add(instruction.clone(labels));
        }
        return result;
    }

    private static void setMixinAnnotationInt(MethodNode method, String desc, String key,
            int value) {
        AnnotationNode annotation = findAnnotation(method.visibleAnnotations, desc);
        if (annotation == null) {
            annotation = findAnnotation(method.invisibleAnnotations, desc);
        }
        require(annotation != null, "Missing annotation " + desc + " on " + method.name);
        if (annotation.values == null) {
            annotation.values = new java.util.ArrayList<>();
        }
        for (int index = 0; index < annotation.values.size(); index += 2) {
            if (key.equals(annotation.values.get(index))) {
                annotation.values.set(index + 1, Integer.valueOf(value));
                return;
            }
        }
        annotation.values.add(key);
        annotation.values.add(Integer.valueOf(value));
    }

    private static AnnotationNode findAnnotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotation : annotations) {
            if (desc.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    private static void addPublicBridgeCopy(ClassNode node, MethodNode source, String name) {
        require(findMethodOptional(node, name, name, source.desc) == null,
                "Duplicate bridge " + node.name + "." + name + source.desc);
        int access = source.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED
                | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE);
        access |= Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC;
        MethodNode bridge = new MethodNode(Opcodes.ASM5, access, name, source.desc,
                source.signature, source.exceptions == null ? null
                        : source.exceptions.toArray(new String[source.exceptions.size()]));
        source.accept(bridge);
        node.methods.add(bridge);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode(Opcodes.ASM5);
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode findMethod(ClassNode node, String mcpName, String srgName, String desc) {
        MethodNode method = findMethodOptional(node, mcpName, srgName, desc);
        if (method != null) {
            return method;
        }
        throw new IllegalStateException("Missing method " + node.name + "." + mcpName + desc);
    }

    private static MethodNode findMethodByName(ClassNode node, String name) {
        MethodNode result = null;
        for (MethodNode method : node.methods) {
            if (!name.equals(method.name)) {
                continue;
            }
            require(result == null, "Multiple methods named " + name + " in " + node.name);
            result = method;
        }
        require(result != null, "Missing method " + node.name + "." + name);
        return result;
    }

    private static MethodNode findMethodOptional(ClassNode node, String mcpName, String srgName, String desc) {
        for (MethodNode method : node.methods) {
            if (desc.equals(method.desc) && (mcpName.equals(method.name) || srgName.equals(method.name))) {
                return method;
            }
        }
        return null;
    }

    private static void replace(MethodNode method, InsnList instructions) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.instructions.add(instructions);
    }

    private static InsnList list(AbstractInsnNode... nodes) {
        InsnList list = new InsnList();
        for (AbstractInsnNode node : nodes) {
            list.add(node);
        }
        return list;
    }

    private static InsnList list(InsnList prefix, AbstractInsnNode... nodes) {
        InsnList list = new InsnList();
        list.add(prefix);
        for (AbstractInsnNode node : nodes) {
            list.add(node);
        }
        return list;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
