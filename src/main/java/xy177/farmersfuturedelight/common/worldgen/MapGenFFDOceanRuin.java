package xy177.farmersfuturedelight.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public final class MapGenFFDOceanRuin extends MapGenStructure {
    private static final int SPACING = 20;
    private static final int SEPARATION = 8;
    private static final int SALT = 14357621;
    private static final String[] WARM = names("warm_", 1, 8);
    private static final String[] BIG_WARM = names("big_warm_", 4, 7);
    private static final String[] BRICK = names("brick_", 1, 8);
    private static final String[] CRACKED = names("cracked_", 1, 8);
    private static final String[] MOSSY = names("mossy_", 1, 8);
    private static final String[] BIG_BRICK = {"big_brick_1", "big_brick_2", "big_brick_3", "big_brick_8"};
    private static final String[] BIG_CRACKED = {"big_cracked_1", "big_cracked_2", "big_cracked_3", "big_cracked_8"};
    private static final String[] BIG_MOSSY = {"big_mossy_1", "big_mossy_2", "big_mossy_3", "big_mossy_8"};

    public MapGenFFDOceanRuin() {
        range = 3;
    }

    @Override
    public String getStructureName() {
        return "FFDOceanRuin";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (!isCandidate(chunkX, chunkZ)) {
            return false;
        }
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9));
        return FFDOceanStructures.isOcean(biome);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9));
        return new Start(world, rand, chunkX, chunkZ, FFDOceanStructures.isWarmOcean(biome));
    }

    @Override
    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        return findNearest(worldIn, pos, findUnexplored, 100);
    }

    @Nullable
    BlockPos findNearest(World worldIn, BlockPos pos, boolean findUnexplored, int radius) {
        world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, SPACING, SEPARATION,
                SALT, false, radius, findUnexplored);
    }

    private boolean isCandidate(int chunkX, int chunkZ) {
        int sourceX = chunkX < 0 ? chunkX - SPACING + 1 : chunkX;
        int sourceZ = chunkZ < 0 ? chunkZ - SPACING + 1 : chunkZ;
        int regionX = sourceX / SPACING;
        int regionZ = sourceZ / SPACING;
        Random random = world.setRandomSeed(regionX, regionZ, SALT);
        int candidateX = regionX * SPACING + random.nextInt(SPACING - SEPARATION);
        int candidateZ = regionZ * SPACING + random.nextInt(SPACING - SEPARATION);
        return chunkX == candidateX && chunkZ == candidateZ;
    }

    private static String[] names(String prefix, int first, int last) {
        String[] result = new String[last - first + 1];
        for (int value = first; value <= last; value++) {
            result[value - first] = prefix + value;
        }
        return result;
    }

    public static final class Start extends StructureStart {
        public Start() {
        }

        Start(World world, Random random, int chunkX, int chunkZ, boolean warm) {
            super(chunkX, chunkZ);
            TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
            BlockPos origin = new BlockPos(chunkX << 4, 90, chunkZ << 4);
            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            boolean large = random.nextFloat() <= 0.3F;
            float integrity = large ? 0.9F : 0.8F;
            Piece main = addRuin(manager, components, random, origin, rotation, warm, large,
                    integrity, true);
            if (large && random.nextFloat() <= 0.9F) {
                addCluster(manager, components, random, origin, rotation, warm,
                        main.getBoundingBox());
            }
            updateBoundingBox();
        }

        private static Piece addRuin(TemplateManager manager, List<StructureComponent> components,
                                     Random random, BlockPos position, Rotation rotation,
                                     boolean warm, boolean large, float integrity,
                                     boolean primary) {
            if (warm) {
                String[] choices = large ? BIG_WARM : WARM;
                Piece piece = new Piece(manager, choices[random.nextInt(choices.length)], position,
                        rotation, integrity, true, large, primary);
                components.add(piece);
                return piece;
            }
            String[] brick = large ? BIG_BRICK : BRICK;
            String[] cracked = large ? BIG_CRACKED : CRACKED;
            String[] mossy = large ? BIG_MOSSY : MOSSY;
            int index = random.nextInt(brick.length);
            Piece base = new Piece(manager, brick[index], position, rotation, integrity,
                    false, large, primary);
            components.add(base);
            components.add(new Piece(manager, cracked[index], position, rotation, 0.7F,
                    false, large, false));
            components.add(new Piece(manager, mossy[index], position, rotation, 0.5F,
                    false, large, false));
            return base;
        }

        private static void addCluster(TemplateManager manager, List<StructureComponent> components,
                                       Random random, BlockPos origin, Rotation mainRotation,
                                       boolean warm, StructureBoundingBox mainBounds) {
            int minX = mainBounds.minX;
            int minZ = mainBounds.minZ;
            List<BlockPos> candidates = new ArrayList<>();
            candidates.add(new BlockPos(minX - 16 + between(random, 1, 8), 90,
                    minZ + 16 + between(random, 1, 7)));
            candidates.add(new BlockPos(minX - 16 + between(random, 1, 8), 90,
                    minZ + between(random, 1, 7)));
            candidates.add(new BlockPos(minX - 16 + between(random, 1, 8), 90,
                    minZ - 16 + between(random, 4, 8)));
            candidates.add(new BlockPos(minX + between(random, 1, 7), 90,
                    minZ + 16 + between(random, 1, 7)));
            candidates.add(new BlockPos(minX + between(random, 1, 7), 90,
                    minZ - 16 + between(random, 4, 6)));
            candidates.add(new BlockPos(minX + 16 + between(random, 1, 7), 90,
                    minZ + 16 + between(random, 3, 8)));
            candidates.add(new BlockPos(minX + 16 + between(random, 1, 7), 90,
                    minZ + between(random, 1, 7)));
            candidates.add(new BlockPos(minX + 16 + between(random, 1, 7), 90,
                    minZ - 16 + between(random, 4, 8)));
            int count = between(random, 4, 8);
            for (int i = 0; i < count && !candidates.isEmpty(); i++) {
                BlockPos position = candidates.remove(random.nextInt(candidates.size()));
                Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
                Piece piece = preview(manager, random, position, rotation, warm);
                if (!piece.getBoundingBox().intersectsWith(mainBounds)) {
                    components.add(piece);
                    if (!warm) {
                        addColdOverlays(manager, components, piece, position, rotation);
                    }
                }
            }
        }

        private static Piece preview(TemplateManager manager, Random random, BlockPos position,
                                     Rotation rotation, boolean warm) {
            if (warm) {
                return new Piece(manager, WARM[random.nextInt(WARM.length)], position, rotation,
                        0.8F, true, false, true);
            }
            int index = random.nextInt(BRICK.length);
            return new Piece(manager, BRICK[index], position, rotation, 0.8F,
                    false, false, true, index);
        }

        private static void addColdOverlays(TemplateManager manager,
                                            List<StructureComponent> components, Piece base,
                                            BlockPos position, Rotation rotation) {
            int index = base.variantIndex;
            components.add(new Piece(manager, CRACKED[index], position, rotation, 0.7F,
                    false, false, false));
            components.add(new Piece(manager, MOSSY[index], position, rotation, 0.5F,
                    false, false, false));
        }

        private static int between(Random random, int min, int max) {
            return min + random.nextInt(max - min + 1);
        }
    }

    public static final class Piece extends StructureComponentTemplate {
        private String templateName;
        private Rotation rotation;
        private float integrity;
        private boolean warm;
        private boolean large;
        private boolean primary;
        private boolean heightResolved;
        private int variantIndex = -1;

        public Piece() {
        }

        Piece(TemplateManager manager, String templateName, BlockPos position, Rotation rotation,
              float integrity, boolean warm, boolean large, boolean primary) {
            this(manager, templateName, position, rotation, integrity, warm, large, primary, -1);
        }

        Piece(TemplateManager manager, String templateName, BlockPos position, Rotation rotation,
              float integrity, boolean warm, boolean large, boolean primary, int variantIndex) {
            super(0);
            this.templateName = templateName;
            this.rotation = rotation;
            this.integrity = integrity;
            this.warm = warm;
            this.large = large;
            this.primary = primary;
            this.variantIndex = variantIndex;
            templatePosition = position;
            loadTemplate(manager);
        }

        private void loadTemplate(TemplateManager manager) {
            Template template = manager.getTemplate((MinecraftServer) null,
                    new ResourceLocation(FarmerFutureDelight.MODID,
                            "underwater_ruin/" + templateName));
            PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true)
                    .setRotation(rotation).setMirror(Mirror.NONE).setReplacedBlock(Blocks.AIR)
                    .setIntegrity(integrity);
            setup(template, templatePosition, settings);
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
            super.writeStructureToNBT(tag);
            tag.setString("Template", templateName);
            tag.setString("Rot", rotation.name());
            tag.setFloat("Integrity", integrity);
            tag.setBoolean("Warm", warm);
            tag.setBoolean("Large", large);
            tag.setBoolean("Primary", primary);
            tag.setBoolean("HeightResolved", heightResolved);
            tag.setInteger("Variant", variantIndex);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            super.readStructureFromNBT(tag, manager);
            templateName = tag.getString("Template");
            rotation = Rotation.valueOf(tag.getString("Rot"));
            integrity = tag.getFloat("Integrity");
            warm = tag.getBoolean("Warm");
            large = tag.getBoolean("Large");
            primary = tag.getBoolean("Primary");
            heightResolved = tag.getBoolean("HeightResolved");
            variantIndex = tag.getInteger("Variant");
            loadTemplate(manager);
        }

        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            if (!heightResolved) {
                resolveHeight(world);
            }
            boolean placed = super.addComponentParts(world, random, bounds);
            FFDOceanStructures.waterlogStructureBlocks(world, boundingBox, bounds, true);
            return placed;
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, World world, Random random,
                                        StructureBoundingBox bounds) {
            if (!primary || !bounds.isVecInside(pos)) {
                return;
            }
            if ("chest".equals(function)) {
                IBlockState chestState = Blocks.CHEST.getDefaultState();
                if (pos.getY() <= world.getSeaLevel()) {
                    IBlockState waterlogged = WaterloggedBlockApi.withWaterlogged(chestState, true);
                    if (waterlogged != null) {
                        chestState = waterlogged;
                    }
                }
                world.setBlockState(pos, chestState, 2);
                TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileEntityChest) {
                    FFDOceanStructures.fillTreasureMapLoot((TileEntityChest) tile, large
                                    ? FFDOceanStructures.OCEAN_RUIN_BIG_LOOT
                                    : FFDOceanStructures.OCEAN_RUIN_SMALL_LOOT,
                            pos, random.nextLong());
                }
            } else if ("drowned".equals(function)) {
                EntityLiving drowned = FFDOceanStructures.createDrowned(world);
                if (drowned != null) {
                    drowned.enablePersistence();
                    drowned.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(),
                            pos.getZ() + 0.5D, 0.0F, 0.0F);
                    drowned.onInitialSpawn(world.getDifficultyForLocation(pos),
                            (IEntityLivingData) null);
                    world.spawnEntity(drowned);
                }
                world.setBlockState(pos, pos.getY() <= world.getSeaLevel()
                        ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState(), 2);
            }
        }

        private void resolveHeight(World world) {
            int initial = FFDOceanStructures.oceanFloorY(world,
                    templatePosition.getX(), templatePosition.getZ());
            int minimum = Integer.MAX_VALUE;
            int lowerColumns = 0;
            for (int x = boundingBox.minX; x <= boundingBox.maxX; x++) {
                for (int z = boundingBox.minZ; z <= boundingBox.maxZ; z++) {
                    int height = FFDOceanStructures.oceanFloorY(world, x, z);
                    minimum = Math.min(minimum, height);
                    if (height < initial - 2) {
                        lowerColumns++;
                    }
                }
            }
            int width = Math.max(boundingBox.getXSize(), boundingBox.getZSize());
            int targetY = initial - minimum > 2 && lowerColumns > width - 2
                    ? minimum + 1 : initial;
            targetY = Math.max(FFDOceanStructures.minY(world) + 1,
                    Math.min(targetY, FFDOceanStructures.maxY(world) - template.getSize().getY()));
            offset(0, targetY - templatePosition.getY(), 0);
            heightResolved = true;
        }
    }
}
