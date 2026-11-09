package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelWanderingTrader extends ModelVillager {
    public ModelWanderingTrader() {
        super(0.0F);
        textureWidth = 64;
        textureHeight = 64;
        ModelRenderer hat = new ModelRenderer(this, 32, 0);
        hat.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.51F);
        villagerHead.addChild(hat);
        ModelRenderer rim = new ModelRenderer(this, 30, 47);
        rim.addBox(-8.0F, -8.0F, -6.0F, 16, 16, 1);
        rim.rotateAngleX = -(float) Math.PI / 2.0F;
        hat.addChild(rim);
        villagerBody = new ModelRenderer(this, 16, 20);
        villagerBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6);
        ModelRenderer jacket = new ModelRenderer(this, 0, 38);
        jacket.addBox(-4.0F, 0.0F, -3.0F, 8, 20, 6, 0.5F);
        villagerBody.addChild(jacket);
    }
}
