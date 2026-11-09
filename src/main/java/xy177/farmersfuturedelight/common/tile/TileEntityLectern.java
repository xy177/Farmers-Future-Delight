package xy177.farmersfuturedelight.common.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.block.BlockLectern;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class TileEntityLectern extends TileEntity {
    private ItemStack book = ItemStack.EMPTY;
    private int page;
    private int pageCount = 1;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public ItemStack getBook() {
        return book;
    }

    public void setBook(ItemStack stack) {
        setBook(stack, null);
    }

    public void setBook(ItemStack stack, @javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player) {
        book = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!book.isEmpty()) {
            book.setCount(1);
        }
        resolveBook(player);
        page = 0;
        pageCount = getPageCount(book);
        if (world != null && !world.isRemote) {
            BlockPos pos = getPos();
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockLectern) {
                world.setBlockState(pos, state.withProperty(BlockLectern.HAS_BOOK,
                        !book.isEmpty()).withProperty(BlockLectern.POWERED, false), 3);
                world.notifyNeighborsOfStateChange(pos.down(), state.getBlock(), false);
                world.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
        markDirty();
        notifyUpdate();
    }

    public ItemStack removeBook() {
        return removeBook(true);
    }

    public ItemStack removeBook(boolean updateState) {
        ItemStack removed = book;
        book = ItemStack.EMPTY;
        page = 0;
        pageCount = 1;
        if (updateState && world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockLectern) {
                world.setBlockState(pos, state.withProperty(BlockLectern.HAS_BOOK, false)
                        .withProperty(BlockLectern.POWERED, false), 3);
                world.notifyNeighborsOfStateChange(pos.down(), state.getBlock(), false);
                world.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
        markDirty();
        notifyUpdate();
        return removed;
    }

    public int getPage() {
        return page;
    }

    @Override
    public void onLoad() {
        if (resolveBook(null)) {
            markDirty();
            notifyUpdate();
        }
    }

    private boolean resolveBook(@javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player) {
        if (world == null || world.isRemote || book.getItem() != net.minecraft.init.Items.WRITTEN_BOOK
                || !book.hasTagCompound() || book.getTagCompound().getBoolean("resolved")) {
            return false;
        }
        NBTTagCompound tag = book.getTagCompound();
        tag.setBoolean("resolved", true);
        if (!net.minecraft.item.ItemWrittenBook.validBookTagContents(tag)) return true;
        net.minecraft.command.ICommandSender delegate = player == null ? world.getMinecraftServer() : player;
        net.minecraft.command.CommandSenderWrapper sender = new net.minecraft.command.CommandSenderWrapper(
                delegate, new net.minecraft.util.math.Vec3d(pos).addVector(0.5D, 0.5D, 0.5D),
                pos, 2, player, false);
        NBTTagList pages = tag.getTagList("pages", 8);
        for (int index = 0; index < pages.tagCount(); index++) {
            String text = pages.getStringTagAt(index);
            net.minecraft.util.text.ITextComponent component;
            try {
                component = net.minecraft.util.text.ITextComponent.Serializer.fromJsonLenient(text);
                component = net.minecraft.util.text.TextComponentUtils.processComponent(sender, component, player);
            } catch (Exception exception) {
                component = new net.minecraft.util.text.TextComponentString(text);
            }
            pages.set(index, new net.minecraft.nbt.NBTTagString(
                    net.minecraft.util.text.ITextComponent.Serializer.componentToJson(component)));
        }
        tag.setTag("pages", pages);
        return true;
    }

    public void setPage(int requestedPage) {
        int nextPage = MathHelper.clamp(requestedPage, 0, Math.max(0, pageCount - 1));
        if (nextPage == page || book.isEmpty()) {
            return;
        }
        page = nextPage;
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockLectern) {
                world.setBlockState(pos, state.withProperty(BlockLectern.POWERED, true), 3);
                world.notifyNeighborsOfStateChange(pos.down(), state.getBlock(), false);
                world.scheduleUpdate(pos, state.getBlock(), 2);
                world.playSound(null, pos, FFDSounds.BOOK_PAGE_TURN,
                        net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
        markDirty();
        notifyUpdate();
    }

    public int getRedstoneSignal() {
        if (book.isEmpty()) {
            return 0;
        }
        float progress = pageCount > 1 ? (float) page / (pageCount - 1) : 1.0F;
        return MathHelper.floor(progress * 14.0F) + 1;
    }

    public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!book.isEmpty()) {
            compound.setTag("Book", book.writeToNBT(new NBTTagCompound()));
            compound.setInteger("Page", page);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        book = compound.hasKey("Book", 10)
                ? new ItemStack(compound.getCompoundTag("Book")) : ItemStack.EMPTY;
        pageCount = getPageCount(book);
        page = MathHelper.clamp(compound.getInteger("Page"), 0, Math.max(0, pageCount - 1));
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    private void notifyUpdate() {
        if (world != null) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    private static int getPageCount(ItemStack book) {
        if (book.isEmpty() || !book.hasTagCompound()) {
            return 1;
        }
        NBTTagList pages = book.getTagCompound().getTagList("pages", 8);
        return Math.max(1, pages.tagCount());
    }
}
