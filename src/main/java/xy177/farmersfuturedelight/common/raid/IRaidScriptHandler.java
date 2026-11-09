package xy177.farmersfuturedelight.common.raid;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@ZenClass("mods.farmersfuturedelight.IRaidHandler")
@FunctionalInterface
public interface IRaidScriptHandler {
    void handle(ScriptRaidEvent event);
}
