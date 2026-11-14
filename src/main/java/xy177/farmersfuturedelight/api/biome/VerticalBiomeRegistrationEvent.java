package xy177.farmersfuturedelight.api.biome;

import net.minecraftforge.fml.common.eventhandler.Event;

public final class VerticalBiomeRegistrationEvent extends Event {
    public VerticalBiomeDefinition register(VerticalBiomeDefinition definition) {
        return VerticalBiomeRegistry.register(definition);
    }
}
