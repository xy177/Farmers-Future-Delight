package xy177.farmersfuturedelight.common.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FFDWanderingTraderTrades {
    private static final Logger LOGGER = LogManager.getLogger("FFD Wandering Trader");
    private static final List<Group> GROUPS = new ArrayList<>();
    private static boolean loaded;

    private FFDWanderingTraderTrades() {
    }

    public static synchronized MerchantRecipeList create(Random random) {
        load();
        MerchantRecipeList result = new MerchantRecipeList();
        for (Group group : GROUPS) {
            List<JsonObject> candidates = new ArrayList<>(group.trades);
            candidates = FFDTradeConfiguration.weightedOrder(candidates, random);
            int count = 0;
            for (JsonObject definition : candidates) {
                MerchantRecipe trade = createTrade(definition, random);
                if (trade != null) {
                    result.add(trade);
                    if (++count >= group.amount) break;
                }
            }
        }
        return result;
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        String path = "/assets/farmers_future_delight/trades/wandering_trader_defaults.json";
        try (InputStream stream = FFDWanderingTraderTrades.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing " + path);
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement element : root.getAsJsonArray("groups")) {
                JsonObject groupDefinition = element.getAsJsonObject();
                Group group = new Group(groupDefinition.get("amount").getAsInt());
                for (JsonElement tradeElement : groupDefinition.getAsJsonArray("trades")) {
                    JsonObject trade = tradeElement.getAsJsonObject();
                    JsonObject definition = trade.getAsJsonObject("definition");
                    if (trade.has("weight") && !definition.has("weight")) {
                        definition.add("weight", trade.get("weight"));
                    }
                    ItemStack input = stack(definition.getAsJsonObject("wants"));
                    ItemStack output = stack(definition.getAsJsonObject("gives"));
                    if (input.isEmpty() || output.isEmpty()) {
                        LOGGER.debug("Unavailable trader offer {}: item outside the active content registry", trade.get("id"));
                        continue;
                    }
                    group.trades.add(definition);
                }
                GROUPS.add(group);
            }
        } catch (Exception exception) {
            GROUPS.clear();
            LOGGER.error("Cannot load wandering trader defaults", exception);
        }
    }

    private static ItemStack stack(JsonObject definition) {
        return FFDTradeItemModifiers.stack(definition);
    }

    private static MerchantRecipe createTrade(JsonObject definition, Random random) {
        return FFDTradeItemModifiers.create(definition, random, false);
    }

    private static final class Group {
        private final int amount;
        private final List<JsonObject> trades = new ArrayList<>();

        private Group(int amount) {
            this.amount = amount;
        }
    }

    public static List<JsonObject> getDisplayOffers() {
        load();
        List<JsonObject> result = new ArrayList<>();
        for (Group group : GROUPS) {
            for (JsonObject offer : group.trades) {
                result.add(new JsonParser().parse(offer.toString()).getAsJsonObject());
            }
        }
        return Collections.unmodifiableList(result);
    }
}
