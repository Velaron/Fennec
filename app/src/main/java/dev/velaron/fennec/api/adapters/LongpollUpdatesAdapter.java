package dev.velaron.fennec.api.adapters;

import static dev.velaron.fennec.util.Objects.nonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.velaron.fennec.api.model.longpoll.AbsLongpollEvent;
import dev.velaron.fennec.api.model.longpoll.VkApiLongpollUpdates;
import dev.velaron.fennec.util.Logger;

/**
 * Created by ruslan.kolbasa on 23.12.2016.
 * phoenix
 */
public class LongpollUpdatesAdapter extends AbsAdapter implements JsonDeserializer<VkApiLongpollUpdates> {

    private static final String TAG = LongpollUpdatesAdapter.class.getSimpleName();

    @Override
    public VkApiLongpollUpdates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VkApiLongpollUpdates updates = new VkApiLongpollUpdates();

        updates.failed = optInt(root, "failed");
        updates.ts = optLong(root, "ts");

        JsonArray array = root.getAsJsonArray("updates");

        if (nonNull(array)) {
            for (int i = 0; i < array.size(); i++) {
                JsonArray updateArray = array.get(i).getAsJsonArray();

                AbsLongpollEvent event = context.deserialize(updateArray, AbsLongpollEvent.class);

                if (nonNull(event)) {
                    updates.putUpdate(event);
                } else {
                    Logger.d(TAG, "Unhandled Longpoll event: array: " + updateArray);
                }
            }
        }

        return updates;
    }
}
