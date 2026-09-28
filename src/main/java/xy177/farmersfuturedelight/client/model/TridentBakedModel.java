package xy177.farmersfuturedelight.client.model;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public final class TridentBakedModel implements IBakedModel {
    private final IBakedModel flatModel;
    private final IBakedModel handModel;
    private final ItemOverrideList overrides;

    public TridentBakedModel(IBakedModel flatModel, IBakedModel handModel,
                             IBakedModel throwingModel) {
        this.flatModel = flatModel;
        this.handModel = handModel;
        TridentBakedModel throwing = new TridentBakedModel(flatModel, throwingModel);
        this.overrides = new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack,
                                               World world, EntityLivingBase entity) {
                return entity != null && entity.isHandActive()
                        && entity.getActiveItemStack() == stack
                        ? throwing : TridentBakedModel.this;
            }
        };
    }

    private TridentBakedModel(IBakedModel flatModel, IBakedModel handModel) {
        this.flatModel = flatModel;
        this.handModel = handModel;
        this.overrides = ItemOverrideList.NONE;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side,
                                    long rand) {
        return flatModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return flatModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return flatModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return flatModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return flatModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return flatModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
            ItemCameraTransforms.TransformType cameraTransformType) {
        switch (cameraTransformType) {
            case GUI:
            case GROUND:
            case FIXED:
                return flatModel.handlePerspective(cameraTransformType);
            default:
                return handModel.handlePerspective(cameraTransformType);
        }
    }
}
