package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityMooshroom;
import xy177.farmersfuturedelight.common.entity.FFDMooshroomData;

public class ModelMooshroom extends ModelCow {
    private final ModelCow red = new ModelCow();
    private final BrownModel adult = new BrownModel(false);
    private final BrownModel baby = new BrownModel(true);

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age,
                       float yaw, float pitch, float scale) {
        ModelCow selected = entity instanceof EntityMooshroom
                && FFDMooshroomData.isBrown((EntityMooshroom) entity) ? isChild ? baby : adult : red;
        selected.setModelAttributes(this);
        selected.render(entity, limbSwing, limbSwingAmount, age, yaw, pitch, scale);
        head = selected.head;
    }

    private static final class BrownModel extends ModelCow {
        private final boolean baby;

        private BrownModel(boolean baby) {
            this.baby = baby;
            textureWidth = 64;
            textureHeight = 64;
            if (baby) {
                head = new ModelRenderer(this, 0, 18);
                head.addBox(-3.0F, -4.569F, -4.8333F, 6, 6, 5);
                head.setTextureOffset(8, 29).addBox(3.0F, -5.569F, -3.8333F, 1, 2, 1);
                head.setTextureOffset(4, 29).mirror = true;
                head.addBox(-4.0F, -5.569F, -3.8333F, 1, 2, 1);
                head.mirror = false;
                head.setTextureOffset(12, 29).addBox(-2.0F, -1.569F, -5.8333F, 4, 3, 1);
                head.setRotationPoint(0.0F, 13.569F, -5.1667F);
                body = new ModelRenderer(this, 0, 0);
                body.addBox(-7.0F, -7.0F, -1.0F, 8, 6, 12);
                body.setRotationPoint(3.0F, 19.0F, -5.0F);
                leg1 = leg(22, 27, -2.5F, 18.0F, 3.5F, true, false);
                leg2 = leg(34, 27, 2.5F, 18.0F, 3.5F, true, false);
                leg3 = leg(22, 18, -2.5F, 18.0F, -3.5F, true, false);
                leg4 = leg(34, 18, 2.5F, 18.0F, -3.5F, true, false);
            } else {
                head = new ModelRenderer(this, 0, 0);
                head.addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6);
                head.setTextureOffset(1, 33).addBox(-3.0F, 1.0F, -7.0F, 6, 3, 1);
                head.setTextureOffset(22, 0).addBox(-5.0F, -5.0F, -5.0F, 1, 3, 1);
                head.setTextureOffset(22, 0).addBox(4.0F, -5.0F, -5.0F, 1, 3, 1);
                head.setRotationPoint(0.0F, 4.0F, -8.0F);
                body = new ModelRenderer(this, 18, 4);
                body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10);
                body.setRotationPoint(0.0F, 5.0F, 2.0F);
                body.setTextureOffset(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4, 6, 1);
                leg1 = leg(0, 16, -4.0F, 12.0F, 7.0F, false, false);
                leg2 = leg(0, 16, 4.0F, 12.0F, 7.0F, false, true);
                leg3 = leg(0, 16, -4.0F, 12.0F, -5.0F, false, false);
                leg4 = leg(0, 16, 4.0F, 12.0F, -5.0F, false, true);
            }
        }

        private ModelRenderer leg(int u, int v, float x, float y, float z, boolean small, boolean mirror) {
            ModelRenderer leg = new ModelRenderer(this, u, v);
            leg.mirror = mirror;
            float radius = small ? 1.5F : 2.0F;
            leg.addBox(-radius, 0.0F, -radius, small ? 3 : 4, small ? 6 : 12, small ? 3 : 4);
            leg.setRotationPoint(x, y, z);
            return leg;
        }

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount, float age,
                           float yaw, float pitch, float scale) {
            setRotationAngles(limbSwing, limbSwingAmount, age, yaw, pitch, scale, entity);
            if (baby) body.rotateAngleX = 0.0F;
            head.render(scale);
            body.render(scale);
            leg1.render(scale);
            leg2.render(scale);
            leg3.render(scale);
            leg4.render(scale);
        }
    }
}
