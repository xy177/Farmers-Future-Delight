package xy177.farmersfuturedelight.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import xy177.farmersfuturedelight.FarmerFutureDelight;
import xy177.farmersfuturedelight.common.FFDCreativeTab;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class BlockNetherTrapDoor extends BlockTrapDoor {
    public BlockNetherTrapDoor(String name) {
        super(Material.WOOD);
        setRegistryName(FarmerFutureDelight.MODID, name);
        setUnlocalizedName(FarmerFutureDelight.MODID + "." + name);
        setCreativeTab(FFDCreativeTab.INSTANCE);
        setHardness(3.0F);
        setResistance(3.0F);
        setSoundType(FFDSounds.NETHER_WOOD);
    }

    @Override
    protected void playSound(@Nullable EntityPlayer player, World world, BlockPos pos, boolean open) {
        world.playSound(player, pos,
                open ? FFDSounds.NETHER_WOOD_TRAPDOOR_OPEN
                        : FFDSounds.NETHER_WOOD_TRAPDOOR_CLOSE,
                SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
    }
}
