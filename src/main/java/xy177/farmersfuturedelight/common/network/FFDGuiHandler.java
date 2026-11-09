package xy177.farmersfuturedelight.common.network;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.inventory.ContainerCookingFurnace;
import xy177.farmersfuturedelight.common.inventory.ContainerStonecutter;
import xy177.farmersfuturedelight.common.inventory.ContainerSmithingTable;
import xy177.farmersfuturedelight.common.inventory.ContainerGrindstone;
import xy177.farmersfuturedelight.common.inventory.ContainerCartographyTable;
import xy177.farmersfuturedelight.common.inventory.ContainerLoom;
import xy177.farmersfuturedelight.common.inventory.ContainerLectern;
import xy177.farmersfuturedelight.common.tile.TileEntityLectern;
import xy177.farmersfuturedelight.common.tile.TileEntityCookingFurnace;
import xy177.farmersfuturedelight.common.tile.TileEntityStonecutter;
import xy177.farmersfuturedelight.common.tile.TileEntityGrindstone;
import xy177.farmersfuturedelight.common.tile.TileEntityCartographyTable;

public class FFDGuiHandler implements IGuiHandler {
    public static final int COOKING_FURNACE = 0;
    public static final int STONECUTTER = 1;
    public static final int GRINDSTONE = 2;
    public static final int CARTOGRAPHY_TABLE = 3;
    public static final int LOOM = 4;
    public static final int LECTERN = 5;
    public static final int SMITHING_TABLE = 6;

    @Override
    @Nullable
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == COOKING_FURNACE && tile instanceof TileEntityCookingFurnace
                && ((TileEntityCookingFurnace) tile).isUsableByPlayer(player)) {
            return new ContainerCookingFurnace(player.inventory, (TileEntityCookingFurnace) tile);
        }
        if (id == STONECUTTER && tile instanceof TileEntityStonecutter
                && ((TileEntityStonecutter) tile).isUsableByPlayer(player)) {
            return new ContainerStonecutter(player.inventory, (TileEntityStonecutter) tile);
        }
        if (id == GRINDSTONE && tile instanceof TileEntityGrindstone
                && ((TileEntityGrindstone) tile).isUsableByPlayer(player)) {
            return new ContainerGrindstone(player.inventory, (TileEntityGrindstone) tile);
        }
        if (id == CARTOGRAPHY_TABLE && tile instanceof TileEntityCartographyTable
                && ((TileEntityCartographyTable) tile).isUsableByPlayer(player)) {
            return new ContainerCartographyTable(player.inventory,
                    (TileEntityCartographyTable) tile);
        }
        if (id == LOOM) {
            return new ContainerLoom(player.inventory, world, new BlockPos(x, y, z));
        }
        if (id == LECTERN && tile instanceof TileEntityLectern
                && ((TileEntityLectern) tile).isUsableByPlayer(player)) {
            return new ContainerLectern((TileEntityLectern) tile);
        }
        if (id == SMITHING_TABLE) {
            ContainerSmithingTable container = new ContainerSmithingTable(player.inventory, world,
                    new BlockPos(x, y, z));
            return container.canInteractWith(player) ? container : null;
        }
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return FarmerFutureDelight.proxy.getWorkstationGui(id, player, world, new BlockPos(x, y, z));
    }
}
