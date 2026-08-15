package xy177.farmersfuturedelight.common.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;

public class BlockInfestedDeepslate extends BlockDeepslate {
    public BlockInfestedDeepslate() {
        super("infested_deepslate");
        setHardness(1.5F);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(net.minecraft.init.Blocks.AIR);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return FFDItems.effectiveStack(FFDItems.DEEPSLATE);
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state,
                                          float chance, int fortune) {
        if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
            EntitySilverfish silverfish = new EntitySilverfish(world);
            silverfish.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                    0.0F, 0.0F);
            world.spawnEntity(silverfish);
            silverfish.spawnExplosionParticle();
        }
    }
}
