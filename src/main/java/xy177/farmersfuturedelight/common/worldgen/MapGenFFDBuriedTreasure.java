package xy177.farmersfuturedelight.common.worldgen;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.TemplateManager;

public final class MapGenFFDBuriedTreasure extends MapGenStructure {
    private static final int SALT = 10387320;
    private static final float CHANCE = 0.01F;

    public MapGenFFDBuriedTreasure() {
        range = 0;
    }

    @Override
    public String getStructureName() {
        return "FFDBuriedTreasure";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        return canSpawnAt(world, chunkX, chunkZ);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start(chunkX, chunkZ);
    }

    @Override
    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        return findNearest(worldIn, pos, findUnexplored, 100);
    }

    @Nullable
    BlockPos findNearest(World worldIn, BlockPos pos, boolean findUnexplored, int radius) {
        world = worldIn;
        int originX = pos.getX() >> 4;
        int originZ = pos.getZ() >> 4;
        for (int ring = 0; ring <= radius; ring++) {
            for (int offsetX = -ring; offsetX <= ring; offsetX++) {
                boolean edgeX = offsetX == -ring || offsetX == ring;
                for (int offsetZ = -ring; offsetZ <= ring; offsetZ++) {
                    if (!edgeX && offsetZ != -ring && offsetZ != ring) {
                        continue;
                    }
                    int chunkX = originX + offsetX;
                    int chunkZ = originZ + offsetZ;
                    if (canSpawnAt(worldIn, chunkX, chunkZ)
                            && (!findUnexplored || !worldIn.isChunkGeneratedAt(chunkX, chunkZ))) {
                        return new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9);
                    }
                }
            }
        }
        return null;
    }

    private static boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        Random random = new Random((long) chunkX * 341873128712L
                + (long) chunkZ * 132897987541L + world.getSeed() + SALT);
        if (random.nextFloat() >= CHANCE) {
            return false;
        }
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 9, 0, (chunkZ << 4) + 9));
        return FFDOceanStructures.isBeach(biome);
    }

    public static final class Start extends StructureStart {
        public Start() {
        }

        Start(int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            components.add(new Piece(new BlockPos((chunkX << 4) + 9, 90, (chunkZ << 4) + 9)));
            updateBoundingBox();
        }
    }

    public static final class Piece extends StructureComponent {
        public Piece() {
        }

        Piece(BlockPos pos) {
            super(0);
            boundingBox = new StructureBoundingBox(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
        }

        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
            int x = boundingBox.minX;
            int z = boundingBox.minZ;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x,
                    FFDOceanStructures.oceanFloorY(world, x, z), z);
            while (cursor.getY() > FFDOceanStructures.minY(world)) {
                IBlockState current = world.getBlockState(cursor);
                IBlockState below = world.getBlockState(cursor.down());
                if (isFoundation(below)) {
                    IBlockState filler = isAirOrWater(current)
                            ? Blocks.SAND.getDefaultState() : current;
                    for (EnumFacing facing : EnumFacing.values()) {
                        BlockPos neighbor = cursor.offset(facing);
                        IBlockState neighborState = world.getBlockState(neighbor);
                        if (!isAirOrWater(neighborState)) {
                            continue;
                        }
                        IBlockState belowNeighbor = world.getBlockState(neighbor.down());
                        world.setBlockState(neighbor,
                                facing != EnumFacing.UP && isAirOrWater(belowNeighbor)
                                        ? below : filler, 3);
                    }
                    BlockPos chestPos = cursor.toImmutable();
                    boundingBox = new StructureBoundingBox(chestPos.getX(), chestPos.getY(),
                            chestPos.getZ(), chestPos.getX(), chestPos.getY(), chestPos.getZ());
                    return generateChest(world, bounds, random, chestPos,
                            FFDOceanStructures.BURIED_TREASURE_LOOT, null);
                }
                cursor.move(EnumFacing.DOWN);
            }
            return false;
        }

        private static boolean isFoundation(IBlockState state) {
            return state.getBlock() == Blocks.SANDSTONE || state.getBlock() == Blocks.STONE;
        }

        private static boolean isAirOrWater(IBlockState state) {
            return state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER
                    || state.getBlock() == Blocks.FLOWING_WATER;
        }
    }
}
