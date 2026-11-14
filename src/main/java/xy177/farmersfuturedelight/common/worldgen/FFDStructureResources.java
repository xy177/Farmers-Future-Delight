package xy177.farmersfuturedelight.common.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class FFDStructureResources {
    private static final int MAX_JSON_BYTES = 1048576;

    private FFDStructureResources() {
    }

    static JsonObject readJson(InputStream input, String oversizedMessage) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer, 0, Math.min(buffer.length, MAX_JSON_BYTES + 1 - output.size()))) != -1) {
            if (output.size() + count > MAX_JSON_BYTES) throw new IllegalArgumentException(oversizedMessage);
            output.write(buffer, 0, count);
        }
        return new JsonParser().parse(new String(output.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
