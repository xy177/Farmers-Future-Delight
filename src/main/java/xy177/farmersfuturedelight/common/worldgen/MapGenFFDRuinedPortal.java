package xy177.farmersfuturedelight.common.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.function.IntBinaryOperator;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDConfig;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.world.nether.NetherGeneration;
import xy177.farmersfuturedelight.common.world.nether.NetherNoise;
import xy177.farmersfuturedelight.core.FFDHeightHooks;

@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID)
public final class MapGenFFDRuinedPortal extends MapGenStructure {
    private static final String NAME = "FFDRuinedPortal";
    private static final int SPACING = 40;
    private static final int SEPARATION = 15;
    private static final int SALT = 34222645;
    private static final double[] SPREAD = {1, 1, 1, 1, 1, 1, 1, .9, .9, .8, .7, .6, .4, .2};
    private static final Map<World, MapGenFFDRuinedPortal> GENERATORS = new WeakHashMap<>();

    private MapGenFFDRuinedPortal() { range = 3; }

    static void init() {
        MapGenStructureIO.registerStructure(Start.class, NAME);
        MapGenStructureIO.registerStructureComponent(Piece.class, "FFDRuinedPortalPiece");
    }

    private static boolean enabled(World world) {
        return FFDConfig.ruinedPortalMode != FFDConfig.FeatureMode.DISABLED
                && world instanceof WorldServer && world.getWorldInfo().isMapFeaturesEnabled()
                && (NetherGeneration.isModern(world) || FFDModernStructures.supported(world));
    }

    public static boolean matches(String name) {
        return NAME.equals(name) || "Ruined_Portal".equals(name) || "ruined_portal".equals(name)
                || "minecraft:ruined_portal".equals(name) || "farmers_future_delight:ruined_portal".equals(name);
    }

