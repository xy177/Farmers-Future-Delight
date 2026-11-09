package xy177.farmersfuturedelight.client;

import java.io.IOException;
import java.lang.reflect.Method;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import xy177.farmersfuturedelight.common.inventory.ContainerLectern;

public class GuiLecternBook extends GuiContainer {
    private static final Method UPDATE_BUTTONS = ReflectionHelper.findMethod(
            GuiScreenBook.class, "updateButtons", "func_146464_h");
    private final EntityPlayer player;
    private final ContainerLectern container;
    private ItemStack displayedBook = ItemStack.EMPTY;
    private BookView view;

    public GuiLecternBook(EntityPlayer player, ContainerLectern container) {
        super(container);
        this.player = player;
        this.container = container;
    }

    @Override
    public void initGui() {
        super.initGui();
        view = null;
        updateBook();
    }

    private void updateBook() {
        if (view == null || !ItemStack.areItemStacksEqual(displayedBook, container.getBook())) {
            displayedBook = container.getBook().copy();
            view = new BookView(displayedBook);
            view.setWorldAndResolution(mc, width, height);
        }
        view.setPage(container.getPage());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateBook();
        view.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        updateBook();
        view.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        view.click(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        if (view != null) {
            view.onGuiClosed();
        }
        super.onGuiClosed();
    }

    private void sendButton(int button) {
        mc.playerController.sendEnchantPacket(container.windowId, button);
    }

    private class BookView extends GuiScreenBook {
        private BookView(ItemStack book) {
            super(player, readableBook(book), false);
        }

        @Override
        public void initGui() {
            super.initGui();
            if (player.capabilities.allowEdit) {
                GuiButton done = null;
                for (GuiButton button : buttonList) {
                    if (button.id == 0) {
                        done = button;
                        break;
                    }
                }
                if (done != null) {
                    done.width = 98;
                    done.x = width / 2 - 100;
                    addButton(new GuiButton(3, width / 2 + 2, done.y,
                            98, 20, I18n.format("lectern.take_book")));
                }
            }
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (!button.enabled) {
                return;
            }
            if (button.id == 0) {
                mc.player.closeScreen();
            } else if (button.id == 1) {
                sendButton(2);
            } else if (button.id == 2) {
                sendButton(1);
            } else if (button.id == 3) {
                sendButton(3);
            }
        }

        @Override
        public boolean handleComponentClick(ITextComponent component) {
            boolean handled = super.handleComponentClick(component);
            int page = ObfuscationReflectionHelper.getPrivateValue(
                    GuiScreenBook.class, this, "field_146484_x");
            if (page != container.getPage()) {
                sendButton(100 + page);
            }
            return handled;
        }

        private void setPage(int page) {
            ObfuscationReflectionHelper.setPrivateValue(GuiScreenBook.class, this,
                    page, "field_146484_x");
            try {
                UPDATE_BUTTONS.invoke(this);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot update lectern page controls", exception);
            }
        }

        private void click(int mouseX, int mouseY, int button) throws IOException {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private static ItemStack readableBook(ItemStack book) {
        if (book.getItem() != Items.WRITABLE_BOOK) {
            return book;
        }
        ItemStack readable = new ItemStack(Items.WRITTEN_BOOK);
        NBTTagCompound tag = book.hasTagCompound() ? book.getTagCompound().copy()
                : new NBTTagCompound();
        NBTTagList sourcePages = tag.getTagList("pages", 8);
        NBTTagList pages = new NBTTagList();
        for (int index = 0; index < sourcePages.tagCount(); index++) {
            pages.appendTag(new NBTTagString(ITextComponent.Serializer.componentToJson(
                    new TextComponentString(sourcePages.getStringTagAt(index)))));
        }
        tag.setTag("pages", pages);
        tag.setString("title", "");
        tag.setString("author", "");
        readable.setTagCompound(tag);
        return readable;
    }
}
