package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.api.WaterloggedBlockApi;

public final class MapGenFFDShipwreck extends MapGenStructure {
    private static final int SPACING = 24;
    private static final int SEPARATION = 4;
    private static final int SALT = 165745295;
    private static final String[] BEACHED = {
            "with_mast", "sideways_full", "sideways_fronthalf", "sideways_backhalf",
            "rightsideup_full", "rightsideup_fronthalf", "rightsideup_backhalf",
            "with_mast_degraded", "rightsideup_full_degraded",
            "rightsideup_fronthalf_degraded", "rightsideup_backhalf_degraded"
    };
    private static final String[] OCEAN = {
            "with_mast", "upsidedown_full", "upsidedown_fronthalf", "upsidedown_backhalf",
            "sideways_full", "sideways_fronthalf", "sideways_backhalf", "rightsideup_full",
            "rightsideup_fronthalf", "rightsideup_backhalf", "with_mast_degraded",
            "upsidedown_full_degraded", "upsidedown_fronthalf_degraded",
            "upsidedown_backhalf_degraded", "sideways_full_degraded",
            "sideways_fronthalf_degraded", "sideways_backhalf_degraded",
            "rightsideup_full_degraded", "rightsideup_fronthalf_degraded",
            "rightsideup_backhalf_degraded"
    };

    public MapGenFFDShipwreck() {
        range = 2;
    }

    @Override
    public String getStructureName() {
        return "FFDShipwreck";
    }

    @Override
    public synchronized boolean generateStructure(World world, Random random, ChunkPos chunk) {
        boolean generated = super.generateStructure(world, random, chunk);
        return resumePending(world) || generated;
    }

