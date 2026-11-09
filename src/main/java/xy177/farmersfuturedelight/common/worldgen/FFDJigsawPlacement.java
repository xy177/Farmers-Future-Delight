package xy177.farmersfuturedelight.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntBinaryOperator;
import java.util.zip.GZIPInputStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xy177.farmersfuturedelight.common.block.BlockJigsaw;
import xy177.farmersfuturedelight.common.tile.TileEntityJigsaw;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

public final class FFDJigsawPlacement {
    private static final ResourceLocation EMPTY = new ResourceLocation("minecraft:empty");
    private static final int MAX_PIECES = 512;
    private static final int MAX_ATTEMPTS = 32768;
    private static final int MAX_DISTANCE = 128;
    private static final int MAX_EXTERNAL_POOL_WEIGHT = 8192;
    private static final Logger LOGGER = LogManager.getLogger("FFD Village Generation");
    private static final Set<String> LOGGED_EXTERNAL_POOL_LIMITS =
            Collections.synchronizedSet(new HashSet<>());
    private static final Field TEMPLATE_BLOCKS = ObfuscationReflectionHelper.findField(Template.class, "field_186270_a");
    private static final Field TEMPLATE_FOLDER = ObfuscationReflectionHelper.findField(TemplateManager.class, "field_186241_b");
    private static final Map<TemplateManager, Map<ResourceLocation, Template>> MATERIAL_TEMPLATES = new java.util.WeakHashMap<>();

    private FFDJigsawPlacement() {
    }

    public static int generate(WorldServer world, TileEntityJigsaw source, int levels, boolean keepJigsaws) {
        NBTTagCompound data = source.getDefinition();
        IBlockState state = world.getBlockState(source.getPos());
        if (!(state.getBlock() instanceof BlockJigsaw)) throw new IllegalArgumentException("jigsaw");
        BlockPos start = source.getPos().offset(state.getValue(BlockJigsaw.ORIENTATION).front);
        Plan plan = plan(world, new ResourceLocation(data.getString("pool")),
                new ResourceLocation(data.getString("target")), levels, start, world.rand);
        plan.place(world, keepJigsaws);
        return plan.pieces.size();
    }

    public static Plan plan(WorldServer world, ResourceLocation pool, ResourceLocation target, int levels,
                             BlockPos position, Random random) {
        return plan(world, pool, target, levels, position, random, (blockX, blockZ) -> {
            BlockPos column = new BlockPos(blockX, 0, blockZ);
            if (!world.isBlockLoaded(column)) throw new IllegalArgumentException("Jigsaw terrain requires loaded chunks");
            return world.getHeight(blockX, blockZ);
        }, false);
    }

    public static Plan plan(WorldServer world, ResourceLocation pool, ResourceLocation target, int levels,
                            BlockPos position, Random random, IntBinaryOperator heights, boolean projectStart) {
        return plan(world, pool, target, levels, position, random, heights, projectStart, null);
    }

    static Plan plan(WorldServer world, ResourceLocation pool, ResourceLocation target, int levels,
                     BlockPos position, Random random, IntBinaryOperator heights, boolean projectStart,
                     FFDVillageExtensions extensions) {
        if (levels < 0 || levels > 20) throw new IllegalArgumentException("levels: 0..20");
        Context context = new Context(world, position, heights);
        Pool starting = context.pool(pool);
        Rotation rotation = Rotation.values()[random.nextInt(4)];
        Element rootElement = starting.random(random);
        if (rootElement == null || rootElement.isEmpty()) return new Plan(Collections.emptyList(), Collections.emptyList());
        Template rootTemplate = context.template(rootElement);
        Piece root = new Piece(rootElement, rootTemplate, rotation, position);
        Connector anchor = null;
        if (target != null) {
            for (Connector candidate : root.connectors(random)) {
                if (target.equals(candidate.name)) {
                    anchor = candidate;
                    break;
                }
            }
        }
        if (target != null && anchor == null) throw new IllegalArgumentException("Missing start connector " + target + " in " + rootElement.location);
        root = new Piece(rootElement, rootTemplate, rotation,
                anchor == null ? position : position.subtract(anchor.position.subtract(position)));
        if (projectStart) {
            int centerX = (root.bounds.minX + root.bounds.maxX) / 2;
            int centerZ = (root.bounds.minZ + root.bounds.maxZ) / 2;
            int offset = heights.applyAsInt(centerX, centerZ) - root.bounds.minY - 1;
            root = new Piece(rootElement, rootTemplate, rotation, root.origin.add(0, offset, 0));
        }
        if (root.bounds.minY < FFDHeightHooks.minY(world) || root.bounds.maxY >= FFDHeightHooks.maxYExclusive(world)) {
            return new Plan(Collections.emptyList(), Collections.emptyList());
        }
        root.space = context.space;
        context.center = new BlockPos((root.bounds.minX + root.bounds.maxX) / 2,
                root.bounds.minY + 1, (root.bounds.minZ + root.bounds.maxZ) / 2);
        context.maxDistance = projectStart ? 80 : MAX_DISTANCE;
        context.space.occupied.add(root.bounds);
        context.pieces.add(root);
        PriorityQueue<Expansion> queue = new PriorityQueue<>(Comparator
                .comparingInt((Expansion expansion) -> expansion.priority).reversed()
                .thenComparingLong(expansion -> expansion.order));
        long sequence = 0;
        if (levels > 0) queue.add(new Expansion(root, 0, 0, sequence++));
        while (!queue.isEmpty()) {
            Expansion expansion = queue.remove();
            for (Connector source : expansion.parent.connectors(random)) {
            if (EMPTY.equals(source.pool)) continue;
            Pool choices = context.pool(source.pool);
            List<Element> elements = expansion.depth == levels ? new ArrayList<>() : choices.shuffled(random);
            if (extensions != null && expansion.depth < levels
                    && source.pool.getResourcePath().endsWith("/houses")) {
                appendExternalCandidates(elements, extensions.available(), source.pool, random);
                Collections.shuffle(elements, random);
            }
            elements.addAll(context.pool(choices.fallback).shuffled(random));
            boolean accepted = false;
            for (Element element : elements) {
                if (element.external != null) {
                    CollisionSpace space = expansion.parent.childSpace(source);
                    List<StructureBoundingBox> occupied = new ArrayList<>(space.occupied);
                    List<StructureComponent> additions = extensions.build(element.external,
                            source.position.offset(source.front), source.front,
                            expansion.depth + 1, occupied, space.enclosing, random);
                    if (additions.isEmpty()) continue;
                    context.externalPieces.addAll(additions);
                    for (StructureComponent addition : additions) space.occupied.add(addition.getBoundingBox());
                    accepted = true;
                    break;
                }
                if (element.isEmpty()) break;
                Template template = context.template(element);
                List<Rotation> rotations = new ArrayList<>();
                Collections.addAll(rotations, Rotation.values());
                Collections.shuffle(rotations, random);
                for (Rotation candidateRotation : rotations) {
                    Piece local = new Piece(element, template, candidateRotation, BlockPos.ORIGIN);
                    for (Connector candidate : local.connectors(random)) {
                        if (!source.canAttach(candidate)) continue;
                        if (++context.attempts > MAX_ATTEMPTS) throw new IllegalArgumentException("Jigsaw planning limit exceeded");
                        BlockPos origin = source.position.offset(source.front)
                                .subtract(candidate.position);
                        if (expansion.parent.element.terrainMatching || element.terrainMatching) {
                            origin = new BlockPos(origin.getX(), context.height(source.position)
                                    - candidate.position.getY(), origin.getZ());
                        }
                        Piece placed = new Piece(element, template, candidateRotation, origin);
                        if (projectStart) context.expand(placed);
                        CollisionSpace space = expansion.parent.childSpace(source);
                        if (!context.fits(placed, space)) continue;
                        placed.space = space;
                        space.occupied.add(placed.bounds);
                        if (context.pieces.size() >= MAX_PIECES) throw new IllegalArgumentException("Jigsaw piece limit exceeded");
                        context.pieces.add(placed);
                        int sourceLocalY = source.position.getY() - expansion.parent.bounds.minY;
                        int targetLocalY = candidate.position.getY();
                        int deltaY = sourceLocalY - targetLocalY + source.front.getFrontOffsetY();
                        placed.groundDelta = element.terrainMatching ? 1 : expansion.parent.groundDelta - deltaY;
                        int junctionY = !expansion.parent.element.terrainMatching ? source.position.getY()
                                : !element.terrainMatching ? placed.bounds.minY + targetLocalY
                                : context.height(source.position) + deltaY / 2;
                        BlockPos nextPosition = source.position.offset(source.front);
                        expansion.parent.junctions.add(new BlockPos(nextPosition.getX(),
                                junctionY - sourceLocalY + expansion.parent.groundDelta, nextPosition.getZ()));
                        placed.junctions.add(new BlockPos(source.position.getX(),
                                junctionY - targetLocalY + placed.groundDelta, source.position.getZ()));
                        if (expansion.depth + 1 <= levels) {
                            queue.add(new Expansion(placed, expansion.depth + 1, source.placementPriority, sequence++));
                        }
                        accepted = true;
                        break;
                    }
                    if (accepted) break;
                }
                if (accepted) break;
            }
            }
        }
        return new Plan(context.pieces, context.externalPieces);
    }

