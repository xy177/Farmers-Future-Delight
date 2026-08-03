package xy177.farmersfuturedelight.common.block;

import net.minecraft.util.IStringSerializable;

public enum DripleafTilt implements IStringSerializable {
    NONE("none"),
    UNSTABLE("unstable"),
    PARTIAL("partial"),
    FULL("full");

    private final String name;

    DripleafTilt(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
