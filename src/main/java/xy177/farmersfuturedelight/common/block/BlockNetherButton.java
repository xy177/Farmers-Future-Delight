package xy177.farmersfuturedelight.common.block;

import net.minecraft.block.BlockButtonWood;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherButton extends BlockButtonWood {
    public BlockNetherButton(String name) {
        super();
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(0.5F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    protected void playClickSound(@Nullable EntityPlayer player, World world, BlockPos pos) {
        world.playSound(player, pos, FFDSounds.NETHER_WOOD_BUTTON_CLICK_ON,
                SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    @Override
    protected void playReleaseSound(World world, BlockPos pos) {
        world.playSound(null, pos, FFDSounds.NETHER_WOOD_BUTTON_CLICK_OFF,
                SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
}
