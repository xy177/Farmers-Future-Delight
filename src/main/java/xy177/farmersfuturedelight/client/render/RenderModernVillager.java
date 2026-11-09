package xy177.farmersfuturedelight.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.registry.FFDVillagerTypes;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = FarmerFutureDelight.MODID, value = Side.CLIENT)
public final class RenderModernVillager extends RenderLiving<EntityVillager> {
    private static final String[] BADGES = {"stone", "iron", "gold", "emerald", "diamond"};
    private static final Map<ResourceLocation, String> HATS = new HashMap<>();
    private static RenderModernVillager adultRenderer;
    private static RenderModernVillager babyRenderer;
    private static boolean reloadRegistered;
    private final ModelModernVillager model;

    private RenderModernVillager(RenderManager manager, boolean baby) {
        super(manager, new ModelModernVillager(baby), baby ? 0.25F : 0.5F);
        model = (ModelModernVillager) getMainModel();
        addLayer(new ProfessionLayer());
        addLayer(new LayerCustomHead(model.villagerHead));
        addLayer(new LayerVillagerHeldItem(this));
        if (!reloadRegistered && Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(resourceManager -> HATS.clear());
            reloadRegistered = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderModern(RenderLivingEvent.Pre<?> event) {
        if (event.getEntity().getClass() != EntityVillager.class
                || event.getRenderer().getClass() != RenderVillager.class) return;
        EntityVillager villager = (EntityVillager) event.getEntity();
        if (villager.isChild() && !xy177.farmersfuturedelight.common.FFDConfig.modernBabyModels) return;
        if (!state(villager).getBoolean("modern")) return;
        if (adultRenderer == null) {
            adultRenderer = new RenderModernVillager(Minecraft.getMinecraft().getRenderManager(), false);
            babyRenderer = new RenderModernVillager(Minecraft.getMinecraft().getRenderManager(), true);
        }
        event.setCanceled(true);
        (villager.isChild() ? babyRenderer : adultRenderer).doRender(villager, event.getX(), event.getY(),
                event.getZ(), villager.rotationYaw, event.getPartialRenderTick());
    }

    private static NBTTagCompound state(EntityVillager villager) {
        return villager.getEntityData().getCompoundTag(FFDVillagerTypes.CLIENT_STATE);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(FarmerFutureDelight.MODID, "textures/entity/villager/" + path + ".png");
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVillager villager) {
        return texture(villager.isChild() ? "villager_baby" : "villager");
    }

    @Override
    protected void preRenderCallback(EntityVillager villager, float partialTicks) {
        if (!villager.isChild()) GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected void applyRotations(EntityVillager villager, float age, float yaw, float partialTicks) {
        if (!state(villager).getBoolean("sleeping") || villager.deathTime > 0) {
            super.applyRotations(villager, age, yaw, partialTicks);
            return;
        }
        BlockPos bed = BlockPos.fromLong(state(villager).getLong("home"));
        net.minecraft.block.state.IBlockState block = villager.world.getBlockState(bed);
        EnumFacing facing = block.getBlock() instanceof net.minecraft.block.BlockBed
                ? block.getValue(net.minecraft.block.BlockBed.FACING) : EnumFacing.NORTH;
        float standingEyeHeight = villager.isChild() ? 0.81F : 1.62F;
        float headOffset = standingEyeHeight - 0.1F;
        GlStateManager.translate(-facing.getFrontOffsetX() * headOffset, 0.0F,
                -facing.getFrontOffsetZ() * headOffset);
        float rotation = facing == EnumFacing.SOUTH ? 90.0F : facing == EnumFacing.WEST ? 0.0F
                : facing == EnumFacing.NORTH ? 270.0F : 180.0F;
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
    }

    private static String hat(ResourceLocation texture) {
        return HATS.computeIfAbsent(texture, location -> {
            ResourceLocation metadata = new ResourceLocation(location.getResourceDomain(),
                    location.getResourcePath() + ".mcmeta");
            try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(metadata);
                 InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
                return root.has("villager") && root.getAsJsonObject("villager").has("hat")
                        ? root.getAsJsonObject("villager").get("hat").getAsString() : "none";
            } catch (Exception exception) {
                return "none";
            }
        });
    }

    private final class ProfessionLayer implements LayerRenderer<EntityVillager> {
        @Override
        public void doRenderLayer(EntityVillager villager, float limbSwing, float limbAmount,
                                  float partialTicks, float age, float yaw, float pitch, float scale) {
            if (villager.isInvisible()) return;
            NBTTagCompound state = state(villager);
            String type = state.getString("type");
            String profession = state.getString("profession");
            ResourceLocation typeTexture = texture((villager.isChild() ? "baby/" : "type/") + type);
            ResourceLocation professionTexture = texture("profession/" + profession);
            String professionHat = "none".equals(profession) ? "none" : hat(professionTexture);
            String typeHat = hat(texture("type/" + type));
            boolean showTypeHead = "none".equals(professionHat)
                    || "partial".equals(professionHat) && !"full".equals(typeHat);
            GlStateManager.enablePolygonOffset();
            try {
                GlStateManager.doPolygonOffset(-1.0F, -1.0F);
                model.villagerHead.showModel = showTypeHead;
                bindTexture(typeTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                model.render(villager, limbSwing, limbAmount, age, yaw, pitch, scale);
                model.villagerHead.showModel = true;
                if (villager.isChild() || "none".equals(profession)) return;
                GlStateManager.doPolygonOffset(-2.0F, -2.0F);
                bindTexture(professionTexture);
                model.render(villager, limbSwing, limbAmount, age, yaw, pitch, scale);
                if ("nitwit".equals(profession)) return;
                int level = Math.max(1, Math.min(5, state.getInteger("level")));
                GlStateManager.doPolygonOffset(-3.0F, -3.0F);
                bindTexture(texture("profession_level/" + BADGES[level - 1]));
                model.render(villager, limbSwing, limbAmount, age, yaw, pitch, scale);
            } finally {
                model.villagerHead.showModel = true;
                GlStateManager.doPolygonOffset(0.0F, 0.0F);
                GlStateManager.disablePolygonOffset();
            }
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

}