    private static void appendExternalCandidates(List<Element> elements,
                                                 List<net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight> weights,
                                                 ResourceLocation pool, Random random) {
        if (weights.isEmpty()) return;
        int capacity = MAX_EXTERNAL_POOL_WEIGHT - elements.size();
        if (capacity <= 0) {
            warnExternalPool(pool + ":capacity",
                    "Skipping external village buildings for pool {} because its native candidate weight already uses {} of the {}-entry safety budget",
                    pool, elements.size(), MAX_EXTERNAL_POOL_WEIGHT);
            return;
        }
        if (weights.size() > capacity) {
            List<net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight> selected =
                    new ArrayList<>(weights);
            Collections.shuffle(selected, random);
            warnExternalPool(pool + ":types",
                    "External village pool {} exposes {} building types but only {} candidate slots remain; selecting a bounded subset without aborting world generation",
                    pool, weights.size(), capacity);
            for (int index = 0; index < capacity; index++) {
                elements.add(Element.external(selected.get(index)));
            }
            return;
        }

        long totalWeight = 0L;
        for (net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight : weights) {
            totalWeight += weight.villagePieceWeight;
        }
        if (totalWeight <= capacity) {
            for (net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight : weights) {
                for (int index = 0; index < weight.villagePieceWeight; index++) {
                    elements.add(Element.external(weight));
                }
            }
            return;
        }

        warnExternalPool(pool + ":weight",
                "External village pool {} has total building weight {} but only {} candidate slots remain; normalizing external weights",
                pool, totalWeight, capacity);
        List<ExternalCandidate> candidates = new ArrayList<>(weights.size());
        int extraCapacity = capacity - weights.size();
        int assignedExtra = 0;
        for (net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight : weights) {
            long share = (long) weight.villagePieceWeight * extraCapacity;
            int copies = 1 + (int) (share / totalWeight);
            assignedExtra += copies - 1;
            candidates.add(new ExternalCandidate(weight, copies, share % totalWeight));
        }
        int remaining = extraCapacity - assignedExtra;
        Collections.sort(candidates, (left, right) -> {
            int result = Long.compare(right.remainder, left.remainder);
            if (result != 0) return result;
            return left.weight.villagePieceClass.getName().compareTo(right.weight.villagePieceClass.getName());
        });
        for (int index = 0; index < remaining; index++) candidates.get(index).copies++;
        for (ExternalCandidate candidate : candidates) {
            for (int index = 0; index < candidate.copies; index++) {
                elements.add(Element.external(candidate.weight));
            }
        }
    }

    private static void warnExternalPool(String key, String message, Object... arguments) {
        if (LOGGED_EXTERNAL_POOL_LIMITS.add(key)) LOGGER.warn(message, arguments);
    }

    public static IBlockState parseFinalState(String value) {
        int opening = value.indexOf('[');
        String name = opening < 0 ? value : value.substring(0, opening);
        if (name.startsWith("minecraft:potted_")) {
            finalStateData(value);
            return Blocks.FLOWER_POT.getDefaultState();
        }
        return FFDStructureBlockStates.parse(value);
    }

    private static NBTTagCompound finalStateData(String value) {
        String prefix = "minecraft:potted_";
        if (!value.startsWith(prefix)) return null;
        String plant = value.substring(prefix.length());
        String item;
        int metadata = 0;
        String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
        String[] flowers = {"poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
                "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"};
        if (plant.endsWith("_sapling")) {
            metadata = java.util.Arrays.asList(woods).indexOf(plant.substring(0, plant.length() - 8));
            if (metadata < 0) throw new IllegalArgumentException("Unknown potted plant " + value);
            item = "sapling";
        } else if (java.util.Arrays.asList(flowers).contains(plant)) {
            metadata = java.util.Arrays.asList(flowers).indexOf(plant);
            item = "red_flower";
        } else if ("dandelion".equals(plant)) {
            item = "yellow_flower";
        } else if ("dead_bush".equals(plant)) {
            item = "deadbush";
        } else if ("fern".equals(plant)) {
            item = "tallgrass";
            metadata = 2;
        } else if ("cactus".equals(plant) || "red_mushroom".equals(plant) || "brown_mushroom".equals(plant)) {
            item = plant;
        } else {
            throw new IllegalArgumentException("Unknown potted plant " + value);
        }
        NBTTagCompound data = new NBTTagCompound();
        data.setString("id", "minecraft:flower_pot");
        data.setString("Item", "minecraft:" + item);
        data.setInteger("Data", metadata);
        return data;
    }

    public static final class Plan {
        private final List<Piece> pieces;
        private final List<StructureComponent> externalPieces;

        private Plan(List<Piece> pieces, List<StructureComponent> externalPieces) {
            this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
            this.externalPieces = Collections.unmodifiableList(new ArrayList<>(externalPieces));
        }

        public int size() {
            return pieces.size() + externalPieces.size();
        }

        public List<StructureComponent> components(Random random) {
            List<StructureComponent> result = new ArrayList<>();
            for (Piece piece : pieces) result.add(new SavedPiece(piece, random.nextLong()));
            result.addAll(externalPieces);
            return result;
        }

