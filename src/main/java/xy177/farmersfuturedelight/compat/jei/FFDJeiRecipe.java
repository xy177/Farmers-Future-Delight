package xy177.farmersfuturedelight.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public final class FFDJeiRecipe implements IRecipeWrapper {
    final List<List<ItemStack>> inputs = new ArrayList<>();
    final List<List<ItemStack>> outputs = new ArrayList<>();
    final List<String> rows = new ArrayList<>();
    int headerStart = -1;
    String heading = "";
    BannerPattern bannerPattern;
    VillagerProfession profession;
    int career;
    int seconds;
    int processingTime;
    mezz.jei.api.gui.IDrawableAnimated cookingProgress;
    mezz.jei.api.gui.IDrawable tradeArrow;
    float experience;
    double chance = -1.0D;
    private EntityVillager displayedVillager;

    public FFDJeiRecipe input(ItemStack stack) {
        inputs.add(stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack.copy()));
        return this;
    }

    public FFDJeiRecipe output(ItemStack stack) {
        outputs.add(stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack.copy()));
        return this;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int width, int height, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (cookingProgress != null) cookingProgress.draw(minecraft, 26, height == 44 ? 7 : 17);
        if (bannerPattern != null) {
            net.minecraft.util.ResourceLocation texture = BannerTextures.BANNER_DESIGNS.getResourceLocation(
                    "ffd_jei_" + bannerPattern.getHashname(),
                    java.util.Arrays.asList(BannerPattern.BASE, bannerPattern),
                    java.util.Arrays.asList(EnumDyeColor.WHITE, EnumDyeColor.BLACK));
            if (texture != null) {
                GlStateManager.disableLighting();
                minecraft.getTextureManager().bindTexture(texture);
                net.minecraft.client.gui.Gui.drawScaledCustomSizeModalRect(94, 8, 1, 1, 20, 40,
                        20, 40, 64, 64);
            }
        }
        if (profession != null && minecraft.world != null) {
            if (displayedVillager == null || displayedVillager.world != minecraft.world) {
                displayedVillager = new EntityVillager(minecraft.world);
                displayedVillager.setProfession(profession);
                ObfuscationReflectionHelper.setPrivateValue(EntityVillager.class, displayedVillager,
                        career + 1, "field_175563_bv");
                String id = String.valueOf(profession.getRegistryName());
                if (id.startsWith("minecraft:") || "farmers_future_delight:mason".equals(id)) {
                    NBTTagCompound state = new NBTTagCompound();
                    state.setBoolean("modern", true);
                    state.setString("type", "plains");
                    state.setString("profession", FFDJeiRecipes.modernCareer(profession.getCareer(career).getName()));
                    state.setInteger("level", 1);
                    displayedVillager.getEntityData().setTag(
                            xy177.farmersfuturedelight.common.registry.FFDVillagerTypes.CLIENT_STATE, state);
                }
            }
            GuiInventory.drawEntityOnScreen(24, 56, 18, 0, 0, displayedVillager);
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (!heading.isEmpty()) {
            int left = headerStart < 0 ? 2 : 5 + (inputs.size() - headerStart) * 20;
            minecraft.fontRenderer.drawString(minecraft.fontRenderer.trimStringToWidth(heading, width - left - 2),
                    left, 4, 0x404040);
        }
        for (int index = 0; index < rows.size(); index++) {
            if (tradeArrow != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                tradeArrow.draw(minecraft, 67, 22 + index * 23);
            }
            String[] lines = rows.get(index).split("\n");
            for (int line = 0; line < lines.length; line++) {
                minecraft.fontRenderer.drawString(lines[line], 118, 23 + index * 23 + line * 10, 0x606060);
            }
        }
        if (seconds > 0) {
            String time = I18n.format("jei.farmers_future_delight.cooking_time", seconds);
            minecraft.fontRenderer.drawString(time, Math.max(0, width - minecraft.fontRenderer.getStringWidth(time)),
                    height - 10, 0x808080);
        }
        if (experience > 0.0F) {
            String text = I18n.format("gui.jei.category.smelting.experience", experience);
            minecraft.fontRenderer.drawString(text, Math.max(0, width - minecraft.fontRenderer.getStringWidth(text)),
                    0, 0x808080);
        }
        if (chance >= 0.0D) {
            minecraft.fontRenderer.drawString(I18n.format("jei.farmers_future_delight.compost_chance",
                    String.format(java.util.Locale.ROOT, "%.0f", chance * 100.0D)), 24, 5, 0x404040);
        }
    }

}
