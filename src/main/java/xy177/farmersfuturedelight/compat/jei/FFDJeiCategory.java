package xy177.farmersfuturedelight.compat.jei;

import java.util.Locale;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import xy177.farmersfuturedelight.FarmerFutureDelight;

public final class FFDJeiCategory implements IRecipeCategory<FFDJeiRecipe> {
    final String name;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private final IGuiHelper helper;
    private final mezz.jei.api.gui.IDrawableStatic progressTexture;
    private final IDrawableAnimated flame;

    FFDJeiCategory(IGuiHelper helper, String name, ItemStack machine) {
        this.name = name;
        this.helper = helper;
        int width = "trades".equals(name) ? 180 : "composting".equals(name) ? 120
                : "smithing".equals(name) ? 108
                : "loom".equals(name) || "professions".equals(name) ? 144 : 82;
        int height = "trades".equals(name) ? 118 : "professions".equals(name) ? 60
                : "composting".equals(name) ? 18 : "smithing".equals(name) ? 28
                : "stonecutting".equals(name) ? 34 : "campfire_cooking".equals(name) ? 44
                : "loom".equals(name) ? 60 : 54;
        background = helper.createBlankDrawable(width, height);
        icon = helper.createDrawableIngredient(machine);
        slot = helper.getSlotDrawable();
        ResourceLocation furnace = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
        arrow = helper.createDrawable(furnace, 79, 34, 24, 17);
        progressTexture = helper.createDrawable(furnace, 176, 14, 24, 17);
        flame = helper.createAnimatedDrawable(helper.createDrawable(furnace, 176, 0, 14, 14),
                300, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public String getUid() {
        return FarmerFutureDelight.MODID + ":" + name;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.farmers_future_delight." + name.toLowerCase(Locale.ROOT));
    }

    @Override
    public String getModName() {
        return FarmerFutureDelight.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    private boolean isCooking() {
        return "smoking".equals(name) || "blasting".equals(name) || "campfire_cooking".equals(name);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        if (!"trades".equals(name) && !"composting".equals(name) && !"professions".equals(name)) {
            int arrowX = "smithing".equals(name) ? 60 : "loom".equals(name) ? 53 : 26;
            int arrowY = "smithing".equals(name) ? 6 : "campfire_cooking".equals(name) ? 7
                    : "stonecutting".equals(name) ? 9 : "loom".equals(name) ? 23 : 17;
            arrow.draw(minecraft, arrowX, arrowY);
            if (isCooking()) {
                flame.draw(minecraft, 1, "campfire_cooking".equals(name) ? 28 : 20);
                if (!"campfire_cooking".equals(name)) slot.draw(minecraft, 0, 36);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayout layout, FFDJeiRecipe recipe, IIngredients ingredients) {
        if ("trades".equals(name)) recipe.tradeArrow = arrow;
        if (isCooking() && recipe.cookingProgress == null) {
            recipe.cookingProgress = helper.createAnimatedDrawable(progressTexture,
                    Math.max(1, recipe.processingTime), IDrawableAnimated.StartDirection.LEFT, false);
        }
        IGuiItemStackGroup stacks = layout.getItemStacks();
        int inputCount = recipe.inputs.size();
        for (int index = 0; index < inputCount; index++) {
            int x;
            int y;
            if ("trades".equals(name)) {
                boolean header = recipe.headerStart >= 0 && index >= recipe.headerStart;
                x = header ? 4 + (index - recipe.headerStart) * 20 : index % 2 == 0 ? 4 : 29;
                y = header ? 0 : 22 + index / 2 * 23;
            } else if ("smithing".equals(name)) {
                x = index * 18;
                y = 5;
            } else {
                x = "loom".equals(name) ? 15 : 0;
                y = "loom".equals(name) ? 20
                        : "campfire_cooking".equals(name) || "stonecutting".equals(name) ? 8 : 0;
            }
            stacks.init(index, true, x, y);
            stacks.setBackground(index, slot);
            stacks.set(index, ingredients.getInputs(VanillaTypes.ITEM).get(index));
        }
        for (int index = 0; index < recipe.outputs.size(); index++) {
            if ("loom".equals(name)) continue;
            int x = "trades".equals(name) ? 95 : "professions".equals(name) ? 102
                    : "smithing".equals(name) ? 90 : 60;
            int y = "trades".equals(name) ? 22 + index * 23 : "professions".equals(name) ? 29
                    : "smithing".equals(name) ? 5
                    : "campfire_cooking".equals(name) || "stonecutting".equals(name) ? 8 : 18;
            stacks.init(inputCount + index, false, x, y);
            stacks.setBackground(inputCount + index, slot);
            stacks.set(inputCount + index, ingredients.getOutputs(VanillaTypes.ITEM).get(index));
        }
    }
}
