package xy177.farmersfuturedelight.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public abstract class FFDDeferredCreativeTab extends CreativeTabs {
    private static final Field TAB_INDEX = ObfuscationReflectionHelper.findField(CreativeTabs.class, "field_78033_n");

    protected FFDDeferredCreativeTab(String label) {
        super(label);
        CREATIVE_TAB_ARRAY = Arrays.copyOf(CREATIVE_TAB_ARRAY, CREATIVE_TAB_ARRAY.length - 1);
        setIndex(this, -1);
    }

    public final void setRegistered(boolean registered) {
        List<CreativeTabs> tabs = new ArrayList<>(Arrays.asList(CREATIVE_TAB_ARRAY));
        boolean present = tabs.contains(this);
        if (present == registered) return;
        if (registered) tabs.add(this);
        else tabs.remove(this);
        for (int index = 0; index < tabs.size(); index++) {
            CreativeTabs tab = tabs.get(index);
            if (tab != null && tab.getTabIndex() != index) setIndex(tab, index);
        }
        if (!registered) setIndex(this, -1);
        CREATIVE_TAB_ARRAY = tabs.toArray(new CreativeTabs[0]);
    }

    private static void setIndex(CreativeTabs tab, int index) {
        try {
            TAB_INDEX.setInt(tab, index);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to update creative tab registration", exception);
        }
    }
}
