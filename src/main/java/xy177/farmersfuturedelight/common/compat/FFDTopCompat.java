package xy177.farmersfuturedelight.common.compat;

import java.util.function.Function;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.block.BlockBeehive;
import xy177.farmersfuturedelight.common.tile.TileEntityBeehive;

/** Loaded through TOP IMC only when The One Probe is installed. */
public final class FFDTopCompat implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return FarmerFutureDelight.MODID + ":beehive";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player,
                             World world, IBlockState blockState, IProbeHitData data) {
        if (!(blockState.getBlock() instanceof BlockBeehive)) {
            return;
        }
        TileEntity tile = world.getTileEntity(data.getPos());
        if (!(tile instanceof TileEntityBeehive)) {
            return;
        }
        // Honey is stored in the tile entity, not the block metadata in 1.12.2.
        TileEntityBeehive hive = (TileEntityBeehive) tile;
        int honey = hive.getHoneyLevel();
        probeInfo.progress(honey, TileEntityBeehive.MAX_HONEY_LEVEL,
                probeInfo.defaultProgressStyle()
                        .width(120).height(12)
                        .borderColor(0xFF5C421B).backgroundColor(0xFF242424)
                        .filledColor(0xFF996515).alternateFilledColor(0xFF996515)
                        .showText(true).numberFormat(NumberFormat.NONE)
                        .prefix(new TextComponentTranslation("ffd.probe.honey",
                                fraction(honey, TileEntityBeehive.MAX_HONEY_LEVEL)))
                        .suffix(""));
        probeInfo.text(new TextComponentTranslation("ffd.probe.bees",
                fraction(hive.getOccupantCount(), TileEntityBeehive.MAX_BEES)));
    }

    private static ITextComponent fraction(int current, int max) {
        return new TextComponentString(current + "/" + max).setStyle(
                new Style().setColor(current >= max ? TextFormatting.GREEN : TextFormatting.WHITE));
    }
}
