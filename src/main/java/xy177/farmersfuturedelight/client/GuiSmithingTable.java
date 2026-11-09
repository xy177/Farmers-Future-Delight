package xy177.farmersfuturedelight.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerSmithingTable;

public final class GuiSmithingTable extends GuiContainer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            FarmerFutureDelight.MODID, "textures/gui/container/smithing.png");
    private static final ResourceLocation TEMPLATE_SLOT = new ResourceLocation(
            FarmerFutureDelight.MODID,
            "textures/gui/sprites/container/slot/smithing_template_netherite_upgrade.png");
    private static final ResourceLocation ERROR = new ResourceLocation(FarmerFutureDelight.MODID,
            "textures/gui/sprites/container/smithing/error.png");
    private final InventoryPlayer playerInventory;
    private final EntityArmorStand preview;

    public GuiSmithingTable(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerSmithingTable(playerInventory, world, pos));
        this.playerInventory = playerInventory;
        preview = new EntityArmorStand(world);
        NBTTagCompound data = new NBTTagCompound();
        data.setBoolean("ShowArms", true);
        data.setBoolean("NoBasePlate", true);
        preview.readEntityFromNBT(data);
        preview.renderYawOffset = 210.0F;
        preview.prevRenderYawOffset = 210.0F;
        preview.rotationYaw = 210.0F;
        preview.prevRotationYaw = 210.0F;
        preview.rotationYawHead = 210.0F;
        preview.prevRotationYawHead = 210.0F;
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.farmers_future_delight.upgrade"), 44, 15, 4210752);
        fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(),
                8, ySize - 94, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        if (!inventorySlots.getSlot(0).getHasStack()) {
            mc.getTextureManager().bindTexture(TEMPLATE_SLOT);
            drawModalRectWithCustomSizedTexture(guiLeft + 8, guiTop + 48,
                    0, 0, 16, 16, 16, 16);
        }
        if (hasRecipeError()) {
            mc.getTextureManager().bindTexture(ERROR);
            drawModalRectWithCustomSizedTexture(guiLeft + 65, guiTop + 46,
                    0, 0, 28, 21, 28, 21);
        }
        drawPreview();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    private boolean hasRecipeError() {
        return inventorySlots.getSlot(0).getHasStack() && inventorySlots.getSlot(1).getHasStack()
                && inventorySlots.getSlot(2).getHasStack() && !inventorySlots.getSlot(3).getHasStack();
    }

    private void drawPreview() {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            preview.setItemStackToSlot(slot, ItemStack.EMPTY);
        }
        ItemStack result = inventorySlots.getSlot(3).getStack();
        if (!result.isEmpty()) {
            EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(result);
            preview.setItemStackToSlot(slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR
                    ? slot : EntityEquipmentSlot.OFFHAND, result.copy());
        }
        GlStateManager.enableColorMaterial();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        previewLight(GL11.GL_LIGHT0, new Vec3d(0.2D, -1.0D, 1.0D));
        previewLight(GL11.GL_LIGHT1, new Vec3d(-0.2D, -1.0D, 0.0D));
        float brightnessX = OpenGlHelper.lastBrightnessX;
        float brightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft + 141.0F, guiTop + 75.0F, 50.0F);
        GlStateManager.scale(25.0F, 25.0F, -25.0F);
        GlStateManager.rotate(25.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        RenderManager manager = mc.getRenderManager();
        boolean shadows = manager.isRenderShadow();
        float viewYaw = manager.playerViewY;
        manager.setPlayerViewY(180.0F);
        manager.setRenderShadow(false);
        manager.renderEntity(preview, 0.0D, 0.0D, 0.0D, 210.0F, 1.0F, false);
        manager.setRenderShadow(shadows);
        manager.setPlayerViewY(viewYaw);
        GlStateManager.popMatrix();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightnessX, brightnessY);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void previewLight(int light, Vec3d direction) {
        Vec3d normal = direction.normalize();
        java.nio.FloatBuffer position = BufferUtils.createFloatBuffer(4);
        position.put(new float[]{(float) normal.x, (float) normal.y, (float) normal.z, 0.0F});
        position.flip();
        GL11.glLight(light, GL11.GL_POSITION, position);
    }
}
