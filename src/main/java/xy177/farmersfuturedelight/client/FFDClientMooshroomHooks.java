package xy177.farmersfuturedelight.client;

import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.common.entity.FFDMooshroomData;

public final class FFDClientMooshroomHooks {
    private static final ResourceLocation BROWN_TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/cow/mooshroom_brown.png");
    private static final ResourceLocation BROWN_BABY_TEXTURE = new ResourceLocation(
            "farmers_future_delight", "textures/entity/cow/mooshroom_brown_baby.png");

    private FFDClientMooshroomHooks() {
    }

    public static BlockBush getMushroom(BlockBush original, EntityMooshroom entity) {
        return FFDMooshroomData.isBrown(entity) ? Blocks.BROWN_MUSHROOM : original;
    }

    public static ResourceLocation getTexture(EntityMooshroom entity,
                                              ResourceLocation original) {
        if (!FFDMooshroomData.isBrown(entity)) {
            return original;
        }
        return entity.isChild() ? BROWN_BABY_TEXTURE : BROWN_TEXTURE;
    }
}
