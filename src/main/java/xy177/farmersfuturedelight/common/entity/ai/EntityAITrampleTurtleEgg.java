package xy177.farmersfuturedelight.common.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import xy177.farmersfuturedelight.common.registry.FFDBlocks;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityAITrampleTurtleEgg extends EntityAIMoveToBlock {
    private static final int DESTROY_TICKS = 60;

    private final EntityZombie zombie;
    private int ticksSinceReachedGoal;

    public EntityAITrampleTurtleEgg(EntityZombie zombie) {
        super(zombie, 1.0D, 3);
        this.zombie = zombie;
    }

    @Override
    public boolean shouldExecute() {
        return ForgeEventFactory.getMobGriefingEvent(zombie.world, zombie) && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return ForgeEventFactory.getMobGriefingEvent(zombie.world, zombie)
                && super.shouldContinueExecuting();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        ticksSinceReachedGoal = 0;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        zombie.fallDistance = 1.0F;
        ticksSinceReachedGoal = 0;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        if (!getIsAboveDestination()) {
            return;
        }

        if (ticksSinceReachedGoal > 0) {
            zombie.motionY = 0.3D;
            spawnEggParticles();
        }
        if (ticksSinceReachedGoal % 2 == 0) {
            zombie.motionY = -0.3D;
            if (ticksSinceReachedGoal % 6 == 0) {
                zombie.world.playSound(null, destinationBlock, FFDSounds.ZOMBIE_DESTROY_EGG,
                        SoundCategory.HOSTILE, 0.5F, 0.9F + zombie.getRNG().nextFloat() * 0.2F);
            }
        }
        Block turtleEgg = FFDItems.effectiveBlock(FFDBlocks.TURTLE_EGG);
        if (ticksSinceReachedGoal++ > DESTROY_TICKS && turtleEgg != null
                && zombie.world.getBlockState(destinationBlock).getBlock() == turtleEgg) {
            zombie.world.setBlockToAir(destinationBlock);
            zombie.world.playSound(null, destinationBlock, FFDSounds.TURTLE_EGG_BREAK,
                    SoundCategory.BLOCKS, 0.7F, 0.9F + zombie.getRNG().nextFloat() * 0.2F);
        }
    }

    @Override
    protected boolean shouldMoveTo(World world, BlockPos pos) {
        Block turtleEgg = FFDItems.effectiveBlock(FFDBlocks.TURTLE_EGG);
        return turtleEgg != null && world.getBlockState(pos).getBlock() == turtleEgg
                && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2));
    }

    private void spawnEggParticles() {
        for (int i = 0; i < 3; i++) {
            zombie.world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                    destinationBlock.getX() + 0.5D,
                    destinationBlock.getY() + 0.7D,
                    destinationBlock.getZ() + 0.5D,
                    (zombie.getRNG().nextFloat() - 0.5F) * 0.08F,
                    (zombie.getRNG().nextFloat() - 0.5F) * 0.08F,
                    (zombie.getRNG().nextFloat() - 0.5F) * 0.08F,
                    net.minecraft.item.Item.getIdFromItem(Items.EGG));
        }
    }
}
