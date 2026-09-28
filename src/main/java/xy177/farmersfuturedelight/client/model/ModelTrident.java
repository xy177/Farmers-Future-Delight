package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelTrident extends ModelBase {
    private final ModelRenderer root;

    public ModelTrident() {
        textureWidth = 32;
        textureHeight = 32;
        root = new ModelRenderer(this, 0, 6);
        root.addBox(-0.5F, 2.0F, -0.5F, 1, 25, 1, 0.0F);
        ModelRenderer middle = new ModelRenderer(this, 4, 0);
        middle.addBox(-1.5F, 0.0F, -0.5F, 3, 2, 1);
        root.addChild(middle);
        ModelRenderer left = new ModelRenderer(this, 4, 3);
        left.addBox(-2.5F, -3.0F, -0.5F, 1, 4, 1);
        root.addChild(left);
        ModelRenderer right = new ModelRenderer(this, 4, 3);
        right.mirror = true;
        right.addBox(1.5F, -3.0F, -0.5F, 1, 4, 1);
        root.addChild(right);
        ModelRenderer middleSpike = new ModelRenderer(this, 0, 0);
        middleSpike.addBox(-0.5F, -4.0F, -0.5F, 1, 4, 1);
        root.addChild(middleSpike);
    }

    public void render() {
        root.render(0.0625F);
    }
}
