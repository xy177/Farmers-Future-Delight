package xy177.farmersfuturedelight.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.client.model.ModelAxolotl;
import xy177.farmersfuturedelight.client.model.ModelAxolotlBaby;
import xy177.farmersfuturedelight.common.entity.EntityAxolotl;

@SideOnly(Side.CLIENT)
public class RenderAxolotl extends RenderLiving<EntityAxolotl> {
    private static final ResourceLocation[] ADULT_TEXTURES = textures(false);
    private static final ResourceLocation[] BABY_TEXTURES = textures(true);
    private final ModelBase adultModel;
    private final ModelBase babyModel = new ModelAxolotlBaby();

    public RenderAxolotl(RenderManager manager) {
        super(manager, new ModelAxolotl(), 0.5F);
        adultModel = mainModel;
    }

    @Override
    public void doRender(EntityAxolotl axolotl, double x, double y, double z,
                         float entityYaw, float partialTicks) {
        mainModel = axolotl.isChild() ? babyModel : adultModel;
        shadowSize = axolotl.isChild() ? 0.25F : 0.5F;
        super.doRender(axolotl, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAxolotl axolotl) {
        int id = axolotl.getVariant().getId();
        return axolotl.isChild() ? BABY_TEXTURES[id] : ADULT_TEXTURES[id];
    }

    private static ResourceLocation[] textures(boolean baby) {
        ResourceLocation[] textures = new ResourceLocation[EntityAxolotl.Variant.values().length];
        for (EntityAxolotl.Variant variant : EntityAxolotl.Variant.values()) {
            textures[variant.getId()] = new ResourceLocation(FarmerFutureDelight.MODID,
                    "textures/entity/axolotl/axolotl_" + variant.getName()
                            + (baby ? "_baby" : "") + ".png");
        }
        return textures;
    }
}
