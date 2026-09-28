package xy177.farmersfuturedelight.common.entity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import xy177.farmersfuturedelight.common.registry.FFDItems;
import xy177.farmersfuturedelight.common.registry.FFDSounds;

public class EntityCod extends EntitySchoolingFish {
    public EntityCod(World world) {
        super(world);
        setSize(0.5F, 0.3F);
    }

    @Override
    protected ItemStack getBucketStack() {
        return FFDItems.effectiveStack(FFDItems.COD_BUCKET);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        entityDropItem(new ItemStack(isBurning() ? Items.COOKED_FISH : Items.FISH, 1, 0), 0.0F);
        if (rand.nextFloat() < 0.05F) {
            entityDropItem(new ItemStack(Items.DYE, 1, 15), 0.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FFDSounds.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FFDSounds.COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FFDSounds.COD_DEATH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return FFDSounds.COD_FLOP;
    }
}