    public static void generate(World world, int chunkX, int chunkZ) {
        if (!enabled(world)) return;
        MapGenFFDRuinedPortal generator = GENERATORS.computeIfAbsent(world, ignored -> new MapGenFFDRuinedPortal());
        generator.generate(world, chunkX, chunkZ, null);
        generator.generateStructure(world, new Random(world.getSeed() ^ (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L ^ SALT), new ChunkPos(chunkX, chunkZ));
    }

    @Nullable public static BlockPos locate(World world, BlockPos position, boolean unexplored) {
        if (!enabled(world)) return null;
        return GENERATORS.computeIfAbsent(world, ignored -> new MapGenFFDRuinedPortal())
                .getNearestStructurePos(world, position, unexplored);
    }

    public static boolean inside(World world, BlockPos position) {
        if (!enabled(world)) return false;
        MapGenFFDRuinedPortal generator = GENERATORS.computeIfAbsent(world, ignored -> new MapGenFFDRuinedPortal());
        generator.generate(world, position.getX() >> 4, position.getZ() >> 4, null);
        return generator.isInsideStructure(position);
    }

    @SubscribeEvent public static void unload(WorldEvent.Unload event) { GENERATORS.remove(event.getWorld()); }

    @Override public String getStructureName() { return NAME; }

    @Override protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, SPACING);
        int regionZ = Math.floorDiv(chunkZ, SPACING);
        Random random = new Random(world.getSeed() + (long) regionX * 341873128712L + (long) regionZ * 132897987541L + SALT);
        return chunkX == regionX * SPACING + random.nextInt(SPACING - SEPARATION)
                && chunkZ == regionZ * SPACING + random.nextInt(SPACING - SEPARATION);
    }

    @Override public BlockPos getNearestStructurePos(World world, BlockPos position, boolean unexplored) {
        this.world = world;
        return findNearestStructurePosBySpacing(world, this, position, SPACING, SEPARATION, SALT, false, 100, unexplored);
    }

    @Override protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start((WorldServer) world, chunkX, chunkZ);
    }

    private enum Site {
        LAND, UNDERGROUND, MOUNTAIN, DESERT, JUNGLE, OCEAN, SWAMP, NETHER;

        boolean floor() { return this == OCEAN || this == SWAMP; }
        boolean airPocket() { return this == UNDERGROUND || this == MOUNTAIN; }
        boolean followSurface() { return this == LAND || this == JUNGLE || floor(); }
        boolean vines() { return this == JUNGLE || this == SWAMP; }
        float moss() { return this == JUNGLE || this == OCEAN ? .8F : this == SWAMP ? .5F : this == DESERT || this == NETHER ? 0 : .2F; }
    }

    public static final class Start extends StructureStart {
        public Start() { }

        private Start(WorldServer world, int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            Random random = new Random(world.getSeed());
            random.setSeed((long) chunkX * random.nextLong() ^ (long) chunkZ * random.nextLong() ^ world.getSeed());
            BlockPos base = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            Biome biome = world.getBiomeProvider().getBiome(base.add(8, 0, 8));
            Site site = selectSite(world, biome, random);
            String name = random.nextFloat() < .05F ? "giant_portal_" + (1 + random.nextInt(3)) : "portal_" + (1 + random.nextInt(10));
            TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
            Template template = load(manager, name);
            Rotation rotation = Rotation.values()[random.nextInt(4)];
            Mirror mirror = random.nextFloat() < .5F ? Mirror.NONE : Mirror.FRONT_BACK;
            BlockPos size = template.getSize();
            BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
            BlockPos origin = base.add(pivot.subtract(Template.transformedBlockPos(new PlacementSettings().setRotation(rotation), pivot)));
            StructureBoundingBox box = templateBounds(origin, size, rotation, mirror);
            int height = height(world, random, site, box);
            BlockPos middle = center(box);
            boolean cold = site != Site.NETHER && site != Site.JUNGLE && site != Site.DESERT && site != Site.SWAMP
                    && biome.getTemperature(new BlockPos(middle.getX(), height, middle.getZ())) < .15F;
            components.add(new Piece(template, name, origin.up(height), rotation, mirror, site, cold, random.nextLong()));
            updateBoundingBox();
        }

        @Override public void generateStructure(World world, Random random, StructureBoundingBox bounds) {
            super.generateStructure(world, random, new StructureBoundingBox(bounds.minX, FFDHeightHooks.minY(world),
                    bounds.minZ, bounds.maxX, FFDHeightHooks.maxYExclusive(world) - 1, bounds.maxZ));
        }

        private static Site selectSite(World world, Biome biome, Random random) {
            if (world.provider.getDimension() == -1) return Site.NETHER;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) return Site.OCEAN;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) return Site.SWAMP;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) return Site.JUNGLE;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY)
                    && BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)
                    && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA)) return Site.DESERT;
            boolean mountain = BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.HILLS) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA);
            return random.nextFloat() >= .5F ? Site.LAND : mountain ? Site.MOUNTAIN : Site.UNDERGROUND;
        }

        private static int height(WorldServer world, Random random, Site site, StructureBoundingBox box) {
            int minimum = FFDHeightHooks.minY(world) + 15;
            BlockPos center = center(box);
            if (site == Site.NETHER) {
                NetherNoise noise = NetherGeneration.source(world).noise();
                Map<Long, float[]> lattices = new HashMap<>();
                int height = random.nextFloat() < .5F ? inclusive(random, 27, 29) : inclusive(random, 29, 100);
                for (; height > minimum; height--) {
                    int supported = 0;
                    for (int blockX : new int[]{box.minX, box.maxX}) for (int blockZ : new int[]{box.minZ, box.maxZ}) {
                        float[] lattice = lattices.computeIfAbsent(ChunkPos.asLong(blockX >> 4, blockZ >> 4),
                                ignored -> noise.lattice(blockX >> 4, blockZ >> 4));
                        if (height < NetherGeneration.SEA_LEVEL || NetherNoise.density(lattice, blockX & 15, height, blockZ & 15) > 0) supported++;
                    }
                    if (supported >= 3) break;
                }
                return height;
            }
            IntBinaryOperator heights = site.floor() ? FFDStructureTerrain.floorHeights(world) : FFDStructureTerrain.heights(world);
            int surface = heights.applyAsInt(center.getX(), center.getZ()) - 1;
            int maximum = surface - box.getYSize();
            int height = site == Site.DESERT ? maximum + inclusive(random, 2, 8)
                    : site.airPocket() ? inclusive(random, site == Site.MOUNTAIN ? 70 : minimum, maximum) : surface;
            java.util.function.BiFunction<Integer, Integer, IBlockState[]> sampler = FFDStructureTerrain.columns(world);
            IBlockState[][] columns = {sampler.apply(box.minX, box.minZ), sampler.apply(box.minX, box.maxZ),
                    sampler.apply(box.maxX, box.minZ), sampler.apply(box.maxX, box.maxZ)};
            int bottom = FFDHeightHooks.minY(world);
            for (; height > minimum; height--) {
                int supported = 0;
                for (IBlockState[] column : columns) {
                    int offset = height - bottom;
                    if (column == null || offset < 0 || offset >= column.length) continue;
                    Material material = column[offset].getMaterial();
                    if (site.floor() ? material.blocksMovement() : material != Material.AIR) supported++;
                }
                if (supported >= 3) break;
            }
            return height;
        }
    }

    public static final class Piece extends StructureComponent {
        private Template template;
        private String name;
        private BlockPos origin;
        private Rotation rotation;
        private Mirror mirror;
        private Site site;
        private boolean cold;
        private long seed;
        private StructureBoundingBox frame;

        public Piece() { }

        private Piece(Template template, String name, BlockPos origin, Rotation rotation, Mirror mirror, Site site, boolean cold, long seed) {
            super(0);
            this.template = template;
            this.name = name;
            this.origin = origin;
            this.rotation = rotation;
            this.mirror = mirror;
            this.site = site;
            this.cold = cold;
            this.seed = seed;
            updateBounds();
        }

        private void updateBounds() {
            frame = templateBounds(origin, template.getSize(), rotation, mirror);
            BlockPos center = center(frame);
            int radius = SPREAD.length;
            boundingBox = new StructureBoundingBox(Math.min(frame.minX - 1, center.getX() - radius), frame.minY - 12,
                    Math.min(frame.minZ - 1, center.getZ() - radius), Math.max(frame.maxX + 1, center.getX() + radius),
                    frame.maxY + 1, Math.max(frame.maxZ + 1, center.getZ() + radius));
        }

        @Override protected void writeStructureToNBT(NBTTagCompound tag) {
            tag.setString("Template", name);
            tag.setLong("Origin", origin.toLong());
            tag.setString("Rotation", rotation.name());
            tag.setString("Mirror", mirror.name());
            tag.setString("Site", site.name());
            tag.setBoolean("Cold", cold);
            tag.setLong("Seed", seed);
        }

        @Override protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            name = tag.getString("Template");
            origin = BlockPos.fromLong(tag.getLong("Origin"));
            rotation = Rotation.valueOf(tag.getString("Rotation"));
            mirror = Mirror.valueOf(tag.getString("Mirror"));
            site = Site.valueOf(tag.getString("Site"));
            cold = tag.getBoolean("Cold");
            seed = tag.getLong("Seed");
            template = load(manager, name);
            updateBounds();
        }

        @Override public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(mirror)
                    .setBoundingBox(bounds).setIgnoreEntities(true).setSeed(seed);
            template.addBlocksToWorld(world, origin, (level, position, block) -> {
                if (!bounds.isVecInside(position) || protectedBlock(level.getBlockState(position))) return null;
                IBlockState state = block.blockState;
                if (state.getBlock() == Blocks.STRUCTURE_VOID || state.getBlock() == Blocks.STRUCTURE_BLOCK
                        || state.getBlock() == Blocks.AIR && !site.airPocket()) return null;
                Random aging = new Random(positionSeed(position));
                if (state.getMaterial() == Material.LAVA && site.floor()) state = Blocks.MAGMA.getDefaultState();
                else if (state.getMaterial() == Material.LAVA && cold) state = Blocks.NETHERRACK.getDefaultState();
                else if (state.getMaterial() == Material.LAVA && site != Site.NETHER && aging.nextFloat() < .2F) state = Blocks.MAGMA.getDefaultState();
                state = age(state, aging);
                if (level.getBlockState(position).getMaterial() == Material.LAVA && !state.isFullCube()) state = Blocks.LAVA.getDefaultState();
                return new Template.BlockInfo(block.pos, state, block.tileentityData == null ? null : block.tileentityData.copy());
            }, settings, 2);
            spread(world, bounds);
            return true;
        }

        private IBlockState age(IBlockState state, Random random) {
            if (state.getBlock() == Blocks.GOLD_BLOCK && random.nextFloat() < .3F) return Blocks.AIR.getDefaultState();
            if (state.getBlock() == Blocks.NETHERRACK && !cold && random.nextFloat() < .07F) return Blocks.MAGMA.getDefaultState();
            if (state.getBlock() == Blocks.OBSIDIAN && random.nextFloat() < .15F) return material(FFDBlocks.CRYING_OBSIDIAN, state);
            if ((state.getBlock() == Blocks.STONEBRICK && (state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT
                    || state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.CHISELED)
                    || state.getBlock() == Blocks.STONE) && random.nextFloat() < .5F) {
                IBlockState normalStair = randomStair(Blocks.STONE_BRICK_STAIRS.getDefaultState(), random);
                IBlockState mossyStair = randomStair(decor("mossy_stone_brick_stairs", Blocks.STONE_BRICK_STAIRS.getDefaultState()), random);
                boolean mossy = random.nextFloat() < site.moss();
                state = random.nextBoolean() ? mossy ? mossyStair : normalStair
                        : Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT,
                                mossy ? BlockStoneBrick.EnumType.MOSSY : BlockStoneBrick.EnumType.CRACKED);
            } else if (state.getBlock() instanceof BlockStairs && random.nextFloat() < .5F) {
                if (random.nextFloat() < site.moss()) {
                    state = random.nextBoolean() ? copyStair(state, decor("mossy_stone_brick_stairs", state))
                            : decor("mossy_stone_brick_slab", Blocks.STONE_SLAB.getStateFromMeta(5));
                } else state = random.nextBoolean() ? decor("stone_slab", Blocks.STONE_SLAB.getDefaultState()) : Blocks.STONE_SLAB.getStateFromMeta(5);
            } else if (state.getBlock() instanceof BlockSlab && random.nextFloat() < site.moss()) {
                IBlockState mossy = decor(((BlockSlab) state.getBlock()).isDouble() ? "mossy_stone_brick_double_slab"
                        : "mossy_stone_brick_slab", state);
                state = mossy.getPropertyKeys().contains(BlockSlab.HALF) ? mossy.withProperty(BlockSlab.HALF, state.getValue(BlockSlab.HALF)) : mossy;
            } else if (state.getBlock() instanceof net.minecraft.block.BlockWall && random.nextFloat() < site.moss()) {
                state = decor("mossy_stone_brick_wall", state);
            }
            if (site != Site.NETHER) return state;
            if (state.getBlock() == Blocks.STONE) return material(FFDBlocks.POLISHED_BLACKSTONE, state);
            if (state.getBlock() == Blocks.STONEBRICK) {
                BlockStoneBrick.EnumType variant = state.getValue(BlockStoneBrick.VARIANT);
                return material(variant == BlockStoneBrick.EnumType.CRACKED ? FFDBlocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                        : variant == BlockStoneBrick.EnumType.CHISELED ? FFDBlocks.CHISELED_POLISHED_BLACKSTONE
                        : FFDBlocks.POLISHED_BLACKSTONE_BRICKS, state);
            }
            String path = state.getBlock().getRegistryName().getResourcePath();
            if (state.getBlock() == Blocks.STONE_BRICK_STAIRS || "mossy_stone_brick_stairs".equals(path)) {
                return copyStair(state, material(FFDBlocks.POLISHED_BLACKSTONE_BRICK_STAIRS, state));
            }
            if (state.getBlock() instanceof BlockSlab) {
                boolean brick = "mossy_stone_brick_slab".equals(path) || "mossy_stone_brick_double_slab".equals(path)
                        || (state.getBlock() == Blocks.STONE_SLAB || state.getBlock() == Blocks.DOUBLE_STONE_SLAB)
                        && state.getValue(net.minecraft.block.BlockStoneSlab.VARIANT).getMetadata() == 5;
                boolean doubled = ((BlockSlab) state.getBlock()).isDouble();
                IBlockState replacement = material(brick ? doubled ? FFDBlocks.POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB : FFDBlocks.POLISHED_BLACKSTONE_BRICK_SLAB
                        : doubled ? FFDBlocks.POLISHED_BLACKSTONE_DOUBLE_SLAB : FFDBlocks.POLISHED_BLACKSTONE_SLAB, state);
                return !doubled && replacement.getPropertyKeys().contains(BlockSlab.HALF)
                        ? replacement.withProperty(BlockSlab.HALF, state.getValue(BlockSlab.HALF)) : replacement;
            }
            if ("stone_brick_wall".equals(path) || "mossy_stone_brick_wall".equals(path)) return material(FFDBlocks.POLISHED_BLACKSTONE_BRICK_WALL, state);
            if (state.getBlock() == Blocks.IRON_BARS) return material(FFDBlocks.IRON_CHAIN, state);
            return state;
        }

        private static IBlockState decor(String name, IBlockState fallback) {
            try { return FFDJigsawPlacement.parseFinalState("farmers_future_delight:" + name); }
            catch (IllegalArgumentException exception) { return fallback; }
        }

        private static IBlockState randomStair(IBlockState state, Random random) {
            return state.withProperty(BlockStairs.FACING, EnumFacing.HORIZONTALS[random.nextInt(4)])
                    .withProperty(BlockStairs.HALF, random.nextBoolean() ? BlockStairs.EnumHalf.TOP : BlockStairs.EnumHalf.BOTTOM);
        }

        private static IBlockState copyStair(IBlockState original, IBlockState replacement) {
            return replacement.withProperty(BlockStairs.FACING, original.getValue(BlockStairs.FACING))
                    .withProperty(BlockStairs.HALF, original.getValue(BlockStairs.HALF));
        }

        private void spread(World world, StructureBoundingBox bounds) {
            BlockPos center = center(frame);
            int offset = new Random(seed).nextInt(Math.max(1, 8 - (frame.getXSize() + frame.getZSize()) / 4));
            for (int blockX = Math.max(bounds.minX, boundingBox.minX); blockX <= Math.min(bounds.maxX, boundingBox.maxX); blockX++) {
                for (int blockZ = Math.max(bounds.minZ, boundingBox.minZ); blockZ <= Math.min(bounds.maxZ, boundingBox.maxZ); blockZ++) {
                    int distance = Math.abs(blockX - center.getX()) + Math.abs(blockZ - center.getZ()) + offset;
                    BlockPos floor = new BlockPos(blockX, frame.minY, blockZ);
                    Random random = new Random(positionSeed(floor));
                    if (distance < SPREAD.length && random.nextDouble() < SPREAD[distance]) {
                        BlockPos surface = site == Site.NETHER ? floor : findSurface(world, floor);
                        if (!site.followSurface() && surface.getY() > floor.getY()) surface = floor;
                        if (Math.abs(surface.getY() - floor.getY()) <= 3 && replaceable(world.getBlockState(surface))) {
                            placeGround(world, random, surface);
                            drip(world, random, surface.down());
                            if (site == Site.JUNGLE && random.nextBoolean()) leaves(world, surface.up());
                        }
                    }
                    if (blockX > frame.minX && blockX < frame.maxX && blockZ > frame.minZ && blockZ < frame.maxZ
                            && world.getBlockState(floor).getBlock() == Blocks.NETHERRACK) drip(world, random, floor.down());
                    if (!site.vines() || blockX < frame.minX || blockX > frame.maxX || blockZ < frame.minZ || blockZ > frame.maxZ) continue;
                    for (int height = frame.minY; height <= frame.maxY; height++) {
                        BlockPos support = new BlockPos(blockX, height, blockZ);
                        EnumFacing side = EnumFacing.HORIZONTALS[random.nextInt(4)];
                        BlockPos adjacent = support.offset(side);
                        if (bounds.isVecInside(adjacent) && world.isAirBlock(adjacent)
                                && world.getBlockState(support).isSideSolid(world, support, side)) {
                            world.setBlockState(adjacent, Blocks.VINE.getDefaultState()
                                    .withProperty(BlockVine.getPropertyFor(side.getOpposite()), true), 2);
                        }
                        if (site == Site.JUNGLE && random.nextBoolean() && world.getBlockState(support).getBlock() == Blocks.NETHERRACK) leaves(world, support.up());
                    }
                }
            }
        }

        private BlockPos findSurface(World world, BlockPos base) {
            int height = site.floor() ? world.getTopSolidOrLiquidBlock(base).getY() : world.getHeight(base).getY();
            return new BlockPos(base.getX(), height - 1, base.getZ());
        }

        private void drip(World world, Random random, BlockPos start) {
            placeGround(world, random, start);
            for (int depth = 1; depth <= 8 && random.nextFloat() < .5F; depth++) placeGround(world, random, start.down(depth));
        }

        private void placeGround(World world, Random random, BlockPos position) {
            if (position.getY() >= FFDHeightHooks.minY(world) && replaceable(world.getBlockState(position))) {
                world.setBlockState(position, (!cold && random.nextFloat() < .07F ? Blocks.MAGMA : Blocks.NETHERRACK).getDefaultState(), 2);
            }
        }

        private boolean replaceable(IBlockState state) {
            return state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.OBSIDIAN
                    && state.getBlock() != FFDBlocks.CRYING_OBSIDIAN && !protectedBlock(state)
                    && (site == Site.NETHER || state.getMaterial() != Material.LAVA);
        }

        private void leaves(World world, BlockPos position) {
            if (world.isAirBlock(position)) world.setBlockState(position, Blocks.LEAVES.getStateFromMeta(7)
                    .withProperty(BlockLeaves.CHECK_DECAY, false), 2);
        }

        private long positionSeed(BlockPos position) {
            long mixed = (long) (position.getX() * 3129871) ^ (long) position.getZ() * 116129781L ^ position.getY();
            return seed ^ (mixed * mixed * 42317861L + mixed * 11L);
        }
    }

    private static boolean protectedBlock(IBlockState state) {
        return state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.MOB_SPAWNER
                || state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.END_PORTAL_FRAME;
    }

    private static IBlockState material(net.minecraft.block.Block block, IBlockState fallback) {
        IBlockState state = FFDItems.effectiveBlockState(block);
        return state == null ? fallback : state;
    }

    private static int inclusive(Random random, int low, int high) {
        return low < high ? low + random.nextInt(high - low + 1) : high;
    }

    private static Template load(TemplateManager manager, String name) {
        return FFDJigsawPlacement.readTemplate(manager, new ResourceLocation(FarmerFutureDelight.MODID, "ruined_portal/" + name));
    }

    private static StructureBoundingBox templateBounds(BlockPos origin, BlockPos size, Rotation rotation, Mirror mirror) {
        PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(mirror);
        return new StructureBoundingBox(origin, origin.add(Template.transformedBlockPos(settings, size.add(-1, -1, -1))));
    }

    private static BlockPos center(StructureBoundingBox box) {
        return new BlockPos(box.minX + box.getXSize() / 2, box.minY + box.getYSize() / 2, box.minZ + box.getZSize() / 2);
    }
}