        public void place(WorldServer world, boolean keepJigsaws) {
            for (Piece piece : pieces) {
                StructureBoundingBox bounds = piece.bounds;
                if (piece.template instanceof FeatureTemplate) {
                    if (!FFDJigsawFeatures.isAreaLoaded(world, piece.origin)) {
                        throw new IllegalArgumentException("Jigsaw feature requires loaded neighboring chunks");
                    }
                }
                if (bounds.minY < FFDHeightHooks.minY(world) || bounds.maxY >= FFDHeightHooks.maxYExclusive(world)
                        || !world.isAreaLoaded(new BlockPos(bounds.minX - 1, bounds.minY, bounds.minZ - 1),
                        new BlockPos(bounds.maxX + 1, bounds.maxY, bounds.maxZ + 1))) {
                    throw new IllegalArgumentException("Jigsaw generation requires loaded chunks within build height");
                }
                for (Connector connector : piece.connectors(new Random(0L))) parseFinalState(connector.finalState);
            }
            Map<BlockPos, Template.BlockInfo> deferred = new HashMap<>();
            for (Piece piece : pieces) {
                placePiece(world, piece, null, keepJigsaws, world.rand.nextLong(), deferred);
            }
            placeBeds(world, deferred);
        }
    }

    private static boolean placePiece(WorldServer world, Piece piece, StructureBoundingBox bounds,
                                      boolean keepJigsaws, long seed) {
        return placePiece(world, piece, bounds, keepJigsaws, seed, new HashMap<>());
    }

    private static boolean placePiece(WorldServer world, Piece piece, StructureBoundingBox bounds,
                                      boolean keepJigsaws, long seed, Map<BlockPos, Template.BlockInfo> beds) {
        if (piece.template instanceof FeatureTemplate && !FFDJigsawFeatures.isAreaLoaded(world, piece.origin)) return false;
        PlacementSettings settings = new BedPlacement(beds).setRotation(piece.rotation)
                .setIgnoreEntities(false).setBoundingBox(bounds).setSeed(seed);
        piece.template.addBlocksToWorld(world, piece.origin, (level, position, block) -> {
            if (block.blockState.getBlock() == Blocks.STRUCTURE_VOID) return null;
            if (!(block.blockState.getBlock() instanceof BlockJigsaw) || keepJigsaws) {
                return new Template.BlockInfo(block.pos, block.blockState,
                        block.tileentityData == null ? null : block.tileentityData.copy());
            }
            NBTTagCompound data = block.tileentityData;
            String finalState = data == null || !data.hasKey("final_state", 8)
                    ? "minecraft:air" : data.getString("final_state");
            IBlockState resolved = parseFinalState(finalState);
            return resolved.getBlock() == Blocks.STRUCTURE_VOID ? null
                    : new Template.BlockInfo(block.pos, resolved, finalStateData(finalState));
        }, settings, 2);
        applyFluids(world, piece.template, piece.origin, settings);
        placeBeds(world, beds);
        return true;
    }

    private static Template.BlockInfo deferBed(BlockPos position, Template.BlockInfo block, PlacementSettings settings) {
        if (!(settings instanceof BedPlacement)) return block;
        Map<BlockPos, Template.BlockInfo> beds = ((BedPlacement) settings).beds;
        if (!(block.blockState.getBlock() instanceof BlockBed) && !isAttachment(block.blockState)) {
            beds.remove(position);
            return block;
        }
        beds.put(position, new Template.BlockInfo(position, block.blockState,
                block.tileentityData == null ? null : block.tileentityData.copy()));
        return null;
    }

    private static boolean isAttachment(IBlockState state) {
        return state.getBlock() instanceof net.minecraft.block.BlockBush
                || state.getBlock() instanceof net.minecraft.block.BlockTorch
                || state.getBlock() instanceof net.minecraft.block.BlockCarpet;
    }

    private static BlockPos attachmentSupport(BlockPos position, IBlockState state) {
        return state.getBlock() instanceof net.minecraft.block.BlockTorch
                ? position.offset(state.getValue(net.minecraft.block.BlockTorch.FACING).getOpposite()) : position.down();
    }

    private static boolean canPlaceAttachment(World world, BlockPos position, IBlockState state) {
        if (state.getBlock() instanceof net.minecraft.block.BlockTorch) {
            return state.getBlock().canPlaceBlockOnSide(world, position,
                    state.getValue(net.minecraft.block.BlockTorch.FACING));
        }
        if (state.getBlock() instanceof net.minecraft.block.BlockBush) {
            return ((net.minecraft.block.BlockBush) state.getBlock()).canBlockStay(world, position, state);
        }
        return state.getBlock().canPlaceBlockAt(world, position);
    }

    private static void prepareSupport(World world, BlockPos position, IBlockState replacement) {
        if (world.getBlockState(position).equals(replacement)) return;
        for (EnumFacing direction : EnumFacing.VALUES) {
            BlockPos adjacent = position.offset(direction);
            if (!world.isBlockLoaded(adjacent)) continue;
            IBlockState attachment = world.getBlockState(adjacent);
            if (!isAttachment(attachment) || !attachmentSupport(adjacent, attachment).equals(position)) continue;
            boolean supported;
            if (attachment.getBlock() instanceof net.minecraftforge.common.IPlantable) {
                supported = replacement.getBlock().canSustainPlant(replacement, world, position, EnumFacing.UP,
                        (net.minecraftforge.common.IPlantable) attachment.getBlock());
            } else if (attachment.getBlock() instanceof net.minecraft.block.BlockTorch) {
                supported = direction == EnumFacing.UP ? replacement.getBlock().canPlaceTorchOnTop(replacement, world, position)
                        : replacement.isSideSolid(world, position, direction);
            } else {
                supported = !replacement.getBlock().isAir(replacement, world, position);
            }
            if (supported) continue;
            if (attachment.getBlock() instanceof net.minecraft.block.BlockDoublePlant
                    && world.getBlockState(adjacent.up()).getBlock() == attachment.getBlock()) {
                world.setBlockState(adjacent.up(), Blocks.AIR.getDefaultState(), 2);
            }
            world.setBlockState(adjacent, Blocks.AIR.getDefaultState(), 2);
        }
    }

    private static BlockPos bedPartner(Template.BlockInfo bed) {
        EnumFacing facing = bed.blockState.getValue(BlockBed.FACING);
        return bed.pos.offset(bed.blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT
                ? facing : facing.getOpposite());
    }

    private static boolean hasBedPartner(Template.BlockInfo bed, Map<BlockPos, Template.BlockInfo> beds) {
        Template.BlockInfo partner = beds.get(bedPartner(bed));
        return partner != null && partner.blockState.getBlock() == bed.blockState.getBlock()
                && partner.blockState.getValue(BlockBed.PART) != bed.blockState.getValue(BlockBed.PART)
                && partner.blockState.getValue(BlockBed.FACING) == bed.blockState.getValue(BlockBed.FACING);
    }

