package xy177.farmersfuturedelight.common.raid;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfoServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import xy177.farmersfuturedelight.api.raid.RaidEventDefinition;

final class RaidBossBar {
    private static final Field LANGUAGE = ObfuscationReflectionHelper.findField(EntityPlayerMP.class, "field_71148_cg");
    private final RaidEventDefinition.BossBar style;
    private final Map<EntityPlayerMP, BossInfoServer> bars = new LinkedHashMap<>();
    private Function<String, ITextComponent> name;
    private boolean visible;
    private float percent = 1.0F;

    RaidBossBar(RaidEventDefinition.BossBar style) {
        this.style = style;
        name = language -> new TextComponentTranslation(style.getTitle());
    }

    private ITextComponent title(EntityPlayerMP player) {
        try {
            return name.apply((String) LANGUAGE.get(player));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read raid viewer language", exception);
        }
    }

    void addPlayer(EntityPlayerMP player) {
        BossInfoServer bar = bars.get(player);
        if (bar != null) {
            bar.setName(title(player));
            return;
        }
        bar = new BossInfoServer(title(player), style.getColor(), style.getOverlay());
        bar.setPercent(percent);
        bar.setVisible(visible);
        bars.put(player, bar);
        bar.addPlayer(player);
    }

    void removePlayer(EntityPlayerMP player) {
        BossInfoServer bar = bars.remove(player);
        if (bar != null) bar.removePlayer(player);
    }

    Collection<EntityPlayerMP> getPlayers() { return Collections.unmodifiableSet(bars.keySet()); }

    void setVisible(boolean value) {
        if (visible == value) return;
        visible = value;
        for (BossInfoServer bar : bars.values()) bar.setVisible(value);
    }

    void setName(Function<String, ITextComponent> value) {
        name = value;
        for (Map.Entry<EntityPlayerMP, BossInfoServer> entry : bars.entrySet()) {
            entry.getValue().setName(title(entry.getKey()));
        }
    }

    void setPercent(float value) {
        if (percent == value) return;
        percent = value;
        for (BossInfoServer bar : bars.values()) bar.setPercent(value);
    }
}
