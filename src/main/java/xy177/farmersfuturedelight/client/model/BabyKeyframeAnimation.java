package xy177.farmersfuturedelight.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

final class BabyKeyframeAnimation {
    private final JsonArray channels;
    private final float duration;
    private final boolean looping;

    BabyKeyframeAnimation(String name) {
        this("baby", name);
    }

    BabyKeyframeAnimation(String directory, String name) {
        try (InputStream stream = BabyKeyframeAnimation.class.getResourceAsStream(
                "/assets/farmers_future_delight/animations/entity/" + directory + "/" + name + ".json")) {
            if (stream == null) throw new IllegalArgumentException("Missing entity animation: " + name);
            JsonObject data = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            channels = data.getAsJsonArray("channels");
            duration = data.get("length").getAsFloat();
            looping = data.has("looping") && data.get("looping").getAsBoolean();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to load baby animation: " + name, exception);
        }
    }

    void apply(ModelModernBaby model, float elapsedTicks) {
        float time = Math.max(0.0F, elapsedTicks / 20.0F);
        time = looping && duration > 0.0F ? time % duration : Math.min(time, duration);
        for (int channelIndex = 0; channelIndex < channels.size(); channelIndex++) {
            JsonObject channel = channels.get(channelIndex).getAsJsonObject();
            JsonArray frames = channel.getAsJsonArray("frames");
            int next = 0;
            while (next < frames.size() - 1 && time > frame(frames, next).get("time").getAsFloat()) next++;
            int previous = Math.max(0, next - 1);
            JsonObject start = frame(frames, previous);
            JsonObject end = frame(frames, next);
            float span = end.get("time").getAsFloat() - start.get("time").getAsFloat();
            float blend = span == 0.0F ? 0.0F : MathHelper.clamp((time - start.get("time").getAsFloat()) / span, 0.0F, 1.0F);
            ModelRenderer part = model.part(channel.get("part").getAsString());
            String target = channel.get("target").getAsString();
            for (int axis = 0; axis < 3; axis++) {
                float from = value(start, "post", axis);
                float to = value(end, "pre", axis);
                float result;
                if (end.get("cubic").getAsBoolean()) {
                    float before = value(frame(frames, Math.max(0, previous - 1)), "post", axis);
                    float after = value(frame(frames, Math.min(frames.size() - 1, next + 1)), "pre", axis);
                    result = 0.5F * (2.0F * from + (to - before) * blend
                            + (2.0F * before - 5.0F * from + 4.0F * to - after) * blend * blend
                            + (3.0F * from - before - 3.0F * to + after) * blend * blend * blend);
                } else result = from + (to - from) * blend;
                if ("rotation".equals(target)) {
                    if (axis == 0) part.rotateAngleX += result;
                    else if (axis == 1) part.rotateAngleY += result;
                    else part.rotateAngleZ += result;
                } else if ("position".equals(target)) {
                    if (axis == 0) part.rotationPointX += result;
                    else if (axis == 1) part.rotationPointY += result;
                    else part.rotationPointZ += result;
                }
            }
        }
    }

    private static JsonObject frame(JsonArray frames, int index) {
        return frames.get(index).getAsJsonObject();
    }

    private static float value(JsonObject frame, String side, int axis) {
        return frame.getAsJsonArray(side).get(axis).getAsFloat();
    }
}