    private static boolean placeBeds(WorldServer world, Map<BlockPos, Template.BlockInfo> beds) {
        if (beds.isEmpty()) return false;
        boolean changed = false;
        List<Template.BlockInfo> deferred = new ArrayList<>(beds.values());
        deferred.sort(Comparator.comparingInt(block -> block.pos.getY()));
        for (Template.BlockInfo bed : deferred) {
            if (isAttachment(bed.blockState)) {
                BlockPos support = attachmentSupport(bed.pos, bed.blockState);
                if (!world.isBlockLoaded(bed.pos) || !world.isBlockLoaded(support)) continue;
                if (canPlaceAttachment(world, bed.pos, bed.blockState)) {
                    world.setBlockState(bed.pos, bed.blockState, 2);
                } else if (!world.getChunkFromBlockCoords(support).isTerrainPopulated()) {
                    continue;
                }
                beds.remove(bed.pos);
                changed = true;
                continue;
            }
            if (!beds.containsKey(bed.pos) || !hasBedPartner(bed, beds)
                    || !world.isBlockLoaded(bed.pos) || !world.isBlockLoaded(bedPartner(bed))) continue;
            Template.BlockInfo partner = beds.remove(bedPartner(bed));
            beds.remove(bed.pos);
            for (Template.BlockInfo half : new Template.BlockInfo[]{bed, partner}) {
                world.setBlockState(half.pos, half.blockState, 2);
                TileEntity tile = world.getTileEntity(half.pos);
                if (tile != null && half.tileentityData != null) {
                    NBTTagCompound data = half.tileentityData.copy();
                    data.setInteger("x", half.pos.getX());
                    data.setInteger("y", half.pos.getY());
                    data.setInteger("z", half.pos.getZ());
                    tile.readFromNBT(data);
                    tile.markDirty();
                    world.notifyBlockUpdate(half.pos, half.blockState, half.blockState, 2);
                }
            }
            world.notifyNeighborsRespectDebug(bed.pos, bed.blockState.getBlock(), false);
            world.notifyNeighborsRespectDebug(partner.pos, partner.blockState.getBlock(), false);
            changed = true;
        }
        return changed;
    }

    private static final class BedPlacement extends PlacementSettings {
        private final Map<BlockPos, Template.BlockInfo> beds;

        private BedPlacement(Map<BlockPos, Template.BlockInfo> beds) {
            this.beds = beds;
        }
    }

    public static final class SavedPiece extends StructureComponent {
        private String definition;
        private BlockPos origin;
        private Rotation rotation;
        private long seed;
        private int groundDelta = 1;
        private boolean rigid;
        private final List<BlockPos> junctions = new ArrayList<>();
        private transient Piece piece;
        private final Set<Long> placed = new HashSet<>();
        private final List<StructureBoundingBox> pending = new ArrayList<>();
        private final Map<BlockPos, Template.BlockInfo> beds = new HashMap<>();

        public SavedPiece() {
        }