    synchronized boolean resumePending(World world) {
        initializeStructureData(world);
        boolean changed = false;
        for (StructureStart start : structureMap.values()) {
            boolean updated = false;
            for (StructureComponent component : start.getComponents()) {
                if (component instanceof Piece) {
                    updated |= ((Piece) component).placePending(world);
                }
            }
            if (updated) {
                ((Start) start).updateBoundingBox();
                MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage()
                        .getOrLoadData(MapGenStructureData.class, getStructureName());
                data.writeInstance(start.writeStructureComponentsToNBT(
                        start.getChunkPosX(), start.getChunkPosZ()),
                        start.getChunkPosX(), start.getChunkPosZ());
                data.markDirty();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (!isCandidate(chunkX, chunkZ)) {
            return false;
        }
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9));
        return FFDOceanStructures.isOcean(biome) || FFDOceanStructures.isBeach(biome);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9));
        return new Start(world, rand, chunkX, chunkZ,
                FFDOceanStructures.isBeach(biome) && !FFDOceanStructures.isOcean(biome));
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

    public static final class Start extends StructureStart {
        public Start() {
        }

        Start(World world, Random random, int chunkX, int chunkZ, boolean beached) {
            super(chunkX, chunkZ);
            String[] choices = beached ? BEACHED : OCEAN;
            String template = choices[random.nextInt(choices.length)];
            int palette = random.nextInt(8);
            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            BlockPos origin = new BlockPos(chunkX << 4, 90, chunkZ << 4)
                    .add(FFDOceanStructures.pivotOffset(rotation));
            components.add(new Piece(world.getSaveHandler().getStructureTemplateManager(), template,
                    palette, origin, rotation, beached, random.nextInt(3)));
            updateBoundingBox();
        }

        @Override
        public void generateStructure(World world, Random random, StructureBoundingBox bounds) {
            super.generateStructure(world, random, new StructureBoundingBox(
                    bounds.minX, FFDOceanStructures.minY(world), bounds.minZ,
                    bounds.maxX, FFDOceanStructures.maxY(world), bounds.maxZ));
            updateBoundingBox();
        }

        @Override
        protected void updateBoundingBox() {
            super.updateBoundingBox();
        }
    }

    public static final class Piece extends StructureComponentTemplate {
        private String templateName;
        private int palette;
        private Rotation rotation;
        private boolean beached;
        private int verticalOffset;
        private boolean heightResolved;
        private final List<PendingPlacement> pending = new ArrayList<>();

        public Piece() {
        }

        Piece(TemplateManager manager, String templateName, int palette, BlockPos position,
              Rotation rotation, boolean beached, int verticalOffset) {
            super(0);
            this.templateName = templateName;
            this.palette = palette;
            this.rotation = rotation;
            this.beached = beached;
            this.verticalOffset = verticalOffset;
            templatePosition = position;
            loadTemplate(manager);
        }

        private void loadTemplate(TemplateManager manager) {
            ResourceLocation id = new ResourceLocation(FarmerFutureDelight.MODID,
                    "shipwreck/" + templateName + "_p" + palette);
            Template template = manager.getTemplate((MinecraftServer) null, id);
            PlacementSettings settings = new PlacementSettings().setIgnoreEntities(true)
                    .setRotation(rotation).setMirror(Mirror.NONE)
                    .setReplacedBlock(net.minecraft.init.Blocks.AIR);
            setup(template, templatePosition, settings);
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
            super.writeStructureToNBT(tag);
            tag.setString("Template", templateName);
            tag.setInteger("Palette", palette);
            tag.setString("Rot", rotation.name());
            tag.setBoolean("Beached", beached);
            tag.setInteger("YOffset", verticalOffset);
            tag.setBoolean("HeightResolved", heightResolved);
            NBTTagList placements = new NBTTagList();
            for (PendingPlacement placement : pending) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setTag("Bounds", placement.bounds.toNBTTagIntArray());
                entry.setLong("Seed", placement.seed);
                placements.appendTag(entry);
            }
            tag.setTag("PendingPlacements", placements);
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            super.readStructureFromNBT(tag, manager);
            templateName = tag.getString("Template");
            palette = tag.getInteger("Palette");
            rotation = Rotation.valueOf(tag.getString("Rot"));
            beached = tag.getBoolean("Beached");
            verticalOffset = tag.getInteger("YOffset");
            heightResolved = tag.getBoolean("HeightResolved");
            pending.clear();
            NBTTagList placements = tag.getTagList("PendingPlacements", 10);
            for (int i = 0; i < placements.tagCount(); i++) {
                NBTTagCompound entry = placements.getCompoundTagAt(i);
                pending.add(new PendingPlacement(new StructureBoundingBox(
                        entry.getIntArray("Bounds")), entry.getLong("Seed")));
            }
            loadTemplate(manager);
        }

        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            if (!heightResolved) {
                resolveHeight(world);
            }
            for (PendingPlacement placement : pending) {
                if (placement.bounds.minX == bounds.minX && placement.bounds.minZ == bounds.minZ
                        && placement.bounds.maxX == bounds.maxX && placement.bounds.maxZ == bounds.maxZ) {
                    placePending(world);
                    return true;
                }
            }
            if (!heightResolved || !canPlace(world, bounds)) {
                pending.add(new PendingPlacement(new StructureBoundingBox(bounds), random.nextLong()));
                return true;
            }
            placePending(world);
            return place(world, random, bounds);
        }

        private boolean canPlace(World world, StructureBoundingBox bounds) {
            return world.isAreaLoaded(
                    new BlockPos(Math.max(bounds.minX, boundingBox.minX) - 1, 0,
                            Math.max(bounds.minZ, boundingBox.minZ) - 1),
                    new BlockPos(Math.min(bounds.maxX, boundingBox.maxX) + 1, 0,
                            Math.min(bounds.maxZ, boundingBox.maxZ) + 1), false);
        }

        private boolean placePending(World world) {
            if (pending.isEmpty()) {
                return false;
            }
            boolean resolvedBefore = heightResolved;
            if (!heightResolved) {
                resolveHeight(world);
            }
            if (!heightResolved) {
                return false;
            }
            boolean changed = !resolvedBefore;
            Iterator<PendingPlacement> iterator = pending.iterator();
            while (iterator.hasNext()) {
                PendingPlacement placement = iterator.next();
                if (canPlace(world, placement.bounds)) {
                    place(world, new Random(placement.seed), placement.bounds);
                    iterator.remove();
                    changed = true;
                }
            }
            return changed;
        }

        private boolean place(World world, Random random, StructureBoundingBox bounds) {
            List<BlockPos> replacedWater = new ArrayList<>();
            if (beached && boundingBox.intersectsWith(bounds)) {
                for (BlockPos pos : BlockPos.getAllInBox(
                        new BlockPos(Math.max(bounds.minX, boundingBox.minX),
                                Math.max(bounds.minY, boundingBox.minY),
                                Math.max(bounds.minZ, boundingBox.minZ)),
                        new BlockPos(Math.min(bounds.maxX, boundingBox.maxX),
                                Math.min(bounds.maxY, boundingBox.maxY),
                                Math.min(bounds.maxZ, boundingBox.maxZ)))) {
                    if (WaterloggedBlockApi.isWaterSource(world, pos)) {
                        replacedWater.add(pos);
                    }
                }
            }
            boolean placed = super.addComponentParts(world, random, bounds);
            FFDOceanStructures.waterlogStructureBlocks(world, boundingBox, bounds, !beached);
            for (BlockPos pos : replacedWater) {
                IBlockState wet = WaterloggedBlockApi.withWaterlogged(world.getBlockState(pos), true);
                if (wet != null) {
                    world.setBlockState(pos, wet, 2);
                }
            }
            return placed;
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, World world, Random random,
                                        StructureBoundingBox bounds) {
            BlockPos chestPos = pos.down();
            if (!bounds.isVecInside(chestPos)) {
                return;
            }
            if (!beached && chestPos.getY() <= world.getSeaLevel()) {
                IBlockState current = world.getBlockState(chestPos);
                IBlockState waterlogged = WaterloggedBlockApi.withWaterlogged(current, true);
                if (waterlogged != null && waterlogged != current) {
                    world.setBlockState(chestPos, waterlogged, 2);
                }
            }
            TileEntity tile = world.getTileEntity(chestPos);
            if (!(tile instanceof TileEntityChest)) {
                return;
            }
            if ("map_chest".equals(function)) {
                FFDOceanStructures.fillTreasureMapLoot((TileEntityChest) tile,
                        FFDOceanStructures.SHIPWRECK_MAP_LOOT, chestPos, random.nextLong());
            } else if ("treasure_chest".equals(function)) {
                ((TileEntityChest) tile).setLootTable(FFDOceanStructures.SHIPWRECK_TREASURE_LOOT,
                        random.nextLong());
            } else if ("supply_chest".equals(function)) {
                ((TileEntityChest) tile).setLootTable(FFDOceanStructures.SHIPWRECK_SUPPLY_LOOT,
                        random.nextLong());
            }
        }

        private void resolveHeight(World world) {
            BlockPos origin = templatePosition.subtract(FFDOceanStructures.pivotOffset(rotation));
            int sizeX = template.getSize().getX();
            int sizeZ = template.getSize().getZ();
            if (sizeX == 0 || sizeZ == 0 || !world.isAreaLoaded(
                    new BlockPos(origin.getX(), 0, origin.getZ()),
                    new BlockPos(origin.getX() + sizeX - 1, 0, origin.getZ() + sizeZ - 1), false)) {
                return;
            }
            long sum = 0L;
            int minimum = Integer.MAX_VALUE;
            for (int x = origin.getX(); x < origin.getX() + sizeX; x++) {
                for (int z = origin.getZ(); z < origin.getZ() + sizeZ; z++) {
                    int height = columnHeight(world, x, z);
                    sum += height;
                    minimum = Math.min(minimum, height);
                }
            }
            int targetY = beached
                    ? minimum - template.getSize().getY() / 2 - verticalOffset
                    : (int) (sum / (sizeX * sizeZ));
            targetY = Math.max(FFDOceanStructures.minY(world) + 1,
                    Math.min(targetY, FFDOceanStructures.maxY(world) - template.getSize().getY()));
            offset(0, targetY - templatePosition.getY(), 0);
            heightResolved = true;
        }

        private int columnHeight(World world, int x, int z) {
            for (int y = FFDOceanStructures.maxY(world); y >= FFDOceanStructures.minY(world); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                IBlockState state = world.getBlockState(pos);
                if (beached) {
                    if (!state.getBlock().isAir(state, world, pos)) {
                        return y + 1;
                    }
                } else {
                    IBlockState dry = WaterloggedBlockApi.withWaterlogged(state, false);
                    if ((dry == null ? state : dry).getMaterial().blocksMovement()) {
                        return y + 1;
                    }
                }
            }
            return FFDOceanStructures.minY(world);
        }

        private static final class PendingPlacement {
            private final StructureBoundingBox bounds;
            private final long seed;

            private PendingPlacement(StructureBoundingBox bounds, long seed) {
                this.bounds = bounds;
                this.seed = seed;
            }
        }
    }
}
