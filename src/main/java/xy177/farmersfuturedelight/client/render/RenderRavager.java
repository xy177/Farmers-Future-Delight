package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.common.entity.EntityRavager;

@SideOnly(Side.CLIENT)
public class RenderRavager extends RenderLiving<EntityRavager> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("farmers_future_delight", "textures/entity/illager/ravager.png");

    public RenderRavager(RenderManager manager) {
        super(manager, new ModelRavager(), 1.1F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRavager entity) {
        return TEXTURE;
    }
}