        private SavedPiece(Piece piece, long seed) {
            super(0);
            this.piece = piece;
            this.definition = piece.element.definition.toString();
            this.origin = piece.origin;
            this.rotation = piece.rotation;
            this.seed = seed;
            this.boundingBox = piece.bounds;
            this.groundDelta = piece.groundDelta;
            this.rigid = !piece.element.terrainMatching;
            this.junctions.addAll(piece.junctions);
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
            tag.setString("Element", definition);
            tag.setLong("Origin", origin.toLong());
            tag.setString("Rotation", rotation.name());
            tag.setLong("Seed", seed);
            tag.setInteger("GroundDelta", groundDelta);
            tag.setBoolean("Rigid", rigid);
            NBTTagList connections = new NBTTagList();
            for (BlockPos junction : junctions) connections.appendTag(new net.minecraft.nbt.NBTTagLong(junction.toLong()));
            tag.setTag("Junctions", connections);
            NBTTagList completed = new NBTTagList();
            for (long key : placed) completed.appendTag(new net.minecraft.nbt.NBTTagLong(key));
            tag.setTag("Placed", completed);
            NBTTagList queue = new NBTTagList();
            for (StructureBoundingBox bounds : pending) queue.appendTag(bounds.toNBTTagIntArray());
            tag.setTag("Pending", queue);
            NBTTagList deferredBeds = new NBTTagList();
            for (Template.BlockInfo bed : beds.values()) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong("Pos", bed.pos.toLong());
                entry.setTag("State", NBTUtil.writeBlockState(new NBTTagCompound(), bed.blockState));
                if (bed.tileentityData != null) entry.setTag("Data", bed.tileentityData.copy());
                deferredBeds.appendTag(entry);
            }
            tag.setTag("Beds", deferredBeds);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            definition = tag.getString("Element");
            if (definition.length() > 1048576) throw new IllegalArgumentException("Oversized saved jigsaw element");
            origin = BlockPos.fromLong(tag.getLong("Origin"));
            rotation = Rotation.valueOf(tag.getString("Rotation"));
            seed = tag.getLong("Seed");
            groundDelta = tag.hasKey("GroundDelta", 99) ? tag.getInteger("GroundDelta") : 1;
            rigid = tag.getBoolean("Rigid");
            junctions.clear();
            NBTTagList connections = tag.getTagList("Junctions", 4);
            for (int index = 0; index < connections.tagCount(); index++) {
                junctions.add(BlockPos.fromLong(((net.minecraft.nbt.NBTTagLong) connections.get(index)).getLong()));
            }
            placed.clear();
            NBTTagList completed = tag.getTagList("Placed", 4);
            for (int index = 0; index < completed.tagCount(); index++) {
                placed.add(((net.minecraft.nbt.NBTTagLong) completed.get(index)).getLong());
            }
            pending.clear();
            NBTTagList queue = tag.getTagList("Pending", 11);
            for (int index = 0; index < queue.tagCount(); index++) {
                int[] coordinates = ((net.minecraft.nbt.NBTTagIntArray) queue.get(index)).getIntArray();
                if (coordinates.length != 6) throw new IllegalArgumentException("Invalid saved jigsaw bounds");
                pending.add(new StructureBoundingBox(coordinates));
            }
            piece = null;
            beds.clear();
            NBTTagList deferredBeds = tag.getTagList("Beds", 10);
            for (int index = 0; index < deferredBeds.tagCount(); index++) {
                NBTTagCompound entry = deferredBeds.getCompoundTagAt(index);
                IBlockState state = NBTUtil.readBlockState(entry.getCompoundTag("State"));
                if (!(state.getBlock() instanceof BlockBed) && !isAttachment(state)) continue;
                BlockPos position = BlockPos.fromLong(entry.getLong("Pos"));
                beds.put(position, new Template.BlockInfo(position, state,
                        entry.hasKey("Data", 10) ? entry.getCompoundTag("Data").copy() : null));
            }
        }

        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            if (!(world instanceof WorldServer)) return false;
            long key = columnKey(bounds.minX, bounds.minZ);
            if (placed.contains(key)) return true;
            if (tryPlace((WorldServer) world, bounds)) placed.add(key);
            else if (pending.stream().noneMatch(entry -> entry.minX == bounds.minX && entry.minZ == bounds.minZ)) {
                pending.add(new StructureBoundingBox(bounds.toNBTTagIntArray().getIntArray()));
            }
            return true;
        }

        boolean resume(WorldServer world) {
            boolean changed = false;
            java.util.Iterator<StructureBoundingBox> iterator = pending.iterator();
            while (iterator.hasNext()) {
                StructureBoundingBox bounds = iterator.next();
                if (!tryPlace(world, bounds)) continue;
                placed.add(columnKey(bounds.minX, bounds.minZ));
                iterator.remove();
                changed = true;
            }
            return placeBeds(world, beds) || changed;
        }

        boolean hasPending() {
            return !pending.isEmpty() || beds.values().stream().anyMatch(bed -> isAttachment(bed.blockState) || hasBedPartner(bed, beds));
        }

        void addTerrain(FFDStructureBeard.Builder builder) {
            if (rigid) builder.piece(boundingBox, groundDelta);
            for (BlockPos junction : junctions) builder.junction(junction);
        }

        private boolean tryPlace(WorldServer world, StructureBoundingBox bounds) {
            if (piece == null) {
                Element element = readElement(new JsonParser().parse(definition).getAsJsonObject(), 1, 0);
                Context context = new Context(world, origin, (blockX, blockZ) -> world.getHeight(blockX, blockZ));
                piece = new Piece(element, context.template(element), rotation, origin);
            }
            if (piece.template instanceof FeatureTemplate && !bounds.isVecInside(origin)) return true;
            return placePiece(world, piece, bounds, false, seed, beds);
        }
    }

    private static long columnKey(int blockX, int blockZ) {
        return (long) blockX << 32 ^ blockZ & 0xffffffffL;
    }

    private static final class Context {
        private final WorldServer world;
        private BlockPos center;
        private final IntBinaryOperator heights;
        private final IntBinaryOperator projectedTerrain;
        private final Map<Long, Integer> heightCache = new HashMap<>();
        private final CollisionSpace space;
        private final Map<ResourceLocation, Pool> pools = new HashMap<>();
        private final Map<ResourceLocation, Template> templates = new HashMap<>();
        private final Map<String, Template> preparedTemplates = new HashMap<>();
        private final Map<ResourceLocation, Integer> poolHeights = new HashMap<>();
        private final List<Piece> pieces = new ArrayList<>();
        private final List<StructureComponent> externalPieces = new ArrayList<>();
        private int maxDistance = MAX_DISTANCE;
        private int attempts;

        private Context(WorldServer world, BlockPos center, IntBinaryOperator heights) {
            this.world = world;
            this.center = center;
            this.heights = heights;
            IntBinaryOperator terrain = FFDStructureTerrain.projectedHeights(world);
            this.projectedTerrain = terrain == null ? world::getHeight : terrain;
            this.space = new CollisionSpace(null);
        }

        private int height(BlockPos position) {
            return heightCache.computeIfAbsent(columnKey(position.getX(), position.getZ()),
                    ignored -> heights.applyAsInt(position.getX(), position.getZ()));
        }

        private void expand(Piece piece) {
            if (piece.bounds.getYSize() > 16) return;
            int expansion = 0;
            for (Connector connector : piece.connectors(new Random(0L))) {
                if (!piece.bounds.isVecInside(connector.position.offset(connector.front))) continue;
                expansion = Math.max(expansion, poolHeight(connector.pool));
                expansion = Math.max(expansion, poolHeight(pool(connector.pool).fallback));
            }
            if (expansion > 0) piece.bounds.maxY = piece.bounds.minY
                    + Math.max(expansion + 1, piece.bounds.getYSize() - 1);
        }

        private int poolHeight(ResourceLocation id) {
            return poolHeights.computeIfAbsent(id, key -> {
                int height = 0;
                for (Element element : pool(key).elements) {
                    if (!element.isEmpty()) height = Math.max(height, template(element).getSize().getY());
                }
                return height;
            });
        }

        private Pool pool(ResourceLocation id) {
            if (EMPTY.equals(id)) return new Pool(EMPTY, Collections.emptyList());
            return pools.computeIfAbsent(id, key -> readPool(world, key));
        }

        private Template template(ResourceLocation id) {
            return templates.computeIfAbsent(id, key -> {
                Template template = readTemplate(world, key);
                BlockPos size = template.getSize();
                if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0
                        || size.getX() > 128 || size.getY() > 128 || size.getZ() > 128) {
                    throw new IllegalArgumentException("Missing or oversized jigsaw template " + key);
                }
                return template;
            });
        }

        private Template template(Element element) {
            if (element.feature != null) return new FeatureTemplate(element.feature);
            if (!element.children.isEmpty()) {
                List<Template> layers = new ArrayList<>();
                for (Element child : element.children) {
                    if (!child.isEmpty()) layers.add(template(child));
                }
                if (layers.isEmpty()) throw new IllegalArgumentException("Empty structure template group");
                return new LayeredTemplate(layers);
            }
            String key = element.location + "/" + element.ignoreAir + "/" + element.processors + "/" + element.terrainMatching;
            return preparedTemplates.computeIfAbsent(key, ignored -> new ProcessedTemplate(template(element.location),
                    FFDStructureProcessors.read(world, element.processors), element.ignoreAir, element.terrainMatching,
                    projectedTerrain));
        }

        private boolean fits(Piece piece, CollisionSpace space) {
            StructureBoundingBox bounds = piece.bounds;
            if (bounds.minX < center.getX() - maxDistance || bounds.maxX > center.getX() + maxDistance
                    || bounds.minZ < center.getZ() - maxDistance || bounds.maxZ > center.getZ() + maxDistance
                    || bounds.minY < Math.max(FFDHeightHooks.minY(world), center.getY() - maxDistance)
                    || bounds.maxY >= Math.min(FFDHeightHooks.maxYExclusive(world), center.getY() + maxDistance + 1)) return false;
            AxisAlignedBB candidate = new AxisAlignedBB(bounds.minX + 0.25D, bounds.minY + 0.25D, bounds.minZ + 0.25D,
                    bounds.maxX + 0.75D, bounds.maxY + 0.75D, bounds.maxZ + 0.75D);
            if (space.enclosing != null && (bounds.minX < space.enclosing.minX || bounds.maxX > space.enclosing.maxX
                    || bounds.minY < space.enclosing.minY || bounds.maxY > space.enclosing.maxY
                    || bounds.minZ < space.enclosing.minZ || bounds.maxZ > space.enclosing.maxZ)) return false;
            for (StructureBoundingBox other : space.occupied) {
                if (candidate.intersects(new AxisAlignedBB(other.minX, other.minY, other.minZ,
                        other.maxX + 1.0D, other.maxY + 1.0D, other.maxZ + 1.0D))) return false;
            }
            return true;
        }
    }

    private static Pool readPool(WorldServer world, ResourceLocation id) {
        String path = safePath(id);
        File root = new File(world.getSaveHandler().getWorldDirectory(), "data/ffd_template_pools");
        File file = new File(root, id.getResourceDomain() + "/" + path + ".json");
        String resource = "/assets/" + id.getResourceDomain() + "/template_pools/" + path + ".json";
        try (InputStream input = file.isFile() ? new FileInputStream(file)
                : FFDJigsawPlacement.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalArgumentException("Unknown template pool " + id);
            byte[] bytes = new byte[1048577];
            int used = 0;
            int count;
            while (used < bytes.length && (count = input.read(bytes, used, bytes.length - used)) != -1) used += count;
            if (used == bytes.length) throw new IllegalArgumentException("Oversized template pool " + id);
            JsonObject json = new JsonParser().parse(new String(bytes, 0, used, StandardCharsets.UTF_8)).getAsJsonObject();
            ResourceLocation fallback = json.has("fallback") ? new ResourceLocation(json.get("fallback").getAsString()) : EMPTY;
            JsonArray entries = json.getAsJsonArray("elements");
            if (entries == null || entries.size() > 1024) throw new IllegalArgumentException("Invalid pool elements " + id);
            List<Element> elements = new ArrayList<>();
            int totalWeight = 0;
            for (JsonElement entry : entries) {
                JsonObject weighted = entry.getAsJsonObject();
                int weight = weighted.get("weight").getAsInt();
                if (weight < 1 || weight > 150 || (totalWeight += weight) > 8192) throw new IllegalArgumentException("Invalid pool weight " + id);
                elements.add(readElement(weighted.getAsJsonObject("element"), weight, 0));
            }
            return new Pool(fallback, elements);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalArgumentException("Cannot load template pool " + id + ": " + exception.getMessage(), exception);
        }
    }

    private static Element readElement(JsonObject definition, int weight, int depth) {
        if (depth > 8) throw new IllegalArgumentException("Nested structure template group limit exceeded");
        String type = definition.get("element_type").getAsString();
        String projection = definition.has("projection") ? definition.get("projection").getAsString() : "rigid";
        if (!"rigid".equals(projection) && !"terrain_matching".equals(projection)) {
            throw new IllegalArgumentException("Unsupported pool projection " + projection);
        }
        List<Element> children = new ArrayList<>();
        ResourceLocation location = null;
        ResourceLocation feature = null;
        if ("minecraft:list_pool_element".equals(type)) {
            JsonArray elements = definition.getAsJsonArray("elements");
            if (elements == null || elements.size() == 0 || elements.size() > 64) throw new IllegalArgumentException("Invalid template group");
            for (JsonElement element : elements) {
                JsonObject child = new JsonParser().parse(element.toString()).getAsJsonObject();
                child.addProperty("projection", projection);
                children.add(readElement(child, 1, depth + 1));
            }
        } else if ("minecraft:single_pool_element".equals(type) || "minecraft:legacy_single_pool_element".equals(type)) {
            location = new ResourceLocation(definition.get("location").getAsString());
        } else if ("minecraft:feature_pool_element".equals(type)) {
            feature = new ResourceLocation(definition.get("feature").getAsString());
            FFDJigsawFeatures.requireSupported(feature);
        } else if (!"minecraft:empty_pool_element".equals(type)) throw new IllegalArgumentException("Unsupported pool element " + type);
        return new Element(location, weight, definition.get("processors"),
                "minecraft:legacy_single_pool_element".equals(type), children,
                "terrain_matching".equals(projection), feature, definition);
    }

    private static Template primary(Template template) {
        if (template instanceof ProcessedTemplate) return primary(((ProcessedTemplate) template).source);
        if (template instanceof LayeredTemplate) return primary(((LayeredTemplate) template).layers.get(0));
        return template;
    }

    @SuppressWarnings("unchecked")
    private static List<Template.BlockInfo> blocks(Template template) {
        try {
            return (List<Template.BlockInfo>) TEMPLATE_BLOCKS.get(primary(template));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot inspect structure template blocks", exception);
        }
    }

    private static Rotation inverse(Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 ? Rotation.COUNTERCLOCKWISE_90
                : rotation == Rotation.COUNTERCLOCKWISE_90 ? Rotation.CLOCKWISE_90 : rotation;
    }

    private static void applyFluids(World world, Template template, BlockPos origin, PlacementSettings settings) {
        if (template instanceof LayeredTemplate) {
            for (Template layer : ((LayeredTemplate) template).layers) applyFluids(world, layer, origin, settings);
            return;
        }
        Template source = primary(template);
        if (!(source instanceof LoadedTemplate)) return;
        NBTTagList fluids = ((LoadedTemplate) source).fluids;
        for (int index = 0; index < fluids.tagCount(); index++) {
            NBTTagCompound fluid = fluids.getCompoundTagAt(index);
            NBTTagList coordinates = fluid.getTagList("pos", 3);
            BlockPos position = origin.add(Template.transformedBlockPos(settings,
                    new BlockPos(coordinates.getIntAt(0), coordinates.getIntAt(1), coordinates.getIntAt(2))));
            StructureBoundingBox bounds = settings.getBoundingBox();
            if (bounds != null && (position.getX() < bounds.minX || position.getX() > bounds.maxX
                    || position.getZ() < bounds.minZ || position.getZ() > bounds.maxZ)) continue;
            if (template instanceof ProcessedTemplate && ((ProcessedTemplate) template).terrainMatching) {
                Integer surface = ((ProcessedTemplate) template).projectedHeights.get(columnKey(position.getX(), position.getZ()));
                if (surface == null) throw new IllegalArgumentException("Missing projected fluid column");
                position = new BlockPos(position.getX(), surface - 1 + coordinates.getIntAt(1), position.getZ());
            }
            if (bounds != null && !bounds.isVecInside(position)) continue;
            net.minecraftforge.fluids.Fluid contained = net.minecraftforge.fluids.FluidRegistry.getFluid(
                    new ResourceLocation(fluid.getString("fluid")).getResourcePath());
            if (contained != null) xy177.farmersfuturedelight.api.WaterloggedBlockApi.setContainedFluid(
                    world, position, world.getBlockState(position), contained, 2);
        }
    }

    private static final class ProcessedTemplate extends Template {
        private final Template source;
        private final List<FFDStructureProcessors.Processor> processors;
        private final boolean ignoreAir;
        private final boolean terrainMatching;
        private final IntBinaryOperator terrain;
        private final Map<Long, Integer> projectedHeights = new HashMap<>();

        private ProcessedTemplate(Template source, List<FFDStructureProcessors.Processor> processors,
                                  boolean ignoreAir, boolean terrainMatching, IntBinaryOperator terrain) {
            this.source = source;
            this.processors = processors;
            this.ignoreAir = ignoreAir;
            this.terrainMatching = terrainMatching;
            this.terrain = terrain;
        }

        @Override
        public BlockPos getSize() {
            return source.getSize();
        }

        @Override
        public void addBlocksToWorld(World world, BlockPos position, ITemplateProcessor processor, PlacementSettings settings, int flags) {
            if (terrainMatching) {
                placeProjected(world, position, processor, settings, flags);
                return;
            }
            source.addBlocksToWorld(world, position, (level, target, block) -> {
                if (ignoreAir && block.blockState.getBlock() == Blocks.AIR) return null;
                Template.BlockInfo result = processor == null ? block : processor.processBlock(level, target, block);
                if (result != null) result = new Template.BlockInfo(result.pos,
                        result.blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation()), result.tileentityData);
                for (FFDStructureProcessors.Processor step : processors) {
                    if (result == null) break;
                    result = step.apply(level, target, result);
                }
                if (result != null && result.blockState.getBlock() != Blocks.STRUCTURE_VOID) {
                    if (settings.getBoundingBox() != null && !settings.getBoundingBox().isVecInside(target)) return null;
                    result = deferBed(target, result, settings);
                    if (result != null) prepareSupport(level, target, result.blockState);
                }
                return result == null || result.blockState.getBlock() == Blocks.STRUCTURE_VOID ? null : new Template.BlockInfo(result.pos,
                        result.blockState.withRotation(inverse(settings.getRotation())).withMirror(settings.getMirror()), result.tileentityData);
            }, settings.copy().setIgnoreEntities(true), flags);
            FFDJigsawEntities.place(world, source, position, settings);
        }

        private void placeProjected(World world, BlockPos origin, ITemplateProcessor processor,
                                    PlacementSettings settings, int flags) {
            projectedHeights.clear();
            List<Template.BlockInfo> selected = new ArrayList<>();
            StructureBoundingBox bounds = settings.getBoundingBox();
            for (Template.BlockInfo block : blocks(source)) {
                BlockPos target = origin.add(Template.transformedBlockPos(settings, block.pos));
                if (bounds != null && (target.getX() < bounds.minX || target.getX() > bounds.maxX
                        || target.getZ() < bounds.minZ || target.getZ() > bounds.maxZ)) continue;
                int height = projectedHeights.computeIfAbsent(columnKey(target.getX(), target.getZ()),
                        ignored -> terrain.applyAsInt(target.getX(), target.getZ()));
                if (ignoreAir && block.blockState.getBlock() == Blocks.AIR) continue;
                Template.BlockInfo result = processor == null ? block : processor.processBlock(world, target, block);
                if (result == null) continue;
                result = new Template.BlockInfo(result.pos,
                        result.blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation()),
                        result.tileentityData == null ? null : result.tileentityData.copy());
                for (FFDStructureProcessors.Processor step : processors) {
                    if (result == null) break;
                    result = step.apply(world, target, result);
                }
                if (result == null || result.blockState.getBlock() == Blocks.STRUCTURE_VOID) continue;
                int projectedY = height - 1 + block.pos.getY();
                if (projectedY < FFDHeightHooks.minY(world) || projectedY >= FFDHeightHooks.maxYExclusive(world)) continue;
                BlockPos projectedPosition = new BlockPos(target.getX(), projectedY, target.getZ());
                if (bounds != null && !bounds.isVecInside(projectedPosition)) continue;
                result = deferBed(projectedPosition, result, settings);
                if (result == null) continue;
                prepareSupport(world, projectedPosition, result.blockState);
                selected.add(new Template.BlockInfo(new BlockPos(block.pos.getX(), projectedY - origin.getY(), block.pos.getZ()),
                        result.blockState.withRotation(inverse(settings.getRotation())).withMirror(settings.getMirror()),
                        result.tileentityData));
            }
            Template projected = new Template();
            projected.read(source.writeToNBT(new NBTTagCompound()));
            List<Template.BlockInfo> projectedBlocks = blocks(projected);
            projectedBlocks.clear();
            projectedBlocks.addAll(selected);
            projected.addBlocksToWorld(world, origin, (ITemplateProcessor) null, settings.copy().setIgnoreEntities(true), flags);
            FFDJigsawEntities.place(world, source, origin, settings);
        }
    }

    private static final class FeatureTemplate extends Template {
        private final ResourceLocation feature;

        private FeatureTemplate(ResourceLocation feature) {
            this.feature = feature;
        }

        @Override
        public BlockPos getSize() {
            return new BlockPos(1, 1, 1);
        }

        @Override
        public void addBlocksToWorld(World world, BlockPos position, ITemplateProcessor processor, PlacementSettings settings, int flags) {
            StructureBoundingBox bounds = settings.getBoundingBox();
            if (bounds == null || bounds.isVecInside(position)) {
                FFDJigsawFeatures.place(world, feature, position, settings.getRandom(position));
            }
        }
    }

    private static final class LayeredTemplate extends Template {
        private final List<Template> layers;

        private LayeredTemplate(List<Template> layers) {
            this.layers = layers;
        }

        @Override
        public BlockPos getSize() {
            int width = 0;
            int height = 0;
            int depth = 0;
            for (Template layer : layers) {
                BlockPos size = layer.getSize();
                width = Math.max(width, size.getX());
                height = Math.max(height, size.getY());
                depth = Math.max(depth, size.getZ());
            }
            return new BlockPos(width, height, depth);
        }

        @Override
        public void addBlocksToWorld(World world, BlockPos position, ITemplateProcessor processor, PlacementSettings settings, int flags) {
            for (Template layer : layers) layer.addBlocksToWorld(world, position, processor, settings, flags);
        }
    }

    private static String safePath(ResourceLocation id) {
        String path = id.getResourcePath();
        if (path.startsWith("/") || path.contains("..") || path.contains("\\")
                || ".".equals(id.getResourceDomain()) || "..".equals(id.getResourceDomain())) {
            throw new IllegalArgumentException("Invalid structure resource " + id);
        }
        return path;
    }

    private static Template readTemplate(WorldServer world, ResourceLocation id) {
        File directory = new File(world.getSaveHandler().getWorldDirectory(), "structures");
        return readTemplate(world, directory, id);
    }

    static Template readTemplate(TemplateManager manager, ResourceLocation id) {
        synchronized (MATERIAL_TEMPLATES) {
            Map<ResourceLocation, Template> templates = MATERIAL_TEMPLATES.computeIfAbsent(manager, ignored -> new HashMap<>());
            return templates.computeIfAbsent(id, key -> {
                try {
                    return readTemplate(null, new File((String) TEMPLATE_FOLDER.get(manager)), key);
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException("Cannot read structure template directory", exception);
                }
            });
        }
    }

    private static Template readTemplate(WorldServer world, File directory, ResourceLocation id) {
        String path = safePath(id);
        File file = new File(directory, id.getResourceDomain() + "/" + path + ".nbt");
        if (!file.isFile() && (world == null || "minecraft".equals(id.getResourceDomain()))) file = new File(directory, path + ".nbt");
        String resource = "/assets/" + id.getResourceDomain() + "/structures/" + path + ".nbt";
        try (InputStream input = file.isFile() ? new FileInputStream(file)
                : FFDJigsawPlacement.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalArgumentException("Unknown structure template " + id);
            try (DataInputStream compressed = new DataInputStream(new GZIPInputStream(input))) {
                NBTTagCompound data = CompressedStreamTools.read(compressed, new NBTSizeTracker(16777216L));
                NBTTagList palette = data.getTagList("palette", 10);
                IBlockState[] states = new IBlockState[palette.tagCount()];
                boolean[] replaced = new boolean[palette.tagCount()];
                for (int index = 0; index < palette.tagCount(); index++) {
                    NBTTagCompound entry = palette.getCompoundTagAt(index);
                    states[index] = parseFinalState(stateString(entry));
                    replaced[index] = !entry.getString("Name").equals(states[index].getBlock().getRegistryName().toString());
                    palette.set(index, NBTUtil.writeBlockState(new NBTTagCompound(), states[index]));
                }
                NBTTagList blocks = data.getTagList("blocks", 10);
                for (int index = 0; index < blocks.tagCount(); index++) {
                    NBTTagCompound block = blocks.getCompoundTagAt(index);
                    int state = block.getInteger("state");
                    if (state < 0 || state >= states.length) throw new IllegalArgumentException("Invalid template palette index " + state);
                    if (!replaced[state] || !block.hasKey("nbt", 10)) continue;
                    NBTTagCompound tile = FFDStructureBlockStates.tileData(world, states[state], block.getCompoundTag("nbt"));
                    if (tile == null) block.removeTag("nbt");
                    else block.setTag("nbt", tile);
                }
                LoadedTemplate template = new LoadedTemplate(data.getTagList("ffd_contained_fluids", 10));
                template.read(data);
                return template;
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalArgumentException("Cannot load template " + id + ": " + exception.getMessage(), exception);
        }
    }

    private static String stateString(NBTTagCompound entry) {
        StringBuilder state = new StringBuilder(entry.getString("Name"));
        NBTTagCompound properties = entry.getCompoundTag("Properties");
        if (!properties.hasNoTags()) {
            state.append('[');
            boolean first = true;
            for (String property : properties.getKeySet()) {
                if (!first) state.append(',');
                state.append(property).append('=').append(properties.getString(property));
                first = false;
            }
            state.append(']');
        }
        return state.toString();
    }

    private static final class LoadedTemplate extends Template {
        private final NBTTagList fluids;

        private LoadedTemplate(NBTTagList fluids) {
            this.fluids = fluids;
        }
    }

    private static final class Pool {
        private final ResourceLocation fallback;
        private final List<Element> elements;

        private Pool(ResourceLocation fallback, List<Element> elements) {
            this.fallback = fallback;
            this.elements = elements;
        }

        private List<Element> shuffled(Random random) {
            List<Element> choices = new ArrayList<>();
            for (Element element : elements) {
                for (int index = 0; index < element.weight; index++) choices.add(element);
            }
            Collections.shuffle(choices, random);
            return choices;
        }

        private Element random(Random random) {
            int weight = 0;
            for (Element element : elements) weight += element.weight;
            if (weight == 0) return null;
            int selected = random.nextInt(weight);
            for (Element element : elements) {
                selected -= element.weight;
                if (selected < 0) return element;
            }
            throw new IllegalStateException("Invalid jigsaw pool weight");
        }
    }

    private static final class ExternalCandidate {
        private final net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight;
        private int copies;
        private final long remainder;

        private ExternalCandidate(net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight,
                                  int copies, long remainder) {
            this.weight = weight;
            this.copies = copies;
            this.remainder = remainder;
        }
    }

    private static final class Element {
        private final ResourceLocation location;
        private final int weight;
        private final JsonElement processors;
        private final boolean ignoreAir;
        private final List<Element> children;
        private final boolean terrainMatching;
        private final ResourceLocation feature;
        private final JsonObject definition;
        private net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight external;

        private Element(ResourceLocation location, int weight, JsonElement processors, boolean ignoreAir,
                        List<Element> children, boolean terrainMatching, ResourceLocation feature, JsonObject definition) {
            this.location = location;
            this.weight = weight;
            this.processors = processors;
            this.ignoreAir = ignoreAir;
            this.children = children;
            this.terrainMatching = terrainMatching;
            this.feature = feature;
            this.definition = definition;
        }

        private boolean isEmpty() {
            return location == null && feature == null && external == null && children.isEmpty();
        }

        private static Element external(net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight weight) {
            Element result = new Element(null, 1, null, false, Collections.emptyList(), false, null, new JsonObject());
            result.external = weight;
            return result;
        }
    }

    private static final class CollisionSpace {
        private final StructureBoundingBox enclosing;
        private final List<StructureBoundingBox> occupied = new ArrayList<>();

        private CollisionSpace(StructureBoundingBox enclosing) {
            this.enclosing = enclosing;
        }
    }

    private static final class Piece {
        private final Element element;
        private final Template template;
        private final Rotation rotation;
        private final BlockPos origin;
        private final StructureBoundingBox bounds;
        private CollisionSpace space;
        private CollisionSpace inside;
        private int groundDelta = 1;
        private final List<BlockPos> junctions = new ArrayList<>();

        private Piece(Element element, Template template, Rotation rotation, BlockPos origin) {
            this.element = element;
            this.template = template;
            this.rotation = rotation;
            this.origin = origin;
            BlockPos corner = origin.add(Template.transformedBlockPos(settings(), template.getSize().add(-1, -1, -1)));
            bounds = new StructureBoundingBox(Math.min(origin.getX(), corner.getX()), Math.min(origin.getY(), corner.getY()),
                    Math.min(origin.getZ(), corner.getZ()), Math.max(origin.getX(), corner.getX()),
                    Math.max(origin.getY(), corner.getY()), Math.max(origin.getZ(), corner.getZ()));
        }

        private PlacementSettings settings() {
            return new PlacementSettings().setRotation(rotation).setMirror(Mirror.NONE);
        }

        private List<Connector> connectors(Random random) {
            if (template instanceof FeatureTemplate) {
                return Collections.singletonList(new Connector(origin, BlockJigsaw.Orientation.DOWN_SOUTH,
                        new NBTTagCompound(), true));
            }
            List<Template.BlockInfo> blocks = blocks(template);
            List<Connector> result = new ArrayList<>();
            PlacementSettings settings = settings();
            for (Template.BlockInfo block : blocks) {
                if (!(block.blockState.getBlock() instanceof BlockJigsaw)) continue;
                NBTTagCompound data = block.tileentityData == null ? new NBTTagCompound() : block.tileentityData;
                BlockJigsaw.Orientation orientation = block.blockState.withRotation(rotation).getValue(BlockJigsaw.ORIENTATION);
                result.add(new Connector(origin.add(Template.transformedBlockPos(settings, block.pos)), orientation, data));
            }
            Collections.shuffle(result, random);
            result.sort(Comparator.comparingInt((Connector connector) -> connector.selectionPriority).reversed());
            return result;
        }

        private CollisionSpace childSpace(Connector connector) {
            if (!bounds.isVecInside(connector.position.offset(connector.front))) return space;
            if (inside == null) inside = new CollisionSpace(bounds);
            return inside;
        }
    }

    private static final class Connector {
        private final BlockPos position;
        private final EnumFacing front;
        private final EnumFacing top;
        private final ResourceLocation name;
        private final ResourceLocation target;
        private final ResourceLocation pool;
        private final boolean aligned;
        private final String finalState;
        private final int selectionPriority;
        private final int placementPriority;

        private Connector(BlockPos position, BlockJigsaw.Orientation orientation, NBTTagCompound data) {
            this(position, orientation, data, false);
        }

        private Connector(BlockPos position, BlockJigsaw.Orientation orientation, NBTTagCompound data, boolean wildcard) {
            this.position = position;
            front = orientation.front;
            top = orientation.top;
            name = wildcard ? null : identifier(data, "name");
            target = identifier(data, "target");
            pool = identifier(data, "pool");
            aligned = data.hasKey("joint", 8) ? "aligned".equals(data.getString("joint")) : front.getAxis() != EnumFacing.Axis.Y;
            finalState = data.hasKey("final_state", 8) ? data.getString("final_state") : "minecraft:air";
            selectionPriority = data.getInteger("selection_priority");
            placementPriority = data.getInteger("placement_priority");
        }

        private boolean canAttach(Connector other) {
            return front == other.front.getOpposite() && (!aligned || top == other.top)
                    && (other.name == null || target.equals(other.name));
        }

        private static ResourceLocation identifier(NBTTagCompound data, String key) {
            return data.hasKey(key, 8) && !data.getString(key).isEmpty() ? new ResourceLocation(data.getString(key)) : EMPTY;
        }
    }

    private static final class Expansion {
        private final Piece parent;
        private final int depth;
        private final int priority;
        private final long order;

        private Expansion(Piece parent, int depth, int priority, long order) {
            this.parent = parent;
            this.depth = depth;
            this.priority = priority;
            this.order = order;
        }
    }
}
