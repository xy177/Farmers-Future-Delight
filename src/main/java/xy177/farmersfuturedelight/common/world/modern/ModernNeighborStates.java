package xy177.farmersfuturedelight.common.world.modern;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.block.state.IBlockState;
import xy177.farmersfuturedelight.common.world.terrain.FFDModernWorldgenData;

final class ModernNeighborStates {
    private final AtomicReferenceArray<AtomicReferenceArray<IBlockState>> sections =
            new AtomicReferenceArray<>(FFDModernWorldgenData.HEIGHT >> 4);
    private final AtomicInteger populated = new AtomicInteger();
    private final AtomicInteger allocated = new AtomicInteger();

    IBlockState get(int localX, int blockY, int localZ) {
        int height = blockY - FFDModernWorldgenData.MIN_Y;
        AtomicReferenceArray<IBlockState> section = sections.get(height >> 4);
        return section == null ? null : section.get(index(localX, height, localZ));
    }

    void remember(int localX, int blockY, int localZ, IBlockState state) {
        int height = blockY - FFDModernWorldgenData.MIN_Y;
        int sectionIndex = height >> 4;
        AtomicReferenceArray<IBlockState> section = sections.get(sectionIndex);
        if (section == null) {
            AtomicReferenceArray<IBlockState> created = new AtomicReferenceArray<>(4096);
            if (sections.compareAndSet(sectionIndex, null, created)) {
                section = created;
                allocated.incrementAndGet();
            } else {
                section = sections.get(sectionIndex);
            }
        }
        if (section.compareAndSet(index(localX, height, localZ), null, state)) populated.incrementAndGet();
    }

    int populatedStates() {
        return populated.get();
    }

    int allocatedSections() {
        return allocated.get();
    }

    private static int index(int localX, int height, int localZ) {
        return (height & 15) << 8 | (localZ & 15) << 4 | localX & 15;
    }
}
