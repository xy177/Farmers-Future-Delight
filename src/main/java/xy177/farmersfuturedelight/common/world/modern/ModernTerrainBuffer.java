package xy177.farmersfuturedelight.common.world.modern;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

public final class ModernTerrainBuffer implements IBlockAccess {
    private final ExtendedBlockStorage[] sections;
    private final ModernGenerationInput input;
    private final boolean skylight;
    private StateReader neighbors;

    @FunctionalInterface
    public interface StateReader {
        IBlockState get(int blockX, int blockY, int blockZ);
    }

    public ModernTerrainBuffer(ExtendedBlockStorage[] sections, ModernGenerationInput input, boolean skylight) {
        this.sections = sections;
        this.input = input;
        this.skylight = skylight;
    }

    public ExtendedBlockStorage[] getBlockStorageArray() {
        return sections;
    }

    public boolean hasSkylight() {
        return skylight;
    }

    public void setNeighborReader(StateReader neighbors) {
        this.neighbors = neighbors;
    }

    public void setState(int localX, int blockY, int localZ, IBlockState state) {
        int logical = (blockY - FFDModernWorldgenData.MIN_Y) >> 4;
        if (logical < 0 || logical >= sections.length) return;
        ExtendedBlockStorage section = sections[logical];
        if (section == null) {
            if (state.getMaterial() == net.minecraft.block.material.Material.AIR) return;
            section = new ExtendedBlockStorage(blockY >> 4 << 4, skylight);
            sections[logical] = section;
        }
        section.set(localX & 15, blockY & 15, localZ & 15, state);
    }

    public IBlockState stateAt(int localX, int blockY, int localZ) {
        int logical = (blockY - FFDModernWorldgenData.MIN_Y) >> 4;
        if (logical < 0 || logical >= sections.length) return Blocks.AIR.getDefaultState();
        ExtendedBlockStorage section = sections[logical];
        return section == null ? Blocks.AIR.getDefaultState() : section.get(localX & 15, blockY & 15, localZ & 15);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos position) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos position, int lightValue) {
        return lightValue << 4;
    }

    @Override
    public IBlockState getBlockState(BlockPos position) {
        if (position.getX() >> 4 != input.chunkX || position.getZ() >> 4 != input.chunkZ) {
            return neighbors == null ? Blocks.AIR.getDefaultState()
                    : neighbors.get(position.getX(), position.getY(), position.getZ());
        }
        return stateAt(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public boolean isAirBlock(BlockPos position) {
        return getBlockState(position).getMaterial() == net.minecraft.block.material.Material.AIR;
    }

    @Override
    public Biome getBiome(BlockPos position) {
        return input.biomeAt(position.getX(), position.getZ());
    }

    @Override
    public int getStrongPower(BlockPos position, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos position, EnumFacing side, boolean defaultValue) {
        return getBlockState(position).isSideSolid(this, position, side);
    }
}
