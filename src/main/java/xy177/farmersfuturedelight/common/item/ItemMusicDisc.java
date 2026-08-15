package xy177.farmersfuturedelight.common.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;
import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;

public final class ItemMusicDisc extends ItemRecord {
    public ItemMusicDisc(String name, SoundEvent sound) {
        super(name, sound);
        setRegistryName(FarmerFutureDelight.MODID, "music_disc_" + name);
        setUnlocalizedName(FarmerFutureDelight.MODID + ".music_disc_" + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
    }
}
